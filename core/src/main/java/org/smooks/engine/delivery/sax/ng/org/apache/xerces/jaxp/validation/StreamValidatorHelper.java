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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.SoftReference;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.Constants;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLEntityManager;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.XMLErrorReporter;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.XMLSchemaValidator;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.parsers.SAXParser;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.parsers.XML11Configuration;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XNIException;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLInputSource;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLParseException;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.smooks.engine.delivery.sax.ng.org.apache.xml.serialize.Method;
import org.smooks.engine.delivery.sax.ng.org.apache.xml.serialize.OutputFormat;
import org.smooks.engine.delivery.sax.ng.org.apache.xml.serialize.Serializer;
import org.smooks.engine.delivery.sax.ng.org.apache.xml.serialize.SerializerFactory;
import org.xml.sax.SAXException;

/**
 * <p>A validator helper for <code>StreamSource</code>s.</p>
 * 
 * @author Michael Glavassevich, IBM
 * @version $Id$
 */
final class StreamValidatorHelper implements org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.validation.ValidatorHelper {
    
    // feature identifiers
    
    /** Feature identifier: parser settings. */
    private static final String PARSER_SETTINGS = 
        Constants.XERCES_FEATURE_PREFIX + Constants.PARSER_SETTINGS;    
    
    // property identifiers
    
    /** Property identifier: entity resolver. */
    private static final String ENTITY_RESOLVER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_RESOLVER_PROPERTY;
    
    /** Property identifier: error handler. */
    private static final String ERROR_HANDLER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_HANDLER_PROPERTY;
    
    /** Property identifier: error reporter. */
    private static final String ERROR_REPORTER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;
    
    /** Property identifier: XML Schema validator. */
    private static final String SCHEMA_VALIDATOR =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_VALIDATOR_PROPERTY;
    
    /** Property identifier: symbol table. */
    private static final String SYMBOL_TABLE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;
    
    /** Property identifier: validation manager. */
    private static final String VALIDATION_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.VALIDATION_MANAGER_PROPERTY;
    
    /** Property identifier: security manager. */
    private static final String SECURITY_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY;
    
    //
    // Data
    //
    
    /** SoftReference to parser configuration. **/
    private SoftReference fConfiguration = new SoftReference(null);
    
    /** Schema validator. **/
    private final XMLSchemaValidator fSchemaValidator;
    
    /** Component manager. **/
    private final XMLSchemaValidatorComponentManager fComponentManager;

    /**
     * The parser maintains a reference to the configuration, so it must be a SoftReference too.
     */
    private SoftReference fParser = new SoftReference(null);
    
    /** Serializer factory. **/
    private SerializerFactory fSerializerFactory;
    
    public StreamValidatorHelper(XMLSchemaValidatorComponentManager componentManager) {
        fComponentManager = componentManager;
        fSchemaValidator = (XMLSchemaValidator) fComponentManager.getProperty(SCHEMA_VALIDATOR);
    }

