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

import java.util.Hashtable;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.DocumentBuilderImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.JAXPConstants;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.parsers.DOMParser;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.SAXMessageFormatter;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * @author Rajiv Mordani
 * @author Edwin Goei
 * @version $Id$
 */
public class DocumentBuilderFactoryImpl extends DocumentBuilderFactory {
    
    /** Feature identifier: namespaces. */
    private static final String NAMESPACES_FEATURE =
        Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACES_FEATURE;
    
    /** Feature identifier: validation */
    private static final String VALIDATION_FEATURE =
        Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE;
    
    /** Feature identifier: XInclude processing */
    private static final String XINCLUDE_FEATURE = 
        Constants.XERCES_FEATURE_PREFIX + Constants.XINCLUDE_FEATURE;
    
    /** Feature identifier: include ignorable white space. */
    private static final String INCLUDE_IGNORABLE_WHITESPACE =
        Constants.XERCES_FEATURE_PREFIX + Constants.INCLUDE_IGNORABLE_WHITESPACE;
    
    /** Feature identifier: create entiry ref nodes feature. */
    private static final String CREATE_ENTITY_REF_NODES_FEATURE =
        Constants.XERCES_FEATURE_PREFIX + Constants.CREATE_ENTITY_REF_NODES_FEATURE;
    
    /** Feature identifier: include comments feature. */
    private static final String INCLUDE_COMMENTS_FEATURE =
        Constants.XERCES_FEATURE_PREFIX + Constants.INCLUDE_COMMENTS_FEATURE;
    
    /** Feature identifier: create cdata nodes feature. */
    private static final String CREATE_CDATA_NODES_FEATURE =
        Constants.XERCES_FEATURE_PREFIX + Constants.CREATE_CDATA_NODES_FEATURE;
    
    /** These are DocumentBuilderFactory attributes not DOM attributes */
    private Hashtable attributes;
    private Hashtable features;
    private Schema grammar;
    private boolean isXIncludeAware;
    
    /**
     * State of the secure processing feature, initially <code>false</code>
     */
    private boolean fSecureProcess = false;

    /**
     * Creates a new instance of a {@link DocumentBuilder}
     * using the currently configured parameters.
     */
    public DocumentBuilder newDocumentBuilder()
        throws ParserConfigurationException 
    {
        /** Check that if a Schema has been specified that neither of the schema properties have been set. */
        if (grammar != null && attributes != null) {
            if (attributes.containsKey(org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.JAXPConstants.JAXP_SCHEMA_LANGUAGE)) {
                throw new ParserConfigurationException(
                        SAXMessageFormatter.formatMessage(null, 
                        "schema-already-specified", new Object[] {org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.JAXPConstants.JAXP_SCHEMA_LANGUAGE}));
            }
            else if (attributes.containsKey(org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.JAXPConstants.JAXP_SCHEMA_SOURCE)) {
                throw new ParserConfigurationException(
                        SAXMessageFormatter.formatMessage(null, 
                        "schema-already-specified", new Object[] {JAXPConstants.JAXP_SCHEMA_SOURCE}));                
            }
        }
        
        try {
            return new org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.DocumentBuilderImpl(this, attributes, features, fSecureProcess);
        } catch (SAXException se) {
            // Handles both SAXNotSupportedException, SAXNotRecognizedException
            throw new ParserConfigurationException(se.getMessage());
        }
    }

