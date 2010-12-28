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
package org.milyn.delivery;

import org.xml.sax.ContentHandler;
import org.xml.sax.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class TestXMLReader implements XMLReader {

    public Map<String, Boolean> features = new HashMap<String, Boolean>();
    public DTDHandler dtdHandler;
    public ErrorHandler errorHandler;
    public EntityResolver entityResolver;

    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return false;
    }

    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        features.put(name, value);
    }

    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return null;
    }

    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    public void setEntityResolver(EntityResolver resolver) {
        entityResolver = resolver;
    }

    public EntityResolver getEntityResolver() {
        return null;
    }

    public void setDTDHandler(DTDHandler handler) {
        dtdHandler = handler;
    }

    public DTDHandler getDTDHandler() {
        return null;
    }

    public void setContentHandler(ContentHandler handler) {
    }

    public ContentHandler getContentHandler() {
        return null;
    }

    public void setErrorHandler(ErrorHandler handler) {
        errorHandler = handler;
    }

    public TestErrorHandler getErrorHandler() {
        return null;
    }

    public void parse(InputSource input) throws IOException, SAXException {
    }

    public void parse(String systemId) throws IOException, SAXException {
    }
}
