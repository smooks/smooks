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
import org.smooks.cdr.xpath.SelectorPath;
import org.smooks.classpath.ClasspathUtils;
import org.smooks.delivery.Visitor;
import org.smooks.delivery.dom.serialize.DOMSerializerVisitor;
import org.smooks.delivery.dom.serialize.DefaultDOMSerializerVisitor;
import org.smooks.io.StreamUtils;
import org.smooks.profile.Profile;
import org.smooks.resource.URIResourceLocator;
import org.smooks.util.ClassUtil;
import org.smooks.xml.XmlUtil;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
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
 * &lt;smooks-resource-list xmlns="https://www.smooks.org/xsd/smooks-2.0.xsd"&gt;
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
 * &lt;smooks-resource-list xmlns="https://www.smooks.org/xsd/smooks-2.0.xsd"&gt;
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
    private static final URIResourceLocator uriResourceLocator = new URIResourceLocator();
    /**
     * A special selector for resource targeted at the document as a whole (the roor element).
     */
    public static final String DOCUMENT_FRAGMENT_SELECTOR = "#document";

    /**
     * A special selector for resource targeted at the document as a whole (the roor element).
     */
    public static final String DOCUMENT_VOID_SELECTOR = "$void";
    
    /**
     * Selector steps.
     */
    private SelectorPath selectorPath = new SelectorPath();
    
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
     * Is this resource defined inline in the configuration, or is it
     * referenced through a URI.
     */
    private boolean isInline = false;
  
    /**
     * The type of the resource.  "class", "groovy", "xsl" etc....
     */
    private String resourceType;
   
    /**
     * SmooksResourceConfiguration parameters - String name and String value.
     */
    private LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
    private int parameterCount;
   
    /**
     * Flag indicating whether or not the resource is a default applied resource
     * e.g. {@link DefaultDOMSerializerVisitor} or
     * {@link org.smooks.delivery.sax.DefaultSAXElementSerializer}.
     */
    private boolean defaultResource = false;
    /**
     * The extended config namespace from which the  resource was created.
     */
    @Deprecated
    private String extendedConfigNS;
    /**
     * Change listeners.
     */
    private final Set<SmooksResourceConfigurationChangeListener> changeListeners = new HashSet<SmooksResourceConfigurationChangeListener>();

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
        clone.selectorPath = selectorPath.clone();
        clone.targetProfile = targetProfile;
        clone.defaultResource = defaultResource;
        clone.profileTargetingExpressionStrings = profileTargetingExpressionStrings;
        clone.profileTargetingExpressions = profileTargetingExpressions;
        clone.resource = resource;
        clone.isInline = isInline;
        clone.resourceType = resourceType;
        if (parameters != null) {
            clone.parameters = (LinkedHashMap<String, Object>) parameters.clone();
        }
        clone.parameterCount = parameterCount;

        return clone;
    }

    /**
     * Get the extended config namespace from which this configuration was created.
	 * @return The extended config namespace from which this configuration was created.
	 */
    @Deprecated
    public String getExtendedConfigNS() {
        return extendedConfigNS;
    }

    /**
     * Set the extended config namespace from which this configuration was created.
	 * @param extendedConfigNS The extended config namespace from which this configuration was created.
	 */
    @Deprecated
	public void setExtendedConfigNS(String extendedConfigNS) {
		this.extendedConfigNS = extendedConfigNS;
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
    public SmooksResourceConfiguration(String selector, @Deprecated String selectorNamespaceURI, String targetProfile, String resource) {
        this(selector, targetProfile, resource);
        selectorPath.setSelectorNamespaceURI(selectorNamespaceURI);
    }

    /**
     * Set the config selector.
     *
     * @param selector The selector definition.
     */
    public void setSelector(final String selector) {
        if (selector != null) {
            selectorPath.setSelector(selector);
        } else {
            selectorPath.setSelector(SELECTOR_NONE);
        }

        fireChangedEvent();
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
     * Set the selector steps.
     * @param selectorPath The selector steps.
     */
    public void setSelectorPath(SelectorPath selectorPath) {
        this.selectorPath = selectorPath;
        fireChangedEvent();
    }

    /**
     * Get the selector steps.
     * @return The selector steps.
     */
    public SelectorPath getSelectorPath() {
        return selectorPath;
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
     * Is this resource config a default applied resource.
     * <p/>
     * Some resources (e.g. {@link DOMSerializerVisitor} or
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
     * Some resources (e.g. {@link DOMSerializerVisitor} or
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
            throw new IllegalArgumentException("Empty 'target-profile'. [" + selectorPath.getSelector() + "][" + resource + "]");
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
        return selectorPath.getSelector().startsWith(XML_DEF_PREFIX);
    }

    /* (non-Javadoc)
      * @see java.lang.Object#toString()
      */
    public String toString() {
        return "Target Profile: [" + Arrays.asList(profileTargetingExpressionStrings) + "], Selector: [" + selectorPath.getSelector() + "], Selector Namespace URI: [" + selectorPath.getSelectorNamespaceURI() + "], Resource: [" + resource + "], Num Params: [" + getParameterCount() + "]";
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
        // Ala DTD v2.0, where the <resource> element can carry the inlined resource data.
        return resource.getBytes(StandardCharsets.UTF_8);
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
               .append(selectorPath.getSelector())
               .append("\"");
        if (selectorPath.getSelectorNamespaceURI() != null) {
            builder.append(" selector-namespace=\"")
                   .append(selectorPath.getSelectorNamespaceURI())
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

        if (selectorPath.getConditionEvaluator() != null) {
            builder.append("\t<condition evaluator=\"").append(selectorPath.getConditionEvaluator().getClass().getName()).append("\">").append(selectorPath.getConditionEvaluator().getExpression())
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

    private void fireChangedEvent() {
        if(!changeListeners.isEmpty()) {
            for(SmooksResourceConfigurationChangeListener listener : changeListeners) {
                listener.changed(this);
            }
        }
    }
}
