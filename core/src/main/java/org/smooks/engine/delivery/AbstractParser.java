/*-
 * ========================LICENSE_START=================================
 * Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.engine.delivery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.api.TypedKey;
import org.smooks.api.delivery.ContentDeliveryConfig;
import org.smooks.api.lifecycle.LifecycleManager;
import org.smooks.api.resource.config.Parameter;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.reader.JavaXMLReader;
import org.smooks.api.resource.reader.SmooksXMLReader;
import org.smooks.assertion.AssertArgument;
import org.smooks.engine.injector.Scope;
import org.smooks.engine.lifecycle.PostConstructLifecyclePhase;
import org.smooks.engine.lookup.LifecycleManagerLookup;
import org.smooks.engine.resource.reader.NullSourceXMLReader;
import org.smooks.engine.resource.reader.XStreamXMLReader;
import org.smooks.engine.xml.NamespaceManager;
import org.smooks.io.DocumentInputSource;
import org.smooks.io.NullReader;
import org.smooks.io.payload.FilterSource;
import org.smooks.io.payload.JavaSource;
import org.smooks.namespace.NamespaceDeclarationStack;
import org.smooks.namespace.NamespaceDeclarationStackAware;
import org.smooks.support.ClassUtil;
import org.w3c.dom.Document;
import org.xml.sax.*;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Stack;

/**
 * Abstract Parser.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class AbstractParser {

    public static final String ORG_XML_SAX_DRIVER = "org.xml.sax.driver";
    public static final String FEATURE_ON = "feature-on";
    public static final String FEATURE_OFF = "feature-off";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractParser.class);
    private static final TypedKey<Stack<XMLReader>> XML_READER_STACK_TYPED_KEY = new TypedKey<>();
    
    private final ExecutionContext executionContext;
    private final ResourceConfig saxDriverConfig;

    /**
     * Public constructor.
     *
     * @param executionContext     The Smooks Container Request that the parser is being instantiated on behalf of.
     * @param saxDriverConfig SAX Parser configuration. See <a href="#parserconfig">.cdrl Configuration</a>.
     */
    public AbstractParser(ExecutionContext executionContext, ResourceConfig saxDriverConfig) {
        AssertArgument.isNotNull(executionContext, "executionContext");
        this.executionContext = executionContext;
        this.saxDriverConfig = saxDriverConfig;
    }

    public AbstractParser(ExecutionContext executionContext) {
        this(executionContext, getSAXParserConfiguration(executionContext.getContentDeliveryRuntime().getContentDeliveryConfig()));
    }

    protected ExecutionContext getExecutionContext() {
        return executionContext;
    }

    @SuppressWarnings("unused")
    protected ResourceConfig getSaxDriverConfig() {
        return saxDriverConfig;
    }

    public static void attachXMLReader(XMLReader xmlReader, ExecutionContext execContext) {
        getReaders(execContext).push(xmlReader);

        NamespaceDeclarationStack namespaceDeclarationStack = execContext.get(NamespaceManager.NAMESPACE_DECLARATION_STACK_TYPED_KEY);
        if (namespaceDeclarationStack == null) {
            throw new IllegalStateException("No NamespaceDeclarationStack attached to the ExecutionContext.");
        }
        namespaceDeclarationStack.pushReader(xmlReader);
    }

    public static XMLReader getXMLReader(ExecutionContext executionContext) {
        Stack<XMLReader> xmlReaderStack = getReaders(executionContext);

        if(!xmlReaderStack.isEmpty()) {
            return xmlReaderStack.peek();
        } else {
            return null;
        }
    }

    public static void detachXMLReader(ExecutionContext executionContext) {
        Stack<XMLReader> xmlReaderStack = getReaders(executionContext);

        if(!xmlReaderStack.isEmpty()) {
            xmlReaderStack.pop();
            executionContext.get(NamespaceManager.NAMESPACE_DECLARATION_STACK_TYPED_KEY).popReader();
        }
    }

    @SuppressWarnings("unchecked")
    public static Stack<XMLReader> getReaders(ExecutionContext executionContext) {
        Stack<XMLReader> readers = executionContext.get(XML_READER_STACK_TYPED_KEY);

        if(readers == null) {
            readers = new Stack<>();
            setReaders(readers, executionContext);
        }
        return readers;
    }

    public static void setReaders(Stack<XMLReader> readers, ExecutionContext executionContext) {
        executionContext.put(XML_READER_STACK_TYPED_KEY, readers);
    }

    /**
     * Get the SAX Parser configuration for the profile associated with the supplied delivery configuration.
     *
     * @param deliveryConfig Content delivery configuration.
     * @return Returns the SAX Parser configuration for the profile associated with the supplied delivery
     *         configuration, or null if no parser configuration is specified.
     */
    public static ResourceConfig getSAXParserConfiguration(ContentDeliveryConfig deliveryConfig) {
        if (deliveryConfig == null) {
            throw new IllegalArgumentException("null 'deliveryConfig' arg in method call.");
        }

        ResourceConfig saxDriverConfig = null;
        List<ResourceConfig> saxConfigs = deliveryConfig.getResourceConfigs(ORG_XML_SAX_DRIVER);

        if (saxConfigs != null && !saxConfigs.isEmpty()) {
            saxDriverConfig = saxConfigs.get(0);
        }

        return saxDriverConfig;
    }

    @SuppressWarnings("WeakerAccess")
    protected static Reader getReader(Source source, String contentEncoding) {
    	if(source != null) {
	        if (source instanceof StreamSource) {
	            StreamSource streamSource = (StreamSource) source;
	            if (streamSource.getReader() != null) {
	                return streamSource.getReader();
	            } else if (streamSource.getInputStream() != null) {
	            	return streamToReader(streamSource.getInputStream(), contentEncoding);
				} else if (streamSource.getSystemId() != null) {
					return systemIdToReader(streamSource.getSystemId(), contentEncoding);
				}

	            throw new SmooksException("Invalid " + StreamSource.class.getName() + ".  No InputStream, Reader or SystemId instance.");
			} else if (source.getSystemId() != null) {
				return systemIdToReader(source.getSystemId(), contentEncoding);
			}
    	}

        return new NullReader();
    }

	private static Reader systemIdToReader(String systemId, String contentEncoding) {
        return streamToReader(systemIdToStream(systemId), contentEncoding);
	}

    private static InputStream systemIdToStream(String systemId) {
        try {
            return systemIdToURL(systemId).openStream();
        } catch (IOException e) {
            throw new SmooksException("Invalid System ID on StreamSource: '" + systemId + "'.  Unable to open stream to resource.", e);
        }
    }

	private static URL systemIdToURL(final String systemId)
	{
		try {
			return new URL(systemId);
		} catch (MalformedURLException e) {
		    throw new SmooksException("Invalid System ID on StreamSource: '" + systemId + "'.  Must be a valid URL.", e);
		}

	}

	private static Reader streamToReader(InputStream inputStream, String contentEncoding) {
		try {
		    if (contentEncoding != null) {
		        return new InputStreamReader(inputStream, contentEncoding);
		    } else {
		        return new InputStreamReader(inputStream, StandardCharsets.UTF_8);
		    }
		} catch (UnsupportedEncodingException e) {
		    throw new SmooksException("Unable to decode input stream.", e);
		}
	}

    protected InputSource createInputSource(Source source, String contentEncoding) {
        // Also attach the underlying stream to the InputSource...
        if (source instanceof StreamSource) {
            StreamSource streamSource = (StreamSource) source;
            InputStream inputStream;
            Reader reader;

            inputStream = getInputStream(streamSource);
            reader = streamSource.getReader();
            if (reader == null) {
                if (inputStream == null) {
                    throw new SmooksException("Invalid StreamSource.  Unable to extract an InputStream (even by systemId) or Reader instance.");
                }
                reader = streamToReader(inputStream, contentEncoding);
            }

            InputSource inputSource = new InputSource();
            inputSource.setByteStream(inputStream);
            inputSource.setCharacterStream(reader);

            return inputSource;
        } else if (source instanceof DOMSource)  {
            return new DocumentInputSource((Document) ((DOMSource) source).getNode());
        } else {
            return new InputSource(getReader(source, contentEncoding));
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected InputStream getInputStream(StreamSource streamSource) {
        InputStream inputStream = streamSource.getInputStream();
        String systemId = streamSource.getSystemId();

        if (inputStream != null) {
            return inputStream;
        } else if (systemId != null) {
            return systemIdToStream(systemId);
        }

        return null;
    }

    protected XMLReader createXMLReader() throws SAXException {
        XMLReader xmlReader;
        Source source = FilterSource.getSource(executionContext);

        if (saxDriverConfig != null && saxDriverConfig.getResource() != null) {
            xmlReader = XMLReaderFactory.createXMLReader(saxDriverConfig.getResource());
        } else if (source instanceof JavaSource) {
            JavaSource javaSource = (JavaSource) source;

            if (isFeatureOn(JavaSource.FEATURE_GENERATE_EVENT_STREAM, saxDriverConfig) && !javaSource.isEventStreamRequired()) {
                throw new SAXException("Invalid Smooks configuration.  Feature '" + JavaSource.FEATURE_GENERATE_EVENT_STREAM + "' is explicitly configured 'on' in the Smooks configuration, while the supplied JavaSource has explicitly configured event streaming to be off (through a call to JavaSource.setEventStreamRequired).");
            }

            // Event streaming must be explicitly turned off.  If is on as long as it is (a) not configured "off" in
            // the smooks config (via the reader features) and (b) not turned off via the supplied JavaSource...
            boolean eventStreamingOn = (!isFeatureOff(JavaSource.FEATURE_GENERATE_EVENT_STREAM, saxDriverConfig) && javaSource.isEventStreamRequired());
            if (eventStreamingOn && javaSource.getSourceObjects() != null) {
                xmlReader = new XStreamXMLReader();
            } else {
                xmlReader = new NullSourceXMLReader();
            }
        } else if (source instanceof DOMSource) {
            xmlReader = new DOMReader();
        } else {
            xmlReader = XMLReaderFactory.createXMLReader();
        }

        if (xmlReader instanceof SmooksXMLReader) {
            final LifecycleManager lifecycleManager = executionContext.getApplicationContext().getRegistry().lookup(new LifecycleManagerLookup());
            if (saxDriverConfig != null) {
                lifecycleManager.applyPhase(xmlReader, new PostConstructLifecyclePhase(new Scope(executionContext.getApplicationContext().getRegistry(), saxDriverConfig, xmlReader)));
            } else {
                lifecycleManager.applyPhase(xmlReader, new PostConstructLifecyclePhase());
            }
        }

        xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
        xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);

        setHandlers(xmlReader);
        setFeatures(xmlReader);

        return xmlReader;
    }

    protected void attachNamespaceDeclarationStack(XMLReader reader, ExecutionContext execContext) {
        if (reader instanceof NamespaceDeclarationStackAware) {
            NamespaceDeclarationStack nsDeclarationStack = execContext.get(NamespaceManager.NAMESPACE_DECLARATION_STACK_TYPED_KEY);

            if (nsDeclarationStack == null) {
                throw new IllegalStateException("NamespaceDeclarationStack not configured on ExecutionContext.");
            }

            ((NamespaceDeclarationStackAware) reader).setNamespaceDeclarationStack(nsDeclarationStack);
        }
    }

    protected void configureReader(XMLReader xmlReader, DefaultHandler2 contentHandler, ExecutionContext execContext, Source source) throws SAXException {
		if (xmlReader instanceof SmooksXMLReader) {
            ((SmooksXMLReader) xmlReader).setExecutionContext(execContext);
        }

        if (xmlReader instanceof JavaXMLReader) {
            if (!(source instanceof JavaSource)) {
                throw new SAXException("A " + JavaSource.class.getName() + " source must be supplied for " + JavaXMLReader.class.getName() + " implementations.");
            }
            ((JavaXMLReader) xmlReader).setSourceObjects(((JavaSource) source).getSourceObjects());
        }

        xmlReader.setContentHandler(contentHandler);

        try {
            xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", contentHandler);
        } catch (SAXNotRecognizedException e) {
            LOGGER.warn("XMLReader property 'http://xml.org/sax/properties/lexical-handler' not recognized by XMLReader '" + xmlReader.getClass().getName() + "'.");
        }
    }

    private void setHandlers(XMLReader reader) throws SAXException {
        if (saxDriverConfig != null) {
            List<Parameter<?>> handlers;

            handlers = saxDriverConfig.getParameters("sax-handler");
            if (handlers != null) {
                for (Parameter<?> handler : handlers) {
                    Object handlerObj = createHandler((String) handler.getValue());

                    if (handlerObj instanceof EntityResolver) {
                        reader.setEntityResolver((EntityResolver) handlerObj);
                    }
                    if (handlerObj instanceof DTDHandler) {
                        reader.setDTDHandler((DTDHandler) handlerObj);
                    }
                    if (handlerObj instanceof ErrorHandler) {
                        reader.setErrorHandler((ErrorHandler) handlerObj);
                    }
                }
            }
        }
    }

    private Object createHandler(String handlerName) throws SAXException {
        try {
            return ClassUtil.forName(handlerName, getClass()).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new SAXException("Failed to create SAX Handler '" + handlerName + "'.", e);
        }
    }

    private void setFeatures(XMLReader reader) throws SAXNotSupportedException, SAXNotRecognizedException {
        // Try setting the xerces "notify-char-refs" feature, may fail if it's not Xerces but that's OK...
        try {
            reader.setFeature("http://apache.org/xml/features/scanner/notify-char-refs", true);
        } catch (Throwable t) {
            // Ignore
        }
        // Report namespace decls as per SAX 2.0.2 spec...
        try {
        	// http://www.saxproject.org/apidoc/org/xml/sax/package-summary.html#package_description
            reader.setFeature("http://xml.org/sax/features/xmlns-uris", true);
        } catch (Throwable t) {
            // Not a SAX 2.0.2 compliant parser... Ignore
        }

        if (saxDriverConfig != null) {
            List<Parameter<?>> features;

            features = saxDriverConfig.getParameters(FEATURE_ON);
            if (features != null) {
                for (Parameter<?> feature : features) {
                    reader.setFeature((String) feature.getValue(), true);
                }
            }

            features = saxDriverConfig.getParameters(FEATURE_OFF);
            if (features != null) {
                for (Parameter<?> feature : features) {
                    reader.setFeature((String) feature.getValue(), false);
                }
            }
        }
    }

    public static boolean isFeatureOn(String name, ResourceConfig saxDriverConfig) throws SAXException {
        boolean featureOn = isFeature(name, FeatureValue.ON, saxDriverConfig);

        // Make sure the same feature is not also configured off...
        if (featureOn && isFeature(name, FeatureValue.OFF, saxDriverConfig)) {
            throw new SAXException("Invalid Smooks configuration.  Feature '" + name + "' is explicitly configured 'on' and 'off'.  Must be one or the other!");
        }

        return featureOn;
    }

    public static boolean isFeatureOff(String name, ResourceConfig saxDriverConfig) throws SAXException {
        boolean featureOff = isFeature(name, FeatureValue.OFF, saxDriverConfig);

        // Make sure the same feature is not also configured on...
        if (featureOff && isFeature(name, FeatureValue.ON, saxDriverConfig)) {
            throw new SAXException("Invalid Smooks configuration.  Feature '" + name + "' is explicitly configured 'on' and 'off'.  Must be one or the other!");
        }

        return featureOff;
    }

    private enum FeatureValue {
        ON,
        OFF
    }

    private static boolean isFeature(String name, FeatureValue featureValue, ResourceConfig saxDriverConfig) {
        if (saxDriverConfig != null) {
            List<Parameter<?>> features;

            if (featureValue == FeatureValue.ON) {
                features = saxDriverConfig.getParameters(FEATURE_ON);
            } else {
                features = saxDriverConfig.getParameters(FEATURE_OFF);
            }
            if (features != null) {
                for (Parameter<?> feature : features) {
                    if (feature.getValue().equals(name)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
