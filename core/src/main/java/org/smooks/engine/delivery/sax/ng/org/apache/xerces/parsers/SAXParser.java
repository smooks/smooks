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

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.parsers.AbstractSAXParser;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.parsers.ObjectFactory;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.SymbolTable;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLParserConfiguration;

/**
 * This is the main Xerces SAX parser class. It uses the abstract SAX
 * parser with a document scanner, a dtd scanner, and a validator, as
 * well as a grammar pool.
 *
 * @author Arnaud  Le Hors, IBM
 * @author Andy Clark, IBM
 *
 * @version $Id$
 */
public class SAXParser
    extends AbstractSAXParser {

    //
    // Constants
    //

    // features

    /** Feature identifier: notify built-in refereces. */
    protected static final String NOTIFY_BUILTIN_REFS =
        Constants.XERCES_FEATURE_PREFIX + Constants.NOTIFY_BUILTIN_REFS_FEATURE;

    /** Recognized features. */
    private static final String[] RECOGNIZED_FEATURES = {
        NOTIFY_BUILTIN_REFS,
    };

    // properties

    /** Property identifier: symbol table. */
    protected static final String SYMBOL_TABLE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;

    /** Property identifier: XML grammar pool. */
    protected static final String XMLGRAMMAR_POOL =
        Constants.XERCES_PROPERTY_PREFIX+Constants.XMLGRAMMAR_POOL_PROPERTY;

    /** Recognized properties. */
    private static final String[] RECOGNIZED_PROPERTIES = {
        SYMBOL_TABLE,
        XMLGRAMMAR_POOL,
    };

    //
    // Constructors
    //

    /**
     * Constructs a SAX parser using the specified parser configuration.
     */
    public SAXParser(XMLParserConfiguration config) {
        super(config);
    } // <init>(XMLParserConfiguration)

    /**
     * Constructs a SAX parser using the dtd/xml schema parser configuration.
     */
    public SAXParser() {
        this(null, null);
    } // <init>()

    /**
     * Constructs a SAX parser using the specified symbol table.
     */
    public SAXParser(SymbolTable symbolTable) {
        this(symbolTable, null);
    } // <init>(SymbolTable)

    /**
     * Constructs a SAX parser using the specified symbol table and
     * grammar pool.
     */
    public SAXParser(SymbolTable symbolTable, XMLGrammarPool grammarPool) {
        super((XMLParserConfiguration) ObjectFactory.createObject(
            "org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLParserConfiguration",
            "org.smooks.engine.delivery.sax.ng.org.apache.xerces.parsers.XIncludeAwareParserConfiguration"
            ));

        // set features
        fConfiguration.addRecognizedFeatures(RECOGNIZED_FEATURES);
        fConfiguration.setFeature(NOTIFY_BUILTIN_REFS, true);

        // set properties
        fConfiguration.addRecognizedProperties(RECOGNIZED_PROPERTIES);
        if (symbolTable != null) {
            fConfiguration.setProperty(SYMBOL_TABLE, symbolTable);
        }
        if (grammarPool != null) {
            fConfiguration.setProperty(XMLGRAMMAR_POOL, grammarPool);
        }

    } // <init>(SymbolTable,XMLGrammarPool)

} // class SAXParser
