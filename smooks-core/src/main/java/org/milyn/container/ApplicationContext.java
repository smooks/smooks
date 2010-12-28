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

package org.milyn.container;

import org.milyn.cdr.SmooksResourceConfigurationStore;
import org.milyn.resource.ContainerResourceLocator;
import org.milyn.javabean.context.BeanContext;
import org.milyn.javabean.context.BeanIdStore;
import org.milyn.profile.ProfileStore;

/**
 * Smooks Application context interface definition.
 * @author tfennelly
 */
public interface ApplicationContext extends BoundAttributeStore {

    /**
	 * Get the container resource locator for the context.
	 * @return ContainerResourceLocator for the context.
	 */
	public abstract ContainerResourceLocator getResourceLocator();

    /**
     * Set the resource locator for this Smooks application context.
     * @param resourceLocator The Resource locator.
     */
    public void setResourceLocator(ContainerResourceLocator resourceLocator);

    /**
	 * Get the Store for from the container application context.
	 * @return SmooksResourceConfigurationStore instance.
	 */
	public abstract SmooksResourceConfigurationStore getStore();

    /**
	 * Get the ProfileStore in use within this Context.
	 * @return The ProfileStore.
	 */
    public ProfileStore getProfileStore();

    /**
     * Get the BeanIdStore in use within this Context
     * @return The BeanIdStore
     */
    public BeanIdStore getBeanIdStore();

    /**
     * Get the {@link ClassLoader} to be used by the associated Smooks instance
     *
     * @return The ClassLoader.
     */
    public ClassLoader getClassLoader();
}
