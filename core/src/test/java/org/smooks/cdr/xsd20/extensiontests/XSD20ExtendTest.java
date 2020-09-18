/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.cdr.xsd20.extensiontests;

import org.junit.Test;
import org.smooks.Smooks;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.ContentDeliveryConfig;
import org.smooks.payload.StringResult;
import org.smooks.payload.StringSource;
import org.smooks.xml.XmlUtil;
import org.smooks.xml.XsdDOMValidator;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class XSD20ExtendTest {

	@Test
    public void test_validation() throws IOException, SAXException, ParserConfigurationException {
        Document configDoc = XmlUtil.parseStream(getClass().getResourceAsStream("config_01.xml"));
        XsdDOMValidator validator = new XsdDOMValidator(configDoc);

        assertEquals("https://www.smooks.org/xsd/smooks-2.0.xsd", validator.getDefaultNamespace().toString());
        assertEquals("[https://www.smooks.org/xsd/smooks-2.0.xsd, http://www.milyn.org/xsd/smooks/test-xsd-01.xsd, http://www.w3.org/2001/XMLSchema-instance]", validator.getNamespaces().toString());

        validator.validate();
    }

	@Test
    public void test_digest_01_simple() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config_01.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new StringSource("<a><b/><c/><d/></a>"), result);
        assertEquals("<a><c></c><c></c><d></d></a>", result.getResult());
    }

	@Test
    public void test_digest_02_simple_import() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config_02.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new StringSource("<a><b/><c/></a>"), result);
        assertEquals("<a><c></c><b></b></a>", result.getResult());
    }

	@Test
    public void test_digest_03_simple_invalid_condition() throws IOException, SAXException {
        try {
            new Smooks(getClass().getResourceAsStream("config_03.xml"));
            fail("Expected SmooksConfigurationException");
        } catch (SmooksConfigurationException e) {
            assertEquals("Failed to construct Smooks instance for processing extended configuration resource '/META-INF/xsd/smooks/test-xsd-03.xsd-smooks.xml'.", e.getMessage());
            assertEquals("Configuration element 'conditions' not supported in an extension configuration.", e.getCause().getMessage());
        }
    }

	@Test
    public void test_digest_04_simple_invalid_profiles() throws IOException, SAXException {
        try {
            new Smooks(getClass().getResourceAsStream("config_04.xml"));
            fail("Expected SmooksConfigurationException");
        } catch (SmooksConfigurationException e) {
            assertEquals("Failed to construct Smooks instance for processing extended configuration resource '/META-INF/xsd/smooks/test-xsd-04.xsd-smooks.xml'.", e.getMessage());
            assertEquals("Configuration element 'profiles' not supported in an extension configuration.", e.getCause().getMessage());
        }
    }

	@Test
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
        configList = deliveryConf.getSmooksResourceConfigurations("an:aa");
        assertNotNull(configList);

        // Make sure the resource has the other default settings...
        SmooksResourceConfiguration config = configList.get(0);
        assertEquals("http://www.milyn.org/xsd/smooks/test-xsd-05.xsd", config.getSelectorPath().getNamespaces().getProperty("t01"));
        assertEquals("http://an", config.getSelectorPath().getNamespaces().getProperty("an"));
        assertNotNull(config.getSelectorPath().getConditionEvaluator());
    }

	@Test
    public void test_digest_05_simple_default_2() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config_05.2.xml"));
        ExecutionContext execContext;
        ContentDeliveryConfig deliveryConf;
        List<SmooksResourceConfiguration> configList;

        // config_05.2.xml defines a name attribute, so that should override the default...
        execContext = smooks.createExecutionContext("xxx");
        deliveryConf = execContext.getDeliveryConfig();
        configList = deliveryConf.getSmooksResourceConfigurations("an:j");
        assertNotNull(configList);

        // Make sure the resource has the other default settings...
        SmooksResourceConfiguration config = configList.get(0);
        assertEquals("http://www.milyn.org/xsd/smooks/test-xsd-05.xsd", config.getSelectorPath().getNamespaces().getProperty("t01"));
        assertEquals("http://an", config.getSelectorPath().getNamespaces().getProperty("an"));
        assertNotNull(config.getSelectorPath().getConditionEvaluator());
    }
}
