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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl;

import java.io.CharConversionException;
import java.io.EOFException;
import java.io.IOException;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLDocumentFragmentScannerImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dtd.XMLDTDDescription;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.io.MalformedByteSequenceException;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.validation.ValidationManager;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.NamespaceSupport;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.XMLChar;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.XMLStringBuffer;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.NamespaceContext;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLResourceIdentifier;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLString;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLComponentManager;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLConfigurationException;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLDTDScanner;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLInputSource;

/**
 * This class is responsible for scanning XML document structure
 * and content. The scanner acts as the source for the document
 * information which is communicated to the document handler.
 * <p>
 * This component requires the following features and properties from the
 * component manager that uses it:
 * <ul>
 *  <li>http://xml.org/sax/features/namespaces</li>
 *  <li>http://xml.org/sax/features/validation</li>
 *  <li>http://apache.org/xml/features/nonvalidating/load-external-dtd</li>
 *  <li>http://apache.org/xml/features/scanner/notify-char-refs</li>
 *  <li>http://apache.org/xml/features/scanner/notify-builtin-refs</li>
 *  <li>http://apache.org/xml/properties/internal/symbol-table</li>
 *  <li>http://apache.org/xml/properties/internal/error-reporter</li>
 *  <li>http://apache.org/xml/properties/internal/entity-manager</li>
 *  <li>http://apache.org/xml/properties/internal/dtd-scanner</li>
 * </ul>
 * 
 * @xerces.internal
 *
 * @author Glenn Marcy, IBM
 * @author Andy Clark, IBM
 * @author Arnaud  Le Hors, IBM
 * @author Eric Ye, IBM
 *
 * @version $Id$
 */
