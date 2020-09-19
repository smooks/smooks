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
package org.smooks.cdr.registry;

import com.fasterxml.classmate.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.SmooksException;
import org.smooks.assertion.AssertArgument;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.cdr.SmooksResourceConfigurationList;
import org.smooks.cdr.XMLConfigDigester;
import org.smooks.cdr.injector.Scope;
import org.smooks.cdr.lifecycle.DefaultLifecycleManager;
import org.smooks.cdr.lifecycle.LifecycleManager;
import org.smooks.cdr.lifecycle.phase.PostConstructLifecyclePhase;
import org.smooks.cdr.lifecycle.phase.PreDestroyLifecyclePhase;
import org.smooks.cdr.registry.lookup.LifecycleManagerLookup;
import org.smooks.cdr.registry.lookup.SmooksResourceConfigurationListsLookup;
import org.smooks.cdr.registry.lookup.SystemSmooksResourceConfigurationListLookup;
import org.smooks.cdr.registry.lookup.converter.TypeConverterFactoryLookup;
import org.smooks.converter.TypeConverterFactoryLoader;
import org.smooks.converter.factory.TypeConverterFactory;
import org.smooks.profile.ProfileSet;
import org.smooks.profile.ProfileStore;
import org.smooks.resource.ContainerResourceLocator;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * {@link org.smooks.cdr.SmooksResourceConfiguration} context store.
 * <p/>
 * Stores the {@link org.smooks.cdr.SmooksResourceConfiguration SmooksResourceConfigurations}
 * for a given container context in the form of
 * {@link org.smooks.cdr.SmooksResourceConfigurationList} entries.  Also maintains
 * a "default" config list for the context.
 *
 * @author tfennelly
 */
public class Registry {

    private static final Logger LOGGER = LoggerFactory.getLogger(Registry.class);
   
    private final Map<Object, Object> registry = new ConcurrentHashMap<>();
    private final ContainerResourceLocator containerResourceLocator;
    private final ClassLoader classLoader;

    public Registry(ClassLoader classLoader, ContainerResourceLocator containerResourceLocator, ProfileStore profileStore) {
        AssertArgument.isNotNull(containerResourceLocator, "containerResourceLocator");
        AssertArgument.isNotNull(profileStore, "profileStore");

        this.containerResourceLocator = containerResourceLocator;
        this.classLoader = classLoader;
        registerObject(ProfileStore.class, profileStore);

        final Set<TypeConverterFactory<?, ?>> typeConverterFactories = new TypeConverterFactoryLoader().load();
        registerObject(TypeConverterFactoryLookup.TYPE_CONVERTER_FACTORY_REGISTRY_KEY, typeConverterFactories);
        registerObject(LifecycleManager.class, new DefaultLifecycleManager());
 
        // add the default list to the list.
        final SmooksResourceConfigurationList systemSmooksResourceConfigurationList = new SmooksResourceConfigurationList("default");
        systemSmooksResourceConfigurationList.setSystemConfigList(true);

        registerObject(SmooksResourceConfigurationList.class, systemSmooksResourceConfigurationList);

        final List<SmooksResourceConfigurationList> smooksResourceConfigurationLists = new ArrayList<>();
        smooksResourceConfigurationLists.add(systemSmooksResourceConfigurationList);
        registerObject(new TypeResolver().resolve(List.class, SmooksResourceConfigurationList.class), smooksResourceConfigurationLists);
    }

    public void registerObject(final Object value) {
        AssertArgument.isNotNull(value, "value");

        final String name;
        if (value.getClass().isAnnotationPresent(Resource.class) && value.getClass().getAnnotation(Resource.class).name().length() > 0) {
            name = value.getClass().getAnnotation(Resource.class).name();
        } else {
            name = value.getClass().getName() + ":" + UUID.randomUUID().toString();
        }
        registerObject(name, value);
    }
    
    public void registerObject(final Object key, final Object value) {
        AssertArgument.isNotNull(key, "key");
        AssertArgument.isNotNull(value, "value");
     
        if (registry.putIfAbsent(key, value) != null) {
            throw new SmooksException(String.format("Duplicate registration: %s", key));
        }
    }

    public void deRegisterObject(Object key) {
        registry.remove(key);
    }
    
    public <R> R lookup(final Function<Map<Object, Object>, R> function) {
        return function.apply(Collections.unmodifiableMap(registry));
    }

    public <T> T lookup(final Object key) {
        return (T) registry.get(key);
    }

