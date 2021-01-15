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
package org.smooks.container.standalone;

import org.smooks.cdr.SystemResourceConfigListFactory;
import org.smooks.container.ApplicationContext;
import org.smooks.container.ApplicationContextBuilder;
import org.smooks.delivery.ContentDeliveryRuntimeFactory;
import org.smooks.delivery.ContentHandlerFactory;
import org.smooks.delivery.DefaultContentDeliveryRuntimeFactory;
import org.smooks.injector.Scope;
import org.smooks.lifecycle.phase.PostConstructLifecyclePhase;
import org.smooks.payload.Exports;
import org.smooks.profile.DefaultProfileSet;
import org.smooks.profile.Profile;
import org.smooks.registry.Registry;
import org.smooks.registry.lookup.LifecycleManagerLookup;

import java.util.ServiceLoader;

public class DefaultApplicationContextBuilder implements ApplicationContextBuilder {

    private boolean registerSystemResources = true;
    private ClassLoader classLoader;
    private Registry registry;
    private ContentDeliveryRuntimeFactory contentDeliveryConfigBuilderFactory;

    public DefaultApplicationContextBuilder setClassLoader(final ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    public DefaultApplicationContextBuilder setRegisterSystemResources(final boolean registerSystemResources) {
        this.registerSystemResources = registerSystemResources;
        return this;
    }

    public DefaultApplicationContextBuilder setRegistry(final Registry registry) {
        this.registry = registry;
        return this;
    }

    public DefaultApplicationContextBuilder setContentDeliveryConfigBuilderFactory(ContentDeliveryRuntimeFactory contentDeliveryConfigBuilderFactory) {
        this.contentDeliveryConfigBuilderFactory = contentDeliveryConfigBuilderFactory;
        return this;
    }

    @Override
    public ApplicationContext build() {
        final StandaloneApplicationContext applicationContext = new StandaloneApplicationContext();
        applicationContext.setClassLoader(classLoader);

        final Registry appContextRegistry;
        if (registry == null) {
            appContextRegistry = new Registry(applicationContext.getClassLoader(), applicationContext.getResourceLocator(), applicationContext.getProfileStore());
        } else {
            appContextRegistry = registry;
        }

        appContextRegistry.registerObject(ApplicationContext.class, applicationContext);
        appContextRegistry.registerObject(Exports.class, new Exports());
        registerSystemContentHandlerFactories(appContextRegistry);
        if (registerSystemResources) {
            registerSystemResources(appContextRegistry);
        }
        applicationContext.setRegistry(appContextRegistry);

        if (contentDeliveryConfigBuilderFactory == null) {
            applicationContext.setContentDeliveryConfigBuilderFactory(new DefaultContentDeliveryRuntimeFactory(applicationContext.getRegistry()));
        } else {
            applicationContext.setContentDeliveryConfigBuilderFactory(contentDeliveryConfigBuilderFactory);
        }
        applicationContext.getProfileStore().addProfileSet(new DefaultProfileSet(Profile.DEFAULT_PROFILE));
        
        return applicationContext;
    }
    
    private void registerSystemContentHandlerFactories(final Registry registry) {
        for (ContentHandlerFactory<?> contentHandlerFactory : ServiceLoader.load(ContentHandlerFactory.class)) {
            registry.lookup(new LifecycleManagerLookup()).applyPhase(contentHandlerFactory, new PostConstructLifecyclePhase(new Scope(registry)));
            registry.registerObject(contentHandlerFactory);
        }
    }
    
    private void registerSystemResources(final Registry registry) {
        registry.registerResourceConfigList(new SystemResourceConfigListFactory("/null-dom.xml", registry.getClassLoader()).create());
        registry.registerResourceConfigList(new SystemResourceConfigListFactory("/null-sax.xml", registry.getClassLoader()).create());
        registry.registerResourceConfigList(new SystemResourceConfigListFactory("/system-param-decoders.xml", registry.getClassLoader()).create());
        registry.registerResourceConfigList(new SystemResourceConfigListFactory("/system-serializers.xml", registry.getClassLoader()).create());
        registry.registerResourceConfigList(new SystemResourceConfigListFactory("/system-interceptors.xml", registry.getClassLoader()).create());
    }
}
