/*-
 * ========================LICENSE_START=================================
 * API
 * %%
 * Copyright (C) 2020 - 2021 Smooks
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
package org.smooks.api.resource.config;

import org.smooks.api.resource.config.xpath.SelectorPath;
import org.smooks.api.resource.reader.SmooksXMLReader;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.api.resource.visitor.dom.DOMElementVisitor;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Configuration of a Smooks resource.
 * <br/><br/>
 * <code>ResourceConfig</code> drives the behaviour of a Smooks resource. It defines:
 * <ul>
 *     <li>which event/s from the event stream target the resource</li>
 *     <li>the parameters injected into the resource</li>
 *     <li>the type of resource that is instantiated</li>
 * </ul>
 * <p>
 * A <code>ResourceConfig</code> can be assembled with Java code but it is more convenient to configure one through XML.
 * The following are some resource configurations described in XML.
 * </p>
 * <br/>
 * <h3>Basic Sample</h3>
 * <pre>
 * &lt;?xml version='1.0'?&gt;
 * &lt;smooks-resource-list xmlns="https://www.smooks.org/xsd/smooks-2.0.xsd"&gt;
 *
 * (1) &lt;resource-config <a href="#selector">selector</a>="order/order-header"&gt;
 *      &lt;resource type="xsl"&gt;<a target="new" href="https://www.smooks.org#Smooks-smookscartridges">/com/acme/transform/OrderHeaderTransformer.xsl</a>&lt;/resource&gt;
 *     &lt;/resource-config&gt;
 *
 * (2) &lt;resource-config <a href="#selector">selector</a>="order-items/order-item"&gt;
 *      &lt;resource&gt;{@link DOMElementVisitor com.acme.transform.MyJavaOrderItemTransformer}&lt;/resource&gt;
 *     &lt;/resource-config&gt;
 * &lt;/smooks-resource-list&gt;
 * </pre>
 * The <code>resource-config</code> XML element maps to an instance of <code>ResourceConfig</code>. The first <code>resource-config</code>
 * declares a resource of type XSL. The text content of the <code>resource</code> element references the resource itself which could be a path, an identifier, etc....
 * In this case, the resource is a path to an XSL script. The resource will be executed when the selector "<i>order/order-header<i/>" evaluates to true.
 * <br/><br/>
 * The second <code>resource-config</code> does not have a type. Without a type, Smooks will assume that the <code>resource-config</code>
 * is for a Java resource. The resource itself is a qualified class name. If the resource is valid (e.g., the class exists),
 * the instantiated class will be invoked when the selector "<i>order-items/order-item<i/>" evaluates to true.
 * <br/><br/>
 * <h3>More Complex Sample with Profiling</h3>
 * <pre>
 * &lt;?xml version='1.0'?&gt;
 * &lt;smooks-resource-list xmlns="https://www.smooks.org/xsd/smooks-2.0.xsd"&gt;
 *      &lt;profiles&gt;
 *          &lt;profile base-profile="message-exchange-1" sub-profiles="message-producer-A, message-consumer-B" /&gt;
 *          &lt;profile base-profile="message-exchange-2" sub-profiles="message-producer-A, message-consumer-C" /&gt;
 *      &lt;/profiles&gt;
 *
 * (1)  &lt;resource-config selector="order/order-header" target-profile="message-producer-A"&gt;
 *          &lt;resource&gt;com.acme.transform.AddIdentityInfo&lt;/resource&gt;
 *      &lt;/resource-config&gt;
 *
 * (2)  &lt;resource-config selector="order-items/order-item" target-profile="message-consumer-B"&gt;
 *          &lt;resource&gt;com.acme.transform.MyJavaOrderItemTransformer&lt;/resource&gt;
 *          &lt;param name="execution-param-X"&gt;param-value-forB&lt;/param&gt;
 *      &lt;/resource-config&gt;
 *
 * (3)  &lt;resource-config selector="order-items/order-item" target-profile="message-consumer-C"&gt;
 *          &lt;resource&gt;com.acme.transform.MyJavaOrderItemTransformer&lt;/resource&gt;
 *          &lt;param name="execution-param-X"&gt;param-value-forC&lt;/param&gt;
 *      &lt;/resource-config&gt;
 *
 * &lt;/smooks-resource-list&gt;
 * </pre>
 * <br/>
 * The first <code>resource-config</code> declares a resource that targets both <code>message-exchange-1</code> and <code>message-exchange-2</code> profiles.
 * The second <code>resource-config</code> declares a resource that targets <code>message-exchange-1</code> profile while the third <code>resource-config</code>
 * declares a resource targeting <code>message-exchange-2</code> profile (see {@link org.smooks.Smooks#createExecutionContext(String)}).
 * It is worth noting that both the second and third resource configs inject parameters into the resources with the <code>param</code>
 * element.
 * <br/><br/>
 * <h3 id="attribdefs">XML attributes</h3>
 * <ol>
 *  <li>
 *      <b id="target-profile">target-profile</b>: a list of one or more {@link ProfileTargetingExpression} profile targeting expressions (supports wildcards "*").
 *  </li>
 *  <br/>
 *  <li>
 *      <b id="selector">selector</b>: a string specifying when the resource should be activated. The selector
 *      is used by the Smooks engine to look-up a resource config. This is typically the name of a fragment (partial XPath support).
 *      This attribute supports a list of comma-separated selectors, allowing you to target a single resource with multiple
 *      selectors (e.g. fragments). When the resource is a {@link Visitor} implementation, the selector is treated as an
 *      XPath expression (full XPath spec not supported), otherwise the selector value is treated as an opaque value.
 *      <br/><br/>
 *      Example selectors:
 *      <ol>
 *          <li>
 *              For a {@link Visitor}, use the target fragment name e.g. "order", "address", "address/name", "item[2]/price[text() = 99.99]" etc.
 *              Also supports wildcard based fragment selection (i.e., "*").  See the <a href="https://www.smooks.org">User Guide</a> for more
 *              details on setting selectors for {@link Visitor} type resources.
 *          </li>
 *          <li>
 *              "#document" is a special selector that targets a resource on the whole document.
 *          </li>
 *          <li>
 *              Targeting a specific {@link SmooksXMLReader} at a specific profile.
 *          </li>
 *      </ol>
 *  </li>
 * </ol>
 */