    /**
     * Load all .cdrl files listed in the BufferedReader stream.
     * <p/>
     * Because this method uses the ContainerResourceLocator it may be possible
     * to load external cdrl files.  If the ContainerResourceLocator is a
     * ServletResourceLocator the lines in the BufferedReader param can contain
     * external URLs.
     *
     * @param cdrlLoadList BufferedReader cdrl list - one cdrl def per line.
     * @throws java.io.IOException Error reading list buffer.
     */
    public void load(BufferedReader cdrlLoadList) throws IOException {
        String uri;

        while ((uri = cdrlLoadList.readLine()) != null) {
            uri = uri.trim();
            if (uri.equals("") || uri.charAt(0) == '#') {
                continue;
            }

            try {
                InputStream resource = containerResourceLocator.getResource(uri);

                LOGGER.info("Loading Smooks Resources from uri [" + uri + "].");
                registerResources(uri, resource);
                LOGGER.debug("[" + uri + "] Loaded.");
            } catch (IllegalArgumentException e) {
                LOGGER.error("[" + uri + "] Load failure. " + e.getMessage(), e);
            } catch (IOException e) {
                LOGGER.error("[" + uri + "] Load failure. " + e.getMessage(), e);
            } catch (SAXException e) {
                LOGGER.error("[" + uri + "] Load failure. " + e.getMessage(), e);
            } catch (URISyntaxException e) {
                LOGGER.error("[" + uri + "] Load failure. " + e.getMessage(), e);
            }
        }
    }

    /**
     * Register the set of resources specified in the supplied XML configuration
     * stream.
     *
     * @param baseURI              The base URI to be associated with the configuration stream.
     * @param resourceConfigStream XML resource configuration stream.
     * @return The SmooksResourceConfigurationList created from the added resource configuration.
     * @throws SAXException Error parsing the resource stream.
     * @throws IOException  Error reading resource stream.
     * @see SmooksResourceConfiguration
     */
    public SmooksResourceConfigurationList registerResources(String baseURI, InputStream resourceConfigStream) throws SAXException, IOException, URISyntaxException {
        AssertArgument.isNotEmpty(baseURI, "baseURI");
        AssertArgument.isNotNull(resourceConfigStream, "resourceConfigStream");
        
        SmooksResourceConfigurationList smooksResourceConfigurationList = XMLConfigDigester.digestConfig(resourceConfigStream, baseURI, classLoader);
        registerSmooksResourceConfigurationList(smooksResourceConfigurationList);

        return smooksResourceConfigurationList;
    }
    
    private void addProfileSets(List<ProfileSet> profileSets) {
        final ProfileStore profileStore = lookup(ProfileStore.class);
        if (profileSets == null) {
            return;
        }
        
        for (ProfileSet profileSet : profileSets) {
            profileStore.addProfileSet(profileSet);
        }
    }


    /**
     * Register a {@link SmooksResourceConfiguration} on this context store.
     * <p/>
     * The config gets added to the default resource list.
     *
     * @param resourceConfig The Content Delivery Resource definition to be registered.
     */
    public void registerSmooksResourceConfiguration(SmooksResourceConfiguration smooksResourceConfiguration) {
        AssertArgument.isNotNull(smooksResourceConfiguration, "smooksResourceConfiguration");

        lookup(new LifecycleManagerLookup()).applyPhase(smooksResourceConfiguration, new PostConstructLifecyclePhase(new Scope(this)));
        lookup(new SystemSmooksResourceConfigurationListLookup()).add(smooksResourceConfiguration);
    }

    /**
     * Add a {@link SmooksResourceConfigurationList} to this registry.
     *
     * @param smooksResourceConfigurationList All the SmooksResourceConfigurationList instances added on this registry.
     */
    public void registerSmooksResourceConfigurationList(SmooksResourceConfigurationList smooksResourceConfigurationList) {
        lookup(new SmooksResourceConfigurationListsLookup()).add(smooksResourceConfigurationList);
        lookup(new LifecycleManagerLookup()).applyPhase(smooksResourceConfigurationList, new PostConstructLifecyclePhase(new Scope(this)));

        // XSD v1.0 added profiles to the resource config.  If there were any, add them to the
        // profile store.
        addProfileSets(smooksResourceConfigurationList.getProfiles());
    }

    /**
     * Close this resource configuration store, {@link javax.annotation.PreDestroy uninitializing}
     * all {@link org.smooks.delivery.ContentHandler ContentHandlers} allocated from this store instance.
     */
    public void close() {
        LOGGER.debug("Un-initializing all ContentHandler instances allocated through this registry");
        for (Object registeredObject : registry.values()) {
            LOGGER.debug("Un-initializing ContentHandler instance: " + registeredObject.getClass().getName());
            try {
                lookup(new LifecycleManagerLookup()).applyPhase(registeredObject, new PreDestroyLifecyclePhase());
            } catch (Throwable throwable) {
                LOGGER.error("Error un-initializing " + registeredObject.getClass().getName() + ".", throwable);
            }
        }
    }
}
