/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.validation;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stax.StAXResult;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.validation.StAXDocumentHandler;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.validation.StAXValidatorHelper;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.JAXPNamespaceContextWrapper;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.NamespaceContext;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.QName;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLAttributes;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLLocator;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLResourceIdentifier;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLString;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLDocumentSource;

/**
 * <p>StAX event result builder.</p>
 * 
 * @author Michael Glavassevich, IBM
 * @version $Id$
 */
final class StAXEventResultBuilder implements org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.validation.StAXDocumentHandler {
    
    //
    // Data
    //
    
    private XMLEventWriter fEventWriter;
    private final XMLEventFactory fEventFactory;
    private final org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.validation.StAXValidatorHelper fStAXValidatorHelper;
    private final JAXPNamespaceContextWrapper fNamespaceContext;
    private boolean fIgnoreChars;
    private boolean fInCDATA;
    private final QName fAttrName = new QName();

    public StAXEventResultBuilder(org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.validation.StAXValidatorHelper helper, JAXPNamespaceContextWrapper context) {
        fStAXValidatorHelper = helper;
        fNamespaceContext = context;
        fEventFactory = XMLEventFactory.newInstance();
    }
    
    /*
     * StAXDocumentHandler methods
     */

    public void setStAXResult(StAXResult result) {
        fIgnoreChars = false;
        fInCDATA = false;
        fEventWriter = (result != null) ? result.getXMLEventWriter() : null;
    }

    public void startDocument(XMLStreamReader reader) throws XMLStreamException {
        String version = reader.getVersion();
        String encoding = reader.getCharacterEncodingScheme();
        boolean standalone = reader.standaloneSet();
        fEventWriter.add(fEventFactory.createStartDocument(encoding != null ? encoding : "UTF-8",
                version != null ? version : "1.0", standalone));
    }

    public void endDocument(XMLStreamReader reader) throws XMLStreamException {
        fEventWriter.add(fEventFactory.createEndDocument());
        fEventWriter.flush();
    }

    public void comment(XMLStreamReader reader) throws XMLStreamException {
        fEventWriter.add(fEventFactory.createComment(reader.getText()));
    }

    public void processingInstruction(XMLStreamReader reader)
            throws XMLStreamException {
        String data = reader.getPIData();
        fEventWriter.add(fEventFactory.createProcessingInstruction(reader.getPITarget(), 
                data != null ? data : ""));
    }

    public void entityReference(XMLStreamReader reader)
            throws XMLStreamException {
        String name = reader.getLocalName();
        fEventWriter.add(fEventFactory.createEntityReference(name, 
                fStAXValidatorHelper.getEntityDeclaration(name)));
    }

    public void startDocument(StartDocument event) throws XMLStreamException {
        fEventWriter.add(event);
    }

    public void endDocument(EndDocument event) throws XMLStreamException {
        fEventWriter.add(event);
        fEventWriter.flush();
    }

    public void doctypeDecl(DTD event) throws XMLStreamException {
        fEventWriter.add(event);
    }

    public void characters(Characters event) throws XMLStreamException {
        fEventWriter.add(event);
    }

    public void cdata(Characters event) throws XMLStreamException {
        fEventWriter.add(event);
    }

    public void comment(Comment event) throws XMLStreamException {
        fEventWriter.add(event);
    }

    public void processingInstruction(ProcessingInstruction event)
            throws XMLStreamException {
        fEventWriter.add(event);
    }

    public void entityReference(EntityReference event)
            throws XMLStreamException {
        fEventWriter.add(event);
    }

    public void setIgnoringCharacters(boolean ignore) {
        fIgnoreChars = ignore;
    }
    
    /*
     * XMLDocumentHandler methods
     */

    public void startDocument(XMLLocator locator, String encoding,
            NamespaceContext namespaceContext, Augmentations augs)
            throws XNIException {}

    public void xmlDecl(String version, String encoding, String standalone,
            Augmentations augs) throws XNIException {}

    public void doctypeDecl(String rootElement, String publicId,
            String systemId, Augmentations augs) throws XNIException {}

    public void comment(XMLString text, Augmentations augs) throws XNIException {}

    public void processingInstruction(String target, XMLString data,
            Augmentations augs) throws XNIException {}

    public void startElement(QName element, XMLAttributes attributes,
            Augmentations augs) throws XNIException {
        try {
            int length = attributes.getLength();
            if (length == 0) {
                /** Avoid creating a new StartElement event object (if possible). */
                XMLEvent start = fStAXValidatorHelper.getCurrentEvent();
                if (start != null) {
                    fEventWriter.add(start);
                    return;
                }
            }
            fEventWriter.add(fEventFactory.createStartElement(element.prefix, 
                    element.uri != null ? element.uri : "", element.localpart, 
                    getAttributeIterator(attributes, length), getNamespaceIterator(),
                    fNamespaceContext.getNamespaceContext()));
        }
        catch (XMLStreamException e) {
            throw new XNIException(e);
        }
    }

