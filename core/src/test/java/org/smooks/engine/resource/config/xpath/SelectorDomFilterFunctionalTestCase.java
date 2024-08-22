/*-
 * ========================LICENSE_START=================================
 * Core
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
package org.smooks.engine.resource.config.xpath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smooks.FilterSettings;
import org.smooks.Smooks;
import org.smooks.io.source.StreamSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class SelectorDomFilterFunctionalTestCase {

    private Properties namespaces;

    @BeforeEach
    public void setUp() throws Exception {
        XPathVisitor.domVisitedBeforeElementStatic = null;
        XPathVisitor.domVisitedAfterElementStatic = null;
        XPathAfterVisitor.domVisitedAfterElement = null;

        namespaces = new Properties();

        namespaces.put("a", "http://a");
        namespaces.put("b", "http://b");
        namespaces.put("c", "http://c");
        namespaces.put("d", "http://d");
    }
	
	@Test
    @DisplayName("item[@c:code = 8655]")
    public void testSelector1() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-01.xml"));

        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
        assertEquals("8655", XPathVisitor.domVisitedBeforeElementStatic.getAttributeNS("http://c", "code"));
        assertEquals("8655", XPathVisitor.domVisitedAfterElementStatic.getAttributeNS("http://c", "code"));
    }

	@Test
    @DisplayName("item[@c:code = '8655']/units[text() = 1]")
    public void testSelector2() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-03.xml"));

        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
        assertEquals("1", XPathAfterVisitor.domVisitedAfterElement.getTextContent());
    }

	@Test
    @DisplayName("item[@c:code = 8655]")
    public void testAddVisitorSelector1() {
        Properties properties = new Properties();
        properties.setProperty("c", "http://c");

        Smooks smooks = new Smooks();

        smooks.setNamespaces(properties);
        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.addVisitor(new XPathVisitor(), "item[@c:code = 8655]");
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
        assertEquals("8655", XPathVisitor.domVisitedBeforeElementStatic.getAttributeNS("http://c", "code"));
        assertEquals("8655", XPathVisitor.domVisitedAfterElementStatic.getAttributeNS("http://c", "code"));
    }

	@Test
    @DisplayName("item[@c:code = '8655']/units[text() = 1]")
    public void testAddVisitorSelector2() {
        Properties properties = new Properties();
        properties.setProperty("c", "http://c");

        Smooks smooks = new Smooks();

        smooks.setNamespaces(properties);
        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.addVisitor(new XPathAfterVisitor(), "item[@c:code = '8655']/units[text() = 1]");
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
        assertEquals("1", XPathAfterVisitor.domVisitedAfterElement.getTextContent());
    }

	@Test
    @DisplayName("c:item[@c:code = '8655']/d:units[text() = 1]")
    public void testSelector3() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-04.xml"));

        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
        assertEquals("1", XPathAfterVisitor.domVisitedAfterElement.getTextContent());
    }

	@Test
    @DisplayName("c:item[@d:code = '8655']/d:units[text() = 1]")
    public void testSelector4() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-05.xml"));

        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
        assertNull(XPathAfterVisitor.domVisitedAfterElement);
    }

	@Test
    @DisplayName("/a:ord[@num = 3122 and @state = 'finished']/a:items/c:item[@c:code = '8655']/d:units[text() = 1]")
    public void testSelector5() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-06.xml"));

        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
        assertEquals("1", XPathAfterVisitor.domVisitedAfterElement.getTextContent());
    }

	@Test
    @DisplayName("/a:ord[@num = 3122 and @state = 'finished']/a:items/c:item[@c:code = '8655']/d:units[text() = 1]")
    public void testAddVisitorSelector3() {
        Smooks smooks = new Smooks();
        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.setNamespaces(namespaces);

        smooks.addVisitor(new XPathAfterVisitor(), "/a:ord[@num = 3122 and @state = 'finished']/a:items/c:item[@c:code = '8655']/d:units[text() = 1]");
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
        assertEquals("1", XPathAfterVisitor.domVisitedAfterElement.getTextContent());
    }

    @Test
    @DisplayName("items/c:item[2]/units")
    public void testSelectorGivenIndex() throws Exception {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-07.xml"));

        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order_02.xml")));

        assertNotNull(XPathVisitor.domVisitedBeforeElementStatic);
        assertNotNull(XPathVisitor.domVisitedAfterElementStatic);
    }

    @Test
    @DisplayName("items/c:item[2]/units")
    public void testAddVisitorSelectorGivenIndex() {
        Smooks smooks = new Smooks();

        smooks.setNamespaces(namespaces);
        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.addVisitor(new XPathVisitor(), "items/c:item[2]/units");
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order_02.xml")));

        assertNotNull(XPathVisitor.domVisitedBeforeElementStatic);
        assertNotNull(XPathVisitor.domVisitedAfterElementStatic);
    }

    @Test
    @DisplayName("items/item[3]/units")
    public void testSelectorGivenWrongIndex() throws Exception {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-08.xml"));

        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));

        assertNull(XPathVisitor.domVisitedBeforeElementStatic);
        assertNull(XPathVisitor.domVisitedAfterElementStatic);
    }

    @Test
    @DisplayName("items/item[3]/units")
    public void testAddVisitorSelectorGivenWrongIndex() {
        Smooks smooks = new Smooks();

        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.addVisitor(new XPathVisitor(), "items/item[3]/units");
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));

        assertNull(XPathVisitor.domVisitedBeforeElementStatic);
        assertNull(XPathVisitor.domVisitedAfterElementStatic);
    }

    @Test
    @DisplayName("items[1]/item[2]/units, items[2]/item[1]/units")
    public void testAddVisitorSelectorsGivenIndexes() {
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
    @DisplayName("items[1]/c:item[2]/units, items[2]/c:item[1]/units")
    public void testAddVisitorSelectorsGivenIndexesAndPrefixes() {
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
    @DisplayName("items[1]/d:item[2]/units, items[2]/d:item[1]/units")
    public void testAddVisitorSelectorsGivenIndexesAndWrongPrefixes() {
        Smooks smooks = new Smooks();
        XPathVisitor visitor1 = new XPathVisitor();
        XPathVisitor visitor2 = new XPathVisitor();

        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.setNamespaces(namespaces);

        smooks.addVisitor(visitor1, "items[1]/d:item[2]/units");
        smooks.addVisitor(visitor2, "items[2]/d:item[1]/units");

        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order_02.xml")));

        assertNull(visitor1.getDomVisitedAfterElement());
        assertNull(visitor2.getDomVisitedAfterElement());
    }
}
