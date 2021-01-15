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

import org.smooks.cdr.ResourceConfig;
import org.smooks.cdr.ResourceConfigSortComparator;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.ordering.Consumer;
import org.smooks.delivery.ordering.Producer;
import org.smooks.dtd.DTDStore;
import org.smooks.event.types.ConfigBuilderEvent;
import org.smooks.registry.Registry;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.List;
import java.util.Map;

/**
 * Content delivery configuration.
 * <p/>
 * Provides access to Content Delivery Resources 
 * (e.g. {@link ContentHandler Content Delivery Units})
 * and other information for the targeted profile.
 * @author tfennelly
 */
@NotThreadSafe
public interface ContentDeliveryConfig {

    /**
     * Should Smooks sort the Visitors, targeted at each selector, based on the
     * what the Visitors {@link Producer produce} and {@link Consumer consume}.
     * Default value "true".
     */
    String SMOOKS_VISITORS_SORT = "smooks.visitors.sort";

    /**
	 * Get the list of {@link ResourceConfig}s for the specified selector definition.
	 * <p/>
	 * This list will be preselected and {@link ResourceConfigSortComparator preordered}
	 * for the target execution context.
	 * @param selector Configuration {@link ResourceConfig#getSelector() selector}.  This
	 * parameter is treated case incensitively.
	 * @return List of {@link ResourceConfig} instances, or null if no {@link ResourceConfig}s are 
	 * defined under that selector (for the device).
	 * @see #getObjects(String)
	 */
	List<ResourceConfig> getResourceConfigs(String selector);

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
	Map<String, List<ResourceConfig>> getResourceConfigs();
	
	/**
	 * Get a list of {@link Object}s from the {@link ResourceConfig}s specified by the selector.
	 * <p/>
	 * Gets the {@link ResourceConfig}s specified for the selector and attempts to instanciate
	 * a Java class instance from the resource specified by each of the {@link ResourceConfig}s.
	 * <p/>
	 * Implementations should use {@link Registry#lookup(Object)}} to
	 * construct each object.
	 * @param selector selector attribute value from the .cdrl file in the .cdrar.  This 
	 * parameter is treated case incensitively.
	 * @return List of Object instances.  An empty list is returned where no 
	 * selectors exist.
	 * @see Registry#lookup(Object)
	 * @see #getResourceConfigs(String)
	 */
	List getObjects(String selector);

    /**
     * Get a new stream filter for the content delivery configuration.
     * @return The stream filter.
     * @param executionContext Execution context.
     */
    Filter newFilter(ExecutionContext executionContext);

    /**
	 * Get the DTD ({@link org.smooks.dtd.DTDStore.DTDObjectContainer}) for this delivery context.
	 * @return The DTD ({@link org.smooks.dtd.DTDStore.DTDObjectContainer}) for this delivery context.
	 */
	DTDStore.DTDObjectContainer getDTD();

    /**
     * Get the list of Execution Events generated during the build of
     * the configuration.
     * @return The list of events.
     */
    List<ConfigBuilderEvent> getConfigBuilderEvents();

    /**
     * Is default serialization on..
     * @return True if default serialization is on, otherwise false.
     */
    boolean isDefaultSerializationOn();

    /**
     * Sort the Visitors, targeted at each selector, based on the
     * what the Visitors {@link Producer produce} and {@link Consumer consume}.
     * @throws SmooksConfigurationException Sort failure.
     * @see #SMOOKS_VISITORS_SORT
     */
    void sort() throws SmooksConfigurationException;

    /**
     * Add the execution lifecycle sets for the configuration.
     * @throws SmooksConfigurationException Error resolving the handlers interested
     * in the Execution lifecycle.
     */
    void addToExecutionLifecycleSets() throws SmooksConfigurationException;

    /**
     * Initialize execution context lifecycle aware handlers.
     * @param executionContext The execution context.
     */
    void executeHandlerInit(ExecutionContext executionContext);

    /**
     * Cleanup execution context lifecycle aware handlers.
     * @param executionContext The execution context.
     */
    void executeHandlerCleanup(ExecutionContext executionContext);
    
    /**
     * Get the {@link FilterBypass} for this delivery configuration. 
     * @return The {@link FilterBypass} for this delivery configuration, or
     * null if non configured.
     */
    FilterBypass getFilterBypass();
}
