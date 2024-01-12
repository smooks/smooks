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
 * A configuration for a Smooks resource.
 * <p>
 * <code>ResourceConfig</code> controls the behaviour of a Smooks resource, including:
 * <ul>
 *     <li>which event/s from the input stream target the resource</li>
 *     <li>the parameters injected into the resource</li>
 *     <li>the type of resource that is instantiated</li>
 * </ul>
 * <p>
 * A <code>ResourceConfig</code> can be configured through Java code but it is easier to configure through XML. The
 * following are a few sample configurations. Explanations follow the samples.
 * <p/>
 * <h3>Basic Sample</h3>
 * <pre>
 * <i>&lt;?xml version='1.0'?&gt;
 * &lt;smooks-resource-list xmlns="https://www.smooks.org/xsd/smooks-2.0.xsd"&gt;
 *      <b>&lt;resource-config <a href="#selector">selector</a>="order/order-header"&gt;
 *          &lt;resource type="xsl"&gt;<a target="new" href="https://www.smooks.org#Smooks-smookscartridges">/com/acme/transform/OrderHeaderTransformer.xsl</a>&lt;/resource&gt;
 *      &lt;/resource-config&gt;</b>
 *
 *      <b>&lt;resource-config <a href="#selector">selector</a>="order-items/order-item"&gt;
 *          &lt;resource&gt;{@link DOMElementVisitor com.acme.transform.MyJavaOrderItemTransformer}&lt;/resource&gt;
 *      &lt;/resource-config&gt;</b>
 * &lt;/smooks-resource-list&gt;</i>
 * </pre>
 * The <b>resource-config</b> XML element maps directly to an instance of <code>ResourceConfig</code>.
 * <p>
 * <h3>More Complex Sample with Profiling</h3>
 * <pre>
 * <i>&lt;?xml version='1.0'?&gt;
 * &lt;smooks-resource-list xmlns="https://www.smooks.org/xsd/smooks-2.0.xsd"&gt;
 *      <b>&lt;profiles&gt;
 *          &lt;profile base-profile="message-exchange-1" sub-profiles="message-producer-A, message-consumer-B" /&gt;
 *          &lt;profile base-profile="message-exchange-2" sub-profiles="message-producer-A, message-consumer-C" /&gt;
 *      &lt;/profiles&gt;</b>
 *
 * (1)  &lt;resource-config selector="order/order-header" <b>target-profile="message-producer-A"</b>&gt;
 *          &lt;resource&gt;com.acme.transform.AddIdentityInfo&lt;/resource&gt;
 *      &lt;/resource-config&gt;
 *
 * (2)  &lt;resource-config selector="order-items/order-item" <b>target-profile="message-consumer-B"</b>&gt;
 *          &lt;resource&gt;com.acme.transform.MyJavaOrderItemTransformer&lt;/resource&gt;
 *          &lt;param name="execution-param-X"&gt;param-value-forB&lt;/param&gt;
 *      &lt;/resource-config&gt;
 *
 * (3)  &lt;resource-config selector="order-items/order-item" <b>target-profile="message-consumer-C"</b>&gt;
 *          &lt;resource&gt;com.acme.transform.MyJavaOrderItemTransformer&lt;/resource&gt;
 *          &lt;param name="execution-param-X"&gt;param-value-forC&lt;/param&gt;
 *      &lt;/resource-config&gt;
 *
 * &lt;/smooks-resource-list&gt;</i></pre>
 * <p>
 * The first resource is targeted for both "message-exchange-1" and "message-exchange-2" profiles. The second resource
 * is only targeted for "message-exchange-1" profile and the third resource only for "message-exchange-2" profile
 * (see {@link org.smooks.Smooks#createExecutionContext(String)}).
 * <p>
 * <h4 id="attribdefs">Attribute Definitions</h4>
 * <ul>
 * <li><b id="useragent">target-profile</b>: a list of one or more {@link ProfileTargetingExpression profile targeting expressions}
 * (supports wildcards "*").
 * </ol>
 * </li>
 * <br/>
 * <li><b id="selector">selector</b>: selector string.  Used by Smooks to lookup a resource config.
 * This is typically an input fragment name (partial XPath support). This attribute supports a list of comma separated selectors,
 * allowing you to target a single resource at multiple selector (e.g. fragments).  Where the resource is a {@link Visitor} implementation,
 * the selector is treated as an XPath expression (full XPath spec not supported), otherwise the selector value is treated as an opaque value.
 * <p>
 * <p>
 * Example selectors:
 * <ol>
 * <li>For a {@link Visitor}, use the target fragment name e.g. "order", "address", "address/name", "item[2]/price[text() = 99.99]" etc.
 * Also supports wildcard based fragment selection (i.e., "*").  See the <a href="www.smooks.org">User Guide</a> for more
 * details on setting selectors for {@link Visitor} type resources.
 * </li>
 * <li>"#document" is a special selector that targets a resource at the "document" fragment i.e. the whole document,
 * or document root node fragment.</li>
 * <li>Targeting a specific {@link SmooksXMLReader} at a specific profile.</li>
 * </ol>
 */
public interface ResourceConfig {
    /**
     * A special selector for resource targeted at the document as a whole (the root element).
     */
    String DOCUMENT_FRAGMENT_SELECTOR = "#document";

    /**
     * XML selector type definition prefix
     */
    String XML_DEF_PREFIX = "xmldef:";
    /**
     *
     */
    String SELECTOR_NONE = "none";

    ResourceConfig copy();

    void addParameters(ResourceConfig resourceConfig);

    void setSelector(String selector, Properties namespaces);

    void setResource(String resource);

    boolean isInline();

    String getTargetProfile();

    void setTargetProfile(String targetProfile);

    void setResourceType(String resourceType);

    void setSelectorPath(SelectorPath selectorPath);

    SelectorPath getSelectorPath();

    ProfileTargetingExpression[] getProfileTargetingExpressions();

    String getResource();

    boolean isDefaultResource();

    void setDefaultResource(boolean defaultResource);

    String getResourceType();

    <T> Parameter<T> setParameter(String name, T value);

    <T> Parameter<T> setParameter(String name, String type, T value);

    <T> void setParameter(Parameter<T> parameter);

    <T> Parameter<T> getParameter(String name, Class<T> valueClass);

    Map<String, Object> getParameters();

    List<?> getParameterValues();

    List<Parameter<?>> getParameters(String name);

    Object getParameterValue(String name);

    <T> T getParameterValue(String name, Class<T> valueClass);

    <T> T getParameterValue(String name, Class<T> valueClass, T defaultValue);

    int getParameterCount();

    void removeParameter(String name);

    boolean isXmlDef();

    byte[] getBytes();

    boolean isJavaResource();

    void addChangeListener(ResourceConfigChangeListener listener);

    void removeChangeListener(ResourceConfigChangeListener listener);

    String toXml();

    Properties toProperties();
}
