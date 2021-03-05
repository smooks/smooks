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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLDTDContentModelHandler;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLLocator;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLResourceIdentifier;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLString;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLDTDSource;

/**
 * The DTD handler interface defines callback methods to report
 * information items in the DTD of an XML document. Parser components
 * interested in DTD information implement this interface and are
 * registered as the DTD handler on the DTD source.
 *
 * @see XMLDTDContentModelHandler
 *
 * @author Andy Clark, IBM
 *
 * @version $Id$
 */
public interface XMLDTDHandler {

    //
    // Constants
    //

    /**
     * Conditional section: INCLUDE. 
     *
     * @see #CONDITIONAL_IGNORE
     */
    public static final short CONDITIONAL_INCLUDE = 0;

    /** 
     * Conditional section: IGNORE.
     *
     * @see #CONDITIONAL_INCLUDE
     */
    public static final short CONDITIONAL_IGNORE = 1;

    //
    // XMLDTDHandler methods
    //

    /**
     * The start of the DTD.
     *
     * @param locator  The document locator, or null if the document
     *                 location cannot be reported during the parsing of 
     *                 the document DTD. However, it is <em>strongly</em>
     *                 recommended that a locator be supplied that can 
     *                 at least report the base system identifier of the
     *                 DTD.
     * @param augmentations Additional information that may include infoset
     *                      augmentations.
     *
     * @throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException Thrown by handler to signal an error.
     */
    public void startDTD(XMLLocator locator, org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations augmentations) 
        throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;

    /**
     * This method notifies of the start of a parameter entity. The parameter
     * entity name start with a '%' character.
     * 
     * @param name     The name of the parameter entity.
     * @param identifier The resource identifier.
     * @param encoding The auto-detected IANA encoding name of the entity
     *                 stream. This value will be null in those situations
     *                 where the entity encoding is not auto-detected (e.g.
     *                 internal parameter entities).
     * @param augmentations Additional information that may include infoset
     *                      augmentations.
     *
     * @throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException Thrown by handler to signal an error.
     */
    public void startParameterEntity(String name, 
                                     XMLResourceIdentifier identifier,
                                     String encoding,
                                     org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations augmentations) throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;

    /**
     * Notifies of the presence of a TextDecl line in an entity. If present,
     * this method will be called immediately following the startEntity call.
     * <p>
     * <strong>Note:</strong> This method is only called for external
     * parameter entities referenced in the DTD.
     * 
     * @param version  The XML version, or null if not specified.
     * @param encoding The IANA encoding name of the entity.
     * @param augmentations Additional information that may include infoset
     *                      augmentations.
     *
     * @throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException Thrown by handler to signal an error.
     */
    public void textDecl(String version, String encoding,
                         org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations augmentations) throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;

    /**
     * This method notifies the end of a parameter entity. Parameter entity
     * names begin with a '%' character.
     * 
     * @param name The name of the parameter entity.
     * @param augmentations Additional information that may include infoset
     *                      augmentations.
     *
     * @throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException Thrown by handler to signal an error.
     */
    public void endParameterEntity(String name, org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations augmentations) 
        throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;

    /**
     * The start of the DTD external subset.
     * 
     * @param identifier The resource identifier.
     * @param augmentations
     *                   Additional information that may include infoset
     *                   augmentations.
     * @exception org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException
     *                   Thrown by handler to signal an error.
     */
    public void startExternalSubset(XMLResourceIdentifier identifier, 
                                    org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations augmentations) 
        throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;

    /**
     * The end of the DTD external subset.
     *
     * @param augmentations Additional information that may include infoset
     *                      augmentations.
     *
     * @throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException Thrown by handler to signal an error.
     */
    public void endExternalSubset(org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations augmentations) 
        throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;

    /**
     * A comment.
     * 
     * @param text The text in the comment.
     * @param augmentations Additional information that may include infoset
     *                      augmentations.
     *
     * @throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException Thrown by application to signal an error.
     */
    public void comment(org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLString text, org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations augmentations) 
        throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;

    /**
     * A processing instruction. Processing instructions consist of a
     * target name and, optionally, text data. The data is only meaningful
     * to the application.
     * <p>
     * Typically, a processing instruction's data will contain a series
     * of pseudo-attributes. These pseudo-attributes follow the form of
     * element attributes but are <strong>not</strong> parsed or presented
     * to the application as anything other than text. The application is
     * responsible for parsing the data.
     * 
     * @param target The target.
     * @param data   The data or null if none specified.
     * @param augmentations Additional information that may include infoset
     *                      augmentations.
     *
     * @throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException Thrown by handler to signal an error.
     */
    public void processingInstruction(String target, org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLString data,
                                      org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations augmentations)
        throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;

    /**
     * An element declaration.
     * 
     * @param name         The name of the element.
     * @param contentModel The element content model.
     * @param augmentations Additional information that may include infoset
     *                      augmentations.
     *
     * @throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException Thrown by handler to signal an error.
     */
    public void elementDecl(String name, String contentModel,
                            org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations augmentations)
        throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;

    /**
     * The start of an attribute list.
     * 
     * @param elementName The name of the element that this attribute
     *                    list is associated with.
     * @param augmentations Additional information that may include infoset
     *                      augmentations.
     *
     * @throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException Thrown by handler to signal an error.
     */
    public void startAttlist(String elementName,
                             org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations augmentations) throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;

