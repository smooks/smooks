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
package org.smooks.cdr;

import org.jaxen.saxpath.SAXPathException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.assertion.AssertArgument;
import org.smooks.cdr.annotation.Configurator;
import org.smooks.cdr.xpath.SelectorStep;
import org.smooks.classpath.ClasspathUtils;
import org.smooks.container.ApplicationContext;
import org.smooks.container.ApplicationContextInitializer;
import org.smooks.delivery.ContentHandler;
import org.smooks.delivery.ContentHandlerFactory;
import org.smooks.delivery.JavaContentHandlerFactory;
import org.smooks.delivery.UnsupportedContentHandlerTypeException;
import org.smooks.delivery.annotation.Resource;
import org.smooks.javabean.DataDecoder;
import org.smooks.profile.ProfileSet;
import org.smooks.profile.ProfileStore;
import org.smooks.resource.ContainerResourceLocator;
import org.smooks.util.ClassUtil;
import org.smooks.xml.NamespaceMappings;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * {@link org.smooks.cdr.SmooksResourceConfiguration} context store.
 * <p/>
 * Stores the {@link org.smooks.cdr.SmooksResourceConfiguration SmooksResourceConfigurations}
 * for a given container context in the form of
 * {@link org.smooks.cdr.SmooksResourceConfigurationList} entries.  Also maintains
 * a "default" config list for the context.
 * @author tfennelly
 */
public class SmooksResourceConfigurationStore {

