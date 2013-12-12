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
package org.milyn.cdr.xsd11.extensiontests;

import junit.framework.TestCase;
import org.milyn.Smooks;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.commons.cdr.SmooksConfigurationException;
import org.milyn.commons.xml.XmlUtil;
import org.milyn.commons.xml.XsdDOMValidator;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.ContentDeliveryConfig;
import org.milyn.payload.StringResult;
import org.milyn.payload.StringSource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class XSD11ExtendTest extends TestCase {

    public void test_validation() throws IOException, SAXException, ParserConfigurationException {
        Document configDoc = XmlUtil.parseStream(getClass().getResourceAsStream("config_01.xml"));
        XsdDOMValidator validator = new XsdDOMValidator(configDoc);

        assertEquals("http://www.milyn.org/xsd/smooks-1.1.xsd", validator.getDefaultNamespace().toString());
        assertEquals("[http://www.milyn.org/xsd/smooks-1.1.xsd, http://www.milyn.org/xsd/smooks/test-xsd-01.xsd, http://www.w3.org/2001/XMLSchema-instance]", validator.getNamespaces().toString());

        validator.validate();
    }

    public void test_digest_01_simple() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config_01.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new StringSource("<a><b/><c/><d/></a>"), result);
        assertEquals("<a><c></c><c></c><d></d></a>", result.getResult());
    }

    public void test_digest_02_simple_import() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config_02.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new StringSource("<a><b/><c/></a>"), result);
        assertEquals("<a><c></c><b></b></a>", result.getResult());
    }

    public void test_digest_03_simple_invalid_condition() throws IOException, SAXException {
        try {
            new Smooks(getClass().getResourceAsStream("config_03.xml"));
            fail("Expected SmooksConfigurationException");
        } catch (SmooksConfigurationException e) {
            assertEquals("Failed to construct Smooks instance for processing extended configuration resource '/META-INF/xsd/smooks/test-xsd-03.xsd-smooks.xml'.", e.getMessage());
            assertEquals("Configuration element 'conditions' not supported in an extension configuration.", e.getCause().getMessage());
        }
    }

    public void test_digest_04_simple_invalid_profiles() throws IOException, SAXException {
        try {
            new Smooks(getClass().getResourceAsStream("config_04.xml"));
            fail("Expected SmooksConfigurationException");
        } catch (SmooksConfigurationException e) {
            assertEquals("Failed to construct Smooks instance for processing extended configuration resource '/META-INF/xsd/smooks/test-xsd-04.xsd-smooks.xml'.", e.getMessage());
            assertEquals("Configuration element 'profiles' not supported in an extension configuration.", e.getCause().getMessage());
        }
    }

    public void test_digest_05_simple_default_1() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config_05.1.xml"));
        ExecutionContext execContext;
        ContentDeliveryConfig deliveryConf;
        List<SmooksResourceConfiguration> configList;

        // config_05.1.xml defines a default profile of "xxx", so creating the context without specifying
        // a profile should exclude the "aa" resource...
        execContext = smooks.createExecutionContext();
        deliveryConf = execContext.getDeliveryConfig();
        configList = deliveryConf.getSmooksResourceConfigurations("aa");
        assertNull(configList);

        // config_05.1.xml defines a default profile of "xxx", so creating the context by specifying
        // a profile of "xxx" should include the "aa" resource...
        execContext = smooks.createExecutionContext("xxx");
        deliveryConf = execContext.getDeliveryConfig();
        configList = deliveryConf.getSmooksResourceConfigurations("aa");
        assertNotNull(configList);

        // Make sure the resource has the other default settings...
        SmooksResourceConfiguration config = configList.get(0);
        assertEquals("http://an", config.getSelectorNamespaceURI());
        assertNotNull(config.getConditionEvaluator());
    }

    public void test_digest_05_simple_default_2() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config_05.2.xml"));
        ExecutionContext execContext;
        ContentDeliveryConfig deliveryConf;
        List<SmooksResourceConfiguration> configList;

        // config_05.2.xml defines a name attribute, so that should override the default...
        execContext = smooks.createExecutionContext("xxx");
        deliveryConf = execContext.getDeliveryConfig();
        configList = deliveryConf.getSmooksResourceConfigurations("j");
        assertNotNull(configList);

        // Make sure the resource has the other default settings...
        SmooksResourceConfiguration config = configList.get(0);
        assertEquals("http://an", config.getSelectorNamespaceURI());
        assertNotNull(config.getConditionEvaluator());
    }
}
