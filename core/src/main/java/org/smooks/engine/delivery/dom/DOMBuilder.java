/*-
 * ========================LICENSE_START=================================
 * Core
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
package org.smooks.engine.delivery.dom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.ExecutionContext;
import org.smooks.api.delivery.Filter;
import org.smooks.engine.resource.config.ParameterAccessor;
import org.smooks.engine.delivery.SmooksContentHandler;
import org.smooks.engine.delivery.replay.EndElementEvent;
import org.smooks.engine.delivery.replay.StartElementEvent;
import org.smooks.engine.xml.DocType;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.HashSet;
import java.util.Stack;

/**
 * DOM Document builder.
 * <p/>
 * Handler class for DOM construction.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@SuppressWarnings("unchecked")
public class DOMBuilder extends SmooksContentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DOMBuilder.class);
    private static final DocumentBuilder documentBuilder;

    private final ExecutionContext execContext;
    private Document ownerDocument;
    private final Stack nodeStack = new Stack();
    private boolean inEntity = false;
    private final HashSet emptyElements = new HashSet();
    private final StringBuilder cdataNodeBuilder = new StringBuilder();
    private final boolean rewriteEntities;

    static {
    	try {
			documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
          throw new IllegalStateException("XML DOM Parsing environment not configured properly.", e);
		}
    }

    public DOMBuilder(ExecutionContext execContext) {
        this(execContext, null);
    }

    public DOMBuilder(ExecutionContext execContext, SmooksContentHandler parentContentHandler) {
        super(execContext, parentContentHandler);

        this.execContext = execContext;
        rewriteEntities = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.ENTITIES_REWRITE, String.class, "true", execContext.getContentDeliveryRuntime().getContentDeliveryConfig()));
    }

    public void startDocument() throws SAXException {
        super.startDocument();
        if(ownerDocument == null) {
            // Parsing a new ownerDocument from scratch - create the DOM Document
            // instance and set it as the startNode.
            ownerDocument = documentBuilder.newDocument();
            // Initialise the stack with the Document node.
            nodeStack.push(ownerDocument);
        }
    }

    /**
     * Get the Document node of the document into which this handler
     * is parsing.
     * @return Returns the ownerDocument.
     */
    public Document getDocument() {
        return ownerDocument;
    }

    /**
     * Set the DOM Element node on which the parsed content it to be added.
     * <p/>
     * Used to merge ownerDocument fragments etc.
     * @param appendElement The append DOM element.
     */
    @SuppressWarnings({ "WeakerAccess", "unchecked" })
    public void setAppendElement(Element appendElement) {
        ownerDocument = appendElement.getOwnerDocument();
        // Initialise the stack with the append element node.
        nodeStack.push(appendElement);
    }

    @SuppressWarnings("RedundantThrows")
    public void endDocument() throws SAXException {
    }

    @SuppressWarnings({ "unchecked", "RedundantThrows" })
    public void startElement(StartElementEvent startEvent) throws SAXException {
        Element newElement;
        int attsCount = startEvent.attributes.getLength();
        Node currentNode = (Node)nodeStack.peek();

        try {
            if(startEvent.uri != null && startEvent.qName != null && !startEvent.qName.equals("")) {
                newElement = ownerDocument.createElementNS(startEvent.uri.intern(), startEvent.qName);
            } else {
                newElement = ownerDocument.createElement(startEvent.localName.intern());
            }

            currentNode.appendChild(newElement);
            if(!emptyElements.contains(startEvent.qName != null?startEvent.qName:startEvent.localName)) {
                nodeStack.push(newElement);
            }
        } catch(DOMException e) {
            LOGGER.error("DOMException creating start element: namespaceURI=" + startEvent.uri + ", localName=" + startEvent.localName, e);
            throw e;
        }

        for(int i = 0; i < attsCount; i++) {
            String attNamespace = startEvent.attributes.getURI(i);
            String attQName = startEvent.attributes.getQName(i);
            String attLocalName = startEvent.attributes.getLocalName(i);
            String attValue = startEvent.attributes.getValue(i);
            try {
                if(attNamespace != null && attQName != null) {
                    attNamespace = attNamespace.intern();
                    if(attNamespace.equals(XMLConstants.NULL_NS_URI)) {
                        if(attQName.startsWith(XMLConstants.XMLNS_ATTRIBUTE)) {
                            attNamespace = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
                        } else if(attQName.startsWith("xml:")) {
                            attNamespace = XMLConstants.XML_NS_URI;
                        }
                    }
                    newElement.setAttributeNS(attNamespace, attQName, attValue);
                } else {
                    newElement.setAttribute(attLocalName.intern(), attValue);
                }
            } catch(DOMException e) {
                LOGGER.error("DOMException setting element attribute " + attLocalName + "=" + attValue + "[namespaceURI=" + startEvent.uri + ", localName=" + startEvent.localName + "].", e);
                throw e;
            }
        }
    }

    @SuppressWarnings("RedundantThrows")
    public void endElement(EndElementEvent endEvent) throws SAXException {
        String elName;

        if(endEvent.qName != null && !endEvent.qName.equals("")) {
            elName = endEvent.qName;
        }else {
            elName = endEvent.localName;
        }

        if(!emptyElements.contains(elName)) {
            int index = getIndex(elName);
            if(index != -1) {
                nodeStack.setSize(index);
            } else {
                LOGGER.debug("Ignoring unexpected end [" + endEvent.localName + "] element event. Request: [" + execContext.getDocumentSource() + "] - document location: [" + getCurPath() + "]");
            }
        }
    }

    @Override
    public void cleanup() {
    }

    private String getCurPath() {
        StringBuilder path = new StringBuilder();
        int stackSize = nodeStack.size();

        for(int i = 0; i < stackSize; i++) {
            Node node = (Node)nodeStack.elementAt(i);
            if(node.getNodeType() == Node.ELEMENT_NODE) {
                path.append('/').append(((Element)node).getTagName());
            }
        }

        return path.toString();
    }

    private int getIndex(String elName) {
        for(int i = nodeStack.size() - 1; i >= 0; i--) {
            Node node = (Node)nodeStack.elementAt(i);
            if(node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element)node;
                if(element.getTagName().equals(elName)) {
                    return i;
                }
            }
        }

        return -1;
    }

    @SuppressWarnings("RedundantThrows")
    public void characters(char[] ch, int start, int length) throws SAXException {
        try {
            Node currentNode = (Node)nodeStack.peek();

            switch (currentNode.getNodeType()) {
            case Node.ELEMENT_NODE:
                if(inEntity && !rewriteEntities) {
                    currentNode.appendChild(ownerDocument.createTextNode("&#"+ (int)ch[start] + ";"));
                } else {
                    currentNode.appendChild(ownerDocument.createTextNode(new String(ch, start, length)));
                }
                break;
            case Node.CDATA_SECTION_NODE:
                cdataNodeBuilder.append(ch, start, length);
                break;
            default:
                break;
            }
        } catch(DOMException e) {
            LOGGER.error("DOMException appending character data [" + new String(ch, start, length) + "]", e);
            throw e;
        }
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        characters(ch, start, length);
    }

    @SuppressWarnings({ "unchecked", "RedundantThrows" })
    public void startCDATA() throws SAXException {
        CDATASection newCDATASection = ownerDocument.createCDATASection("dummy");
        Node currentNode;

        currentNode = (Node)nodeStack.peek();
        currentNode.appendChild(newCDATASection);
        nodeStack.push(newCDATASection);
        cdataNodeBuilder.setLength(0);
    }

    @SuppressWarnings("RedundantThrows")
    public void endCDATA() throws SAXException {
        CDATASection cdata = (CDATASection) nodeStack.pop();
        cdata.setData(cdataNodeBuilder.toString());
        cdataNodeBuilder.setLength(0);
    }

    @SuppressWarnings("RedundantThrows")
    public void comment(char[] ch, int start, int length) throws SAXException {
        try {
            Node currentNode = (Node)nodeStack.peek();
            Comment newComment;

            newComment = ownerDocument.createComment(new String(ch, start, length));

            currentNode.appendChild(newComment);
        } catch(DOMException e) {
            LOGGER.error("DOMException comment data [" + new String(ch, start, length) + "]", e);
            throw e;
        }
    }

    @SuppressWarnings("RedundantThrows")
    public void startEntity(String name) throws SAXException {
        inEntity = true;
    }

    @SuppressWarnings("RedundantThrows")
    public void endEntity(String name) throws SAXException {
        inEntity = false;
    }

    @SuppressWarnings("RedundantThrows")
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        DocumentType docType = documentBuilder.getDOMImplementation().createDocumentType(name, publicId, systemId);

        ownerDocument.appendChild(docType);

        DocType.setDocType(name, publicId, systemId, null, execContext);
    }
}
