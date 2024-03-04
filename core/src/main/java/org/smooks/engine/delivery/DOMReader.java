/*-
 * ========================LICENSE_START=================================
 * Core
 * %%
 * Copyright (C) 2020 - 2021 Smooks
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
/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */

package org.smooks.engine.delivery;

import org.smooks.api.ExecutionContext;
import org.smooks.api.resource.reader.SmooksXMLReader;
import org.smooks.io.DocumentInputSource;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.LocatorImpl;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.*;

/**
 * <p>
 * <code>SAXWriter</code> writes a DOM tree to a SAX ContentHandler.
 * </p>
 *
 * @author <a href="mailto:james.strachan@metastuff.com">James Strachan </a>
 * @version $Revision: 1.24 $
 */
public class DOMReader implements SmooksXMLReader {
    protected static final String[] LEXICAL_HANDLER_NAMES = {
            "http://xml.org/sax/properties/lexical-handler",
            "http://xml.org/sax/handlers/LexicalHandler"};

    protected static final String FEATURE_NAMESPACE_PREFIXES
            = "http://xml.org/sax/features/namespace-prefixes";

    protected static final String FEATURE_NAMESPACES
            = "http://xml.org/sax/features/namespaces";

    /**
     * <code>ContentHandler</code> to which SAX events are raised
     */
    private ContentHandler contentHandler;

    /**
     * <code>DTDHandler</code> fired when a document has a DTD
     */
    private DTDHandler dtdHandler;

    /**
     * <code>EntityResolver</code> fired when a document has a DTD
     */
    private EntityResolver entityResolver;

    private ErrorHandler errorHandler;

    /**
     * <code>LexicalHandler</code> fired on Entity and CDATA sections
     */
    private LexicalHandler lexicalHandler;

    /**
     * <code>AttributesImpl</code> used when generating the Attributes
     */
    private AttributesImpl attributes = new AttributesImpl();

    /**
     * Stores the features
     */
    private Map<String, Boolean> features = new HashMap();

    /**
     * Stores the properties
     */
    private Map<String, Object> properties = new HashMap();

    /**
     * Whether namespace declarations are exported as attributes or not
     */
    private boolean declareNamespaceAttributes;
    private ExecutionContext executionContext;

    public DOMReader() {
        properties.put(FEATURE_NAMESPACE_PREFIXES, Boolean.FALSE);
        properties.put(FEATURE_NAMESPACE_PREFIXES, Boolean.TRUE);
    }

    public DOMReader(ContentHandler contentHandler) {
        this();
        this.contentHandler = contentHandler;
    }

    public DOMReader(ContentHandler contentHandler,
                     LexicalHandler lexicalHandler) {
        this();
        this.contentHandler = contentHandler;
        this.lexicalHandler = lexicalHandler;
    }

    public DOMReader(ContentHandler contentHandler,
                     LexicalHandler lexicalHandler, EntityResolver entityResolver) {
        this();
        this.contentHandler = contentHandler;
        this.lexicalHandler = lexicalHandler;
        this.entityResolver = entityResolver;
    }

    /**
     * A polymorphic method to write any Node to this SAX stream
     *
     * @param node DOCUMENT ME!
     * @throws SAXException DOCUMENT ME!
     */
    public void write(Node node) throws SAXException {
        int nodeType = node.getNodeType();

        switch (nodeType) {
            case Node.ELEMENT_NODE:
                write((Element) node);
                break;

            case Node.TEXT_NODE:
                write(node.getTextContent());
                break;

            case Node.CDATA_SECTION_NODE:
                write((CDATASection) node);
                break;

            case Node.ENTITY_REFERENCE_NODE:
                write((Entity) node);
                break;

            case Node.PROCESSING_INSTRUCTION_NODE:
                write((ProcessingInstruction) node);
                break;

            case Node.COMMENT_NODE:
                write((Comment) node);
                break;

            case Node.DOCUMENT_NODE:
                write((Document) node);
                break;

            case Node.DOCUMENT_TYPE_NODE:
                write(node);
                break;

            default:
                throw new SAXException("Invalid node type: " + node);
        }
    }

    /**
     * Generates SAX events for the given Document and all its content
     *
     * @param document is the Document to parse
     * @throws SAXException if there is a SAX error processing the events
     */
    public void write(Document document) throws SAXException {
        if (document != null) {
            checkForNullHandlers();

            documentLocator(document);
            startDocument();
            entityResolver(document);
            dtdHandler(document);

            writeContent(document, new Stack<>());
            endDocument();
        }
    }

