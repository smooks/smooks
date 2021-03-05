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

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.XSModelGroupImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSAnnotation;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSConstants;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSModelGroup;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSModelGroupDefinition;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSNamespaceItem;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSObjectList;

/**
 * The XML representation for a group declaration
 * schema component is a global &lt;group&gt; element information item
 *
 * @xerces.internal 
 *
 * @author Sandy Gao, IBM
 * @version $Id$
 */
public class XSGroupDecl implements XSModelGroupDefinition {

    // name of the group
    public String fName = null;
    // target namespace of the group
    public String fTargetNamespace = null;
    // model group of the group
    public XSModelGroupImpl fModelGroup = null;
    // optional annotations
    public XSObjectList fAnnotations = null;
    // The namespace schema information item corresponding to the target namespace 
    // of the model group definition, if it is globally declared; or null otherwise.
    private XSNamespaceItem fNamespaceItem = null;

    /**
     * Get the type of the object, i.e ELEMENT_DECLARATION.
     */
    public short getType() {
        return XSConstants.MODEL_GROUP_DEFINITION;
    }

    /**
     * The <code>name</code> of this <code>XSObject</code> depending on the
     * <code>XSObject</code> type.
     */
    public String getName() {
        return fName;
    }

    /**
     * The namespace URI of this node, or <code>null</code> if it is
     * unspecified.  defines how a namespace URI is attached to schema
     * components.
     */
    public String getNamespace() {
        return fTargetNamespace;
    }

    /**
     * {model group} A model group.
     */
    public XSModelGroup getModelGroup() {
        return fModelGroup;
    }

    /**
     * Optional. Annotation.
     */
    public XSAnnotation getAnnotation() {
        return (fAnnotations != null) ? (XSAnnotation) fAnnotations.item(0) : null;
    }

    /**
     * Optional. Annotations.
     */
    public XSObjectList getAnnotations() {
        return (fAnnotations != null) ? fAnnotations : XSObjectListImpl.EMPTY_LIST;
    }
    
    /**
     * @see org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSObject#getNamespaceItem()
     */
    public XSNamespaceItem getNamespaceItem() {
        return fNamespaceItem;
    }

    void setNamespaceItem(XSNamespaceItem namespaceItem) {
        fNamespaceItem = namespaceItem;
    }

} // class XSGroupDecl
