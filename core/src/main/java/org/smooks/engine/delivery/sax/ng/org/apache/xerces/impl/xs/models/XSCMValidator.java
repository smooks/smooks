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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.models;

import java.util.Vector;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.SubstitutionGroupHandler;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.XMLSchemaException;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.QName;

/**
 * Note: State of the content model is stored in the validator
 *
 * @xerces.internal 
 *
 * @author Sandy Gao, IBM
 * @author Elena Litani, IBM
 * @version $Id$
 */
public interface XSCMValidator {


    public static final short FIRST_ERROR = -1;

    // on subsequent errors the validator should not report
    // an error
    //
    public static final short SUBSEQUENT_ERROR = -2;

    /**
     * This methods to be called on entering a first element whose type
     * has this content model. It will return the initial state of the content model
     *
     * @return Start state of the content model
     */
    public int[] startContentModel();


    /**
     * The method corresponds to one transaction in the content model.
     *
     * @param elementName
     * @param state  Current state
     * @return element decl or wildcard decl that
     *         corresponds to the element from the Schema grammar
     */
    public Object oneTransition (QName elementName, int[] state, SubstitutionGroupHandler subGroupHandler);


    /**
     * The method indicates the end of list of children
     *
     * @param state  Current state of the content model
     * @return true if the last state was a valid final state
     */
    public boolean endContentModel (int[] state);

    /**
     * check whether this content violates UPA constraint.
     *
     * @param subGroupHandler the substitution group handler
     * @return true if this content model contains other or list wildcard
     */
    public boolean checkUniqueParticleAttribution(SubstitutionGroupHandler subGroupHandler) throws XMLSchemaException;

    /**
     * Check which elements are valid to appear at this point. This method also
     * works if the state is in error, in which case it returns what should
     * have been seen.
     * 
     * @param state  the current state
     * @return       a Vector whose entries are instances of
     *               either XSWildcardDecl or XSElementDecl.
     */
    public Vector whatCanGoHere(int[] state);
    
    /**
     * <p>Returns an array containing information about the current repeating term
     * or <code>null</code> if no occurrence counting was being performed at the
     * current state.</p>
     * 
     * <p>If an array is returned it will have a length == 4 and will contain:
     *  <ul>
     *   <li>a[0] :: min occurs</li>
     *   <li>a[1] :: max occurs</li>
     *   <li>a[2] :: current value of the counter</li>
     *   <li>a[3] :: identifier for the repeating term</li>
     *  </ul>
     * </p>
     * 
     * @param state the current state
     * @return an array containing information about the current repeating term
     */
    public int [] occurenceInfo(int[] state);
    
    /**
     * Returns the name of the term (element or wildcard) for the given identifier.
     * 
     * @param termId identifier for the element declaration or wildcard
     * @return the name of the element declaration or wildcard
     */
    public String getTermName(int termId);
    
    /**
     * Checks if this content model has had its min/maxOccurs values reduced for
     * purposes of speeding up UPA.  If so, this content model should not be used
     * for any purpose other than checking unique particle attribution
     * 
     * @return a boolean that says whether this content has been compacted for UPA
     */
    public boolean isCompactedForUPA();
} // XSCMValidator
