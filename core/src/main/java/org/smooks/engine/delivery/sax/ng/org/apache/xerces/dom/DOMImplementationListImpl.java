/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.dom;

import java.util.ArrayList;
import java.util.Vector;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DOMImplementationList;

/**
 * <p>This class implements the DOM Level 3 Core interface DOMImplementationList.</p>
 * 
 * @xerces.internal
 * 
 * @author Neil Delima, IBM
 * @since DOM Level 3 Core
 */
public class DOMImplementationListImpl implements DOMImplementationList {

    // A collection of DOMImplementations
    private final ArrayList fImplementations;

    /**
     * Construct an empty list of DOMImplementations
     */
    public DOMImplementationListImpl() {
        fImplementations = new ArrayList();
    }
    
    /** 
     * Construct a list of DOMImplementations from an ArrayList
     */ 
    public DOMImplementationListImpl(ArrayList params) {
        fImplementations = params;    
    }

    /** 
     * Construct a list of DOMImplementations from a Vector
     */ 
    public DOMImplementationListImpl(Vector params) {
        fImplementations = new ArrayList(params);
    }

    /**
     * Returns the indexth item in the collection.
     * 
     * @param index The index of the DOMImplemetation from the list to return.
     */
    public DOMImplementation item(int index) {
        final int length = getLength();
        if (index >= 0 && index < length) {
            return (DOMImplementation) fImplementations.get(index);
        }
        return null;
    }
    
    /**
     * Returns the number of DOMImplementations in the list.
     * 
     * @return An integer indicating the number of DOMImplementations.
     */
    public int getLength() {
        return fImplementations.size();
    }
}
