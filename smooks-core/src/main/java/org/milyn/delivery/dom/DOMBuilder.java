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
package org.milyn.delivery.dom;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.SmooksContentHandler;
import org.milyn.delivery.replay.EndElementEvent;
import org.milyn.delivery.replay.StartElementEvent;
import org.milyn.dtd.DTDStore;
import org.milyn.xml.DocType;
import org.milyn.cdr.ParameterAccessor;
import org.milyn.delivery.Filter;
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
public class DOMBuilder extends SmooksContentHandler {

    private static Log logger = LogFactory.getLog(DOMBuilder.class);
    private static DocumentBuilder documentBuilder;

    private ExecutionContext execContext;
    private Document ownerDocument;
    private Stack nodeStack = new Stack();
    private boolean inEntity = false;
    private HashSet emptyElements = new HashSet();
    private StringBuilder cdataNodeBuilder = new StringBuilder();
    private boolean rewriteEntities = true;

    static {
    	try {
			documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			IllegalStateException state = new IllegalStateException("XML DOM Parsing environment not configured properly.");
			state.initCause(e);
			throw state;
		}
    }

    public DOMBuilder(ExecutionContext execContext) {
        this(execContext, null);
    }

    public DOMBuilder(ExecutionContext execContext, SmooksContentHandler parentContentHandler) {
        super(execContext, parentContentHandler);
        
        this.execContext = execContext;
        initialiseEmptyElements();
        rewriteEntities = ParameterAccessor.getBoolParameter(Filter.ENTITIES_REWRITE, true, execContext.getDeliveryConfig());
    }

    private void initialiseEmptyElements() {
        DTDStore.DTDObjectContainer dtd = execContext.getDeliveryConfig().getDTD();
        if(dtd != null) {
            String[] emptyEls = dtd.getEmptyElements();

            if(emptyEls != null && emptyEls.length > 0) {
                for(int i = 0; i < emptyEls.length; i++) {
                    emptyElements.add(emptyEls[i]);
                }
            }
        }
    }

    public void startDocument() throws SAXException {
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
    public void setAppendElement(Element appendElement) {
        ownerDocument = appendElement.getOwnerDocument();
        // Initialise the stack with the append element node.
        nodeStack.push(appendElement);
    }

    public void endDocument() throws SAXException {
    }

    public void startElement(StartElementEvent startEvent) throws SAXException {
        Element newElement = null;
        int attsCount = startEvent.atts.getLength();
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
            logger.error("DOMException creating start element: namespaceURI=" + startEvent.uri + ", localName=" + startEvent.localName, e);
            throw e;
        }

        for(int i = 0; i < attsCount; i++) {
            String attNamespace = startEvent.atts.getURI(i);
            String attQName = startEvent.atts.getQName(i);
            String attLocalName = startEvent.atts.getLocalName(i);
            String attValue = startEvent.atts.getValue(i);
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
                logger.error("DOMException setting element attribute " + attLocalName + "=" + attValue + "[namespaceURI=" + startEvent.uri + ", localName=" + startEvent.localName + "].", e);
                throw e;
            }
        }
    }

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
                logger.warn("Ignoring unexpected end [" + endEvent.localName + "] element event. Request: [" + execContext.getDocumentSource() + "] - document location: [" + getCurPath() + "]");
            }
        }
    }

    @Override
    public void cleanup() {        
    }

    private String getCurPath() {
        StringBuffer path = new StringBuffer();
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
            logger.error("DOMException appending character data [" + new String(ch, start, length) + "]", e);
            throw e;
        }
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        characters(ch, start, length);
    }

    public void startCDATA() throws SAXException {
        CDATASection newCDATASection = ownerDocument.createCDATASection("dummy");
        Node currentNode;

        currentNode = (Node)nodeStack.peek();
        currentNode.appendChild(newCDATASection);
        nodeStack.push(newCDATASection);
        cdataNodeBuilder.setLength(0);
    }

    public void endCDATA() throws SAXException {
        CDATASection cdata = (CDATASection) nodeStack.pop();
        cdata.setData(cdataNodeBuilder.toString());
        cdataNodeBuilder.setLength(0);
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        try {
            Node currentNode = (Node)nodeStack.peek();
            Comment newComment;

            newComment = ownerDocument.createComment(new String(ch, start, length));

            currentNode.appendChild(newComment);
        } catch(DOMException e) {
            logger.error("DOMException comment data [" + new String(ch, start, length) + "]", e);
            throw e;
        }
    }

    public void startEntity(String name) throws SAXException {
        inEntity = true;
    }

    public void endEntity(String name) throws SAXException {
        inEntity = false;
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        DocumentType docType = documentBuilder.getDOMImplementation().createDocumentType(name, publicId, systemId);

        ownerDocument.appendChild(docType);

        DocType.setDocType(name, publicId, systemId, null, execContext);
    }
}
