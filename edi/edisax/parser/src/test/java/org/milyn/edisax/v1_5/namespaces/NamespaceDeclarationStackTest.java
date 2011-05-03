/*
 * Milyn - Copyright (C) 2006 - 2011
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (version 2.1) as published by the Free Software
 * Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */
package org.milyn.edisax.v1_5.namespaces;

import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;

import org.milyn.edisax.util.NamespaceDeclarationStack;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import junit.framework.TestCase;


public class NamespaceDeclarationStackTest extends TestCase {

	public static final class MockContentHandler extends DefaultHandler {
	
		public List<String> history = new ArrayList<String>();
		
		@Override
		public void startPrefixMapping(String prefix, String uri)
				throws SAXException {
			history.add("start:" + prefix + ":" + uri);
		}
		
		@Override
		public void endPrefixMapping(String prefix) throws SAXException {
			history.add("end:" + prefix);
		}
		
		
	}
	
	public void testSimpleMaping() throws Exception {
		MockContentHandler handler = new MockContentHandler();
		NamespaceDeclarationStack nds = new NamespaceDeclarationStack(handler);
		Attributes a1 = nds.push("a", "nsa", null);
		nds.pop();
		assertEquals("[start:a:nsa, end:a]", handler.history.toString());
		assertEquals(1, a1.getLength());
		assertEquals("xmlns:a", a1.getQName(0));
		assertEquals("nsa", a1.getValue(0));
	}
	
	public void testSimpleMaping2() throws Exception {
		MockContentHandler handler = new MockContentHandler();
		NamespaceDeclarationStack nds = new NamespaceDeclarationStack(handler);
		Attributes a1 = nds.push("a", "nsa", null);
		Attributes a2 = nds.push("a", "nsa", null);
		nds.pop();
		nds.pop();
		assertEquals("[start:a:nsa, end:a]", handler.history.toString());
		assertEquals(1, a1.getLength());
		assertEquals("xmlns:a", a1.getQName(0));
		assertEquals("nsa", a1.getValue(0));
		assertEquals(0, a2.getLength());
	}
	
	public void testTwoNamespacesMapping() throws Exception {
		MockContentHandler handler = new MockContentHandler();
		NamespaceDeclarationStack nds = new NamespaceDeclarationStack(handler);
		Attributes a1 = nds.push("a", "nsa", null);
		Attributes a2 = nds.push("b", "nsb", null);
		nds.pop();
		nds.pop();
		assertEquals("[start:a:nsa, start:b:nsb, end:b, end:a]", handler.history.toString());
		assertEquals(1, a1.getLength());
		assertEquals("xmlns:a", a1.getQName(0));
		assertEquals("nsa", a1.getValue(0));
		assertEquals(1, a2.getLength());
		assertEquals("xmlns:b", a2.getQName(0));
		assertEquals("nsb", a2.getValue(0));
	}
	
	public void testNamespacesWithAttributes() throws Exception {
		AttributesImpl attrs = new AttributesImpl();
		attrs.addAttribute(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "b", "xmlns:b", "CDATA", "nsb");
		MockContentHandler handler = new MockContentHandler();
		NamespaceDeclarationStack nds = new NamespaceDeclarationStack(handler);
		nds.push("a", "nsa", attrs);
		nds.push("b", "nsb", null);
		nds.pop();
		nds.pop();
		assertEquals("[start:b:nsb, start:a:nsa, end:a, end:b]", handler.history.toString());
	}
	
}
