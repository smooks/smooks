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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.xpointer;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.XSSimpleType;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.SymbolTable;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.QName;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLAttributes;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xpointer.XPointerPart;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.AttributePSVI;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSTypeDefinition;

/**
 * <p>
 * Implements the XPointerPart interface and handles processing of
 * ShortHand Pointers.  It identifies at most one element in the 
 * resource's information set; specifically, the first one (if any) 
 * in document order that has a matching NCName as an identifier.
 * </p>
 *
 * @version $Id$
 */
final class ShortHandPointer implements org.smooks.engine.delivery.sax.ng.org.apache.xerces.xpointer.XPointerPart {
    
    // The name of the ShortHand pointer
    private String fShortHandPointer;
    
    // The name of the ShortHand pointer
    private boolean fIsFragmentResolved = false;
    
    // SymbolTable
    private SymbolTable fSymbolTable;
    
    //
    // Constructors
    //
    public ShortHandPointer() {
    }
    
    public ShortHandPointer(SymbolTable symbolTable) {
        fSymbolTable = symbolTable;
    }
    
    /**
     * The XPointerProcessor takes care of this.  Simply set the ShortHand Pointer here.
     * 
     * @see org.smooks.engine.delivery.sax.ng.org.apache.xerces.xpointer.XPointerPart#parseXPointer(String)
     */
    public void parseXPointer(String part) throws XNIException {
        fShortHandPointer = part;
        // reset fIsFragmentResolved
        fIsFragmentResolved = false;
    }
    
    /**
     * Resolves the XPointer ShortHand pointer based on the rules defined in 
     * Section 3.2 of the XPointer Framework Recommendation.
     * Note that in the current implementation only supports DTD determined ID's. 
     *
     * @see org.smooks.engine.delivery.sax.ng.org.apache.xerces.xpointer.XPointerPart#resolveXPointer(QName, XMLAttributes, Augmentations, int event)
     */
    int fMatchingChildCount = 0;
    public boolean resolveXPointer(QName element, XMLAttributes attributes,
            Augmentations augs, int event) throws XNIException {

        // reset fIsFragmentResolved
        if (fMatchingChildCount == 0) {
            fIsFragmentResolved = false;
        }

        // On startElement or emptyElement, if no matching elements or parent 
        // elements were found, check for a matching idenfitier.
        if (event == org.smooks.engine.delivery.sax.ng.org.apache.xerces.xpointer.XPointerPart.EVENT_ELEMENT_START) {
            if (fMatchingChildCount == 0) {
                fIsFragmentResolved = hasMatchingIdentifier(element, attributes, augs,
                    event);
            }
            if (fIsFragmentResolved) {
               fMatchingChildCount++;
            }
        } else if (event == XPointerPart.EVENT_ELEMENT_EMPTY) {
            if (fMatchingChildCount == 0) {
                fIsFragmentResolved = hasMatchingIdentifier(element, attributes, augs,
                    event);
            }
        }
        else {
            // On endElement, decrease the matching child count if the child or
            // its parent was resolved.
            if (fIsFragmentResolved) {
                fMatchingChildCount--;
            }
        }
        
        return fIsFragmentResolved ;
    }
    
