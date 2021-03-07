/*-
 * ========================LICENSE_START=================================
 * Commons
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
package org.smooks.namespace;

import org.junit.Test;
import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class NamespaceDeclarationStackTestCase {

	public static final class MockContentHandler extends DefaultHandler {
	
		public final List<String> history = new ArrayList<String>();
		
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
	
	@Test
	public void testSimpleMaping() throws Exception {
		MockContentHandler handler = new MockContentHandler();
        NamespaceDeclarationStack nds = new NamespaceDeclarationStack();
        nds.pushReader(new MockXMLReader(handler));
		nds.pushNamespaces("a:element", "nsa", null);
		nds.popNamespaces();
		assertEquals("[start:a:nsa, end:a]", handler.history.toString());
	}
	
	@Test
	public void testSimpleMaping2() throws Exception {
		MockContentHandler handler = new MockContentHandler();
        NamespaceDeclarationStack nds = new NamespaceDeclarationStack();
        nds.pushReader(new MockXMLReader(handler));
		nds.pushNamespaces("a:element", "nsa", null);
		nds.pushNamespaces("a:element", "nsa", null);
		nds.popNamespaces();
		nds.popNamespaces();
		assertEquals("[start:a:nsa, end:a]", handler.history.toString());
	}
	
	@Test
	public void testTwoNamespacesMapping() throws Exception {
		MockContentHandler handler = new MockContentHandler();
        NamespaceDeclarationStack nds = new NamespaceDeclarationStack();
        nds.pushReader(new MockXMLReader(handler));
		nds.pushNamespaces("a:element", "nsa", null);
		nds.pushNamespaces("b:element", "nsb", null);
		nds.popNamespaces();
		nds.popNamespaces();
		assertEquals("[start:a:nsa, start:b:nsb, end:b, end:a]", handler.history.toString());
	}
	
	@Test
	public void testNamespacesWithAttributes() throws Exception {
		AttributesImpl attrs = new AttributesImpl();
		attrs.addAttribute(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "b", "xmlns:b", "CDATA", "nsb");
		MockContentHandler handler = new MockContentHandler();
		NamespaceDeclarationStack nds = new NamespaceDeclarationStack();
        nds.pushReader(new MockXMLReader(handler));
		nds.pushNamespaces("a:element", "nsa", attrs);
		nds.pushNamespaces("b:element", "nsb", null);
		nds.popNamespaces();
		nds.popNamespaces();
		assertEquals("[start:b:nsb, start:a:nsa, end:b, end:a]", handler.history.toString());
	}

    private class MockXMLReader implements XMLReader {

        private ContentHandler contentHandler;

        private MockXMLReader(ContentHandler contentHandler) {
            this.contentHandler = contentHandler;
        }

		@Override
		public boolean getFeature(String s) throws SAXNotRecognizedException, SAXNotSupportedException {
            return false;
        }

		@Override
		public void setFeature(String s, boolean b) throws SAXNotRecognizedException, SAXNotSupportedException {
        }

		@Override
		public Object getProperty(String s) throws SAXNotRecognizedException, SAXNotSupportedException {
            return null;
        }

		@Override
		public void setProperty(String s, Object o) throws SAXNotRecognizedException, SAXNotSupportedException {
        }

		@Override
		public void setEntityResolver(EntityResolver entityResolver) {
        }

		@Override
		public EntityResolver getEntityResolver() {
            return null;
        }

		@Override
		public void setDTDHandler(DTDHandler dtdHandler) {
        }

		@Override
		public DTDHandler getDTDHandler() {
            return null;
        }

		@Override
		public void setContentHandler(ContentHandler contentHandler) {
            this.contentHandler = contentHandler;
        }

		@Override
		public ContentHandler getContentHandler() {
            return contentHandler;
        }

		@Override
		public void setErrorHandler(ErrorHandler errorHandler) {
        }

		@Override
		public ErrorHandler getErrorHandler() {
            return null;
        }

		@Override
		public void parse(InputSource inputSource) throws IOException, SAXException {
        }

		@Override
		public void parse(String s) throws IOException, SAXException {
        }
    }
}
