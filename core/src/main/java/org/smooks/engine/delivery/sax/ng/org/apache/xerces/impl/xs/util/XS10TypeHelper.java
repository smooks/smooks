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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.util;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xs.XSTypeDefinition;

/**
 * Class defining utility/helper methods to support XML Schema 1.0 implementation.
 * 
 * @xerces.internal
 * 
 * @author Mukul Gandhi, IBM
 * @version $Id$
 */
public class XS10TypeHelper {
    
    /*
     * Class constructor.
     */
    private XS10TypeHelper() {
       // a private constructor, to prohibit instantiating this class from an outside class/application.
       // this is a good practice, since all methods of this class are "static".
    }
    
    /*
     * Get name of an XSD type definition as a string value (which will typically be the value of "name" attribute of a
     * type definition, or an internal name determined by the validator for anonymous types).
     */
    public static String getSchemaTypeName(XSTypeDefinition typeDefn) {
        
        String typeNameStr = "";
        if (typeDefn instanceof XSSimpleTypeDefinition) {
            typeNameStr = ((XSSimpleTypeDecl) typeDefn).getTypeName();
        }
        else {
            typeNameStr = ((XSComplexTypeDecl) typeDefn).getTypeName();
        }
        
        return typeNameStr;
        
    } // getSchemaTypeName
    

} // class XS10TypeHelper