    /**
     * Generates SAX events for the given Element and all its content
     *
     * @param element is the Element to parse
     * @throws SAXException if there is a SAX error processing the events
     */
    public void write(Element element) throws SAXException {
        write(element, new Stack<>());
    }

    /**
     * <p>
     * Writes the opening tag of an {@link Element}, including its {@link
     * Attribute}s but without its content.
     * </p>
     *
     * @param element <code>Element</code> to output.
     * @throws SAXException DOCUMENT ME!
     */
    public void writeOpen(Element element) throws SAXException {
        startElement(element, null);
    }

    /**
     * <p>
     * Writes the closing tag of an {@link Element}
     * </p>
     *
     * @param element <code>Element</code> to output.
     * @throws SAXException DOCUMENT ME!
     */
    public void writeClose(Element element) throws SAXException {
        endElement(element);
    }

    /**
     * Generates SAX events for the given text
     *
     * @param text is the text to send to the SAX ContentHandler
     * @throws SAXException if there is a SAX error processing the events
     */
    public void write(String text) throws SAXException {
        if (text != null) {
            char[] chars = text.toCharArray();
            contentHandler.characters(chars, 0, chars.length);
        }
    }

    /**
     * Generates SAX events for the given CDATA
     *
     * @param cdata is the CDATA to parse
     * @throws SAXException if there is a SAX error processing the events
     */
    public void write(CDATASection cdata) throws SAXException {
        String text = cdata.getData();

        if (lexicalHandler != null) {
            lexicalHandler.startCDATA();
            write(text);
            lexicalHandler.endCDATA();
        } else {
            write(text);
        }
    }

    /**
     * Generates SAX events for the given Comment
     *
     * @param comment is the Comment to parse
     * @throws SAXException if there is a SAX error processing the events
     */
    public void write(Comment comment) throws SAXException {
        if (lexicalHandler != null) {
            String text = comment.getData();
            char[] chars = text.toCharArray();
            lexicalHandler.comment(chars, 0, chars.length);
        }
    }

    /**
     * Generates SAX events for the given Entity
     *
     * @param entity is the Entity to parse
     * @throws SAXException if there is a SAX error processing the events
     */
    public void write(Entity entity) throws SAXException {
        String text = entity.getTextContent();

        if (lexicalHandler != null) {
            String name = entity.getNodeName();
            lexicalHandler.startEntity(name);
            write(text);
            lexicalHandler.endEntity(name);
        } else {
            write(text);
        }
    }

    /**
     * Generates SAX events for the given ProcessingInstruction
     *
     * @param pi is the ProcessingInstruction to parse
     * @throws SAXException if there is a SAX error processing the events
     */
    public void write(ProcessingInstruction pi) throws SAXException {
        String target = pi.getTarget();
        String text = pi.getData();
        contentHandler.processingInstruction(target, text);
    }

    /**
     * Should namespace declarations be converted to "xmlns" attributes. This
     * property defaults to <code>false</code> as per the SAX specification.
     * This property is set via the SAX feature
     * "http://xml.org/sax/features/namespace-prefixes"
     *
     * @return DOCUMENT ME!
     */
    public boolean isDeclareNamespaceAttributes() {
        return declareNamespaceAttributes;
    }

    /**
     * Sets whether namespace declarations should be exported as "xmlns"
     * attributes or not. This property is set from the SAX feature
     * "http://xml.org/sax/features/namespace-prefixes"
     *
     * @param declareNamespaceAttrs DOCUMENT ME!
     */
    public void setDeclareNamespaceAttributes(boolean declareNamespaceAttrs) {
        this.declareNamespaceAttributes = declareNamespaceAttrs;
    }

    // XMLReader methods
    // -------------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return the <code>ContentHandler</code> called when SAX events are
     * raised
     */
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    /**
     * Sets the <code>ContentHandler</code> called when SAX events are raised
     *
     * @param contentHandler is the <code>ContentHandler</code> called when SAX events
     *                       are raised
     */
    public void setContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the <code>DTDHandler</code>
     */
    public DTDHandler getDTDHandler() {
        return dtdHandler;
    }

