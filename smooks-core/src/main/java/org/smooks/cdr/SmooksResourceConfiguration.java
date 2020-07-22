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

import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.jaxen.saxpath.SAXPathException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.cdr.xpath.SelectorStep;
import org.smooks.cdr.xpath.SelectorStepBuilder;
import org.smooks.cdr.xpath.evaluators.PassThruEvaluator;
import org.smooks.cdr.xpath.evaluators.XPathExpressionEvaluator;
import org.smooks.classpath.ClasspathUtils;
import org.smooks.container.ApplicationContext;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.Filter;
import org.smooks.delivery.Visitor;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.expression.ExecutionContextExpressionEvaluator;
import org.smooks.expression.ExpressionEvaluator;
import org.smooks.io.StreamUtils;
import org.smooks.profile.Profile;
import org.smooks.resource.URIResourceLocator;
import org.smooks.util.ClassUtil;
import org.smooks.xml.DomUtils;
import org.smooks.xml.XmlUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.*;

/**
 * Smooks Resource Targeting Configuration.
 * <p/>
 * A Smooks <b>Resource</b> is anything that can be used by Smooks in the process of analysing or
 * transforming a data stream.  They could be pieces
 * of Java logic (SAX or DOM element {@link Visitor} implementations), some text or script resource, or perhaps
 * simply a configuration parameter (see {@link org.smooks.cdr.ParameterAccessor}).
 * <p/>
 * <h2 id="restargeting">What is Resource Targeting?</h2>
 * Smooks works by "targeting" {@link Visitor} resources at message fragments.
 * This typically means targeting a piece of tranformation logic (XSLT, Java, Groovy etc)
 * at a specific fragment of that message.  The fragment may
 * include as much or as little of the document as required.  Smooks also allows you to target multilpe
 * resources at the same fragment.
 * <p/>
 * <h2 id="restargeting">Resource Targeting Configurations</h2>
 * Smooks can be manually/programmatically configured (through code), but the easiest way of working is through XML.  The follwoing
 * are a few sample configurations.  Explanations follow the samples.
 * <p/>
 * <b>A basic sample</b>.  Note that it is not using any profiling.  The <b>resource-config</b> element maps directly to an instance of this class.
 * <pre>
 * <i>&lt;?xml version='1.0'?&gt;
 * &lt;smooks-resource-list xmlns="https://www.smooks.org/xsd/smooks-1.2.xsd"&gt;
 *      <b>&lt;resource-config <a href="#selector">selector</a>="order/order-header"&gt;
 *          &lt;resource type="xsl"&gt;<a target="new" href="https://www.smooks.org#Smooks-smookscartridges">/com/acme/transform/OrderHeaderTransformer.xsl</a>&lt;/resource&gt;
 *      &lt;/resource-config&gt;</b>
 *      <b>&lt;resource-config <a href="#selector">selector</a>="order-items/order-item"&gt;
 *          &lt;resource&gt;{@link org.smooks.delivery.dom.DOMElementVisitor com.acme.transform.MyJavaOrderItemTransformer}&lt;/resource&gt;
 *      &lt;/resource-config&gt;</b>
 * &lt;/smooks-resource-list&gt;</i></pre>
 * <p/>
 * <b>A more complex sample</b>, using profiling.  So resource 1 is targeted at both "message-exchange-1" and "message-exchange-2",
 * whereas resource 2 is only targeted at "message-exchange-1" and resource 3 at "message-exchange-2" (see {@link org.smooks.Smooks#createExecutionContext(String)}).
 * <pre>
 * <i>&lt;?xml version='1.0'?&gt;
 * &lt;smooks-resource-list xmlns="https://www.smooks.org/xsd/smooks-1.2.xsd"&gt;
 *      <b>&lt;profiles&gt;
 *          &lt;profile base-profile="message-exchange-1" sub-profiles="message-producer-A, message-consumer-B" /&gt;
 *          &lt;profile base-profile="message-exchange-2" sub-profiles="message-producer-A, message-consumer-C" /&gt;
 *      &lt;/profiles&gt;</b>
 * (1)  &lt;resource-config selector="order/order-header" <b>target-profile="message-producer-A"</b>&gt;
 *          &lt;resource&gt;com.acme.transform.AddIdentityInfo&lt;/resource&gt;
 *      &lt;/resource-config&gt;
 * (2)  &lt;resource-config selector="order-items/order-item" <b>target-profile="message-consumer-B"</b>&gt;
 *          &lt;resource&gt;com.acme.transform.MyJavaOrderItemTransformer&lt;/resource&gt;
 *          &lt;param name="execution-param-X"&gt;param-value-forB&lt;/param&gt;
 *      &lt;/resource-config&gt;
 * (3)  &lt;resource-config selector="order-items/order-item" <b>target-profile="message-consumer-C"</b>&gt;
 *          &lt;resource&gt;com.acme.transform.MyJavaOrderItemTransformer&lt;/resource&gt;
 *          &lt;param name="execution-param-X"&gt;param-value-forC&lt;/param&gt;
 *      &lt;/resource-config&gt;
 * &lt;/smooks-resource-list&gt;</i></pre>
 * <p/>
 * <h3 id="attribdefs">Attribute Definitions</h3>
 * <ul>
 * <li><b id="useragent">target-profile</b>: A list of 1 or more {@link ProfileTargetingExpression profile targeting expressions}.
 * (supports wildcards "*").
 * </ol>
 * </li>
 * <li><b id="selector">selector</b>: Selector string.  Used by Smooks to "lookup" a resource configuration.
 * This is typically the message fragment name (partial XPath support), but as mentioned above, not all resources are
 * transformation/analysis resources targeted at a message fragment - this is why we didn't call this attribute
 * "target-fragment". This attribute supports a list of comma separated selectors, allowing you to target a
 * single resource at multiple selector (e.g. fragments).  Where the resource is a {@link Visitor} implementation, the selector
 * is treated as an XPath expression (full XPath spec not supported), otherwise the selector value is treated as an opaque value.
 * <p/>
 * <br/>
 * Example selectors:
 * <ol>
 * <li>For a {@link Visitor} implementation, use the target fragment name e.g. "order", "address", "address/name", "item[2]/price[text() = 99.99]" etc.
 * Also supports wildcard based fragment selection ("*").  See the <a href="www.smooks.org">User Guide</a> for more details on setting selectors for {@link Visitor} type
 * resources.
 * </li>
 * <li>"#document" is a special selector that targets a resource at the "document" fragment i.e. the whole document,
 * or document root node fragment.</li>
 * <li>Targeting a specific {@link org.smooks.xml.SmooksXMLReader} at a specific profile.</li>
 * </ol>
 * <p/>
 * </li>
 * <li><b id="namespace">selector-namespace</b>: The XML namespace of the selector target for this resource.  This is used
 * to target {@link Visitor} implementations at fragments from a
 * specific XML namespace e.g. "http://www.w3.org/2002/xforms".  If not defined, the resource
 * is targeted at all namespces.
 * <p/>
 * Note that since Smooks v1.3, namespace URI-to-prefix mappings can be configured through the "smooks-core" configuration namespace.  Then,
 * selectors can be configured with namespace prefixes, removing the need to use the "selector-namespace" configuration.
 * </li>
 * </ul>
 * <p/>
 * <h2 id="conditions">Resource Targeting Configurations</h2>
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 * @see SmooksResourceConfigurationSortComparator
 */
