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
package org.milyn.commons.namespace;

import junit.framework.TestCase;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


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
        NamespaceDeclarationStack nds = new NamespaceDeclarationStack();
        nds.pushReader(new MockXMLReader(handler));
        nds.pushNamespaces("a:element", "nsa", null);
        nds.popNamespaces();
        assertEquals("[start:a:nsa, end:a]", handler.history.toString());
    }

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

        public boolean getFeature(String s) throws SAXNotRecognizedException, SAXNotSupportedException {
            return false;
        }

        public void setFeature(String s, boolean b) throws SAXNotRecognizedException, SAXNotSupportedException {
        }

        public Object getProperty(String s) throws SAXNotRecognizedException, SAXNotSupportedException {
            return null;
        }

        public void setProperty(String s, Object o) throws SAXNotRecognizedException, SAXNotSupportedException {
        }

        public void setEntityResolver(EntityResolver entityResolver) {
        }

        public EntityResolver getEntityResolver() {
            return null;
        }

        public void setDTDHandler(DTDHandler dtdHandler) {
        }

        public DTDHandler getDTDHandler() {
            return null;
        }

        public void setContentHandler(ContentHandler contentHandler) {
            this.contentHandler = contentHandler;
        }

        public ContentHandler getContentHandler() {
            return contentHandler;
        }

        public void setErrorHandler(ErrorHandler errorHandler) {
        }

        public ErrorHandler getErrorHandler() {
            return null;
        }

        public void parse(InputSource inputSource) throws IOException, SAXException {
        }

        public void parse(String s) throws IOException, SAXException {
        }
    }
}
