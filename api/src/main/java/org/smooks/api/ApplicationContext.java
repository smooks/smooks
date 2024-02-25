/*-
 * ========================LICENSE_START=================================
 * API
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
package org.smooks.api;

import org.smooks.api.delivery.ContentDeliveryRuntimeFactory;
import org.smooks.api.bean.context.BeanContext;
import org.smooks.api.bean.context.BeanIdStore;
import org.smooks.api.bean.lifecycle.BeanContextLifecycleObserver;
import org.smooks.api.profile.ProfileStore;
import org.smooks.api.resource.ContainerResourceLocator;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;

/**
 * Application-scoped service locator.
 * <p>
 * Clients obtain references to application services like {@link Registry} from the <code>ApplicationContext</code>. A 
 * Smooks resource obtains a reference to an <code>ApplicationContext</code> by annotating an
 * <code>ApplicationContext</code> field in the resource class with {@link javax.inject.Inject}.
 */
@ThreadSafe
public interface ApplicationContext {

    /**
	 * Get the container resource locator for the context.
	 * @return ContainerResourceLocator for the context.
	 */
	ContainerResourceLocator getResourceLocator();

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
     * {@link BeanContext#addObserver(org.smooks.bean.lifecycle.BeanContextLifecycleObserver) BeanContext}
     * instances.
     *
     * @param observer The actual BeanObserver instance.
     */
    void addBeanContextLifecycleObserver(BeanContextLifecycleObserver observer);

    /**
     * Get bean context observers.
     * <p/>
     * These observer instances will be automatically added to all
     * {@link BeanContext#addObserver(org.smooks.bean.lifecycle.BeanContextLifecycleObserver) BeanContext}
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
    
    ContentDeliveryRuntimeFactory getContentDeliveryRuntimeFactory();
}
