/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/

package org.milyn.cdr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.Smooks;
import org.milyn.cdr.extension.ExtensionContext;
import org.milyn.container.ExecutionContext;
import org.milyn.container.standalone.StandaloneApplicationContext;
import org.milyn.expression.ExpressionEvaluator;
import org.milyn.io.StreamUtils;
import org.milyn.net.URIUtil;
import org.milyn.profile.DefaultProfileSet;
import org.milyn.resource.URIResourceLocator;
import org.milyn.util.ClassUtil;
import org.milyn.xml.DomUtils;
import org.milyn.xml.LocalDTDEntityResolver;
import org.milyn.xml.LocalEntityResolver;
import org.milyn.xml.XmlUtil;
import org.milyn.xml.XsdDOMValidator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Digester class for an XML {@link org.milyn.cdr.SmooksResourceConfiguration} file (.cdrl).
 *
 * @author tfennelly
 */
public final class XMLConfigDigester {

    public static final String DTD_V10 = "http://www.milyn.org/dtd/smooksres-list-1.0.dtd";
    public static final String XSD_V10 = "http://www.milyn.org/xsd/smooks-1.0.xsd";
    public static final String XSD_V11 = "http://www.milyn.org/xsd/smooks-1.1.xsd";

    private static Log logger = LogFactory.getLog(XMLConfigDigester.class);

    private final SmooksResourceConfigurationList resourcelist;
    private final Stack<SmooksConfig> configStack = new Stack<SmooksConfig>();
    private ClassLoader classLoader;

    private Map<String, Smooks> extendedConfigDigesters = new HashMap<String, Smooks>();
    private static ThreadLocal<Boolean> extentionDigestOn = new ThreadLocal<Boolean>();

    /**
     * Private constructor.
     * <p/>
     * Never make this constructor (or any other) public.  It's only called from 2 places:
     * <ul>
     *  <li>From the {@link #digestConfig(java.io.InputStream, String)} method.</li>
     *  <li>From the {@link #getExtenededConfigDigester(String)} method.</li>
     * </ul>
     *
     * The {@link #digestConfig(java.io.InputStream, String)} method is public and always calls
     * {@link #setExtentionDigestOff()}.  The {@link #getExtenededConfigDigester(String)} method is
     * private and always calls {@link #setExtentionDigestOn()}.  Playing with the
     * public/private nature of these methods may effect the behavior of the {@link #extentionDigestOn}
     * ThreadLocal.
     *
     *
     * @param list Config list.
     */
    public XMLConfigDigester(SmooksResourceConfigurationList list) {
        this.resourcelist = list;
        configStack.push(new SmooksConfig("root-config"));
    }

    /**
     * Digest the XML Smooks configuration stream.
     *
     * @param stream  The stream.
     * @param baseURI The base URI to be associated with the configuration stream.
     * @param extendedConfigDigesters Config digesters.
     * @return A {@link SmooksResourceConfigurationList} containing the list of
     *         {@link SmooksResourceConfiguration SmooksResourceConfigurations} defined in the
     *         XML configuration.
     * @throws SAXException Error parsing the XML stream.
     * @throws IOException  Error reading the XML stream.
     * @throws SmooksConfigurationException  Invalid configuration..
     */
    public static SmooksResourceConfigurationList digestConfig(InputStream stream, String baseURI, Map<String, Smooks> extendedConfigDigesters) throws SAXException, IOException, URISyntaxException, SmooksConfigurationException {
        return digestConfig(stream, baseURI, extendedConfigDigesters, null);
    }

    /**
     * Digest the XML Smooks configuration stream.
     *
     * @param stream  The stream.
     * @param baseURI The base URI to be associated with the configuration stream.
     * @param extendedConfigDigesters Config digesters.
     * @param classLoader The ClassLoader to be used.
     * @return A {@link SmooksResourceConfigurationList} containing the list of
     *         {@link SmooksResourceConfiguration SmooksResourceConfigurations} defined in the
     *         XML configuration.
     * @throws SAXException Error parsing the XML stream.
     * @throws IOException  Error reading the XML stream.
     * @throws SmooksConfigurationException  Invalid configuration..
     */
    public static SmooksResourceConfigurationList digestConfig(InputStream stream, String baseURI, Map<String, Smooks> extendedConfigDigesters, ClassLoader classLoader) throws SAXException, IOException, URISyntaxException, SmooksConfigurationException {
        SmooksResourceConfigurationList list = new SmooksResourceConfigurationList(baseURI);

        setExtentionDigestOff();
        XMLConfigDigester digester = new XMLConfigDigester(list);

        if(classLoader != null) {
            digester.classLoader = classLoader;
        }

        digester.extendedConfigDigesters = extendedConfigDigesters;
        digester.digestConfigRecursively(new InputStreamReader(stream), baseURI);

        return list;
    }

