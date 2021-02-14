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
package org.smooks.delivery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.SmooksException;
import org.smooks.assertion.AssertArgument;
import org.smooks.cdr.*;
import org.smooks.dtd.DTDStore;
import org.smooks.dtd.DTDStore.DTDObjectContainer;
import org.smooks.event.types.ConfigBuilderEvent;
import org.smooks.profile.ProfileSet;
import org.smooks.registry.Registry;
import org.smooks.registry.lookup.ContentHandlerFactoryLookup;
import org.smooks.registry.lookup.InstanceLookup;
import org.smooks.registry.lookup.ResourceConfigsProfileSetLookup;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Content delivery configuration builder.
 * @author tfennelly
 */
public class DefaultContentDeliveryConfigBuilder implements ContentDeliveryConfigBuilder {

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultContentDeliveryConfigBuilder.class);

    /**
     * Profile set.
     */
    private final ProfileSet profileSet;

	private final Registry registry;
	
    /**
	 * XML selector content spec definition prefix
	 */
	private static final String ELCSPEC_PREFIX = "elcspec:";
    /**
     * An unsorted list of ResourceConfig.
     */
    private final List<ResourceConfig> resourceConfigs = new ArrayList<>();
    /**
	 * Table (by element) of sorted ResourceConfig instances keyed by selector value. Each table entry
	 * contains a List of ResourceConfig instances.
	 */
	private final Map<String, List<ResourceConfig>> resourceConfigTable = new LinkedHashMap<>();

    private final List<ContentHandlerBinding<Visitor>> visitorBindings = new ArrayList<>();

    /**
     * Config builder events list.
     */
    private final List<ConfigBuilderEvent> configBuilderEvents = new ArrayList<>();
    private final List<FilterProvider> filterProviders;

    /**
	 * DTD for the associated device.
	 */
	private DTDObjectContainer dtd;
	
	private ContentDeliveryConfig contentDeliveryConfig;
	
    /**
	 * Private (hidden) constructor.
     * @param profileSet Profile set.
	 * @param registry Container context.
	 */
	public DefaultContentDeliveryConfigBuilder(final ProfileSet profileSet, final Registry registry, final List<FilterProvider> filterProviders) {
        AssertArgument.isNotNull(profileSet, "profileSet");
        AssertArgument.isNotNull(registry, "registry");
        AssertArgument.isNotNull(filterProviders, "filterProviders");

        this.profileSet = profileSet;
        this.registry = registry;
		this.filterProviders = filterProviders;
    }

	/**
	 * Get the ContentDeliveryConfig instance for the specified profile set.
     * @param extendedContentHandlerBindings Preconfigured/extended Visitor Configuration Map.
     * @return The ContentDeliveryConfig instance for the named table.
	 */
    @Override
    public ContentDeliveryConfig build(List<ContentHandlerBinding<Visitor>> extendedContentHandlerBindings) {
        if (contentDeliveryConfig == null) {
            synchronized (DefaultContentDeliveryConfigBuilder.class) {
                if (contentDeliveryConfig == null) {
                    load(profileSet);
                    fireEvent(ContentDeliveryConfigBuilderLifecycleEvent.CONTENT_DELIVERY_BUILDER_CREATED);
                    contentDeliveryConfig = buildConfig(extendedContentHandlerBindings);
                }
            }
        }

        return contentDeliveryConfig;
    }
    
    private ContentDeliveryConfig buildConfig(List<ContentHandlerBinding<Visitor>> extendedContentHandlerBindings) {
        if (extendedContentHandlerBindings != null) {
            visitorBindings.addAll(extendedContentHandlerBindings);
        }
        FilterProvider filterProvider = getFilterProvider();
        
        LOGGER.debug(String.format("Activating %s filter", filterProvider.getName()));
        configBuilderEvents.add(new ConfigBuilderEvent("SAX/DOM support characteristics of the Resource Configuration map:\n" + getResourceFilterCharacteristics()));
        configBuilderEvents.add(new ConfigBuilderEvent(String.format("Activating %s filter", filterProvider.getName())));

        ContentDeliveryConfig contentDeliveryConfig = filterProvider.createContentDeliveryConfig(visitorBindings, registry, resourceConfigTable, configBuilderEvents, dtd);
        fireEvent(ContentDeliveryConfigBuilderLifecycleEvent.CONTENT_DELIVERY_CONFIG_CREATED);

        return contentDeliveryConfig;
    }

    private FilterProvider getFilterProvider() {
        final List<FilterProvider> candidateFilterProviders = filterProviders.stream().filter(s -> s.isProvider(visitorBindings)).collect(Collectors.toList());
        final String filterTypeParam = ParameterAccessor.getParameterValue(Filter.STREAM_FILTER_TYPE, String.class, resourceConfigTable);
        if (filterTypeParam == null && candidateFilterProviders.isEmpty()) {
            throw new SmooksException("Ambiguous Resource Configuration set.  All Element Content Handlers must support processing on the SAX and/or DOM Filter:\n" + getResourceFilterCharacteristics());
        } else if (filterTypeParam == null) {
            return candidateFilterProviders.get(0);
        } else {
            final Optional<FilterProvider> filterProviderOptional = candidateFilterProviders.stream().filter(c -> c.getName().equalsIgnoreCase(filterTypeParam)).findFirst();
            if (filterProviderOptional.isPresent()) {
                return filterProviderOptional.get();
            } else {
                throw new SmooksException("The configured Filter ('" + filterTypeParam + "') cannot be used: " + Arrays.toString(candidateFilterProviders.stream().map(FilterProvider::getName).collect(Collectors.toList()).toArray()) + " filters can be used for the given set of visitors. Turn on debug logging for more information.");
            }
        }
    }

    /**
     * Logging support function.
     * @return Verbose characteristics string.
     */
    private String getResourceFilterCharacteristics() {
        StringBuffer stringBuf = new StringBuffer();
        List<ContentHandler> printedHandlers = new ArrayList<>();

        stringBuf.append("\t\tDOM   SAX    Resource  ('x' equals supported)\n");
        stringBuf.append("\t\t---------------------------------------------------------------------\n");

        for (ContentHandlerBinding<Visitor> contentHandlerBinding : visitorBindings) {
            printHandlerCharacteristics(contentHandlerBinding, stringBuf, printedHandlers);
        }

        stringBuf.append("\n\n");

        return stringBuf.toString();
    }

    private void printHandlerCharacteristics(ContentHandlerBinding<Visitor> contentHandlerBinding, StringBuffer stringBuf, List<ContentHandler> printedHandlers) {
        ContentHandler handler = contentHandlerBinding.getContentHandler();

        if (printedHandlers.contains(handler)) {
            return;
        } else {
            printedHandlers.add(handler);
        }

        stringBuf.append("\t\t ");
        Map<String, Boolean> supportedFilterProviders = filterProviders.stream().map(s -> new AbstractMap.SimpleEntry<>(s.getName(), s.isProvider(Collections.singletonList(contentHandlerBinding)))).
                collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
        for (Entry<String, Boolean> supportedFilterProvider : supportedFilterProviders.entrySet()) {
            stringBuf.append(supportedFilterProvider.getValue() ? supportedFilterProvider.getKey() : " ").append("     ");
        }

        stringBuf.append(contentHandlerBinding.getResourceConfig())
                .append("\n");
    }
    
    /**
	 * Build the ContentDeliveryConfigBuilder for the specified device.
	 * <p/>
	 * Creates the buildTable instance and populates it with the ProcessingUnit matrix
	 * for the specified device.
	 */
	private void load(ProfileSet profileSet) {
        resourceConfigs.clear();
        resourceConfigs.addAll(Arrays.asList(registry.lookup(new ResourceConfigsProfileSetLookup(registry, profileSet))));

		// Build and sort the resourceConfigTable table - non-transforming elements.
		buildResourceConfigTable(resourceConfigs);
		sortResourceConfigs(resourceConfigTable, profileSet);

		// If there's a DTD for this device, get it and add it to the DTDStore.
		List dtdResourceConfigs = resourceConfigTable.get("dtd");
		if(dtdResourceConfigs != null && dtdResourceConfigs.size() > 0) {
            ResourceConfig dtdResourceConfig = (ResourceConfig)dtdResourceConfigs.get(0);
            byte[] dtdDataBytes = dtdResourceConfig.getBytes();

            if(dtdDataBytes != null) {
                DTDStore.addDTD(profileSet, new ByteArrayInputStream(dtdDataBytes));
                // Initialise the DTD reference for this config table.
                dtd = DTDStore.getDTDObject(profileSet);
            } else {
                LOGGER.error("DTD resource [" + dtdResourceConfig.getResource() + "] not found in classpath.");
            }
		}

		// Expand the ResourceConfig table and resort
		expandResourceConfigTable();
		sortResourceConfigs(resourceConfigTable, profileSet);

        // Extract the ContentDeliveryUnits and build the tables
        extractContentHandlers();

        // Tell all interested listeners that all the handlers have now been created.
        fireEvent(ContentDeliveryConfigBuilderLifecycleEvent.HANDLERS_CREATED);

        if(LOGGER.isDebugEnabled()) {
            logResourceConfig(profileSet);
        }
	}

    /**
	 * Print a debug log of the resource configurations for the associated profile.
	 */
	private void logResourceConfig(ProfileSet profileSet) {
		LOGGER.debug("==================================================================================================");
		LOGGER.debug("Resource configuration (sorted) for profile [" + profileSet.getBaseProfile() + "].  Sub Profiles: [" + profileSet + "]");
		Iterator configurations = resourceConfigTable.entrySet().iterator();
		int i = 0;

		while(configurations.hasNext()) {
			Map.Entry entry = (Entry) configurations.next();
			List resources = (List)entry.getValue();

			LOGGER.debug(i + ") " + entry.getKey());
			for (int ii = 0; ii < resources.size(); ii++) {
				LOGGER.debug("\t(" + ii + ") " + resources.get(ii));
			}
		}
		LOGGER.debug("==================================================================================================");
	}

	/**
	 * Build the basic ResourceConfig table from the list.
	 * @param resourceConfigsList List of ResourceConfigs.
	 */
    private void buildResourceConfigTable(List<ResourceConfig> resourceConfigsList) {
        for (final ResourceConfig resourceConfig : resourceConfigsList) {
            addResourceConfig(resourceConfig);
        }
    }

    /**
     * Add the supplied resource configuration to this configuration's main
     * resource configuration list.
     * @param config The configuration to be added.
     */
    private void addResourceConfig(ResourceConfig config) {
        String target = config.getSelectorPath().getSelector();

        // If it's contextual, it's targeting an XML element...
        if(config.getSelectorPath().size() > 1) {
            target = config.getSelectorPath().getTargetElement();
        }

        addResourceConfig(target, config);
    }

    /**
     * Add the config for the specified element.
     * @param element The element to which the config is to be added.
     * @param resourceConfig The Object to be added.
     */
    private void addResourceConfig(String element, ResourceConfig resourceConfig) {
        // Add it to the unsorted list...
        if(!resourceConfigs.contains(resourceConfig)) {
            resourceConfigs.add(resourceConfig);
        }

        // Add it to the sorted resourceConfigTable...
        final List<ResourceConfig> elementConfigList = resourceConfigTable.computeIfAbsent(element, k -> new Vector<>());
        if(!elementConfigList.contains(resourceConfig)) {
            elementConfigList.add(resourceConfig);
        }
    }

    /**
	 * Expand the ResourceConfig table.
	 * <p/>
	 * Expand the XmlDef entries to the target elements etc.
	 */
    private void expandResourceConfigTable() {
        class ExpansionResourceConfigStrategy implements ResourceConfigStrategy {
            private ExpansionResourceConfigStrategy() {
            }

            public void applyStrategy(String elementName, ResourceConfig resourceConfig) {
                // Expand XmlDef entries.
                if (resourceConfig.isXmlDef()) {
                    String[] elements = getDTDElements(resourceConfig.getSelectorPath().getSelector().substring(ResourceConfig.XML_DEF_PREFIX.length()));
                    for (final String element : elements) {
                        addResourceConfig(element, resourceConfig);
                    }
                }

                // Add code to expand other expandable entry types here.
            }
        }
        ResourceConfigTableIterator tableIterator = new ResourceConfigTableIterator(new ExpansionResourceConfigStrategy());
        tableIterator.iterate();
    }

    /**
     * Iterate over the table smooks-resource instances and sort the ResourceConfigs
     * on each element.  Ordered by specificity.
     */
    @SuppressWarnings("unchecked")
    private void sortResourceConfigs(Map<String, List<ResourceConfig>> table, ProfileSet profileSet) {
        Parameter<String> sortParam = ParameterAccessor.getParameter("sort.resources", String.class, table);
        if (sortParam != null && sortParam.getValue().trim().equalsIgnoreCase("true")) {
            if (!table.isEmpty()) {

                for (final Object o : table.entrySet()) {
                    Entry entry = (Entry) o;
                    List markupElResourceConfigs = (List) entry.getValue();
                    ResourceConfig[] resourceConfigs = (ResourceConfig[]) markupElResourceConfigs
                            .toArray(new ResourceConfig[0]);
                    ResourceConfigSortComparator sortComparator = new ResourceConfigSortComparator(profileSet);

                    Arrays.sort(resourceConfigs, sortComparator);
                    entry.setValue(new Vector(Arrays.asList(resourceConfigs)));
                }
            }
        }
    }

    /**
	 * Extract the ContentHandler instances from the ResourceConfig table and add them to
	 * their respective tables.
	 */
	private void extractContentHandlers() {
		ContentHandlerExtractionStrategy contentHandlerExtractionStrategy = new ContentHandlerExtractionStrategy(registry);
		ResourceConfigTableIterator resourceConfigTableIterator = new ResourceConfigTableIterator(contentHandlerExtractionStrategy);

        resourceConfigTableIterator.iterate();
    }

    /**
	 * Get the DTD elements for specific device context.
	 * @param string DTD spec string e.g. "elcspec:empty"
	 * @return List of element names.
	 */
	private String[] getDTDElements(String string) {
		String tmpString = string;

		if(tmpString.startsWith(ELCSPEC_PREFIX)) {
			tmpString = tmpString.substring(ELCSPEC_PREFIX.length());
			if(tmpString.equals("empty")) {
				return dtd.getEmptyElements();
			} else if(tmpString.equals("not-empty")) {
				return dtd.getNonEmptyElements();
			} else if(tmpString.equals("any")) {
				return dtd.getAnyElements();
			} else if(tmpString.equals("not-any")) {
				return dtd.getNonAnyElements();
			} else if(tmpString.equals("mixed")) {
				return dtd.getMixedElements();
			} else if(tmpString.equals("not-mixed")) {
				return dtd.getNonMixedElements();
			} else if(tmpString.equals("pcdata")) {
				return dtd.getPCDataElements();
			} else if(tmpString.equals("not-pcdata")) {
				return dtd.getNonPCDataElements();
			}
		}

		throw new IllegalStateException("Unsupported DTD spec definition [" + string + "]");
	}

    private void logExecutionEvent(ResourceConfig resourceConfig, String message) {
        configBuilderEvents.add(new ConfigBuilderEvent(resourceConfig, message));
    }

    private void fireEvent(ContentDeliveryConfigBuilderLifecycleEvent event) {
        for(Object object : registry.lookup(new InstanceLookup<>(ContentDeliveryConfigBuilderLifecycleListener.class)).values()) {
            ((ContentDeliveryConfigBuilderLifecycleListener) object).handle(event);
        }
    }

    /**
	 * ContentHandler extraction strategy.
	 * @author tfennelly
	 */
	private final class ContentHandlerExtractionStrategy implements ResourceConfigStrategy {

        private final Registry registry;

        private ContentHandlerExtractionStrategy(Registry registry) {
            this.registry = registry;
        }

        public void applyStrategy(String elementName, ResourceConfig resourceConfig) {
            applyContentDeliveryUnitStrategy(resourceConfig);
        }

        private boolean applyContentDeliveryUnitStrategy(final ResourceConfig resourceConfig) {
            // Try it as a Java class before trying anything else.  This is to
            // accommodate specification of the class in the standard
            // Java form e.g. java.lang.String Vs java/lang/String.class
            if (resourceConfig.isJavaResource()) {
                final ContentHandlerFactory<?> contentHandlerFactory = registry.lookup(new ContentHandlerFactoryLookup("class"));
                if (contentHandlerFactory == null) {
                    throw new SmooksException("No ContentHandlerFactory configured (IoC) for type 'class' (Java).");
                }
                // Job done - it's a CDU and we've added it!
                return addContentDeliveryUnit(resourceConfig, contentHandlerFactory);

            } else {
                // Get the resource type and "try" creating a ContentHandlerFactory for that resource
                // type.
                final String resourceType = resourceConfig.getResourceType();
                final ContentHandlerFactory<?> contentHandlerFactory = tryCreateCreator(resourceType);

                // If we have a creator but it's the JavaContentHandlerFactory we ignore it because
                // we know the class in question does not implement ContentHandler.  We know because
                // we tried this above.
                if (contentHandlerFactory != null) {
                    if (!(contentHandlerFactory instanceof JavaContentHandlerFactory)) {
                        return addContentDeliveryUnit(resourceConfig, contentHandlerFactory);
                    }
                } else {
                    // Just ignore it - something else will use it (hopefully)
                    if (resourceType != null) {
                        logExecutionEvent(resourceConfig, "Unable to create ContentHandler class instance for resource.  " +
                                "This is probably because there's no " + ContentHandlerFactory.class.getSimpleName() + " implementation for resource " +
                                "type '" + resourceType + "' available on the classpath.");
                    }
                }
            }

            return false;
        }

        /**
         * Try create the CDU creator for the specified resource type.
         * <p/>
         * Return null if unsuccessful i.e. no exceptions.
         * @param restype The resource type.
         * @return The appropriate CDU creator instance, or null if there is none.
         */
        private ContentHandlerFactory<?> tryCreateCreator(String restype) {
            if (restype == null || restype.trim().equals("")) {
                LOGGER.debug("Request to attempt ContentHandlerFactory creation based on a null/empty resource type.");
                return null;
            }

            return registry.lookup(new ContentHandlerFactoryLookup(restype));

        }

        /**
		 * Add a {@link ContentHandler} for the specified element and configuration.
		 * @param resourceConfig Configuration.
		 * @param contentHandlerFactory CDU Creator class.
		 * @return True if the CDU was added, otherwise false.
		 */
        private boolean addContentDeliveryUnit(ResourceConfig resourceConfig, ContentHandlerFactory<?> contentHandlerFactory) {
            Object contentHandler;

            // Create the ContentHandler.
            try {
                contentHandler = contentHandlerFactory.create(resourceConfig);
            } catch (SmooksConfigurationException e) {
                throw e;
            } catch (Throwable thrown) {
                String message = "ContentHandlerFactory [" + contentHandlerFactory.getClass().getName() + "] unable to create resource processing instance for resource [" + resourceConfig + "]. ";
                LOGGER.error(message + thrown.getMessage(), thrown);
                configBuilderEvents.add(new ConfigBuilderEvent(resourceConfig, message, thrown));

                return false;
            }
            
            //TODO: register object
            
            if (contentHandler instanceof Visitor) {
                // Add the visitor.  No need to configure it as that should have been done by
                // creator...
                visitorBindings.add(new ContentHandlerBinding<>((Visitor) contentHandler, resourceConfig));
            }

            // Content delivery units are allowed to dynamically add new configurations...
            if (contentHandler instanceof ConfigurationExpander) {
                List<ResourceConfig> additionalConfigs = ((ConfigurationExpander) contentHandler).expandConfigurations();
                if (additionalConfigs != null && !additionalConfigs.isEmpty()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Adding expansion resource configurations created by: " + resourceConfig);
                        for (ResourceConfig additionalConfig : additionalConfigs) {
                            LOGGER.debug("\tAdding expansion resource configuration: " + additionalConfig);
                        }
                    }
                    processExpansionConfigurations(additionalConfigs);
                }
            }

            if (contentHandler instanceof VisitorAppender) {
                visitorBindings.addAll(((VisitorAppender) contentHandler).addVisitors());
            }

            return true;
        }

        /**
         * Process the supplied expansion configurations.
         * @param additionalConfigs Expansion configs.
         */
        private void processExpansionConfigurations(List<ResourceConfig> additionalConfigs) {
            for (final ResourceConfig resourceConfig : additionalConfigs) {
                registry.registerResourceConfig(resourceConfig);
                // Try adding it as a ContentHandler instance...
                if (!applyContentDeliveryUnitStrategy(resourceConfig)) {
                    // Else just add it to the main list...
                    addResourceConfig(resourceConfig);
                }
            }
        }

    }

    /**
	 * Iterate over the ResourceConfig table applying the constructor
	 * supplied ResourceConfigStrategy.
	 * @author tfennelly
	 */
	private class ResourceConfigTableIterator {

		/**
		 * Iteration strategy.
		 */
		private final ResourceConfigStrategy strategy;

		/**
		 * Private constructor.
		 * @param strategy Strategy algorithm implementation.
		 */
		private ResourceConfigTableIterator(ResourceConfigStrategy strategy) {
			this.strategy = strategy;
		}

		/**
		 * Iterate over the table applying the strategy.
		 */
        private void iterate() {
            for (ResourceConfig resourceConfig : resourceConfigs) {
                strategy.applyStrategy(resourceConfig.getSelectorPath().getTargetElement(), resourceConfig);
            }
        }
	}

	/**
	 * Unitdef iteration strategy interface.
	 * @author tfennelly
	 */
	private interface ResourceConfigStrategy {
		/**
		 * Apply the strategy algorithm.
		 * @param elementName The element name
		 * @param unitDef The ResourceConfig
		 */
		void applyStrategy(String elementName, ResourceConfig unitDef);
	}
}
