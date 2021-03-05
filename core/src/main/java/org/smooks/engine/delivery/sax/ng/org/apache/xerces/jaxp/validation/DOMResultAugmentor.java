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

import javax.xml.transform.dom.DOMResult;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.dom.AttrImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.dom.CoreDocumentImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.dom.ElementImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.dom.ElementNSImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.dom.PSVIAttrNSImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.dom.PSVIDocumentImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.dom.PSVIElementNSImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.XSSimpleType;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.validation.DOMDocumentHandler;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.validation.DOMValidatorHelper;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.NamespaceContext;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.QName;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLAttributes;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLLocator;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLResourceIdentifier;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLString;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLDocumentSource;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.AttributePSVI;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.ElementPSVI;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSTypeDefinition;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

/**
 * <p>DOM result augmentor.</p>
 * 
 * @author Michael Glavassevich, IBM
 * @version $Id$
 */
final class DOMResultAugmentor implements org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.validation.DOMDocumentHandler {

    //
    // Data
    //
    
    private final DOMValidatorHelper fDOMValidatorHelper;
    
    private Document fDocument;
    private CoreDocumentImpl fDocumentImpl;
    private boolean fStorePSVI;
    
    private boolean fIgnoreChars;
    
    private final QName fAttributeQName = new QName();
    
    public DOMResultAugmentor(DOMValidatorHelper helper) {
        fDOMValidatorHelper = helper;
    }

    public void setDOMResult(DOMResult result) {
        fIgnoreChars = false;
        if (result != null) {
            final Node target = result.getNode();
            fDocument = (target.getNodeType() == Node.DOCUMENT_NODE) ? (Document) target : target.getOwnerDocument();
            fDocumentImpl = (fDocument instanceof CoreDocumentImpl) ? (CoreDocumentImpl) fDocument : null;
            fStorePSVI = (fDocument instanceof PSVIDocumentImpl);
            return;
        }
        fDocument = null;
        fDocumentImpl = null;
        fStorePSVI = false;
    }

    public void doctypeDecl(DocumentType node) throws XNIException {}

    public void characters(Text node) throws XNIException {}

    public void cdata(CDATASection node) throws XNIException {}

    public void comment(Comment node) throws XNIException {}

    public void processingInstruction(ProcessingInstruction node)
            throws XNIException {}

    public void setIgnoringCharacters(boolean ignore) {
        fIgnoreChars = ignore;
    }

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
        final Element currentElement = (Element) fDOMValidatorHelper.getCurrentElement();
        final NamedNodeMap attrMap = currentElement.getAttributes();
        
        final int oldLength = attrMap.getLength();
        // If it's a Xerces DOM store type information for attributes, set idness, etc..
        if (fDocumentImpl != null) {
            AttrImpl attr;
            for (int i = 0; i < oldLength; ++i) {
                attr = (AttrImpl) attrMap.item(i);
                
                // write type information to this attribute
                AttributePSVI attrPSVI = (AttributePSVI) attributes.getAugmentations(i).getItem (Constants.ATTRIBUTE_PSVI);
                if (attrPSVI != null) {
                    if (processAttributePSVI(attr, attrPSVI)) {
                        ((ElementImpl) currentElement).setIdAttributeNode (attr, true);
                    }
                }
            }
        }
        
        final int newLength = attributes.getLength();
        // Add default/fixed attributes
        if (newLength > oldLength) {
            if (fDocumentImpl == null) {
                for (int i = oldLength; i < newLength; ++i) {
                    attributes.getName(i, fAttributeQName);
                    currentElement.setAttributeNS(fAttributeQName.uri, fAttributeQName.rawname, attributes.getValue(i));
                }
            }
            // If it's a Xerces DOM store type information for attributes, set idness, etc..
            else {
                for (int i = oldLength; i < newLength; ++i) {
                    attributes.getName(i, fAttributeQName);
                    AttrImpl attr = (AttrImpl) fDocumentImpl.createAttributeNS(fAttributeQName.uri, 
                            fAttributeQName.rawname, fAttributeQName.localpart);
                    attr.setValue(attributes.getValue(i));
                    currentElement.setAttributeNodeNS(attr);
                    
                    // write type information to this attribute
                    AttributePSVI attrPSVI = (AttributePSVI) attributes.getAugmentations(i).getItem (Constants.ATTRIBUTE_PSVI);
                    if (attrPSVI != null) {
                        if (processAttributePSVI(attr, attrPSVI)) {
                            ((ElementImpl) currentElement).setIdAttributeNode (attr, true);
                        }
                    }
                    attr.setSpecified(false);
                }
            }
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
            final Element currentElement = (Element) fDOMValidatorHelper.getCurrentElement();
            currentElement.appendChild(fDocument.createTextNode(text.toString()));
        }
    }

    public void ignorableWhitespace(XMLString text, Augmentations augs)
            throws XNIException {
        characters(text, augs);
    }

    public void endElement(QName element, Augmentations augs)
            throws XNIException {
        final Node currentElement = fDOMValidatorHelper.getCurrentElement();
        // Write type information to this element
        if (augs != null && fDocumentImpl != null) {
            ElementPSVI elementPSVI = (ElementPSVI)augs.getItem(Constants.ELEMENT_PSVI);
            if (elementPSVI != null) {
                if (fStorePSVI) {
                    ((PSVIElementNSImpl) currentElement).setPSVI(elementPSVI);
                }
                XSTypeDefinition type = elementPSVI.getMemberTypeDefinition();
                if (type == null) {
                    type = elementPSVI.getTypeDefinition();
                }
                ((ElementNSImpl) currentElement).setType(type);
            }
        }
    }

    public void startCDATA(Augmentations augs) throws XNIException {}

    public void endCDATA(Augmentations augs) throws XNIException {}

    public void endDocument(Augmentations augs) throws XNIException {}

    public void setDocumentSource(XMLDocumentSource source) {}

    public XMLDocumentSource getDocumentSource() {
        return null;
    }
    
    /** Returns whether the given attribute is an ID type. **/
    private boolean processAttributePSVI(AttrImpl attr, AttributePSVI attrPSVI) {
        if (fStorePSVI) {
            ((PSVIAttrNSImpl) attr).setPSVI (attrPSVI);
        }
        Object type = attrPSVI.getMemberTypeDefinition ();
        if (type == null) {
            type = attrPSVI.getTypeDefinition ();
            if (type != null) {
                attr.setType(type);
                return ((XSSimpleType) type).isIDType();
            }
        }
        else {
            attr.setType(type);
            return ((XSSimpleType) type).isIDType();
        }
        return false;
    }

} // DOMResultAugmentor
