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

import org.smooks.api.delivery.ReaderPoolFactory;
import org.smooks.api.profile.Profile;
import org.smooks.api.resource.ContainerResourceLocator;
import org.smooks.api.resource.config.loader.ResourceConfigLoader;
import org.smooks.engine.delivery.DefaultReaderPoolFactory;
import org.smooks.engine.resource.config.SystemResourceConfigSeqFactory;
import org.smooks.api.ApplicationContext;
import org.smooks.api.ApplicationContextBuilder;
import org.smooks.api.delivery.ContentDeliveryRuntimeFactory;
import org.smooks.api.delivery.ContentHandlerFactory;
import org.smooks.engine.delivery.DefaultContentDeliveryRuntimeFactory;
import org.smooks.engine.injector.Scope;
import org.smooks.engine.lifecycle.PostConstructLifecyclePhase;
import org.smooks.engine.resource.config.loader.xml.XmlResourceConfigLoader;
import org.smooks.io.payload.Exports;
import org.smooks.engine.profile.DefaultProfileSet;
import org.smooks.api.Registry;
import org.smooks.engine.lookup.LifecycleManagerLookup;
import org.smooks.resource.URIResourceLocator;

import java.util.ServiceLoader;

public class DefaultApplicationContextBuilder implements ApplicationContextBuilder {

    private boolean systemResources = true;
    private ClassLoader classLoader = getClass().getClassLoader();
    private Registry registry;
    private ContentDeliveryRuntimeFactory contentDeliveryRuntimeFactory;
    private ContainerResourceLocator resourceLocator = new URIResourceLocator();
    private ResourceConfigLoader resourceConfigLoader = new XmlResourceConfigLoader();

    private ReaderPoolFactory readerPoolFactory = new DefaultReaderPoolFactory();

    public DefaultApplicationContextBuilder() {

    }

    protected DefaultApplicationContextBuilder(boolean systemResources, ClassLoader classLoader, Registry registry, ContentDeliveryRuntimeFactory contentDeliveryRuntimeFactory, ContainerResourceLocator resourceLocator, ResourceConfigLoader resourceConfigLoader, ReaderPoolFactory readerPoolFactory) {
        this.systemResources = systemResources;
        this.classLoader = classLoader;
        this.registry = registry;
        this.contentDeliveryRuntimeFactory = contentDeliveryRuntimeFactory;
        this.resourceLocator = resourceLocator;
        this.resourceConfigLoader = resourceConfigLoader;
        this.readerPoolFactory = readerPoolFactory;
    }

    @Override
    public DefaultApplicationContextBuilder withClassLoader(ClassLoader classLoader) {
        return new DefaultApplicationContextBuilder(systemResources, classLoader, registry, contentDeliveryRuntimeFactory, resourceLocator, resourceConfigLoader, readerPoolFactory);
    }

    public DefaultApplicationContextBuilder withSystemResources(boolean systemResources) {
        return new DefaultApplicationContextBuilder(systemResources, classLoader, registry, contentDeliveryRuntimeFactory, resourceLocator, resourceConfigLoader, readerPoolFactory);
    }

    @Override
    public DefaultApplicationContextBuilder withRegistry(Registry registry) {
        return new DefaultApplicationContextBuilder(systemResources, classLoader, registry, contentDeliveryRuntimeFactory, resourceLocator, resourceConfigLoader, readerPoolFactory);
    }

    @Override
    public DefaultApplicationContextBuilder withContentDeliveryRuntimeFactory(ContentDeliveryRuntimeFactory contentDeliveryRuntimeFactory) {
        return new DefaultApplicationContextBuilder(systemResources, classLoader, registry, contentDeliveryRuntimeFactory, resourceLocator, resourceConfigLoader, readerPoolFactory);
    }

    @Override
    public DefaultApplicationContextBuilder withResourceLocator(ContainerResourceLocator resourceLocator) {
        return new DefaultApplicationContextBuilder(systemResources, classLoader, registry, contentDeliveryRuntimeFactory, resourceLocator, resourceConfigLoader, readerPoolFactory);
    }

