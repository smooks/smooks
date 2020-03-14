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
package org.smooks.general;

import org.smooks.container.ExecutionContext;
import org.smooks.xml.SmooksXMLReader;
import org.smooks.delivery.sax.SAXContentDeliveryConfig;
import org.smooks.delivery.sax.SAXElementVisitorMap;
import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.ext.LexicalHandler;

import javax.xml.stream.*;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class StAX2SAXReader implements SmooksXMLReader {

    private XMLInputFactory staxInputFactory = XMLInputFactory.newInstance();
    private ExecutionContext executionContext;
    private ContentHandler contentHandler;
    private LexicalHandler lexicalHandler;
    private AttributesImpl attributes = new AttributesImpl();
    private SAXContentDeliveryConfig deliveryConfig;
    private Map<String, SAXElementVisitorMap> visitorConfigMap;

    public StAX2SAXReader() {
        staxInputFactory.setProperty(XMLInputFactory.IS_COALESCING, false);
    }

    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
        deliveryConfig = ((SAXContentDeliveryConfig)executionContext.getDeliveryConfig());
        visitorConfigMap = deliveryConfig.getOptimizedVisitorConfig();
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
        if("http://xml.org/sax/properties/lexical-handler".equals(name)) {
            lexicalHandler = (LexicalHandler) value;
        }
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

    public void setContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    public void setErrorHandler(ErrorHandler handler) {
    }

    public ErrorHandler getErrorHandler() {
        return null;
    }

    public void parse(InputSource input) throws IOException, SAXException {
        XMLStreamReader staxReader = openStAXReader(input);

        contentHandler.startDocument();
        try {
            eatMessage(staxReader);
            //moveToNextElement(staxReader);
            //parseElement(staxReader);
        } catch (XMLStreamException e) {
            throw new SAXException("Error reading XML Stream.", e);
        } finally {
            contentHandler.endDocument();            
        }
    }

    private void parseElement(XMLStreamReader staxReader) throws XMLStreamException, SAXException {
        QName name = staxReader.getName();
        String nsUri = name.getNamespaceURI();
        String localPart = name.getLocalPart();
        String nsPrefix = name.getPrefix();
        String qName;

        if(nsPrefix != null) {
            qName = nsPrefix + ":" + localPart;
        } else {
            qName = localPart;
        }
        contentHandler.startElement(nsUri, localPart, qName, getAttributes(staxReader));

        while(staxReader.hasNext()) {
            staxReader.next();
            switch (staxReader.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    parseElement(staxReader);
                    break;
                case XMLStreamConstants.CHARACTERS:
                    contentHandler.characters(staxReader.getTextCharacters(), staxReader.getTextStart(), staxReader.getTextLength());
                    break;
                case XMLStreamConstants.COMMENT:
                    lexicalHandler.comment(staxReader.getTextCharacters(), staxReader.getTextStart(), staxReader.getTextLength());
                    break;
                case XMLStreamConstants.CDATA:
                    lexicalHandler.startCDATA();
                    contentHandler.characters(staxReader.getTextCharacters(), staxReader.getTextStart(), staxReader.getTextLength());
                    lexicalHandler.endCDATA();
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    contentHandler.endElement(nsUri, localPart, qName);
                    return;
            }
        }
    }

    private void eatMessage(XMLStreamReader staxReader) throws XMLStreamException {
        while(staxReader.hasNext()) {
            staxReader.next();
            switch (staxReader.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    String elementName = staxReader.getLocalName();
                    SAXElementVisitorMap config = visitorConfigMap.get(elementName);
                    if(config != null) {
                        QName qName = staxReader.getName();
                    }
                    break;
            }
        }
    }

    private void moveToNextElement(XMLStreamReader staxReader) throws XMLStreamException {
        while(staxReader.hasNext()) {
            staxReader.next();
            switch (staxReader.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    return;
            }
        }
    }

    private Attributes getAttributes(XMLStreamReader staxReader) {
        int attributeCount = staxReader.getAttributeCount();

        attributes.clear();
        for(int i = 0; i < attributeCount; i++) {
            String localPart = staxReader.getAttributeLocalName(i);
            String nsPrefix = staxReader.getAttributePrefix(i);

            if(nsPrefix != null) {
                attributes.addAttribute(staxReader.getAttributeNamespace(i), localPart, nsPrefix + ":" + localPart, null, staxReader.getAttributeValue(i));
            } else {
                attributes.addAttribute(staxReader.getAttributeNamespace(i), localPart, localPart, null, staxReader.getAttributeValue(i));
            }
        }

        return attributes;
    }

    private String getText(XMLStreamReader staxReader) {
        return new String(staxReader.getTextCharacters(), staxReader.getTextStart(), staxReader.getTextLength());
    }

    private XMLStreamReader openStAXReader(InputSource input) throws SAXException, IOException {
        XMLStreamReader staxReader;

        Reader inputReader = input.getCharacterStream();
        if (inputReader != null) {
            try {
                staxReader = staxInputFactory.createXMLStreamReader(inputReader);
            } catch (XMLStreamException e) {
                throw new SAXException("Failed to create " + XMLStreamReader.class.getName() + " instance from " + inputReader.getClass().getName() + ".", e);
            }
        } else {
            InputStream inputStream = input.getByteStream();
            if (inputStream != null) {
                try {
                    staxReader = staxInputFactory.createXMLStreamReader(inputStream, executionContext.getContentEncoding());
                } catch (XMLStreamException e) {
                    throw new SAXException("Failed to create " + XMLStreamReader.class.getName() + " instance from " + inputStream.getClass().getName() + ".", e);
                }
            } else {
                throw new IOException("Invalid 'input' InputSource.  Not Character or Byte Stream set.");
            }
        }

        return staxReader;
    }

    public void parse(String systemId) throws IOException, SAXException {
    }
}