public interface ResourceConfig {
    /**
     * A special selector for resource that targets the document as a whole (i.e., the root element). This selector
     * is especially useful when the name of the first event is not known ahead of time.
     */
    String DOCUMENT_FRAGMENT_SELECTOR = "#document";

    /**
     * A special selector for a resource that does not target any event. Such a selector is useful when the resource is not
     * actively participating in the event processing (e.g., a resource shared between other resources for looking up values).
     */
    String SELECTOR_NONE = "none";

    /**
     * Performs a shallow clone of this <code>ResourceConfig</code>.
     *
     * @return clone of this <code>>ResourceConfig</code>
     */
    ResourceConfig copy();

    /**
     * Shallow copies parameters from another <code>ResourceConfig</code> and adds them to this <code>ResourceConfig</code>.
     *
     * @param resourceConfig <code>ResourceConfig<code/> to copy parameters from
     */
    void addParameters(ResourceConfig resourceConfig);

    /**
     * Sets the raw selector of this <code>ResourceConfig</code>.
     *
     * @param selector   an expression that defines the target of this resource. Any namespaces must be bound in the <code>namespaces</code> parameter.
     * @param namespaces namespaces bound to prefixes. The property key is the prefix while the value is the namespace.
     */
    void setSelector(String selector, Properties namespaces);

    /**
     * Sets the resource of this <code>ResourceConfig</code>. Unless the resource type is set, the resource is assumed to
     * be a Java resource.
     *
     * @param resource an identifier for the resource. This could be anything like a path, a name, etc... The meaning
     *                 of this value is driven by the resource type.
     * @see            #setResourceType(String)
     */
    void setResource(String resource);