    @Override
    public DefaultApplicationContextBuilder withResourceConfigLoader(ResourceConfigLoader resourceConfigLoader) {
        return new DefaultApplicationContextBuilder(systemResources, classLoader, registry, contentDeliveryRuntimeFactory, resourceLocator, resourceConfigLoader, readerPoolFactory);
    }

    @Override
    public DefaultApplicationContextBuilder withReaderPoolFactory(ReaderPoolFactory readerPoolFactory) {
        return new DefaultApplicationContextBuilder(systemResources, classLoader, registry, contentDeliveryRuntimeFactory, resourceLocator, resourceConfigLoader, readerPoolFactory);
    }

    @Override
    public ApplicationContext build() {
        final DefaultApplicationContext applicationContext = new DefaultApplicationContext();
        applicationContext.setClassLoader(classLoader);
        applicationContext.setResourceConfigLoader(resourceConfigLoader);
        applicationContext.setReaderPoolFactory(readerPoolFactory);
        applicationContext.setResourceLocator(resourceLocator);

        Registry applicationContextRegistry;
        if (registry == null) {
            applicationContextRegistry = new DefaultRegistry(applicationContext.getClassLoader(), applicationContext.getResourceConfigLoader(), applicationContext.getProfileStore());
        } else {
            applicationContextRegistry = registry;
        }
        initRegistry(applicationContext, applicationContextRegistry);
        applicationContext.setRegistry(applicationContextRegistry);

        if (contentDeliveryRuntimeFactory == null) {
            applicationContext.setContentDeliveryRuntimeFactory(new DefaultContentDeliveryRuntimeFactory(applicationContext.getRegistry(), applicationContext.getReaderPoolFactory()));
        } else {
            applicationContext.setContentDeliveryRuntimeFactory(contentDeliveryRuntimeFactory);
        }

        applicationContext.getProfileStore().addProfileSet(new DefaultProfileSet(Profile.DEFAULT_PROFILE));

        return applicationContext;
    }

    protected void initRegistry(ApplicationContext applicationContext, Registry registry) {
        registry.registerObject(ApplicationContext.class, applicationContext);
        registry.registerObject(Exports.class, new Exports());
        registerSystemContentHandlerFactories(registry);
        if (systemResources) {
            registerSystemResources(registry, applicationContext);
        }
    }

    protected void registerSystemContentHandlerFactories(final Registry registry) {
        for (ContentHandlerFactory<?> contentHandlerFactory : ServiceLoader.load(ContentHandlerFactory.class, classLoader)) {
            registry.lookup(new LifecycleManagerLookup()).applyPhase(contentHandlerFactory, new PostConstructLifecyclePhase(new Scope(registry)));
            registry.registerObject(contentHandlerFactory);
        }
    }

    protected void registerSystemResources(final Registry registry, final ApplicationContext applicationContext) {
        registry.registerResourceConfigSeq(new SystemResourceConfigSeqFactory("/null-dom.xml", registry.getClassLoader(), applicationContext.getResourceLocator(), applicationContext.getResourceConfigLoader()).create());
        registry.registerResourceConfigSeq(new SystemResourceConfigSeqFactory("/null-sax.xml", registry.getClassLoader(), applicationContext.getResourceLocator(), applicationContext.getResourceConfigLoader()).create());
        registry.registerResourceConfigSeq(new SystemResourceConfigSeqFactory("/system-param-decoders.xml", registry.getClassLoader(), applicationContext.getResourceLocator(), applicationContext.getResourceConfigLoader()).create());
        registry.registerResourceConfigSeq(new SystemResourceConfigSeqFactory("/system-serializers.xml", registry.getClassLoader(), applicationContext.getResourceLocator(), applicationContext.getResourceConfigLoader()).create());
        registry.registerResourceConfigSeq(new SystemResourceConfigSeqFactory("/system-interceptors.xml", registry.getClassLoader(), applicationContext.getResourceLocator(), applicationContext.getResourceConfigLoader()).create());
    }
}