    /**
     * Allows the user to set specific attributes on the underlying 
     * implementation.
     * @param name    name of attribute
     * @param value   null means to remove attribute
     */
    public void setAttribute(String name, Object value)
        throws IllegalArgumentException
    {
        // This handles removal of attributes
        if (value == null) {
            if (attributes != null) {
                attributes.remove(name);
            }
            // Unrecognized attributes do not cause an exception
            return;
        }
        
        // This is ugly.  We have to collect the attributes and then
        // later create a DocumentBuilderImpl to verify the attributes.

        // Create Hashtable if none existed before
        if (attributes == null) {
            attributes = new Hashtable();
        }

        attributes.put(name, value);

        // Test the attribute name by possibly throwing an exception
        try {
            new org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.DocumentBuilderImpl(this, attributes, features);
        } catch (Exception e) {
            attributes.remove(name);
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * Allows the user to retrieve specific attributes on the underlying 
     * implementation.
     */
    public Object getAttribute(String name)
        throws IllegalArgumentException
    {
        // See if it's in the attributes Hashtable
        if (attributes != null) {
            Object val = attributes.get(name);
            if (val != null) {
                return val;
            }
        }

        DOMParser domParser = null;
        try {
            // We create a dummy DocumentBuilderImpl in case the attribute
            // name is not one that is in the attributes hashtable.
            domParser =
                new org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.DocumentBuilderImpl(this, attributes, features).getDOMParser();
            return domParser.getProperty(name);
        } catch (SAXException se1) {
            // assert(name is not recognized or not supported), try feature
            try {
                boolean result = domParser.getFeature(name);
                // Must have been a feature
                return result ? Boolean.TRUE : Boolean.FALSE;
            } catch (SAXException se2) {
                // Not a property or a feature
                throw new IllegalArgumentException(se1.getMessage());
            }
        }
    }
    
    public Schema getSchema() {
        return grammar;
    }
    
    public void setSchema(Schema grammar) {
        this.grammar = grammar;
    }
    
    public boolean isXIncludeAware() {
        return this.isXIncludeAware;
    }
    
    public void setXIncludeAware(boolean state) {
        this.isXIncludeAware = state;
    }
    
    public boolean getFeature(String name) 
        throws ParserConfigurationException {
        if (name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)) {
            return fSecureProcess;
        }
        else if (name.equals(NAMESPACES_FEATURE)) {
            return isNamespaceAware();
        }
        else if (name.equals(VALIDATION_FEATURE)) {
            return isValidating();
        }
        else if (name.equals(XINCLUDE_FEATURE)) {
            return isXIncludeAware();
        }
        else if (name.equals(INCLUDE_IGNORABLE_WHITESPACE)) {
            return !isIgnoringElementContentWhitespace();
        }
        else if (name.equals(CREATE_ENTITY_REF_NODES_FEATURE)) {
            return !isExpandEntityReferences();
        }
        else if (name.equals(INCLUDE_COMMENTS_FEATURE)) {
            return !isIgnoringComments();
        }
        else if (name.equals(CREATE_CDATA_NODES_FEATURE)) {
            return !isCoalescing();
        }
        // See if it's in the features Hashtable
        if (features != null) {
            Object val = features.get(name);
            if (val != null) {
                return ((Boolean) val).booleanValue();
            }
        }
        try {
            DOMParser domParser = new org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.DocumentBuilderImpl(this, attributes, features).getDOMParser();
            return domParser.getFeature(name);
        }
        catch (SAXException e) {
            throw new ParserConfigurationException(e.getMessage());
        }
    }
    
    public void setFeature(String name, boolean value) 
        throws ParserConfigurationException {
        // If this is the secure processing feature, save it then return.
        if (name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)) {
            fSecureProcess = value;
            return;
        }
        // Keep built-in settings in synch with the feature values.
        else if (name.equals(NAMESPACES_FEATURE)) {
            setNamespaceAware(value);
            return;
        }
        else if (name.equals(VALIDATION_FEATURE)) {
            setValidating(value);
            return;
        }
        else if (name.equals(XINCLUDE_FEATURE)) {
            setXIncludeAware(value);
            return;
        }
        else if (name.equals(INCLUDE_IGNORABLE_WHITESPACE)) {
            setIgnoringElementContentWhitespace(!value);
            return;
        }
        else if (name.equals(CREATE_ENTITY_REF_NODES_FEATURE)) {
            setExpandEntityReferences(!value);
            return;
        }
        else if (name.equals(INCLUDE_COMMENTS_FEATURE)) {
            setIgnoringComments(!value);
            return;
        }
        else if (name.equals(CREATE_CDATA_NODES_FEATURE)) {
            setCoalescing(!value);
            return;
        }
           
        if (features == null) {
            features = new Hashtable();
        }
        features.put(name, value ? Boolean.TRUE : Boolean.FALSE);
        // Test the feature by possibly throwing SAX exceptions
        try {
            new DocumentBuilderImpl(this, attributes, features);
        } 
        catch (SAXNotSupportedException e) {
            features.remove(name);
            throw new ParserConfigurationException(e.getMessage());
        } 
        catch (SAXNotRecognizedException e) {
            features.remove(name);
            throw new ParserConfigurationException(e.getMessage());
        }
    }
}
