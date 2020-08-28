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
package org.smooks.container;

import org.smooks.cdr.registry.Registry;
import org.smooks.delivery.ContentDeliveryConfigBuilderFactory;
import org.smooks.javabean.context.BeanContext;
import org.smooks.javabean.context.BeanIdStore;
import org.smooks.javabean.lifecycle.BeanContextLifecycleObserver;
import org.smooks.profile.ProfileStore;
import org.smooks.resource.ContainerResourceLocator;

import java.util.Collection;

/**
 * Smooks Application context interface definition.
 * @author tfennelly
 */
public interface ApplicationContext {

    /**
	 * Get the container resource locator for the context.
	 * @return ContainerResourceLocator for the context.
	 */
	ContainerResourceLocator getResourceLocator();

    /**
     * Set the resource locator for this Smooks application context.
     * @param resourceLocator The Resource locator.
     */
    void setResourceLocator(ContainerResourceLocator resourceLocator);

    /**
	 * Get the registry for from the container application context.
	 * @return Registry instance.
	 */
	Registry getRegistry();

    /**
	 * Get the ProfileStore in use within this Context.
	 * @return The ProfileStore.
	 */
    ProfileStore getProfileStore();

    /**
     * Get the BeanIdStore in use within this Context
     * @return The BeanIdStore
     */
    BeanIdStore getBeanIdStore();

    /**
     * Registers a bean context observer.
     * <p/>
     * This observer instance will be automatically added to all
     * {@link BeanContext#addObserver(org.smooks.javabean.lifecycle.BeanContextLifecycleObserver) BeanContext}
     * instances.
     *
     * @param observer The actual BeanObserver instance.
     */
    void addBeanContextLifecycleObserver(BeanContextLifecycleObserver observer);

    /**
     * Get bean context observers.
     * <p/>
     * These observer instances will be automatically added to all
     * {@link BeanContext#addObserver(org.smooks.javabean.lifecycle.BeanContextLifecycleObserver) BeanContext}
     * instances.
     *
     * @return The collection of BeanObserver instance.
     * @see #addBeanContextLifecycleObserver(org.smooks.javabean.lifecycle.BeanContextLifecycleObserver)
     */
    Collection<BeanContextLifecycleObserver> getBeanContextLifecycleObservers();

    /**
     * Get the {@link ClassLoader} to be used by the associated Smooks instance
     *
     * @return The ClassLoader.
     */
    ClassLoader getClassLoader();
    
    ContentDeliveryConfigBuilderFactory getContentDeliveryConfigBuilderFactory();
}
