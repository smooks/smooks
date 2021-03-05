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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.validation.ValidationManager;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.XSMessageFormatter;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.validation.XSGrammarPoolContainer;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLComponentManager;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLConfigurationException;

/**
 * <p>Parser configuration for Xerces' XMLSchemaValidator.</p>
 * 
 * @version $Id$
 */
final class SchemaValidatorConfiguration implements XMLComponentManager {
    
    // feature identifiers
    
    /** Feature identifier: schema validation. */
    private static final String SCHEMA_VALIDATION =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_VALIDATION_FEATURE;
    
    /** Feature identifier: validation. */
    private static final String VALIDATION =
        Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE;
    
    /** Feature identifier: use grammar pool only. */
    private static final String USE_GRAMMAR_POOL_ONLY =
        Constants.XERCES_FEATURE_PREFIX + Constants.USE_GRAMMAR_POOL_ONLY_FEATURE;
    
    /** Feature identifier: parser settings. */
    private static final String PARSER_SETTINGS = 
        Constants.XERCES_FEATURE_PREFIX + Constants.PARSER_SETTINGS;
    
    // property identifiers
    
    /** Property identifier: error reporter. */
    private static final String ERROR_REPORTER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;
    
    /** Property identifier: validation manager. */
    private static final String VALIDATION_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.VALIDATION_MANAGER_PROPERTY;
    
    /** Property identifier: grammar pool. */
    private static final String XMLGRAMMAR_POOL =
        Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY;
    
    //
    // Data
    //
    
    /** Parent component manager. **/
    private final XMLComponentManager fParentComponentManager;
    
    /** The Schema's grammar pool. **/
    private final XMLGrammarPool fGrammarPool;

    /** 
     * Tracks whether the validator should use components from 
     * the grammar pool to the exclusion of all others.
     */
    private final boolean fUseGrammarPoolOnly;
    
    /** Validation manager. */
    private final ValidationManager fValidationManager;
    
    public SchemaValidatorConfiguration(XMLComponentManager parentManager, 
            XSGrammarPoolContainer grammarContainer, ValidationManager validationManager) {
        fParentComponentManager = parentManager;
        fGrammarPool = grammarContainer.getGrammarPool();
        fUseGrammarPoolOnly = grammarContainer.isFullyComposed();
        fValidationManager = validationManager;
        // add schema message formatter to error reporter
        try {
            XMLErrorReporter errorReporter = (XMLErrorReporter) fParentComponentManager.getProperty(ERROR_REPORTER);
            if (errorReporter != null) {
                errorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN, new XSMessageFormatter());
            }
        }
        // Ignore exception.
        catch (XMLConfigurationException exc) {}
    }
    
    /**
     * Returns the state of a feature.
     * 
     * @param featureId The feature identifier.
     * @return true if the feature is supported
     * 
     * @throws XMLConfigurationException Thrown for configuration error.
     *                                   In general, components should
     *                                   only throw this exception if
     *                                   it is <strong>really</strong>
     *                                   a critical error.
     */
    public boolean getFeature(String featureId)
            throws XMLConfigurationException {
        if (PARSER_SETTINGS.equals(featureId)) {
            return fParentComponentManager.getFeature(featureId);
        }
        else if (VALIDATION.equals(featureId) || SCHEMA_VALIDATION.equals(featureId)) {
            return true;
        }
        else if (USE_GRAMMAR_POOL_ONLY.equals(featureId)) {
            return fUseGrammarPoolOnly;
        }
        return fParentComponentManager.getFeature(featureId);
    }

    /**
     * Returns the value of a property.
     * 
     * @param propertyId The property identifier.
     * @return the value of the property
     * 
     * @throws XMLConfigurationException Thrown for configuration error.
     *                                   In general, components should
     *                                   only throw this exception if
     *                                   it is <strong>really</strong>
     *                                   a critical error.
     */
    public Object getProperty(String propertyId)
            throws XMLConfigurationException {
        if (XMLGRAMMAR_POOL.equals(propertyId)) {
            return fGrammarPool;
        }
        else if (VALIDATION_MANAGER.equals(propertyId)) {
            return fValidationManager;
        }
        return fParentComponentManager.getProperty(propertyId);
    }
}