@SuppressWarnings({ "WeakerAccess", "unused", "deprecation", "unchecked" })
public class SmooksResourceConfiguration {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SmooksResourceConfiguration.class);
    
    /**
     * XML selector type definition prefix
     */
    public static final String XML_DEF_PREFIX = "xmldef:".toLowerCase();
    /**
     *
     */
    public static final String SELECTOR_NONE = "none";
    /**
     * URI resource locator.
     */
    private static URIResourceLocator uriResourceLocator = new URIResourceLocator();
    /**
     * A special selector for resource targeted at the document as a whole (the roor element).
     */
    public static final String DOCUMENT_FRAGMENT_SELECTOR = "#document";

    /**
     * A special selector for resource targeted at the document as a whole (the roor element).
     */
    public static final String DOCUMENT_VOID_SELECTOR = "$void";

    /**
     * Document target on which the resource is to be applied.
     */
    private String selector;
    /**
     * The name of the target element specified on the selector.
     */
    private QName targetElement;
    /**
     * The name of an attribute, if one is specified on the selector.
     */
    private QName targetAttribute;
    /**
     * Selector step.  Is the last step in the selectorSteps array.
     * We maintsain it as a stanalone variable as a small optimization i.e to save
     * indexing into the selectorSteps array.
     */
    private SelectorStep selectorStep;
    /**
     * Selector steps.
     */
    private SelectorStep[] selectorSteps;
    /**
     * Is the selector contextual i.e. does it have multiple steps.
     */
    private boolean isContextualSelector;
    /**
     * Target profile.
     */
    private String targetProfile;
    /**
     * List of device/profile names on which the Content Delivery Resource is to be applied
     * for instances of selector.
     */
    private String[] profileTargetingExpressionStrings;
    /**
     * Targeting expressions built from the target-profile list.
     */
    private ProfileTargetingExpression[] profileTargetingExpressions;
    /**
     * The resource.
     */
    private String resource;
    /**
     * Java resource object instance.
     */
    private Object javaResourceObject;
    /**
     * Is this resource defined inline in the configuration, or is it
     * referenced through a URI.
     */
    private boolean isInline = false;
    /**
     * Condition evaluator used in resource targeting.
     */
    private ExpressionEvaluator expressionEvaluator;
    /**
     * The type of the resource.  "class", "groovy", "xsl" etc....
     */
    private String resourceType;
    /**
     * Is this selector defininition an XML based definition.
     */
    private boolean isXmlDef;
    /**
     * SmooksResourceConfiguration parameters - String name and String value.
     */
    private LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
    private int parameterCount;
    /**
     * Global Parameters object.
     */
    private SmooksResourceConfiguration globalParams;
    /**
     * The XML namespace of the tag to which this config
     * should only be applied.
     */
    private String namespaceURI;
    /**
     * Flag indicating whether or not the resource is a default applied resource
     * e.g. {@link org.smooks.delivery.dom.serialize.DefaultSerializationUnit} or
     * {@link org.smooks.delivery.sax.DefaultSAXElementSerializer}.
     */
    private boolean defaultResource = false;
    /**
     * The extended config namespace from which the  resource was created.
     */
    private String extendedConfigNS;
    /**
     * Change listeners.
     */
    private Set<SmooksResourceConfigurationChangeListener> changeListeners = new HashSet<SmooksResourceConfigurationChangeListener>();

    /**
     * Public default constructor.
     *
     * @see #setSelector(String)
     * @see #setSelectorNamespaceURI(String)
     * @see #setTargetProfile(String)
     * @see #setResource(String)
     * @see #setResourceType(String)
     * @see #setParameter(String, String)
     */
    public SmooksResourceConfiguration() {
        setSelector(SELECTOR_NONE);
        setTargetProfile(Profile.DEFAULT_PROFILE);
    }

    /**
     * Public constructor.
     *
     * @param selector The selector definition.
     * @see #setSelectorNamespaceURI(String)
     * @see #setTargetProfile(String)
     * @see #setResource(String)
     * @see #setResourceType(String)
     * @see #setParameter(String, String)
     */
    public SmooksResourceConfiguration(String selector) {
        setSelector(selector);
        setTargetProfile(Profile.DEFAULT_PROFILE);
    }

    /**
     * Public constructor.
     *
     * @param selector The selector definition.
     * @param resource The resource.
     * @see #setSelectorNamespaceURI(String)
     * @see #setTargetProfile(String)
     * @see #setResourceType(String)
     * @see #setParameter(String, String)
     */
    public SmooksResourceConfiguration(String selector, String resource) {
        this(selector, Profile.DEFAULT_PROFILE, resource);
    }

    /**
     * Public constructor.
     *
     * @param selector      The selector definition.
     * @param targetProfile Target Profile(s).  Comma separated list of
     *                      {@link ProfileTargetingExpression ProfileTargetingExpressions}.
     * @param resource      The resource.
     * @see #setSelectorNamespaceURI(String)
     * @see #setResourceType(String)
     * @see #setParameter(String, String)
     */
    public SmooksResourceConfiguration(String selector, String targetProfile, String resource) {
        this(selector);

        setTargetProfile(targetProfile);
        setResource(resource);
    }

    /**
     * Perform a shallow clone of this configuration.
     *
     * @return Configuration clone.
     */
    @SuppressWarnings({ "MethodDoesntCallSuperMethod", "unchecked" })
    public Object clone() {
        SmooksResourceConfiguration clone = new SmooksResourceConfiguration();

        clone.extendedConfigNS = extendedConfigNS;
        clone.selector = selector;
        clone.selectorSteps = selectorSteps;
        clone.targetElement = targetElement;
        clone.targetAttribute = targetAttribute;
        clone.isContextualSelector = isContextualSelector;
        clone.targetProfile = targetProfile;
        clone.defaultResource = defaultResource;
        clone.profileTargetingExpressionStrings = profileTargetingExpressionStrings;
        clone.profileTargetingExpressions = profileTargetingExpressions;
        clone.resource = resource;
        clone.isInline = isInline;
        clone.resourceType = resourceType;
        clone.isXmlDef = isXmlDef;
        if (parameters != null) {
            clone.parameters = (LinkedHashMap<String, Object>) parameters.clone();
        }
        clone.parameterCount = parameterCount;
        clone.namespaceURI = namespaceURI;
        clone.expressionEvaluator = expressionEvaluator;

        return clone;
    }

    /**
     * Get the extended config namespace from which this configuration was created.
	 * @return The extended config namespace from which this configuration was created.
	 */
  public String getExtendedConfigNS() {
		return extendedConfigNS;
	}

    /**
     * Set the extended config namespace from which this configuration was created.
	 * @param extendedConfigNS The extended config namespace from which this configuration was created.
	 */
	public void setExtendedConfigNS(String extendedConfigNS) {
		this.extendedConfigNS = extendedConfigNS;
	}

	public void attachGlobalParameters(ApplicationContext appContext) {
        if(globalParams == null) {
            globalParams = appContext.getStore().getGlobalParams();
        }
    }

    public SmooksResourceConfiguration merge(SmooksResourceConfiguration config) {
        SmooksResourceConfiguration clone = (SmooksResourceConfiguration) clone();
        clone.parameters.clear();
        clone.parameters.putAll(config.parameters);
        clone.parameters.putAll(this.parameters);
        return clone;
    }

    public void addParameters(SmooksResourceConfiguration config) {
        parameters.putAll(config.parameters);
    }

    /**
     * Public constructor.
     *
     * @param selector             The selector definition.
     * @param selectorNamespaceURI The selector namespace URI.
     * @param targetProfile        Target Profile(s).  Comma separated list of
     *                             {@link ProfileTargetingExpression ProfileTargetingExpressions}.
     * @param resource             The resource.
     * @see #setResourceType(String)
     * @see #setParameter(String, String)
     */
    public SmooksResourceConfiguration(String selector, String selectorNamespaceURI, String targetProfile, String resource) {
        this(selector, targetProfile, resource);
        setSelectorNamespaceURI(selectorNamespaceURI);
    }

    /**
     * Set the config selector.
     *
     * @param selector The selector definition.
     */
    public void setSelector(final String selector) {
        if (selector == null || selector.trim().equals("")) {
            throw new IllegalArgumentException("null or empty 'selector' arg in constructor call.");
        }
        this.selector = selector;

        // If there's a "#document" token in the selector, but it's not at the very start,
        // then we have an invalid selector...
        int docSelectorIndex = selector.trim().indexOf(DOCUMENT_FRAGMENT_SELECTOR);
        if(docSelectorIndex != -1 && docSelectorIndex > 0) {
            throw new SmooksConfigurationException("Invalid selector '" + selector + "'.  '" + DOCUMENT_FRAGMENT_SELECTOR + "' token can only exist at the start of the selector.");
        }

        isXmlDef = selector.startsWith(XML_DEF_PREFIX);

        try {
            selectorSteps = SelectorStepBuilder.buildSteps(selector);
        } catch (SAXPathException e) {
            selectorSteps = constructSelectorStepsFromLegacySelector(selector);
        }

        initTarget();
        fireChangedEvent();
    }

    private void initTarget() {
        selectorStep = selectorSteps[selectorSteps.length - 1];
        targetElement = selectorStep.getTargetElement();
        targetAttribute = selectorStep.getTargetAttribute();
        isContextualSelector = (selectorSteps.length > 1);
    }

    private SelectorStep[] constructSelectorStepsFromLegacySelector(String selector) {
        // In case it's a legacy selector that we don't support...

        if(selector.startsWith("/")) {
            selector = DOCUMENT_FRAGMENT_SELECTOR + selector;
        }

        String[] contextualSelector = parseSelector(selector);

        List<LegacySelectorStep> selectorStepList = new ArrayList<LegacySelectorStep>();
        for(int i = 0; i < contextualSelector.length; i++) {
            String targetElementName = contextualSelector[i];

            if(i == contextualSelector.length - 2 && contextualSelector[contextualSelector.length - 1].startsWith("@")) {
                selectorStepList.add(new LegacySelectorStep(selector,  targetElementName, contextualSelector[contextualSelector.length - 1]));
                break;
            } else {
                selectorStepList.add(new LegacySelectorStep(selector,  targetElementName));
            }
        }

        LOGGER.debug("Unable to parse selector '" + selector + "' as an XPath selector (even after normalization).  Parsing as a legacy style selector.");

        return selectorStepList.toArray(new SelectorStep[0]);
    }

    public static String[] parseSelector(String selector) {
        String[] splitTokens;

        if(selector.startsWith("/")) {
            selector = selector.substring(1);
        }

        // Parse the selector in case it's a contextual selector...
        if (selector.indexOf('/') != -1) {
            // Parse it as e.g. "a/b/c" ...
            splitTokens = selector.split("/");
        } else {
            // Parse it as a CSS form selector e.g. "TD UL LI" ...
            splitTokens = selector.split(" +");
        }

        for (int i = 0; i < splitTokens.length; i++) {
            String splitToken = splitTokens[i];

            if (!splitToken.startsWith("@")) {
                splitTokens[i] = splitToken;
            }
        }

        return splitTokens;
    }

    /**
     * Set the namespace URI to which the selector is associated.
     *
     * @param namespaceURI Selector namespace.
     */
    public void setSelectorNamespaceURI(String namespaceURI) {
        if (namespaceURI != null) {
            if (namespaceURI.equals("*")) {
                this.namespaceURI = null;
            } else {
                this.namespaceURI = namespaceURI.intern();
            }
            fireChangedEvent();
        }
    }

    /**
     * Set the configs "resource".
     *
     * @param resource The resource.
     */
    public void setResource(String resource) {
        this.resource = resource;

        if (resource != null) {
            try {
                // If the resource resolves as a valid URI, then it's not an inline resource.
                new URI(resource);
                isInline = false;
            } catch (Exception e) {
                isInline = true;
            }
        }
        fireChangedEvent();
    }

    /**
     * Get the Java resource object instance associated with this resource, if one exists and
     * it has been create.
     * @return The Java resource object instance associated with this resource, if one exists and
     * it has been create, otherwise null.
     */
    public Object getJavaResourceObject() {
        return javaResourceObject;
    }

    /**
     * Set the Java resource object instance associated with this resource.
     * @param javaResourceObject The Java resource object instance associated with this resource.
     */
    public void setJavaResourceObject(Object javaResourceObject) {
        this.javaResourceObject = javaResourceObject;
    }

    /**
     * Is this resource defined inline in the configuration, or is it
     * referenced through a URI.
     * <p/>
     * Note that this method also returns false if the resource is undefined (null).
     *
     * @return True if the resource is defined inline, otherwise false.
     */
    public boolean isInline() {
        return isInline;
    }

    /**
     * Get the target profile string as set in the configuration.
     *
     * @return The target profile.
     */
    public String getTargetProfile() {
        return targetProfile;
    }

    /**
     * Set the configs "target profile".
     *
     * @param targetProfile Target Profile(s).  Comma separated list of
     *                      {@link ProfileTargetingExpression ProfileTargetingExpressions}.
     */
    public void setTargetProfile(String targetProfile) {
        if (targetProfile == null || targetProfile.trim().equals("")) {
            // Default the target profile to everything if not specified.
            targetProfile = Profile.DEFAULT_PROFILE;
        }
        this.targetProfile = targetProfile;
        parseTargetingExpressions(targetProfile);
    }

    /**
     * Explicitly set the resource type.
     * <p/>
     * E.g. "class", "xsl", "groovy" etc.
     *
     * @param resourceType The resource type.
     */
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    /**
     * Get the selector definition for this SmooksResourceConfiguration.
     *
     * @return The selector definition.
     */
    public String getSelector() {
        return selector;
    }

    /**
     * Set the selector steps.
     * @param selectorSteps The selector steps.
     */
    public void setSelectorSteps(SelectorStep[] selectorSteps) {
        this.selectorSteps = selectorSteps;
        initTarget();
        fireChangedEvent();
    }

    /**
     * Get the selector steps.
     * @return The selector steps.
     */
    public SelectorStep[] getSelectorSteps() {
        return selectorSteps;
    }

    /**
     * Get the targeting selector step.
     * @return The targeting selector step.
     */
    public SelectorStep getSelectorStep() {
        return selectorStep;
    }

    /**
     * Get the name of the target element where the {@link #getSelector() selector}
     * is targeting the resource at an XML element.
     * <p/>
     * Accomodates the fact that element based selectors can be contextual. This method
     * is not relevant where the selector is not targeting an XML element.
     * <p/>
     * See details about the "selector" attribute in the
     * <a href="#attribdefs">Attribute Definitions</a> section.
     *
     * @return The target XML element name.
     */
    public String getTargetElement() {
        return targetElement.getLocalPart();
    }

    /**
     * Get the {@link QName} of the target element where the {@link #getSelector() selector}
     * is targeting the resource at an XML element.
     * <p/>
     * Accomodates the fact that element based selectors can be contextual. This method
     * is not relevant where the selector is not targeting an XML element.
     * <p/>
     * See details about the "selector" attribute in the
     * <a href="#attribdefs">Attribute Definitions</a> section.
     *
     * @return The target XML element {@link QName}.
     */
    public QName getTargetElementQName() {
        return targetElement;
    }

    /**
     * Get the name of the attribute specified on the selector, if one was
     * specified.
     * @return An attribute name, if one was specified on the selector, otherwise null.
     */
    public String getTargetAttribute() {
        if(targetAttribute == null) {
            return null;
        }
        return targetAttribute.getLocalPart();
    }

    /**
     * Get the name of the attribute specified on the selector, if one was
     * specified.
     * @return An attribute name, if one was specified on the selector, otherwise null.
     */
    public QName getTargetAttributeQName() {
        return targetAttribute;
    }

    /**
     * The the selector namespace URI.
     *
     * @return The XML namespace URI of the element to which this configuration
     *         applies, or null if not namespaced.
     */
    public String getSelectorNamespaceURI() {
        return namespaceURI;
    }

    /**
     * Get the profile targeting expressions for this SmooksResourceConfiguration.
     *
     * @return The profile targeting expressions.
     */
    public ProfileTargetingExpression[] getProfileTargetingExpressions() {
        return profileTargetingExpressions;
    }

    /**
     * Get the resource for this SmooksResourceConfiguration.
     *
     * @return The cdrar path.
     */
    public String getResource() {
        return resource;
    }

    /**
     * Set the condition evaluator to be used in targeting of this resource.
     *
     * @param expressionEvaluator The {@link org.smooks.expression.ExpressionEvaluator}, or null if no condition is to be used.
     */
    public void setConditionEvaluator(ExpressionEvaluator expressionEvaluator) {
        if (expressionEvaluator != null && !(expressionEvaluator instanceof ExecutionContextExpressionEvaluator)) {
            throw new UnsupportedOperationException("Unsupported ExpressionEvaluator type '" + expressionEvaluator.getClass().getName() + "'.  Currently only support '" + ExecutionContextExpressionEvaluator.class.getName() + "' implementations.");
        }
        this.expressionEvaluator = expressionEvaluator;
    }

    /**
     * Get the condition evaluator used in targeting of this resource.
     *
     * @return The {@link org.smooks.expression.ExpressionEvaluator}, or null if no condition is specified.
     */
    public ExpressionEvaluator getConditionEvaluator() {
        return expressionEvaluator;
    }

    /**
     * Is this resource config a default applied resource.
     * <p/>
     * Some resources (e.g. {@link org.smooks.delivery.dom.serialize.DefaultSerializationUnit} or
     * {@link org.smooks.delivery.sax.DefaultSAXElementSerializer}) are applied by default when no other
     * resources are targeted at an element.
     *
     * @return True if this is a default applied resource, otherwise false.
     */
    public boolean isDefaultResource() {
        return defaultResource;
    }

    /**
     * Set this resource config as a default applied resource.
     * <p/>
     * Some resources (e.g. {@link org.smooks.delivery.dom.serialize.DefaultSerializationUnit} or
     * {@link org.smooks.delivery.sax.DefaultSAXElementSerializer}) are applied by default when no other
     * resources are targeted at an element.
     *
     * @param defaultResource True if this is a default applied resource, otherwise false.
     */
    public void setDefaultResource(boolean defaultResource) {
        this.defaultResource = defaultResource;
    }

    /**
     * Get the resource "type" for this resource.
     * <p/>
     * Determines the type through the following checks (in order):
     * <ol>
     * <li>Is it a Java resource. See {@link #isJavaResource()}.  If it is, return "class".</li>
     * <li>Is the "restype" resource paramater specified.  If it is, return it's value. Ala DTD v1.0</li>
     * <li>Is the resource type explicitly set on this configuration.  If it is, return it's
     * value. Ala the "type" attribute on the resource element on DTD v2.0</li>
     * <li>Return the resource path file extension e.g. "xsl".</li>
     * </ol>
     *
     * @return The resource type.
     */
    public String getResourceType() {
        if (isJavaResource()) {
            return "class";
        }

        final String restype;
        if (resourceType != null) {
            // Ala DTD v2.0, where the type is set through the "type" attribute on the <resource> element.
            restype = resourceType;
        } else {
            restype = getExtension(getResource());
        }

        return restype;
    }

    /**
     * Parse the targeting expressions for this configuration.
     *
     * @param targetProfiles The <b>target-profile</b> expression from the resource configuration.
     */
    private void parseTargetingExpressions(String targetProfiles) {
        // Parse the profiles.  Seperation tokens: ',' '|' and ';'
        StringTokenizer tokenizer = new StringTokenizer(targetProfiles, ",|;");
        if (tokenizer.countTokens() == 0) {
            throw new IllegalArgumentException("Empty 'target-profile'. [" + selector + "][" + resource + "]");
        } else {
            this.profileTargetingExpressionStrings = new String[tokenizer.countTokens()];
            profileTargetingExpressions = new ProfileTargetingExpression[tokenizer.countTokens()];
            for (int i = 0; tokenizer.hasMoreTokens(); i++) {
                String expression = tokenizer.nextToken();
                this.profileTargetingExpressionStrings[i] = expression;
                profileTargetingExpressions[i] = new ProfileTargetingExpression(expression);
            }
        }
    }

    /**
     * Get the file extension from the resource path.
     *
     * @param path Resource path.
     * @return File extension, or null if the resource path has no file extension.
     */
    private String getExtension(String path) {
        if (path != null) {
            File resFile = new File(path);
            String resName = resFile.getName();

            if (!resName.trim().equals("")) {
                int extensionIndex = resName.lastIndexOf('.');
                if (extensionIndex != -1 && (extensionIndex + 1 < resName.length())) {
                    return resName.substring(extensionIndex + 1);
                }
            }
        }

        return null;
    }

    /**
     * Set the named SmooksResourceConfiguration parameter value (default type - String).
     * <p/>
     * Overwrites previous value of the same name.
     *
     * @param name  Parameter name.
     * @param value Parameter value.
     * @return The parameter instance.
     */
    public <T> Parameter<T> setParameter(String name, T value) {
        Parameter<T> param = new Parameter(name, value);
        setParameter(param);
        return param;
    }

    /**
     * Set the named SmooksResourceConfiguration parameter value (with type).
     * <p/>
     * Overwrites previous value of the same name.
     *
     * @param name  Parameter name.
     * @param type  Parameter type.
     * @param value Parameter value.
     * @return The parameter instance.
     */
    public <T> Parameter<T> setParameter(String name, String type, T value) {
        Parameter<T> param = new Parameter(name, value, type);
        setParameter(param);
        return param;
    }

    public <T> void setParameter(Parameter<T> parameter) {
        if (parameters == null) {
            parameters = new LinkedHashMap<>();
        }
        Object exists = parameters.get(parameter.getName());

        if (exists == null) {
            parameters.put(parameter.getName(), parameter);
        } else if (exists instanceof Parameter) {
            Vector<Parameter> paramList = new Vector<Parameter>();
            paramList.add((Parameter) exists);
            paramList.add(parameter);
            parameters.put(parameter.getName(), paramList);
        } else if (exists instanceof List) {
            ((List) exists).add(parameter);
        }
        parameterCount++;
    }

    /**
     * Get the named SmooksResourceConfiguration {@link Parameter parameter}.
     * <p/>
     * If there is more than one of the named parameters defined, the first
     * defined value is returned.
     *
     * @param name Name of parameter to get.
     * @return Parameter value, or null if not set.
     */
    public <T> Parameter<T> getParameter(String name, Class<T> valueClass) {
        if (parameters == null) {
            return null;
        }
        Object parameter = parameters.get(name);

        if (parameter instanceof List) {
            return (Parameter<T>) ((List) parameter).get(0);
        } else if (parameter instanceof Parameter) {
            return (Parameter<T>) parameter;
        }

        if(globalParams != null) {
            return globalParams.getParameter(name, valueClass);
        }

        return null;
    }

    /**
     * Get the param map associated with this configuration.
     *
     * @return The configuration parameters.  The Map value is either a
     *         {@link Parameter} or parameter list (List&lt;{@link Parameter}&gt;).
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Get all {@link Parameter parameter} values set on this configuration.
     *
     * @return {@link Parameter} value {@link List}, or null if not set.
     */
    public List getParameterList() {
        if (parameters == null) {
            return null;
        }

        return new ArrayList(parameters.values());
    }

    /**
     * Get the named SmooksResourceConfiguration {@link Parameter parameter} List.
     *
     * @param name Name of parameter to get.
     * @return {@link Parameter} value {@link List}, or null if not set.
     */
    public List<Parameter> getParameters(String name) {
        if (parameters == null) {
            return null;
        }
        Object parameter = parameters.get(name);

        if (parameter instanceof List) {
            return (List) parameter;
        } else if (parameter instanceof Parameter) {
            Vector paramList = new Vector();
            paramList.add(parameter);
            //parameters.put(name, paramList);
            return paramList;
        }

        return null;
    }


    public Object getParameterValue(String name) {
        Parameter<Object> parameter = getParameter(name, Object.class);
        return (parameter != null ? parameter.getValue() : null);
    }
    
    /**
     * Get the named SmooksResourceConfiguration parameter.
     *
     * @param name Name of parameter to get.
     * @return Parameter value, or null if not set.
     */
    public <T> T getParameterValue(String name, Class<T> valueClass) {
        Parameter<T> parameter = getParameter(name, valueClass);
        return (parameter != null ? parameter.getValue() : null);
    }

    /**
     * Get the named SmooksResourceConfiguration parameter.
     *
     * @param name       Name of parameter to get.
     * @param defaultValue The default value to be returned if there are no
     *                   parameters on the this SmooksResourceConfiguration instance, or the parameter is not defined.
     * @return Parameter value, or defaultVal if not defined.
     */
    public <T> T getParameterValue(String name, Class<T> valueClass, T defaultValue) {
        T value = getParameterValue(name, valueClass);

        return (value != null ? value : defaultValue);
    }

    /**
     * Get the SmooksResourceConfiguration parameter count.
     *
     * @return Number of parameters defined on this SmooksResourceConfiguration.
     */
    public int getParameterCount() {
        return parameterCount;
    }

    /**
     * Remove the named parameter.
     *
     * @param name The name of the parameter to be removed.
     */
    public void removeParameter(String name) {
        parameters.remove(name);
    }

    /**
     * Is this selector defininition an XML based definition.
     * <p/>
     * I.e. is the selector attribute value prefixed with "xmldef:".
     *
     * @return True if this selector defininition is an XML based definition, otherwise false.
     */
    public boolean isXmlDef() {
        return isXmlDef;
    }

    /* (non-Javadoc)
      * @see java.lang.Object#toString()
      */
    public String toString() {
        return "Target Profile: [" + Arrays.asList(profileTargetingExpressionStrings) + "], Selector: [" + selector + "], Selector Namespace URI: [" + namespaceURI + "], Resource: [" + resource + "], Num Params: [" + getParameterCount() + "]";
    }

    /**
     * Get the resource as a byte array.
     * <p/>
     * If the resource data is not inlined in the configuration (in a "resdata" param), it will be
     * resolved using the {@link URIResourceLocator} i.e. the path will be enterpretted as a {@link java.net.URI}.
     * If the resource doesn't resolve to a stream producing URI, the resource string will be converted to
     * bytes and returned.
     *
     * @return The resource as a byte array, or null if resource path
     *         is null or the resource doesn't exist.
     */
    public byte[] getBytes() {
        // Defines the resource in a resource element, so it can be used to specify a path
        // or inlined resourcec data ala the "resdata" parameter in the 1.0 DTD.
        
        if (resource != null) {
            InputStream resStream;
            try {
                resStream = uriResourceLocator.getResource(resource);
            } catch (Exception e) {
                return getInlineResourceBytes();
            }

            try {
                byte[] resourceBytes;

                if (resStream == null) {
                    throw new IOException("Resource [" + resource + "] not found.");
                }

                try {
                    resourceBytes = StreamUtils.readStream(resStream);
                } finally {
                    resStream.close();
                }

                return resourceBytes;
            } catch (IOException e) {
                return getInlineResourceBytes();
            }
        }

        return null;
    }

    private byte[] getInlineResourceBytes() {
        try {
            // Ala DTD v2.0, where the <resource> element can carry the inlined resource data.
            return resource.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e1) {
            return resource.getBytes();
        }
    }

    /**
     * Returns the resource as a Java Class instance.
     *
     * @return The Java Class instance refered to be this resource configuration, or null
     *         if the resource doesn't refer to a Java Class.
     */
    public Class toJavaResource() {
        String className;

        if (resource == null) {
            return null;
        }

        className = ClasspathUtils.toClassName(resource);
        try {
            return ClassUtil.forName(className, getClass());
        } catch (ClassNotFoundException e) {
            if (resource.equals(className)) {
                LOGGER.debug("Resource path [" + resource + "] looks as though it may be a Java resource reference.  If so, this class is not available on the classpath.");
            }

            return null;
        } catch (IllegalArgumentException e) {
    		if (resource.equals(className)) {
    			LOGGER.debug("The string [" + resource + "] contains unescaped characters that are illegal in a Java resource name.");
    		}

    		return null;
        }
    }

    /**
     * Does this resource configuration refer to a Java Class resource.
     *
     * @return True if this resource configuration refers to a Java Class
     *         resource, otherwise false.
     */
    public boolean isJavaResource() {
        return (toJavaResource() != null);
    }

    /**
     * Is this resource a Java Class.
     *
     * @return True if this resource is a Java class, otherwise false.
     */
    public boolean isJavaContentHandler() {
        Class runtimeClass = toJavaResource();

        return (runtimeClass != null);
    }

    /**
     * Is this resource configuration targets at the same namespace as the
     * specified elemnt.
     *
     * @param namespace The element to check against.
     * @return True if this resource config is targeted at the element namespace,
     *         or if the resource is not targeted at any namespace (i.e. not specified),
     *         otherwise false.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isTargetedAtNamespace(String namespace) {
        if (namespaceURI != null) {
            return namespaceURI.equals(namespace);
        }

        return true;
    }

    /**
     * Is the resource selector contextual.
     * <p/>
     * See details about the "selector" attribute in the
     * <a href="#attribdefs">Attribute Definitions</a> section.
     *
     * @return True if the selector is contextual, otherwise false.
     */
    public boolean isSelectorContextual() {
        return isContextualSelector;
    }

    /**
     * Is this resource configuration targeted at the specified DOM element
     * in context.
     * <p/>
     * See details about the "selector" attribute in the
     * <a href="#attribdefs">Attribute Definitions</a> section.
     * <p/>
     * Note this doesn't perform any namespace checking.
     *
     * @param element The element to check against.
     * @param executionContext The current execution context.
     * @return True if this resource configuration is targeted at the specified
     *         element in context, otherwise false.
     */
    private boolean isTargetedAtElementContext(Element element, ExecutionContext executionContext) {
        Node currentNode = element;
        ContextIndex index = new ContextIndex(executionContext);

        index.i = selectorSteps.length - 1;

        // Unless it's **, start at the parent because the current element
        // has already been tested...
        if(!selectorSteps[index.i].isStarStar()) {
            index.i = selectorSteps.length - 2;
            currentNode = element.getParentNode();
        } else {
        	// The target selector step is "**".  If the parent one is "#document" and we're at
        	// the root now, then fail...
        	if(selectorSteps.length == 2 && selectorSteps[0].isRooted() && element.getParentNode() == null) {
        		return false;
        	}
        }

        if (currentNode == null || currentNode.getNodeType() != Node.ELEMENT_NODE) {
            return false;
        }

        // Check the element name(s).
        while (index.i >= 0) {
            Element currentElement = (Element) currentNode;
            Node parentNode;

            parentNode = currentElement.getParentNode();
            if(parentNode == null || parentNode.getNodeType() != Node.ELEMENT_NODE) {
                parentNode = null;
            }

            if(!isTargetedAtElementContext(currentElement, (Element) parentNode, index)) {
                return false;
            }

            if (parentNode == null) {
                return true;
            }

            currentNode = parentNode;
        }

        return true;
    }

    /**
     * Is this resource configuration targeted at the specified SAX element
     * in context.
     * <p/>
     * See details about the "selector" attribute in the
     * <a href="#attribdefs">Attribute Definitions</a> section.
     * <p/>
     * Note this doesn't perform any namespace checking.
     *
     * @param element The element to check against.
     * @param executionContext The current execution context.
     * @return True if this resource configuration is targeted at the specified
     *         element in context, otherwise false.
     */
    private boolean isTargetedAtElementContext(SAXElement element, ExecutionContext executionContext) {
        SAXElement currentElement = element;
        ContextIndex index = new ContextIndex(executionContext);

        index.i = selectorSteps.length - 1;

        // Unless it's **, start at the parent because the current element
        // has already been tested...
        if(!selectorSteps[index.i].isStarStar()) {
            index.i = selectorSteps.length - 2;
            currentElement = element.getParent();
        } else {
        	// The target selector step is "**".  If the parent one is "#document" and we're at
        	// the root now, then fail...
        	if(selectorSteps.length == 2 && selectorSteps[0].isRooted() && element.getParent() == null) {
        		return false;
        	}
        }

        if (currentElement == null) {
            return false;
        }

        // Check the element name(s).
        while (index.i >= 0) {
            SAXElement parentElement = currentElement.getParent();

            if(!isTargetedAtElementContext(currentElement, parentElement, index)) {
                return false;
            }

            if (parentElement == null) {
                return true;
            }

            currentElement = parentElement;
        }

        return true;
    }

    private boolean isTargetedAtElementContext(Element element, Element parentElement, ContextIndex index) {
        if(selectorSteps[index.i].isRooted() && parentElement != null) {
            return false;
        } else if (selectorSteps[index.i].isStar()) {
            index.i--;
        } else if (selectorSteps[index.i].isStarStar()) {
            if(index.i == 0) {
                // No more tokens to match and ** matches everything
                return true;
            } else if(index.i == 1) {
                SelectorStep parentStep = selectorSteps[index.i - 1];

                if(parentElement == null && parentStep.isRooted()) {
                    // we're at the root of the document and the only selector left is
                    // the document selector.  Pass..
                    return true;
                } else
                	if(parentElement == null) {
                    // we're at the root of the document, yet there are still
                    // unmatched tokens in the selector.  Fail...
                    return false;
                }
            } else if(parentElement == null) {
                // we're at the root of the document, yet there are still
                // unmatched tokens in the selector.  Fail...
                return false;
            }

            SelectorStep parentStep = selectorSteps[index.i - 1];

            if(parentStep.isTargetedAtElement(parentElement)) {
                if(!parentStep.isStarStar()) {
                    XPathExpressionEvaluator evaluator = parentStep.getPredicatesEvaluator();
                    if(evaluator == null) {
                        LOGGER.debug("Predicate Evaluators for resource [" + this + "] is null.  XPath step predicates will not be evaluated.");
                    } else if(!evaluator.evaluate(parentElement, index.executionContext)) {
                        return false;
                    }
                }
                index.i--;
            }
        } else if (!selectorSteps[index.i].isTargetedAtElement(element)) {
            return false;
        } else {
            if(!selectorSteps[index.i].isStarStar()) {
                XPathExpressionEvaluator evaluator = selectorSteps[index.i].getPredicatesEvaluator();
                if(evaluator == null) {
                    LOGGER.debug("Predicate Evaluators for resource [" + this + "] is null.  XPath step predicates will not be evaluated.");
                } else if(!evaluator.evaluate(element, index.executionContext)) {
                    return false;
                }
            }
            index.i--;
        }

        if (parentElement == null) {
            if(index.i >= 0 && !selectorSteps[index.i].isStarStar()) {
                return selectorSteps[index.i].isRooted();
            }
        }

        return true;
    }

    private boolean isTargetedAtElementContext(SAXElement element, SAXElement parentElement, ContextIndex index) {
        if(selectorSteps[index.i].isRooted() && parentElement != null) {
            return false;
        } else if (selectorSteps[index.i].isStar()) {
            index.i--;
        } else if (selectorSteps[index.i].isStarStar()) {
            if(index.i == 0) {
                // No more tokens to match and ** matches everything
                return true;
            } else if(index.i == 1) {
                SelectorStep parentStep = selectorSteps[index.i - 1];

                if(parentElement == null && parentStep.isRooted()) {
                    // we're at the root of the document and the only selector left is
                    // the document selector.  Pass..
                    return true;
                } else
                	if(parentElement == null) {
                    // we're at the root of the document, yet there are still
                    // unmatched tokens in the selector.  Fail...
                    return false;
                }
            } else if(parentElement == null) {
                // we're at the root of the document, yet there are still
                // unmatched tokens in the selector.  Fail...
                return false;
            }

            SelectorStep parentStep = selectorSteps[index.i - 1];

            if(parentStep.isTargetedAtElement(parentElement)) {
                if(!parentStep.isStarStar()) {
                    XPathExpressionEvaluator evaluator = parentStep.getPredicatesEvaluator();
                    if(evaluator == null) {
                        LOGGER.debug("Predicate Evaluators for resource [" + this + "] is null.  XPath step predicates will not be evaluated.");
                    } else if(!evaluator.evaluate(parentElement, index.executionContext)) {
                        return false;
                    }
                }
                index.i--;
            }
        } else if (!selectorSteps[index.i].isTargetedAtElement(element)) {
            return false;
        } else {
            if(!selectorSteps[index.i].isStarStar()) {
                XPathExpressionEvaluator evaluator = selectorSteps[index.i].getPredicatesEvaluator();
                if(evaluator == null) {
                    LOGGER.debug("Predicate Evaluators for resource [" + this + "] is null.  XPath step predicates will not be evaluated.");
                } else if(!evaluator.evaluate(element, index.executionContext)) {
                    return false;
                }
            }
            index.i--;
        }

        if (parentElement == null) {
            if(index.i >= 0 && !selectorSteps[index.i].isStarStar()) {
                return selectorSteps[index.i].isRooted();
            }
        }

        return true;
    }

    /**
     * Is this configuration targeted at the supplied DOM element.
     * <p/>
     * Checks that the element is in the correct namespace and is a contextual
     * match for the configuration.
     *
     * @param element The element to be checked.
     * @param executionContext The current execution context.
     * @return True if this configuration is targeted at the supplied element, otherwise false.
     */
    public boolean isTargetedAtElement(Element element, ExecutionContext executionContext) {
        if (!assertConditionTrue()) {
            return false;
        }

        if (namespaceURI != null) {
            if(!isTargetedAtNamespace(element.getNamespaceURI())) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Not applying resource [" + this + "] to element [" + DomUtils.getXPath(element) + "].  Element not in namespace [" + getSelectorNamespaceURI() + "].");
                }
                return false;
            }
        } else {
            // We don't test the SelectorStep namespace if a namespace is configured on the
            // resource configuration.  This is why we have this code inside the else block.
            if(!selectorStep.isTargetedAtNamespace(element.getNamespaceURI())) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Not applying resource [" + this + "] to element [" + DomUtils.getXPath(element) + "].  Element not in namespace [" + selectorStep.getTargetElement().getNamespaceURI() + "].");
                }
                return false;
            }
        }

        XPathExpressionEvaluator evaluator = selectorStep.getPredicatesEvaluator();
        if(evaluator == null) {
            LOGGER.debug("Predicate Evaluators for resource [" + this + "] is null.  XPath step predicates will not be evaluated.");
        } else if(!evaluator.evaluate(element, executionContext)) {
            return false;
        }

        if (isContextualSelector && !isTargetedAtElementContext(element, executionContext)) {
            // Note: If the selector is not contextual, there's no need to perform the
            // isTargetedAtElementContext check because we already know the unit is targeted at the
            // element by name - because we looked it up by name in the 1st place (at least that's the assumption).
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Not applying resource [" + this + "] to element [" + DomUtils.getXPath(element) + "].  This resource is only targeted at '" + DomUtils.getName(element) + "' when in the following context '" + getSelector() + "'.");
            }
            return false;
        }

        return true;
    }

    /**
     * Is this configuration targeted at the supplied SAX element.
     * <p/>
     * Checks that the element is in the correct namespace and is a contextual
     * match for the configuration.
     *
     * @param element The element to be checked.
     * @param executionContext The current execution context.
     * @return True if this configuration is targeted at the supplied element, otherwise false.
     */
    public boolean isTargetedAtElement(SAXElement element, ExecutionContext executionContext) {
        if (expressionEvaluator != null && !assertConditionTrue()) {
            return false;
        }

        if (namespaceURI != null) {
            if(!isTargetedAtNamespace(element.getName().getNamespaceURI())) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Not applying resource [" + this + "] to element [" + element.getName() + "].  Element not in namespace [" + namespaceURI + "].");
                }
                return false;
            }
        } else {
            // We don't test the SelectorStep namespace if a namespace is configured on the
            // resource configuration.  This is why we have this code inside the else block.
            if(!selectorStep.isTargetedAtNamespace(element.getName().getNamespaceURI())) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Not applying resource [" + this + "] to element [" + element.getName() + "].  Element not in namespace [" + selectorStep.getTargetElement().getNamespaceURI() + "].");
                }
                return false;
            }
        }

        XPathExpressionEvaluator evaluator = selectorStep.getPredicatesEvaluator();
        if(evaluator == null) {
            LOGGER.debug("Predicate Evaluators for resource [" + this + "] is null.  XPath step predicates will not be evaluated.");
        } else if(!evaluator.evaluate(element, executionContext)) {
            return false;
        }

        if (isContextualSelector && !isTargetedAtElementContext(element, executionContext)) {
            // Note: If the selector is not contextual, there's no need to perform the
            // isTargetedAtElementContext check because we already know the visitor is targeted at the
            // element by name - because we looked it up by name in the 1st place (at least that's the assumption).
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Not applying resource [" + this + "] to element [" + element.getName() + "].  This resource is only targeted at '" + element.getName().getLocalPart() + "' when in the following context '" + getSelector() + "'.");
            }
            return false;
        }

        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean assertConditionTrue() {
        if (expressionEvaluator == null) {
            return true;
        }

        ExecutionContextExpressionEvaluator evaluator = (ExecutionContextExpressionEvaluator) expressionEvaluator;
        ExecutionContext execContext = Filter.getCurrentExecutionContext();

        return evaluator.eval(execContext);
    }

    /**
     * Add the specified change listener to the list of change listeners.
     * @param listener The listener instance.
     */
    public void addChangeListener(SmooksResourceConfigurationChangeListener listener) {
        changeListeners.add(listener);
    }

    /**
     * Remove the specified change listener from the list of change listeners.
     * @param listener The listener instance.
     */
    public void removeChangeListener(SmooksResourceConfigurationChangeListener listener) {
        changeListeners.remove(listener);
    }

    /**
     * Generate an XML'ified description of this resource.
     *
     * @return XML'ified description of the resource.
     */
    public String toXML() {
        StringBuilder builder = new StringBuilder();

        builder.append("<resource-config selector=\"")
               .append(selector)
               .append("\"");
        if (namespaceURI != null) {
            builder.append(" selector-namespace=\"")
                   .append(namespaceURI)
                   .append("\"");
        }
        if (targetProfile != null && !targetProfile.equals(Profile.DEFAULT_PROFILE)) {
            builder.append(" target-profile=\"")
                   .append(targetProfile)
                   .append("\"");
        }
        builder.append(">\n");

        if (resource != null) {
            String resourceStartEl;
            if (resourceType != null) {
                resourceStartEl = "<resource type=\"" + resourceType + "\">";
            } else {
                resourceStartEl = "<resource>";
            }

            builder.append("\t")
                   .append(resourceStartEl);
            if (resource.length() < 300) {
               builder.append(resource)
                       .append("</resource>\n");
            } else {
               builder.append(resource, 0, 300)
                       .append(" ... more</resource>\n");
            }
        }

        if (expressionEvaluator != null) {
            builder.append("\t<condition evaluator=\"").append(expressionEvaluator.getClass().getName()).append("\">").append(expressionEvaluator.getExpression())
                   .append("</condition>\n");
        }

        if (parameters != null) {
            Set<String> paramNames = parameters.keySet();
            for (String paramName : paramNames) {
                List params = getParameters(paramName);
                for (Object param : params) {
                    Element element = ((Parameter) param).getXml();
                    Object value;

                    if (element != null) {
                        value = XmlUtil.serialize(element.getChildNodes());
                    } else {
                        value = ((Parameter) param).getValue();
                    }
                    builder.append("\t<param name=\"")
                           .append(paramName)
                           .append("\">")
                           .append(value)
                           .append("</param>\n");
                }
            }
        }

        builder.append("</resource-config>");

        return builder.toString();
    }

    @Override
    public final boolean equals(Object obj) {
        // Do not override this method !!
        return super.equals(obj);
    }

    /**
     * Create a {@link Properties} instance from this supplied {@link org.smooks.cdr.SmooksResourceConfiguration}
     * @return The resource parameters as a {@link Properties} instance.
     */
    public Properties toProperties() {
        Properties properties = new Properties();
        Set<String> names = parameters.keySet();

        for(String name : names) {
            properties.setProperty(name, getParameterValue(name).toString());
        }

        return properties;
    }

    private String extractTargetElement(String[] contextualSelector) {
        if (contextualSelector != null) {
            String token = contextualSelector[contextualSelector.length - 1];
            if(token.startsWith("@")) {
                if(contextualSelector.length > 1) {
                    token = contextualSelector[contextualSelector.length - 2];
                }
            }
            return token;
        } else {
            return null;
        }
    }

    public static String extractTargetAttribute(String[] selectorTokens) {
        StringBuilder selectorProp = new StringBuilder();

        for (String selectorToken : selectorTokens) {
            if (selectorToken.trim().startsWith("@")) {
                selectorProp.append(selectorToken.substring(1));
            }
        }

        if(selectorProp.length() == 0) {
            return null;
        }

        return selectorProp.toString();
    }

    private class ContextIndex {
        private int i;
        private ExecutionContext executionContext;

        public ContextIndex(ExecutionContext executionContext) {
            this.executionContext = executionContext;
        }
    }

    private class LegacySelectorStep extends SelectorStep {
        public LegacySelectorStep(String selector, String targetElementName) {
            super(selector, targetElementName);
        }
        public LegacySelectorStep(String xpathExpression, String targetElementName, String targetAttributeName) {
            super(xpathExpression, targetElementName, targetAttributeName);
        }
        public XPathExpressionEvaluator getPredicatesEvaluator() {
            return PassThruEvaluator.INSTANCE;
        }
        @SuppressWarnings("RedundantThrows")
        public void buildPredicatesEvaluator(Properties namespaces) throws SAXPathException, NotFoundException, CannotCompileException, IllegalAccessException, InstantiationException {
            // Ignore this.
        }
    }

    private void fireChangedEvent() {
        if(!changeListeners.isEmpty()) {
            for(SmooksResourceConfigurationChangeListener listener : changeListeners) {
                listener.changed(this);
            }
        }
    }
}
