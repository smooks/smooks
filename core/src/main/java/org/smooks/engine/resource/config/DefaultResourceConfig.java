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
import org.smooks.api.resource.config.Parameter;
import org.smooks.api.resource.config.ProfileTargetingExpression;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.config.ResourceConfigChangeListener;
import org.smooks.api.resource.config.xpath.SelectorPath;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

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


    @Override
    public ResourceConfig copy() {
        DefaultResourceConfig copyResourceConfig = new DefaultResourceConfig();

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

	@Override
    public void addParameters(ResourceConfig resourceConfig) {
        parameters.putAll(resourceConfig.getParameters());
    }


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

    @Override
    public boolean isInline() {
        return isInline;
    }

    @Override
    public String getTargetProfile() {
        return targetProfile;
    }

    @Override
    public void setTargetProfile(String targetProfile) {
        if (targetProfile == null || targetProfile.trim().equals("")) {
            // Default the target profile to everything if not specified.
            targetProfile = Profile.DEFAULT_PROFILE;
        }
        this.targetProfile = targetProfile;
        parseTargetingExpressions(targetProfile);
    }

    @Override
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    @Override
    public void setSelectorPath(SelectorPath selectorPath) {
        this.selectorPath = selectorPath;
        fireChangedEvent();
    }

    @Override
    public SelectorPath getSelectorPath() {
        return selectorPath;
    }

    @Override
    public ProfileTargetingExpression[] getProfileTargetingExpressions() {
        return profileTargetingExpressions;
    }

    @Override
    public String getResource() {
        return resource;
    }

    @Override
    public boolean isDefaultResource() {
        return defaultResource;
    }

    @Override
    public void setDefaultResource(boolean defaultResource) {
        this.defaultResource = defaultResource;
    }

    @Override
    public String getResourceType() {
        if (isJavaResource()) {
            return "class";
        }

        final String resourceType;
        if (this.resourceType != null) {
            // Ala DTD v2.0, where the type is set through the "type" attribute on the <resource> element.
            resourceType = this.resourceType;
        } else {
            resourceType = getExtension(getResource());
        }

        return resourceType;
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

    @Override
    public <T> Parameter<T> setParameter(String name, T value) {
        Parameter<T> param = new DefaultParameter<>(name, value);
        setParameter(param);
        return param;
    }

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

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public List<?> getParameterValues() {
        if (parameters == null) {
            return Collections.emptyList();
        } else {
            return new ArrayList<>(parameters.values());
        }
    }

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

    @Override
    public <T> T getParameterValue(String name, Class<T> valueClass) {
        Parameter<T> parameter = getParameter(name, valueClass);
        return (parameter != null ? parameter.getValue() : null);
    }

    @Override
    public <T> T getParameterValue(String name, Class<T> valueClass, T defaultValue) {
        T value = getParameterValue(name, valueClass);

        return (value != null ? value : defaultValue);
    }

    @Override
    public int getParameterCount() {
        return parameterCount;
    }

    @Override
    public void removeParameter(String name) {
        parameters.remove(name);
    }
    
    @Override
    public String toString() {
        return "Target Profile: [" + Arrays.asList(profileTargetingExpressionStrings) + "], Selector: [" + selectorPath.getSelector() + "], Resource: [" + resource + "], Num Params: [" + getParameterCount() + "]";
    }

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

    @Override
    public boolean isJavaResource() {
        return (toJavaResource() != null);
    }

    @Override
    public void addChangeListener(ResourceConfigChangeListener resourceConfigChangeListener) {
        changeListeners.add(resourceConfigChangeListener);
    }

    @Override
    public void removeChangeListener(ResourceConfigChangeListener resourceConfigChangeListener) {
        changeListeners.remove(resourceConfigChangeListener);
    }

    @Override
    public String toXml() {
        StringBuilder builder = new StringBuilder();

        builder.append("<resource-config selector=\"")
               .append(selectorPath.getSelector())
               .append("\"");
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
