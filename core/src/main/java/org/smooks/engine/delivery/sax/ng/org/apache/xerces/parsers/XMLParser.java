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

import java.io.IOException;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLInputSource;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLParserConfiguration;

/**
 * Base class of all XML-related parsers.
 * <p>
 * In addition to the features and properties recognized by the parser
 * configuration, this parser recognizes these additional features and
 * properties:
 * <ul>
 * <li>Properties
 *  <ul>
 *   <li>http://apache.org/xml/properties/internal/error-handler</li>
 *   <li>http://apache.org/xml/properties/internal/entity-resolver</li>
 *  </ul>
 * </ul>
 *
 * @author Arnaud  Le Hors, IBM
 * @author Andy Clark, IBM
 *
 * @version $Id$
 */
public abstract class XMLParser {

    //
    // Constants
    //

    // properties

    /** Property identifier: entity resolver. */
    protected static final String ENTITY_RESOLVER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_RESOLVER_PROPERTY;

    /** Property identifier: error handler. */
    protected static final String ERROR_HANDLER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_HANDLER_PROPERTY;

    /** Recognized properties. */
    private static final String[] RECOGNIZED_PROPERTIES = {
        ENTITY_RESOLVER,
        ERROR_HANDLER,
    };

    //
    // Data
    //

    /** The parser configuration. */
    protected final XMLParserConfiguration fConfiguration;

    //
    // Constructors
    //

    /**
     * Default Constructor.
     */
    protected XMLParser(XMLParserConfiguration config) {

        // save configuration
        fConfiguration = config;

        // add default recognized properties
        fConfiguration.addRecognizedProperties(RECOGNIZED_PROPERTIES);

    } // <init>(XMLParserConfiguration)

    //
    // Public methods
    //

    /**
     * parse
     *
     * @param inputSource
     *
     * @exception XNIException
     * @exception IOException
     */
    public void parse(XMLInputSource inputSource) 
        throws XNIException, IOException {

        reset();
        fConfiguration.parse(inputSource);

    } // parse(XMLInputSource) 

    //
    // Protected methods
    //

    /**
     * reset all components before parsing
     */
    protected void reset() throws XNIException {
    } // reset()

} // class XMLParser
