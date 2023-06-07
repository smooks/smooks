/*-
 * ========================LICENSE_START=================================
 * Core
 * %%
 * Copyright (C) 2020 - 2021 Smooks
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

import com.fasterxml.classmate.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.SmooksException;
import org.smooks.api.profile.ProfileSet;
import org.smooks.api.profile.ProfileStore;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.delivery.ContentHandler;
import org.smooks.api.Registry;
import org.smooks.assertion.AssertArgument;
import org.smooks.engine.converter.TypeConverterFactoryLoader;
import org.smooks.api.converter.TypeConverterFactory;
import org.smooks.api.resource.config.ResourceConfigSeq;
import org.smooks.engine.resource.config.DefaultResourceConfigSeq;
import org.smooks.engine.resource.config.XMLConfigDigester;
import org.smooks.engine.injector.Scope;
import org.smooks.engine.lifecycle.DefaultLifecycleManager;
import org.smooks.api.lifecycle.LifecycleManager;
import org.smooks.engine.lifecycle.PostConstructLifecyclePhase;
import org.smooks.engine.lifecycle.PreDestroyLifecyclePhase;
import org.smooks.engine.lookup.LifecycleManagerLookup;
import org.smooks.engine.lookup.ResourceConfigListsLookup;
import org.smooks.engine.lookup.SystemResourceConfigListLookup;
import org.smooks.engine.lookup.converter.TypeConverterFactoryLookup;
import org.smooks.api.resource.ContainerResourceLocator;
import org.xml.sax.SAXException;

import jakarta.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class DefaultRegistry implements Registry {
    private static final Logger LOGGER = LoggerFactory.getLogger(Registry.class);

    private final Map<Object, Object> registry = new ConcurrentHashMap<>();
    private final ContainerResourceLocator containerResourceLocator;
    private final ClassLoader classLoader;

    public DefaultRegistry(ClassLoader classLoader, ContainerResourceLocator containerResourceLocator, ProfileStore profileStore) {
        AssertArgument.isNotNull(containerResourceLocator, "containerResourceLocator");
        AssertArgument.isNotNull(profileStore, "profileStore");

        this.containerResourceLocator = containerResourceLocator;
        this.classLoader = classLoader;
        registerObject(ProfileStore.class, profileStore);

        final Set<TypeConverterFactory<?, ?>> typeConverterFactories = new TypeConverterFactoryLoader().load(classLoader);
        registerObject(TypeConverterFactoryLookup.TYPE_CONVERTER_FACTORY_REGISTRY_KEY, typeConverterFactories);
        registerObject(LifecycleManager.class, new DefaultLifecycleManager());

        // add the default list to the list.
        final ResourceConfigSeq systemResourceConfigList = new DefaultResourceConfigSeq("default");
        systemResourceConfigList.setSystemConfigList(true);

        registerObject(ResourceConfigSeq.class, systemResourceConfigList);

        final List<ResourceConfigSeq> resourceConfigLists = new ArrayList<>();
        resourceConfigLists.add(systemResourceConfigList);
        registerObject(new TypeResolver().resolve(List.class, ResourceConfigSeq.class), resourceConfigLists);
    }

    @Override
    public void registerObject(final Object value) {
        AssertArgument.isNotNull(value, "value");

        final String name;
        if (value.getClass().isAnnotationPresent(Resource.class) && value.getClass().getAnnotation(Resource.class).name().length() > 0) {
            name = value.getClass().getAnnotation(Resource.class).name();
        } else {
            name = value.getClass().getName() + ":" + UUID.randomUUID();
        }
        registerObject(name, value);
    }

    @Override
    public void registerObject(final Object key, final Object value) {
        AssertArgument.isNotNull(key, "key");
        AssertArgument.isNotNull(value, "value");

        if (registry.putIfAbsent(key, value) != null) {
            throw new SmooksException(String.format("Duplicate registration: %s", key));
        }
    }

    @Override
    public void deRegisterObject(Object key) {
        registry.remove(key);
    }

    @Override
    public <R> R lookup(final Function<Map<Object, Object>, R> function) {
        return function.apply(Collections.unmodifiableMap(registry));
    }

    @Override
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
            } catch (IllegalArgumentException | IOException | SAXException | URISyntaxException e) {
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
     * @return The ResourceConfigList created from the added resource configuration.
     * @throws SAXException Error parsing the resource stream.
     * @throws IOException  Error reading resource stream.
     * @see ResourceConfig
     */
    @Override
    public ResourceConfigSeq registerResources(String baseURI, InputStream resourceConfigStream) throws SAXException, IOException, URISyntaxException {
        AssertArgument.isNotEmpty(baseURI, "baseURI");
        AssertArgument.isNotNull(resourceConfigStream, "resourceConfigStream");

        ResourceConfigSeq resourceConfigList = XMLConfigDigester.digestConfig(resourceConfigStream, baseURI, classLoader);
        registerResourceConfigSeq(resourceConfigList);

        return resourceConfigList;
    }

    protected void addProfileSets(List<ProfileSet> profileSets) {
        final ProfileStore profileStore = lookup(ProfileStore.class);
        if (profileSets == null) {
            return;
        }

        for (ProfileSet profileSet : profileSets) {
            profileStore.addProfileSet(profileSet);
        }
    }


    /**
     * Register a {@link ResourceConfig} on this context store.
     * <p/>
     * The config gets added to the default resource list.
     *
     * @param resourceConfig The Content Delivery Resource definition to be registered.
     */
    @Override
    public void registerResourceConfig(ResourceConfig resourceConfig) {
        AssertArgument.isNotNull(resourceConfig, "resourceConfig");

        lookup(new LifecycleManagerLookup()).applyPhase(resourceConfig, new PostConstructLifecyclePhase(new Scope(this)));
        lookup(new SystemResourceConfigListLookup()).add(resourceConfig);
    }

    /**
     * Add a {@link ResourceConfigSeq} to this registry.
     *
     * @param resourceConfigSeq All the ResourceConfigList instances added on this registry.
     */
    @Override
    public void registerResourceConfigSeq(ResourceConfigSeq resourceConfigSeq) {
        lookup(new ResourceConfigListsLookup()).add(resourceConfigSeq);
        lookup(new LifecycleManagerLookup()).applyPhase(resourceConfigSeq, new PostConstructLifecyclePhase(new Scope(this)));

        // XSD v1.0 added profiles to the resource config.  If there were any, add them to the
        // profile store.
        addProfileSets(resourceConfigSeq.getProfiles());
    }

    @Override
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

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }
}
