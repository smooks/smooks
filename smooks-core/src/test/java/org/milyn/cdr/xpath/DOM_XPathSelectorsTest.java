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
package org.milyn.cdr.xpath;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.milyn.Smooks;
import org.milyn.FilterSettings;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;

import java.io.IOException;
import java.util.Properties;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class DOM_XPathSelectorsTest {

	@Before
    public void setUp() throws Exception {
        XPathVisitor.domVisitedBeforeElementStatic = null;
        XPathVisitor.domVisitedAfterElementStatic = null;
        XPathAfterVisitor.domVisitedAfterElement = null;
    }
	
	@Test
    public void test_01() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-01.xml"));

        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
        assertEquals("8655", XPathVisitor.domVisitedBeforeElementStatic.getAttributeNS("http://c", "code"));
        assertEquals("8655", XPathVisitor.domVisitedAfterElementStatic.getAttributeNS("http://c", "code"));
    }

	@Test
    public void test_03() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-03.xml"));

        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
        assertEquals("1", XPathAfterVisitor.domVisitedAfterElement.getTextContent());
    }

	@Test
    public void test_04() throws IOException, SAXException {
        Smooks smooks = new Smooks();

        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.addVisitor(new XPathVisitor(), "item[@code = 8655]");
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
        assertEquals("8655", XPathVisitor.domVisitedBeforeElementStatic.getAttributeNS("http://c", "code"));
        assertEquals("8655", XPathVisitor.domVisitedAfterElementStatic.getAttributeNS("http://c", "code"));
    }

	@Test
    public void test_06() throws IOException, SAXException {
        Smooks smooks = new Smooks();

        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.addVisitor(new XPathAfterVisitor(), "item[@code = '8655']/units[text() = 1]");
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
        assertEquals("1", XPathAfterVisitor.domVisitedAfterElement.getTextContent());
    }

	@Test
    public void test_07() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-04.xml"));

        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
        assertEquals("1", XPathAfterVisitor.domVisitedAfterElement.getTextContent());
    }

	@Test
    public void test_08() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-05.xml"));

        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
        assertEquals(null, XPathAfterVisitor.domVisitedAfterElement);
    }

	@Test
    public void test_09() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-06.xml"));

        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
        assertEquals("1", XPathAfterVisitor.domVisitedAfterElement.getTextContent());
    }

	@Test
    public void test_10() throws IOException, SAXException {
        Smooks smooks = new Smooks();
        Properties namespaces = new Properties();

        namespaces.put("a", "http://a");
        namespaces.put("b", "http://b");
        namespaces.put("c", "http://c");
        namespaces.put("d", "http://d");

        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);
        smooks.setNamespaces(namespaces);

        smooks.addVisitor(new XPathAfterVisitor(), "/a:ord[@num = 3122 and @state = 'finished']/a:items/c:item[@c:code = '8655']/d:units[text() = 1]");
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
        assertEquals("1", XPathAfterVisitor.domVisitedAfterElement.getTextContent());
    }
}
