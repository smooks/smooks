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

import org.smooks.cdr.SystemSmooksResourceConfigurationListFactory;
import org.smooks.cdr.injector.Scope;
import org.smooks.cdr.lifecycle.phase.PostConstructLifecyclePhase;
import org.smooks.cdr.registry.Registry;
import org.smooks.cdr.registry.lookup.LifecycleManagerLookup;
import org.smooks.container.ApplicationContext;
import org.smooks.container.ApplicationContextBuilder;
import org.smooks.delivery.ContentHandlerFactory;
import org.smooks.delivery.DefaultContentDeliveryConfigBuilderFactory;
import org.smooks.profile.DefaultProfileSet;
import org.smooks.profile.Profile;

import java.util.Iterator;
import java.util.ServiceLoader;

public class DefaultApplicationContextBuilder implements ApplicationContextBuilder {

    private boolean registerInstalledResources = true;
    private ClassLoader classLoader;

    public DefaultApplicationContextBuilder setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    public DefaultApplicationContextBuilder setRegisterInstalledResources(boolean registerInstalledResources) {
        this.registerInstalledResources = registerInstalledResources;
        return this;
    }
    
    @Override
    public StandaloneApplicationContext build() {
        final StandaloneApplicationContext standaloneApplicationContext = new StandaloneApplicationContext();
        standaloneApplicationContext.setContentDeliveryConfigBuilderFactory(new DefaultContentDeliveryConfigBuilderFactory(standaloneApplicationContext));
        
        final Registry registry = new Registry(standaloneApplicationContext.getClassLoader(), standaloneApplicationContext.getResourceLocator(), standaloneApplicationContext.getProfileStore());
        registry.registerObject(ApplicationContext.class, standaloneApplicationContext);
        
        registerInstalledContentHandlerFactories(registry);
        if (registerInstalledResources) {
            registerInstalledResources(registry);
        }
         
        standaloneApplicationContext.setRegistry(registry);
        standaloneApplicationContext.getProfileStore().addProfileSet(new DefaultProfileSet(Profile.DEFAULT_PROFILE));
        
        return standaloneApplicationContext;
    }


    private void registerInstalledContentHandlerFactories(final Registry registry) {
        final Iterator<ContentHandlerFactory> contentHandlerFactoryIterator = ServiceLoader.load(ContentHandlerFactory.class).iterator();
        while (contentHandlerFactoryIterator.hasNext()) {
            final ContentHandlerFactory contentHandlerFactory = contentHandlerFactoryIterator.next();
            registry.lookup(new LifecycleManagerLookup()).applyPhase(contentHandlerFactory, new PostConstructLifecyclePhase(new Scope(registry)));
            registry.registerObject(contentHandlerFactory);
        }
    }

    /**
     * Register the pre-installed CDU Creator classes.
     *
     * @param resourceFile Installed (internal) resource config file.
     */
    private void registerInstalledResources(final Registry registry) {
        registry.addSmooksResourceConfigurationList(new SystemSmooksResourceConfigurationListFactory("/null-dom.cdrl", classLoader).create());
        registry.addSmooksResourceConfigurationList(new SystemSmooksResourceConfigurationListFactory("/null-sax.cdrl", classLoader).create());
        registry.addSmooksResourceConfigurationList(new SystemSmooksResourceConfigurationListFactory("/installed-param-decoders.cdrl", classLoader).create());
        registry.addSmooksResourceConfigurationList(new SystemSmooksResourceConfigurationListFactory("/installed-serializers.cdrl", classLoader).create());
    }
}
