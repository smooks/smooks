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
package org.smooks.cdr.xpath;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.smooks.Smooks;
import org.smooks.FilterSettings;
import org.smooks.cdr.SmooksConfigurationException;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;

import java.io.IOException;
import java.util.Properties;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class SAX_XPathSelectorsTest {

    private Properties namespaces;

    @Before
    public void setUp() throws Exception {
        XPathVisitor.saxVisitedBeforeElementStatic = null;
        XPathVisitor.saxVisitedAfterElementStatic = null;
        XPathVisitor.domVisitedBeforeElementStatic = null;
        XPathVisitor.domVisitedAfterElementStatic = null;
        XPathAfterVisitor.saxVisitedAfterElement = null;

        namespaces = new Properties();

        namespaces.put("a", "http://a");
        namespaces.put("b", "http://b");
        namespaces.put("c", "http://c");
        namespaces.put("d", "http://d");
    }

    @Test
    public void test_01() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-01.xml"));

        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
        assertEquals("8655", XPathVisitor.saxVisitedBeforeElementStatic.getAttribute("code"));
        assertEquals("8655", XPathVisitor.saxVisitedAfterElementStatic.getAttribute("code"));
    }

    @Test
    public void test_02() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-02.xml"));

        try {
            smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
            smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
            fail("Expected SmooksConfigurationException");
        } catch(SmooksConfigurationException e) {

            assertEquals("Unsupported selector 'item[@code = '8655']/units[text() = 1]' on resource 'Target Profile: [[org.smooks.profile.Profile#default_profile]], Selector: [item[@code = '8655']/units[text() = 1]], Selector Namespace URI: [null], Resource: [org.smooks.cdr.xpath.XPathVisitor], Num Params: [0]'.  The 'text()' XPath token is only supported on SAX Visitor implementations that implement the org.smooks.delivery.sax.SAXVisitAfter interface only.  Class 'org.smooks.cdr.xpath.XPathVisitor' implements other SAX Visitor interfaces.", e.getMessage());
        }
    }

    @Test
    public void test_03() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-03.xml"));

        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
        assertEquals("1", XPathAfterVisitor.saxVisitedAfterElement.getTextContent());
    }

    @Test
    public void test_04() throws IOException, SAXException {
        Smooks smooks = new Smooks();

        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        smooks.addVisitor(new XPathVisitor(), "item[@code = 8655]");
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
        assertEquals("8655", XPathVisitor.saxVisitedBeforeElementStatic.getAttribute("code"));
        assertEquals("8655", XPathVisitor.saxVisitedAfterElementStatic.getAttribute("code"));
    }

    @Test
    public void test_05() throws IOException, SAXException {
        Smooks smooks = new Smooks();

        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        smooks.addVisitor(new XPathVisitor(), "item[@code = '8655']/units[text() = 1]");
        try {
            smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
            fail("Expected SmooksConfigurationException");
        } catch(SmooksConfigurationException e) {
            assertEquals("Unsupported selector 'item[@code = '8655']/units[text() = 1]' on resource 'Target Profile: [[org.smooks.profile.Profile#default_profile]], Selector: [item[@code = '8655']/units[text() = 1]], Selector Namespace URI: [null], Resource: [org.smooks.cdr.xpath.XPathVisitor], Num Params: [0]'.  The 'text()' XPath token is only supported on SAX Visitor implementations that implement the org.smooks.delivery.sax.SAXVisitAfter interface only.  Class 'org.smooks.cdr.xpath.XPathVisitor' implements other SAX Visitor interfaces.", e.getMessage());
        }
    }

    @Test
    public void test_06() throws IOException, SAXException {
        Smooks smooks = new Smooks();

        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        smooks.addVisitor(new XPathAfterVisitor(), "item[@code = '8655']/units[text() = 1]");
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
        assertEquals("1", XPathAfterVisitor.saxVisitedAfterElement.getTextContent());
    }

    @Test
    public void test_07() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-04.xml"));

        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
        assertEquals("1", XPathAfterVisitor.saxVisitedAfterElement.getTextContent());
    }

    @Test
    public void test_08() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-05.xml"));

        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
        assertEquals(null, XPathAfterVisitor.saxVisitedAfterElement);
    }

    @Test
    public void test_09() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-06.xml"));

        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
        assertEquals("1", XPathAfterVisitor.saxVisitedAfterElement.getTextContent());
    }

    @Test
    public void test_10() throws IOException, SAXException {
        Smooks smooks = new Smooks();

        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        smooks.setNamespaces(namespaces);

        smooks.addVisitor(new XPathAfterVisitor(), "/a:ord[@num = 3122 and @state = 'finished']/a:items/c:item[@c:code = '8655']/d:units[text() = 1]");
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
        assertEquals("1", XPathAfterVisitor.saxVisitedAfterElement.getTextContent());
    }

    @Test
    public void test_indexevaluator_sax_01() throws Exception {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-07.xml"));

        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
        
        assertTrue(XPathVisitor.saxVisitedBeforeElementStatic != null);
        assertTrue(XPathVisitor.saxVisitedAfterElementStatic != null);
    }

    @Test
    public void test_indexevaluator_sax_02() {
        Smooks smooks = new Smooks();

        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        smooks.addVisitor(new XPathVisitor(), "items/item[2]/units");
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));

        assertTrue(XPathVisitor.saxVisitedBeforeElementStatic != null);
        assertTrue(XPathVisitor.saxVisitedAfterElementStatic != null);
    }

    @Test
    public void test_indexevaluator_sax_03() throws Exception {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-08.xml"));

        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));

        assertTrue(XPathVisitor.saxVisitedBeforeElementStatic == null);
        assertTrue(XPathVisitor.saxVisitedAfterElementStatic == null);
    }

    @Test
    public void test_indexevaluator_sax_04() {
        Smooks smooks = new Smooks();

        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        smooks.addVisitor(new XPathVisitor(), "items/item[3]/units");
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));

        assertTrue(XPathVisitor.saxVisitedBeforeElementStatic == null);
        assertTrue(XPathVisitor.saxVisitedAfterElementStatic == null);
    }

    @Test
    public void test_indexevaluator_sax_05() {
        Smooks smooks = new Smooks();
        XPathVisitor visitor1 = new XPathVisitor();
        XPathVisitor visitor2 = new XPathVisitor();

        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);

        smooks.addVisitor(visitor1, "items[1]/item[2]/units");
        smooks.addVisitor(visitor2, "items[2]/item[1]/units");

        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order_02.xml")));

        assertEquals("2", visitor1.getSaxVisitedAfterElement().getAttribute("index"));
        assertEquals("1", visitor2.getSaxVisitedAfterElement().getAttribute("index"));
    }

    @Test
    public void test_indexevaluator_sax_06() {
        Smooks smooks = new Smooks();
        XPathVisitor visitor1 = new XPathVisitor();
        XPathVisitor visitor2 = new XPathVisitor();

        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        smooks.setNamespaces(namespaces);

        smooks.addVisitor(visitor1, "items[1]/c:item[2]/units");
        smooks.addVisitor(visitor2, "items[2]/c:item[1]/units");

        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order_02.xml")));

        assertEquals("2", visitor1.getSaxVisitedAfterElement().getAttribute("index"));
        assertEquals("1", visitor2.getSaxVisitedAfterElement().getAttribute("index"));
    }


    @Test
    public void test_indexevaluator_sax_07() {
        Smooks smooks = new Smooks();
        XPathVisitor visitor1 = new XPathVisitor();
        XPathVisitor visitor2 = new XPathVisitor();

        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        smooks.setNamespaces(namespaces);

        smooks.addVisitor(visitor1, "items[1]/d:item[2]/units"); // wrong namespace prefix
        smooks.addVisitor(visitor2, "items[2]/d:item[1]/units"); // wrong namespace prefix

        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order_02.xml")));

        assertEquals(null, visitor1.getSaxVisitedAfterElement());
        assertEquals(null, visitor2.getSaxVisitedAfterElement());
    }

    @Test
    public void test_indexevaluator_dom_01() throws Exception {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-07.xml"));

        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));

        assertTrue(XPathVisitor.domVisitedBeforeElementStatic != null);
        assertTrue(XPathVisitor.domVisitedAfterElementStatic != null);
    }

    @Test
    public void test_indexevaluator_dom_02() {
        Smooks smooks = new Smooks();

        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.addVisitor(new XPathVisitor(), "items/item[2]/units");
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));

        assertTrue(XPathVisitor.domVisitedBeforeElementStatic != null);
        assertTrue(XPathVisitor.domVisitedAfterElementStatic != null);
    }

    @Test
    public void test_indexevaluator_dom_03() throws Exception {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-08.xml"));

        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));

        assertTrue(XPathVisitor.domVisitedBeforeElementStatic == null);
        assertTrue(XPathVisitor.domVisitedAfterElementStatic == null);
    }

    @Test
    public void test_indexevaluator_dom_04() {
        Smooks smooks = new Smooks();

        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.addVisitor(new XPathVisitor(), "items/item[3]/units");
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));

        assertTrue(XPathVisitor.domVisitedBeforeElementStatic == null);
        assertTrue(XPathVisitor.domVisitedAfterElementStatic == null);
    }

    @Test
    public void test_indexevaluator_dom_05() {
        Smooks smooks = new Smooks();
        XPathVisitor visitor1 = new XPathVisitor();
        XPathVisitor visitor2 = new XPathVisitor();

        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);

        smooks.addVisitor(visitor1, "items[1]/item[2]/units");
        smooks.addVisitor(visitor2, "items[2]/item[1]/units");

        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order_02.xml")));

        assertEquals("2", visitor1.getDomVisitedAfterElement().getAttribute("index"));
        assertEquals("1", visitor2.getDomVisitedAfterElement().getAttribute("index"));
    }

    @Test
    public void test_indexevaluator_dom_06() {
        Smooks smooks = new Smooks();
        XPathVisitor visitor1 = new XPathVisitor();
        XPathVisitor visitor2 = new XPathVisitor();

        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.setNamespaces(namespaces);

        smooks.addVisitor(visitor1, "items[1]/c:item[2]/units");
        smooks.addVisitor(visitor2, "items[2]/c:item[1]/units");

        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order_02.xml")));

        assertEquals("2", visitor1.getDomVisitedAfterElement().getAttribute("index"));
        assertEquals("1", visitor2.getDomVisitedAfterElement().getAttribute("index"));
    }


    @Test
    public void test_indexevaluator_dom_07() {
        Smooks smooks = new Smooks();
        XPathVisitor visitor1 = new XPathVisitor();
        XPathVisitor visitor2 = new XPathVisitor();

        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.setNamespaces(namespaces);

        smooks.addVisitor(visitor1, "items[1]/d:item[2]/units"); // wrong namespace prefix
        smooks.addVisitor(visitor2, "items[2]/d:item[1]/units"); // wrong namespace prefix

        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order_02.xml")));

        assertEquals(null, visitor1.getDomVisitedAfterElement());
        assertEquals(null, visitor2.getDomVisitedAfterElement());
    }
}
