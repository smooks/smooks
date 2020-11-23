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
package org.smooks.delivery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.cdr.ParameterAccessor;
import org.smooks.cdr.ResourceConfig;
import org.smooks.cdr.ResourceConfigSortComparator;
import org.smooks.container.ApplicationContext;
import org.smooks.container.ExecutionContext;
import org.smooks.dtd.DTDStore;
import org.smooks.event.types.ConfigBuilderEvent;
import org.smooks.lifecycle.ExecutionLifecycleCleanable;
import org.smooks.lifecycle.ExecutionLifecycleInitializable;
import org.smooks.registry.Registry;
import org.smooks.registry.lookup.ContentHandlerFactoryLookup;
import org.xml.sax.XMLReader;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

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
    private ApplicationContext applicationContext;
    /**
     * Table of ResourceConfig instances keyed by selector value. Each table entry
     * contains a List of ResourceConfig instances.
     */
    private Map<String, List<ResourceConfig>> resourceConfigTable = new LinkedHashMap<String, List<ResourceConfig>>();
    /**
     * Table of Object instance lists keyed by selector. Each table entry
     * contains a List of Objects.
     */
    private final Map objectsTable = new LinkedHashMap();
    /**
     * DTD for the associated device.
     */
    private DTDStore.DTDObjectContainer dtd;
    /**
     * Config builder events list.
     */
    private final List<ConfigBuilderEvent> configBuilderEvents = new ArrayList<ConfigBuilderEvent>();

    private final Set<ExecutionLifecycleInitializable> execInitializableHandlers = new LinkedHashSet<ExecutionLifecycleInitializable>();
    private final Set<ExecutionLifecycleCleanable> execCleanableHandlers = new LinkedHashSet<ExecutionLifecycleCleanable>();

    private Boolean isDefaultSerializationOn = null;

    private final List<XMLReader> readerPool = new CopyOnWriteArrayList<XMLReader>();
    private int readerPoolSize;

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Get the list of {@link ResourceConfig}s for the specified selector definition.
     * @param selector The configuration "selector" attribute value from the .cdrl file in the .cdrar.
     * @return List of ResourceConfig instances, or null.
     */
    public List getResourceConfigs(String selector) {
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
    public Map<String, List<ResourceConfig>> getResourceConfigs() {
        return resourceConfigTable;
    }

    private static final Vector EMPTY_LIST = new Vector();

    /**
     * Get a list {@link Object}s from the supplied {@link ResourceConfig} selector value.
     * <p/>
     * Uses {@link Registry#getObject(ResourceConfig)} to construct the object.
     * @param selector selector attribute value from the .cdrl file in the .cdrar.
     * @return List of Object instances.  An empty list is returned where no
     * selectors exist.
     */
    public List getObjects(String selector) {
        Vector objects;

        objects = (Vector) objectsTable.get(selector);
        if (objects == null) {
            List unitDefs = resourceConfigTable.get(selector);

            if (unitDefs != null && unitDefs.size() > 0) {
                objects = new Vector(unitDefs.size());

                if (applicationContext == null) {
                    throw new IllegalStateException("Call to getObjects() before the setApplicationContext() was called.");
                }

                for (final Object unitDef : unitDefs) {
                    ResourceConfig resourceConfig    = (ResourceConfig) unitDef;
                    objects.add(applicationContext.getRegistry().lookup(new ContentHandlerFactoryLookup("class")).create(resourceConfig));
                }
            } else {
                objects = EMPTY_LIST;
            }

            objectsTable.put(selector, objects);
        }

        return objects;
    }

    /* (non-Javadoc)
     * @see org.smooks.delivery.ContentDeliveryConfig#getDTD()
     */
    public DTDStore.DTDObjectContainer getDTD() {
        return dtd;
    }

    public void setDtd(DTDStore.DTDObjectContainer dtd) {
        this.dtd = dtd;
    }

    public List<ConfigBuilderEvent> getConfigBuilderEvents() {
        return configBuilderEvents;
    }

    public boolean isDefaultSerializationOn() {
        if(isDefaultSerializationOn == null) {
            isDefaultSerializationOn = Boolean.valueOf(ParameterAccessor.getParameterValue(Filter.DEFAULT_SERIALIZATION_ON, String.class, "true", this));
        }

        return isDefaultSerializationOn;
    }

    @SuppressWarnings("WeakerAccess")
    public <T extends Visitor> void addToExecutionLifecycleSets(ContentHandlerBindings<T> handlerSet) {
        Collection<List<ContentHandlerBinding<T>>> mapEntries = handlerSet.getTable().values();

        for(List<ContentHandlerBinding<T>> mapList : mapEntries) {
            for(ContentHandlerBinding<T> map : mapList) {
                if(map.getContentHandler() instanceof ExecutionLifecycleInitializable) {
                    execInitializableHandlers.add((ExecutionLifecycleInitializable) map.getContentHandler());
                }
                if(map.getContentHandler() instanceof ExecutionLifecycleCleanable) {
                    execCleanableHandlers.add((ExecutionLifecycleCleanable) map.getContentHandler());
                }
            }
        }
    }

    public void executeHandlerInit(ExecutionContext executionContext) {
        for(ExecutionLifecycleInitializable handler : execInitializableHandlers) {
            handler.executeExecutionLifecycleInitialize(executionContext);
        }
    }

    public void executeHandlerCleanup(ExecutionContext executionContext) {
        for(ExecutionLifecycleCleanable handler : execCleanableHandlers) {
            try {
                handler.executeExecutionLifecycleCleanup(executionContext);
            } catch(Throwable t) {
                LOGGER.error("Error during Visit handler cleanup.", t);
            }
        }
    }

    public void initializeXMLReaderPool() {
    	try {
	        readerPoolSize = Integer.parseInt(ParameterAccessor.getParameterValue(Filter.READER_POOL_SIZE, String.class, "0", this));
    	} catch(NumberFormatException e) {
    		readerPoolSize = 0;
    	}
    }

    public XMLReader getXMLReader() {
        synchronized (readerPool) {
            if (!readerPool.isEmpty()) {
                return readerPool.remove(0);
            } else {
                return null;
            }
        }
    }

	public void returnXMLReader(XMLReader reader) {
		synchronized(readerPool) {
			if(readerPool.size() < readerPoolSize) {
				readerPool.add(reader);
			}
		}
	}

    protected FilterBypass getFilterBypass(ContentHandlerBindings... visitorTables) {
    	for(ContentHandlerBindings visitorTable : visitorTables) {
            if(visitorTable != null && visitorTable.getUserConfiguredCount() > 1) {
            	// If any of the visitor tables have more than 1 visitor instance...
            	return null;
            }
    	}

    	// Gather the possible set of FilterBypass instances...
    	Set<FilterBypass> bypassSet = new HashSet<FilterBypass>();
    	for(ContentHandlerBindings visitorTable : visitorTables) {
            if(visitorTable != null && visitorTable.getUserConfiguredCount() == 1) {
            	FilterBypass filterBypass = getFilterBypass(visitorTable);

            	if(filterBypass != null) {
            		bypassSet.add(filterBypass);
            	} else {
            		// There's a non-FilterBypass Visitor configured, so we can't
            		// use the Bypass Filter
            		return null;
            	}
            }
    	}

    	// If there's just one FilterBypass instance, return it...
    	if(bypassSet.size() == 1) {
    		return bypassSet.iterator().next();
    	}

    	// Otherwise we're not going to allow filter bypassing...
    	return null;
    }

    private <T extends Visitor> FilterBypass getFilterBypass(ContentHandlerBindings<T> visitorTable) {
        Set<Entry<String, List<ContentHandlerBinding<T>>>> entries = visitorTable.getTable().entrySet();

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
}