    /**
     * Evaluates whether the resource of this <code>ResourceConfig</code> is defined inline or referenced from a URI.
     * <p/>
     * Note that this method also returns false if the resource is undefined (null).
     *
     * @return <code>true</code> if the resource is defined inline, otherwise <code>false</code>
     * @see    #setResource(String)
     */
    boolean isInline();

    /**
     * Gets the target profile/s of this <code>ResourceConfig</code>.
     *
     * @return target profile/s
     * @see    #setTargetProfile(String)
     */
    String getTargetProfile();

    /**
     * Sets the target profile of this <code>ResourceConfig</code>.
     *
     * @param targetProfile comma-separated list of {@link ProfileTargetingExpression ProfileTargetingExpressions}
     * @see                 #getTargetProfile()
     */
    void setTargetProfile(String targetProfile);

    /**
     * Sets the resource type (e.g., "class", "xsl", "groovy", etc...)
     *
     * @param resourceType resource type
     * @see                #getResourceType()
     * @see                #setResource(String)
     */
    void setResourceType(String resourceType);

    /**
     * Sets the selector in its parsed form.
     *
     * @param selectorPath selector steps
     * @see                #setSelector(String, Properties)
     * @see                #getSelectorPath()
     */
    void setSelectorPath(SelectorPath selectorPath);

    /**
     * Gets the selector in its parsed form.
     *
     * @return selector steps
     * @see    #setSelector(String, Properties)
     * @see    #getSelectorPath()
     */
    SelectorPath getSelectorPath();

    /**
     * Gets the profile targeting expressions of this <code>ResourceConfig</code>.
     *
     * @return profile targeting expressions
     */
    ProfileTargetingExpression[] getProfileTargetingExpressions();

    /**
     * Get the resource for this <code>ResourceConfig</code>.
     *
     * @return resource
     * @see    #setResource(String)
     */
    String getResource();

    /**
     * Evaluates whether this <code>ResourceConfig</code> is for a default resource.
     * <p/>
     * System resources (e.g. {@link org.smooks.engine.delivery.sax.ng.SystemConsumeSerializerVisitor}) are applied by
     * default when no other resource is targeting the element.
     *
     * @return <code>true</code> if this is a default applied resource, otherwise false
     * @see    #setSystem(boolean)
     */
    boolean isSystem();

    /**
     * Set this <code>ResourceConfig</code> as a default applied resource.
     * <p/>
     * System resources (e.g. {@link org.smooks.engine.delivery.sax.ng.SystemConsumeSerializerVisitor}) are applied by
     * default when no other resource is targeting the element.
     *
     * @param isSystem whether this <code>ResourceConfig</code> is for a default resource
     */
    void setSystem(boolean isSystem);

    /**
     * Get the resource type of this <code>ResourceConfig</code>.
     * <p/>
     * Determines the type through the following checks (in order):
     * <ol>
     *  <li>Is it a Java resource. See {@link #isJavaResource()}.  If it is, return "class".</li>
     *  <li>Is the resource type explicitly set on this configuration.  If it is, return the value. Ala the "type"
     * attribute on the resource element on DTD v2.0</li>
     *  <li>Return the resource path file extension e.g. "xsl".</li>
     * </ol>
     *
     * @return resource type
     * @see    #getResourceType()
     */
    String getResourceType();

    /**
     * Adds a parameter to this <code>ResourceConfig</code>. Multiple parameters with the same names can coexist.
     *
     * @param name  parameter name
     * @param value parameter value
     * @return      new parameter added to this <code>ResourceConfig</code>
     */
    <T> Parameter<T> setParameter(String name, T value);

    /**
     * Adds a parameter with a specified type to this <code>ResourceConfig</code>. Multiple parameters with the same names
     * can coexist.
     *
     * @param name  parameter name
     * @param type  parameter type
     * @param value parameter value
     * @return      new parameter added to this <code>ResourceConfig</code>
     */
    <T> Parameter<T> setParameter(String name, String type, T value);

    /**
     * Adds a parameter to this <code>ResourceConfig</code>. Multiple parameters with the same names can coexist.
     *
     * @param parameter parameter to add to this <code>ResourceConfig</code>
     */
    <T> void setParameter(Parameter<T> parameter);

