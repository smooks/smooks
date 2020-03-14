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
package org.smooks.edi.xsd14;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.xml.transform.stream.StreamSource;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.smooks.Smooks;
import org.smooks.payload.StringResult;
import org.xml.sax.SAXException;

import org.junit.Test;

/**
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Xsd14Test {

        @Test
	public void test_no_newlines() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("default-config.xml"));
		StringResult result = new StringResult();
		
		smooks.filterSource(new StreamSource(getClass().getResourceAsStream("edi-input-01.txt")), result);

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream("expected.xml")), new StringReader(result.toString()));
	}

        @Test
	public void test_with_newlines() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("newlines-config.xml"));
		StringResult result;
		
		// Doesn't actually contain newlines... but that shouldn't matter...
		result = new StringResult();
		smooks.filterSource(new StreamSource(getClass().getResourceAsStream("edi-input-01.txt")), result);
        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream("expected.xml")), new StringReader(result.toString()));

		// Contains newlines... should be ignored...
        result = new StringResult();
		smooks.filterSource(new StreamSource(getClass().getResourceAsStream("edi-input-02.txt")), result);
        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream("expected.xml")), new StringReader(result.toString()));
	}
}
