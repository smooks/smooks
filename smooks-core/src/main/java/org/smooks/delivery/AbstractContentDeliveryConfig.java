/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package org.smooks.delivery;

import org.smooks.cdr.ParameterAccessor;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.container.ApplicationContext;
import org.smooks.container.ExecutionContext;
import org.smooks.dtd.DTDStore;
import org.smooks.event.types.ConfigBuilderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.XMLReader;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Abstract {@link ContentDeliveryConfig}.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@SuppressWarnings("unchecked")
public abstract class AbstractContentDeliveryConfig implements ContentDeliveryConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContentDeliveryConfig.class);

	/**
     * Container context.
     */
    private ApplicationContext applicationContext;
    /**
     * Table of SmooksResourceConfiguration instances keyed by selector value. Each table entry
     * contains a List of SmooksResourceConfiguration instances.
     */
    private Map<String, List<SmooksResourceConfiguration>> resourceConfigTable = new LinkedHashMap<String, List<SmooksResourceConfiguration>>();
    /**
     * Table of Object instance lists keyed by selector. Each table entry
     * contains a List of Objects.
     */
    private Map objectsTable = new LinkedHashMap();
    /**
     * DTD for the associated device.
     */
    private DTDStore.DTDObjectContainer dtd;
    /**
     * Config builder events list.
     */
    private List<ConfigBuilderEvent> configBuilderEvents = new ArrayList<ConfigBuilderEvent>();

    private Set<ExecutionLifecycleInitializable> execInitializableHandlers = new LinkedHashSet<ExecutionLifecycleInitializable>();
    private Set<ExecutionLifecycleCleanable> execCleanableHandlers = new LinkedHashSet<ExecutionLifecycleCleanable>();

    private Boolean isDefaultSerializationOn = null;

    private final List<XMLReader> readerPool = new CopyOnWriteArrayList<XMLReader>();
	private         int             readerPoolSize;

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Get the list of {@link org.smooks.cdr.SmooksResourceConfiguration}s for the specified selector definition.
     * @param selector The configuration "selector" attribute value from the .cdrl file in the .cdrar.
     * @return List of SmooksResourceConfiguration instances, or null.
     */
    public List getSmooksResourceConfigurations(String selector) {
        return resourceConfigTable.get(selector);
    }

    public void setSmooksResourceConfigurations(Map<String, List<SmooksResourceConfiguration>> resourceConfigTable) {
        this.resourceConfigTable = resourceConfigTable;
    }

    /**
     * Get the {@link org.smooks.cdr.SmooksResourceConfiguration} map for the target execution context.
     * <p/>
     * This Map will be {@link org.smooks.cdr.SmooksResourceConfigurationSortComparator preordered}
     * for the target execution context.
     *
     * @return {@link org.smooks.cdr.SmooksResourceConfiguration} map for the target execution context, keyed by the configuration
     * {@link org.smooks.cdr.SmooksResourceConfiguration#getSelector() selector}, with each value being a
     * {@link List} of preordered {@link org.smooks.cdr.SmooksResourceConfiguration} instances.
     */
    public Map<String, List<SmooksResourceConfiguration>> getSmooksResourceConfigurations() {
        return resourceConfigTable;
    }

    private static final Vector EMPTY_LIST = new Vector();

    /**
     * Get a list {@link Object}s from the supplied {@link org.smooks.cdr.SmooksResourceConfiguration} selector value.
     * <p/>
     * Uses {@link org.smooks.cdr.SmooksResourceConfigurationStore#getObject(org.smooks.cdr.SmooksResourceConfiguration)} to construct the object.
     * @param selector selector attribute value from the .cdrl file in the .cdrar.
     * @return List of Object instances.  An empty list is returned where no
     * selectors exist.
     */
    public List getObjects(String selector) {
        Vector objects;

        objects = (Vector)objectsTable.get(selector);
        if(objects == null) {
            List unitDefs = resourceConfigTable.get(selector);

            if(unitDefs != null && unitDefs.size() > 0) {
                objects = new Vector(unitDefs.size());

                if(applicationContext == null) {
                    throw new IllegalStateException("Call to getObjects() before the setApplicationContext() was called.");
                }

              for (final Object unitDef : unitDefs)
              {
                SmooksResourceConfiguration resConfig = (SmooksResourceConfiguration) unitDef;
                objects.add(applicationContext.getStore().getObject(resConfig));
              }
            } else {
                objects = EMPTY_LIST;
            }

            objectsTable.put(selector, objects);
        }

        return objects;
    }

    /* (non-Javadoc)
     * @see org.smooks.delivery.ContentDeliveryConfig#getDTD()
     */
    public DTDStore.DTDObjectContainer getDTD() {
        return dtd;
    }

    public void setDtd(DTDStore.DTDObjectContainer dtd) {
        this.dtd = dtd;
    }

    public List<ConfigBuilderEvent> getConfigBuilderEvents() {
        return configBuilderEvents;
    }

    public boolean isDefaultSerializationOn() {
        if(isDefaultSerializationOn == null) {
            isDefaultSerializationOn = ParameterAccessor.getBoolParameter(Filter.DEFAULT_SERIALIZATION_ON, true, this);
        }

        return isDefaultSerializationOn;
    }

    @SuppressWarnings("WeakerAccess")
    public <T extends Visitor> void addToExecutionLifecycleSets(ContentHandlerConfigMapTable<T> handlerSet) {
        Collection<List<ContentHandlerConfigMap<T>>> mapEntries = handlerSet.getTable().values();

        for(List<ContentHandlerConfigMap<T>> mapList : mapEntries) {
            for(ContentHandlerConfigMap<T> map : mapList) {
                if(map.isLifecycleInitializable()) {
                    execInitializableHandlers.add((ExecutionLifecycleInitializable) map.getContentHandler());
                }
                if(map.isLifecycleCleanable()) {
                    execCleanableHandlers.add((ExecutionLifecycleCleanable) map.getContentHandler());
                }
            }
        }
    }

    public void executeHandlerInit(ExecutionContext executionContext) {
        for(ExecutionLifecycleInitializable handler : execInitializableHandlers) {
            handler.executeExecutionLifecycleInitialize(executionContext);
        }
    }

    public void executeHandlerCleanup(ExecutionContext executionContext) {
        for(ExecutionLifecycleCleanable handler : execCleanableHandlers) {
            try {
                handler.executeExecutionLifecycleCleanup(executionContext);
            } catch(Throwable t) {
                LOGGER.error("Error during Visit handler cleanup.", t);
            }
        }
    }

    public void initializeXMLReaderPool() {
    	try {
	        readerPoolSize = Integer.parseInt(ParameterAccessor.getStringParameter(Filter.READER_POOL_SIZE, "0", this).trim());
    	} catch(NumberFormatException e) {
    		readerPoolSize = 0;
    	}
    }

	public XMLReader getXMLReader()
  {
		synchronized(readerPool) {
			if(!readerPool.isEmpty()) {
				return readerPool.remove(0);
			} else {
				return null;
			}
		}
	}

	public void returnXMLReader(XMLReader reader) {
		synchronized(readerPool) {
			if(readerPool.size() < readerPoolSize) {
				readerPool.add(reader);
			}
		}
	}

    protected FilterBypass getFilterBypass(ContentHandlerConfigMapTable... visitorTables) {
    	for(ContentHandlerConfigMapTable visitorTable : visitorTables) {
            if(visitorTable != null && visitorTable.getUserConfiguredCount() > 1) {
            	// If any of the visitor tables have more than 1 visitor instance...
            	return null;
            }
    	}

    	// Gather the possible set of FilterBypass instances...
    	Set<FilterBypass> bypassSet = new HashSet<FilterBypass>();
    	for(ContentHandlerConfigMapTable visitorTable : visitorTables) {
            if(visitorTable != null && visitorTable.getUserConfiguredCount() == 1) {
            	FilterBypass filterBypass = getFilterBypass(visitorTable);

            	if(filterBypass != null) {
            		bypassSet.add(filterBypass);
            	} else {
            		// There's a non-FilterBypass Visitor configured, so we can't
            		// use the Bypass Filter
            		return null;
            	}
            }
    	}

    	// If there's just one FilterBypass instance, return it...
    	if(bypassSet.size() == 1) {
    		return bypassSet.iterator().next();
    	}

    	// Otherwise we're not going to allow filter bypassing...
    	return null;
    }

    private <T extends Visitor> FilterBypass getFilterBypass(ContentHandlerConfigMapTable<T> visitorTable) {
        Set<Entry<String, List<ContentHandlerConfigMap<T>>>> entries = visitorTable.getTable().entrySet();

        for(Entry<String, List<ContentHandlerConfigMap<T>>> entry : entries) {
        	ContentHandlerConfigMap<T> configMap = entry.getValue().get(0);
        	SmooksResourceConfiguration resourceConfig = configMap.getResourceConfig();

        	if(!resourceConfig.isDefaultResource()) {
	        	if(resourceConfig.getTargetElement().equals(SmooksResourceConfiguration.DOCUMENT_FRAGMENT_SELECTOR)) {
	        		T visitor = configMap.getContentHandler();
	        		if(visitor instanceof FilterBypass) {
	        			return (FilterBypass) visitor;
	        		}
	        	}

	        	// Make sure we only iterate once...
	        	return null;
        	}
        }

    	return null;
    }
}
