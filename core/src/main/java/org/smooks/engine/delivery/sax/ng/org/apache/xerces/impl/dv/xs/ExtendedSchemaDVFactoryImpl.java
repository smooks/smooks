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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.XSSimpleType;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.BaseSchemaDVFactory;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.SymbolHash;

/**
 * A special factory to create/return built-in schema DVs and create user-defined DVs
 * that includes anyAtomicType, yearMonthDuration and dayTimeDuration
 * 
 * @xerces.internal 
 *
 * @author Khaled Noaman, IBM
 *
 * @version $Id$
 */
public class ExtendedSchemaDVFactoryImpl extends BaseSchemaDVFactory {

    static SymbolHash fBuiltInTypes = new SymbolHash();
    static {
        createBuiltInTypes();
    }
    
    // create all built-in types
    static void createBuiltInTypes() {
        final String ANYATOMICTYPE     = "anyAtomicType";
        final String DURATION          = "duration";
        final String YEARMONTHDURATION = "yearMonthDuration";
        final String DAYTIMEDURATION   = "dayTimeDuration";

    	createBuiltInTypes(fBuiltInTypes, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.fAnyAtomicType);

        // add anyAtomicType
        fBuiltInTypes.put(ANYATOMICTYPE, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.fAnyAtomicType);

        // add 2 duration types
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl durationDV = (org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl)fBuiltInTypes.get(DURATION);
        fBuiltInTypes.put(YEARMONTHDURATION, new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(durationDV, YEARMONTHDURATION, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.DV_YEARMONTHDURATION, XSSimpleType.ORDERED_PARTIAL, false, false, false, true, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.YEARMONTHDURATION_DT));
        fBuiltInTypes.put(DAYTIMEDURATION, new org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl(durationDV, DAYTIMEDURATION, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl.DV_DAYTIMEDURATION, XSSimpleType.ORDERED_PARTIAL, false, false, false, true, XSSimpleTypeDecl.DAYTIMEDURATION_DT));
    } //createBuiltInTypes()

    /**
     * Get a built-in simple type of the given name
     * REVISIT: its still not decided within the Schema WG how to define the
     *          ur-types and if all simple types should be derived from a
     *          complex type, so as of now we ignore the fact that anySimpleType
     *          is derived from anyType, and pass 'null' as the base of
     *          anySimpleType. It needs to be changed as per the decision taken.
     *
     * @param name  the name of the datatype
     * @return      the datatype validator of the given name
     */
    public XSSimpleType getBuiltInType(String name) {
        return (XSSimpleType)fBuiltInTypes.get(name);
    }

    /**
     * get all built-in simple types, which are stored in a hashtable keyed by
     * the name
     *
     * @return      a hashtable which contains all built-in simple types
     */
    public SymbolHash getBuiltInTypes() {
        return (SymbolHash)fBuiltInTypes.makeClone();
    }
}
