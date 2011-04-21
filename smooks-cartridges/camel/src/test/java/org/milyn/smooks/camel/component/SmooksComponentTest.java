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
package org.milyn.smooks.camel.component;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

import java.io.InputStreamReader;
import java.io.StringReader;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.milyn.delivery.Filter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Unit test for {@link SmooksComponent}.
 * 
 * @author Christian Mueller
 * @author Daniel Bevenius
 * 
 */
public class SmooksComponentTest extends CamelTestSupport
{
    @EndpointInject(uri = "mock:result")
    private MockEndpoint result;

    @BeforeClass
    public static void setup()
    {
        XMLUnit.setIgnoreWhitespace(true);
        System.setProperty(Filter.STREAM_FILTER_TYPE, "DOM");
    }

    @AfterClass
    public static void resetFilter()
    {
        System.getProperties().remove(Filter.STREAM_FILTER_TYPE);
    }

    @Test
    public void unmarshalEDI() throws Exception
    {
        result.expectedMessageCount(1);
        assertMockEndpointsSatisfied();

        Exchange exchange = result.assertExchangeReceived(0);

        assertIsInstanceOf(Document.class, exchange.getIn().getBody());
        assertXMLEqual(getExpectedOrderXml(), getBodyAsString(exchange));
    }

    private InputStreamReader getExpectedOrderXml()
    {
        return new InputStreamReader(getClass().getResourceAsStream("/xml/expected-order.xml"));
    }

    private StringReader getBodyAsString(Exchange exchange)
    {
        return new StringReader(exchange.getIn().getBody(String.class));
    }

    protected RouteBuilder createRouteBuilder() throws Exception
    {
        return new RouteBuilder()
        {
            public void configure() throws Exception
            {
                from("file://src/test/data?noop=true")
                .to("smooks://edi-to-xml-smooks-config.xml")
                .convertBodyTo(Node.class).to("mock:result");
            }
        };
    }
}