    /**
     * Sets the <code>DTDHandler</code>.
     *
     * @param handler DOCUMENT ME!
     */
    public void setDTDHandler(DTDHandler handler) {
        this.dtdHandler = handler;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the <code>ErrorHandler</code>
     */
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * Sets the <code>ErrorHandler</code>.
     *
     * @param errorHandler DOCUMENT ME!
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the <code>EntityResolver</code> used when a Document contains a
     * DTD
     */
    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    /**
     * Sets the <code>EntityResolver</code>.
     *
     * @param entityResolver is the <code>EntityResolver</code>
     */
    public void setEntityResolver(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the <code>LexicalHandler</code> used when a Document contains a
     * DTD
     */
    public LexicalHandler getLexicalHandler() {
        return lexicalHandler;
    }

    /**
     * Sets the <code>LexicalHandler</code>.
     *
     * @param lexicalHandler is the <code>LexicalHandler</code>
     */
    public void setLexicalHandler(LexicalHandler lexicalHandler) {
        this.lexicalHandler = lexicalHandler;
    }

    /**
     * Sets the <code>XMLReader</code> used to write SAX events to
     *
     * @param xmlReader is the <code>XMLReader</code>
     */
    public void setXMLReader(XMLReader xmlReader) {
        setContentHandler(xmlReader.getContentHandler());
        setDTDHandler(xmlReader.getDTDHandler());
        setEntityResolver(xmlReader.getEntityResolver());
        setErrorHandler(xmlReader.getErrorHandler());
    }

    /**
     * Looks up the value of a feature.
     *
     * @param name DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws SAXNotRecognizedException DOCUMENT ME!
     * @throws SAXNotSupportedException  DOCUMENT ME!
     */
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        Boolean answer = features.get(name);

        return (answer != null) && answer;
    }

    /**
     * This implementation does actually use any features but just stores them
     * for later retrieval
     *
     * @param name  DOCUMENT ME!
     * @param value DOCUMENT ME!
     * @throws SAXNotRecognizedException DOCUMENT ME!
     * @throws SAXNotSupportedException  DOCUMENT ME!
     */
    public void setFeature(String name, boolean value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        if (FEATURE_NAMESPACE_PREFIXES.equals(name)) {
            setDeclareNamespaceAttributes(value);
        } else if (FEATURE_NAMESPACE_PREFIXES.equals(name)) {
            if (!value) {
                String msg = "Namespace feature is always supported in dom4j";
                throw new SAXNotSupportedException(msg);
            }
        }

        features.put(name, (value) ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Sets the given SAX property
     *
     * @param name  DOCUMENT ME!
     * @param value DOCUMENT ME!
     */
    public void setProperty(String name, Object value) {
        for (String lexicalHandlerName : LEXICAL_HANDLER_NAMES) {
            if (lexicalHandlerName.equals(name)) {
                setLexicalHandler((LexicalHandler) value);

                return;
            }
        }

        properties.put(name, value);
    }

    /**
     * Gets the given SAX property
     *
     * @param name DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws SAXNotRecognizedException DOCUMENT ME!
     * @throws SAXNotSupportedException  DOCUMENT ME!
     */
    public Object getProperty(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        for (String lexicalHandlerName : LEXICAL_HANDLER_NAMES) {
            if (lexicalHandlerName.equals(name)) {
                return getLexicalHandler();
            }
        }

        return properties.get(name);
    }

    /**
     * This method is not supported.
     *
     * @param systemId DOCUMENT ME!
     * @throws SAXNotSupportedException DOCUMENT ME!
     */
    public void parse(String systemId) throws SAXNotSupportedException {
        throw new SAXNotSupportedException("This XMLReader can only accept"
                + " <dom4j> InputSource objects");
    }

    /**
     * Parses an XML document. This method can only accept DocumentInputSource
     * inputs otherwise a {@link SAXNotSupportedException}exception is thrown.
     *
     * @param input DOCUMENT ME!
     * @throws SAXException             DOCUMENT ME!
     * @throws SAXNotSupportedException if the input source is not wrapping a dom4j document
     */
    public void parse(InputSource input) throws SAXException {
        if (input instanceof DocumentInputSource) {
            Document document = ((DocumentInputSource) input).getDocument();
            write(document);
        } else {
            throw new SAXNotSupportedException("This XMLReader can only accept " + "<dom4j> InputSource objects");
        }
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected void writeContent(Node branch, Stack<QName> namespaceStack) throws SAXException {
        NodeList childNodes = branch.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);

            if (childNode instanceof Element) {
                write((Element) childNode, namespaceStack);
            } else if (childNode instanceof CharacterData) {
                if (childNode instanceof CDATASection) {
                    write((CDATASection) childNode);
                } else if (childNode instanceof Text) {
                    Text text = (Text) childNode;
                    write(text.getData());
                } else if (childNode instanceof Comment) {
                    write((Comment) childNode);
                } else {
                    throw new SAXException("Invalid node in DOM content: " + childNode + " of type: " + childNode.getClass());
                }
            } else if (childNode instanceof Entity) {
                write((Entity) childNode);
            } else if (childNode instanceof ProcessingInstruction) {
                write((ProcessingInstruction) childNode);
            } else {
                throw new SAXException("Invalid node in DOM content: " + childNode);
            }
        }
    }

    /**
     * The {@link org.xml.sax.Locator}is only really useful when parsing a
     * textual document as its main purpose is to identify the line and column
     * number. Since we are processing an in memory tree which will probably
     * have its line number information removed, we'll just use -1 for the line
     * and column numbers.
     *
     * @param document DOCUMENT ME!
     * @throws SAXException DOCUMENT ME!
     */
    protected void documentLocator(Document document) throws SAXException {
        LocatorImpl locator = new LocatorImpl();

        String publicID = null;
        String systemID = null;
        DocumentType docType = document.getDoctype();

        if (docType != null) {
            publicID = docType.getPublicId();
            systemID = docType.getSystemId();
        }

        if (publicID != null) {
            locator.setPublicId(publicID);
        }

        if (systemID != null) {
            locator.setSystemId(systemID);
        }

        locator.setLineNumber(-1);
        locator.setColumnNumber(-1);

        contentHandler.setDocumentLocator(locator);
    }

    protected void entityResolver(Document document) throws SAXException {
        if (entityResolver != null) {
            DocumentType docType = document.getDoctype();

            if (docType != null) {
                String publicID = docType.getPublicId();
                String systemID = docType.getSystemId();

                if ((publicID != null) || (systemID != null)) {
                    try {
                        entityResolver.resolveEntity(publicID, systemID);
                    } catch (IOException e) {
                        throw new SAXException("Could not resolve publicID: "
                                + publicID + " systemID: " + systemID, e);
                    }
                }
            }
        }
    }

    /**
     * We do not yet support DTD or XML Schemas so this method does nothing
     * right now.
     *
     * @param document DOCUMENT ME!
     * @throws SAXException DOCUMENT ME!
     */
    protected void dtdHandler(Document document) throws SAXException {
    }

    protected void startDocument() throws SAXException {
        contentHandler.startDocument();
    }

    protected void endDocument() throws SAXException {
        contentHandler.endDocument();
    }

    protected void write(Element element, Stack<QName> namespaceStack)
            throws SAXException {
        int stackSize = namespaceStack.size();
        AttributesImpl namespaceAttributes = startPrefixMapping(element, namespaceStack);
        startElement(element, namespaceAttributes);
        writeContent(element, namespaceStack);
        endElement(element);
        endPrefixMapping(namespaceStack, stackSize);
    }

    /**
     * Fires a SAX startPrefixMapping event for all the namespaceStack which
     * have just come into scope
     *
     * @param element        DOCUMENT ME!
     * @param namespaceStack DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws SAXException DOCUMENT ME!
     */
    protected AttributesImpl startPrefixMapping(Element element, Stack<QName> namespaceStack) throws SAXException {
        AttributesImpl namespaceAttributes = null;

        // start with the namespace of the element
        String elementNamespace = element.getNamespaceURI();
        final QName elementQName;
        String elementLocalName = element.getLocalName() == null ? element.getNodeName() : element.getLocalName();
        if (element.getPrefix() == null) {
            elementQName = new QName(elementNamespace, elementLocalName);
        } else {
            elementQName = new QName(elementNamespace, elementLocalName, element.getPrefix());
        }

        if ((elementNamespace != null) && !isIgnoreableNamespace(elementQName, namespaceStack)) {
            namespaceStack.push(elementQName);
            contentHandler.startPrefixMapping(element.getPrefix(), elementNamespace);
            namespaceAttributes = addNamespaceAttribute(namespaceAttributes, elementQName);
        }

        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            String attrLocalName = attribute.getLocalName() == null ? attribute.getNodeName() : attribute.getLocalName();
            if (attribute.getNamespaceURI() != null && attribute.getNamespaceURI().equals(XMLConstants.XML_NS_URI) && isIgnoreableNamespace(new QName(attribute.getNodeValue(), attrLocalName), namespaceStack)) {
                QName attrQName = new QName(attribute.getNodeValue(), attrLocalName);
                namespaceStack.push(attrQName);
                contentHandler.startPrefixMapping(attrLocalName, attribute.getNodeValue());
                namespaceAttributes = addNamespaceAttribute(namespaceAttributes, attrQName);
            }
        }

        return namespaceAttributes;
    }

    /**
     * Fires a SAX endPrefixMapping event for all the namespaceStack which have
     * gone out of scope
     *
     * @param stack     DOCUMENT ME!
     * @param stackSize DOCUMENT ME!
     * @throws SAXException DOCUMENT ME!
     */
    protected void endPrefixMapping(Stack<QName> stack, int stackSize) throws SAXException {
        while (stack.size() > stackSize) {
            QName namespace = stack.pop();

            if (namespace != null) {
                contentHandler.endPrefixMapping(namespace.getPrefix());
            }
        }
    }

    protected void startElement(Element element, AttributesImpl namespaceAttributes) throws SAXException {
        String localName = element.getLocalName() == null ? element.getNodeName() : element.getLocalName();
        contentHandler.startElement(element.getNamespaceURI(), localName, getQName(element.getPrefix(), localName), createAttributes(element, namespaceAttributes));
    }

    protected void endElement(Element element) throws SAXException {
        String localName = element.getLocalName() == null ? element.getNodeName() : element.getLocalName();
        contentHandler.endElement(element.getNamespaceURI(), localName, getQName(element.getPrefix(), localName));
    }

    protected Attributes createAttributes(Element element, Attributes namespaceAttributes) throws SAXException {
        attributes.clear();

        if (namespaceAttributes != null) {
            attributes.setAttributes(namespaceAttributes);
        }

        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            String localName = attribute.getLocalName() == null ? attribute.getNodeName() : attribute.getLocalName();
            this.attributes.addAttribute(attribute.getNamespaceURI(), localName, getQName(attribute.getPrefix(), localName), "CDATA", attribute.getNodeValue());
        }

        return this.attributes;
    }

