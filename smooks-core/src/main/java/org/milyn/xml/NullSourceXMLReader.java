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
package org.milyn.xml;

import org.milyn.container.ExecutionContext;
import org.milyn.xml.SmooksXMLReader;
import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.XMLConstants;
import java.io.IOException;

/**
 * Null Source reader.
 * <p/>
 * Used for null sources.  Fires just a single root element ("#document" element)
 * event that can be targeted through the "#document" selector.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class NullSourceXMLReader implements SmooksXMLReader {

    public static final String NULLSOURCE_DOCUMENT_ELEMENT_LOCALNAME = "nullsource-document";
    
    private ContentHandler handler;

    public void setContentHandler(ContentHandler handler) {
        this.handler = handler;
    }

    public void parse(InputSource input) throws IOException, SAXException {
        handler.startDocument();
        handler.startElement(XMLConstants.NULL_NS_URI, NULLSOURCE_DOCUMENT_ELEMENT_LOCALNAME, "", EMPTY_ATTRIBS);
        handler.endElement(XMLConstants.NULL_NS_URI, NULLSOURCE_DOCUMENT_ELEMENT_LOCALNAME, "");
        handler.endDocument();
    }

    public void setExecutionContext(ExecutionContext executionContext) {
    }

    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return false;
    }

    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return null;
    }

    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    public void setEntityResolver(EntityResolver resolver) {
    }

    public EntityResolver getEntityResolver() {
        return null;
    }

    public void setDTDHandler(DTDHandler handler) {
    }

    public DTDHandler getDTDHandler() {
        return null;
    }

    public ContentHandler getContentHandler() {
        return null;
    }

    public void setErrorHandler(ErrorHandler handler) {
    }

    public ErrorHandler getErrorHandler() {
        return null;
    }

    private static Attributes EMPTY_ATTRIBS = new AttributesImpl();

    public void parse(String systemId) throws IOException, SAXException {
    }
}