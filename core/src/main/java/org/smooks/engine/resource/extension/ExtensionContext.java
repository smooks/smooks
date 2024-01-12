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
package org.smooks.engine.resource.extension;

import org.smooks.api.resource.config.ConfigSearch;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.config.ResourceConfigChangeListener;
import org.smooks.api.resource.config.ResourceConfigSeq;
import org.smooks.api.TypedKey;
import org.smooks.api.expression.ExpressionEvaluator;
import org.smooks.engine.resource.config.XMLConfigDigester;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

/**
 * Context object used by Smooks configuration extension visitors.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ExtensionContext {

    public static final TypedKey<ExtensionContext> EXTENSION_CONTEXT_TYPED_KEY = new TypedKey<>();

    private final XMLConfigDigester xmlConfigDigester;
    private final String defaultProfile;
    private final ExpressionEvaluator defaultConditionEvaluator;

    private final Stack<ResourceConfig> resourceStack = new Stack<ResourceConfig>() {
        @Override
        public ResourceConfig push(ResourceConfig resourceConfiguration) {
            if (!isEmpty()) {
                resourceConfiguration.addChangeListener(resChangeListener);
            }
            return super.push(resourceConfiguration);
        }

        @Override
        public ResourceConfig pop() {
            ResourceConfig resourceConfig = super.pop();
            if (!isEmpty()) {
                resourceConfig.removeChangeListener(resChangeListener);
            }
            return resourceConfig;
        }
    };
    private final ResourceConfigChangeListener resChangeListener = resourceConfig -> {
        String selector = resourceConfig.getSelectorPath().getSelector();
        if (selector != null && selector.startsWith("#/")) {
            ResourceConfig parentResource = resourceStack.get(resourceStack.size() - 2);
            resourceConfig.setSelector(parentResource.getSelectorPath().getSelector() + selector.substring(1), new Properties());
        }
    };
    private final List<ResourceConfig> resources = new ArrayList<>();

    /**
     * Public constructor.
     *
     * @param xmlConfigDigester         The base XMLConfigDigester.
     * @param defaultProfile            The default profile.
     * @param defaultConditionEvaluator The default condition evaluator.
     */
    public ExtensionContext(XMLConfigDigester xmlConfigDigester, String defaultProfile, ExpressionEvaluator defaultConditionEvaluator) {
        this.xmlConfigDigester = xmlConfigDigester;
        this.defaultProfile = defaultProfile;
        this.defaultConditionEvaluator = defaultConditionEvaluator;
    }

    /**
     * Add a resource configuration to the list of resources for this Extension Context.
     * <p/>
     * The resource gets added to the {@link #getResourceStack() resourceStack} and the
     * basic list of {@link #getResources() resources}.
     *
     * @param resource The resource to be added.
     */
    public void addResource(ResourceConfig resource) {
        resourceStack.push(resource);
        resources.add(resource);
    }

    /**
     * Add a resource configuration template to the resources stack for this Extension Context.
     * <p/>
     * This resource is not added as a resource on the Smooks instance, but is instead available
     * for cloning.
     *
     * @param resourceConfig The resource to be added.
     */
    public void addResourceTemplate(ResourceConfig resourceConfig) {
        resourceStack.push(resourceConfig);
    }

    /**
     * Get the resource stack.
     *
     * @return The resource stack.
     * @see #addResource(ResourceConfig)
     */
    public Stack<ResourceConfig> getResourceStack() {
        return resourceStack;
    }

    /**
     * Get the resource list.
     *
     * @return The resource list.
     * @see #addResource(ResourceConfig)
     */
    public List<ResourceConfig> getResources() {
        return resources;
    }

    /**
     * Get the active resource configuration list.
     * <p/>
     * This is the global config list i.e. not just the config list for the config
     * being processed.
     *
     * @return The active resource configuration list.
     */
    public ResourceConfigSeq getResourceList() {
        return xmlConfigDigester.getResourceList();
    }

    public ResourceConfig getCurrentConfig() {
        if (resourceStack.isEmpty()) {
            return null;
        } else {
            return resourceStack.peek();
        }
    }

    public XMLConfigDigester getXmlConfigDigester() {
        return xmlConfigDigester;
    }

    public String getDefaultProfile() {
        return defaultProfile;
    }

    public ExpressionEvaluator getDefaultConditionEvaluator() {
        return defaultConditionEvaluator;
    }

    public ResourceConfig getResourceByName(String name) {
        for (int i = resourceStack.size() - 1; i >= 0; i--) {
            ResourceConfig resourceConfig = resourceStack.get(i);
            String resourceName = resourceConfig.getResource();
            if (name.equals(resourceName)) {
                return resourceConfig;
            }
        }

        return null;
    }

    /**
     * Lookup an existing resource configuration from the global config list.
     * <p/>
     * Note that this is resource config order-dependent.  It will not locate configs that
     * have not yet been loaded.
     *
     * @param searchCriteria The resource lookup criteria.
     * @return List of matches resources, or an empty List if no matches are found.
     */
    public List<ResourceConfig> lookupResource(ConfigSearch searchCriteria) {
        return xmlConfigDigester.getResourceList().lookupResource(searchCriteria);
    }
}