    /**
     * If isDelcareNamespaceAttributes() is enabled then this method will add
     * the given namespace declaration to the supplied attributes object,
     * creating one if it does not exist.
     *
     * @param attrs     DOCUMENT ME!
     * @param namespace DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    protected AttributesImpl addNamespaceAttribute(AttributesImpl attrs, QName namespace) {
        if (declareNamespaceAttributes) {
            if (attrs == null) {
                attrs = new AttributesImpl();
            }

            String prefix = namespace.getPrefix();
            String qualifiedName = "xmlns";

            String localName;
            if ((prefix != null) && (prefix.length() > 0)) {
                qualifiedName = "xmlns:" + prefix;
                localName = prefix;
            } else {
                localName = qualifiedName;
            }

            String uri = "";
            String type = "CDATA";
            String value = namespace.getNamespaceURI();

            attrs.addAttribute(uri, localName, qualifiedName, type, value);
        }

        return attrs;
    }

    /**
     * DOCUMENT ME!
     *
     * @param namespace      DOCUMENT ME!
     * @param namespaceStack DOCUMENT ME!
     * @return true if the given namespace is an ignorable namespace (such as
     * Namespace.NO_NAMESPACE or Namespace.XML_NAMESPACE) or if the
     * namespace has already been declared in the current scope
     */
    protected boolean isIgnoreableNamespace(QName namespace, Stack<QName> namespaceStack) {
        if ((namespace.getPrefix().equals(XMLConstants.DEFAULT_NS_PREFIX) && namespace.getNamespaceURI().equals(XMLConstants.NULL_NS_URI)) || (namespace.getPrefix().equals(XMLConstants.XML_NS_PREFIX) && namespace.getNamespaceURI().equals(XMLConstants.XML_NS_URI))) {
            return true;
        }

        for (QName qName : namespaceStack) {
            if (qName.getPrefix().equals(namespace.getPrefix()) && qName.getNamespaceURI().equals(namespace.getNamespaceURI())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Ensures non-null content handlers?
     */
    protected void checkForNullHandlers() {
    }

    protected String getQName(String prefix, String localName) {
        return (prefix != null && prefix.length() > 0) ? prefix + ":" + localName : localName;
    }

    @Override
    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }
}

/*
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright statements and
 * notices. Redistributions must also contain a copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. The name "DOM4J" must not be used to endorse or promote products derived
 * from this Software without prior written permission of MetaStuff, Ltd. For
 * written permission, please contact dom4j-info@metastuff.com.
 *
 * 4. Products derived from this Software may not be called "DOM4J" nor may
 * "DOM4J" appear in their names without prior written permission of MetaStuff,
 * Ltd. DOM4J is a registered trademark of MetaStuff, Ltd.
 *
 * 5. Due credit should be given to the DOM4J Project - http://www.dom4j.org
 *
 * THIS SOFTWARE IS PROVIDED BY METASTUFF, LTD. AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL METASTUFF, LTD. OR ITS CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 */