    private static List<Class<ContentHandlerFactory>> handlerFactories = ClassUtil.getClasses("META-INF/content-handlers.inf", ContentHandlerFactory.class);

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SmooksResourceConfigurationStore.class);
	/**
	 * Table of loaded SmooksResourceConfigurationList objects.
	 */
	private List<SmooksResourceConfigurationList> configLists = new ArrayList<SmooksResourceConfigurationList>();
    /**
     * A complete list of all the that have been initialized and added to this store.
     * This has been transformed into a CopyOnWriteArrayList to fix http://jira.codehaus.org/browse/MILYN-381
     */
    private List<Object> initializedObjects = new CopyOnWriteArrayList<Object>() {
        public boolean add(Object object) {
            if(contains(object)) {
                // Don't add the same object again...
                return false;
            }
            return super.add(object);
        }
    };

    /**
     * Default configuration list.
     */
    private SmooksResourceConfigurationList defaultList = new SmooksResourceConfigurationList("default");
	/**
	 * Container context in which this store lives.
	 */
	private ApplicationContext applicationContext;
    private static final String CDU_CREATOR = "cdu-creator";

    /**
	 * Public constructor.
	 * @param applicationContext Container context in which this store lives.
	 */
	public SmooksResourceConfigurationStore(ApplicationContext applicationContext) {
        this(applicationContext, true);
    }

    public SmooksResourceConfigurationStore(ApplicationContext applicationContext, boolean registerInstalledResources) {
        AssertArgument.isNotNull(applicationContext, "applicationContext");
        this.applicationContext = applicationContext;

        // add the default list to the list.
        configLists.add(defaultList);
        defaultList.setSystemConfigList(true);

        registerInstalledHandlerFactories();
        if(registerInstalledResources) {
            registerInstalledResources("null-dom.cdrl");
            registerInstalledResources("null-sax.cdrl");
            registerInstalledResources("installed-param-decoders.cdrl");
            registerInstalledResources("installed-serializers.cdrl");
        }
    }

    private void registerInstalledHandlerFactories() {
        for (Class<ContentHandlerFactory> handlerFactory : handlerFactories) {
            Resource resourceAnnotation = handlerFactory.getAnnotation(Resource.class);

            if(resourceAnnotation != null) {
                addHandlerFactoryConfig(handlerFactory, resourceAnnotation.type());
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
        defaultList.add(res);
    }

    /**
     * Register the pre-installed CDU Creator classes.
     * @param resourceFile Installed (internal) resource config file.
     */
    private void registerInstalledResources(String resourceFile) {
        InputStream resource = ClassUtil.getResourceAsStream(resourceFile, getClass());

        if(resource == null) {
            throw new IllegalStateException("Failed to load " + resourceFile + ".  Expected to be in the same package as " + getClass().getName());
        }
        try {
            SmooksResourceConfigurationList resourceList = registerResources(resourceFile, resource);
            for(int i = 0; i < resourceList.size(); i++) {
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
	 * @param cdrlLoadList BufferedReader cdrl list - one cdrl def per line.
     * @throws java.io.IOException Error reading list buffer.
	 */
	public void load(BufferedReader cdrlLoadList) throws IOException {
		String uri;
		ContainerResourceLocator resLocator = applicationContext.getResourceLocator();

		while((uri = cdrlLoadList.readLine()) != null) {
			uri = uri.trim();
			if(uri.equals("") || uri.charAt(0) == '#') {
				continue;
			}

			try {
				InputStream resource = resLocator.getResource(uri);

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
     * @param baseURI The base URI to be associated with the configuration stream.
     * @param resourceConfigStream XML resource configuration stream.
     * @return The SmooksResourceConfigurationList created from the added resource configuration.
     * @throws SAXException Error parsing the resource stream.
     * @throws IOException Error reading resource stream.
     * @see SmooksResourceConfiguration
     */
    public SmooksResourceConfigurationList registerResources(String baseURI, InputStream resourceConfigStream) throws SAXException, IOException, URISyntaxException {
        SmooksResourceConfigurationList configList;

        if(baseURI == null || baseURI.trim().equals("")) {
            throw new IllegalArgumentException("null or empty 'name' arg in method call.");
        }
        if(resourceConfigStream == null) {
            throw new IllegalArgumentException("null 'resourceConfigStream' arg in method call.");
        }

        configList = XMLConfigDigester.digestConfig(resourceConfigStream, baseURI, applicationContext.getClassLoader());
        addSmooksResourceConfigurationList(configList);

        return configList;
    }

    private void processAppContextInitializers(SmooksResourceConfigurationList configList) {
        for(int i = 0; i < configList.size(); i++) {
            SmooksResourceConfiguration resourceConfig = configList.get(i);
            Class javaClass = resourceConfig.toJavaResource();

            if(javaClass != null && ApplicationContextInitializer.class.isAssignableFrom(javaClass)) {
                ApplicationContextInitializer initializer;

                try {
                    initializer = (ApplicationContextInitializer) javaClass.newInstance();
                } catch (Exception e) {
                    throw new SmooksConfigurationException("Failed to create an instance of the '" + javaClass.getName() + "' ApplicationContextInitializer class.", e);
                }

                Configurator.configure(initializer, resourceConfig, applicationContext);
                initializedObjects.add(initializer);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void addProfileSets(List<ProfileSet> profileSets) {
        if(profileSets == null) {
            return;
        }

        // TODO Sort out the other app context impls such that we can get the profile store from them too
        if(applicationContext instanceof ApplicationContext) {
            ProfileStore profileStore = applicationContext.getProfileStore();

            for(ProfileSet profileSet : profileSets) {
                profileStore.addProfileSet(profileSet);
            }
        }
    }


    /**
     * Register a {@link SmooksResourceConfiguration} on this context store.
     * <p/>
     * The config gets added to the default resource list.
     * @param resourceConfig The Content Delivery Resource definition to be registered.
     */
    public void registerResource(SmooksResourceConfiguration resourceConfig) {
        if(resourceConfig == null) {
            throw new IllegalArgumentException("null 'resourceConfig' arg in method call.");
        }
        defaultList.add(resourceConfig);
    }

    /**
     * Add a {@link SmooksResourceConfigurationList} to this store.
     *
     * @param resourceList All the SmooksResourceConfigurationList instances added on this store.
     */
    public void addSmooksResourceConfigurationList(SmooksResourceConfigurationList resourceList) {
        processAppContextInitializers(resourceList);
        configLists.add(resourceList);

        // XSD v1.0 added profiles to the resource config.  If there were any, add them to the
        // profile store.
        addProfileSets(resourceList.getProfiles());
    }

    /**
     * Get all the added SmooksResourceConfigurationList instances added on this store.
     *
     * @return All the SmooksResourceConfigurationList instances added on this store.
     */
    @SuppressWarnings("WeakerAccess")
    public Iterator<SmooksResourceConfigurationList> getSmooksResourceConfigurationLists() {
        return configLists.iterator();
    }

    /**
	 * Get all the SmooksResourceConfiguration entries registered on this context store
     * for the specified profile set.
	 * @param profileSet The profile set against which to lookup.
	 * @return All SmooksResourceConfiguration entries targeted at the specified useragent.
	 */
	@SuppressWarnings({ "unchecked", "SuspiciousToArrayCall" })
  public SmooksResourceConfiguration[] getSmooksResourceConfigurations(ProfileSet profileSet) {
		Vector allSmooksResourceConfigurationsColl = new Vector();
		SmooksResourceConfiguration[] allSmooksResourceConfigurations;

		// Iterate through each of the loaded SmooksResourceConfigurationLists.
    for (final SmooksResourceConfigurationList list : configLists)
    {
      SmooksResourceConfiguration[] resourceConfigs = list.getTargetConfigurations(profileSet);

      allSmooksResourceConfigurationsColl.addAll(Arrays.asList(resourceConfigs));
    }

		allSmooksResourceConfigurations = new SmooksResourceConfiguration[allSmooksResourceConfigurationsColl.size()];
		allSmooksResourceConfigurationsColl.toArray(allSmooksResourceConfigurations);

		return allSmooksResourceConfigurations;
	}

    /**
	 * Load a Java Object defined by the supplied SmooksResourceConfiguration instance.
	 * @param resourceConfig SmooksResourceConfiguration instance.
	 * @return An Object instance from the SmooksResourceConfiguration.
	 */
	@SuppressWarnings("unchecked")
  public Object getObject(SmooksResourceConfiguration resourceConfig) {
		Object object = resourceConfig.getJavaResourceObject();

        if(object == null) {
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
            if(object == null) {
                try {
                    object = classRuntime.newInstance();
                } catch (Exception e) {
                  throw new IllegalStateException("Java class " + className + " must contain a default constructor if it does not contain a constructor that takes an instance of " + SmooksResourceConfiguration.class.getName() + "."
                      , e);
                }
            }

            if(object instanceof ContentHandler || object instanceof DataDecoder) {
                Configurator.configure(object, resourceConfig, applicationContext);
                initializedObjects.add(object);
            }

            resourceConfig.setJavaResourceObject(object);
        }

		return object;
	}

    public List<Object> getInitializedObjects() {
        return initializedObjects;
    }

    public SmooksResourceConfiguration getGlobalParams() {
        SmooksResourceConfiguration config = new SmooksResourceConfiguration();

      for (final SmooksResourceConfigurationList list : configLists)
      {
        for (int ii = 0; ii < list.size(); ii++)
        {
          SmooksResourceConfiguration nextConfig = list.get(ii);
          if (ParameterAccessor.GLOBAL_PARAMETERS.equals(nextConfig.getSelector()))
          {
            config.addParameters(nextConfig);
          }
        }
      }

        return config;
    }

    /**
     * Get the {@link org.smooks.delivery.ContentHandlerFactory} for a resource based on the
     * supplied resource type.
     * <p/>
     * Note that {@link org.smooks.delivery.ContentHandlerFactory} implementations must be  configured under a selector value of "cdu-creator".
     * @param type {@link org.smooks.delivery.ContentHandlerFactory} type e.g. "class", "xsl" etc.
     * @return {@link org.smooks.delivery.ContentHandlerFactory} for the resource.
     * @throws org.smooks.delivery.UnsupportedContentHandlerTypeException No {@link org.smooks.delivery.ContentHandlerFactory}
     * registered for the specified resource type.
     */
    public ContentHandlerFactory getContentHandlerFactory(String type) throws UnsupportedContentHandlerTypeException {
        if(type == null) {
            throw new IllegalArgumentException("null 'resourceExtension' arg in method call.");
        }

      for (final SmooksResourceConfigurationList list : configLists)
      {
        for (int ii = 0; ii < list.size(); ii++)
        {
          SmooksResourceConfiguration config = list.get(ii);
          String selector = config.getSelector();

          if (CDU_CREATOR.equals(selector) && type.equalsIgnoreCase(config.getStringParameter(ContentHandlerFactory.PARAM_RESTYPE)))
          {
            return (ContentHandlerFactory) getObject(config);
          }
        }
      }

        throw new UnsupportedContentHandlerTypeException(type);
    }

    /**
     * Close this resource configuration store, {@link org.smooks.delivery.annotation.Uninitialize uninitializing}
     * all {@link org.smooks.delivery.ContentHandler ContentHandlers} allocated from this store instance.
     */
    public void close() {
        if(initializedObjects != null) {
            LOGGER.debug("Uninitializing all ContentHandler instances allocated through this store.");
            // We uninitialize in reverse order...
            for(int i = initializedObjects.size() - 1; i >= 0; i--) {
                Object object = initializedObjects.get(i);
                try {
                    LOGGER.debug("Uninitializing ContentHandler instance: " + object.getClass().getName());
                    Configurator.preDestroy(object);
                } catch (Throwable throwable) {
                    LOGGER.error("Error uninitializing " + object.getClass().getName() + ".", throwable);
                }
            }
            initializedObjects = null;
        }
    }

    public void setNamespaces() throws SAXPathException {
        Properties namespaces = NamespaceMappings.getMappings(applicationContext);

        for(SmooksResourceConfigurationList resourceConfigList : configLists) {
            for(int i = 0; i < resourceConfigList.size(); i++) {
                SelectorStep.setNamespaces(resourceConfigList.get(i).getSelectorSteps(), namespaces);
            }
        }
    }

    @SuppressWarnings("unused")
    public List<SmooksResourceConfiguration> lookupResource(ConfigSearch configSearch) {
        List<SmooksResourceConfiguration> resultSet = new ArrayList<SmooksResourceConfiguration>();

        for(SmooksResourceConfigurationList configList : configLists) {
            resultSet.addAll(configList.lookupResource(configSearch));
        }

        return resultSet;
    }


    public SmooksResourceConfigurationList getUserDefinedResourceList() {
        SmooksResourceConfigurationList userDefinedResources = new SmooksResourceConfigurationList("userDefinedResources");

        for(SmooksResourceConfigurationList configList : configLists) {
            if(!configList.isSystemConfigList()) {
                userDefinedResources.addAll(configList);
            }
        }

        return userDefinedResources;
    }
}
