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

package org.milyn.container.standalone;

import java.net.URI;
import java.util.Hashtable;
import java.util.Map;

import org.milyn.cdr.SmooksResourceConfigurationStore;
import org.milyn.container.ApplicationContext;
import org.milyn.javabean.context.BeanIdStore;
import org.milyn.javabean.context.StandaloneBeanContextFactory;
import org.milyn.profile.*;
import org.milyn.resource.ContainerResourceLocator;
import org.milyn.resource.URIResourceLocator;

/**
 * Standalone container execution context for Smooks.
 * <p/>
 * This context allows Smooks to be executed outside the likes of a
 * Servlet Container.
 * @author tfennelly
 */
public class StandaloneApplicationContext implements ApplicationContext {

    private Hashtable<Object, Object> attributes = new Hashtable<Object, Object>();
	private ContainerResourceLocator resourceLocator;
	private SmooksResourceConfigurationStore resStore;
	private DefaultProfileStore profileStore = new DefaultProfileStore();
	private BeanIdStore beanIdStore = new BeanIdStore();
    private ClassLoader classLoader;

    /**
     * Public constructor.
     */
    public StandaloneApplicationContext(boolean registerInstalledResources) {
        resourceLocator = new URIResourceLocator();
        ((URIResourceLocator)resourceLocator).setBaseURI(URI.create(URIResourceLocator.SCHEME_CLASSPATH + ":/"));
        resStore = new SmooksResourceConfigurationStore(this, registerInstalledResources);
        // Add the open profile...
        profileStore.addProfileSet(new DefaultProfileSet(Profile.DEFAULT_PROFILE));
    }

	/* (non-Javadoc)
	 * @see org.milyn.container.BoundAttributeStore#setAttribute(java.lang.Object, java.lang.Object)
	 */
	public void setAttribute(Object key, Object value) {
		attributes.put(key, value);
	}

	/* (non-Javadoc)
	 * @see org.milyn.container.BoundAttributeStore#getAttribute(java.lang.Object)
	 */
	public Object getAttribute(Object key) {
		return attributes.get(key);
	}

	/* (non-Javadoc)
	 * @see org.milyn.container.BoundAttributeStore#removeAttribute(java.lang.Object)
	 */
	public void removeAttribute(Object key) {
		attributes.remove(key);
	}

	public ContainerResourceLocator getResourceLocator() {
		return resourceLocator;
	}

    public void setResourceLocator(ContainerResourceLocator resourceLocator) {
        this.resourceLocator = resourceLocator;
    }

	public SmooksResourceConfigurationStore getStore() {
		return resStore;
	}

    /**
	 * Get the ProfileStore in use within the Standalone Context.
	 * @return The ProfileStore.
	 */
	public ProfileStore getProfileStore() {
		return profileStore;
	}

	public Map<Object, Object> getAttributes()
	{
		return attributes;
	}

	public BeanIdStore getBeanIdStore() {
		return beanIdStore;
	}

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        if(classLoader != null) {
            return classLoader;
        }
        return getClass().getClassLoader();
    }

}
