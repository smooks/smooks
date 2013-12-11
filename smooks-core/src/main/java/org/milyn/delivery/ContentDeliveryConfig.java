/*
� Milyn - Copyright (C) 2006 - 2010

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

package org.milyn.delivery;

import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.commons.cdr.SmooksConfigurationException;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.ordering.Consumer;
import org.milyn.delivery.ordering.Producer;
import org.milyn.dtd.DTDStore;
import org.milyn.event.types.ConfigBuilderEvent;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.util.List;
import java.util.Map;

/**
 * Content delivery configuration.
 * <p/>
 * Provides access to Content Delivery Resources
 * (e.g. {@link ContentHandler Content Delivery Units})
 * and other information for the targeted profile.
 *
 * @author tfennelly
 */
public interface ContentDeliveryConfig {

    /**
     * Should Smooks sort the Visitors, targeted at each selector, based on the
     * what the Visitors {@link Producer produce} and {@link Consumer consume}.
     * Default value "true".
     */
    public static final String SMOOKS_VISITORS_SORT = "smooks.visitors.sort";

    /**
     * Get the list of {@link SmooksResourceConfiguration}s for the specified selector definition.
     * <p/>
     * This list will be preselected and {@link org.milyn.cdr.SmooksResourceConfigurationSortComparator preordered}
     * for the target execution context.
     *
     * @param selector Configuration {@link org.milyn.cdr.SmooksResourceConfiguration#getSelector() selector}.  This
     *                 parameter is treated case incensitively.
     * @return List of {@link SmooksResourceConfiguration} instances, or null if no {@link SmooksResourceConfiguration}s are
     *         defined under that selector (for the device).
     * @see #getObjects(String)
     */
    public abstract List<SmooksResourceConfiguration> getSmooksResourceConfigurations(String selector);

    /**
     * Get the {@link SmooksResourceConfiguration} map for the target execution context.
     * <p/>
     * This Map will be {@link org.milyn.cdr.SmooksResourceConfigurationSortComparator preordered}
     * for the target execution context.
     *
     * @return {@link SmooksResourceConfiguration} map for the target execution context, keyed by the configuration
     *         {@link org.milyn.cdr.SmooksResourceConfiguration#getSelector() selector}, with each value being a
     *         {@link List} of preordered {@link SmooksResourceConfiguration} instances.
     */
    public abstract Map<String, List<SmooksResourceConfiguration>> getSmooksResourceConfigurations();

    /**
     * Get a list of {@link Object}s from the {@link SmooksResourceConfiguration}s specified by the selector.
     * <p/>
     * Gets the {@link SmooksResourceConfiguration}s specified for the selector and attempts to instanciate
     * a Java class instance from the resource specified by each of the {@link SmooksResourceConfiguration}s.
     * <p/>
     * Implementations should use {@link org.milyn.cdr.SmooksResourceConfigurationStore#getObject(org.milyn.cdr.SmooksResourceConfiguration)} to
     * construct each object.
     *
     * @param selector selector attribute value from the .cdrl file in the .cdrar.  This
     *                 parameter is treated case incensitively.
     * @return List of Object instances.  An empty list is returned where no
     *         selectors exist.
     * @see org.milyn.cdr.SmooksResourceConfigurationStore#getObject(org.milyn.cdr.SmooksResourceConfiguration)
     * @see #getSmooksResourceConfigurations(String)
     */
    public abstract List getObjects(String selector);

    /**
     * Get a new stream filter for the content delivery configuration.
     *
     * @param executionContext Execution context.
     * @return The stream filter.
     */
    public abstract Filter newFilter(ExecutionContext executionContext);

    /**
     * Get the DTD ({@link org.milyn.dtd.DTDStore.DTDObjectContainer}) for this delivery context.
     *
     * @return The DTD ({@link org.milyn.dtd.DTDStore.DTDObjectContainer}) for this delivery context.
     */
    public abstract DTDStore.DTDObjectContainer getDTD();

    /**
     * Get the list of Execution Events generated during the build of
     * the configuration.
     *
     * @return The list of events.
     */
    public abstract List<ConfigBuilderEvent> getConfigBuilderEvents();

    /**
     * Is default serialization on..
     *
     * @return True if default serialization is on, otherwise false.
     */
    public abstract boolean isDefaultSerializationOn();

    /**
     * Sort the Visitors, targeted at each selector, based on the
     * what the Visitors {@link Producer produce} and {@link Consumer consume}.
     *
     * @throws SmooksConfigurationException Sort failure.
     * @see #SMOOKS_VISITORS_SORT
     */
    public void sort() throws SmooksConfigurationException;

    /**
     * Add the execution lifecycle sets for the configuration.
     *
     * @throws SmooksConfigurationException Error resolving the handlers interested
     *                                      in the Execution lifecycle.
     */
    public void addToExecutionLifecycleSets() throws SmooksConfigurationException;

    /**
     * Initialize execution context lifecycle aware handlers.
     *
     * @param executionContext The execution context.
     */
    public void executeHandlerInit(ExecutionContext executionContext);

    /**
     * Cleanup execution context lifecycle aware handlers.
     *
     * @param executionContext The execution context.
     */
    public void executeHandlerCleanup(ExecutionContext executionContext);

    /**
     * Get an {@link XMLReader} instance from the
     * reader pool associated with this ContentDelivery config instance.
     *
     * @return An XMLReader instance if the pool is not empty, otherwise null.
     */
    public XMLReader getXMLReader() throws SAXException;

    /**
     * Return an {@link XMLReader} instance to the
     * reader pool associated with this ContentDelivery config instance.
     *
     * @param reader The XMLReader instance to be returned.  If the pool is full, the instance
     *               is left to the GC (i.e. lost).
     */
    public void returnXMLReader(XMLReader reader);

    /**
     * Get the {@link FilterBypass} for this delivery configuration.
     *
     * @return The {@link FilterBypass} for this delivery configuration, or
     *         null if non configured.
     */
    public FilterBypass getFilterBypass();
}