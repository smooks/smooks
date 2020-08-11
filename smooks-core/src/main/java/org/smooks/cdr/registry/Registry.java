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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.assertion.AssertArgument;
import org.smooks.cdr.SmooksConfigurationException;
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
import org.smooks.classpath.ClasspathUtils;
import org.smooks.container.ApplicationContextInitializer;
import org.smooks.converter.TypeConverterDescriptor;
import org.smooks.converter.TypeConverterFactoryLoader;
import org.smooks.converter.factory.TypeConverterFactory;
import org.smooks.delivery.ContentHandler;
import org.smooks.delivery.ContentHandlerFactory;
import org.smooks.delivery.JavaContentHandlerFactory;
import org.smooks.delivery.UnsupportedContentHandlerTypeException;
import org.smooks.profile.ProfileSet;
import org.smooks.profile.ProfileStore;
import org.smooks.resource.ContainerResourceLocator;
import org.smooks.util.ClassUtil;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
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

    private static final List<Class<ContentHandlerFactory>> HANDLER_FACTORIES = ClassUtil.getClasses("META-INF/content-handlers.inf", ContentHandlerFactory.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(Registry.class);
    private static final String CDU_CREATOR = "cdu-creator";
   
    private final Map<Object, Object> registry = new ConcurrentHashMap<>();
    private final ContainerResourceLocator containerResourceLocator;
    private final ClassLoader classLoader;
    private final ProfileStore profileStore;

    public Registry(ClassLoader classLoader, ContainerResourceLocator containerResourceLocator, Boolean registerInstalledResources, ProfileStore profileStore) {
        AssertArgument.isNotNull(containerResourceLocator, "containerResourceLocator");
        AssertArgument.isNotNull(registerInstalledResources, "registerInstalledResources");
        AssertArgument.isNotNull(profileStore, "profileStore");

        this.containerResourceLocator = containerResourceLocator;
        this.classLoader = classLoader;
        this.profileStore = profileStore;

        // add the default list to the list.
        final SmooksResourceConfigurationList systemSmooksResourceConfigurationList = new SmooksResourceConfigurationList("default");
        systemSmooksResourceConfigurationList.setSystemConfigList(true);

        registry.put(SmooksResourceConfigurationList.class, systemSmooksResourceConfigurationList);

        final List<SmooksResourceConfigurationList> smooksResourceConfigurationLists = new ArrayList<>();
        smooksResourceConfigurationLists.add(systemSmooksResourceConfigurationList);
        registry.put(SmooksResourceConfigurationList[].class, smooksResourceConfigurationLists);
        
        registerInstalledHandlerFactories();
        if (registerInstalledResources) {
            registerInstalledResources("/null-dom.cdrl");
            registerInstalledResources("/null-sax.cdrl");
            registerInstalledResources("/installed-param-decoders.cdrl");
            registerInstalledResources("/installed-serializers.cdrl");
        }

        final Map<TypeConverterDescriptor<?, ?>, TypeConverterFactory<?, ?>> typeConverterFactories = new TypeConverterFactoryLoader().load();
        registry.put(TypeConverterFactory[].class, typeConverterFactories);
        registry.put(LifecycleManager.class, new DefaultLifecycleManager());
    }

    public void registerObject(Object value) {
        final String name;
        if (value.getClass().isAnnotationPresent(Resource.class) && value.getClass().getAnnotation(Resource.class).name().length() > 0) {
            name = value.getClass().getAnnotation(Resource.class).name();
        } else {
            name = value.getClass().getName() + ":" + UUID.randomUUID().toString();
        }
        registry.put(name, value);
    }
    
    public void registerObject(Object key, Object value) {
        registry.put(key, value);
    }

    public void deRegisterObject(Object key) {
        registry.remove(key);
    }
    
    public <R> R lookup(final Function<Map<Object, Object>, R> function) {
        return function.apply(Collections.unmodifiableMap(registry));
    }

    public Object lookup(final Object key) {
        return registry.get(key);
    }
    
    private void registerInstalledHandlerFactories() {
        for (Class<ContentHandlerFactory> handlerFactory : HANDLER_FACTORIES) {
            Resource resourceAnnotation = handlerFactory.getAnnotation(Resource.class);

            if (resourceAnnotation != null) {
                addHandlerFactoryConfig(handlerFactory, resourceAnnotation.name());
            }
        }

        // And add the Java handler...
        addHandlerFactoryConfig(JavaContentHandlerFactory.class, "class");
    }

    private void addHandlerFactoryConfig(Class handlerFactory, String type) {
        SmooksResourceConfiguration res = new SmooksResourceConfiguration(CDU_CREATOR);
        res.setTargetProfile("*");
        res.setResource(handlerFactory.getName());
        res.setParameter(ContentHandlerFactory.PARAM_RESTYPE, type);
        lookup(new SystemSmooksResourceConfigurationListLookup()).add(res);
    }

    /**
     * Register the pre-installed CDU Creator classes.
     *
     * @param resourceFile Installed (internal) resource config file.
     */
    private void registerInstalledResources(String resourceFile) {
        InputStream resource = getClass().getResourceAsStream(resourceFile);

        if (resource == null) {
            throw new IllegalStateException("Failed to load " + resourceFile);
        }
        try {
            SmooksResourceConfigurationList resourceList = registerResources(resourceFile, resource);
            for (int i = 0; i < resourceList.size(); i++) {
                resourceList.get(i).setDefaultResource(true);
            }
            resourceList.setSystemConfigList(true);
        } catch (Exception e) {
            throw new IllegalStateException("Error processing resource file '" + resourceFile + "'.", e);
        }
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
        SmooksResourceConfigurationList configList;

        if (baseURI == null || baseURI.trim().equals("")) {
            throw new IllegalArgumentException("null or empty 'name' arg in method call.");
        }
        if (resourceConfigStream == null) {
            throw new IllegalArgumentException("null 'resourceConfigStream' arg in method call.");
        }

        configList = XMLConfigDigester.digestConfig(resourceConfigStream, baseURI, classLoader);
        addSmooksResourceConfigurationList(configList);

        return configList;
    }

    private void processAppContextInitializers(SmooksResourceConfigurationList configList) {
        for (int i = 0; i < configList.size(); i++) {
            SmooksResourceConfiguration resourceConfig = configList.get(i);
            Class javaClass = resourceConfig.toJavaResource();

            if (javaClass != null && ApplicationContextInitializer.class.isAssignableFrom(javaClass)) {
                ApplicationContextInitializer initializer;

                try {
                    initializer = (ApplicationContextInitializer) javaClass.newInstance();
                } catch (Exception e) {
                    throw new SmooksConfigurationException("Failed to create an instance of the '" + javaClass.getName() + "' ApplicationContextInitializer class.", e);
                }
                
                lookup(new LifecycleManagerLookup()).applyPhase(initializer, new PostConstructLifecyclePhase(new Scope(this, resourceConfig, initializer)));
                registerObject(initializer);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void addProfileSets(List<ProfileSet> profileSets) {
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
    public void registerResource(SmooksResourceConfiguration resourceConfig) {
        if (resourceConfig == null) {
            throw new IllegalArgumentException("null 'resourceConfig' arg in method call.");
        }
        lookup(new SystemSmooksResourceConfigurationListLookup()).add(resourceConfig);
    }

    /**
     * Add a {@link SmooksResourceConfigurationList} to this registry.
     *
     * @param resourceList All the SmooksResourceConfigurationList instances added on this registry.
     */
    public void addSmooksResourceConfigurationList(SmooksResourceConfigurationList resourceList) {
        processAppContextInitializers(resourceList);
        lookup(new SmooksResourceConfigurationListsLookup()).add(resourceList);

        // XSD v1.0 added profiles to the resource config.  If there were any, add them to the
        // profile store.
        addProfileSets(resourceList.getProfiles());
    }

    /**
     * Load a Java Object defined by the supplied SmooksResourceConfiguration instance.
     *
     * @param resourceConfig SmooksResourceConfiguration instance.
     * @return An Object instance from the SmooksResourceConfiguration.
     */
    @SuppressWarnings("unchecked")
    public Object getObject(SmooksResourceConfiguration resourceConfig) {
        Object object = resourceConfig.getJavaResourceObject();

        if (object == null) {
            String className = ClasspathUtils.toClassName(resourceConfig.getResource());

            // Load the runtime class...
            Class classRuntime;
            try {
                classRuntime = ClassUtil.forName(className, getClass());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Error loading Java class: " + className, e);
            }

            // Try constructing via a SmooksResourceConfiguration constructor...
            Constructor constructor;
            try {
                constructor = classRuntime.getConstructor(SmooksResourceConfiguration.class);
                object = constructor.newInstance(resourceConfig);
            } catch (NoSuchMethodException e) {
                // OK, we'll try a default constructor later...
            } catch (Exception e) {
                throw new IllegalStateException("Error loading Java class: " + className, e);
            }

            // If we still don't have an object, try constructing via the default construtor...
            if (object == null) {
                try {
                    object = classRuntime.newInstance();
                } catch (Exception e) {
                    throw new IllegalStateException("Java class " + className + " must contain a default constructor if it does not contain a constructor that takes an instance of " + SmooksResourceConfiguration.class.getName() + "."
                            , e);
                }
            }

            if (object instanceof ContentHandler || object instanceof TypeConverterFactory) {
                lookup(new LifecycleManagerLookup()).applyPhase(object, new PostConstructLifecyclePhase(new Scope(this, resourceConfig, object)));
                this.registerObject(object);
            }

            resourceConfig.setJavaResourceObject(object);
        }

        return object;
    }

    /**
     * Get the {@link org.smooks.delivery.ContentHandlerFactory} for a resource based on the
     * supplied resource type.
     * <p/>
     * Note that {@link org.smooks.delivery.ContentHandlerFactory} implementations must be  configured under a selector value of "cdu-creator".
     *
     * @param type {@link org.smooks.delivery.ContentHandlerFactory} type e.g. "class", "xsl" etc.
     * @return {@link org.smooks.delivery.ContentHandlerFactory} for the resource.
     * @throws org.smooks.delivery.UnsupportedContentHandlerTypeException No {@link org.smooks.delivery.ContentHandlerFactory}
     *                                                                    registered for the specified resource type.
     */
    public ContentHandlerFactory getContentHandlerFactory(String type) throws UnsupportedContentHandlerTypeException {
        if (type == null) {
            throw new IllegalArgumentException("null 'resourceExtension' arg in method call.");
        }

        for (final SmooksResourceConfigurationList list : lookup(new SmooksResourceConfigurationListsLookup())) {
            for (int ii = 0; ii < list.size(); ii++) {
                SmooksResourceConfiguration config = list.get(ii);
                String selector = config.getSelector();

                if (CDU_CREATOR.equals(selector) && type.equalsIgnoreCase(config.getParameterValue(ContentHandlerFactory.PARAM_RESTYPE, String.class))) {
                    return (ContentHandlerFactory) getObject(config);
                }
            }
        }

        throw new UnsupportedContentHandlerTypeException(type);
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
