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
package org.smooks.engine.resource.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.profile.Profile;
import org.smooks.api.resource.config.*;
import org.smooks.api.resource.config.xpath.SelectorPath;
import org.smooks.api.resource.reader.SmooksXMLReader;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.api.resource.visitor.dom.DOMElementVisitor;
import org.smooks.classpath.ClasspathUtils;
import org.smooks.engine.delivery.dom.serialize.DOMSerializerVisitor;
import org.smooks.engine.delivery.dom.serialize.DefaultDOMSerializerVisitor;
import org.smooks.engine.resource.config.xpath.SelectorPathFactory;
import org.smooks.resource.URIResourceLocator;
import org.smooks.support.ClassUtil;
import org.smooks.support.StreamUtils;
import org.smooks.support.XmlUtil;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SuppressWarnings({ "WeakerAccess", "unused", "deprecation", "unchecked" })
public class DefaultResourceConfig implements ResourceConfig {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultResourceConfig.class);
    
    /**
     * URI resource locator.
     */
    private static final URIResourceLocator uriResourceLocator = new URIResourceLocator();
    
    /**
     * Selector steps.
     */
    private SelectorPath selectorPath;
    
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
    private boolean isInline;
  
    /**
     * The type of the resource.  "class", "groovy", "xsl" etc....
     */
    private String resourceType;
   
    /**
     * ResourceConfig parameters - String name and String value.
     */
    private LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
    private int parameterCount;
   
    /**
     * Flag indicating whether or not the resource is a default applied resource
     * e.g. {@link DefaultDOMSerializerVisitor} or
     * {@link DefaultSAXElementSerializer}.
     */
    private boolean defaultResource;
    /**
     * The extended config namespace from which the  resource was created.
     */
    @Deprecated
    private String extendedConfigNS;
    /**
     * Change listeners.
     */
    private final Set<ResourceConfigChangeListener> changeListeners = new HashSet<>();

    /**
     * Public default constructor.
     *
     * @see #setSelector(String)
     * @see #setTargetProfile(String)
     * @see #setResource(String)
     * @see #setResourceType(String)
     * @see #setParameter(String, Object)
     */
    public DefaultResourceConfig() {
        setSelector(SELECTOR_NONE, new Properties());
        setTargetProfile(Profile.DEFAULT_PROFILE);
    }

    /**
     * Public constructor.
     *
     * @param selector The selector definition.
     * @see #setTargetProfile(String)
     * @see #setResource(String)
     * @see #setResourceType(String)
     * @see #setParameter(String, Object)
     */
    public DefaultResourceConfig(String selector, Properties namespaces) {
        setSelector(selector, namespaces);
        setTargetProfile(Profile.DEFAULT_PROFILE);
    }
    
    public DefaultResourceConfig(ResourceConfig resourceConfig) {
        setTargetProfile(resourceConfig.getTargetProfile());
        setResource(resourceConfig.getResource());
        setSelectorPath(SelectorPathFactory.newSelectorPath(resourceConfig.getSelectorPath()));
        setDefaultResource(resourceConfig.isDefaultResource());
    }

    /**
     * Public constructor.
     *
     * @param selector The selector definition.
     * @param resource The resource.
     * @see #setTargetProfile(String)
     * @see #setResourceType(String)
     * @see #setParameter(String, Object)
     */
    public DefaultResourceConfig(String selector, Properties namespaces, String resource) {
        this(selector, namespaces, Profile.DEFAULT_PROFILE, resource);
    }

    /**
     * Public constructor.
     *
     * @param selector      The selector definition.
     * @param targetProfile Target Profile(s).  Comma separated list of
     *                      {@link ProfileTargetingExpression ProfileTargetingExpressions}.
     * @param resource      The resource.
     * @see #setResourceType(String)
     * @see #setParameter(String, Object)
     */
    public DefaultResourceConfig(String selector, Properties namespaces, String targetProfile, String resource) {
        this(selector, namespaces);

        setTargetProfile(targetProfile);
        setResource(resource);
    }

    /**
     * Perform a shallow clone of this configuration.
     *
     * @return Configuration clone.
     */
    @Override
    @SuppressWarnings({ "MethodDoesntCallSuperMethod", "unchecked" })
    public ResourceConfig copy() {
        DefaultResourceConfig copyResourceConfig = new DefaultResourceConfig();

        copyResourceConfig.extendedConfigNS = extendedConfigNS;
        copyResourceConfig.selectorPath = SelectorPathFactory.newSelectorPath(selectorPath);
        copyResourceConfig.targetProfile = targetProfile;
        copyResourceConfig.defaultResource = defaultResource;
        copyResourceConfig.profileTargetingExpressionStrings = profileTargetingExpressionStrings;
        copyResourceConfig.profileTargetingExpressions = profileTargetingExpressions;
        copyResourceConfig.resource = resource;
        copyResourceConfig.isInline = isInline;
        copyResourceConfig.resourceType = resourceType;
        if (parameters != null) {
            copyResourceConfig.parameters = (LinkedHashMap<String, Object>) parameters.clone();
        }
        copyResourceConfig.parameterCount = parameterCount;

        return copyResourceConfig;
    }

    /**
     * Get the extended config namespace from which this configuration was created.
	 * @return The extended config namespace from which this configuration was created.
	 */
    @Override
    @Deprecated
    public String getExtendedConfigNS() {
        return extendedConfigNS;
    }

    /**
     * Set the extended config namespace from which this configuration was created.
	 * @param extendedConfigNS The extended config namespace from which this configuration was created.
	 */
    @Override
    @Deprecated
	public void setExtendedConfigNS(String extendedConfigNS) {
		this.extendedConfigNS = extendedConfigNS;
	}
	
	@Override
    public void addParameters(ResourceConfig resourceConfig) {
        parameters.putAll(resourceConfig.getParameters());
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
     * @see #setParameter(String, Object)
     */
    public DefaultResourceConfig(String selector, Properties namespaces, @Deprecated String selectorNamespaceURI, String targetProfile, String resource) {
        this(selector, namespaces, targetProfile, resource);
        selectorPath.setSelectorNamespaceURI(selectorNamespaceURI);
    }

    /**
     * Set the config selector.
     *
     * @param selector The selector definition.
     */
    @Override
    public void setSelector(final String selector, final Properties namespaces) {
        if (selector != null) {
            if (selectorPath == null) {
                selectorPath = SelectorPathFactory.newSelectorPath(selector, namespaces);
            } else {
                selectorPath = SelectorPathFactory.newSelectorPath(selector, selectorPath);
                selectorPath.getNamespaces().putAll(namespaces);
            }
        } else {
            selectorPath = SelectorPathFactory.newSelectorPath(SELECTOR_NONE, namespaces);
        }

        fireChangedEvent();
    }

    /**
     * Set the configs "resource".
     *
     * @param resource The resource.
     */
    @Override
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
    @Override
    public boolean isInline() {
        return isInline;
    }

    /**
     * Get the target profile string as set in the configuration.
     *
     * @return The target profile.
     */
    @Override
    public String getTargetProfile() {
        return targetProfile;
    }

    /**
     * Set the configs "target profile".
     *
     * @param targetProfile Target Profile(s).  Comma separated list of
     *                      {@link ProfileTargetingExpression ProfileTargetingExpressions}.
     */
    @Override
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
    @Override
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    /**
     * Set the selector steps.
     * @param selectorPath The selector steps.
     */
    @Override
    public void setSelectorPath(SelectorPath selectorPath) {
        this.selectorPath = selectorPath;
        fireChangedEvent();
    }

    /**
     * Get the selector steps.
     * @return The selector steps.
     */
    @Override
    public SelectorPath getSelectorPath() {
        return selectorPath;
    }

    /**
     * Get the profile targeting expressions for this ResourceConfig.
     *
     * @return The profile targeting expressions.
     */
    @Override
    public ProfileTargetingExpression[] getProfileTargetingExpressions() {
        return profileTargetingExpressions;
    }

    /**
     * Get the resource for this ResourceConfig.
     *
     * @return The cdrar path.
     */
    @Override
    public String getResource() {
        return resource;
    }
    
    /**
     * Is this resource config a default applied resource.
     * <p/>
     * Some resources (e.g. {@link DOMSerializerVisitor} or
     * {@link DefaultSAXElementSerializer}) are applied by default when no other
     * resources are targeted at an element.
     *
     * @return True if this is a default applied resource, otherwise false.
     */
    @Override
    public boolean isDefaultResource() {
        return defaultResource;
    }

    /**
     * Set this resource config as a default applied resource.
     * <p/>
     * Some resources (e.g. {@link DOMSerializerVisitor} or
     * {@link DefaultSAXElementSerializer}) are applied by default when no other
     * resources are targeted at an element.
     *
     * @param defaultResource True if this is a default applied resource, otherwise false.
     */
    @Override
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
    @Override
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
            profileTargetingExpressions = new DefaultProfileTargetingExpression[tokenizer.countTokens()];
            for (int i = 0; tokenizer.hasMoreTokens(); i++) {
                String expression = tokenizer.nextToken();
                this.profileTargetingExpressionStrings[i] = expression;
                profileTargetingExpressions[i] = new DefaultProfileTargetingExpression(expression);
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
     * Set the named ResourceConfig parameter value (default type - String).
     * <p/>
     * Overwrites previous value of the same name.
     *
     * @param name  Parameter name.
     * @param value Parameter value.
     * @return The parameter instance.
     */
    @Override
    public <T> Parameter<T> setParameter(String name, T value) {
        Parameter<T> param = new DefaultParameter(name, value);
        setParameter(param);
        return param;
    }

    /**
     * Set the named ResourceConfig parameter value (with type).
     * <p/>
     * Overwrites previous value of the same name.
     *
     * @param name  Parameter name.
     * @param type  Parameter type.
     * @param value Parameter value.
     * @return The parameter instance.
     */
    @Override
    public <T> Parameter<T> setParameter(String name, String type, T value) {
        Parameter<T> param = new DefaultParameter<>(name, value, type);
        setParameter(param);
        return param;
    }

    @Override
    public <T> void setParameter(Parameter<T> parameter) {
        if (parameters == null) {
            parameters = new LinkedHashMap<>();
        }
        Object exists = parameters.get(parameter.getName());

        if (exists == null) {
            parameters.put(parameter.getName(), parameter);
        } else if (exists instanceof Parameter) {
            Vector<Parameter<?>> paramList = new Vector<>();
            paramList.add((Parameter<?>) exists);
            paramList.add(parameter);
            parameters.put(parameter.getName(), paramList);
        } else if (exists instanceof List) {
            ((List<Object>) exists).add(parameter);
        }
        parameterCount++;
    }

    /**
     * Get the named ResourceConfig {@link Parameter parameter}.
     * <p/>
     * If there is more than one of the named parameters defined, the first
     * defined value is returned.
     *
     * @param name Name of parameter to get.
     * @return Parameter value, or null if not set.
     */
    @Override
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
    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Get all {@link Parameter parameter} values set on this configuration.
     *
     * @return {@link Parameter} value {@link List}, or null if not set.
     */
    @Override
    public List<?> getParameterValues() {
        if (parameters == null) {
            return Collections.emptyList();
        } else {
            return new ArrayList<>(parameters.values());
        }
    }

    /**
     * Get the named ResourceConfig {@link Parameter parameter} List.
     *
     * @param name Name of parameter to get.
     * @return {@link Parameter} value {@link List}, or null if not set.
     */
    @Override
    public List<Parameter<?>> getParameters(String name) {
        if (parameters != null) {
            Object parameter = parameters.get(name);

            if (parameter instanceof List) {
                return (List<Parameter<?>>) parameter;
            } else if (parameter instanceof Parameter) {
                List<Parameter<?>> paramList = new ArrayList<>();
                paramList.add((Parameter<?>) parameter);

                return paramList;
            }
        }
        return Collections.emptyList();
    }


    @Override
    public Object getParameterValue(String name) {
        Parameter<Object> parameter = getParameter(name, Object.class);
        return (parameter != null ? parameter.getValue() : null);
    }
    
    /**
     * Get the named ResourceConfig parameter.
     *
     * @param name Name of parameter to get.
     * @return Parameter value, or null if not set.
     */
    @Override
    public <T> T getParameterValue(String name, Class<T> valueClass) {
        Parameter<T> parameter = getParameter(name, valueClass);
        return (parameter != null ? parameter.getValue() : null);
    }

    /**
     * Get the named ResourceConfig parameter.
     *
     * @param name       Name of parameter to get.
     * @param defaultValue The default value to be returned if there are no
     *                   parameters on the this ResourceConfig instance, or the parameter is not defined.
     * @return Parameter value, or defaultVal if not defined.
     */
    @Override
    public <T> T getParameterValue(String name, Class<T> valueClass, T defaultValue) {
        T value = getParameterValue(name, valueClass);

        return (value != null ? value : defaultValue);
    }

    /**
     * Get the ResourceConfig parameter count.
     *
     * @return Number of parameters defined on this ResourceConfig.
     */
    @Override
    public int getParameterCount() {
        return parameterCount;
    }

    /**
     * Remove the named parameter.
     *
     * @param name The name of the parameter to be removed.
     */
    @Override
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
    @Override
    public boolean isXmlDef() {
        return selectorPath.getSelector().startsWith(XML_DEF_PREFIX);
    }
    
    @Override
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
    @Override
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
    protected Class<?> toJavaResource() {
        String className;

        if (resource == null || resource.trim().length() < 1) {
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
    @Override
    public boolean isJavaResource() {
        return (toJavaResource() != null);
    }

    /**
     * Add the specified change listener to the list of change listeners.
     * @param listener The listener instance.
     */
    @Override
    public void addChangeListener(ResourceConfigChangeListener listener) {
        changeListeners.add(listener);
    }

    /**
     * Remove the specified change listener from the list of change listeners.
     * @param listener The listener instance.
     */
    @Override
    public void removeChangeListener(ResourceConfigChangeListener listener) {
        changeListeners.remove(listener);
    }

    /**
     * Generate an XML'ified description of this resource.
     *
     * @return XML'ified description of the resource.
     */
    @Override
    public String toXml() {
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
                        value = XmlUtil.serialize(element.getChildNodes(), true);
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
     * Create a {@link Properties} instance from this supplied {@link DefaultResourceConfig}
     * @return The resource parameters as a {@link Properties} instance.
     */
    @Override
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
            for(ResourceConfigChangeListener listener : changeListeners) {
                listener.changed(this);
            }
        }
    }
}
