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
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.XSAnnotationImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.XSConstraints;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.XSGroupDecl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.XSModelGroupImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.XSParticleDecl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.traversers.XSAttributeChecker;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.traversers.XSDAbstractParticleTraverser;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.traversers.XSDHandler;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.traversers.XSDocumentInfo;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.util.XInt;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.DOMUtil;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.XMLSymbols;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.QName;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSObjectList;
import org.w3c.dom.Element;

/**
 * The model group schema component traverser.
 *
 * <group
 *   name = NCName>
 *   Content: (annotation?, (all | choice | sequence))
 * </group>
 *
 * @xerces.internal 
 *
 * @author Rahul Srivastava, Sun Microsystems Inc.
 * @author Elena Litani, IBM
 * @author Lisa Martin,  IBM
 * @version $Id$
 */
class  XSDGroupTraverser extends XSDAbstractParticleTraverser {
    
    XSDGroupTraverser (org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.traversers.XSDHandler handler,
                       XSAttributeChecker gAttrCheck) {
        
        super(handler, gAttrCheck);
    }
    
    XSParticleDecl traverseLocal(Element elmNode,
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.traversers.XSDocumentInfo schemaDoc,
            SchemaGrammar grammar) {
        
        // General Attribute Checking for elmNode declared locally
        Object[] attrValues = fAttrChecker.checkAttributes(elmNode, false,
                schemaDoc);
        QName refAttr = (QName) attrValues[XSAttributeChecker.ATTIDX_REF];
        XInt  minAttr = (XInt)  attrValues[XSAttributeChecker.ATTIDX_MINOCCURS];
        XInt  maxAttr = (XInt)  attrValues[XSAttributeChecker.ATTIDX_MAXOCCURS];
        
        XSGroupDecl group = null;
        
        // ref should be here.
        if (refAttr == null) {
            reportSchemaError("s4s-att-must-appear", new Object[]{"group (local)", "ref"}, elmNode);
        } else {
            // get global decl
            // index is a particle index.
            group = (XSGroupDecl)fSchemaHandler.getGlobalDecl(schemaDoc, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.traversers.XSDHandler.GROUP_TYPE, refAttr, elmNode);
        }
        
        XSAnnotationImpl annotation = null;
        // no children other than "annotation?" are allowed
        Element child = DOMUtil.getFirstChildElement(elmNode);
        if (child != null && DOMUtil.getLocalName(child).equals(SchemaSymbols.ELT_ANNOTATION)) {
            annotation = traverseAnnotationDecl(child, attrValues, false, schemaDoc);
            child = DOMUtil.getNextSiblingElement(child);
        } 
        else {
            String text = DOMUtil.getSyntheticAnnotation(elmNode);
            if (text != null) {
                annotation = traverseSyntheticAnnotation(elmNode, text, attrValues, false, schemaDoc);
            }
        }
        
        if (child != null) {
            reportSchemaError("s4s-elt-must-match.1", new Object[]{"group (local)", "(annotation?)", DOMUtil.getLocalName(elmNode)}, elmNode);
        }
        
        int minOccurs = minAttr.intValue();
        int maxOccurs = maxAttr.intValue();
        
        XSParticleDecl particle = null;
        
        // not empty group, not empty particle
        if (group != null && group.fModelGroup != null &&
                !(minOccurs == 0 && maxOccurs == 0)) {
            // create a particle to contain this model group
            if (fSchemaHandler.fDeclPool != null) {
                particle = fSchemaHandler.fDeclPool.getParticleDecl();
            } else {        
                particle = new XSParticleDecl();
            }
            particle.fType = XSParticleDecl.PARTICLE_MODELGROUP;
            particle.fValue = group.fModelGroup;
            particle.fMinOccurs = minOccurs;
            particle.fMaxOccurs = maxOccurs;
            if (group.fModelGroup.fCompositor == XSModelGroupImpl.MODELGROUP_ALL) {
                Long defaultVals = (Long)attrValues[XSAttributeChecker.ATTIDX_FROMDEFAULT];
                particle = checkOccurrences(particle, SchemaSymbols.ELT_GROUP,
                        (Element)elmNode.getParentNode(), GROUP_REF_WITH_ALL,
                        defaultVals.longValue());
            }
            if (refAttr != null) {
                XSObjectList annotations;
                if (annotation != null) {
                    annotations = new XSObjectListImpl();
                    ((XSObjectListImpl) annotations).addXSObject(annotation);
                } else {
                    annotations = XSObjectListImpl.EMPTY_LIST;
                }
                particle.fAnnotations = annotations;
            } else {
                particle.fAnnotations = group.fAnnotations;
            }
        }
        
        fAttrChecker.returnAttrArray(attrValues, schemaDoc);
        
        return particle;
        
    } // traverseLocal
    
