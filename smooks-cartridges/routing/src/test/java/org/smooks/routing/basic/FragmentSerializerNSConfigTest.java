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
package org.smooks.routing.basic;

import org.junit.Test;

import org.smooks.FilterSettings;
import org.smooks.Smooks;
import org.smooks.payload.JavaResult;
import org.xml.sax.SAXException;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XMLAssert;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class FragmentSerializerNSConfigTest {

    @Test
    public void test_children_only_SAX() throws IOException, SAXException {
    	test_children_only(FilterSettings.DEFAULT_SAX);
    }

    @Test
    public void test_children_only_DOM() throws IOException, SAXException {
    	test_children_only(FilterSettings.DEFAULT_DOM);
    }

    private void test_children_only(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-01-ext.xml"));
        StreamSource source = new StreamSource(getClass().getResourceAsStream("input-message-01.xml"));
        JavaResult result = new JavaResult();

        smooks.setFilterSettings(filterSettings);
        smooks.filterSource(source, result);

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream("children-only.xml")), new StringReader(result.getBean("soapBody").toString().trim()));
    }

    @Test
    public void test_all_SAX() throws IOException, SAXException {
    	test_all(FilterSettings.DEFAULT_SAX);
    }
  
    @Test
    public void test_all_DOM() throws IOException, SAXException {
    	test_all(FilterSettings.DEFAULT_DOM);
    }

    private void test_all(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-02-ext.xml"));
        StreamSource source = new StreamSource(getClass().getResourceAsStream("input-message-01.xml"));
        JavaResult result = new JavaResult();

        smooks.setFilterSettings(filterSettings);
        smooks.filterSource(source, result);

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream("all.xml")), new StringReader(result.getBean("soapBody").toString().trim()));
    }
}
