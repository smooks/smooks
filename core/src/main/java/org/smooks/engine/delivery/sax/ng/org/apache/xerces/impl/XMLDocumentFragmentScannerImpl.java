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
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.ExternalSubsetResolver;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityHandler;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLScanner;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.io.MalformedByteSequenceException;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.AugmentationsImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.XMLAttributesImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.XMLChar;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.XMLStringBuffer;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.XMLSymbols;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.QName;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLAttributes;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLDocumentHandler;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLResourceIdentifier;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLString;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLComponent;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLComponentManager;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLConfigurationException;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLDocumentScanner;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLInputSource;

/**
 * This class is responsible for scanning the structure and content
 * of document fragments. The scanner acts as the source for the 
 * document information which is communicated to the document handler.
 * <p>
 * This component requires the following features and properties from the
 * component manager that uses it:
 * <ul>
 *  <li>http://xml.org/sax/features/validation</li>
 *  <li>http://apache.org/xml/features/scanner/notify-char-refs</li>
 *  <li>http://apache.org/xml/features/scanner/notify-builtin-refs</li>
 *  <li>http://apache.org/xml/properties/internal/symbol-table</li>
 *  <li>http://apache.org/xml/properties/internal/error-reporter</li>
 *  <li>http://apache.org/xml/properties/internal/entity-manager</li>
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
public class XMLDocumentFragmentScannerImpl
    extends XMLScanner
    implements XMLDocumentScanner, XMLComponent, XMLEntityHandler {

    //
    // Constants
    //

    // scanner states

    /** Scanner state: start of markup. */
    protected static final int SCANNER_STATE_START_OF_MARKUP = 1;

    /** Scanner state: comment. */
    protected static final int SCANNER_STATE_COMMENT = 2;

    /** Scanner state: processing instruction. */
    protected static final int SCANNER_STATE_PI = 3;

    /** Scanner state: DOCTYPE. */
    protected static final int SCANNER_STATE_DOCTYPE = 4;

    /** Scanner state: root element. */
    protected static final int SCANNER_STATE_ROOT_ELEMENT = 6;

    /** Scanner state: content. */
    protected static final int SCANNER_STATE_CONTENT = 7;

    /** Scanner state: reference. */
    protected static final int SCANNER_STATE_REFERENCE = 8;

    /** Scanner state: end of input. */
    protected static final int SCANNER_STATE_END_OF_INPUT = 13;

    /** Scanner state: terminated. */
    protected static final int SCANNER_STATE_TERMINATED = 14;

    /** Scanner state: CDATA section. */
    protected static final int SCANNER_STATE_CDATA = 15;

    /** Scanner state: Text declaration. */
    protected static final int SCANNER_STATE_TEXT_DECL = 16;

    // feature identifiers

    /** Feature identifier: namespaces. */
    protected static final String NAMESPACES = 
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.SAX_FEATURE_PREFIX + org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.NAMESPACES_FEATURE;

    /** Feature identifier: notify built-in refereces. */
    protected static final String NOTIFY_BUILTIN_REFS =
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.XERCES_FEATURE_PREFIX + org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.NOTIFY_BUILTIN_REFS_FEATURE;
        
    // property identifiers
    
    /** Property identifier: entity resolver. */
    protected static final String ENTITY_RESOLVER =
        org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.XERCES_PROPERTY_PREFIX + org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.ENTITY_RESOLVER_PROPERTY;
    
    // recognized features and properties

    /** Recognized features. */
    private static final String[] RECOGNIZED_FEATURES = {
        NAMESPACES, 
        VALIDATION, 
        NOTIFY_BUILTIN_REFS,
        NOTIFY_CHAR_REFS, 
    };

    /** Feature defaults. */
    private static final Boolean[] FEATURE_DEFAULTS = {
        null,
        null,
        Boolean.FALSE,
        Boolean.FALSE,
    };

    /** Recognized properties. */
    private static final String[] RECOGNIZED_PROPERTIES = {
        SYMBOL_TABLE,
        ERROR_REPORTER,
        ENTITY_MANAGER,
        ENTITY_RESOLVER,
    };

    /** Property defaults. */
    private static final Object[] PROPERTY_DEFAULTS = {
        null,
        null,
        null,
        null,
    };

    // debugging

    /** Debug scanner state. */
    private static final boolean DEBUG_SCANNER_STATE = false;

    /** Debug dispatcher. */
    private static final boolean DEBUG_DISPATCHER = false;

    /** Debug content dispatcher scanning. */
    protected static final boolean DEBUG_CONTENT_SCANNING = false;

    //
    // Data
    //

    // protected data

    /** Document handler. */
    protected XMLDocumentHandler fDocumentHandler;

    /** Entity stack. */
    protected int[] fEntityStack = new int[4];

    /** Markup depth. */
    protected int fMarkupDepth;

    /** Scanner state. */
    protected int fScannerState;

    /** SubScanner state: inside scanContent method. */
    protected boolean fInScanContent = false;

    /** has external dtd */
    protected boolean fHasExternalDTD;
    
    /** Standalone. */
    protected boolean fStandalone;
    
    /** True if [Entity Declared] is a VC; false if it is a WFC. */
    protected boolean fIsEntityDeclaredVC;
    
    /** External subset resolver. **/
    protected org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.ExternalSubsetResolver fExternalSubsetResolver;

    // element information

    /** Current element. */
    protected QName fCurrentElement;

    /** Element stack. */
    protected final ElementStack fElementStack = new ElementStack();

    // other info

    /** Document system identifier. 
     * REVISIT:  So what's this used for?  - NG
    * protected String fDocumentSystemId;
     ******/

    // features

    /** Notify built-in references. */
    protected boolean fNotifyBuiltInRefs = false;

    // dispatchers

    /** Active dispatcher. */
    protected Dispatcher fDispatcher;

    /** Content dispatcher. */
    protected final Dispatcher fContentDispatcher = createContentDispatcher();

    // temporary variables

    /** Element QName. */
    protected final QName fElementQName = new QName();

    /** Attribute QName. */
    protected final QName fAttributeQName = new QName();

    /** Element attributes. */
    protected final XMLAttributesImpl fAttributes = new XMLAttributesImpl();

    /** String. */
    protected final XMLString fTempString = new XMLString();

    /** String. */
    protected final XMLString fTempString2 = new XMLString();

    /** Array of 3 strings. */
    private final String[] fStrings = new String[3];

    /** String buffer. */
    private final XMLStringBuffer fStringBuffer = new XMLStringBuffer();

    /** String buffer. */
    private final XMLStringBuffer fStringBuffer2 = new XMLStringBuffer();

    /** Another QName. */
    private final QName fQName = new QName();

    /** Single character array. */
    private final char[] fSingleChar = new char[1];
    
    /** 
     * Saw spaces after element name or between attributes.
     * 
     * This is reserved for the case where scanning of a start element spans
     * several methods, as is the case when scanning the start of a root element 
     * where a DTD external subset may be read after scanning the element name.
     */
    private boolean fSawSpace;
    
    /** Reusable Augmentations. */
    private Augmentations fTempAugmentations = null;

    //
    // Constructors
    //

    /** Default constructor. */
    public XMLDocumentFragmentScannerImpl() {} // <init>()

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
        fEntityManager.startEntity("$fragment$", inputSource, false, true);
        //fDocumentSystemId = fEntityManager.expandSystemId(inputSource.getSystemId());
    } // setInputSource(XMLInputSource)

    /** 
     * Scans a document.
     *
     * @param complete True if the scanner should scan the document
     *                 completely, pushing all events to the registered
     *                 document handler. A value of false indicates that
     *                 that the scanner should only scan the next portion
     *                 of the document and return. A scanner instance is
     *                 permitted to completely scan a document if it does
     *                 not support this "pull" scanning model.
     *
     * @return True if there is more to scan, false otherwise.
     */
    public boolean scanDocument(boolean complete) 
        throws IOException, XNIException {
        
        // reset entity scanner
        fEntityScanner = fEntityManager.getEntityScanner();
        
        // keep dispatching "events"
        fEntityManager.setEntityHandler(this);
        do {
            if (!fDispatcher.dispatch(complete)) {
                return false;
            }
        } while (complete);

        // return success
        return true;

    } // scanDocument(boolean):boolean

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
        //fDocumentSystemId = null;

        // sax features
        fAttributes.setNamespaces(fNamespaces);

        // initialize vars
        fMarkupDepth = 0;
        fCurrentElement = null;
        fElementStack.clear();
        fHasExternalDTD = false;
        fStandalone = false;
        fIsEntityDeclaredVC = false;
        fInScanContent = false;

		// setup dispatcher
		setScannerState(SCANNER_STATE_CONTENT);
		setDispatcher(fContentDispatcher);
        

        if (fParserSettings) {
            // parser settings have changed. reset them.
        	
            // xerces features
            try {
                fNotifyBuiltInRefs = componentManager.getFeature(NOTIFY_BUILTIN_REFS);
            } catch (XMLConfigurationException e) {
                fNotifyBuiltInRefs = false;
            }
            
            // xerces properties
            try {
                Object resolver = componentManager.getProperty(ENTITY_RESOLVER);
                fExternalSubsetResolver = (resolver instanceof org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.ExternalSubsetResolver) ?
                    (org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.ExternalSubsetResolver) resolver : null;
            }
            catch (XMLConfigurationException e) {
                fExternalSubsetResolver = null;
            }
        }

    } // reset(XMLComponentManager)

    /**
     * Returns a list of feature identifiers that are recognized by
     * this component. This method may return null if no features
     * are recognized by this component.
     */
    public String[] getRecognizedFeatures() {
        return (String[])(RECOGNIZED_FEATURES.clone());
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
            if (suffixLength == org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.NOTIFY_BUILTIN_REFS_FEATURE.length() && 
                featureId.endsWith(org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.NOTIFY_BUILTIN_REFS_FEATURE)) {
                fNotifyBuiltInRefs = state;
            }
        }

    } // setFeature(String,boolean)

    /**
     * Returns a list of property identifiers that are recognized by
     * this component. This method may return null if no properties
     * are recognized by this component.
     */
    public String[] getRecognizedProperties() {
        return (String[])(RECOGNIZED_PROPERTIES.clone());
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
            if (suffixLength == org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.ENTITY_MANAGER_PROPERTY.length() && 
                propertyId.endsWith(org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.ENTITY_MANAGER_PROPERTY)) {
                fEntityManager = (org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager)value;
                return;
            }
            if (suffixLength == org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.ENTITY_RESOLVER_PROPERTY.length() && 
                propertyId.endsWith(org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants.ENTITY_RESOLVER_PROPERTY)) {
                fExternalSubsetResolver = (value instanceof org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.ExternalSubsetResolver) ?
                    (ExternalSubsetResolver) value : null;
                return;
            }
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
        return null;
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
        return null;
    } // getPropertyDefault(String):Object

    //
    // XMLDocumentSource methods
    //

    /**
     * setDocumentHandler
     * 
     * @param documentHandler 
     */
    public void setDocumentHandler(XMLDocumentHandler documentHandler) {
        fDocumentHandler = documentHandler;
    } // setDocumentHandler(XMLDocumentHandler)


    /** Returns the document handler */
    public XMLDocumentHandler getDocumentHandler(){
        return fDocumentHandler;
    }

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
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void startEntity(String name, 
                            XMLResourceIdentifier identifier,
                            String encoding, Augmentations augs) throws XNIException {

        // keep track of this entity before fEntityDepth is increased
        if (fEntityDepth == fEntityStack.length) {
            int[] entityarray = new int[fEntityStack.length * 2];
            System.arraycopy(fEntityStack, 0, entityarray, 0, fEntityStack.length);
            fEntityStack = entityarray;
        }
        fEntityStack[fEntityDepth] = fMarkupDepth;

        super.startEntity(name, identifier, encoding, augs);

        // WFC:  entity declared in external subset in standalone doc
        if(fStandalone && fEntityManager.isEntityDeclInExternalSubset(name)) {
            reportFatalError("MSG_REFERENCE_TO_EXTERNALLY_DECLARED_ENTITY_WHEN_STANDALONE",
                new Object[]{name});
        }

        // call handler
        if (fDocumentHandler != null && !fScanningAttribute) {
            if (!name.equals("[xml]")) {
                fDocumentHandler.startGeneralEntity(name, identifier, encoding, augs);
            }
        }

    } // startEntity(String,XMLResourceIdentifier,String)

    /**
     * This method notifies the end of an entity. The DTD has the pseudo-name
     * of "[dtd]" parameter entity names start with '%'; and general entities 
     * are just specified by their name.
     * 
     * @param name The name of the entity.
     * @param augs Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void endEntity(String name, Augmentations augs) throws XNIException {

        // flush possible pending output buffer - see scanContent
        if (fInScanContent && fStringBuffer.length != 0
            && fDocumentHandler != null) {
            fDocumentHandler.characters(fStringBuffer, null);
            fStringBuffer.length = 0; // make sure we know it's been flushed
        }

        super.endEntity(name, augs);

        // make sure markup is properly balanced
        if (fMarkupDepth != fEntityStack[fEntityDepth]) {
            reportFatalError("MarkupEntityMismatch", null);
        }

        // call handler
        if (fDocumentHandler != null && !fScanningAttribute) {
            if (!name.equals("[xml]")) {
                fDocumentHandler.endGeneralEntity(name, augs);
            }
        }
        
    } // endEntity(String)

    //
    // Protected methods
    //

    // dispatcher factory methods

    /** Creates a content dispatcher. */
    protected Dispatcher createContentDispatcher() {
        return new FragmentContentDispatcher();
    } // createContentDispatcher():Dispatcher

    // scanning methods

    /**
     * Scans an XML or text declaration.
     * <p>
     * <pre>
     * [23] XMLDecl ::= '&lt;?xml' VersionInfo EncodingDecl? SDDecl? S? '?>'
     * [24] VersionInfo ::= S 'version' Eq (' VersionNum ' | " VersionNum ")
     * [80] EncodingDecl ::= S 'encoding' Eq ('"' EncName '"' |  "'" EncName "'" )
     * [81] EncName ::= [A-Za-z] ([A-Za-z0-9._] | '-')*
     * [32] SDDecl ::= S 'standalone' Eq (("'" ('yes' | 'no') "'")
     *                 | ('"' ('yes' | 'no') '"'))
     *
     * [77] TextDecl ::= '&lt;?xml' VersionInfo? EncodingDecl S? '?>'
     * </pre>
     *
     * @param scanningTextDecl True if a text declaration is to
     *                         be scanned instead of an XML
     *                         declaration.
     */
    protected void scanXMLDeclOrTextDecl(boolean scanningTextDecl) 
        throws IOException, XNIException {

        // scan decl
        super.scanXMLDeclOrTextDecl(scanningTextDecl, fStrings);
        fMarkupDepth--;

        // pseudo-attribute values
        String version = fStrings[0];
        String encoding = fStrings[1];
        String standalone = fStrings[2];

        // set standalone
        fStandalone = standalone != null && standalone.equals("yes");
        fEntityManager.setStandalone(fStandalone);
        
        // set version on reader
        fEntityScanner.setXMLVersion(version);

        // call handler
        if (fDocumentHandler != null) {
            if (scanningTextDecl) {
                fDocumentHandler.textDecl(version, encoding, null);
            }
            else {
                fDocumentHandler.xmlDecl(version, encoding, standalone, null);
            }
        }

        // set encoding on reader
        if (encoding != null && !fEntityScanner.fCurrentEntity.isEncodingExternallySpecified()) {
            fEntityScanner.setEncoding(encoding);
        }

    } // scanXMLDeclOrTextDecl(boolean)

    /**
     * Scans a processing data. This is needed to handle the situation
     * where a document starts with a processing instruction whose 
     * target name <em>starts with</em> "xml". (e.g. xmlfoo)
     *
     * @param target The PI target
     * @param data The string to fill in with the data
     */
    protected void scanPIData(String target, XMLString data) 
        throws IOException, XNIException {

        super.scanPIData(target, data);
        fMarkupDepth--;

        // call handler
        if (fDocumentHandler != null) {
            fDocumentHandler.processingInstruction(target, data, null);
        }

    } // scanPIData(String)

    /**
     * Scans a comment.
     * <p>
     * <pre>
     * [15] Comment ::= '&lt!--' ((Char - '-') | ('-' (Char - '-')))* '-->'
     * </pre>
     * <p>
     * <strong>Note:</strong> Called after scanning past '&lt;!--'
     */
    protected void scanComment() throws IOException, XNIException {

        scanComment(fStringBuffer);
        fMarkupDepth--;

        // call handler
        if (fDocumentHandler != null) {
            fDocumentHandler.comment(fStringBuffer, null);
        }

    } // scanComment()
    
    /** 
     * Scans a start element. This method will handle the binding of
     * namespace information and notifying the handler of the start
     * of the element.
     * <p>
     * <pre>
     * [44] EmptyElemTag ::= '&lt;' Name (S Attribute)* S? '/>'
     * [40] STag ::= '&lt;' Name (S Attribute)* S? '>'
     * </pre> 
     * <p>
     * <strong>Note:</strong> This method assumes that the leading
     * '&lt;' character has been consumed.
     * <p>
     * <strong>Note:</strong> This method uses the fElementQName and
     * fAttributes variables. The contents of these variables will be
     * destroyed. The caller should copy important information out of
     * these variables before calling this method.
     *
     * @return True if element is empty. (i.e. It matches
     *          production [44].
     */
    protected boolean scanStartElement() 
        throws IOException, XNIException {
        if (DEBUG_CONTENT_SCANNING) System.out.println(">>> scanStartElement()");

        // name
        if (fNamespaces) {
            fEntityScanner.scanQName(fElementQName);
        }
        else {
            String name = fEntityScanner.scanName();
            fElementQName.setValues(null, name, name, null);
        }
        String rawname = fElementQName.rawname;

        // push element stack
        fCurrentElement = fElementStack.pushElement(fElementQName);

        // attributes
        boolean empty = false;
        fAttributes.removeAllAttributes();
        do {
            // spaces
            boolean sawSpace = fEntityScanner.skipSpaces();

            // end tag?
            int c = fEntityScanner.peekChar();
            if (c == '>') {
                fEntityScanner.scanChar();
                break;
            }
            else if (c == '/') {
                fEntityScanner.scanChar();
                if (!fEntityScanner.skipChar('>')) {
                    reportFatalError("ElementUnterminated",
                                     new Object[]{rawname});
                }
                empty = true;
                break;
            }
            else if (!isValidNameStartChar(c) || !sawSpace) {
                // Second chance. Check if this character is a high
                // surrogate of a valid name start character.
                if (!isValidNameStartHighSurrogate(c) || !sawSpace) {
                    reportFatalError("ElementUnterminated",
                                     new Object[] { rawname });
                }
            }

            // attributes
            scanAttribute(fAttributes);

        } while (true);

        // call handler
        if (fDocumentHandler != null) {
            if (empty) {

                //decrease the markup depth..
                fMarkupDepth--;
                // check that this element was opened in the same entity
                if (fMarkupDepth < fEntityStack[fEntityDepth - 1]) {
                    reportFatalError("ElementEntityMismatch",
                                     new Object[]{fCurrentElement.rawname});
                }

                fDocumentHandler.emptyElement(fElementQName, fAttributes, null);

                //pop the element off the stack..
                fElementStack.popElement(fElementQName);
            }
            else {
                fDocumentHandler.startElement(fElementQName, fAttributes, null);
            }
        }

        if (DEBUG_CONTENT_SCANNING) System.out.println("<<< scanStartElement(): "+empty);
        return empty;

    } // scanStartElement():boolean
    
    /**
     * Scans the name of an element in a start or empty tag. 
     * 
     * @see #scanStartElement()
     */
    protected void scanStartElementName ()
        throws IOException, XNIException {
        // name
        if (fNamespaces) {
            fEntityScanner.scanQName(fElementQName);
        }
        else {
            String name = fEntityScanner.scanName();
            fElementQName.setValues(null, name, name, null);
        }
        // Must skip spaces here because the DTD scanner
        // would consume them at the end of the external subset.
        fSawSpace = fEntityScanner.skipSpaces();
    } // scanStartElementName()

    /**
     * Scans the remainder of a start or empty tag after the element name.
     * 
     * @see #scanStartElement
     * @return True if element is empty.
     */
    protected boolean scanStartElementAfterName()
        throws IOException, XNIException {
        String rawname = fElementQName.rawname;

        // push element stack
        fCurrentElement = fElementStack.pushElement(fElementQName);

        // attributes
        boolean empty = false;
        fAttributes.removeAllAttributes();
        do {
        	
            // end tag?
            int c = fEntityScanner.peekChar();
            if (c == '>') {
                fEntityScanner.scanChar();
                break;
            }
            else if (c == '/') {
                fEntityScanner.scanChar();
                if (!fEntityScanner.skipChar('>')) {
                    reportFatalError("ElementUnterminated",
                                     new Object[]{rawname});
                }
                empty = true;
                break;
            }
            else if (!isValidNameStartChar(c) || !fSawSpace) {
                // Second chance. Check if this character is a high
                // surrogate of a valid name start character.
                if (!isValidNameStartHighSurrogate(c) || !fSawSpace) {
                    reportFatalError("ElementUnterminated",
                                     new Object[] { rawname });
                }
            }

            // attributes
            scanAttribute(fAttributes);
            
            // spaces
            fSawSpace = fEntityScanner.skipSpaces();

        } while (true);

        // call handler
        if (fDocumentHandler != null) {
            if (empty) {

                //decrease the markup depth..
                fMarkupDepth--;
                // check that this element was opened in the same entity
                if (fMarkupDepth < fEntityStack[fEntityDepth - 1]) {
                    reportFatalError("ElementEntityMismatch",
                                     new Object[]{fCurrentElement.rawname});
                }

                fDocumentHandler.emptyElement(fElementQName, fAttributes, null);

                //pop the element off the stack..
                fElementStack.popElement(fElementQName);
            }
            else {
                fDocumentHandler.startElement(fElementQName, fAttributes, null);
            }
        }

        if (DEBUG_CONTENT_SCANNING) System.out.println("<<< scanStartElementAfterName(): "+empty);
        return empty;
    } // scanStartElementAfterName()

    /** 
     * Scans an attribute.
     * <p>
     * <pre>
     * [41] Attribute ::= Name Eq AttValue
     * </pre> 
     * <p>
     * <strong>Note:</strong> This method assumes that the next 
     * character on the stream is the first character of the attribute
     * name.
     * <p>
     * <strong>Note:</strong> This method uses the fAttributeQName and
     * fQName variables. The contents of these variables will be
     * destroyed.
     *
     * @param attributes The attributes list for the scanned attribute.
     */
    protected void scanAttribute(XMLAttributes attributes) 
        throws IOException, XNIException {
        if (DEBUG_CONTENT_SCANNING) System.out.println(">>> scanAttribute()");

        // name
        if (fNamespaces) {
            fEntityScanner.scanQName(fAttributeQName);
        }
        else {
            String name = fEntityScanner.scanName();
            fAttributeQName.setValues(null, name, name, null);
        }

        // equals
        fEntityScanner.skipSpaces();
        if (!fEntityScanner.skipChar('=')) {
            reportFatalError("EqRequiredInAttribute",
                             new Object[]{fCurrentElement.rawname,fAttributeQName.rawname});
        }
        fEntityScanner.skipSpaces();

        // content
        int oldLen = attributes.getLength();
        int attrIndex = attributes.addAttribute(fAttributeQName, XMLSymbols.fCDATASymbol, null);

        // WFC: Unique Att Spec
        if (oldLen == attributes.getLength()) {
            reportFatalError("AttributeNotUnique",
                             new Object[]{fCurrentElement.rawname,
                                          fAttributeQName.rawname});
        }      
        
        // Scan attribute value and return true if the un-normalized and normalized value are the same
        boolean isSameNormalizedAttr =  scanAttributeValue(fTempString, fTempString2,
                fAttributeQName.rawname, fIsEntityDeclaredVC, fCurrentElement.rawname);
        
        attributes.setValue(attrIndex, fTempString.toString());
        // If the non-normalized and normalized value are the same, avoid creating a new string.
        if (!isSameNormalizedAttr) {
            attributes.setNonNormalizedValue(attrIndex, fTempString2.toString());
        }
        attributes.setSpecified(attrIndex, true);

        if (DEBUG_CONTENT_SCANNING) System.out.println("<<< scanAttribute()");
    } // scanAttribute(XMLAttributes)

    /**
     * Scans element content.
     *
     * @return Returns the next character on the stream.
     */
    protected int scanContent() throws IOException, XNIException {

        XMLString content = fTempString;
        int c = fEntityScanner.scanContent(content);
        if (c == '\r') {
            // happens when there is the character reference &#13;
            fEntityScanner.scanChar();
            fStringBuffer.clear();
            fStringBuffer.append(fTempString);
            fStringBuffer.append((char)c);
            content = fStringBuffer;
            c = -1;
        }
        if (fDocumentHandler != null && content.length > 0) {
            fDocumentHandler.characters(content, null);
        }

        if (c == ']' && fTempString.length == 0) {
            fStringBuffer.clear();
            fStringBuffer.append((char)fEntityScanner.scanChar());
            // remember where we are in case we get an endEntity before we
            // could flush the buffer out - this happens when we're parsing an
            // entity which ends with a ]
            fInScanContent = true;
            //
            // We work on a single character basis to handle cases such as:
            // ']]]>' which we might otherwise miss.
            //
            if (fEntityScanner.skipChar(']')) {
                fStringBuffer.append(']');
                while (fEntityScanner.skipChar(']')) {
                    fStringBuffer.append(']');
                }
                if (fEntityScanner.skipChar('>')) {
                    reportFatalError("CDEndInContent", null);
                }
            }
            if (fDocumentHandler != null && fStringBuffer.length != 0) {
                fDocumentHandler.characters(fStringBuffer, null);
            }
            fInScanContent = false;
            c = -1;
        }
        return c;

    } // scanContent():int


    /** 
     * Scans a CDATA section. 
     * <p>
     * <strong>Note:</strong> This method uses the fTempString and
     * fStringBuffer variables.
     *
     * @param complete True if the CDATA section is to be scanned
     *                 completely.
     *
     * @return True if CDATA is completely scanned.
     */
    protected boolean scanCDATASection(boolean complete) 
        throws IOException, XNIException {
        
        // call handler
        if (fDocumentHandler != null) {
            fDocumentHandler.startCDATA(null);
        }

        while (true) {
            fStringBuffer.clear();
            if (!fEntityScanner.scanData("]]", fStringBuffer)) {
                if (fDocumentHandler != null && fStringBuffer.length > 0) {
                    fDocumentHandler.characters(fStringBuffer, null);
                }
                int brackets = 0;
                while (fEntityScanner.skipChar(']')) {
                    brackets++;
                }
                if (fDocumentHandler != null && brackets > 0) {
                    fStringBuffer.clear();
                    if (brackets > org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.DEFAULT_BUFFER_SIZE) {
                        // Handle large sequences of ']'
                        int chunks = brackets / org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.DEFAULT_BUFFER_SIZE;
                        int remainder = brackets % org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager.DEFAULT_BUFFER_SIZE;
                        for (int i = 0; i < XMLEntityManager.DEFAULT_BUFFER_SIZE; i++) {
                            fStringBuffer.append(']');
                        }
                        for (int i = 0; i < chunks; i++) {
                            fDocumentHandler.characters(fStringBuffer, null);
                        }
                        if (remainder != 0) {
                            fStringBuffer.length = remainder;
                            fDocumentHandler.characters(fStringBuffer, null);
                        }
                    }
                    else {
                    	for (int i = 0; i < brackets; i++) {
                    	    fStringBuffer.append(']');
                    	}
                       fDocumentHandler.characters(fStringBuffer, null);
                    }
                }
                if (fEntityScanner.skipChar('>')) {
                    break;
                }
                if (fDocumentHandler != null) {
                    fStringBuffer.clear();
                    fStringBuffer.append("]]");
                    fDocumentHandler.characters(fStringBuffer, null);
                }
            }
            else {
                if (fDocumentHandler != null) {
                    fDocumentHandler.characters(fStringBuffer, null);
                }
                int c = fEntityScanner.peekChar();
                if (c != -1 && isInvalidLiteral(c)) {
                    if (XMLChar.isHighSurrogate(c)) {
                        fStringBuffer.clear();
                        scanSurrogates(fStringBuffer);
                        if (fDocumentHandler != null) {
                            fDocumentHandler.characters(fStringBuffer, null);
                        }
                    }
                    else {
                        reportFatalError("InvalidCharInCDSect",
                                        new Object[]{Integer.toString(c,16)});
                        fEntityScanner.scanChar();
                    }
                }
            }
        }
        fMarkupDepth--;

        // call handler
        if (fDocumentHandler != null) {
            fDocumentHandler.endCDATA(null);
        }

        return true;

    } // scanCDATASection(boolean):boolean

    /**
     * Scans an end element.
     * <p>
     * <pre>
     * [42] ETag ::= '&lt;/' Name S? '>'
     * </pre>
     * <p>
     * <strong>Note:</strong> This method uses the fElementQName variable.
     * The contents of this variable will be destroyed. The caller should
     * copy the needed information out of this variable before calling
     * this method.
     *
     * @return The element depth.
     */
    protected int scanEndElement() throws IOException, XNIException {
        if (DEBUG_CONTENT_SCANNING) System.out.println(">>> scanEndElement()");

        fElementStack.popElement(fElementQName) ;

        // Take advantage of the fact that next string _should_ be "fElementQName.rawName",
        //In scanners most of the time is consumed on checks done for XML characters, we can
        // optimize on it and avoid the checks done for endElement,
        //we will also avoid symbol table lookup - neeraj.bajaj@sun.com

        // this should work both for namespace processing true or false...

        //REVISIT: if the string is not the same as expected.. we need to do better error handling..
        //We can skip this for now... In any case if the string doesn't match -- document is not well formed.
        if (!fEntityScanner.skipString(fElementQName.rawname)) {
            reportFatalError("ETagRequired", new Object[]{fElementQName.rawname});
        }

        // end
        fEntityScanner.skipSpaces();
        if (!fEntityScanner.skipChar('>')) {
            reportFatalError("ETagUnterminated",
                             new Object[]{fElementQName.rawname});
        }
        fMarkupDepth--;

        //we have increased the depth for two markup "<" characters
        fMarkupDepth--;
      
        // check that this element was opened in the same entity
        if (fMarkupDepth < fEntityStack[fEntityDepth - 1]) {
            reportFatalError("ElementEntityMismatch",
                             new Object[]{fCurrentElement.rawname});
        }

        // call handler
        if (fDocumentHandler != null ) {
            fDocumentHandler.endElement(fElementQName, null);
        }

        return fMarkupDepth;
 
    } // scanEndElement():int

    /**
     * Scans a character reference.
     * <p>
     * <pre>
     * [66] CharRef ::= '&#' [0-9]+ ';' | '&#x' [0-9a-fA-F]+ ';'
     * </pre>
     */
    protected void scanCharReference() 
        throws IOException, XNIException {

        fStringBuffer2.clear();
        int ch = scanCharReferenceValue(fStringBuffer2, null);
        fMarkupDepth--;
        if (ch != -1) {
            // call handler
            if (fDocumentHandler != null) {
                if (fNotifyCharRefs) {
                    fDocumentHandler.startGeneralEntity(fCharRefLiteral, null, null, null);
                }
                Augmentations augs = null;
                if (fValidation && ch <= 0x20) {
                    if (fTempAugmentations != null) {
                        fTempAugmentations.removeAllItems();
                    }
                    else {
                        fTempAugmentations = new AugmentationsImpl();
                    }
                    augs = fTempAugmentations;
                    augs.putItem(Constants.CHAR_REF_PROBABLE_WS, Boolean.TRUE);
                }
                fDocumentHandler.characters(fStringBuffer2, augs);
                if (fNotifyCharRefs) {
                    fDocumentHandler.endGeneralEntity(fCharRefLiteral, null);
                }
            }
        }

    } // scanCharReference()

    /**
     * Scans an entity reference.
     *
     * @throws IOException  Thrown if i/o error occurs.
     * @throws XNIException Thrown if handler throws exception upon
     *                      notification.
     */
    protected void scanEntityReference() throws IOException, XNIException {

        // name
        String name = fEntityScanner.scanName();
        if (name == null) {
            reportFatalError("NameRequiredInReference", null);
            return;
        }

        // end
        if (!fEntityScanner.skipChar(';')) {
            reportFatalError("SemicolonRequiredInReference", new Object []{name});
        }
        fMarkupDepth--;

        // handle built-in entities
        if (name == fAmpSymbol) {
            handleCharacter('&', fAmpSymbol);
        }
        else if (name == fLtSymbol) {
            handleCharacter('<', fLtSymbol);
        }
        else if (name == fGtSymbol) {
            handleCharacter('>', fGtSymbol);
        }
        else if (name == fQuotSymbol) {
            handleCharacter('"', fQuotSymbol);
        }
        else if (name == fAposSymbol) {
            handleCharacter('\'', fAposSymbol);
        }
        // start general entity
        else if (fEntityManager.isUnparsedEntity(name)) {
            reportFatalError("ReferenceToUnparsedEntity", new Object[]{name});
        }
        else {
            if (!fEntityManager.isDeclaredEntity(name)) {
                if (fIsEntityDeclaredVC) {
                    if (fValidation)
                        fErrorReporter.reportError( XMLMessageFormatter.XML_DOMAIN,"EntityNotDeclared", 
                                                    new Object[]{name}, org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_ERROR);
                }
                else {
                    reportFatalError("EntityNotDeclared", new Object[]{name});
                }
            }
            fEntityManager.startEntity(name, false);
        }

    } // scanEntityReference()

    // utility methods

    /** 
     * Calls document handler with a single character resulting from
     * built-in entity resolution. 
     *
     * @param c
     * @param entity built-in name
     */
    private void handleCharacter(char c, String entity) throws XNIException {
        if (fDocumentHandler != null) {
            if (fNotifyBuiltInRefs) {
                fDocumentHandler.startGeneralEntity(entity, null, null, null);
            }
            
            fSingleChar[0] = c;
            fTempString.setValues(fSingleChar, 0, 1);
            fDocumentHandler.characters(fTempString, null);
            
            if (fNotifyBuiltInRefs) {
                fDocumentHandler.endGeneralEntity(entity, null);
            }
        }
    } // handleCharacter(char)

    /** 
     * Handles the end element. This method will make sure that
     * the end element name matches the current element and notify
     * the handler about the end of the element and the end of any
     * relevent prefix mappings.
     * <p>
     * <strong>Note:</strong> This method uses the fQName variable.
     * The contents of this variable will be destroyed.
     *
     * @param element The element.
     *
     * @return The element depth.
     *
     * @throws XNIException Thrown if the handler throws a SAX exception
     *                      upon notification.
     *
     */
    // REVISIT: need to remove this method. It's not called anymore, because
    // the handling is done when the end tag is scanned. - SG
    protected int handleEndElement(QName element, boolean isEmpty) 
        throws XNIException {

        fMarkupDepth--;
        // check that this element was opened in the same entity
        if (fMarkupDepth < fEntityStack[fEntityDepth - 1]) {
            reportFatalError("ElementEntityMismatch",
                             new Object[]{fCurrentElement.rawname});
        }
        // make sure the elements match
        QName startElement = fQName;
        fElementStack.popElement(startElement);
        if (element.rawname != startElement.rawname) {
            reportFatalError("ETagRequired",
                             new Object[]{startElement.rawname});
        }

        // bind namespaces
        if (fNamespaces) {
            element.uri = startElement.uri;
        }
        
        // call handler
        if (fDocumentHandler != null && !isEmpty) {
            fDocumentHandler.endElement(element, null);
        }

        return fMarkupDepth;

    } // callEndElement(QName,boolean):int

    // helper methods

    /**
     * Sets the scanner state.
     *
     * @param state The new scanner state.
     */
    protected final void setScannerState(int state) {

        fScannerState = state;
        if (DEBUG_SCANNER_STATE) {
            System.out.print("### setScannerState: ");
            System.out.print(getScannerStateName(state));
            System.out.println();
        }

    } // setScannerState(int)

    /**
     * Sets the dispatcher.
     *
     * @param dispatcher The new dispatcher.
     */
    protected final void setDispatcher(Dispatcher dispatcher) {
        fDispatcher = dispatcher;
        if (DEBUG_DISPATCHER) {
            System.out.print("%%% setDispatcher: ");
            System.out.print(getDispatcherName(dispatcher));
            System.out.println();
        }
    }

    //
    // Private methods
    //

    /** Returns the scanner state name. */
    protected String getScannerStateName(int state) {

        switch (state) {
            case SCANNER_STATE_DOCTYPE: return "SCANNER_STATE_DOCTYPE";
            case SCANNER_STATE_ROOT_ELEMENT: return "SCANNER_STATE_ROOT_ELEMENT";
            case SCANNER_STATE_START_OF_MARKUP: return "SCANNER_STATE_START_OF_MARKUP";
            case SCANNER_STATE_COMMENT: return "SCANNER_STATE_COMMENT";
            case SCANNER_STATE_PI: return "SCANNER_STATE_PI";
            case SCANNER_STATE_CONTENT: return "SCANNER_STATE_CONTENT";
            case SCANNER_STATE_REFERENCE: return "SCANNER_STATE_REFERENCE";
            case SCANNER_STATE_END_OF_INPUT: return "SCANNER_STATE_END_OF_INPUT";
            case SCANNER_STATE_TERMINATED: return "SCANNER_STATE_TERMINATED";
            case SCANNER_STATE_CDATA: return "SCANNER_STATE_CDATA";
            case SCANNER_STATE_TEXT_DECL: return "SCANNER_STATE_TEXT_DECL";
        }

        return "??? ("+state+')';

    } // getScannerStateName(int):String

    /** Returns the dispatcher name. */
    public String getDispatcherName(Dispatcher dispatcher) {

        if (DEBUG_DISPATCHER) {
            if (dispatcher != null) {
                String name = dispatcher.getClass().getName();
                int index = name.lastIndexOf('.');
                if (index != -1) {
                    name = name.substring(index + 1);
                    index = name.lastIndexOf('$');
                    if (index != -1) {
                        name = name.substring(index + 1);
                    }
                }
                return name;
            }
        }
        return "null";

    } // getDispatcherName():String

    //
    // Classes
    //

    /**
     * Element stack. This stack operates without synchronization, error
     * checking, and it re-uses objects instead of throwing popped items
     * away.
     *
     * @author Andy Clark, IBM
     */
    protected static class ElementStack {

        //
        // Data
        //

        /** The stack data. */
        protected QName[] fElements;

        /** The size of the stack. */
        protected int fSize;

        //
        // Constructors
        //

        /** Default constructor. */
        public ElementStack() {
            fElements = new QName[10];
            for (int i = 0; i < fElements.length; i++) {
                fElements[i] = new QName();
            }
        } // <init>()

        //
        // Public methods
        //

        /** 
         * Pushes an element on the stack. 
         * <p>
         * <strong>Note:</strong> The QName values are copied into the
         * stack. In other words, the caller does <em>not</em> orphan
         * the element to the stack. Also, the QName object returned
         * is <em>not</em> orphaned to the caller. It should be 
         * considered read-only.
         *
         * @param element The element to push onto the stack.
         *
         * @return Returns the actual QName object that stores the
         */
        public QName pushElement(QName element) {
            if (fSize == fElements.length) {
                QName[] array = new QName[fElements.length * 2];
                System.arraycopy(fElements, 0, array, 0, fSize);
                fElements = array;
                for (int i = fSize; i < fElements.length; i++) {
                    fElements[i] = new QName();
                }
            }
            fElements[fSize].setValues(element);
            return fElements[fSize++];
        } // pushElement(QName):QName

        /** 
         * Pops an element off of the stack by setting the values of
         * the specified QName.
         * <p>
         * <strong>Note:</strong> The object returned is <em>not</em>
         * orphaned to the caller. Therefore, the caller should consider
         * the object to be read-only.
         */
        public void popElement(QName element) {
            element.setValues(fElements[--fSize]);
        } // popElement(QName)

        /** Clears the stack without throwing away existing QName objects. */
        public void clear() {
            fSize = 0;
        } // clear()

    } // class ElementStack

    /** 
     * This interface defines an XML "event" dispatching model. Classes
     * that implement this interface are responsible for scanning parts
     * of the XML document and dispatching callbacks.
     * 
     * @xerces.internal
     *
     * @author Glenn Marcy, IBM
     */
    protected interface Dispatcher {

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
            throws IOException, XNIException;

    } // interface Dispatcher

    /**
     * Dispatcher to handle content scanning.
     *
     * @author Andy Clark, IBM
     * @author Eric Ye, IBM
     */
    protected class FragmentContentDispatcher
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
                        case SCANNER_STATE_CONTENT: {
                            if (fEntityScanner.skipChar('<')) {
                                setScannerState(SCANNER_STATE_START_OF_MARKUP);
                                again = true;
                            }
                            else if (fEntityScanner.skipChar('&')) {
                                setScannerState(SCANNER_STATE_REFERENCE);
                                again = true;
                            }
                            else {
                                do {
                                    int c = scanContent();
                                    if (c == '<') {
                                        fEntityScanner.scanChar();
                                        setScannerState(SCANNER_STATE_START_OF_MARKUP);
                                        break;
                                    }
                                    else if (c == '&') {
                                        fEntityScanner.scanChar();
                                        setScannerState(SCANNER_STATE_REFERENCE);
                                        break;
                                    }
                                    else if (c != -1 && isInvalidLiteral(c)) {
                                        if (XMLChar.isHighSurrogate(c)) {
                                            // special case: surrogates
                                            fStringBuffer.clear();
                                            if (scanSurrogates(fStringBuffer)) {
                                                // call handler
                                                if (fDocumentHandler != null) {
                                                    fDocumentHandler.characters(fStringBuffer, null);
                                                }
                                            }
                                        }
                                        else {
                                            reportFatalError("InvalidCharInContent",
                                                             new Object[] {
                                                Integer.toString(c, 16)});
                                            fEntityScanner.scanChar();
                                        }
                                    }
                                } while (complete);
                            }
                            break;
                        }
                        case SCANNER_STATE_START_OF_MARKUP: {
                            fMarkupDepth++;
                            if (fEntityScanner.skipChar('/')) {
                                if (scanEndElement() == 0) {
                                    if (elementDepthIsZeroHook()) {
                                        return true;
                                    }
                                }
                                setScannerState(SCANNER_STATE_CONTENT);
                            }
                            else if (isValidNameStartChar(fEntityScanner.peekChar())) {
                                scanStartElement();
                                setScannerState(SCANNER_STATE_CONTENT);
                            }
                            else if (fEntityScanner.skipChar('!')) {
                                if (fEntityScanner.skipChar('-')) {
                                    if (!fEntityScanner.skipChar('-')) {
                                        reportFatalError("InvalidCommentStart",
                                                         null);
                                    }
                                    setScannerState(SCANNER_STATE_COMMENT);
                                    again = true;
                                }
                                else if (fEntityScanner.skipString("[CDATA[")) {
                                    setScannerState(SCANNER_STATE_CDATA);
                                    again = true;
                                }
                                else if (!scanForDoctypeHook()) {
                                    reportFatalError("MarkupNotRecognizedInContent",
                                                     null);
                                }
                            }
                            else if (fEntityScanner.skipChar('?')) {
                                setScannerState(SCANNER_STATE_PI);
                                again = true;
                            }
                            else if (isValidNameStartHighSurrogate(fEntityScanner.peekChar())) {
                                scanStartElement();
                                setScannerState(SCANNER_STATE_CONTENT);
                            }
                            else {
                                reportFatalError("MarkupNotRecognizedInContent",
                                                 null);
                                setScannerState(SCANNER_STATE_CONTENT);                 
                            }
                            break;
                        }
                        case SCANNER_STATE_COMMENT: {
                            scanComment();
                            setScannerState(SCANNER_STATE_CONTENT);
                            break;  
                        }
                        case SCANNER_STATE_PI: {
                            scanPI();
                            setScannerState(SCANNER_STATE_CONTENT);
                            break;  
                        }
                        case SCANNER_STATE_CDATA: {
                            scanCDATASection(complete);
                            setScannerState(SCANNER_STATE_CONTENT);
                            break;
                        }
                        case SCANNER_STATE_REFERENCE: {
                            fMarkupDepth++;
                            // NOTE: We need to set the state beforehand
                            //       because the XMLEntityHandler#startEntity
                            //       callback could set the state to
                            //       SCANNER_STATE_TEXT_DECL and we don't want
                            //       to override that scanner state.
                            setScannerState(SCANNER_STATE_CONTENT);
                            if (fEntityScanner.skipChar('#')) {
                                scanCharReference();
                            }
                            else {
                                scanEntityReference();
                            }
                            break;
                        }
                        case SCANNER_STATE_TEXT_DECL: {
                            // scan text decl
                            if (fEntityScanner.skipString("<?xml")) {
                                fMarkupDepth++;
                                // NOTE: special case where entity starts with a PI
                                //       whose name starts with "xml" (e.g. "xmlfoo")
                                if (isValidNameChar(fEntityScanner.peekChar())) {
                                    fStringBuffer.clear();
                                    fStringBuffer.append("xml");
                                    if (fNamespaces) {
                                        while (isValidNCName(fEntityScanner.peekChar())) {
                                            fStringBuffer.append((char)fEntityScanner.scanChar());
                                        }
                                    }
                                    else {
                                        while (isValidNameChar(fEntityScanner.peekChar())) {
                                            fStringBuffer.append((char)fEntityScanner.scanChar());
                                        }
                                    }
                                    String target = fSymbolTable.addSymbol(fStringBuffer.ch, fStringBuffer.offset, fStringBuffer.length);
                                    scanPIData(target, fTempString);
                                }
                
                                // standard text declaration
                                else {
                                    scanXMLDeclOrTextDecl(true);
                                }
                            }
                            // now that we've straightened out the readers, we can read in chunks:
                            fEntityManager.fCurrentEntity.mayReadChunks = true;
                            setScannerState(SCANNER_STATE_CONTENT);
                            break;
                        }
                        case SCANNER_STATE_ROOT_ELEMENT: {
                            if (scanRootElementHook()) {
                                return true;
                            }
                            setScannerState(SCANNER_STATE_CONTENT);
                            break;
                        }
                        case SCANNER_STATE_DOCTYPE: {
                            reportFatalError("DoctypeIllegalInContent",
                                             null);
                            setScannerState(SCANNER_STATE_CONTENT);
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
            // premature end of file
            catch (EOFException e) {
                endOfFileHook(e);
                return false;
            }

            return true;

        } // dispatch(boolean):boolean

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
            return false;
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

            // NOTE: An end of file is only only an error if we were
            //       in the middle of scanning some markup. -Ac
            if (fMarkupDepth != 0) {
                reportFatalError("PrematureEOF", null);
            }

        } // endOfFileHook()

    } // class FragmentContentDispatcher

} // class XMLDocumentFragmentScannerImpl
