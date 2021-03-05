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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.traversers;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.SchemaGrammar;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.SchemaSymbols;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.XSElementDecl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.identity.IdentityConstraint;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.identity.KeyRef;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.identity.UniqueOrKey;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.traversers.XSAttributeChecker;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.traversers.XSDAbstractIDConstraintTraverser;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.traversers.XSDHandler;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.traversers.XSDocumentInfo;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.QName;
import org.w3c.dom.Element;

/**
 * This class contains code that is used to traverse <keyref>s.
 *
 * @xerces.internal 
 *
 * @author Neil Graham, IBM
 * @version $Id$
 */
class XSDKeyrefTraverser extends XSDAbstractIDConstraintTraverser {

    public XSDKeyrefTraverser (org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.traversers.XSDHandler handler,
                               org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.traversers.XSAttributeChecker gAttrCheck) {
        super(handler, gAttrCheck);
    }

    void traverse(Element krElem, XSElementDecl element,
                  org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.traversers.XSDocumentInfo schemaDoc, SchemaGrammar grammar) {

        // General Attribute Checking
        Object[] attrValues = fAttrChecker.checkAttributes(krElem, false, schemaDoc);

        // create identity constraint
        String krName = (String)attrValues[org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.traversers.XSAttributeChecker.ATTIDX_NAME];
        if(krName == null){
            reportSchemaError("s4s-att-must-appear", new Object [] {SchemaSymbols.ELT_KEYREF , SchemaSymbols.ATT_NAME }, krElem);
            //return this array back to pool
            fAttrChecker.returnAttrArray(attrValues, schemaDoc);
            return;
        }
        QName kName = (QName)attrValues[XSAttributeChecker.ATTIDX_REFER];
        if(kName == null){
            reportSchemaError("s4s-att-must-appear", new Object [] {SchemaSymbols.ELT_KEYREF , SchemaSymbols.ATT_REFER }, krElem);
            //return this array back to pool
            fAttrChecker.returnAttrArray(attrValues, schemaDoc);
            return;
        }

        UniqueOrKey key = null;
        IdentityConstraint ret = (IdentityConstraint)fSchemaHandler.getGlobalDecl(schemaDoc, XSDHandler.IDENTITYCONSTRAINT_TYPE, kName, krElem);
        // if ret == null, we've already reported an error in getGlobalDecl
        // we report an error only when ret != null, and the return type keyref
        if (ret != null) {
            if (ret.getCategory() == IdentityConstraint.IC_KEY ||
                ret.getCategory() == IdentityConstraint.IC_UNIQUE) {
                key = (UniqueOrKey)ret;
            } else {
                reportSchemaError("src-resolve", new Object[]{kName.rawname, "identity constraint key/unique"}, krElem);
            }
        }

        if(key == null) {
            fAttrChecker.returnAttrArray(attrValues, schemaDoc);
            return;
        }

        KeyRef keyRef = new KeyRef(schemaDoc.fTargetNamespace, krName, element.fName, key);

        // If errors occurred in traversing the identity constraint, then don't
        // add it to the schema, to avoid errors when processing the instance.
        if (traverseIdentityConstraint(keyRef, krElem, schemaDoc, attrValues)) {
            //Schema Component Constraint: Identity-constraint Definition Properties Correct
            //2 If the {identity-constraint category} is keyref, the cardinality of the {fields} must equal that of the {fields} of the {referenced key}.
            if(key.getFieldCount() != keyRef.getFieldCount()) {
                reportSchemaError("c-props-correct.2" , new Object [] {krName,key.getIdentityConstraintName()}, krElem);
            } else {
                // add key reference to element decl
                // and stuff this in the grammar
                if (grammar.getIDConstraintDecl(keyRef.getIdentityConstraintName()) == null) {
                    grammar.addIDConstraintDecl(element, keyRef);
                }

                // also add it to extended map
                final String loc = fSchemaHandler.schemaDocument2SystemId(schemaDoc);
                final IdentityConstraint idc = grammar.getIDConstraintDecl(keyRef.getIdentityConstraintName(), loc); 
                if (idc  == null) {
                    grammar.addIDConstraintDecl(element, keyRef, loc);
                }

                // handle duplicates
                if (fSchemaHandler.fTolerateDuplicates) {
                    if (idc  != null) {
                        if (idc instanceof KeyRef) {
                            keyRef = (KeyRef) idc;
                        }
                    }
                    fSchemaHandler.addIDConstraintDecl(keyRef);
                }
            }
        }

        // and put back attributes
        fAttrChecker.returnAttrArray(attrValues, schemaDoc);
    } // traverse(Element,int,XSDocumentInfo, SchemaGrammar)
} // XSDKeyrefTraverser

