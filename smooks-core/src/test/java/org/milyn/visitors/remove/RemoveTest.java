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
package org.milyn.visitors.remove;

import org.junit.Test;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.milyn.FilterSettings;
import org.milyn.Smooks;
import org.milyn.payload.StringResult;
import org.milyn.payload.StringSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class RemoveTest {

	@Test
    public void test_no_children_SAX() throws IOException, SAXException {
        test(FilterSettings.DEFAULT_SAX, false, "<a><something /></a>");
    }

	@Test
    public void test_no_children_DOM() throws IOException, SAXException {
        test(FilterSettings.DEFAULT_DOM, false, "<a><something></something></a>");
    }

	@Test
    public void test_children_SAX() throws IOException, SAXException {
        test(FilterSettings.DEFAULT_SAX, true, "<a><d><e>some text</e></d><something /></a>");
    }

	@Test
    public void test_children_DOM() throws IOException, SAXException {
        test(FilterSettings.DEFAULT_DOM, true, "<a><d><e>some text</e></d><something></something></a>");
    }

    public void test(FilterSettings filterSettings, boolean keepChildren, String expected) throws IOException, SAXException {
        Smooks smooks = new Smooks();
        StringResult result = new StringResult();

        smooks.setFilterSettings(filterSettings);
        smooks.addVisitor(new RemoveElement().setKeepChildren(keepChildren), "b");

        smooks.filterSource(new StringSource("<a><b><d><e>some text</e></d></b><something/></a>"), result);

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new StringReader(expected), new StringReader(result.getResult()));
    }

	@Test
    public void test_XML_config_SAX() throws IOException, SAXException {
        test_XML_config(FilterSettings.DEFAULT_SAX);
    }

	@Test
    public void test_XML_config_DOM() throws IOException, SAXException {
        test_XML_config(FilterSettings.DEFAULT_DOM);
    }

    public void test_XML_config(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-01.xml"));
        StringResult result = new StringResult();

        smooks.setFilterSettings(filterSettings);

        smooks.filterSource(new StringSource("<a attrib1='1' ns1:attrib2='2' xmlns:ns1='http://ns1'><b xmlns:ns2='http://ns2'><d><e>some text</e></d></b><something/></a>"), result);

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new StringReader("<a><b><e>some text</e></b><something /></a>"), new StringReader(result.getResult()));
    }
}
