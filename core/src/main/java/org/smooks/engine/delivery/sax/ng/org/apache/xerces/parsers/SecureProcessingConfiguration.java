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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityDescription;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dtd.XMLDTDProcessor;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.parsers.SecuritySupport;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.parsers.XIncludeAwareParserConfiguration;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.SecurityManager;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.SymbolTable;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLDTDHandler;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLLocator;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLResourceIdentifier;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLString;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLComponentManager;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLConfigurationException;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLDTDFilter;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLDTDScanner;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLDTDSource;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLEntityResolver;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLInputSource;

/**
 * This configuration enhances Xerces support for the JAXP secure processing feature.
 * 
 * @author Michael Glavassevich, IBM
 * 
 * @version $Id$
 */
public final class SecureProcessingConfiguration extends
        XIncludeAwareParserConfiguration {
    
    //
    // Constants
    //
    
    /** Property identifier: security manager. */
    private static final String SECURITY_MANAGER_PROPERTY =
            Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY;
    
    /** Property identifier: entity resolver. */
    private static final String ENTITY_RESOLVER_PROPERTY = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_RESOLVER_PROPERTY;
    
    /** Feature identifier: external general entities. */
    private static final String EXTERNAL_GENERAL_ENTITIES =
        Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE;

    /** Feature identifier: external parameter entities. */
    private static final String EXTERNAL_PARAMETER_ENTITIES =
        Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE;
    
    /** Feature identifier: load external DTD. */
    private static final String LOAD_EXTERNAL_DTD =
        Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE;
    
    /** Set to true for debugging */
    private static final boolean DEBUG = isDebugEnabled();
    
    /** Cache the contents of the jaxp.properties file, if used. */
    private static Properties jaxpProperties = null;

    /** Cache the timestamp of the jaxp.properties file, if used. */
    private static long lastModified = -1;
    
    /** Xerces SecurityManager default value for entity expansion limit. **/
    private static final int SECURITY_MANAGER_DEFAULT_ENTITY_EXPANSION_LIMIT = 100000;
    
    /** Xerces SecurityManager default value of number of nodes created. **/
    private static final int SECURITY_MANAGER_DEFAULT_MAX_OCCUR_NODE_LIMIT = 3000;
    
    private static final String ENTITY_EXPANSION_LIMIT_PROPERTY_NAME = "jdk.xml.entityExpansionLimit";
    private static final String MAX_OCCUR_LIMIT_PROPERTY_NAME = "jdk.xml.maxOccur";
    private static final String TOTAL_ENTITY_SIZE_LIMIT_PROPERTY_NAME = "jdk.xml.totalEntitySizeLimit";
    private static final String MAX_GENERAL_ENTITY_SIZE_LIMIT_PROPERTY_NAME = "jdk.xml.maxGeneralEntitySizeLimit";
    private static final String MAX_PARAMETER_ENTITY_SIZE_LIMIT_PROPERTY_NAME = "jdk.xml.maxParameterEntitySizeLimit";
    private static final String RESOLVE_EXTERNAL_ENTITIES_PROPERTY_NAME = "jdk.xml.resolveExternalEntities";
    
    private static final int ENTITY_EXPANSION_LIMIT_DEFAULT_VALUE = 64000;
    private static final int MAX_OCCUR_LIMIT_DEFAULT_VALUE = 5000;
    private static final int TOTAL_ENTITY_SIZE_LIMIT_DEFAULT_VALUE = 50000000;
    private static final int MAX_GENERAL_ENTITY_SIZE_LIMIT_DEFAULT_VALUE = Integer.MAX_VALUE;
    private static final int MAX_PARAMETER_ENTITY_SIZE_LIMIT_DEFAULT_VALUE = Integer.MAX_VALUE;
    private static final boolean RESOLVE_EXTERNAL_ENTITIES_DEFAULT_VALUE = true;
    
    protected final int ENTITY_EXPANSION_LIMIT_SYSTEM_VALUE;
    protected final int MAX_OCCUR_LIMIT_SYSTEM_VALUE;
    protected final int TOTAL_ENTITY_SIZE_LIMIT_SYSTEM_VALUE;
    protected final int MAX_GENERAL_ENTITY_SIZE_LIMIT_SYSTEM_VALUE;
    protected final int MAX_PARAMETER_ENTITY_SIZE_LIMIT_SYSTEM_VALUE;
    protected final boolean RESOLVE_EXTERNAL_ENTITIES_SYSTEM_VALUE;
    
    //
    // Fields
    //
    
    private final boolean fJavaSecurityManagerEnabled;
    private boolean fLimitSpecified;
    private SecurityManager fSecurityManager;
    private InternalEntityMonitor fInternalEntityMonitor;
    private final ExternalEntityMonitor fExternalEntityMonitor;
    private int fTotalEntitySize = 0;
    
    /** Default constructor. */
    public SecureProcessingConfiguration() {
        this(null, null, null);
    } // <init>()
    
    /** 
     * Constructs a parser configuration using the specified symbol table. 
     *
     * @param symbolTable The symbol table to use.
     */
    public SecureProcessingConfiguration(SymbolTable symbolTable) {
        this(symbolTable, null, null);
    } // <init>(SymbolTable)
    
    /**
     * Constructs a parser configuration using the specified symbol table and
     * grammar pool.
     * <p>
     *
     * @param symbolTable The symbol table to use.
     * @param grammarPool The grammar pool to use.
     */
    public SecureProcessingConfiguration(
            SymbolTable symbolTable,
            XMLGrammarPool grammarPool) {
        this(symbolTable, grammarPool, null); 
    } // <init>(SymbolTable,XMLGrammarPool)
    
    /**
     * Constructs a parser configuration using the specified symbol table,
     * grammar pool, and parent settings.
     * <p>
     *
     * @param symbolTable    The symbol table to use.
     * @param grammarPool    The grammar pool to use.
     * @param parentSettings The parent settings.
     */
    public SecureProcessingConfiguration(
            SymbolTable symbolTable,
            XMLGrammarPool grammarPool,
            XMLComponentManager parentSettings) {
        super(symbolTable, grammarPool, parentSettings);
        fJavaSecurityManagerEnabled = (System.getSecurityManager() != null);
        ENTITY_EXPANSION_LIMIT_SYSTEM_VALUE = getPropertyValue(ENTITY_EXPANSION_LIMIT_PROPERTY_NAME, ENTITY_EXPANSION_LIMIT_DEFAULT_VALUE);
        MAX_OCCUR_LIMIT_SYSTEM_VALUE = getPropertyValue(MAX_OCCUR_LIMIT_PROPERTY_NAME, MAX_OCCUR_LIMIT_DEFAULT_VALUE);
        TOTAL_ENTITY_SIZE_LIMIT_SYSTEM_VALUE = getPropertyValue(TOTAL_ENTITY_SIZE_LIMIT_PROPERTY_NAME, TOTAL_ENTITY_SIZE_LIMIT_DEFAULT_VALUE);
        MAX_GENERAL_ENTITY_SIZE_LIMIT_SYSTEM_VALUE = getPropertyValue(MAX_GENERAL_ENTITY_SIZE_LIMIT_PROPERTY_NAME, MAX_GENERAL_ENTITY_SIZE_LIMIT_DEFAULT_VALUE);
        MAX_PARAMETER_ENTITY_SIZE_LIMIT_SYSTEM_VALUE = getPropertyValue(MAX_PARAMETER_ENTITY_SIZE_LIMIT_PROPERTY_NAME, MAX_PARAMETER_ENTITY_SIZE_LIMIT_DEFAULT_VALUE);
        RESOLVE_EXTERNAL_ENTITIES_SYSTEM_VALUE = getPropertyValue(RESOLVE_EXTERNAL_ENTITIES_PROPERTY_NAME, RESOLVE_EXTERNAL_ENTITIES_DEFAULT_VALUE);
        if (fJavaSecurityManagerEnabled || fLimitSpecified) {
            if (!RESOLVE_EXTERNAL_ENTITIES_SYSTEM_VALUE) {
                super.setFeature(EXTERNAL_GENERAL_ENTITIES, false);
                super.setFeature(EXTERNAL_PARAMETER_ENTITIES, false);
                super.setFeature(LOAD_EXTERNAL_DTD, false);
            }
            fSecurityManager = new SecurityManager();
            fSecurityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT_SYSTEM_VALUE);
            fSecurityManager.setMaxOccurNodeLimit(MAX_OCCUR_LIMIT_SYSTEM_VALUE);
            super.setProperty(SECURITY_MANAGER_PROPERTY, fSecurityManager);   
        }
        fExternalEntityMonitor = new ExternalEntityMonitor();
        super.setProperty(ENTITY_RESOLVER_PROPERTY, fExternalEntityMonitor);
    }
    
    protected void checkEntitySizeLimits(int sizeOfEntity, int delta, boolean isPE) {
        fTotalEntitySize += delta;
        if (fTotalEntitySize > TOTAL_ENTITY_SIZE_LIMIT_SYSTEM_VALUE) {
            fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                    "TotalEntitySizeLimitExceeded",
                    new Object[] {new Integer(TOTAL_ENTITY_SIZE_LIMIT_SYSTEM_VALUE)},
                    XMLErrorReporter.SEVERITY_FATAL_ERROR);
        }
        if (isPE) {
            if (sizeOfEntity > MAX_PARAMETER_ENTITY_SIZE_LIMIT_SYSTEM_VALUE) {
                fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                        "MaxParameterEntitySizeLimitExceeded",
                        new Object[] {new Integer(MAX_PARAMETER_ENTITY_SIZE_LIMIT_SYSTEM_VALUE)},
                        XMLErrorReporter.SEVERITY_FATAL_ERROR);
            }
        }
        else if (sizeOfEntity > MAX_GENERAL_ENTITY_SIZE_LIMIT_SYSTEM_VALUE) {
            fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                    "MaxGeneralEntitySizeLimitExceeded",
                    new Object[] {new Integer(MAX_GENERAL_ENTITY_SIZE_LIMIT_SYSTEM_VALUE)},
                    XMLErrorReporter.SEVERITY_FATAL_ERROR);
        }
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
        if (SECURITY_MANAGER_PROPERTY.equals(propertyId)) {
            return fSecurityManager;
        }
        else if (ENTITY_RESOLVER_PROPERTY.equals(propertyId)) {
            return fExternalEntityMonitor;
        }
        return super.getProperty(propertyId);
    }
    
    /**
     * setProperty
     * 
     * @param propertyId 
     * @param value 
     */
    public void setProperty(String propertyId, Object value)
        throws XMLConfigurationException {
        if (SECURITY_MANAGER_PROPERTY.equals(propertyId)) {
            // Do not allow the Xerces SecurityManager to be 
            // removed if the Java SecurityManager has been installed.
            if (value == null && fJavaSecurityManagerEnabled) {
                return;
            }
            fSecurityManager = (SecurityManager) value;
            if (fSecurityManager != null) {
                // Override SecurityManager default values with the system property / jaxp.properties / config default determined values.
                if (fSecurityManager.getEntityExpansionLimit() == SECURITY_MANAGER_DEFAULT_ENTITY_EXPANSION_LIMIT) {
                    fSecurityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT_SYSTEM_VALUE);
                }
                if (fSecurityManager.getMaxOccurNodeLimit() == SECURITY_MANAGER_DEFAULT_MAX_OCCUR_NODE_LIMIT) {
                    fSecurityManager.setMaxOccurNodeLimit(MAX_OCCUR_LIMIT_SYSTEM_VALUE);
                }
            }  
        }
        else if (ENTITY_RESOLVER_PROPERTY.equals(propertyId)) {
            fExternalEntityMonitor.setEntityResolver((XMLEntityResolver) value);
            return;
        }
        super.setProperty(propertyId, value);
    }
    
    /** Configures the XML 1.0 pipeline. */
    protected void configurePipeline() {
        super.configurePipeline();
        configurePipelineCommon(true);
    }
    
    /** Configures the XML 1.1 pipeline. */
    protected void configureXML11Pipeline() {
        super.configureXML11Pipeline();
        configurePipelineCommon(false);
    }
    
    private void configurePipelineCommon(boolean isXML10) {
        if (fSecurityManager != null) {
            fTotalEntitySize = 0;
            if (fInternalEntityMonitor == null) {
                fInternalEntityMonitor = new InternalEntityMonitor();
            }
            // Reconfigure DTD pipeline. Insert internal entity decl monitor.
            final XMLDTDScanner dtdScanner;
            final XMLDTDProcessor dtdProcessor;
            if (isXML10) {
                dtdScanner = fDTDScanner;
                dtdProcessor = fDTDProcessor;
            }
            else {
                dtdScanner = fXML11DTDScanner;
                dtdProcessor = fXML11DTDProcessor;
            }
            dtdScanner.setDTDHandler(fInternalEntityMonitor);
            fInternalEntityMonitor.setDTDSource(dtdScanner);
            fInternalEntityMonitor.setDTDHandler(dtdProcessor);
            dtdProcessor.setDTDSource(fInternalEntityMonitor);
        }
    }
    
    private int getPropertyValue(String propertyName, int defaultValue) {
        
        // Step #1: Use the system property first
        try {
            String propertyValue = org.smooks.engine.delivery.sax.ng.org.apache.xerces.parsers.SecuritySupport.getSystemProperty(propertyName);
            if (propertyValue != null && propertyValue.length() > 0) {
                if (DEBUG) {
                    debugPrintln("found system property \"" + propertyName + "\", value=" + propertyValue);
                }
                final int intValue = Integer.parseInt(propertyValue);
                fLimitSpecified = true;
                if (intValue > 0) {
                    return intValue;
                }
                // Treat 0 and negative numbers as no limit (i.e. max integer).
                return Integer.MAX_VALUE;
            }
        }
        // The VM ran out of memory or there was some other serious problem. Re-throw.
        catch (VirtualMachineError vme) {
            throw vme;
        }
        // ThreadDeath should always be re-thrown
        catch (ThreadDeath td) {
            throw td;
        }
        catch (Throwable e) {
            // Ignore all other exceptions/errors and continue w/ next location
            if (DEBUG) {
                debugPrintln(e.getClass().getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Step #2: Use $java.home/lib/jaxp.properties
        try {
            boolean fExists = false;
            File f = null;
            try {               
                String javah = org.smooks.engine.delivery.sax.ng.org.apache.xerces.parsers.SecuritySupport.getSystemProperty("java.home");
                String configFile = javah + File.separator +
                        "lib" + File.separator + "jaxp.properties";

                f = new File(configFile);
                fExists = org.smooks.engine.delivery.sax.ng.org.apache.xerces.parsers.SecuritySupport.getFileExists(f);

            }
            catch (SecurityException se) {
                // If there is a security exception, move on to next location.
                lastModified = -1;
                jaxpProperties = null;            
            }

            synchronized (SecureProcessingConfiguration.class) {    

                boolean runBlock = false;
                FileInputStream fis = null;

                try {
                    if (lastModified >= 0) {
                        // File has been modified, or didn't previously exist. 
                        // Need to reload properties    
                        if ((fExists) &&
                            (lastModified < (lastModified = org.smooks.engine.delivery.sax.ng.org.apache.xerces.parsers.SecuritySupport.getLastModified(f)))) {  
                            runBlock = true;
                        } 
                        else {
                            if (!fExists) {
                                // file existed, but it's been deleted.
                                lastModified = -1;
                                jaxpProperties = null;
                            }
                        }        
                    } 
                    else {
                        if (fExists) { 
                            // File didn't exist, but it does now.
                            runBlock = true;
                            lastModified = org.smooks.engine.delivery.sax.ng.org.apache.xerces.parsers.SecuritySupport.getLastModified(f);
                        }    
                    }

                    if (runBlock == true) {
                        // Try to read from $java.home/lib/jaxp.properties
                        jaxpProperties = new Properties();

                        fis = org.smooks.engine.delivery.sax.ng.org.apache.xerces.parsers.SecuritySupport.getFileInputStream(f);
                        jaxpProperties.load(fis);
                    }       

                }
                catch (Exception x) {
                    lastModified = -1;
                    jaxpProperties = null;
                    // assert(x instanceof FileNotFoundException
                    //        || x instanceof SecurityException)
                    // In both cases, ignore and return the default value
                }
                finally {
                    // try to close the input stream if one was opened.
                    if (fis != null) {
                        try {
                            fis.close();
                        }
                        // Ignore the exception.
                        catch (IOException exc) {}
                    }
                }
            }

            if (jaxpProperties != null) {            
                String propertyValue = jaxpProperties.getProperty(propertyName);
                if (propertyValue != null && propertyValue.length() > 0) {
                    if (DEBUG) {
                        debugPrintln("found \"" + propertyName + "\" in jaxp.properties, value=" + propertyValue);
                    }
                    final int intValue = Integer.parseInt(propertyValue);
                    fLimitSpecified = true;
                    if (intValue > 0) {
                        return intValue;
                    }
                    // Treat 0 and negative numbers as no limit (i.e. max integer).
                    return Integer.MAX_VALUE;
                }
            }
        }
        // The VM ran out of memory or there was some other serious problem. Re-throw.
        catch (VirtualMachineError vme) {
            throw vme;
        }
        // ThreadDeath should always be re-thrown
        catch (ThreadDeath td) {
            throw td;
        }
        catch (Throwable e) {
            // Ignore all other exceptions/errors and return the default value.
            if (DEBUG) {
                debugPrintln(e.getClass().getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Step #3: Return the default value.
        return defaultValue;
    }
    
    private boolean getPropertyValue(String propertyName, boolean defaultValue) {
        
        // Step #1: Use the system property first
        try {
            String propertyValue = org.smooks.engine.delivery.sax.ng.org.apache.xerces.parsers.SecuritySupport.getSystemProperty(propertyName);
            if (propertyValue != null && propertyValue.length() > 0) {
                if (DEBUG) {
                    debugPrintln("found system property \"" + propertyName + "\", value=" + propertyValue);
                }
                final boolean booleanValue = Boolean.valueOf(propertyValue).booleanValue();
                fLimitSpecified = true;
                return booleanValue;
            }
        }
        // The VM ran out of memory or there was some other serious problem. Re-throw.
        catch (VirtualMachineError vme) {
            throw vme;
        }
        // ThreadDeath should always be re-thrown
        catch (ThreadDeath td) {
            throw td;
        }
        catch (Throwable e) {
            // Ignore all other exceptions/errors and continue w/ next location
            if (DEBUG) {
                debugPrintln(e.getClass().getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Step #2: Use $java.home/lib/jaxp.properties
        try {
            boolean fExists = false;
            File f = null;
            try {               
                String javah = org.smooks.engine.delivery.sax.ng.org.apache.xerces.parsers.SecuritySupport.getSystemProperty("java.home");
                String configFile = javah + File.separator +
                        "lib" + File.separator + "jaxp.properties";

                f = new File(configFile);
                fExists = org.smooks.engine.delivery.sax.ng.org.apache.xerces.parsers.SecuritySupport.getFileExists(f);

            }
            catch (SecurityException se) {
                // If there is a security exception, move on to next location.
                lastModified = -1;
                jaxpProperties = null;            
            }

            synchronized (SecureProcessingConfiguration.class) {    

                boolean runBlock = false;
                FileInputStream fis = null;

                try {
                    if (lastModified >= 0) {
                        // File has been modified, or didn't previously exist. 
                        // Need to reload properties    
                        if ((fExists) &&
                            (lastModified < (lastModified = org.smooks.engine.delivery.sax.ng.org.apache.xerces.parsers.SecuritySupport.getLastModified(f)))) {  
                            runBlock = true;
                        } 
                        else {
                            if (!fExists) {
                                // file existed, but it's been deleted.
                                lastModified = -1;
                                jaxpProperties = null;
                            }
                        }        
                    } 
                    else {
                        if (fExists) { 
                            // File didn't exist, but it does now.
                            runBlock = true;
                            lastModified = org.smooks.engine.delivery.sax.ng.org.apache.xerces.parsers.SecuritySupport.getLastModified(f);
                        }    
                    }

                    if (runBlock == true) {
                        // Try to read from $java.home/lib/jaxp.properties
                        jaxpProperties = new Properties();

                        fis = org.smooks.engine.delivery.sax.ng.org.apache.xerces.parsers.SecuritySupport.getFileInputStream(f);
                        jaxpProperties.load(fis);
                    }       

                }
                catch (Exception x) {
                    lastModified = -1;
                    jaxpProperties = null;
                    // assert(x instanceof FileNotFoundException
                    //        || x instanceof SecurityException)
                    // In both cases, ignore and return the default value
                }
                finally {
                    // try to close the input stream if one was opened.
                    if (fis != null) {
                        try {
                            fis.close();
                        }
                        // Ignore the exception.
                        catch (IOException exc) {}
                    }
                }
            }

            if (jaxpProperties != null) {            
                String propertyValue = jaxpProperties.getProperty(propertyName);
                if (propertyValue != null && propertyValue.length() > 0) {
                    if (DEBUG) {
                        debugPrintln("found \"" + propertyName + "\" in jaxp.properties, value=" + propertyValue);
                    }
                    final boolean booleanValue = Boolean.valueOf(propertyValue).booleanValue();
                    fLimitSpecified = true;
                    return booleanValue;
                }
            }
        }
        // The VM ran out of memory or there was some other serious problem. Re-throw.
        catch (VirtualMachineError vme) {
            throw vme;
        }
        // ThreadDeath should always be re-thrown
        catch (ThreadDeath td) {
            throw td;
        }
        catch (Throwable e) {
            // Ignore all other exceptions/errors and return the default value.
            if (DEBUG) {
                debugPrintln(e.getClass().getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Step #3: Return the default value.
        return defaultValue;
    }
    
    //
    // Private static methods
    //
    
    /** Returns true if debug has been enabled. */
    private static boolean isDebugEnabled() {
        try {
            String val = org.smooks.engine.delivery.sax.ng.org.apache.xerces.parsers.SecuritySupport.getSystemProperty("xerces.debug");
            // Allow simply setting the prop to turn on debug
            return (val != null && (!"false".equals(val)));
        } 
        catch (SecurityException se) {}
        return false;
    } // isDebugEnabled()

    /** Prints a message to standard error if debugging is enabled. */
    private static void debugPrintln(String msg) {
        if (DEBUG) {
            System.err.println("XERCES: " + msg);
        }
    } // debugPrintln(String)
    
    /**
     * XMLDTDFilter which checks limits imposed by the application 
     * on the sizes of general and parameter entities.
     */
    final class InternalEntityMonitor implements XMLDTDFilter {
        
        /** DTD source and handler. **/
        private XMLDTDSource fDTDSource;
        private XMLDTDHandler fDTDHandler;
        
        public InternalEntityMonitor() {}

        /*
         * XMLDTDHandler methods
         */

        public void startDTD(XMLLocator locator, Augmentations augmentations)
                throws XNIException {
            if (fDTDHandler != null) {
                fDTDHandler.startDTD(locator, augmentations);
            }
        }

        public void startParameterEntity(String name,
                XMLResourceIdentifier identifier, String encoding,
                Augmentations augmentations) throws XNIException {
            if (fDTDHandler != null) {
                fDTDHandler.startParameterEntity(name, identifier, encoding, augmentations);
            }
        }

        public void textDecl(String version, String encoding,
                Augmentations augmentations) throws XNIException {
            if (fDTDHandler != null) {
                fDTDHandler.textDecl(version, encoding, augmentations);
            }
        }

        public void endParameterEntity(String name, Augmentations augmentations)
                throws XNIException {
            if (fDTDHandler != null) {
                fDTDHandler.endParameterEntity(name, augmentations);
            }
        }

        public void startExternalSubset(XMLResourceIdentifier identifier,
                Augmentations augmentations) throws XNIException {
            if (fDTDHandler != null) {
                fDTDHandler.startExternalSubset(identifier, augmentations);
            }
        }

        public void endExternalSubset(Augmentations augmentations)
                throws XNIException {
            if (fDTDHandler != null) {
                fDTDHandler.endExternalSubset(augmentations);
            }
        }

        public void comment(XMLString text, Augmentations augmentations)
                throws XNIException {
            if (fDTDHandler != null) {
                fDTDHandler.comment(text, augmentations);
            }
        }

        public void processingInstruction(String target, XMLString data,
                Augmentations augmentations) throws XNIException {
            if (fDTDHandler != null) {
                fDTDHandler.processingInstruction(target, data, augmentations);
            }
        }

        public void elementDecl(String name, String contentModel,
                Augmentations augmentations) throws XNIException {
            if (fDTDHandler != null) {
                fDTDHandler.elementDecl(name, contentModel, augmentations);
            }
        }

        public void startAttlist(String elementName, Augmentations augmentations)
                throws XNIException {
            if (fDTDHandler != null) {
                fDTDHandler.startAttlist(elementName, augmentations);
            }
        }

        public void attributeDecl(String elementName, String attributeName,
                String type, String[] enumeration, String defaultType,
                XMLString defaultValue, XMLString nonNormalizedDefaultValue,
                Augmentations augmentations) throws XNIException {
            if (fDTDHandler != null) {
                fDTDHandler.attributeDecl(elementName, attributeName,
                        type, enumeration, defaultType,
                        defaultValue, nonNormalizedDefaultValue,
                        augmentations);
            }
        }

        public void endAttlist(Augmentations augmentations) throws XNIException {
            if (fDTDHandler != null) {
                fDTDHandler.endAttlist(augmentations);
            }
        }

        public void internalEntityDecl(String name, XMLString text,
                XMLString nonNormalizedText, Augmentations augmentations)
                throws XNIException {
            checkEntitySizeLimits(text.length, text.length, name != null && name.startsWith("%"));
            if (fDTDHandler != null) {
                fDTDHandler.internalEntityDecl(name, text,
                        nonNormalizedText, augmentations);
            }
        }

        public void externalEntityDecl(String name,
                XMLResourceIdentifier identifier, Augmentations augmentations)
                throws XNIException {
            if (fDTDHandler != null) {
                fDTDHandler.externalEntityDecl(name, identifier, augmentations);
            }
        }

        public void unparsedEntityDecl(String name,
                XMLResourceIdentifier identifier, String notation,
                Augmentations augmentations) throws XNIException {
            if (fDTDHandler != null) {
                fDTDHandler.unparsedEntityDecl(name, identifier, notation, augmentations);
            }
        }

        public void notationDecl(String name, XMLResourceIdentifier identifier,
                Augmentations augmentations) throws XNIException {
            if (fDTDHandler != null) {
                fDTDHandler.notationDecl(name, identifier, augmentations);
            }
        }

        public void startConditional(short type, Augmentations augmentations)
                throws XNIException {
            if (fDTDHandler != null) {
                fDTDHandler.startConditional(type, augmentations);
            }
        }

        public void ignoredCharacters(XMLString text, Augmentations augmentations)
                throws XNIException {
            if (fDTDHandler != null) {
                fDTDHandler.ignoredCharacters(text, augmentations);
            }

        }

        public void endConditional(Augmentations augmentations) throws XNIException {
            if (fDTDHandler != null) {
                fDTDHandler.endConditional(augmentations);
            }
        }

        public void endDTD(Augmentations augmentations) throws XNIException {
            if (fDTDHandler != null) {
                fDTDHandler.endDTD(augmentations);
            }
        }

        public void setDTDSource(XMLDTDSource source) {
            fDTDSource = source;
        }

        public XMLDTDSource getDTDSource() {
            return fDTDSource;
        }
        
        /*
         * XMLDTDSource methods
         */

        public void setDTDHandler(XMLDTDHandler handler) {
            fDTDHandler = handler;
        }

        public XMLDTDHandler getDTDHandler() {
            return fDTDHandler;
        }
    }
    
    /**
     * XMLEntityResolver which checks limits imposed by the application 
     * on the sizes of general and parameter entities.
     */
    final class ExternalEntityMonitor implements XMLEntityResolver {
        
        /**
         * java.io.InputStream wrapper which check entity size limits.
         */
        final class InputStreamMonitor extends FilterInputStream {
            
            private final boolean isPE;
            private int size = 0;

            protected InputStreamMonitor(InputStream in, boolean isPE) {
                super(in);
                this.isPE = isPE;
            }
            
            public int read() throws IOException {
                int i = super.read();
                if (i != -1) {
                    ++size;
                    checkEntitySizeLimits(size, 1, isPE);
                }
                return i;
            }
            
            public int read(byte[] b, int off, int len) throws IOException {
                int i = super.read(b, off, len);
                if (i > 0) {
                    size += i;
                    checkEntitySizeLimits(size, i, isPE);
                }
                return i;
            }
        }
        
        /**
         * java.io.Reader wrapper which check entity size limits.
         */
        final class ReaderMonitor extends FilterReader {
            
            private final boolean isPE;
            private int size = 0;

            protected ReaderMonitor(Reader in, boolean isPE) {
                super(in);
                this.isPE = isPE;
            }
            
            public int read() throws IOException {
                int i = super.read();
                if (i != -1) {
                    ++size;
                    checkEntitySizeLimits(size, 1, isPE);
                }
                return i;
            }
            
            public int read(char[] cbuf, int off, int len) throws IOException {
                int i = super.read(cbuf, off, len);
                if (i > 0) {
                    size += i;
                    checkEntitySizeLimits(size, i, isPE);
                }
                return i;
            }
        }
        
        private XMLEntityResolver fEntityResolver;

        public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException,
                IOException {
            XMLInputSource source = null;
            if (fEntityResolver != null) {
                source = fEntityResolver.resolveEntity(resourceIdentifier);
            }
            if (fSecurityManager != null && resourceIdentifier instanceof XMLEntityDescription) {
                String name = ((XMLEntityDescription) resourceIdentifier).getEntityName();
                boolean isPE = name != null && name.startsWith("%");
                if (source == null) {
                    String publicId = resourceIdentifier.getPublicId();
                    String systemId = resourceIdentifier.getExpandedSystemId();
                    String baseSystemId = resourceIdentifier.getBaseSystemId();
                    source = new XMLInputSource(publicId, systemId, baseSystemId);
                }
                Reader reader = source.getCharacterStream();
                if (reader != null) {
                    source.setCharacterStream(new ReaderMonitor(reader, isPE));
                }
                else {
                    InputStream stream = source.getByteStream();
                    if (stream != null) {
                        source.setByteStream(new InputStreamMonitor(stream, isPE));
                    }
                    else {
                        String systemId = resourceIdentifier.getExpandedSystemId();
                        URL url = new URL(systemId);
                        stream = url.openStream();
                        source.setByteStream(new InputStreamMonitor(stream, isPE));
                    }
                }
            }
            return source;
        }
        
        /** Sets the XNI entity resolver. */
        public void setEntityResolver(XMLEntityResolver entityResolver) {
            fEntityResolver = entityResolver;
        } // setEntityResolver(XMLEntityResolver)

        /** Returns the XNI entity resolver. */
        public XMLEntityResolver getEntityResolver() {
            return fEntityResolver;
        } // getEntityResolver():XMLEntityResolver
    }  
}