    /**
     * 
     * @param element
     * @param attributes
     * @param augs
     * @param event
     * @return
     * @throws XNIException
     */
    private boolean hasMatchingIdentifier(QName element,
            XMLAttributes attributes, Augmentations augs, int event)
    throws XNIException {
        String normalizedValue = null;
        
        // The identifiers of an element are determined by the 
        // ShortHand Pointer as follows:
        
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                
                // 1. If an element information item has an attribute information item 
                // among its [attributes] that is a schema-determined ID, then it is 
                // identified by the value of that attribute information item's 
                // [schema normalized value] property;
                normalizedValue = getSchemaDeterminedID(attributes, i);
                if (normalizedValue != null) {
                    break;
                }
                
                // 2. If an element information item has an element information item among 
                // its [children] that is a schema-determined ID, then it is identified by 
                // the value of that element information item's [schema normalized value] property;
                // ???
                normalizedValue = getChildrenSchemaDeterminedID(attributes, i);
                if (normalizedValue != null) {
                    break;
                }
                
                // 3. If an element information item has an attribute information item among 
                // its [attributes] that is a DTD-determined ID, then it is identified by the 
                // value of that attribute information item's [normalized value] property.
                // An attribute information item is a DTD-determined ID if and only if it has 
                // a [type definition] property whose value is equal to ID.
                normalizedValue = getDTDDeterminedID(attributes, i);
                if (normalizedValue != null) {
                    break;
                }
                // 4. No externally determined ID's
            }
        }
        
        if (normalizedValue != null
                && normalizedValue.equals(fShortHandPointer)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Rerturns the DTD determine-ID
     * 
     * @param attributes
     * @param index
     * @return String 
     * @throws XNIException
     */
    public String getDTDDeterminedID(XMLAttributes attributes, int index)
    throws XNIException {
        
        if (attributes.getType(index).equals("ID")) {
            return attributes.getValue(index);
        }
        return null;
    }
    
    /**
     * Returns the schema-determined-ID.
     * 
     * 
     * @param attributes
     * @param index
     * @return A String containing the schema-determined ID. 
     * @throws XNIException
     */
    public String getSchemaDeterminedID(XMLAttributes attributes, int index)
    throws XNIException {
        Augmentations augs = attributes.getAugmentations(index);
        AttributePSVI attrPSVI = (AttributePSVI) augs
        .getItem(Constants.ATTRIBUTE_PSVI);
        
        if (attrPSVI != null) {
            // An element or attribute information item is a schema-determined 
            // ID if and only if one of the following is true:]
            
            // 1. It has a [member type definition] or [type definition] property 
            // whose value in turn has [name] equal to ID and [target namespace] 
            // equal to http://www.w3.org/2001/XMLSchema;
            
            // 2. It has a [base type definition] whose value has that [name] and [target namespace];
            
            // 3. It has a [base type definition] whose value has a [base type definition] 
            // whose value has that [name] and [target namespace], and so on following 
            // the [base type definition] property recursively;
            
            XSTypeDefinition typeDef = attrPSVI.getMemberTypeDefinition();
            if (typeDef != null) {
                typeDef = attrPSVI.getTypeDefinition();
            }
            
            // 
            if (typeDef != null && ((XSSimpleType) typeDef).isIDType()) {
                return attrPSVI.getSchemaNormalizedValue();
            }
            
            // 4 & 5 NA
        }
        
        return null;
    }
    
    /**
     * Not quite sure how this can be correctly implemented.
     * 
     * @param attributes
     * @param index
     * @return String - We return null since we currenly do not supprt this. 
     * @throws XNIException
     */
    public String getChildrenSchemaDeterminedID(XMLAttributes attributes,
            int index) throws XNIException {
        return null;
    }
    
    /**
     * 
     * @see XPointerPart#isFragmentResolved()
     */
    public boolean isFragmentResolved() {
        return fIsFragmentResolved;
    }
    
    /**
     * 
     * @see XPointerPart#isChildFragmentResolved()
     */
    public boolean isChildFragmentResolved() {
        return fIsFragmentResolved && (fMatchingChildCount > 0);
    }
    
    /**
     * Returns the name of the ShortHand pointer
     * 
     * @see XPointerPart#getSchemeName()
     */
    public String getSchemeName() {
        return fShortHandPointer;
    }
    
    /**
     * @see XPointerPart#getSchemeData()
     */
    public String getSchemeData() {
        return null;
    }
    
    /**
     * @see XPointerPart#setSchemeName(String)
     */
    public void setSchemeName(String schemeName) {
        fShortHandPointer = schemeName;
    }
    
    /**
     * @see XPointerPart#setSchemeData(String)
     */
    public void setSchemeData(String schemeData) {
        // NA
    }
}