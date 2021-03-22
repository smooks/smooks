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
package org.smooks.engine.delivery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.delivery.ContentDeliveryConfig;
import org.smooks.api.delivery.ContentHandlerBinding;
import org.smooks.api.delivery.Filter;
import org.smooks.api.delivery.FilterBypass;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.engine.resource.config.ParameterAccessor;
import org.smooks.api.resource.config.ResourceConfigSortComparator;
import org.smooks.api.ExecutionContext;
import org.smooks.api.delivery.event.ConfigBuilderEvent;
import org.smooks.api.lifecycle.ExecutionLifecycleCleanable;
import org.smooks.api.lifecycle.ExecutionLifecycleInitializable;
import org.smooks.api.Registry;
import org.smooks.engine.lookup.ContentHandlerFactoryLookup;

import java.util.*;
import java.util.Map.Entry;

/**
 * Abstract {@link ContentDeliveryConfig}.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@SuppressWarnings("unchecked")
public abstract class AbstractContentDeliveryConfig implements ContentDeliveryConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContentDeliveryConfig.class);

	/**
     * Container context.
     */
    private Registry registry;
    /**
     * Table of ResourceConfig instances keyed by selector value. Each table entry
     * contains a List of ResourceConfig instances.
     */
    private Map<String, List<ResourceConfig>> resourceConfigTable = new LinkedHashMap<>();
    /**
     * Table of Object instance lists keyed by selector. Each table entry
     * contains a List of Objects.
     */
    private final Map objectsTable = new LinkedHashMap();

    /**
     * Config builder events list.
     */
    private final List<ConfigBuilderEvent> configBuilderEvents = new ArrayList<>();

    private final Set<ExecutionLifecycleInitializable> executionLifecycleInitializables = new LinkedHashSet<>();
    private final Set<ExecutionLifecycleCleanable> executionLifecycleCleanables = new LinkedHashSet<>();

    private Boolean isDefaultSerializationOn;
    private Boolean closeSource;
    private Boolean closeResult;

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    /**
     * Get the list of {@link ResourceConfig}s for the specified selector definition.
     * @param selector The configuration "selector" attribute value from the .cdrl file in the .cdrar.
     * @return List of ResourceConfig instances, or null.
     */
    @Override
    public List<ResourceConfig> getResourceConfigs(String selector) {
        return resourceConfigTable.get(selector);
    }

    public void setResourceConfigs(Map<String, List<ResourceConfig>> resourceConfigTable) {
        this.resourceConfigTable = resourceConfigTable;
    }

    /**
     * Get the {@link ResourceConfig} map for the target execution context.
     * <p/>
     * This Map will be {@link ResourceConfigSortComparator preordered}
     * for the target execution context.
     *
     * @return {@link ResourceConfig} map for the target execution context, keyed by the configuration
     * {@link ResourceConfig#getSelector() selector}, with each value being a
     * {@link List} of preordered {@link ResourceConfig} instances.
     */
    @Override
    public Map<String, List<ResourceConfig>> getResourceConfigs() {
        return resourceConfigTable;
    }
    
    /**
     * Get a list {@link Object}s from the supplied {@link ResourceConfig} selector value.
     * <p/>
     * Uses {@link Registry#getObject(ResourceConfig)} to construct the object.
     * @param selector selector attribute value from the .cdrl file in the .cdrar.
     * @return List of Object instances.  An empty list is returned where no
     * selectors exist.
     */
    @Override
    public List<?> getObjects(String selector) {
        List objects;

        objects = (List) objectsTable.get(selector);
        if (objects == null) {
            List unitDefs = resourceConfigTable.get(selector);

            if (unitDefs != null && unitDefs.size() > 0) {
                objects = new ArrayList<>(unitDefs.size());

                if (registry == null) {
                    throw new IllegalStateException("Call to getObjects() before the setRegistry() was called.");
                }

                for (final Object unitDef : unitDefs) {
                    ResourceConfig resourceConfig = (ResourceConfig) unitDef;
                    objects.add(registry.lookup(new ContentHandlerFactoryLookup("class")).create(resourceConfig));
                }
            } else {
                objects = Collections.EMPTY_LIST;
            }

            objectsTable.put(selector, objects);
        }

        return objects;
    }

    @Override
    public List<ConfigBuilderEvent> getConfigBuilderEvents() {
        return configBuilderEvents;
    }

    @Override
    public boolean isDefaultSerializationOn() {
        if (isDefaultSerializationOn == null) {
            isDefaultSerializationOn = Boolean.valueOf(ParameterAccessor.getParameterValue(Filter.DEFAULT_SERIALIZATION_ON, String.class, "true", this));
        }

        return isDefaultSerializationOn;
    }

    @SuppressWarnings("WeakerAccess")
    public <T extends Visitor> void addToExecutionLifecycleSets(SelectorTable<T> selectorTable) {
        Collection<List<ContentHandlerBinding<T>>> selectorTableContentHandlerBindings = selectorTable.values();

        for (List<ContentHandlerBinding<T>> contentHandlerBindings : selectorTableContentHandlerBindings) {
            for (ContentHandlerBinding<T> contentHandlerBinding : contentHandlerBindings) {
                if (contentHandlerBinding.getContentHandler() instanceof ExecutionLifecycleInitializable) {
                    executionLifecycleInitializables.add((ExecutionLifecycleInitializable) contentHandlerBinding.getContentHandler());
                }
                if (contentHandlerBinding.getContentHandler() instanceof ExecutionLifecycleCleanable) {
                    executionLifecycleCleanables.add((ExecutionLifecycleCleanable) contentHandlerBinding.getContentHandler());
                }
            }
        }
    }

    @Override
    public void executeHandlerInit(final ExecutionContext executionContext) {
        for (ExecutionLifecycleInitializable executionLifecycleInitializable : executionLifecycleInitializables) {
            executionLifecycleInitializable.executeExecutionLifecycleInitialize(executionContext);
        }
    }

    @Override
    public void executeHandlerCleanup(final ExecutionContext executionContext) {
        for (ExecutionLifecycleCleanable handler : executionLifecycleCleanables) {
            try {
                handler.executeExecutionLifecycleCleanup(executionContext);
            } catch (Throwable t) {
                LOGGER.error("Error during Visit handler cleanup.", t);
            }
        }
    }

    protected FilterBypass getFilterBypass(SelectorTable<?>... selectorTables) {
    	for (SelectorTable selectorTable : selectorTables) {
            Collection<List<ContentHandlerBinding<?>>> contentHandlerBindings = selectorTable.values();
            long userContentHandlerBindingsCount = contentHandlerBindings.
                    stream().
                    flatMap(l -> l.
                            stream().
                            filter(c -> !c.getResourceConfig().isDefaultResource())).count();
            
            if (userContentHandlerBindingsCount > 1) {
                // If any of the visitor tables have more than 1 visitor instance...
                return null;
            }
        }

        // Gather the possible set of FilterBypass instances...
        Set<FilterBypass> bypassSet = new HashSet<>();
        for (SelectorTable selectorTable : selectorTables) {
            Collection<List<ContentHandlerBinding<?>>> contentHandlerBindings = selectorTable.values();
            long userContentHandlerBindingsCount = contentHandlerBindings.
                    stream().
                    flatMap(l -> l.
                            stream().
                            filter(c -> !c.getResourceConfig().isDefaultResource())).count();
            
            if (userContentHandlerBindingsCount == 1) {
                FilterBypass filterBypass = getFilterBypass(selectorTable);

                if (filterBypass != null) {
                    bypassSet.add(filterBypass);
                } else {
                    // There's a non-FilterBypass Visitor configured, so we can't
                    // use the Bypass Filter
                    return null;
                }
            }
        }

        // If there's just one FilterBypass instance, return it...
        if (bypassSet.size() == 1) {
            return bypassSet.iterator().next();
        }

        // Otherwise we're not going to allow filter bypassing...
        return null;
    }

    private <T extends Visitor> FilterBypass getFilterBypass(SelectorTable<T> selectorTable) {
        Set<Entry<String, List<ContentHandlerBinding<T>>>> entries = selectorTable.entrySet();

        for(Entry<String, List<ContentHandlerBinding<T>>> entry : entries) {
        	ContentHandlerBinding<T> configMap = entry.getValue().get(0);
        	ResourceConfig resourceConfig = configMap.getResourceConfig();

        	if(!resourceConfig.isDefaultResource()) {
	        	if(resourceConfig.getSelectorPath().getTargetElement().equals(ResourceConfig.DOCUMENT_FRAGMENT_SELECTOR)) {
	        		T visitor = configMap.getContentHandler();
	        		if(visitor instanceof FilterBypass) {
	        			return (FilterBypass) visitor;
	        		}
	        	}

	        	// Make sure we only iterate once...
	        	return null;
        	}
        }

    	return null;
    }
    
    protected boolean getCloseSource() {
        if (closeSource == null) {
            closeSource = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.CLOSE_SOURCE, String.class, "true", this));
        }
        
        return closeSource;
    }

    protected boolean getCloseResult() {
        if (closeResult == null) {
            closeResult = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.CLOSE_RESULT, String.class, "true", this));
        }

        return closeResult;
    }
}