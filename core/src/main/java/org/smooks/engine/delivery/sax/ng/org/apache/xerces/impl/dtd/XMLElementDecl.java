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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dtd;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dtd.XMLSimpleType;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dtd.models.ContentModelValidator;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.QName;

/**
 * @xerces.internal
 * 
 * @version $Id$
 */
public class XMLElementDecl {

    //
    // Constants  
    //

    /** TYPE_ANY */
    public static final short TYPE_ANY = 0;

    /** TYPE_EMPTY */
    public static final short TYPE_EMPTY = 1;

    /** TYPE_MIXED */
    public static final short TYPE_MIXED = 2;

    /** TYPE_CHILDREN */
    public static final short TYPE_CHILDREN = 3;

    /** TYPE_SIMPLE */
    public static final short TYPE_SIMPLE = 4;

    //
    // Data
    //

    /** name */
    public final QName name = new QName();

    /** scope */
    public int scope = -1;

    /** type */
    public short type = -1;

    /** contentModelValidator */
    public ContentModelValidator contentModelValidator;

    /** simpleType */
    public final org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dtd.XMLSimpleType simpleType = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dtd.XMLSimpleType();

    //
    // Methods
    //

    /**
     * setValues
     * 
     * @param name 
     * @param scope 
     * @param type 
     * @param contentModelValidator 
     * @param simpleType 
     */
    public void setValues(QName name, int scope, short type, ContentModelValidator contentModelValidator, XMLSimpleType simpleType) {
        this.name.setValues(name);
        this.scope                 = scope;
        this.type                  = type;
        this.contentModelValidator = contentModelValidator;
        this.simpleType.setValues(simpleType);
    } // setValues

    /**
     * clear
     */
    public void clear() {
        this.name.clear();
        this.type          = -1;
        this.scope         = -1;
        this.contentModelValidator = null;
        this.simpleType.clear();
    } // clear

} // class XMLElementDecl