    XSGroupDecl traverseGlobal(Element elmNode,
            org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.traversers.XSDocumentInfo schemaDoc,
            SchemaGrammar grammar) {
        
        // General Attribute Checking for elmNode declared globally
        Object[] attrValues = fAttrChecker.checkAttributes(elmNode, true,
                schemaDoc);
        String  strNameAttr = (String)  attrValues[XSAttributeChecker.ATTIDX_NAME];
        
        // must have a name
        if (strNameAttr == null) {
            reportSchemaError("s4s-att-must-appear", new Object[]{"group (global)", "name"}, elmNode);
        }
        
        // Create the group defi up-front, so it can be passed
        // to the traversal methods
        XSGroupDecl group = new XSGroupDecl();
        XSParticleDecl particle = null;
        
        // must have at least one child
        Element l_elmChild = DOMUtil.getFirstChildElement(elmNode);
        XSAnnotationImpl annotation = null;
        if (l_elmChild == null) {
            reportSchemaError("s4s-elt-must-match.2",
                    new Object[]{"group (global)", "(annotation?, (all | choice | sequence))"},
                    elmNode);
        } else {
            String childName = l_elmChild.getLocalName();
            if (childName.equals(SchemaSymbols.ELT_ANNOTATION)) {
                annotation = traverseAnnotationDecl(l_elmChild, attrValues, true, schemaDoc);
                l_elmChild = DOMUtil.getNextSiblingElement(l_elmChild);
                if (l_elmChild != null)
                    childName = l_elmChild.getLocalName();
            }
            else {
                String text = DOMUtil.getSyntheticAnnotation(elmNode);
                if (text != null) {
                    annotation = traverseSyntheticAnnotation(elmNode, text, attrValues, false, schemaDoc);
                }
            }
            
            if (l_elmChild == null) {
                reportSchemaError("s4s-elt-must-match.2",
                        new Object[]{"group (global)", "(annotation?, (all | choice | sequence))"},
                        elmNode);
            } else if (childName.equals(SchemaSymbols.ELT_ALL)) {
                particle = traverseAll(l_elmChild, schemaDoc, grammar, CHILD_OF_GROUP, group);
            } else if (childName.equals(SchemaSymbols.ELT_CHOICE)) {
                particle = traverseChoice(l_elmChild, schemaDoc, grammar, CHILD_OF_GROUP, group);
            } else if (childName.equals(SchemaSymbols.ELT_SEQUENCE)) {
                particle = traverseSequence(l_elmChild, schemaDoc, grammar, CHILD_OF_GROUP, group);
            } else {
                reportSchemaError("s4s-elt-must-match.1",
                        new Object[]{"group (global)", "(annotation?, (all | choice | sequence))", DOMUtil.getLocalName(l_elmChild)},
                        l_elmChild);
            }
            
            if (l_elmChild != null &&
                    DOMUtil.getNextSiblingElement(l_elmChild) != null) {
                reportSchemaError("s4s-elt-must-match.1",
                        new Object[]{"group (global)", "(annotation?, (all | choice | sequence))",
                        DOMUtil.getLocalName(DOMUtil.getNextSiblingElement(l_elmChild))},
                        DOMUtil.getNextSiblingElement(l_elmChild));
            }
        }
        
        // add global group declaration to the grammar
        if (strNameAttr != null) {
            group.fName = strNameAttr;
            group.fTargetNamespace = schemaDoc.fTargetNamespace;
            if (particle == null) {
                particle = XSConstraints.getEmptySequence(); 
            }
            group.fModelGroup = (XSModelGroupImpl)particle.fValue;
            XSObjectList annotations;
            if (annotation != null) {
                annotations = new XSObjectListImpl();
                ((XSObjectListImpl) annotations).addXSObject(annotation);
            } else {
                annotations = XSObjectListImpl.EMPTY_LIST;
            }
            group.fAnnotations = annotations;
            // Add group declaration to grammar
            if (grammar.getGlobalGroupDecl(group.fName) == null) {
                grammar.addGlobalGroupDecl(group);
            }

            // also add it to extended map
            final String loc = fSchemaHandler.schemaDocument2SystemId(schemaDoc);
            final XSGroupDecl group2 = grammar.getGlobalGroupDecl(group.fName, loc);
            if (group2 == null) {
                grammar.addGlobalGroupDecl(group, loc);
            }

            // handle duplicates
            if (fSchemaHandler.fTolerateDuplicates) {
                if (group2 != null) {
                    group = group2;
                }
                fSchemaHandler.addGlobalGroupDecl(group); 
            }
        }
        else {
            // name attribute is not there, don't return this group.
            group = null;
        }

        if (group != null) { 
            // store groups redefined by restriction in the grammar so
            // that we can get at them at full-schema-checking time.
            Object redefinedGrp = fSchemaHandler.getGrpOrAttrGrpRedefinedByRestriction(XSDHandler.GROUP_TYPE,
                    new QName(XMLSymbols.EMPTY_STRING, strNameAttr, strNameAttr, schemaDoc.fTargetNamespace),
                    schemaDoc, elmNode);
            if (redefinedGrp != null) {
                // store in grammar
                grammar.addRedefinedGroupDecl(group, (XSGroupDecl)redefinedGrp,
                        fSchemaHandler.element2Locator(elmNode));
            }
        }
        
        fAttrChecker.returnAttrArray(attrValues, schemaDoc);
        
        return group;
        
    } // traverseGlobal
}
