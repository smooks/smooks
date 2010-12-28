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

package org.milyn.servlet.container;

import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.milyn.cdr.SmooksResourceConfigurationStore;
import org.milyn.container.ApplicationContext;
import org.milyn.resource.ContainerResourceLocator;
import org.milyn.resource.ServletResourceLocator;
import org.milyn.resource.URIResourceLocator;
import org.milyn.javabean.context.BeanIdStore;
import org.milyn.profile.ProfileStore;
import org.milyn.profile.DefaultProfileStore;

/**
 * ApplicationContext adapter for the javax.servlet.ServletContext interface.
 * @author tfennelly
 */
public class ServletApplicationContext implements ApplicationContext {

	/**
     * Context stream resource locator for the Servlet environment.
     */
    private ServletResourceLocator resourceLocator;
    /**
     * ServletContext instance for requested application resource.
     */
    private ServletContext servletContext;
	/**
	 * Store instance for the context.
	 */
	private SmooksResourceConfigurationStore resStore;

    private DefaultProfileStore profileStore = new DefaultProfileStore();

    private BeanIdStore beanIdStore = new BeanIdStore();

	/**
	 * Public constructor.
	 * @param servletContext ServletContext instance.
	 * @param servletConfig ServletConfig instance.
	 */
	public ServletApplicationContext(ServletContext servletContext, ServletConfig servletConfig) {
		if(servletContext == null) {
			throw new IllegalArgumentException("null 'servletContext' arg in constructor call.");
		}
		if(servletConfig == null) {
			throw new IllegalArgumentException("null 'servletConfig' arg in constructor call.");
		}
		this.servletContext = servletContext;
		resourceLocator = new ServletResourceLocator(servletConfig, new URIResourceLocator());
		resStore = new SmooksResourceConfigurationStore(this);
	}

	/* (non-Javadoc)
	 * @see org.milyn.container.ApplicationContext#getResourceLocator()
	 */
	public ContainerResourceLocator getResourceLocator() {
		return resourceLocator;
	}

	/* (non-Javadoc)
	 * @see org.milyn.container.BoundAttributeStore#setAttribute(java.lang.Object, java.lang.Object)
	 */
	public void setAttribute(Object key, Object value) {
		servletContext.setAttribute(key.toString(), value);
	}

	/* (non-Javadoc)
	 * @see org.milyn.container.BoundAttributeStore#getAttribute(java.lang.Object)
	 */
	public Object getAttribute(Object key) {
		return servletContext.getAttribute(key.toString());
	}

	/* (non-Javadoc)
	 * @see org.milyn.container.BoundAttributeStore#removeAttribute(java.lang.Object)
	 */
	public void removeAttribute(Object key) {
		servletContext.removeAttribute(key.toString());
	}

	/* (non-Javadoc)
	 * @see org.milyn.container.ApplicationContext#getCdrarStore()
	 */
	public SmooksResourceConfigurationStore getStore() {
		return resStore;
	}

    public ProfileStore getProfileStore() {
        return profileStore;
    }

    public void setResourceLocator(ContainerResourceLocator resourceLocator) {
        throw new UnsupportedOperationException("Cannot set the resource locator on the " + ServletApplicationContext.class.getName() + " class.");
    }

    /**
	 * Get the associated ServletContext instance.
	 * @return ServletContext for this ServletApplicationContext.
	 */
	public ServletContext getServletContext() {
		return servletContext;
	}

	public Map getAttributes()
	{
		throw new UnsupportedOperationException( "Method getAttributes is not implemented" );
	}

	public BeanIdStore getBeanIdStore() {
		return beanIdStore;
	}

    public ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }
}
