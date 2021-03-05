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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.stax;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.stax.events.AttributeImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.stax.events.CharactersImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.stax.events.CommentImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.stax.events.DTDImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.stax.events.EndDocumentImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.stax.events.EndElementImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.stax.events.EntityReferenceImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.stax.events.NamespaceImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.stax.events.ProcessingInstructionImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.stax.events.StartDocumentImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.stax.events.StartElementImpl;

/**
 * <p>Implementation of XMLEventFactory.</p>
 * 
 * @xerces.internal
 * 
 * @version $Id$
 */
public final class XMLEventFactoryImpl extends XMLEventFactory {
    
    private Location fLocation;

    public XMLEventFactoryImpl() {}
    
    public void setLocation(Location location) {
        fLocation = location;
    }
    
    public Attribute createAttribute(String prefix, String namespaceURI,
            String localName, String value) {
        return createAttribute(new QName(namespaceURI, localName, prefix), value);
    }

    public Attribute createAttribute(String localName, String value) {
        return createAttribute(new QName(localName), value);
    }

    public Attribute createAttribute(QName name, String value) {
        return new AttributeImpl(name, value, "CDATA", true, fLocation);
    }
    
    public Namespace createNamespace(String namespaceURI) {
        return createNamespace(XMLConstants.DEFAULT_NS_PREFIX, namespaceURI);
    }

    public Namespace createNamespace(String prefix, String namespaceUri) {
        return new NamespaceImpl(prefix, namespaceUri, fLocation);
    }
    
    public StartElement createStartElement(QName name, Iterator attributes,
            Iterator namespaces) {
        return createStartElement(name, attributes, namespaces, null);
    }
    
    public StartElement createStartElement(String prefix, String namespaceUri,
            String localName) {
        return createStartElement(new QName(namespaceUri, localName, prefix), null, null);
    }
  
    public StartElement createStartElement(String prefix, String namespaceUri,
            String localName, Iterator attributes, Iterator namespaces) {
        return createStartElement(new QName(namespaceUri, localName, prefix), attributes, namespaces);
    }
    
    public StartElement createStartElement(String prefix, String namespaceUri,
            String localName, Iterator attributes, Iterator namespaces,
            NamespaceContext context) {
        return createStartElement(new QName(namespaceUri, localName, prefix), attributes, namespaces, context);
    }
    
    private StartElement createStartElement(QName name, Iterator attributes,
            Iterator namespaces, NamespaceContext context) {
        return new StartElementImpl(name, attributes, namespaces, context, fLocation);
    }

    public EndElement createEndElement(QName name, Iterator namespaces) {
        return new EndElementImpl(name, namespaces, fLocation);
    }

    public EndElement createEndElement(String prefix, String namespaceUri,
            String localName) {
        return createEndElement(new QName(namespaceUri, localName, prefix), null);
    }
    
    public EndElement createEndElement(String prefix, String namespaceUri,
            String localName, Iterator namespaces) {
        return createEndElement(new QName(namespaceUri, localName, prefix), namespaces);
    }
    
    public Characters createCharacters(String content) {
        return new CharactersImpl(content, XMLStreamConstants.CHARACTERS, fLocation);
    }

    public Characters createCData(String content) {
        return new CharactersImpl(content, XMLStreamConstants.CDATA, fLocation);
    }

    public Characters createSpace(String content) {
        return createCharacters(content);
    }

    public Characters createIgnorableSpace(String content) {
        return new CharactersImpl(content, XMLStreamConstants.SPACE, fLocation);
    }
    
    public StartDocument createStartDocument() {
        return createStartDocument(null, null);
    }
    
    public StartDocument createStartDocument(String encoding, String version,
            boolean standalone) {
        return new StartDocumentImpl(encoding, encoding != null, standalone, true, version, fLocation);
    }
    
    public StartDocument createStartDocument(String encoding, String version) {
        return new StartDocumentImpl(encoding, encoding != null, false, false, version, fLocation);
    }

    public StartDocument createStartDocument(String encoding) {
        return createStartDocument(encoding, null);
    }
    
    public EndDocument createEndDocument() {
        return new EndDocumentImpl(fLocation);
    }
    
    public EntityReference createEntityReference(String name,
            EntityDeclaration declaration) {
        return new EntityReferenceImpl(name, declaration, fLocation);
    }
    
    public Comment createComment(String text) {
        return new CommentImpl(text, fLocation);
    }
    
    public ProcessingInstruction createProcessingInstruction(String target,
            String data) {
        return new ProcessingInstructionImpl(target, data, fLocation);
    }
    
    public DTD createDTD(String dtd) {
        return new DTDImpl(dtd, fLocation);
    }
}