public class XMLDocumentScannerImpl
    extends XMLDocumentFragmentScannerImpl {

    //
    // Constants
    //

    // scanner states

    /** Scanner state: XML declaration. */
    protected static final int SCANNER_STATE_XML_DECL = 0;

    /** Scanner state: prolog. */
    protected static final int SCANNER_STATE_PROLOG = 5;

    /** Scanner state: trailing misc. */
    protected static final int SCANNER_STATE_TRAILING_MISC = 12;

    /** Scanner state: DTD internal declarations. */
    protected static final int SCANNER_STATE_DTD_INTERNAL_DECLS = 17;

    /** Scanner state: open DTD external subset. */
    protected static final int SCANNER_STATE_DTD_EXTERNAL = 18;

    /** Scanner state: DTD external declarations. */
    protected static final int SCANNER_STATE_DTD_EXTERNAL_DECLS = 19;

    // feature identifiers

    /** Feature identifier: load external DTD. */
    protected static final String LOAD_EXTERNAL_DTD =
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.XERCES_FEATURE_PREFIX + org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.LOAD_EXTERNAL_DTD_FEATURE;

    /** Feature identifier: load external DTD. */
    protected static final String DISALLOW_DOCTYPE_DECL_FEATURE =
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.XERCES_FEATURE_PREFIX + org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.DISALLOW_DOCTYPE_DECL_FEATURE;

    // property identifiers

    /** Property identifier: DTD scanner. */
    protected static final String DTD_SCANNER =
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.XERCES_PROPERTY_PREFIX + org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.DTD_SCANNER_PROPERTY;

    /** property identifier:  ValidationManager */
    protected static final String VALIDATION_MANAGER =
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.XERCES_PROPERTY_PREFIX + org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.VALIDATION_MANAGER_PROPERTY;

    /** property identifier:  NamespaceContext */
    protected static final String NAMESPACE_CONTEXT =
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.XERCES_PROPERTY_PREFIX + org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.NAMESPACE_CONTEXT_PROPERTY;
        


    // recognized features and properties

    /** Recognized features. */
    private static final String[] RECOGNIZED_FEATURES = {
        LOAD_EXTERNAL_DTD,
        DISALLOW_DOCTYPE_DECL_FEATURE,
    };

    /** Feature defaults. */
    private static final Boolean[] FEATURE_DEFAULTS = {
        Boolean.TRUE,
        Boolean.FALSE,
    };

    /** Recognized properties. */
    private static final String[] RECOGNIZED_PROPERTIES = {
        DTD_SCANNER,
        VALIDATION_MANAGER,
        NAMESPACE_CONTEXT,
    };

    /** Property defaults. */
    private static final Object[] PROPERTY_DEFAULTS = {
        null,
        null,
        null,
    };

    //
    // Data
    //

    // properties

    /** DTD scanner. */
    protected XMLDTDScanner fDTDScanner;
    /** Validation manager . */
    protected ValidationManager fValidationManager;

    // protected data

    /** Scanning DTD. */
    protected boolean fScanningDTD;

    // other info

    /** Doctype name. */
    protected String fDoctypeName;

    /** Doctype declaration public identifier. */
    protected String fDoctypePublicId;

    /** Doctype declaration system identifier. */
    protected String fDoctypeSystemId;

    /** Namespace support. */
    protected NamespaceContext fNamespaceContext = new NamespaceSupport();

    // features

    /** Load external DTD. */
    protected boolean fLoadExternalDTD = true;

    /** Disallow doctype declaration. */
    protected boolean fDisallowDoctype = false;

    // state

    /** Seen doctype declaration. */
    protected boolean fSeenDoctypeDecl;

    // dispatchers

    /** XML declaration dispatcher. */
    protected final Dispatcher fXMLDeclDispatcher = new XMLDeclDispatcher();

    /** Prolog dispatcher. */
    protected final Dispatcher fPrologDispatcher = new PrologDispatcher();

    /** DTD dispatcher. */
    protected final Dispatcher fDTDDispatcher = new DTDDispatcher();

    /** Trailing miscellaneous section dispatcher. */
    protected final Dispatcher fTrailingMiscDispatcher = new TrailingMiscDispatcher();

    // temporary variables

    /** Array of 3 strings. */
    private final String[] fStrings = new String[3];

    /** String. */
    private final XMLString fString = new XMLString();

    /** String buffer. */
    private final XMLStringBuffer fStringBuffer = new XMLStringBuffer();
    
    /** External subset source. */
    private XMLInputSource fExternalSubsetSource = null;
    
    /** A DTD Description. */
    private final XMLDTDDescription fDTDDescription = new XMLDTDDescription(null, null, null, null, null);

    //
    // Constructors
    //

    /** Default constructor. */
    public XMLDocumentScannerImpl() {} // <init>()

    //
    // XMLDocumentScanner methods
    //

    /**
     * Sets the input source.
     *
     * @param inputSource The input source.
     *
     * @throws IOException Thrown on i/o error.
     */
    public void setInputSource(XMLInputSource inputSource) throws IOException {
        fEntityManager.setEntityHandler(this);
        fEntityManager.startDocumentEntity(inputSource);
        //fDocumentSystemId = fEntityManager.expandSystemId(inputSource.getSystemId());
    } // setInputSource(XMLInputSource)

    //
    // XMLComponent methods
    //

    /**
     * Resets the component. The component can query the component manager
     * about any features and properties that affect the operation of the
     * component.
     *
     * @param componentManager The component manager.
     *
     * @throws SAXException Thrown by component on initialization error.
     *                      For example, if a feature or property is
     *                      required for the operation of the component, the
     *                      component manager may throw a
     *                      SAXNotRecognizedException or a
     *                      SAXNotSupportedException.
     */
    public void reset(XMLComponentManager componentManager)
        throws XMLConfigurationException {

        super.reset(componentManager);

        // other settings
        fDoctypeName = null;
        fDoctypePublicId = null;
        fDoctypeSystemId = null;
        fSeenDoctypeDecl = false;
        fScanningDTD = false;
        fExternalSubsetSource = null;

		if (!fParserSettings) {
			// parser settings have not been changed
			fNamespaceContext.reset();
			// setup dispatcher
			setScannerState(SCANNER_STATE_XML_DECL);
			setDispatcher(fXMLDeclDispatcher);
			return;
		}

        // xerces features
        try {
            fLoadExternalDTD = componentManager.getFeature(LOAD_EXTERNAL_DTD);
        }
        catch (XMLConfigurationException e) {
            fLoadExternalDTD = true;
        }
        try {
            fDisallowDoctype = componentManager.getFeature(DISALLOW_DOCTYPE_DECL_FEATURE);
        }
        catch (XMLConfigurationException e) {
            fDisallowDoctype = false;
        }

        // xerces properties
        fDTDScanner = (XMLDTDScanner)componentManager.getProperty(DTD_SCANNER);
        try {
            fValidationManager = (ValidationManager)componentManager.getProperty(VALIDATION_MANAGER);
        }
        catch (XMLConfigurationException e) {
            fValidationManager = null;
        }

        try {
            fNamespaceContext = (NamespaceContext)componentManager.getProperty(NAMESPACE_CONTEXT);
        }
        catch (XMLConfigurationException e) { }
        if (fNamespaceContext == null) {
            fNamespaceContext = new NamespaceSupport();
        }
        fNamespaceContext.reset();
        
        // setup dispatcher
        setScannerState(SCANNER_STATE_XML_DECL);
        setDispatcher(fXMLDeclDispatcher);

    } // reset(XMLComponentManager)

    /**
     * Returns a list of feature identifiers that are recognized by
     * this component. This method may return null if no features
     * are recognized by this component.
     */
    public String[] getRecognizedFeatures() {
        String[] featureIds = super.getRecognizedFeatures();
        int length = featureIds != null ? featureIds.length : 0;
        String[] combinedFeatureIds = new String[length + RECOGNIZED_FEATURES.length];
        if (featureIds != null) {
            System.arraycopy(featureIds, 0, combinedFeatureIds, 0, featureIds.length);
        }
        System.arraycopy(RECOGNIZED_FEATURES, 0, combinedFeatureIds, length, RECOGNIZED_FEATURES.length);
        return combinedFeatureIds;
    } // getRecognizedFeatures():String[]

    /**
     * Sets the state of a feature. This method is called by the component
     * manager any time after reset when a feature changes state.
     * <p>
     * <strong>Note:</strong> Components should silently ignore features
     * that do not affect the operation of the component.
     *
     * @param featureId The feature identifier.
     * @param state     The state of the feature.
     *
     * @throws SAXNotRecognizedException The component should not throw
     *                                   this exception.
     * @throws SAXNotSupportedException The component should not throw
     *                                  this exception.
     */
    public void setFeature(String featureId, boolean state)
        throws XMLConfigurationException {

        super.setFeature(featureId, state);

        // Xerces properties
        if (featureId.startsWith(org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.XERCES_FEATURE_PREFIX)) {
            final int suffixLength = featureId.length() - org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.XERCES_FEATURE_PREFIX.length();
        	
            if (suffixLength == org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.LOAD_EXTERNAL_DTD_FEATURE.length() && 
                featureId.endsWith(org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.LOAD_EXTERNAL_DTD_FEATURE)) {
                fLoadExternalDTD = state;
                return;
            }
            else if (suffixLength == org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.DISALLOW_DOCTYPE_DECL_FEATURE.length() && 
                featureId.endsWith(org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.DISALLOW_DOCTYPE_DECL_FEATURE)) {
                fDisallowDoctype = state;
                return;
            }
        }

    } // setFeature(String,boolean)

    /**
     * Returns a list of property identifiers that are recognized by
     * this component. This method may return null if no properties
     * are recognized by this component.
     */
    public String[] getRecognizedProperties() {
        String[] propertyIds = super.getRecognizedProperties();
        int length = propertyIds != null ? propertyIds.length : 0;
        String[] combinedPropertyIds = new String[length + RECOGNIZED_PROPERTIES.length];
        if (propertyIds != null) {
            System.arraycopy(propertyIds, 0, combinedPropertyIds, 0, propertyIds.length);
        }
        System.arraycopy(RECOGNIZED_PROPERTIES, 0, combinedPropertyIds, length, RECOGNIZED_PROPERTIES.length);
        return combinedPropertyIds;
    } // getRecognizedProperties():String[]

    /**
     * Sets the value of a property. This method is called by the component
     * manager any time after reset when a property changes value.
     * <p>
     * <strong>Note:</strong> Components should silently ignore properties
     * that do not affect the operation of the component.
     *
     * @param propertyId The property identifier.
     * @param value      The value of the property.
     *
     * @throws SAXNotRecognizedException The component should not throw
     *                                   this exception.
     * @throws SAXNotSupportedException The component should not throw
     *                                  this exception.
     */
    public void setProperty(String propertyId, Object value)
        throws XMLConfigurationException {

        super.setProperty(propertyId, value);

        // Xerces properties
        if (propertyId.startsWith(org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.XERCES_PROPERTY_PREFIX)) {
            final int suffixLength = propertyId.length() - org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.XERCES_PROPERTY_PREFIX.length();
            
            if (suffixLength == org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.DTD_SCANNER_PROPERTY.length() && 
                propertyId.endsWith(org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.DTD_SCANNER_PROPERTY)) {
                fDTDScanner = (XMLDTDScanner)value;
            }
            if (suffixLength == org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.NAMESPACE_CONTEXT_PROPERTY.length() && 
                propertyId.endsWith(Constants.NAMESPACE_CONTEXT_PROPERTY)) {
                if (value != null) {
                    fNamespaceContext = (NamespaceContext)value;
                }
            }

            return;
        }

    } // setProperty(String,Object)

    /** 
     * Returns the default state for a feature, or null if this
     * component does not want to report a default value for this
     * feature.
     *
     * @param featureId The feature identifier.
     *
     * @since Xerces 2.2.0
     */
    public Boolean getFeatureDefault(String featureId) {

        for (int i = 0; i < RECOGNIZED_FEATURES.length; i++) {
            if (RECOGNIZED_FEATURES[i].equals(featureId)) {
                return FEATURE_DEFAULTS[i];
            }
        }
        return super.getFeatureDefault(featureId);
    } // getFeatureDefault(String):Boolean

    /** 
     * Returns the default state for a property, or null if this
     * component does not want to report a default value for this
     * property. 
     *
     * @param propertyId The property identifier.
     *
     * @since Xerces 2.2.0
     */
    public Object getPropertyDefault(String propertyId) {
        for (int i = 0; i < RECOGNIZED_PROPERTIES.length; i++) {
            if (RECOGNIZED_PROPERTIES[i].equals(propertyId)) {
                return PROPERTY_DEFAULTS[i];
            }
        }
        return super.getPropertyDefault(propertyId);
    } // getPropertyDefault(String):Object

    //
    // XMLEntityHandler methods
    //

    /**
     * This method notifies of the start of an entity. The DTD has the
     * pseudo-name of "[dtd]" parameter entity names start with '%'; and
     * general entities are just specified by their name.
     *
     * @param name     The name of the entity.
     * @param identifier The resource identifier.
     * @param encoding The auto-detected IANA encoding name of the entity
     *                 stream. This value will be null in those situations
     *                 where the entity encoding is not auto-detected (e.g.
     *                 internal entities or a document entity that is
     *                 parsed from a java.io.Reader).
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void startEntity(String name,
                            XMLResourceIdentifier identifier,
                            String encoding, Augmentations augs) throws XNIException {

        super.startEntity(name, identifier, encoding, augs);

        // prepare to look for a TextDecl if external general entity
        if (!name.equals("[xml]") && fEntityScanner.isExternal()) {
            setScannerState(SCANNER_STATE_TEXT_DECL);
        } 

        // call handler
        if (fDocumentHandler != null && name.equals("[xml]")) {
            fDocumentHandler.startDocument(fEntityScanner, encoding, fNamespaceContext, null);
        }

    } // startEntity(String,identifier,String)

    /**
     * This method notifies the end of an entity. The DTD has the pseudo-name
     * of "[dtd]" parameter entity names start with '%'; and general entities
     * are just specified by their name.
     *
     * @param name The name of the entity.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void endEntity(String name, Augmentations augs) throws XNIException {

        super.endEntity(name, augs);

        // call handler
        if (fDocumentHandler != null && name.equals("[xml]")) {
            fDocumentHandler.endDocument(null);
        }

    } // endEntity(String)

    //
    // Protected methods
    //

    // dispatcher factory methods

    /** Creates a content dispatcher. */
    protected Dispatcher createContentDispatcher() {
        return new ContentDispatcher();
    } // createContentDispatcher():Dispatcher

    // scanning methods

    /** Scans a doctype declaration. */
    protected boolean scanDoctypeDecl() throws IOException, XNIException {

        // spaces
        if (!fEntityScanner.skipSpaces()) {
            reportFatalError("MSG_SPACE_REQUIRED_BEFORE_ROOT_ELEMENT_TYPE_IN_DOCTYPEDECL",
                             null);
        }

        // root element name
        fDoctypeName = fEntityScanner.scanName();
        if (fDoctypeName == null) {
            reportFatalError("MSG_ROOT_ELEMENT_TYPE_REQUIRED", null);
        }

        // external id
        if (fEntityScanner.skipSpaces()) {
            scanExternalID(fStrings, false);
            fDoctypeSystemId = fStrings[0];
            fDoctypePublicId = fStrings[1];
            fEntityScanner.skipSpaces();
        }

        fHasExternalDTD = fDoctypeSystemId != null;
        
        // Attempt to locate an external subset with an external subset resolver.
        if (!fHasExternalDTD && fExternalSubsetResolver != null) {
            fDTDDescription.setValues(null, null, fEntityManager.getCurrentResourceIdentifier().getExpandedSystemId(), null);
            fDTDDescription.setRootName(fDoctypeName);
            fExternalSubsetSource = fExternalSubsetResolver.getExternalSubset(fDTDDescription);
            fHasExternalDTD = fExternalSubsetSource != null;
        }

        // call handler
        if (fDocumentHandler != null) {
            // NOTE: I don't like calling the doctypeDecl callback until
            //       end of the *full* doctype line (including internal
            //       subset) is parsed correctly but SAX2 requires that
            //       it knows the root element name and public and system
            //       identifier for the startDTD call. -Ac
            if (fExternalSubsetSource == null) {
                fDocumentHandler.doctypeDecl(fDoctypeName, fDoctypePublicId, fDoctypeSystemId, null);
            }
            else {
                fDocumentHandler.doctypeDecl(fDoctypeName, fExternalSubsetSource.getPublicId(), fExternalSubsetSource.getSystemId(), null);
            }
        }

        // is there an internal subset?
        boolean internalSubset = true;
        if (!fEntityScanner.skipChar('[')) {
            internalSubset = false;
            fEntityScanner.skipSpaces();
            if (!fEntityScanner.skipChar('>')) {
                reportFatalError("DoctypedeclUnterminated", new Object[]{fDoctypeName});
            }
            fMarkupDepth--;
        }

        return internalSubset;

    } // scanDoctypeDecl():boolean

    //
    // Private methods
    //

    /** Returns the scanner state name. */
    protected String getScannerStateName(int state) {

        switch (state) {
            case SCANNER_STATE_XML_DECL: return "SCANNER_STATE_XML_DECL";
            case SCANNER_STATE_PROLOG: return "SCANNER_STATE_PROLOG";
            case SCANNER_STATE_TRAILING_MISC: return "SCANNER_STATE_TRAILING_MISC";
            case SCANNER_STATE_DTD_INTERNAL_DECLS: return "SCANNER_STATE_DTD_INTERNAL_DECLS";
            case SCANNER_STATE_DTD_EXTERNAL: return "SCANNER_STATE_DTD_EXTERNAL";
            case SCANNER_STATE_DTD_EXTERNAL_DECLS: return "SCANNER_STATE_DTD_EXTERNAL_DECLS";
        }
        return super.getScannerStateName(state);

    } // getScannerStateName(int):String

    //
    // Classes
    //

    /**
     * Dispatcher to handle XMLDecl scanning.
     *
     * @author Andy Clark, IBM
     */
    protected final class XMLDeclDispatcher
        implements Dispatcher {

        //
        // Dispatcher methods
        //

        /**
         * Dispatch an XML "event".
         *
         * @param complete True if this dispatcher is intended to scan
         *                 and dispatch as much as possible.
         *
         * @return True if there is more to dispatch either from this
         *          or a another dispatcher.
         *
         * @throws IOException  Thrown on i/o error.
         * @throws XNIException Thrown on parse error.
         */
        public boolean dispatch(boolean complete)
            throws IOException, XNIException {

            // next dispatcher is prolog regardless of whether there
            // is an XMLDecl in this document
            setScannerState(SCANNER_STATE_PROLOG);
            setDispatcher(fPrologDispatcher);

            // scan XMLDecl
            try {
                if (fEntityScanner.skipString("<?xml")) {
                    fMarkupDepth++;
                    // NOTE: special case where document starts with a PI
                    //       whose name starts with "xml" (e.g. "xmlfoo")
                    if (XMLChar.isName(fEntityScanner.peekChar())) {
                        fStringBuffer.clear();
                        fStringBuffer.append("xml");
                        if (fNamespaces) {
                            while (XMLChar.isNCName(fEntityScanner.peekChar())) {
                                fStringBuffer.append((char)fEntityScanner.scanChar());
                            }
                        }
                        else {
                            while (XMLChar.isName(fEntityScanner.peekChar())) {
                                fStringBuffer.append((char)fEntityScanner.scanChar());
                            }
                        }
                        String target = fSymbolTable.addSymbol(fStringBuffer.ch, fStringBuffer.offset, fStringBuffer.length);
                        scanPIData(target, fString);
                    }

                    // standard XML declaration
                    else {
                        scanXMLDeclOrTextDecl(false);
                    }
                }
                fEntityManager.fCurrentEntity.mayReadChunks = true;

                // if no XMLDecl, then scan piece of prolog
                return true;
            }
            // encoding errors
            catch (MalformedByteSequenceException e) {
                fErrorReporter.reportError(e.getDomain(), e.getKey(), 
                    e.getArguments(), org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_FATAL_ERROR, e);
                return false;
            }
            catch (CharConversionException e) {
                fErrorReporter.reportError(
                        XMLMessageFormatter.XML_DOMAIN,
                        "CharConversionFailure",
                        null,
                        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_FATAL_ERROR, e);
                return false;
            }
            // premature end of file
            catch (EOFException e) {
                reportFatalError("PrematureEOF", null);
                return false;
                //throw e;
            }


        } // dispatch(boolean):boolean

    } // class XMLDeclDispatcher

    /**
     * Dispatcher to handle prolog scanning.
     *
     * @author Andy Clark, IBM
     */
    protected final class PrologDispatcher
        implements Dispatcher {

        //
        // Dispatcher methods
        //

        /**
         * Dispatch an XML "event".
         *
         * @param complete True if this dispatcher is intended to scan
         *                 and dispatch as much as possible.
         *
         * @return True if there is more to dispatch either from this
         *          or a another dispatcher.
         *
         * @throws IOException  Thrown on i/o error.
         * @throws XNIException Thrown on parse error.
         */
        public boolean dispatch(boolean complete)
            throws IOException, XNIException {

            try {
                boolean again;
                do {
                    again = false;
                    switch (fScannerState) {
                        case SCANNER_STATE_PROLOG: {
                            fEntityScanner.skipSpaces();
                            if (fEntityScanner.skipChar('<')) {
                                setScannerState(SCANNER_STATE_START_OF_MARKUP);
                                again = true;
                            }
                            else if (fEntityScanner.skipChar('&')) {
                                setScannerState(SCANNER_STATE_REFERENCE);
                                again = true;
                            }
                            else {
                                setScannerState(SCANNER_STATE_CONTENT);
                                again = true;
                            }
                            break;
                        }
                        case SCANNER_STATE_START_OF_MARKUP: {
                            fMarkupDepth++;
                            if (fEntityScanner.skipChar('!')) {
                                if (fEntityScanner.skipChar('-')) {
                                    if (!fEntityScanner.skipChar('-')) {
                                        reportFatalError("InvalidCommentStart",
                                                         null);
                                    }
                                    setScannerState(SCANNER_STATE_COMMENT);
                                    again = true;
                                }
                                else if (fEntityScanner.skipString("DOCTYPE")) {
                                    setScannerState(SCANNER_STATE_DOCTYPE);
                                    again = true;
                                }
                                else {
                                    reportFatalError("MarkupNotRecognizedInProlog",
                                                     null);
                                }
                            }
                            else if (isValidNameStartChar(fEntityScanner.peekChar())) {
                                setScannerState(SCANNER_STATE_ROOT_ELEMENT);
                                setDispatcher(fContentDispatcher);
                                return true;
                            }
                            else if (fEntityScanner.skipChar('?')) {
                                setScannerState(SCANNER_STATE_PI);
                                again = true;
                            }
                            else if (isValidNameStartHighSurrogate(fEntityScanner.peekChar())) {
                                setScannerState(SCANNER_STATE_ROOT_ELEMENT);
                                setDispatcher(fContentDispatcher);
                                return true;
                            }
                            else {
                                reportFatalError("MarkupNotRecognizedInProlog",
                                                 null);
                            }
                            break;
                        }
                        case SCANNER_STATE_COMMENT: {
                            scanComment();
                            setScannerState(SCANNER_STATE_PROLOG);
                            break;
                        }
                        case SCANNER_STATE_PI: {
                            scanPI();
                            setScannerState(SCANNER_STATE_PROLOG);
                            break;
                        }
                        case SCANNER_STATE_DOCTYPE: {
                            if (fDisallowDoctype) {
                                reportFatalError("DoctypeNotAllowed", null);
                            }
                            if (fSeenDoctypeDecl) {
                                reportFatalError("AlreadySeenDoctype", null);
                            }
                            fSeenDoctypeDecl = true;

                            // scanDoctypeDecl() sends XNI doctypeDecl event that 
                            // in SAX is converted to startDTD() event.
                            if (scanDoctypeDecl()) {
                                setScannerState(SCANNER_STATE_DTD_INTERNAL_DECLS);
                                setDispatcher(fDTDDispatcher);
                                return true;
                            }
                            
                            // handle external subset
                            if (fDoctypeSystemId != null) {
                                fIsEntityDeclaredVC = !fStandalone;
                                if (((fValidation || fLoadExternalDTD) 
                                    && (fValidationManager == null || !fValidationManager.isCachedDTD()))) {
                                    setScannerState(SCANNER_STATE_DTD_EXTERNAL);
                                    setDispatcher(fDTDDispatcher);
                                    return true;
                                }
                            }
                            else if (fExternalSubsetSource != null) {
                                fIsEntityDeclaredVC = !fStandalone;
                                if (((fValidation || fLoadExternalDTD) 
                                    && (fValidationManager == null || !fValidationManager.isCachedDTD()))) {
                                    // This handles the case of a DOCTYPE that had neither an internal subset or an external subset.
                                    fDTDScanner.setInputSource(fExternalSubsetSource);
                                    fExternalSubsetSource = null;
                                    setScannerState(SCANNER_STATE_DTD_EXTERNAL_DECLS);
                                    setDispatcher(fDTDDispatcher);
                                    return true;
                                }                       	
                            }
                            
                            // Send endDTD() call if: 
                            // a) systemId is null or if an external subset resolver could not locate an external subset.
                            // b) "load-external-dtd" and validation are false
                            // c) DTD grammar is cached
                                
                            // in XNI this results in 3 events:  doctypeDecl, startDTD, endDTD
                            // in SAX this results in 2 events: startDTD, endDTD
                            fDTDScanner.setInputSource(null);
                            setScannerState(SCANNER_STATE_PROLOG);
                            break;
                        }
                        case SCANNER_STATE_CONTENT: {
                            reportFatalError("ContentIllegalInProlog", null);
                            fEntityScanner.scanChar();
                        }
                        case SCANNER_STATE_REFERENCE: {
                            reportFatalError("ReferenceIllegalInProlog", null);
                        }
                    }
                } while (complete || again);

                if (complete) {
                    if (fEntityScanner.scanChar() != '<') {
                        reportFatalError("RootElementRequired", null);
                    }
                    setScannerState(SCANNER_STATE_ROOT_ELEMENT);
                    setDispatcher(fContentDispatcher);
                }
            }
            // encoding errors
            catch (MalformedByteSequenceException e) {
                fErrorReporter.reportError(e.getDomain(), e.getKey(), 
                    e.getArguments(), org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_FATAL_ERROR, e);
                return false;
            }
            catch (CharConversionException e) {
                fErrorReporter.reportError(
                        XMLMessageFormatter.XML_DOMAIN,
                        "CharConversionFailure",
                        null,
                        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_FATAL_ERROR, e);
                return false;
            }
            // premature end of file
            catch (EOFException e) {
                reportFatalError("PrematureEOF", null);
                return false;
                //throw e;
            }

            return true;

        } // dispatch(boolean):boolean

    } // class PrologDispatcher

    /**
     * Dispatcher to handle the internal and external DTD subsets.
     *
     * @author Andy Clark, IBM
     */
    protected final class DTDDispatcher
        implements Dispatcher {

        //
        // Dispatcher methods
        //

        /**
         * Dispatch an XML "event".
         *
         * @param complete True if this dispatcher is intended to scan
         *                 and dispatch as much as possible.
         *
         * @return True if there is more to dispatch either from this
         *          or a another dispatcher.
         *
         * @throws IOException  Thrown on i/o error.
         * @throws XNIException Thrown on parse error.
         */
        public boolean dispatch(boolean complete)
            throws IOException, XNIException {
            fEntityManager.setEntityHandler(null);
            try {
                boolean again;
                do {
                    again = false;
                    switch (fScannerState) {
                        case SCANNER_STATE_DTD_INTERNAL_DECLS: {
                            // REVISIT: Should there be a feature for
                            //          the "complete" parameter?
                            boolean completeDTD = true;
                            boolean readExternalSubset = (fValidation || fLoadExternalDTD) && (fValidationManager == null || !fValidationManager.isCachedDTD());
                            boolean moreToScan = fDTDScanner.scanDTDInternalSubset(completeDTD, fStandalone, fHasExternalDTD && readExternalSubset);
                            if (!moreToScan) {
                                // end doctype declaration
                                if (!fEntityScanner.skipChar(']')) {
                                    reportFatalError("EXPECTED_SQUARE_BRACKET_TO_CLOSE_INTERNAL_SUBSET",
                                                     null);
                                }
                                fEntityScanner.skipSpaces();
                                if (!fEntityScanner.skipChar('>')) {
                                    reportFatalError("DoctypedeclUnterminated", new Object[]{fDoctypeName});
                                }
                                fMarkupDepth--;

                                // scan external subset next
                                if (fDoctypeSystemId != null) {
                                    fIsEntityDeclaredVC = !fStandalone;
                                    if (readExternalSubset) {
                                        setScannerState(SCANNER_STATE_DTD_EXTERNAL);
                                        break;
                                    }
                                }
                                else if (fExternalSubsetSource != null) {
                                    fIsEntityDeclaredVC = !fStandalone;
                                    if (readExternalSubset) {
                                        // This handles the case of a DOCTYPE that only had an internal subset.
                                        fDTDScanner.setInputSource(fExternalSubsetSource);
                                        fExternalSubsetSource = null;
                                        setScannerState(SCANNER_STATE_DTD_EXTERNAL_DECLS);
                                        break;
                                    }
                                }
                                // This document only has an internal subset. If it contains parameter entity
                                // references and standalone="no" then [Entity Declared] is a validity constraint.
                                else {
                                    fIsEntityDeclaredVC = fEntityManager.hasPEReferences() && !fStandalone;
                                }
                                
                                // break out of this dispatcher.
                                setScannerState(SCANNER_STATE_PROLOG);
                                setDispatcher(fPrologDispatcher);
                                fEntityManager.setEntityHandler(XMLDocumentScannerImpl.this);
                                return true;
                            }
                            break;
                        }
                        case SCANNER_STATE_DTD_EXTERNAL: {
                            fDTDDescription.setValues(fDoctypePublicId, fDoctypeSystemId, null, null);
                            fDTDDescription.setRootName(fDoctypeName);
                            XMLInputSource xmlInputSource =
                                fEntityManager.resolveEntity(fDTDDescription);
                            fDTDScanner.setInputSource(xmlInputSource);
                            setScannerState(SCANNER_STATE_DTD_EXTERNAL_DECLS);
                            again = true;
                            break;
                        }
                        case SCANNER_STATE_DTD_EXTERNAL_DECLS: {
                            // REVISIT: Should there be a feature for
                            //          the "complete" parameter?
                            boolean completeDTD = true;
                            boolean moreToScan = fDTDScanner.scanDTDExternalSubset(completeDTD);
                            if (!moreToScan) {
                                setScannerState(SCANNER_STATE_PROLOG);
                                setDispatcher(fPrologDispatcher);
                                fEntityManager.setEntityHandler(XMLDocumentScannerImpl.this);
                                return true;
                            }
                            break;
                        }
                        default: {
                            throw new XNIException("DTDDispatcher#dispatch: scanner state="+fScannerState+" ("+getScannerStateName(fScannerState)+')');
                        }
                    }
                } while (complete || again);
            }
            // encoding errors
            catch (MalformedByteSequenceException e) {
                fErrorReporter.reportError(e.getDomain(), e.getKey(), 
                    e.getArguments(), org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_FATAL_ERROR, e);
                return false;
            }
            catch (CharConversionException e) {
                fErrorReporter.reportError(
                        XMLMessageFormatter.XML_DOMAIN,
                        "CharConversionFailure",
                        null,
                        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_FATAL_ERROR, e);
                return false;
            }
            // premature end of file
            catch (EOFException e) {
                reportFatalError("PrematureEOF", null);
                return false;
                //throw e;
            }

            // cleanup
            finally {
                fEntityManager.setEntityHandler(XMLDocumentScannerImpl.this);
            }

            return true;

        } // dispatch(boolean):boolean

    } // class DTDDispatcher

    /**
     * Dispatcher to handle content scanning.
     *
     * @author Andy Clark, IBM
     * @author Eric Ye, IBM
     */
    protected class ContentDispatcher
        extends FragmentContentDispatcher {

        //
        // Protected methods
        //

        // hooks

        // NOTE: These hook methods are added so that the full document
        //       scanner can share the majority of code with this class.

        /**
         * Scan for DOCTYPE hook. This method is a hook for subclasses
         * to add code to handle scanning for a the "DOCTYPE" string
         * after the string "<!" has been scanned.
         *
         * @return True if the "DOCTYPE" was scanned; false if "DOCTYPE"
         *          was not scanned.
         */
        protected boolean scanForDoctypeHook()
            throws IOException, XNIException {

            if (fEntityScanner.skipString("DOCTYPE")) {
                setScannerState(SCANNER_STATE_DOCTYPE);
                return true;
            }
            return false;

        } // scanForDoctypeHook():boolean

        /**
         * Element depth iz zero. This methos is a hook for subclasses
         * to add code to handle when the element depth hits zero. When
         * scanning a document fragment, an element depth of zero is
         * normal. However, when scanning a full XML document, the
         * scanner must handle the trailing miscellanous section of
         * the document after the end of the document's root element.
         *
         * @return True if the caller should stop and return true which
         *          allows the scanner to switch to a new scanning
         *          dispatcher. A return value of false indicates that
         *          the content dispatcher should continue as normal.
         */
        protected boolean elementDepthIsZeroHook()
            throws IOException, XNIException {

            setScannerState(SCANNER_STATE_TRAILING_MISC);
            setDispatcher(fTrailingMiscDispatcher);
            return true;

        } // elementDepthIsZeroHook():boolean

        /**
         * Scan for root element hook. This method is a hook for
         * subclasses to add code that handles scanning for the root
         * element. When scanning a document fragment, there is no
         * "root" element. However, when scanning a full XML document,
         * the scanner must handle the root element specially.
         *
         * @return True if the caller should stop and return true which
         *          allows the scanner to switch to a new scanning
         *          dispatcher. A return value of false indicates that
         *          the content dispatcher should continue as normal.
         */
        protected boolean scanRootElementHook()
            throws IOException, XNIException {

            if (fExternalSubsetResolver != null && !fSeenDoctypeDecl 
                && !fDisallowDoctype && (fValidation || fLoadExternalDTD)) {
                scanStartElementName();
                resolveExternalSubsetAndRead();
                if (scanStartElementAfterName()) {
                    setScannerState(SCANNER_STATE_TRAILING_MISC);
                    setDispatcher(fTrailingMiscDispatcher);
                    return true;
                }
            }
            else if (scanStartElement()) {
                setScannerState(SCANNER_STATE_TRAILING_MISC);
                setDispatcher(fTrailingMiscDispatcher);
                return true;
            }
            return false;

        } // scanRootElementHook():boolean

        /**
         * End of file hook. This method is a hook for subclasses to
         * add code that handles the end of file. The end of file in
         * a document fragment is OK if the markup depth is zero.
         * However, when scanning a full XML document, an end of file
         * is always premature.
         */
        protected void endOfFileHook(EOFException e)
            throws IOException, XNIException {

            reportFatalError("PrematureEOF", null);
            // in case continue-after-fatal-error set, should not do this...
            //throw e;

        } // endOfFileHook()
        
        /**
         * <p>Attempt to locate an external subset for a document that does not otherwise
         * have one. If an external subset is located, then it is scanned.</p>
         */
        protected void resolveExternalSubsetAndRead()
            throws IOException, XNIException {
            
            fDTDDescription.setValues(null, null, fEntityManager.getCurrentResourceIdentifier().getExpandedSystemId(), null);
            fDTDDescription.setRootName(fElementQName.rawname);
            XMLInputSource src = fExternalSubsetResolver.getExternalSubset(fDTDDescription);
            
            if (src != null) {
                fDoctypeName = fElementQName.rawname;
                fDoctypePublicId = src.getPublicId();
                fDoctypeSystemId = src.getSystemId();
                // call document handler
                if (fDocumentHandler != null) {
                    // This inserts a doctypeDecl event into the stream though no 
                    // DOCTYPE existed in the instance document.
                    fDocumentHandler.doctypeDecl(fDoctypeName, fDoctypePublicId, fDoctypeSystemId, null);
                }
                try {
                    if (fValidationManager == null || !fValidationManager.isCachedDTD()) {
                        fDTDScanner.setInputSource(src);
                        while (fDTDScanner.scanDTDExternalSubset(true));
                    }
                    else {
                        // This sends startDTD and endDTD calls down the pipeline.
                        fDTDScanner.setInputSource(null);
                    }
                }
                finally {
                    fEntityManager.setEntityHandler(XMLDocumentScannerImpl.this);
                }
            }
        } // resolveExternalSubsetAndRead()

    } // class ContentDispatcher

    /**
     * Dispatcher to handle trailing miscellaneous section scanning.
     *
     * @author Andy Clark, IBM
     * @author Eric Ye, IBM
     */
    protected final class TrailingMiscDispatcher
        implements Dispatcher {

        //
        // Dispatcher methods
        //

        /**
         * Dispatch an XML "event".
         *
         * @param complete True if this dispatcher is intended to scan
         *                 and dispatch as much as possible.
         *
         * @return True if there is more to dispatch either from this
         *          or a another dispatcher.
         *
         * @throws IOException  Thrown on i/o error.
         * @throws XNIException Thrown on parse error.
         */
        public boolean dispatch(boolean complete)
            throws IOException, XNIException {

            try {
                boolean again;
                do {
                    again = false;
                    switch (fScannerState) {
                        case SCANNER_STATE_TRAILING_MISC: {
                            fEntityScanner.skipSpaces();
                            if (fEntityScanner.skipChar('<')) {
                                setScannerState(SCANNER_STATE_START_OF_MARKUP);
                                again = true;
                            }
                            else {
                                setScannerState(SCANNER_STATE_CONTENT);
                                again = true;
                            }
                            break;
                        }
                        case SCANNER_STATE_START_OF_MARKUP: {
                            fMarkupDepth++;
                            if (fEntityScanner.skipChar('?')) {
                                setScannerState(SCANNER_STATE_PI);
                                again = true;
                            }
                            else if (fEntityScanner.skipChar('!')) {
                                setScannerState(SCANNER_STATE_COMMENT);
                                again = true;
                            }
                            else if (fEntityScanner.skipChar('/')) {
                                reportFatalError("MarkupNotRecognizedInMisc",
                                                 null);
                                again = true;
                            }
                            else if (isValidNameStartChar(fEntityScanner.peekChar())) {
                                reportFatalError("MarkupNotRecognizedInMisc",
                                                 null);
                                scanStartElement();
                                setScannerState(SCANNER_STATE_CONTENT);
                            }
                            else if (isValidNameStartHighSurrogate(fEntityScanner.peekChar())) {
                                reportFatalError("MarkupNotRecognizedInMisc",
                                                 null);
                                scanStartElement();
                                setScannerState(SCANNER_STATE_CONTENT);
                            }
                            else {
                                reportFatalError("MarkupNotRecognizedInMisc",
                                                 null);
                            }
                            break;
                        }
                        case SCANNER_STATE_PI: {
                            scanPI();
                            setScannerState(SCANNER_STATE_TRAILING_MISC);
                            break;
                        }
                        case SCANNER_STATE_COMMENT: {
                            if (!fEntityScanner.skipString("--")) {
                                reportFatalError("InvalidCommentStart", null);
                            }
                            scanComment();
                            setScannerState(SCANNER_STATE_TRAILING_MISC);
                            break;
                        }
                        case SCANNER_STATE_CONTENT: {
                            int ch = fEntityScanner.peekChar();
                            if (ch == -1) {
                                setScannerState(SCANNER_STATE_TERMINATED);
                                return false;
                            }
                            reportFatalError("ContentIllegalInTrailingMisc",
                                             null);
                            fEntityScanner.scanChar();
                            setScannerState(SCANNER_STATE_TRAILING_MISC);
                            break;
                        }
                        case SCANNER_STATE_REFERENCE: {
                            reportFatalError("ReferenceIllegalInTrailingMisc",
                                             null);
                            setScannerState(SCANNER_STATE_TRAILING_MISC);
                            break;
                        }
                        case SCANNER_STATE_TERMINATED: {
                            return false;
                        }
                    }
                } while (complete || again);
            }
            // encoding errors
            catch (MalformedByteSequenceException e) {
                fErrorReporter.reportError(e.getDomain(), e.getKey(), 
                    e.getArguments(), org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_FATAL_ERROR, e);
                return false;
            }
            catch (CharConversionException e) {
                fErrorReporter.reportError(
                        XMLMessageFormatter.XML_DOMAIN,
                        "CharConversionFailure",
                        null,
                        XMLErrorReporter.SEVERITY_FATAL_ERROR, e);
                return false;
            }
            catch (EOFException e) {
                // NOTE: This is the only place we're allowed to reach
                //       the real end of the document stream. Unless the
                //       end of file was reached prematurely.
                if (fMarkupDepth != 0) {
                    reportFatalError("PrematureEOF", null);
                    return false;
                    //throw e;
                }

                setScannerState(SCANNER_STATE_TERMINATED);
                return false;
            }

            return true;

        } // dispatch(boolean):boolean

    } // class TrailingMiscDispatcher

} // class XMLDocumentScannerImpl