    /**
     * Digest the XML Smooks configuration stream.
     *
     * @param stream  The stream.
     * @param baseURI The base URI to be associated with the configuration stream.
     * @return A {@link SmooksResourceConfigurationList} containing the list of
     *         {@link SmooksResourceConfiguration SmooksResourceConfigurations} defined in the
     *         XML configuration.
     * @throws SAXException Error parsing the XML stream.
     * @throws IOException  Error reading the XML stream.
     * @throws SmooksConfigurationException  Invalid configuration..
     */
    public static SmooksResourceConfigurationList digestConfig(InputStream stream, String baseURI) throws SAXException, IOException, URISyntaxException, SmooksConfigurationException {
        return digestConfig(stream, baseURI, (ClassLoader) null);
    }

    /**
     * Digest the XML Smooks configuration stream.
     *
     * @param stream  The stream.
     * @param baseURI The base URI to be associated with the configuration stream.
     * @return A {@link SmooksResourceConfigurationList} containing the list of
     *         {@link SmooksResourceConfiguration SmooksResourceConfigurations} defined in the
     *         XML configuration.
     * @throws SAXException Error parsing the XML stream.
     * @throws IOException  Error reading the XML stream.
     * @throws SmooksConfigurationException  Invalid configuration..
     */
    public static SmooksResourceConfigurationList digestConfig(InputStream stream, String baseURI, ClassLoader classLoader) throws SAXException, IOException, URISyntaxException, SmooksConfigurationException {
        SmooksResourceConfigurationList list = new SmooksResourceConfigurationList(baseURI);

        setExtentionDigestOff();
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
    public SmooksResourceConfigurationList getResourceList() {
    	return resourcelist;
    }

    private void digestConfigRecursively(Reader stream, String baseURI) throws IOException, SAXException, URISyntaxException, SmooksConfigurationException {
        Document configDoc;
        String streamData = StreamUtils.readStream(stream);

        try {
            configDoc = XmlUtil.parseStream(new StringReader(streamData), getDTDEntityResolver(), XmlUtil.VALIDATION_TYPE.DTD, true);
            logger.debug("Using a deprecated Smooks configuration DTD '" + DTD_V10 + "'.  Update configuration to use XSD '" + XSD_V10 + "'.");
            digestV10DTDValidatedConfig(configDoc);
            logger.debug("Using a deprecated Smooks configuration DTD '" + DTD_V10 + "'.  Update configuration to use XSD '" + XSD_V10 + "'.");
        } catch (Exception e) {
            // Must be an XSD based config...
            try {
                configDoc = XmlUtil.parseStream(new StringReader(streamData));
            } catch (ParserConfigurationException ee) {
                throw new SAXException("Unable to parse Smooks configuration.", ee);
            }

            XsdDOMValidator validator = new XsdDOMValidator(configDoc);
            String defaultNS = validator.getDefaultNamespace().toString();

            validator.validate();

            configStack.peek().defaultNS = defaultNS;
            if(XSD_V10.equals(defaultNS)) {
                if(validator.getNamespaces().size() > 1) {
                    throw new SmooksConfigurationException("Unsupported use of multiple configuration namespaces from inside a v1.0 Smooks configuration. Configuration extension not supported from a v1.0 configuration.  Use the v1.1 configuration namespace.");
                }
                digestV10XSDValidatedConfig(baseURI, configDoc);
            } else if(XSD_V11.equals(defaultNS)) {
                digestV11XSDValidatedConfig(baseURI, configDoc);
            } else {
                throw new SAXException("Cannot parse Smooks configuration.  Unsupported default Namespace '" + defaultNS + "'.");
            }
        }

        if (resourcelist.isEmpty()) {
            throw new SAXException("Invalid Content Delivery Resource archive definition file: 0 Content Delivery Resource definitions.");
        }
    }

    private void digestV10DTDValidatedConfig(Document configDoc) throws SAXException {
        int cdrIndex = 1;
        Element currentElement;
        String resourceSelector;

        currentElement = (Element) XmlUtil.getNode(configDoc, "/smooks-resource-list");
        String defaultSelector = DomUtils.getAttributeValue(currentElement, "default-selector");
        String defaultNamespace = DomUtils.getAttributeValue(currentElement, "default-namespace");
        String defaultUseragent = DomUtils.getAttributeValue(currentElement, "default-useragent");
        String defaultPath = DomUtils.getAttributeValue(currentElement, "default-path");

        resourceSelector = "/smooks-resource-list/smooks-resource[" + cdrIndex + "]";
        while ((currentElement = (Element) XmlUtil.getNode(configDoc, resourceSelector)) != null) {
            String selector = DomUtils.getAttributeValue(currentElement, "selector");
            String namespace = DomUtils.getAttributeValue(currentElement, "namespace");
            String useragents = DomUtils.getAttributeValue(currentElement, "useragent");
            String path = DomUtils.getAttributeValue(currentElement, "path");
            SmooksResourceConfiguration resourceConfig;

            try {
                resourceConfig = new SmooksResourceConfiguration((selector != null ? selector : defaultSelector),
                        (namespace != null ? namespace : defaultNamespace),
                        (useragents != null ? useragents : defaultUseragent),
                        (path != null ? path : defaultPath));
            } catch (IllegalArgumentException e) {
                throw new SAXException("Invalid unit definition.", e);
            }

            // Add the parameters...
            digestParameters(currentElement, resourceConfig);

            resourcelist.add(resourceConfig);
            if (logger.isDebugEnabled()) {
                logger.debug("Adding smooks-resource config from [" + resourcelist.getName() + "]: " + resourceConfig);
            }

            cdrIndex++;
            resourceSelector = "/smooks-resource-list/smooks-resource[" + cdrIndex + "]";
        }
    }

    private void digestV10XSDValidatedConfig(String baseURI, Document configDoc) throws SAXException, URISyntaxException, SmooksConfigurationException {
        Element currentElement = configDoc.getDocumentElement();

        String defaultSelector = DomUtils.getAttributeValue(currentElement, "default-selector");
        String defaultNamespace = DomUtils.getAttributeValue(currentElement, "default-selector-namespace");
        String defaultProfile = DomUtils.getAttributeValue(currentElement, "default-target-profile");

        NodeList configNodes = currentElement.getChildNodes();

        for (int i = 0; i < configNodes.getLength(); i++) {
            if(configNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element configElement = (Element) configNodes.item(i);

                // Make sure the element is permitted...
                assertElementPermitted(configElement);

                if (DomUtils.getName(configElement).equals("profiles")) {
                    digestProfiles(configElement);
                } else if (DomUtils.getName(configElement).equals("import")) {
                    digestImport(configElement, new URI(baseURI));
                } else if (DomUtils.getName(configElement).equals("resource-config")) {
                    digestResourceConfig(configElement, defaultSelector, defaultNamespace, defaultProfile, null);
                }
            }
        }
    }

    private void digestV11XSDValidatedConfig(String baseURI, Document configDoc) throws SAXException, URISyntaxException, SmooksConfigurationException {
        Element currentElement = configDoc.getDocumentElement();

        String defaultSelector = DomUtils.getAttributeValue(currentElement, "default-selector");
        String defaultNamespace = DomUtils.getAttributeValue(currentElement, "default-selector-namespace");
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
                if(namespaceURI == null || namespaceURI.equals(XSD_V11)) {
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
                    // It's an extended reousource configuration element
                    digestExtendedResourceConfig(configElement, defaultSelector, defaultNamespace, defaultProfile, defaultConditionRef);
                }
            }
        }
    }

    private void digestParams(Element paramsElement) {
        NodeList paramNodes = paramsElement.getElementsByTagName("param");

        if(paramNodes.getLength() > 0) {
            SmooksResourceConfiguration globalParamsConfig = new SmooksResourceConfiguration(ParameterAccessor.GLOBAL_PARAMETERS);

            digestParameters(paramsElement, globalParamsConfig);
            resourcelist.add(globalParamsConfig);
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
                if (logger.isDebugEnabled()) {
                    logger.debug("Importing resource configuration '" + file + "' from inside '" + configStack.peek().configFile + "'.");
                }

                resourceStream = resourceLocator.getResource(file);
                try {
                    List<Element> importParams = DomUtils.getElements(importElement, "param", null);
                    if(!importParams.isEmpty()) {
                        // Inject parameters into import config...
                        String importConfig = StreamUtils.readStreamAsString(resourceStream);

                        for (Element importParam : importParams) {
                            String paramName = DomUtils.getAttributeValue(importParam, "name");
                            String paramValue = XmlUtil.serialize(importParam.getChildNodes());

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

        SmooksResourceConfiguration resourceConfig = new SmooksResourceConfiguration(
        		"org.xml.sax.driver",
        		(profiles != null ? profiles : defaultProfile),
        		readerClass);

        // Add the reader resource...
        configureHandlers(configElement, resourceConfig);
        configureFeatures(configElement, resourceConfig);
        configureParams(configElement, resourceConfig);

        resourcelist.add(resourceConfig);
    }

    private void configureHandlers(Element configElement, SmooksResourceConfiguration resourceConfig) {
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

    private void configureFeatures(Element configElement, SmooksResourceConfiguration resourceConfig) {
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

    private void configureParams(Element configElement, SmooksResourceConfiguration resourceConfig) {
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

    private void digestResourceConfig(Element configElement, String defaultSelector, String defaultNamespace, String defaultProfile, String defaultConditionRef) throws SAXException {
        String selector = DomUtils.getAttributeValue(configElement, "selector");
        String namespace = DomUtils.getAttributeValue(configElement, "selector-namespace");
        String profiles = DomUtils.getAttributeValue(configElement, "target-profile");
        Element resourceElement = getElementByTagName(configElement, "resource");
        Element conditionElement = getElementByTagName(configElement, "condition");
        String resource = null;
        SmooksResourceConfiguration resourceConfig;

        try {
            if (resourceElement != null) {
                resource = DomUtils.getAllText(resourceElement, true);
            }
            resourceConfig = new SmooksResourceConfiguration((selector != null ? selector : defaultSelector),
                    (namespace != null ? namespace : defaultNamespace),
                    (profiles != null ? profiles : defaultProfile),
                    resource);
            if (resourceElement != null) {
                resourceConfig.setResourceType(DomUtils.getAttributeValue(resourceElement, "type"));
            }

            // And add the condition, if defined...
            if(conditionElement != null) {
                ExpressionEvaluator evaluator = digestCondition(conditionElement);
                resourceConfig.setConditionEvaluator(evaluator);
            } else if(defaultConditionRef != null) {
                ExpressionEvaluator evaluator = getConditionEvaluator(defaultConditionRef);
                resourceConfig.setConditionEvaluator(evaluator);
            }
        } catch (IllegalArgumentException e) {
            throw new SAXException("Invalid unit definition.", e);
        }

        // Add the parameters...
        digestParameters(configElement, resourceConfig);

        resourcelist.add(resourceConfig);
        if (resource == null) {
            if (resourceConfig.getParameters(SmooksResourceConfiguration.PARAM_RESDATA) != null) {
                logger.debug("Resource 'null' for resource config: " + resourceConfig + ".  This is probably an error because the configuration contains a 'resdata' param, which suggests it is following the old DTD based configuration model.  The new model requires the resource to be specified in the <resource> element.");
            } else {
                logger.debug("Resource 'null' for resource config: " + resourceConfig + ". This is not invalid!");
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Adding smooks-resource config from [" + resourcelist.getName() + "]: " + resourceConfig);
        }
    }

    private void digestExtendedResourceConfig(Element configElement, String defaultSelector, String defaultNamespace, String defaultProfile, String defaultConditionRef) {
        String configNamespace = configElement.getNamespaceURI();
        Smooks configDigester = getExtenededConfigDigester(configNamespace);
        ExecutionContext executionContext = configDigester.createExecutionContext();
        ExtensionContext extentionContext;
        Element conditionElement = DomUtils.getElement(configElement, "condition", 1);

        // Create the ExtenstionContext and set it on the ExecutionContext...
        if(conditionElement != null && (conditionElement.getNamespaceURI().equals(XSD_V10) || conditionElement.getNamespaceURI().equals(XSD_V11))) {
            extentionContext = new ExtensionContext(this, defaultSelector, defaultNamespace, defaultProfile, digestCondition(conditionElement));
        } else if(defaultConditionRef != null) {
            extentionContext = new ExtensionContext(this, defaultSelector, defaultNamespace, defaultProfile, getConditionEvaluator(defaultConditionRef));
        } else {
            extentionContext = new ExtensionContext(this, defaultSelector, defaultNamespace, defaultProfile, null);
        }
        ExtensionContext.setExtensionContext(extentionContext, executionContext);

        // Filter the extension element through Smooks...
        configDigester.filterSource(executionContext, new DOMSource(configElement), null);

        // Copy the created resources from the ExtensionContext and onto the SmooksResourceConfigurationList...
        List<SmooksResourceConfiguration> resources = extentionContext.getResources();
        for (SmooksResourceConfiguration resource : resources) {
            resourcelist.add(resource);
        }
    }

    private Smooks getExtenededConfigDigester(String configNamespace) {
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
            smooks = new Smooks(StandaloneApplicationContext.createNewInstance(false));
            setExtentionDigestOn();
            try {
                SmooksResourceConfigurationStore configStore = smooks.getApplicationContext().getStore();
                SmooksResourceConfigurationList extConfigList = new SmooksResourceConfigurationList(baseURI);

                XMLConfigDigester configDigester = new XMLConfigDigester(extConfigList);

                configDigester.extendedConfigDigesters = extendedConfigDigesters;
                configDigester.digestConfigRecursively(new InputStreamReader(ClassUtil.getResourceAsStream(resourcePath, classLoader)), baseURI);
                configStore.addSmooksResourceConfigurationList(extConfigList);
            } catch (Exception e) {
                throw new SmooksConfigurationException("Failed to construct Smooks instance for processing extended configuration resource '" + resourcePath + "'.", e);
            } finally {
                setExtentionDigestOff();
            }

            // And add it to the Map of extension digesters...
            extendedConfigDigesters.put(configNamespace, smooks);
        }

        if(classLoader != null) {
            smooks.setClassLoader(classLoader);
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
        if (!XSD_V10.equals(defaultNS) && !XSD_V11.equals(defaultNS)) {
            throw new SmooksConfigurationException("Extended resource configuration '" + resourcePath + "' default namespace must be a valid Smooks configuration namespace.");
        }
        if(validator.getNamespaces().size() > 1) {
            throw new SmooksConfigurationException("Extended resource configuration '" + resourcePath + "' defines configurations from multiple namespaces.  This is not permitted.  Only use configurations from the base Smooks config namespaces e.g. '" + XSD_V11 + "'.");
        }
    }

    private static boolean isExtensionConfig() {
        return extentionDigestOn.get();
    }

    private static void setExtentionDigestOn() {
        extentionDigestOn.set(true);
    }

    private static void setExtentionDigestOff() {
        extentionDigestOn.set(false);
    }

    private static Element getElementByTagName(Element configElement, String name) {
        NodeList elements = configElement.getElementsByTagName(name);
        if(elements.getLength() != 0) {
            return (Element) elements.item(0);
        }
        return null;
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

    public ExpressionEvaluator digestCondition(Element conditionElement) throws SmooksConfigurationException {
        String idRef = DomUtils.getAttributeValue(conditionElement, "idRef");

        if(idRef != null) {
            return getConditionEvaluator(idRef);
        } else {
            String evaluatorClassName = DomUtils.getAttributeValue(conditionElement, "evaluator");

            if(evaluatorClassName == null || evaluatorClassName.trim().equals("")) {
                evaluatorClassName = "org.milyn.javabean.expression.BeanMapExpressionEvaluator";
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

                resourcelist.add(profileSet);
            }
        }
    }

    private void digestParameters(Element resourceConfigElement, SmooksResourceConfiguration resourceConfig) {
        NodeList configNodes = resourceConfigElement.getElementsByTagName("param");

        for (int i = 0; i < configNodes.getLength(); i++) {
            Element paramNode = (Element) configNodes.item(i);
            String paramName = DomUtils.getAttributeValue(paramNode, "name");
            String paramType = DomUtils.getAttributeValue(paramNode, "type");
            String paramValue = DomUtils.getAllText(paramNode, true);
            Parameter paramInstance;

            paramInstance = resourceConfig.setParameter(paramName, paramType, paramValue);
            paramInstance.setXML(paramNode);
        }
    }

    private LocalEntityResolver getDTDEntityResolver() {
        return new LocalDTDEntityResolver();
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
        SmooksConfig currentConfig = configStack.pop();

        // Make sure we don't have a v1.1 imported from config within a v1.0...
        if(XSD_V11.equals(currentConfig.defaultNS) && !configStack.isEmpty()) {
            SmooksConfig parentConfig = configStack.peek();

            if(parentConfig.defaultNS.equals(XSD_V10)) {
                // This must be an import of a v1.1 schema based config from inside a v1.0 schema config.  Not allowed!!
                // Push the current config back onto the stack so as to get the correct config path inserted into the exception...
                configStack.push(currentConfig);
                throw new SmooksConfigurationException("Unsupported import of a v1.1 configuration from inside a v1.0 configuration.  Path to configuration: '" + getCurrentPath() + "'.");
            }
        }
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
