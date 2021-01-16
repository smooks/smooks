/*-
 * ========================LICENSE_START=================================
 * Smooks Core
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
package org.smooks.cdr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.Smooks;
import org.smooks.cdr.extension.ExtensionContext;
import org.smooks.container.ExecutionContext;
import org.smooks.container.standalone.DefaultApplicationContextBuilder;
import org.smooks.expression.ExpressionEvaluator;
import org.smooks.io.StreamUtils;
import org.smooks.net.URIUtil;
import org.smooks.profile.DefaultProfileSet;
import org.smooks.registry.Registry;
import org.smooks.resource.URIResourceLocator;
import org.smooks.util.ClassUtil;
import org.smooks.xml.DomUtils;
import org.smooks.xml.XmlUtil;
import org.smooks.xml.XsdDOMValidator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Digester class for an XML {@link ResourceConfig} file (.cdrl).
 *
 * @author tfennelly
 */
@SuppressWarnings("WeakerAccess")
public final class XMLConfigDigester {
    
    @Deprecated
    public static final String XSD_V12 = "https://www.smooks.org/xsd/smooks-1.2.xsd";
    
    public static final String XSD_V20 = "https://www.smooks.org/xsd/smooks-2.0.xsd";

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLConfigDigester.class);

    private final ResourceConfigList resourceConfigList;
    private final Stack<SmooksConfig> configStack = new Stack<>();
    private ClassLoader classLoader;

    private Map<String, Smooks> extendedConfigDigesters = new HashMap<>();
    private static final ThreadLocal<Boolean> extensionDigestOn = new ThreadLocal<>();

    /**
     * Private constructor.
     * <p/>
     * Never make this constructor (or any other) public.  It's only called from 2 places:
     * <ul>
     *  <li>From the {@link #digestConfig(java.io.InputStream, String)} method.</li>
     *  <li>From the {@link #getExtendedConfigDigester(String)} method.</li>
     * </ul>
     *
     * The {@link #digestConfig(java.io.InputStream, String)} method is public and always calls
     * {@link #setExtensionDigestOff()}.  The {@link #getExtendedConfigDigester(String)} method is
     * private and always calls {@link #setExtensionDigestOn()}.  Playing with the
     * public/private nature of these methods may effect the behavior of the {@link #extensionDigestOn}
     * ThreadLocal.
     *
     *
     * @param resourceConfigList Config list.
     */
    public XMLConfigDigester(ResourceConfigList resourceConfigList) {
        this.resourceConfigList = resourceConfigList;
        configStack.push(new SmooksConfig("root-config"));
    }

    /**
     * Digest the XML Smooks configuration stream.
     *
     * @param stream  The stream.
     * @param baseURI The base URI to be associated with the configuration stream.
     * @param extendedConfigDigesters Config digesters.
     * @return A {@link ResourceConfigList} containing the list of
     *         {@link ResourceConfig ResourceConfigs} defined in the
     *         XML configuration.
     * @throws SAXException Error parsing the XML stream.
     * @throws IOException  Error reading the XML stream.
     * @throws SmooksConfigurationException  Invalid configuration..
     */
    @SuppressWarnings("unused")
    public static ResourceConfigList digestConfig(InputStream stream, String baseURI, Map<String, Smooks> extendedConfigDigesters) throws SAXException, IOException, URISyntaxException, SmooksConfigurationException {
        return digestConfig(stream, baseURI, extendedConfigDigesters, null);
    }

    /**
     * Digest the XML Smooks configuration stream.
     *
     * @param stream  The stream.
     * @param baseURI The base URI to be associated with the configuration stream.
     * @param extendedConfigDigesters Config digesters.
     * @param classLoader The ClassLoader to be used.
     * @return A {@link ResourceConfigList} containing the list of
     *         {@link ResourceConfig ResourceConfigs} defined in the
     *         XML configuration.
     * @throws SAXException Error parsing the XML stream.
     * @throws IOException  Error reading the XML stream.
     * @throws SmooksConfigurationException  Invalid configuration..
     */
    public static ResourceConfigList digestConfig(InputStream stream, String baseURI, Map<String, Smooks> extendedConfigDigesters, ClassLoader classLoader) throws SAXException, IOException, URISyntaxException, SmooksConfigurationException {
        ResourceConfigList resourceConfigList = new ResourceConfigList(baseURI);

        setExtensionDigestOff();
        XMLConfigDigester digester = new XMLConfigDigester(resourceConfigList);

        if(classLoader != null) {
            digester.classLoader = classLoader;
        }

        digester.extendedConfigDigesters = extendedConfigDigesters;
        digester.digestConfigRecursively(new InputStreamReader(stream), baseURI);

        return resourceConfigList;
    }

    /**
     * Digest the XML Smooks configuration stream.
     *
     * @param stream  The stream.
     * @param baseURI The base URI to be associated with the configuration stream.
     * @return A {@link ResourceConfigList} containing the list of
     *         {@link ResourceConfig ResourceConfigs} defined in the
     *         XML configuration.
     * @throws SAXException Error parsing the XML stream.
     * @throws IOException  Error reading the XML stream.
     * @throws SmooksConfigurationException  Invalid configuration..
     */
    public static ResourceConfigList digestConfig(InputStream stream, String baseURI) throws SAXException, IOException, URISyntaxException, SmooksConfigurationException {
        return digestConfig(stream, baseURI, (ClassLoader) null);
    }

    /**
     * Digest the XML Smooks configuration stream.
     *
     * @param stream  The stream.
     * @param baseURI The base URI to be associated with the configuration stream.
     * @return A {@link ResourceConfigList} containing the list of
     *         {@link ResourceConfig ResourceConfigs} defined in the
     *         XML configuration.
     * @throws SAXException Error parsing the XML stream.
     * @throws IOException  Error reading the XML stream.
     * @throws SmooksConfigurationException  Invalid configuration..
     */
    public static ResourceConfigList digestConfig(InputStream stream, String baseURI, ClassLoader classLoader) throws SAXException, IOException, URISyntaxException, SmooksConfigurationException {
        ResourceConfigList list = new ResourceConfigList(baseURI);

        setExtensionDigestOff();
        XMLConfigDigester digester = new XMLConfigDigester(list);

        if(classLoader != null) {
            digester.classLoader = classLoader;
        }
        digester.digestConfigRecursively(new InputStreamReader(stream), baseURI);

        return list;
    }

    /**
     * Get the active resource configuration list.
     * @return The active resource configuration list.
     */
    public ResourceConfigList getResourceList() {
    	return resourceConfigList;
    }

    private void digestConfigRecursively(Reader stream, String baseURI) throws IOException, SAXException, URISyntaxException, SmooksConfigurationException {
        Document configDoc;
        String streamData = StreamUtils.readStream(stream);

        try {
            configDoc = XmlUtil.parseStream(new StringReader(streamData));
        } catch (ParserConfigurationException ee) {
            throw new SAXException("Unable to parse Smooks configuration.", ee);
        }

        XsdDOMValidator validator = new XsdDOMValidator(configDoc);
        String defaultNS = validator.getDefaultNamespace().toString();

        validator.validate();

        configStack.peek().defaultNS = defaultNS;
        if (XSD_V12.equals(defaultNS)) {
            digestV12XSDValidatedConfig(baseURI, configDoc);
        } else if (XSD_V20.equals(defaultNS)) {
            digestV20XSDValidatedConfig(baseURI, configDoc);
        } else {
            throw new SAXException("Cannot parse Smooks configuration.  Unsupported default Namespace '" + defaultNS + "'.");
        }

        if (resourceConfigList.isEmpty()) {
            throw new SAXException("Invalid Content Delivery Resource archive definition file: 0 Content Delivery Resource definitions.");
        }
    }

    @Deprecated
    private void digestV12XSDValidatedConfig(String baseURI, Document configDoc) throws SAXException, URISyntaxException, SmooksConfigurationException {
        Element currentElement = configDoc.getDocumentElement();

        String defaultSelector = DomUtils.getAttributeValue(currentElement, "default-selector");
        String defaultNamespace = DomUtils.getAttributeValue(currentElement, "default-selector-namespace");
        String defaultProfile = DomUtils.getAttributeValue(currentElement, "default-target-profile");
        String defaultConditionRef = DomUtils.getAttributeValue(currentElement, "default-condition-ref");

        NodeList configNodes = currentElement.getChildNodes();

        for (int i = 0; i < configNodes.getLength(); i++) {
            if (configNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element configElement = (Element) configNodes.item(i);

                // Make sure the element is permitted...
                assertElementPermitted(configElement);

                String elementName = DomUtils.getName(configElement);
                String namespaceURI = configElement.getNamespaceURI();
                if (namespaceURI == null || namespaceURI.equals(XSD_V12)) {
                    if (elementName.equals("params")) {
                        digestParams(configElement);
                    } else if (elementName.equals("conditions")) {
                        digestConditions(configElement);
                    } else if (elementName.equals("profiles")) {
                        digestProfiles(configElement);
                    } else if (elementName.equals("import")) {
                        digestImport(configElement, new URI(baseURI));
                    } else if (elementName.equals("reader")) {
                        digestReaderConfig(configElement, defaultProfile);
                    } else if (elementName.equals("resource-config")) {
                        digestResourceConfig(configElement, defaultSelector, defaultNamespace, defaultProfile, defaultConditionRef);
                    }
                } else {
                    // It's an extended resource configuration element
                    digestExtendedResourceConfig(configElement, defaultSelector, defaultNamespace, defaultProfile, defaultConditionRef);
                }
            }
        }
    }
    
    private void digestV20XSDValidatedConfig(String baseURI, Document configDoc) throws SAXException, URISyntaxException, SmooksConfigurationException {
        Element currentElement = configDoc.getDocumentElement();
        
        String defaultProfile = DomUtils.getAttributeValue(currentElement, "default-target-profile");
        String defaultConditionRef = DomUtils.getAttributeValue(currentElement, "default-condition-ref");

        NodeList configNodes = currentElement.getChildNodes();

        for (int i = 0; i < configNodes.getLength(); i++) {
            if(configNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element configElement = (Element) configNodes.item(i);

                // Make sure the element is permitted...
                assertElementPermitted(configElement);

                String elementName = DomUtils.getName(configElement);
                String namespaceURI = configElement.getNamespaceURI();
                if(namespaceURI == null || namespaceURI.equals(XSD_V20)) {
	                if (elementName.equals("params")) {
	                    digestParams(configElement);
	                } else if (elementName.equals("conditions")) {
	                    digestConditions(configElement);
	                } else if (elementName.equals("profiles")) {
	                    digestProfiles(configElement);
	                } else if (elementName.equals("import")) {
	                    digestImport(configElement, new URI(baseURI));
	                } else if (elementName.equals("reader")) {
	                    digestReaderConfig(configElement, defaultProfile);
	                } else if (elementName.equals("resource-config")) {
	                    digestResourceConfig(configElement, null, null, defaultProfile, defaultConditionRef);
	                }
                } else {
                    // It's an extended resource configuration element
                    digestExtendedResourceConfig(configElement, null, null, defaultProfile, defaultConditionRef);
                }
            }
        }
    }

    private void digestParams(Element paramsElement) {
        NodeList paramNodes = paramsElement.getElementsByTagName("param");

        if(paramNodes.getLength() > 0) {
            ResourceConfig globalParamsConfig = new ResourceConfig(ParameterAccessor.GLOBAL_PARAMETERS);

            digestParameters(paramsElement, globalParamsConfig);
            resourceConfigList.add(globalParamsConfig);
        }
    }

    private void assertElementPermitted(Element configElement) {
        if(isExtensionConfig()) {
            String elementName = DomUtils.getName(configElement);
            if(!elementName.equals("import") && !elementName.equals("resource-config")) {
                throw new SmooksConfigurationException("Configuration element '" + elementName + "' not supported in an extension configuration.");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void digestImport(Element importElement, URI baseURI) throws SAXException, URISyntaxException, SmooksConfigurationException {
        String file = DomUtils.getAttributeValue(importElement, "file");
        URIResourceLocator resourceLocator;
        InputStream resourceStream;

        if (file == null) {
            throw new IllegalStateException("Invalid resource import.  'file' attribute must be specified.");
        }

        resourceLocator = new URIResourceLocator();
        resourceLocator.setBaseURI(baseURI);

        try {
            URI fileURI = resourceLocator.resolveURI(file);

            // Add the resource URI to the list.  Will fail if it was already loaded
            pushConfig(file, fileURI);
            try {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Importing resource configuration '" + file + "' from inside '" + configStack.peek().configFile + "'.");
                }

                resourceStream = resourceLocator.getResource(file);
                try {
                    List<Element> importParams = DomUtils.getElements(importElement, "param", null);
                    if(!importParams.isEmpty()) {
                        // Inject parameters into import config...
                        String importConfig = StreamUtils.readStreamAsString(resourceStream, "UTF-8");

                        for (Element importParam : importParams) {
                            String paramName = DomUtils.getAttributeValue(importParam, "name");
                            String paramValue = XmlUtil.serialize(importParam.getChildNodes(), true);

                            importConfig = importConfig.replaceAll("@" + paramName + "@", paramValue);
                        }

                        digestConfigRecursively(new StringReader(importConfig), URIUtil.getParent(fileURI).toString()); // the file's parent URI becomes the new base URI.
                    } else {
                        digestConfigRecursively(new InputStreamReader(resourceStream), URIUtil.getParent(fileURI).toString()); // the file's parent URI becomes the new base URI.
                    }
                } finally {
                    resourceStream.close();
                }
            } finally {
                popConfig();
            }
        } catch (IOException e) {
            throw new SmooksConfigurationException("Failed to load Smooks configuration resource <import> '" + file + "': " + e.getMessage(), e);
        }
    }

    private void digestReaderConfig(Element configElement, String defaultProfile) {
    	String profiles = DomUtils.getAttributeValue(configElement, "targetProfile");

    	String readerClass = DomUtils.getAttributeValue(configElement, "class");

        ResourceConfig resourceConfig = new ResourceConfig(
        		"org.xml.sax.driver",
        		(profiles != null ? profiles : defaultProfile),
        		readerClass);

        // Add the reader resource...
        configureHandlers(configElement, resourceConfig);
        configureFeatures(configElement, resourceConfig);
        configureParams(configElement, resourceConfig);

        resourceConfigList.add(resourceConfig);
    }

    private void configureHandlers(Element configElement, ResourceConfig resourceConfig) {
        Element handlersElement = DomUtils.getElement(configElement, "handlers", 1);

        if(handlersElement != null) {
            NodeList handlers = handlersElement.getChildNodes();
            for(int i = 0; i < handlers.getLength(); i++) {
                if(handlers.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element handler = (Element) handlers.item(i);
                    String handlerClass = handler.getAttribute("class");

                    resourceConfig.setParameter("sax-handler", handlerClass);
                }
            }
        }
    }

    private void configureFeatures(Element configElement, ResourceConfig resourceConfig) {
        Element featuresElement = DomUtils.getElement(configElement, "features", 1);

        if(featuresElement != null) {
            NodeList features = featuresElement.getChildNodes();
            for(int i = 0; i < features.getLength(); i++) {
                if(features.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element feature = (Element) features.item(i);
                    String uri = feature.getAttribute("feature");

                    if(DomUtils.getName(feature).equals("setOn")) {
                        resourceConfig.setParameter("feature-on", uri);
                    } else {
                        resourceConfig.setParameter("feature-off", uri);
                    }
                }
            }
        }
    }

    private void configureParams(Element configElement, ResourceConfig resourceConfig) {
        Element paramsElement = DomUtils.getElement(configElement, "params", 1);

        if(paramsElement != null) {
            NodeList params = paramsElement.getChildNodes();
            for(int i = 0; i < params.getLength(); i++) {
                if(params.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element param = (Element) params.item(i);
                    String name = param.getAttribute("name");
                    String value = DomUtils.getAllText(param, true);

                    resourceConfig.setParameter(name, value);
                }
            }
        }
    }

    private void digestResourceConfig(Element configElement, String defaultSelector, String defaultNamespace, String defaultProfile, String defaultConditionRef) {
        final String factory = DomUtils.getAttributeValue(configElement, "factory");

        final ResourceConfig resourceConfig;
        try {
            final Class<? extends ResourceConfigFactory> resourceConfigFactoryClass;
            if (factory != null) {
                resourceConfigFactoryClass = (Class<ResourceConfigFactory>) Class.forName(factory, true, classLoader);
            } else {
                resourceConfigFactoryClass = DefaultResourceConfigFactory.class;
            }
            try {
                ResourceConfigFactory resourceConfigFactory = resourceConfigFactoryClass.newInstance();
                resourceConfig = resourceConfigFactory.createConfiguration(defaultSelector, defaultNamespace, defaultProfile, configElement);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new SmooksConfigurationException(e.getMessage(), e);
            }

            // And add the condition, if defined...
            final Element conditionElement = DomUtils.getElementByTagName(configElement, "condition");
            if (conditionElement != null) {
                ExpressionEvaluator evaluator = digestCondition(conditionElement);
                resourceConfig.getSelectorPath().setConditionEvaluator(evaluator);
            } else if (defaultConditionRef != null) {
                ExpressionEvaluator evaluator = getConditionEvaluator(defaultConditionRef);
                resourceConfig.getSelectorPath().setConditionEvaluator(evaluator);
            }
        } catch (IllegalArgumentException | ClassNotFoundException e) {
            throw new SmooksConfigurationException("Invalid unit definition.", e);
        }

        // Add the parameters...
        digestParameters(configElement, resourceConfig);

        resourceConfigList.add(resourceConfig);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Adding smooks-resource config from [" + resourceConfigList.getName() + "]: " + resourceConfig);
        }
    }

    @SuppressWarnings("ConfusingArgumentToVarargsMethod")
    private void digestExtendedResourceConfig(Element configElement, @Deprecated String defaultSelector, @Deprecated String defaultNamespace, String defaultProfile, String defaultConditionRef) {
        String configNamespace = configElement.getNamespaceURI();
        Smooks configDigester = getExtendedConfigDigester(configNamespace);
        ExecutionContext executionContext = configDigester.createExecutionContext();
        ExtensionContext extentionContext;
        Element conditionElement = DomUtils.getElement(configElement, "condition", 1);

        // Create the ExtenstionContext and set it on the ExecutionContext...
        if (conditionElement != null && (conditionElement.getNamespaceURI().equals(XSD_V12) || conditionElement.getNamespaceURI().equals(XSD_V20))) {
            extentionContext = new ExtensionContext(this, defaultSelector, defaultNamespace, defaultProfile, digestCondition(conditionElement));
        } else if(defaultConditionRef != null) {
            extentionContext = new ExtensionContext(this, defaultSelector, defaultNamespace, defaultProfile, getConditionEvaluator(defaultConditionRef));
        } else {
            extentionContext = new ExtensionContext(this, defaultSelector, defaultNamespace, defaultProfile, null);
        }
        executionContext.put(ExtensionContext.EXTENSION_CONTEXT_TYPED_KEY, extentionContext);

        // Filter the extension element through Smooks...
        configDigester.filterSource(executionContext, new DOMSource(configElement), null);

        // Copy the created resources from the ExtensionContext and onto the ResourceConfigList...
        List<ResourceConfig> resources = extentionContext.getResources();
        for (ResourceConfig resource : resources) {
            resourceConfigList.add(resource);
        }
    }

    private Smooks getExtendedConfigDigester(String configNamespace) {
        Smooks smooks = extendedConfigDigesters.get(configNamespace);

        if(smooks == null) {
            URI namespaceURI;

            try {
                namespaceURI = new URI(configNamespace);
            } catch (URISyntaxException e) {
                throw new SmooksConfigurationException("Unable to parse extended config namespace URI '" + configNamespace + "'.", e);
            }

            String resourcePath = "/META-INF" + namespaceURI.getPath() + "-smooks.xml";
            File resourceFile = new File(resourcePath);
            String baseURI = resourceFile.getParent().replace('\\', '/');

            // Validate the extended config...
            assertExtendedConfigOK(configNamespace, resourcePath);

            // Construct the Smooks instance for processing this config namespace...
            smooks = new Smooks(new DefaultApplicationContextBuilder().setClassLoader(classLoader).setRegisterSystemResources(false).build());
            setExtensionDigestOn();
            try {
                Registry registry = smooks.getApplicationContext().getRegistry();
                ResourceConfigList extConfigList = new ResourceConfigList(baseURI);

                XMLConfigDigester configDigester = new XMLConfigDigester(extConfigList);

                configDigester.extendedConfigDigesters = extendedConfigDigesters;
                configDigester.digestConfigRecursively(new InputStreamReader(ClassUtil.getResourceAsStream(resourcePath, classLoader)), baseURI);
                registry.registerResourceConfigList(extConfigList);
            } catch (Exception e) {
                throw new SmooksConfigurationException("Failed to construct Smooks instance for processing extended configuration resource '" + resourcePath + "'.", e);
            } finally {
                setExtensionDigestOff();
            }

            // And add it to the Map of extension digesters...
            extendedConfigDigesters.put(configNamespace, smooks);
        }
        
        return smooks;
    }

    private void assertExtendedConfigOK(String configNamespace, String resourcePath) {
        InputStream resourceStream = ClassUtil.getResourceAsStream(resourcePath, classLoader);

        if (resourceStream == null) {
            throw new SmooksConfigurationException("Unable to locate Smooks digest configuration '" + resourcePath + "' for extended resource configuration namespace '" + configNamespace + "'.  This resource must be available on the classpath.");
        }

        Document configDoc;
        try {
            configDoc = XmlUtil.parseStream(resourceStream);
        } catch (Exception e) {
            throw new SmooksConfigurationException("Unable to parse namespace URI '" + configNamespace + "'.", e);
        }

        XsdDOMValidator validator;
        try {
            validator = new XsdDOMValidator(configDoc);
        } catch (SAXException e) {
            throw new SmooksConfigurationException("Unable to create XsdDOMValidator instance for extended resource config '" + resourcePath + "'.", e);
        }

        String defaultNS = validator.getDefaultNamespace().toString();
        if (!XSD_V12.equals(defaultNS) && !XSD_V20.equals(defaultNS)) {
            throw new SmooksConfigurationException("Extended resource configuration '" + resourcePath + "' default namespace must be a valid Smooks configuration namespace.");
        }
        if (XSD_V12.equals(defaultNS) && validator.getNamespaces().size() > 1) {
            throw new SmooksConfigurationException("Extended resource configuration '" + resourcePath + "' defines configurations from multiple namespaces.  This is not permitted.  Only use configurations from the base Smooks config namespaces e.g. '" + XSD_V12 + "'.");
        }
    }

    private static boolean isExtensionConfig() {
        return extensionDigestOn.get();
    }

    private static void setExtensionDigestOn() {
        extensionDigestOn.set(true);
    }

    private static void setExtensionDigestOff() {
        extensionDigestOn.set(false);
    }

    private void digestConditions(Element conditionsElement) {
        NodeList conditions = conditionsElement.getElementsByTagName("condition");

        for(int i = 0; i < conditions.getLength(); i++) {
            Element conditionElement = (Element) conditions.item(i);
            String id = DomUtils.getAttributeValue(conditionElement, "id");

            if(id != null) {
                addConditionEvaluator(id, digestCondition(conditionElement));
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    public ExpressionEvaluator digestCondition(Element conditionElement) throws SmooksConfigurationException {
        String idRef = DomUtils.getAttributeValue(conditionElement, "idRef");

        if(idRef != null) {
            return getConditionEvaluator(idRef);
        } else {
            String evaluatorClassName = DomUtils.getAttributeValue(conditionElement, "evaluator");

            if(evaluatorClassName == null || evaluatorClassName.trim().equals("")) {
                evaluatorClassName = "org.smooks.javabean.expression.BeanMapExpressionEvaluator";
            }

            String evaluatorConditionExpression = DomUtils.getAllText(conditionElement, true);
            if(evaluatorConditionExpression == null || evaluatorConditionExpression.trim().equals("")) {
                throw new SmooksConfigurationException("smooks-resource/condition must specify a condition expression as child text e.g. <condition evaluator=\"....\">A + B > C</condition>.");
            }

            // And construct it...
            return ExpressionEvaluator.Factory.createInstance(evaluatorClassName, evaluatorConditionExpression);
        }
    }

    private void digestProfiles(Element profilesElement) {
        NodeList configNodes = profilesElement.getChildNodes();

        for (int i = 0; i < configNodes.getLength(); i++) {
            if(configNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element profileNode = (Element) configNodes.item(i);
                String baseProfile = DomUtils.getAttributeValue(profileNode, "base-profile");
                String subProfiles = DomUtils.getAttributeValue(profileNode, "sub-profiles");
                DefaultProfileSet profileSet = new DefaultProfileSet(baseProfile);

                if (subProfiles != null) {
                    profileSet.addProfiles(subProfiles.split(","));
                }

                resourceConfigList.add(profileSet);
            }
        }
    }

    private void digestParameters(Element resourceConfigElement, ResourceConfig resourceConfig) {
        NodeList configNodes = resourceConfigElement.getElementsByTagName("param");

        for (int i = 0; i < configNodes.getLength(); i++) {
            Element paramNode = (Element) configNodes.item(i);
            String paramName = DomUtils.getAttributeValue(paramNode, "name");
            String paramType = DomUtils.getAttributeValue(paramNode, "type");
            String paramValue = DomUtils.getAllText(paramNode, true);

            Parameter<?> paramInstance = resourceConfig.setParameter(paramName, paramType, paramValue);
            paramInstance.setXml(paramNode);
        }
    }

    public String getCurrentPath() {
        StringBuilder pathBuilder = new StringBuilder();

        for(int i = configStack.size() - 1; i >= 0; i--) {
            pathBuilder.insert(0, "]");
            pathBuilder.insert(0, configStack.get(i).configFile);
            pathBuilder.insert(0, "/[");
        }

        return pathBuilder.toString();
    }

    private void pushConfig(String file, URI fileURI) {
        for (SmooksConfig smooksConfig : configStack) {
            if(fileURI.equals(smooksConfig.fileURI)) {
                throw new SmooksConfigurationException("Invalid circular reference to config file '" + fileURI + "' from inside config file '" + getCurrentPath() + "'.");
            }
        }

        SmooksConfig config = new SmooksConfig(file);

        config.parent = configStack.peek();
        config.fileURI = fileURI;
        configStack.push(config);
    }

    private void popConfig() {
        configStack.pop();
    }

    public void addConditionEvaluator(String id, ExpressionEvaluator evaluator) {
        assertUniqueConditionId(id);
        configStack.peek().conditionEvaluators.put(id, evaluator);
    }

    public ExpressionEvaluator getConditionEvaluator(String idRef) {
        SmooksConfig smooksConfig = configStack.peek();

        while(smooksConfig != null) {
            ExpressionEvaluator evaluator = smooksConfig.conditionEvaluators.get(idRef);
            if(evaluator != null) {
                return evaluator;
            }
            smooksConfig = smooksConfig.parent;
        }

        throw new SmooksConfigurationException("Unknown condition idRef '" + idRef + "'.");
    }

    private void assertUniqueConditionId(String id) {
        if(configStack.peek().conditionEvaluators.containsKey(id)) {
            throw new SmooksConfigurationException("Duplicate condition ID '" + id + "'.");
        }
    }

    private static class SmooksConfig {

        private String defaultNS;
        private SmooksConfig parent;
        private final String configFile;
        private final Map<String, ExpressionEvaluator> conditionEvaluators = new HashMap<String, ExpressionEvaluator>();
        public URI fileURI;

        private SmooksConfig(String configFile) {
            this.configFile = configFile;
        }
    }
}
