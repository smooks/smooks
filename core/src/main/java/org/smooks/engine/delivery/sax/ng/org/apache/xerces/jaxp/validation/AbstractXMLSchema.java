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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.validation;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.validation.ValidatorHandlerImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.validation.ValidatorImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.validation.XSGrammarPoolContainer;

import java.util.HashMap;

import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import javax.xml.validation.ValidatorHandler;

/**
 * <p>Abstract implementation of Schema for W3C XML Schemas.</p>
 * 
 * @author Michael Glavassevich, IBM
 * @version $Id$
 */
abstract class AbstractXMLSchema extends Schema implements
        XSGrammarPoolContainer {

    /** 
     * Map containing the initial values of features for 
     * validators created using this grammar pool container.
     */
    private final HashMap fFeatures;
    
    public AbstractXMLSchema() {
        fFeatures = new HashMap();
    }
    
    /*
     * Schema methods
     */

    /* 
     * @see javax.xml.validation.Schema#newValidator()
     */
    public final Validator newValidator() {
        return new org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.validation.ValidatorImpl(this);
    }

    /* 
     * @see javax.xml.validation.Schema#newValidatorHandler()
     */
    public final ValidatorHandler newValidatorHandler() {
        return new ValidatorHandlerImpl(this);
    }
    
    /*
     * XSGrammarPoolContainer methods
     */
    
    /**
     * Returns the initial value of a feature for validators created
     * using this grammar pool container or null if the validators
     * should use the default value.
     */
    public final Boolean getFeature(String featureId) {
        return (Boolean) fFeatures.get(featureId);
    }
    
    /*
     * Other methods
     */
    
    final void setFeature(String featureId, boolean state) {
        fFeatures.put(featureId, state ? Boolean.TRUE : Boolean.FALSE);
    }
    
} // AbstractXMLSchema