    public void emptyElement(QName element, XMLAttributes attributes,
            Augmentations augs) throws XNIException {
        startElement(element, attributes, augs);
        endElement(element, augs);
    }

    public void startGeneralEntity(String name,
            XMLResourceIdentifier identifier, String encoding,
            Augmentations augs) throws XNIException {}

    public void textDecl(String version, String encoding, Augmentations augs)
            throws XNIException {}

    public void endGeneralEntity(String name, Augmentations augs)
            throws XNIException {}

    public void characters(XMLString text, Augmentations augs)
            throws XNIException {
        if (!fIgnoreChars) {
            try {
                if (!fInCDATA) {
                    fEventWriter.add(fEventFactory.createCharacters(text.toString()));
                }
                else {
                    fEventWriter.add(fEventFactory.createCData(text.toString()));
                }
            }
            catch (XMLStreamException e) {
                throw new XNIException(e);
            }
        }
    }

    public void ignorableWhitespace(XMLString text, Augmentations augs)
            throws XNIException {
        characters(text, augs);
    }

    public void endElement(QName element, Augmentations augs)
            throws XNIException {
        try {
            /** Avoid creating a new EndElement event object (if possible). */
            XMLEvent end = fStAXValidatorHelper.getCurrentEvent();
            if (end != null) {
                fEventWriter.add(end);
            }
            else {
                fEventWriter.add(fEventFactory.createEndElement(element.prefix, 
                    element.uri, element.localpart, getNamespaceIterator()));
            }
        }
        catch (XMLStreamException e) {
            throw new XNIException(e);
        }
    }

    public void startCDATA(Augmentations augs) throws XNIException {
        fInCDATA = true;
    }

    public void endCDATA(Augmentations augs) throws XNIException {
        fInCDATA = false;
    }

    public void endDocument(Augmentations augs) throws XNIException {}

    public void setDocumentSource(XMLDocumentSource source) {}

    public XMLDocumentSource getDocumentSource() {
        return null;
    }
    
    /*
     * Other methods.
     */
    
    private Iterator getAttributeIterator(XMLAttributes attributes, int length) {
        return (length > 0) ? new AttributeIterator(attributes, length) : EMPTY_COLLECTION_ITERATOR;
    }
    
    private Iterator getNamespaceIterator() {
        int length = fNamespaceContext.getDeclaredPrefixCount();
        return (length > 0) ? new NamespaceIterator(length) : EMPTY_COLLECTION_ITERATOR;
    }
    
    /**
     * An iterator over XMLAttributes which returns Attribute event objects. 
     */
    final class AttributeIterator implements Iterator {

        XMLAttributes fAttributes;
        int fIndex;
        int fEnd;
        
        AttributeIterator(XMLAttributes attributes, int length) {
            fAttributes = attributes;
            fIndex = 0;
            fEnd = length;
        }
        
        public boolean hasNext() {
            if (fIndex < fEnd) {
                return true;
            }
            fAttributes = null;
            return false;
        }
        
        public Object next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            fAttributes.getName(fIndex, fAttrName);
            return fEventFactory.createAttribute(fAttrName.prefix, 
                    fAttrName.uri != null ? fAttrName.uri : "",
                    fAttrName.localpart, fAttributes.getValue(fIndex++));
        }
        
        public void remove() {
            throw new UnsupportedOperationException();                   
        }
    }
    
    /**
     * An iterator over the current context of a NamespaceContext 
     * which returns Namespace event objects.
     */
    final class NamespaceIterator implements Iterator {
        
        javax.xml.namespace.NamespaceContext fNC;
        int fIndex;
        int fEnd;
        
        NamespaceIterator(int length) {
            fNC = fNamespaceContext.getNamespaceContext();
            fIndex = 0;
            fEnd = length;
        }
        
        public boolean hasNext() {
            if (fIndex < fEnd) {
                return true;
            }
            fNC = null;
            return false;
        }
        
        public Object next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            String prefix = fNamespaceContext.getDeclaredPrefixAt(fIndex++);
            String uri = fNC.getNamespaceURI(prefix);
            if (prefix.length() == 0) {
                return fEventFactory.createNamespace(uri != null ? uri : "");
            }
            else {
                return fEventFactory.createNamespace(prefix, uri != null ? uri : "");
            }
        }
        
        public void remove() {
            throw new UnsupportedOperationException(); 
        }
    }
    
    /**
     * An iterator for an empty collection.
     */
    private static final Iterator EMPTY_COLLECTION_ITERATOR = new Iterator () {
        public boolean hasNext() {
            return false;
        }
        public Object next() {
            throw new NoSuchElementException();
        }
        public void remove() {
            throw new UnsupportedOperationException(); 
        }  
    };

} // StAXEventResultBuilder
