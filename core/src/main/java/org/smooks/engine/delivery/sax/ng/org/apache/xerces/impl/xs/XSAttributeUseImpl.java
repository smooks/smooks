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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.ValidatedInfo;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.SchemaSymbols;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.XSAttributeDecl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.ShortList;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSAttributeDeclaration;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSAttributeUse;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSConstants;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSNamespaceItem;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSObjectList;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSValue;

/**
 * The XML representation for an attribute use
 * schema component is a local &lt;attribute&gt; element information item
 *
 * @xerces.internal 
 *
 * @author Sandy Gao, IBM
 * @version $Id$
 */
public class XSAttributeUseImpl implements XSAttributeUse {

    // the referred attribute decl
    public XSAttributeDecl fAttrDecl = null;
    // use information: SchemaSymbols.USE_OPTIONAL, REQUIRED, PROHIBITED
    public short fUse = org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.SchemaSymbols.USE_OPTIONAL;
    // value constraint type: default, fixed or !specified
    public short fConstraintType = XSConstants.VC_NONE;
    // value constraint value
    public ValidatedInfo fDefault = null;
    // optional annotation
    public XSObjectList fAnnotations = null;
    
    public void reset(){
        fDefault = null;
        fAttrDecl = null;
        fUse = org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.SchemaSymbols.USE_OPTIONAL;
        fConstraintType = XSConstants.VC_NONE;
        fAnnotations = null;
    }

    /**
     * Get the type of the object, i.e ELEMENT_DECLARATION.
     */
    public short getType() {
        return XSConstants.ATTRIBUTE_USE;
    }

    /**
     * The <code>name</code> of this <code>XSObject</code> depending on the
     * <code>XSObject</code> type.
     */
    public String getName() {
        return null;
    }

    /**
     * The namespace URI of this node, or <code>null</code> if it is
     * unspecified.  defines how a namespace URI is attached to schema
     * components.
     */
    public String getNamespace() {
        return null;
    }

    /**
     * {required} determines whether this use of an attribute declaration
     * requires an appropriate attribute information item to be present, or
     * merely allows it.
     */
    public boolean getRequired() {
        return fUse == SchemaSymbols.USE_REQUIRED;
    }

    /**
     * {attribute declaration} provides the attribute declaration itself,
     * which will in turn determine the simple type definition used.
     */
    public XSAttributeDeclaration getAttrDeclaration() {
        return fAttrDecl;
    }

    /**
     * Value Constraint: one of default, fixed.
     */
    public short getConstraintType() {
        return fConstraintType;
    }

    /**
     * Value Constraint: The actual value (with respect to the {type
     * definition}).
     */
    public String getConstraintValue() {
        // REVISIT: SCAPI: what's the proper representation
        return getConstraintType() == XSConstants.VC_NONE ?
               null :
               fDefault.stringValue();
    }

    /**
     * @see org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSObject#getNamespaceItem()
     */
    public XSNamespaceItem getNamespaceItem() {
        return null;
    }

    public Object getActualVC() {
        return getConstraintType() == XSConstants.VC_NONE ?
               null :
               fDefault.actualValue;
    }

    public short getActualVCType() {
        return getConstraintType() == XSConstants.VC_NONE ?
               XSConstants.UNAVAILABLE_DT :
               fDefault.actualValueType;
    }

    public ShortList getItemValueTypes() {
        return getConstraintType() == XSConstants.VC_NONE ?
               null :
               fDefault.itemValueTypes;
    }

    public XSValue getValueConstraintValue() {
        return fDefault;
    }

    /**
     * Optional. Annotations.
     */
    public XSObjectList getAnnotations() {
        return (fAnnotations != null) ? fAnnotations : XSObjectListImpl.EMPTY_LIST;
    }
    
} // class XSAttributeUseImpl
