/*-
 * ========================LICENSE_START=================================
 * Smooks API
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

import java.util.List;
import java.util.Map;
import java.util.Properties;

public interface ResourceConfig {
    /**
     * A special selector for resource targeted at the document as a whole (the root element).
     */
    String DOCUMENT_FRAGMENT_SELECTOR = "#document";

    /**
     * A special selector for resource targeted at the document as a whole (the root element).
     */
    String DOCUMENT_VOID_SELECTOR = "$void";

    /**
     * XML selector type definition prefix
     */
    String XML_DEF_PREFIX = "xmldef:";
    /**
     *
     */
    String SELECTOR_NONE = "none";

    @SuppressWarnings({ "MethodDoesntCallSuperMethod", "unchecked" })
    ResourceConfig copy();

    @Deprecated
    String getExtendedConfigNS();

    @Deprecated
    void setExtendedConfigNS(String extendedConfigNS);

    void addParameters(ResourceConfig resourceConfig);

    void setSelector(String selector);

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
