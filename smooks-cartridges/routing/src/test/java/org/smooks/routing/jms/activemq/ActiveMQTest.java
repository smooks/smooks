/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package org.smooks.routing.jms.activemq;

import static org.testng.AssertJUnit.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import org.smooks.Smooks;
import org.smooks.FilterSettings;
import org.smooks.templating.freemarker.FreeMarkerTemplateProcessor;
import org.smooks.templating.TemplatingConfiguration;
import org.smooks.templating.BindTo;
import org.smooks.javabean.Bean;
import org.smooks.payload.StringSource;
import org.smooks.routing.jms.TestJMSMessageListener;
import org.smooks.routing.jms.JMSRouter;
import org.xml.sax.SAXException;

import javax.jms.JMSException;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
@Test ( groups = "unit" )
public class ActiveMQTest {

    private static ActiveMQProvider mqProvider;
    private static TestJMSMessageListener listener;

    @BeforeClass
    public static void startActiveMQ() throws Exception {
        mqProvider = new ActiveMQProvider();
        mqProvider.addQueue("objectAQueue");
        mqProvider.start();
        listener = new TestJMSMessageListener();
        mqProvider.addQueueListener("objectAQueue", listener);
    }

    @AfterClass
    public static void stopActiveMQ() throws Exception {
        mqProvider.stop();
    }

    @Test
    public void test_xml_config_dom() throws IOException, SAXException, JMSException, InterruptedException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-01.xml"));
        test(smooks, FilterSettings.DEFAULT_DOM);
    }

    @Test
    public void test_xml_config_sax() throws IOException, SAXException, JMSException, InterruptedException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-01.xml"));
        test(smooks, FilterSettings.DEFAULT_SAX);
    }

    @Test
    public void test_xml_programmatic_dom() throws JMSException, InterruptedException {
        Smooks smooks = new Smooks();
        configure(smooks);
        test(smooks, FilterSettings.DEFAULT_DOM);
    }

    @Test
    public void test_xml_programmatic_sax() throws JMSException, InterruptedException {
        Smooks smooks = new Smooks();
        configure(smooks);
        test(smooks, FilterSettings.DEFAULT_SAX);
    }

    private void configure(Smooks smooks) {
        // Create a HashMap, name it "object" and then bind the <a> data into it, keyed as "a"...
        smooks.addVisitor(new Bean(HashMap.class, "object").bindTo("a", "a"));

        // On every <a> fragment, apply a simple template and bind the templating result to
        // beanId "orderItem_xml" ...
        smooks.addVisitor(new FreeMarkerTemplateProcessor(new TemplatingConfiguration("${object.a}").setUsage(BindTo.beanId("orderItem_xml"))), "a");

        JMSRouter jmsRouter = new JMSRouter();
        jmsRouter.setDestinationName("objectAQueue");
        jmsRouter.setBeanId("orderItem_xml");
        jmsRouter.setCorrelationIdPattern("${object.a}");
        jmsRouter.setJndiProperties("/org/smooks/routing/jms/activemq/activemq.1.jndi.properties");
        smooks.addVisitor(jmsRouter, "a");
    }

    private void test(Smooks smooks, FilterSettings filterSettings) throws JMSException, InterruptedException {
        try {
            smooks.setFilterSettings(filterSettings);
            listener.getMessages().clear();
            smooks.filterSource(new StringSource("<root><a>1</a><a>2</a><a>3</a></root>"));

            // wait to make sure all messages get delivered...
            Thread.sleep(500);

            assertEquals(3, listener.getMessages().size());
        } finally {
            smooks.close();
        }
    }
}
