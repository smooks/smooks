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
package org.smooks.engine;

import org.smooks.api.ApplicationContext;
import org.smooks.api.Registry;
import org.smooks.api.bean.context.BeanIdStore;
import org.smooks.api.bean.lifecycle.BeanContextLifecycleObserver;
import org.smooks.api.delivery.ContentDeliveryRuntimeFactory;
import org.smooks.api.delivery.ReaderPoolFactory;
import org.smooks.api.profile.ProfileStore;
import org.smooks.api.resource.ContainerResourceLocator;
import org.smooks.api.resource.config.loader.ResourceConfigLoader;
import org.smooks.engine.bean.context.DefaultBeanIdStore;
import org.smooks.engine.profile.DefaultProfileStore;
import org.smooks.resource.URIResourceLocator;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Standalone container execution context for Smooks.
 * <p/>
 * This context allows Smooks to be executed outside the likes of a
 * Servlet Container.
 *
 * @author tfennelly
 */
public class DefaultApplicationContext implements ApplicationContext {

    private ContainerResourceLocator resourceLocator;
    private Registry registry;
    private final ProfileStore profileStore = new DefaultProfileStore();
    private final BeanIdStore beanIdStore = new DefaultBeanIdStore();
    private final List<BeanContextLifecycleObserver> beanContextObservers = new ArrayList<>();
    private ClassLoader classLoader;
    private ContentDeliveryRuntimeFactory contentDeliveryRuntimeFactory;
    private ResourceConfigLoader resourceConfigLoader;
    private ReaderPoolFactory readerPoolFactory;


    /**
     * Private constructor.
     */
    DefaultApplicationContext() {
        resourceLocator = new URIResourceLocator();
        ((URIResourceLocator) resourceLocator).setBaseURI(URI.create(URIResourceLocator.SCHEME_CLASSPATH + ":/"));
    }

    @Override
    public ContainerResourceLocator getResourceLocator() {
        return resourceLocator;
    }

    /**
     * Set the resource locator for this Smooks application context.
     *
     * @param resourceLocator The Resource locator.
     */
    public void setResourceLocator(ContainerResourceLocator resourceLocator) {
        this.resourceLocator = resourceLocator;
    }

    @Override
    public Registry getRegistry() {
        return registry;
    }

    void setRegistry(Registry registry) {
        this.registry = registry;
    }

    /**
     * Get the ProfileStore in use within the Standalone Context.
     *
     * @return The ProfileStore.
     */
    @Override
    public ProfileStore getProfileStore() {
        return profileStore;
    }

    @Override
    public BeanIdStore getBeanIdStore() {
        return beanIdStore;
    }

    @Override
    public void addBeanContextLifecycleObserver(BeanContextLifecycleObserver observer) {
        beanContextObservers.add(observer);
    }

    @Override
    public Collection<BeanContextLifecycleObserver> getBeanContextLifecycleObservers() {
        return Collections.unmodifiableCollection(beanContextObservers);
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setContentDeliveryRuntimeFactory(ContentDeliveryRuntimeFactory contentDeliveryRuntimeFactory) {
        this.contentDeliveryRuntimeFactory = contentDeliveryRuntimeFactory;
    }

    @Override
    public ContentDeliveryRuntimeFactory getContentDeliveryRuntimeFactory() {
        return contentDeliveryRuntimeFactory;
    }

    @Override
    public ResourceConfigLoader getResourceConfigLoader() {
        return resourceConfigLoader;
    }

    public void setResourceConfigLoader(ResourceConfigLoader resourceConfigLoader) {
        this.resourceConfigLoader = resourceConfigLoader;
    }

    @Override
    public ReaderPoolFactory getReaderPoolFactory() {
        return readerPoolFactory;
    }

    public void setReaderPoolFactory(ReaderPoolFactory readerPoolFactory) {
        this.readerPoolFactory = readerPoolFactory;
    }
}
