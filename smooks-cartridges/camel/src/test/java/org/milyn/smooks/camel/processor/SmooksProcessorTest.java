/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.milyn.smooks.camel.processor;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spi.ManagementAgent;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.milyn.io.StreamUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Unit test for {@link SmooksProcessor}.
 * 
 * @author Christian Mueller
 * @author Daniel Bevenius
 */
public class SmooksProcessorTest extends CamelTestSupport
{
    @EndpointInject(uri = "mock:result")
    private MockEndpoint result;
    private MBeanServer mbeanServer;

    @BeforeClass
    public static void setup()
    {
        XMLUnit.setIgnoreWhitespace(true);
    }

    @Before
    public void getMbeanServer()
    {
        ManagementAgent managementAgent = context.getManagementStrategy().getManagementAgent();
        mbeanServer = managementAgent.getMBeanServer();
    }

    @Override
    protected boolean useJmx()
    {
        return true;
    }

    @Test
    public void process() throws Exception
    {
        assertOneProcessedMessage();
    }

    private void assertOneProcessedMessage() throws Exception
    {
        result.expectedMessageCount(1);

        assertMockEndpointsSatisfied();

        Exchange exchange = result.assertExchangeReceived(0);
        assertIsInstanceOf(Document.class, exchange.getIn().getBody());
        assertXMLEqual(getExpectedOrderXml(), exchange.getIn().getBody(String.class));
    }

    @Test
    public void assertSmooksReportWasCreated() throws Exception
    {
        result.expectedMessageCount(1);
        assertMockEndpointsSatisfied();

        File report = new File("target/smooks-report.html");
        report.deleteOnExit();
        assertTrue("Smooks report was not generated.", report.exists());
    }

    @Test
    public void stopStartContext() throws Exception
    {
        ObjectInstance smooksProcessorMBean = getSmooksProcessorObjectInstance();

        assertOneProcessedMessage();
        stopSmooksProcessor(smooksProcessorMBean.getObjectName());
        Thread.sleep(500);

        startSmooksProcessor(smooksProcessorMBean.getObjectName());
        Thread.sleep(500);

        assertOneProcessedMessage();
    }

    private void stopSmooksProcessor(ObjectName objectName) throws Exception
    {
        invokeVoidNoArgsMethod(objectName, "stop");
    }

    private void invokeVoidNoArgsMethod(ObjectName objectName, String methodName) throws Exception
    {
        mbeanServer.invoke(objectName, methodName, null, null);
    }

    private void startSmooksProcessor(ObjectName objectName) throws Exception
    {
        invokeVoidNoArgsMethod(objectName, "start");
    }

    private ObjectInstance getSmooksProcessorObjectInstance() throws Exception
    {
        ObjectInstance mbean = null;
        Set<ObjectInstance> queryMBeans = mbeanServer.queryMBeans(new ObjectName("*:*,type=processors"), null);
        for (ObjectInstance objectInstance : queryMBeans)
        {
            if (objectInstance.getObjectName().toString().contains(SmooksProcessor.class.getSimpleName()))
            {
	            mbean = objectInstance;
            }
        }
        assertNotNull(mbean);
        return mbean;
    }

    protected RouteBuilder createRouteBuilder() throws Exception
    {
        return new RouteBuilder()
        {
            public void configure() throws Exception
            {
                SmooksProcessor processor = new SmooksProcessor("edi-to-xml-smooks-config.xml", context);
                processor.setReportPath("target/smooks-report.html");

                from("file://src/test/data?noop=true").process(processor).convertBodyTo(Node.class).to("mock:result");
            }
        };
    }

    private String getExpectedOrderXml() throws IOException
    {
        return StreamUtils.readStream(new InputStreamReader(getClass().getResourceAsStream("/xml/expected-order.xml")));
    }
}