    /**
     * An attribute declaration.
     * 
     * @param elementName   The name of the element that this attribute
     *                      is associated with.
     * @param attributeName The name of the attribute.
     * @param type          The attribute type. This value will be one of
     *                      the following: "CDATA", "ENTITY", "ENTITIES",
     *                      "ENUMERATION", "ID", "IDREF", "IDREFS", 
     *                      "NMTOKEN", "NMTOKENS", or "NOTATION".
     * @param enumeration   If the type has the value "ENUMERATION" or
     *                      "NOTATION", this array holds the allowed attribute
     *                      values; otherwise, this array is null.
     * @param defaultType   The attribute default type. This value will be
     *                      one of the following: "#FIXED", "#IMPLIED",
     *                      "#REQUIRED", or null.
     * @param defaultValue  The attribute default value, or null if no
     *                      default value is specified.
     * @param nonNormalizedDefaultValue  The attribute default value with no normalization 
     *                      performed, or null if no default value is specified.
     * @param augmentations Additional information that may include infoset
     *                      augmentations.
     *
     * @throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException Thrown by handler to signal an error.
     */
    public void attributeDecl(String elementName, String attributeName,
                              String type, String[] enumeration,
                              String defaultType, org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLString defaultValue,
                              org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLString nonNormalizedDefaultValue, org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations augmentations)
        throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;

    /**
     * The end of an attribute list.
     *
     * @param augmentations Additional information that may include infoset
     *                      augmentations.
     *
     * @throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException Thrown by handler to signal an error.
     */
    public void endAttlist(org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations augmentations) throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;

    /**
     * An internal entity declaration.
     * 
     * @param name The name of the entity. Parameter entity names start with
     *             '%', whereas the name of a general entity is just the 
     *             entity name.
     * @param text The value of the entity.
     * @param nonNormalizedText The non-normalized value of the entity. This
     *             value contains the same sequence of characters that was in 
     *             the internal entity declaration, without any entity
     *             references expanded.
     * @param augmentations Additional information that may include infoset
     *                      augmentations.
     *
     * @throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException Thrown by handler to signal an error.
     */
    public void internalEntityDecl(String name, org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLString text, 
                                   org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLString nonNormalizedText,
                                   org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations augmentations) 
        throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;

    /**
     * An external entity declaration.
     * 
     * @param name     The name of the entity. Parameter entity names start
     *                 with '%', whereas the name of a general entity is just
     *                 the entity name.
     * @param identifier    An object containing all location information 
     *                      pertinent to this external entity.
     * @param augmentations Additional information that may include infoset
     *                      augmentations.
     *
     * @throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException Thrown by handler to signal an error.
     */
    public void externalEntityDecl(String name, 
                                   XMLResourceIdentifier identifier,
                                   org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations augmentations) 
        throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;

    /**
     * An unparsed entity declaration.
     * 
     * @param name     The name of the entity.
     * @param identifier    An object containing all location information 
     *                      pertinent to this unparsed entity declaration.
     * @param notation The name of the notation.
     * @param augmentations Additional information that may include infoset
     *                      augmentations.
     *
     * @throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException Thrown by handler to signal an error.
     */
    public void unparsedEntityDecl(String name, 
                                   XMLResourceIdentifier identifier, 
                                   String notation, org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations augmentations) 
        throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;

    /**
     * A notation declaration
     * 
     * @param name     The name of the notation.
     * @param identifier    An object containing all location information 
     *                      pertinent to this notation.
     * @param augmentations Additional information that may include infoset
     *                      augmentations.
     *
     * @throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException Thrown by handler to signal an error.
     */
    public void notationDecl(String name, XMLResourceIdentifier identifier,
                             org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations augmentations) throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;

    /**
     * The start of a conditional section.
     * 
     * @param type The type of the conditional section. This value will
     *             either be CONDITIONAL_INCLUDE or CONDITIONAL_IGNORE.
     * @param augmentations Additional information that may include infoset
     *                      augmentations.
     *
     * @throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException Thrown by handler to signal an error.
     *
     * @see #CONDITIONAL_INCLUDE
     * @see #CONDITIONAL_IGNORE
     */
    public void startConditional(short type, org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations augmentations) 
        throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;

    /**
     * Characters within an IGNORE conditional section.
     *
     * @param text The ignored text.
     * @param augmentations Additional information that may include infoset
     *                      augmentations.
     *
     * @throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException Thrown by handler to signal an error.
     */
    public void ignoredCharacters(XMLString text, org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations augmentations) 
        throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;

    /**
     * The end of a conditional section.
     *
     * @param augmentations Additional information that may include infoset
     *                      augmentations.
     *
     * @throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException Thrown by handler to signal an error.
     */
    public void endConditional(org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.Augmentations augmentations) throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;

    /**
     * The end of the DTD.
     *
     * @param augmentations Additional information that may include infoset
     *                      augmentations.
     *
     * @throws org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException Thrown by handler to signal an error.
     */
    public void endDTD(Augmentations augmentations) throws XNIException;

    // set the source of this handler
    public void setDTDSource(XMLDTDSource source);

    // return the source from which this handler derives its events
    public XMLDTDSource getDTDSource();

} // interface XMLDTDHandler