    public void validate(Source source, Result result) 
        throws SAXException, IOException {
        if (result instanceof StreamResult || result == null) {
            final StreamSource streamSource = (StreamSource) source;
            final StreamResult streamResult = (StreamResult) result;
            XMLInputSource input = new XMLInputSource(streamSource.getPublicId(), streamSource.getSystemId(), null);
            input.setByteStream(streamSource.getInputStream());
            input.setCharacterStream(streamSource.getReader());
            
            // Gets the parser configuration. We'll create and initialize a new one, if we 
            // haven't created one before or if the previous one was garbage collected.
            boolean newConfig = false;
            XMLParserConfiguration config = (XMLParserConfiguration) fConfiguration.get();
            if (config == null) {
                config = initialize();
                newConfig = true;
            }
            // If settings have changed on the component manager, refresh the error handler and entity resolver.
            else if (fComponentManager.getFeature(PARSER_SETTINGS)) {
                config.setProperty(ENTITY_RESOLVER, fComponentManager.getProperty(ENTITY_RESOLVER));
                config.setProperty(ERROR_HANDLER, fComponentManager.getProperty(ERROR_HANDLER));
                config.setProperty(SECURITY_MANAGER, fComponentManager.getProperty(SECURITY_MANAGER));
            }
            
            // prepare for parse
            fComponentManager.reset();
            
            if (streamResult != null) {
                if (fSerializerFactory == null) {
                    fSerializerFactory = SerializerFactory.getSerializerFactory(Method.XML);
                }

                // there doesn't seem to be a way to reset a serializer, so we need to make
                // a new one each time.
                Serializer ser;
                if (streamResult.getWriter() != null) {
                    ser = fSerializerFactory.makeSerializer(streamResult.getWriter(), new OutputFormat());
                }
                else if (streamResult.getOutputStream() != null) {
                    ser = fSerializerFactory.makeSerializer(streamResult.getOutputStream(), new OutputFormat());
                }
                else if (streamResult.getSystemId() != null) {
                    String uri = streamResult.getSystemId();
                    OutputStream out = XMLEntityManager.createOutputStream(uri);
                    ser = fSerializerFactory.makeSerializer(out, new OutputFormat());
                }
                else {
                    throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(fComponentManager.getLocale(), 
                        "StreamResultNotInitialized", null));
                }

                // we're using the parser only as an XNI-to-SAX converter,
                // so that we can use the SAX-based serializer
                SAXParser parser = (SAXParser) fParser.get();
                if (newConfig || parser == null) {
                    parser = new SAXParser(config);
                    fParser = new SoftReference(parser);
                }
                else {
                    parser.reset();
                }
                config.setDocumentHandler(fSchemaValidator);
                fSchemaValidator.setDocumentHandler(parser);
                parser.setContentHandler(ser.asContentHandler());
            }
            else {
                fSchemaValidator.setDocumentHandler(null);
            }
            
            try {
                config.parse(input);
            }
            catch (XMLParseException e) {
                throw org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.validation.Util.toSAXParseException(e);
            }
            catch (XNIException e) {
                throw org.smooks.engine.delivery.sax.ng.org.apache.xerces.jaxp.validation.Util.toSAXException(e);
            }
            finally {
                // release the references to the SAXParser and Serializer
                fSchemaValidator.setDocumentHandler(null);
            }
            
            return;
        }
        throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(fComponentManager.getLocale(), 
                "SourceResultMismatch", 
                new Object [] {source.getClass().getName(), result.getClass().getName()}));
    }
    
    private XMLParserConfiguration initialize() {
        XML11Configuration config = new XML11Configuration();
        config.setProperty(ENTITY_RESOLVER, fComponentManager.getProperty(ENTITY_RESOLVER));
        config.setProperty(ERROR_HANDLER, fComponentManager.getProperty(ERROR_HANDLER));
        XMLErrorReporter errorReporter = (XMLErrorReporter) fComponentManager.getProperty(ERROR_REPORTER);
        config.setProperty(ERROR_REPORTER, errorReporter);
        // add message formatters
        if (errorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN) == null) {
            XMLMessageFormatter xmft = new XMLMessageFormatter();
            errorReporter.putMessageFormatter(XMLMessageFormatter.XML_DOMAIN, xmft);
            errorReporter.putMessageFormatter(XMLMessageFormatter.XMLNS_DOMAIN, xmft);
        }
        config.setProperty(SYMBOL_TABLE, fComponentManager.getProperty(SYMBOL_TABLE));
        config.setProperty(VALIDATION_MANAGER, fComponentManager.getProperty(VALIDATION_MANAGER));
        config.setProperty(SECURITY_MANAGER, fComponentManager.getProperty(SECURITY_MANAGER));
        config.setDocumentHandler(fSchemaValidator);
        config.setDTDHandler(null);
        config.setDTDContentModelHandler(null);
        fConfiguration = new SoftReference(config);
        return config;
    }
    
} // StreamValidatorHelper
