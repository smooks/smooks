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

import java.io.IOException;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLDocumentScannerImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.dtd.XMLDTDValidatorFilter;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.XMLAttributesImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.XMLSymbols;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.NamespaceContext;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.QName;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLDocumentHandler;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLComponentManager;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLConfigurationException;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLDocumentSource;

/**
 * The scanner acts as the source for the document
 * information which is communicated to the document handler.
 *
 * This class scans an XML document, checks if document has a DTD, and if
 * DTD is not found the scanner will remove the DTD Validator from the pipeline and perform
 * namespace binding.
 *
 * Note: This scanner should only be used when the namespace processing is on!
 *
 * <p>
 * This component requires the following features and properties from the
 * component manager that uses it:
 * <ul>
 *  <li>http://xml.org/sax/features/namespaces {true} -- if the value of this
 *      feature is set to false this scanner must not be used.</li>
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
 * @author Elena Litani, IBM
 *
 * @version $Id$
 */
public class XMLNSDocumentScannerImpl
extends XMLDocumentScannerImpl {

    /** If is true, the dtd validator is no longer in the pipeline
      * and the scanner should bind namespaces */
    protected boolean fBindNamespaces;

    /** If validating parser, make sure we report an error in the
      *   scanner if DTD grammar is missing.*/
    protected boolean fPerformValidation;

    // private data
    //

    /** DTD validator */
    private XMLDTDValidatorFilter fDTDValidator;
    
    /** 
     * Saw spaces after element name or between attributes.
     * 
     * This is reserved for the case where scanning of a start element spans
     * several methods, as is the case when scanning the start of a root element 
     * where a DTD external subset may be read after scanning the element name.
     */
    private boolean fSawSpace;


    /**
     * The scanner is responsible for removing DTD validator
     * from the pipeline if it is not needed.
     *
     * @param dtdValidator The DTDValidator
     */
    public void setDTDValidator(XMLDTDValidatorFilter dtdValidator) {
        fDTDValidator = dtdValidator;
    }

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
        if (DEBUG_CONTENT_SCANNING) System.out.println(">>> scanStartElementNS()");

        // Note: namespace processing is on by default
        fEntityScanner.scanQName(fElementQName);
        // REVISIT - [Q] Why do we need this temp variable? -- mrglavas
        String rawname = fElementQName.rawname;
        if (fBindNamespaces) {
            fNamespaceContext.pushContext();
            if (fScannerState == SCANNER_STATE_ROOT_ELEMENT) {
                if (fPerformValidation) {
                    fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                               "MSG_GRAMMAR_NOT_FOUND",
                                               new Object[]{ rawname},
                                               org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_ERROR);

                    if (fDoctypeName == null || !fDoctypeName.equals(rawname)) {
                        fErrorReporter.reportError( XMLMessageFormatter.XML_DOMAIN,
                                                    "RootElementTypeMustMatchDoctypedecl",
                                                    new Object[]{fDoctypeName, rawname},
                                                    org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_ERROR);
                    }
                }
            }
        }

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
                reportFatalError("ElementUnterminated", new Object[]{rawname});
            }

            // attributes
            scanAttribute(fAttributes);

        } while (true);

        if (fBindNamespaces) {
            // REVISIT: is it required? forbit xmlns prefix for element
            if (fElementQName.prefix == XMLSymbols.PREFIX_XMLNS) {
                fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                           "ElementXMLNSPrefix",
                                           new Object[]{fElementQName.rawname},
                                           org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_FATAL_ERROR);
            }

            // bind the element
            String prefix = fElementQName.prefix != null
                            ? fElementQName.prefix : XMLSymbols.EMPTY_STRING;
            // assign uri to the element
            fElementQName.uri = fNamespaceContext.getURI(prefix);
            // make sure that object in the element stack is updated as well
            fCurrentElement.uri = fElementQName.uri;

            if (fElementQName.prefix == null && fElementQName.uri != null) {
                fElementQName.prefix = XMLSymbols.EMPTY_STRING;
                // making sure that the object in the element stack is updated too.
                fCurrentElement.prefix = XMLSymbols.EMPTY_STRING;
            }
            if (fElementQName.prefix != null && fElementQName.uri == null) {
                fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                           "ElementPrefixUnbound",
                                           new Object[]{fElementQName.prefix, fElementQName.rawname},
                                           org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_FATAL_ERROR);
            }

            // bind attributes (xmlns are already bound bellow)
            int length = fAttributes.getLength();
            // fLength = 0; //initialize structure
            for (int i = 0; i < length; i++) {
                fAttributes.getName(i, fAttributeQName);

                String aprefix = fAttributeQName.prefix != null
                                 ? fAttributeQName.prefix : XMLSymbols.EMPTY_STRING;
                String uri = fNamespaceContext.getURI(aprefix);
                // REVISIT: try removing the first "if" and see if it is faster.
                //
                if (fAttributeQName.uri != null && fAttributeQName.uri == uri) {
                    // checkDuplicates(fAttributeQName, fAttributes);
                    continue;
                }
                if (aprefix != XMLSymbols.EMPTY_STRING) {
                    fAttributeQName.uri = uri;
                    if (uri == null) {
                        fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                                   "AttributePrefixUnbound",
                                                   new Object[]{fElementQName.rawname,fAttributeQName.rawname,aprefix},
                                                   org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_FATAL_ERROR);
                    }
                    fAttributes.setURI(i, uri);
                    // checkDuplicates(fAttributeQName, fAttributes);
                }
            }
            
            if (length > 1) {
                QName name = fAttributes.checkDuplicatesNS();
                if (name != null) {
                    if (name.uri != null) {
                        fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                                   "AttributeNSNotUnique",
                                                   new Object[]{fElementQName.rawname, name.localpart, name.uri},
                                                   org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_FATAL_ERROR);
                    }
                    else {
                        fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                                   "AttributeNotUnique",
                                                   new Object[]{fElementQName.rawname, name.rawname}, 
                                                   org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_FATAL_ERROR);
                    }
                }
            }
        }


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

                if (fBindNamespaces) {
                    fNamespaceContext.popContext();
                }
                //pop the element off the stack..
                fElementStack.popElement(fElementQName);
            } else {
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
        // Note: namespace processing is on by default
        fEntityScanner.scanQName(fElementQName);
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
    	
        // REVISIT - [Q] Why do we need this temp variable? -- mrglavas
        String rawname = fElementQName.rawname;
        if (fBindNamespaces) {
            fNamespaceContext.pushContext();
            if (fScannerState == SCANNER_STATE_ROOT_ELEMENT) {
                if (fPerformValidation) {
                    fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                               "MSG_GRAMMAR_NOT_FOUND",
                                               new Object[]{ rawname},
                                               org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_ERROR);

                    if (fDoctypeName == null || !fDoctypeName.equals(rawname)) {
                        fErrorReporter.reportError( XMLMessageFormatter.XML_DOMAIN,
                                                    "RootElementTypeMustMatchDoctypedecl",
                                                    new Object[]{fDoctypeName, rawname},
                                                    org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_ERROR);
                    }
                }
            }
        }

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
                reportFatalError("ElementUnterminated", new Object[]{rawname});
            }

            // attributes
            scanAttribute(fAttributes);
            
            // spaces
            fSawSpace = fEntityScanner.skipSpaces();

        } while (true);

        if (fBindNamespaces) {
            // REVISIT: is it required? forbit xmlns prefix for element
            if (fElementQName.prefix == XMLSymbols.PREFIX_XMLNS) {
                fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                           "ElementXMLNSPrefix",
                                           new Object[]{fElementQName.rawname},
                                           org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_FATAL_ERROR);
            }

            // bind the element
            String prefix = fElementQName.prefix != null
                            ? fElementQName.prefix : XMLSymbols.EMPTY_STRING;
            // assign uri to the element
            fElementQName.uri = fNamespaceContext.getURI(prefix);
            // make sure that object in the element stack is updated as well
            fCurrentElement.uri = fElementQName.uri;

            if (fElementQName.prefix == null && fElementQName.uri != null) {
                fElementQName.prefix = XMLSymbols.EMPTY_STRING;
                // making sure that the object in the element stack is updated too.
                fCurrentElement.prefix = XMLSymbols.EMPTY_STRING;
            }
            if (fElementQName.prefix != null && fElementQName.uri == null) {
                fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                           "ElementPrefixUnbound",
                                           new Object[]{fElementQName.prefix, fElementQName.rawname},
                                           org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_FATAL_ERROR);
            }

            // bind attributes (xmlns are already bound bellow)
            int length = fAttributes.getLength();
            // fLength = 0; //initialize structure
            for (int i = 0; i < length; i++) {
                fAttributes.getName(i, fAttributeQName);

                String aprefix = fAttributeQName.prefix != null
                                 ? fAttributeQName.prefix : XMLSymbols.EMPTY_STRING;
                String uri = fNamespaceContext.getURI(aprefix);
                // REVISIT: try removing the first "if" and see if it is faster.
                //
                if (fAttributeQName.uri != null && fAttributeQName.uri == uri) {
                    // checkDuplicates(fAttributeQName, fAttributes);
                    continue;
                }
                if (aprefix != XMLSymbols.EMPTY_STRING) {
                    fAttributeQName.uri = uri;
                    if (uri == null) {
                        fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                                   "AttributePrefixUnbound",
                                                   new Object[]{fElementQName.rawname,fAttributeQName.rawname,aprefix},
                                                   org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_FATAL_ERROR);
                    }
                    fAttributes.setURI(i, uri);
                    // checkDuplicates(fAttributeQName, fAttributes);
                }
            }
            
            if (length > 1) {
                QName name = fAttributes.checkDuplicatesNS();
                if (name != null) {
                    if (name.uri != null) {
                        fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                                   "AttributeNSNotUnique",
                                                   new Object[]{fElementQName.rawname, name.localpart, name.uri},
                                                   org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_FATAL_ERROR);
                    }
                    else {
                        fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                                   "AttributeNotUnique",
                                                   new Object[]{fElementQName.rawname, name.rawname}, 
                                                   org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_FATAL_ERROR);
                    }
                }
            }
        }


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

                if (fBindNamespaces) {
                    fNamespaceContext.popContext();
                }
                //pop the element off the stack..
                fElementStack.popElement(fElementQName);
            } else {
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
    protected void scanAttribute(XMLAttributesImpl attributes)
    throws IOException, XNIException {
        if (DEBUG_CONTENT_SCANNING) System.out.println(">>> scanAttribute()");

        // name
        fEntityScanner.scanQName(fAttributeQName);

        // equals
        fEntityScanner.skipSpaces();
        if (!fEntityScanner.skipChar('=')) {
            reportFatalError("EqRequiredInAttribute",
                             new Object[]{fCurrentElement.rawname,fAttributeQName.rawname});
        }
        fEntityScanner.skipSpaces();

        // content
        int attrIndex;
       
        if (fBindNamespaces) {
            attrIndex = attributes.getLength();
            attributes.addAttributeNS(fAttributeQName, XMLSymbols.fCDATASymbol, null);
        }
        else {
            int oldLen = attributes.getLength();
            attrIndex = attributes.addAttribute(fAttributeQName, XMLSymbols.fCDATASymbol, null);
        	
            // WFC: Unique Att Spec
            if (oldLen == attributes.getLength()) {
                reportFatalError("AttributeNotUnique",
                                 new Object[]{fCurrentElement.rawname,
                                 fAttributeQName.rawname});
            }
        }

        // Scan attribute value and return true if the non-normalized and normalized value are the same
        boolean isSameNormalizedAttr = scanAttributeValue(this.fTempString, fTempString2,
                fAttributeQName.rawname, fIsEntityDeclaredVC, fCurrentElement.rawname);
        
        String value = fTempString.toString();
        attributes.setValue(attrIndex, value);
        // If the non-normalized and normalized value are the same, avoid creating a new string.
        if (!isSameNormalizedAttr) {
            attributes.setNonNormalizedValue(attrIndex, fTempString2.toString());
        }
        attributes.setSpecified(attrIndex, true);

        // record namespace declarations if any.
        if (fBindNamespaces) {
            
            String localpart = fAttributeQName.localpart;
            String prefix = fAttributeQName.prefix != null
                            ? fAttributeQName.prefix : XMLSymbols.EMPTY_STRING;
            // when it's of form xmlns="..." or xmlns:prefix="...",
            // it's a namespace declaration. but prefix:xmlns="..." isn't.
            if (prefix == XMLSymbols.PREFIX_XMLNS ||
                prefix == XMLSymbols.EMPTY_STRING && localpart == XMLSymbols.PREFIX_XMLNS) {

                // get the internalized value of this attribute
                String uri = fSymbolTable.addSymbol(value);

                // 1. "xmlns" can't be bound to any namespace
                if (prefix == XMLSymbols.PREFIX_XMLNS && localpart == XMLSymbols.PREFIX_XMLNS) {
                    fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                               "CantBindXMLNS",
                                               new Object[]{fAttributeQName},
                                               org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_FATAL_ERROR);
                }

                // 2. the namespace for "xmlns" can't be bound to any prefix
                if (uri == NamespaceContext.XMLNS_URI) {
                    fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                               "CantBindXMLNS",
                                               new Object[]{fAttributeQName},
                                               org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_FATAL_ERROR);
                }

                // 3. "xml" can't be bound to any other namespace than it's own
                if (localpart == XMLSymbols.PREFIX_XML) {
                    if (uri != NamespaceContext.XML_URI) {
                        fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                                   "CantBindXML",
                                                   new Object[]{fAttributeQName},
                                                   org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_FATAL_ERROR);
                    }
                }
                // 4. the namespace for "xml" can't be bound to any other prefix
                else {
                    if (uri ==NamespaceContext.XML_URI) {
                        fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                                   "CantBindXML",
                                                   new Object[]{fAttributeQName},
                                                   org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter.SEVERITY_FATAL_ERROR);
                    }
                }

                prefix = localpart != XMLSymbols.PREFIX_XMLNS ? localpart : XMLSymbols.EMPTY_STRING;

                // http://www.w3.org/TR/1999/REC-xml-names-19990114/#dt-prefix
                // We should only report an error if there is a prefix,
                // that is, the local part is not "xmlns". -SG
                if (uri == XMLSymbols.EMPTY_STRING && localpart != XMLSymbols.PREFIX_XMLNS) {
                    fErrorReporter.reportError(XMLMessageFormatter.XMLNS_DOMAIN,
                                               "EmptyPrefixedAttName",
                                               new Object[]{fAttributeQName},
                                               XMLErrorReporter.SEVERITY_FATAL_ERROR);
                }

                // declare prefix in context
                fNamespaceContext.declarePrefix(prefix, uri.length() != 0 ? uri : null);
                // bind namespace attribute to a namespace
                attributes.setURI(attrIndex, fNamespaceContext.getURI(XMLSymbols.PREFIX_XMLNS));

            }
            else {
                // attempt to bind attribute
                if (fAttributeQName.prefix != null) {
                    attributes.setURI(attrIndex, fNamespaceContext.getURI(fAttributeQName.prefix));
                }
            }
        }

        if (DEBUG_CONTENT_SCANNING) System.out.println("<<< scanAttribute()");
    } // scanAttribute(XMLAttributes)



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

        // pop context
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
            if (fBindNamespaces) {
                fNamespaceContext.popContext();
            }

        }

        return fMarkupDepth;

    } // scanEndElement():int


    public void reset(XMLComponentManager componentManager)
    throws XMLConfigurationException {

        super.reset(componentManager);
        fPerformValidation = false;
        fBindNamespaces = false;
    }

    /** Creates a content dispatcher. */
    protected Dispatcher createContentDispatcher() {
        return new NSContentDispatcher();
    } // createContentDispatcher():Dispatcher

    /**
     * Dispatcher to handle content scanning.
     */
    protected final class NSContentDispatcher
        extends ContentDispatcher {
        
        /**
         * Scan for root element hook. This method is a hook for
         * subclasses to add code that handles scanning for the root
         * element. This method will also attempt to remove DTD validator
         * from the pipeline, if there is no DTD grammar. If DTD validator
         * is no longer in the pipeline bind namespaces in the scanner.
         *
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
                reconfigurePipeline();
                if (scanStartElementAfterName()) {
                    setScannerState(SCANNER_STATE_TRAILING_MISC);
                    setDispatcher(fTrailingMiscDispatcher);
                    return true;
                }
            }
            else {
                reconfigurePipeline();
                if (scanStartElement()) {
                    setScannerState(SCANNER_STATE_TRAILING_MISC);
                    setDispatcher(fTrailingMiscDispatcher);
                    return true;
                }
            }
            return false;

        } // scanRootElementHook():boolean
        
        /**
         * Re-configures pipeline by removing the DTD validator 
         * if no DTD grammar exists. If no validator exists in the
         * pipeline or there is no DTD grammar, namespace binding
         * is performed by the scanner in the enclosing class.
         */
        private void reconfigurePipeline() {
            if (fDTDValidator == null) {
                fBindNamespaces = true;
            }
            else if (!fDTDValidator.hasGrammar()) {
                fBindNamespaces = true;
                fPerformValidation = fDTDValidator.validate();
                // re-configure pipeline
                XMLDocumentSource source = fDTDValidator.getDocumentSource();
                XMLDocumentHandler handler = fDTDValidator.getDocumentHandler();
                source.setDocumentHandler(handler);
                if (handler != null)
                    handler.setDocumentSource(source);
                fDTDValidator.setDocumentSource(null);
                fDTDValidator.setDocumentHandler(null);
            }
        } // reconfigurePipeline()
    }

} // class XMLNSDocumentScannerImpl
