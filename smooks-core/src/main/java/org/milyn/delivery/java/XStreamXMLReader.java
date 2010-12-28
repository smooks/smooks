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
package org.milyn.delivery.java;

import com.thoughtworks.xstream.io.xml.SaxWriter;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.payload.JavaSource;
import org.xml.sax.*;

import java.io.IOException;
import java.util.List;

/**
 * XStream based {@link JavaXMLReader}.
 * <p/>
 * This is the default Java {@link XMLReader} for Smooks.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class XStreamXMLReader implements JavaXMLReader {

    @ConfigParam(defaultVal = "true")
    private boolean includeEnclosingDocument = true;

    private SaxWriter xstreamReader;
    
    @Initialize
    public void intialize() {
        xstreamReader = new SaxWriter(includeEnclosingDocument);
    }

    public void setSourceObjects(List<Object> sourceObjects) throws SmooksConfigurationException {
        try {
            xstreamReader.setProperty(SaxWriter.SOURCE_OBJECT_LIST_PROPERTY, sourceObjects);
        } catch (SAXNotRecognizedException e) {
            throw new SmooksConfigurationException("Unable to set source Java Objects on the underlying XStream SaxWriter.", e);
        } catch (SAXNotSupportedException e) {
            throw new SmooksConfigurationException("Unable to set source Java Objects on the underlying XStream SaxWriter.", e);
        }
    }

    public void setExecutionContext(ExecutionContext executionContext) {
    }

    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return xstreamReader.getFeature(name);
    }

    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        // Need to ignore some features....
        if(name.equals(JavaSource.FEATURE_GENERATE_EVENT_STREAM)) {
            return;
        }
        xstreamReader.setFeature(name, value);
    }

    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return xstreamReader.getProperty(name);
    }

    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        xstreamReader.setProperty(name, value);
    }

    public void setEntityResolver(EntityResolver resolver) {
        xstreamReader.setEntityResolver(resolver);
    }

    public EntityResolver getEntityResolver() {
        return xstreamReader.getEntityResolver();
    }

    public void setDTDHandler(DTDHandler handler) {
        xstreamReader.setDTDHandler(handler);
    }

    public DTDHandler getDTDHandler() {
        return xstreamReader.getDTDHandler();
    }

    public void setContentHandler(ContentHandler handler) {
        xstreamReader.setContentHandler(handler);
    }

    public ContentHandler getContentHandler() {
        return xstreamReader.getContentHandler();
    }

    public void setErrorHandler(ErrorHandler handler) {
        xstreamReader.setErrorHandler(handler);
    }

    public ErrorHandler getErrorHandler() {
        return xstreamReader.getErrorHandler();
    }

    public void parse(InputSource input) throws IOException, SAXException {
        xstreamReader.parse(input);
    }

    public void parse(String systemId) throws IOException, SAXException {
        xstreamReader.parse(systemId);
    }
}
