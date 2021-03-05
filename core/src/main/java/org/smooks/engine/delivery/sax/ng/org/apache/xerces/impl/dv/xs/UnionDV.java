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

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.ValidationContext;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.TypeValidator;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;

/**
 * Represent the schema union types
 * 
 * @xerces.internal 
 *
 * @author Neeraj Bajaj, Sun Microsystems, inc.
 * @author Sandy Gao, IBM
 *
 * @version $Id$
 */
public class UnionDV extends TypeValidator {

    public short getAllowedFacets(){
          return (XSSimpleTypeDecl.FACET_PATTERN | XSSimpleTypeDecl.FACET_ENUMERATION );
    }

    // this method should never be called: XSSimpleTypeDecl is responsible for
    // calling the member types for the convertion
    public Object getActualValue(String content, ValidationContext context) throws InvalidDatatypeValueException{
        return content;
    }

} // class UnionDV
