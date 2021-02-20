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
package org.smooks.engine.resource.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.config.ResourceConfigFactory;
import org.smooks.support.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Properties;

public class DefaultResourceConfigFactory implements ResourceConfigFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultResourceConfigFactory.class);

    @Override
    @Deprecated
    public ResourceConfig createConfiguration(String defaultSelector, String defaultNamespace, String defaultProfile, Element element) {
        String selector = DomUtils.getAttributeValue(element, "selector");
        String namespace = DomUtils.getAttributeValue(element, "selector-namespace");
        String targetProfile = DomUtils.getAttributeValue(element, "target-profile");
        Element resourceElement = DomUtils.getElementByTagName(element, "resource");

        final String resource;
        if (resourceElement != null) {
            resource = DomUtils.getAllText(resourceElement, true);
        } else {
            resource = null;
        }

        final ResourceConfig resourceConfig = new DefaultResourceConfig((selector != null ? selector : defaultSelector),
                (namespace != null ? namespace : defaultNamespace),
                (targetProfile != null ? targetProfile : defaultProfile),
                resource);
        resourceConfig.getSelectorPath().getNamespaces().putAll(getNamespaces(element));
        
        if (resourceElement != null) {
            resourceConfig.setResourceType(DomUtils.getAttributeValue(resourceElement, "type"));
        }

        if (resource == null) {
            if (resourceConfig.getParameters("restype") != null) {
                LOGGER.debug("Resource 'null' for resource config: " + resourceConfig + ".  This is probably an error because the configuration contains a 'resdata' param, which suggests it is following the old DTD based configuration model.  The new model requires the resource to be specified in the <resource> element.");
            } else {
                LOGGER.debug("Resource 'null' for resource config: " + resourceConfig + ". This is not invalid!");
            }
        }

        return resourceConfig;
    }

    @Override
    public ResourceConfig createConfiguration(String defaultProfile, Element element) {
        String selector = DomUtils.getAttributeValue(element, "selector");
        String targetProfile = DomUtils.getAttributeValue(element, "target-profile");
        Element resourceElement = DomUtils.getElementByTagName(element, "resource");

        final String resource;
        if (resourceElement != null) {
            resource = DomUtils.getAllText(resourceElement, true);
        } else {
            resource = null;
        }

        final ResourceConfig resourceConfig = new DefaultResourceConfig(selector, (targetProfile != null ? targetProfile : defaultProfile), resource);
        resourceConfig.getSelectorPath().getNamespaces().putAll(getNamespaces(element));

        if (resourceElement != null) {
            resourceConfig.setResourceType(DomUtils.getAttributeValue(resourceElement, "type"));
        }

        if (resource == null) {
            if (resourceConfig.getParameters("restype") != null) {
                LOGGER.debug("Resource 'null' for resource config: " + resourceConfig + ".  This is probably an error because the configuration contains a 'resdata' param, which suggests it is following the old DTD based configuration model.  The new model requires the resource to be specified in the <resource> element.");
            } else {
                LOGGER.debug("Resource 'null' for resource config: " + resourceConfig + ". This is not invalid!");
            }
        }

        return resourceConfig;
    }

    private Properties getNamespaces(Element element) {
        Properties namespaces = new Properties();

        if (element.getParentNode() != null && element.getParentNode().getAttributes() != null) {
            for (int i = 0; i < element.getParentNode().getAttributes().getLength(); i++) {
                Node node = element.getParentNode().getAttributes().item(i);
                String prefix = node.getNodeName();
                if (prefix.startsWith("xmlns")) {
                    if (prefix.indexOf(":") > 0) {
                        prefix = prefix.substring(prefix.indexOf(":") + 1);
                        namespaces.put(prefix, node.getNodeValue());
                    }
                }
            }
        }

        return namespaces;
    }
}