    /**
     * Gets a {@link Parameter parameter} by name from this <code>ResourceConfig</code>. If more than one parameter match
     * the name, the first parameter is returned.
     *
     * @param name name of parameter to get
     * @return     parameter reference, or null if not parameter does not exist
     */
    <T> Parameter<T> getParameter(String name, Class<T> valueClass);

    /**
     * Gets the parameters associated with this <code>ResourceConfig</code> as a {@link java.util.Map}.
     *
     * @return parameters represented as a {@link java.util.Map} where the key is the parameter name and the value is
     *         either a {@link Parameter parameter} or a {@link List list} of {@link Parameter parameters}. The value is
     *         a list when more than one parameter has the same name
     */
    Map<String, Object> getParameters();

    /**
     * Gets all {@link Parameter parameter} values set of this <code>ResourceConfig</code>.
     *
     * @return {@link List list} of all the {@link Parameter parameter} values
     */
    List<?> getParameterValues();

    /**
     * Gets all the parameters of this <code>ResourceConfig</code> by name.
     *
     * @param name name of parameter/s to get
     * @return     {@link List list} of all the {@link Parameter parameters} that match the given name, or null if not set
     */
    List<Parameter<?>> getParameters(String name);

    Object getParameterValue(String name);

    /**
     * Get the named parameter from this <code>ResourceConfig</code>.
     *
     * @param name       name of parameter to get from this <code>ResourceConfig</code>
     * @param valueClass expected value type of the parameter to get
     * @return           parameter value, or <code>null</code> if not set
     */
    <T> T getParameterValue(String name, Class<T> valueClass);

    /**
     * Get value of named parameter from this <code>ResourceConfig</code>.
     *
     * @param name         name of parameter to get.
     * @param valueClass   expected value type of the parameter to get
     * @param defaultValue default value to be returned if there are no
     *                     parameters in this <code>ResourceConfig</code> or if the parameter is not defined
     * @return             parameter value, or <code>defaultValue</code> if not defined
     */
    <T> T getParameterValue(String name, Class<T> valueClass, T defaultValue);

    /**
     * Get the parameter count of this <code>ResourceConfig</code>.
     *
     * @return no. of parameters in this <code>ResourceConfig</code>
     */
    int getParameterCount();

    /**
     * Remove the named parameter this <code>ResourceConfig</code>
     *
     * @param name name of the parameter to be removed
     */
    void removeParameter(String name);

    /**
     * Returns the resource in bytes. If the resource content is not inlined in the configuration, it will be resolved
     * using the {@link org.smooks.resource.URIResourceLocator}. That is, the path will be interpreted as a {@link java.net.URI}.
     * If the resource does not resolve to a stream producing URI, the resource string will be converted to bytes and returned.
     *
     * @return resource as a byte array, or <code>null</code> if resource path is <code>null</code> or the resource does not exist
     */
    byte[] getBytes();

    /**
     * Evaluates whether this <code>ResourceConfig<code/> object references a Java class.
     *
     * @return <code>true</code> if this resource configuration refers to a Java Class resource, otherwise <code>false</code>
     */
    boolean isJavaResource();

    /**
     * Adds the specified change listener to the list of change listeners.
     *
     * @param resourceConfigChangeListener {@link ResourceConfigChangeListener} instance to add
     */
    void addChangeListener(ResourceConfigChangeListener resourceConfigChangeListener);

    /**
     * Removes the specified change listener from the list of change listeners.
     *
     * @param resourceConfigChangeListener {@link ResourceConfigChangeListener} instance to remove
     */
    void removeChangeListener(ResourceConfigChangeListener resourceConfigChangeListener);

    /**
     * Generates an XML'ified description of this resource.
     *
     * @return XML'ified description of the resource
     */
    String toXml();

    /**
     * Creates a {@link Properties} instance from the parameters {@link ResourceConfig} object.
     *
     * @return resource config parameter.
     */
    Properties toProperties();
}
