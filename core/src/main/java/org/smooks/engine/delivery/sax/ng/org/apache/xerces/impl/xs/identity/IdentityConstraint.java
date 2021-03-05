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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.identity;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.XSAnnotationImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.identity.Field;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.identity.Selector;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.util.StringListImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.StringList;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSConstants;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSIDCDefinition;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSNamespaceItem;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSObjectList;

/**
 * Base class of Schema identity constraint.
 *
 * @xerces.internal 
 *
 * @author Andy Clark, IBM
 * @version $Id$
 */
public abstract class IdentityConstraint implements XSIDCDefinition {

    //
    // Data
    //

    /** type */
    protected short type;

    /** target namespace */
    protected final String fNamespace;
    
    /** Identity constraint name. */
    protected final String fIdentityConstraintName;

    /** name of owning element */
    protected final String fElementName;

    /** Selector. */
    protected org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.identity.Selector fSelector;

    /** Field count. */
    protected int fFieldCount;

    /** Fields. */
    protected org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.identity.Field[] fFields;

    // optional annotations
    protected XSAnnotationImpl [] fAnnotations = null;

    // number of annotations in this identity constraint
    protected int fNumAnnotations;

    //
    // Constructors
    //

    /** Default constructor. */
    protected IdentityConstraint(String namespace, String identityConstraintName, String elemName) {
        fNamespace = namespace;
        fIdentityConstraintName = identityConstraintName;
        fElementName = elemName;
    } // <init>(String,String)

    //
    // Public methods
    //

    /** Returns the identity constraint name. */
    public String getIdentityConstraintName() {
        return fIdentityConstraintName;
    } // getIdentityConstraintName():String

    /** Sets the selector. */
    public void setSelector(org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.identity.Selector selector) {
        fSelector = selector;
    } // setSelector(Selector)

    /** Returns the selector. */
    public Selector getSelector() {
        return fSelector;
    } // getSelector():Selector

    /** Adds a field. */
    public void addField(org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.identity.Field field) {
        if (fFields == null)
            fFields = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.identity.Field[4];
        else if (fFieldCount == fFields.length)
            fFields = resize(fFields, fFieldCount*2);
        fFields[fFieldCount++] = field;
    } // addField(Field)

    /** Returns the field count. */
    public int getFieldCount() {
        return fFieldCount;
    } // getFieldCount():int

    /** Returns the field at the specified index. */
    public org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.identity.Field getFieldAt(int index) {
        return fFields[index];
    } // getFieldAt(int):Field

    // get the name of the owning element
    public String getElementName () {
        return fElementName;
    } // getElementName(): String

    //
    // Object methods
    //

    /** Returns a string representation of this object. */
    public String toString() {
        String s = super.toString();
        int index1 = s.lastIndexOf('$');
        if (index1 != -1) {
            return s.substring(index1 + 1);
        }
        int index2 = s.lastIndexOf('.');
        if (index2 != -1) {
            return s.substring(index2 + 1);
        }
        return s;
    } // toString():String

    // equals:  returns true if and only if the String
    // representations of all members of both objects (except for
    // the elenemtName field) are equal.
    public boolean equals(IdentityConstraint id) {
        boolean areEqual = fIdentityConstraintName.equals(id.fIdentityConstraintName);
        if(!areEqual) return false;
        areEqual = fSelector.toString().equals(id.fSelector.toString());
        if(!areEqual) return false;
        areEqual = (fFieldCount == id.fFieldCount);
        if(!areEqual) return false;
        for(int i=0; i<fFieldCount; i++)
            if(!fFields[i].toString().equals(id.fFields[i].toString())) return false;
        return true;
    } // equals

    static final org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.identity.Field[] resize(org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.identity.Field[] oldArray, int newSize) {
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.identity.Field[] newArray = new Field[newSize];
        System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
        return newArray;
    }

    /**
     * Get the type of the object, i.e ELEMENT_DECLARATION.
     */
    public short getType() {
        return XSConstants.IDENTITY_CONSTRAINT;
    }

    /**
     * The <code>name</code> of this <code>XSObject</code> depending on the
     * <code>XSObject</code> type.
     */
    public String getName() {
        return fIdentityConstraintName;
    }

    /**
     * The namespace URI of this node, or <code>null</code> if it is
     * unspecified.  defines how a namespace URI is attached to schema
     * components.
     */
    public String getNamespace() {
        return fNamespace;
    }

    /**
     * {identity-constraint category} One of key, keyref or unique.
     */
    public short getCategory() {
        return type;
    }

    /**
     * {selector} A restricted XPath ([XPath]) expression
     */
    public String getSelectorStr() {
        return (fSelector != null) ? fSelector.toString() : null;
    }

    /**
     * {fields} A non-empty list of restricted XPath ([XPath]) expressions.
     */
    public StringList getFieldStrs() {
        String[] strs = new String[fFieldCount];
        for (int i = 0; i < fFieldCount; i++)
            strs[i] = fFields[i].toString();
        return new StringListImpl(strs, fFieldCount);
    }

    /**
     * {referenced key} Required if {identity-constraint category} is keyref,
     * forbidden otherwise. An identity-constraint definition with
     * {identity-constraint category} equal to key or unique.
     */
    public XSIDCDefinition getRefKey() {
        return null;
    }

    /**
     * Optional. Annotation.
     */
    public XSObjectList getAnnotations() {
        return new XSObjectListImpl(fAnnotations, fNumAnnotations);
    }
    
	/**
	 * @see org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSObject#getNamespaceItem()
	 */
	public XSNamespaceItem getNamespaceItem() {
        // REVISIT: implement
		return null;
	}

    public void addAnnotation(XSAnnotationImpl annotation) {
        if(annotation == null)
            return;
        if(fAnnotations == null) {
            fAnnotations = new XSAnnotationImpl[2];
        } else if(fNumAnnotations == fAnnotations.length) {
            XSAnnotationImpl[] newArray = new XSAnnotationImpl[fNumAnnotations << 1];
            System.arraycopy(fAnnotations, 0, newArray, 0, fNumAnnotations);
            fAnnotations = newArray;
        }
        fAnnotations[fNumAnnotations++] = annotation;
    }

} // class IdentityConstraint
