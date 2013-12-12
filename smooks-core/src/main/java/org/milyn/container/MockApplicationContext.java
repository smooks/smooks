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
import org.milyn.javabean.context.BeanIdStore;
import org.milyn.javabean.lifecycle.BeanContextLifecycleObserver;
import org.milyn.commons.profile.DefaultProfileStore;
import org.milyn.commons.profile.ProfileStore;
import org.milyn.commons.resource.ContainerResourceLocator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tfennelly
 */
public class MockApplicationContext implements ApplicationContext {

    public MockContainerResourceLocator containerResourceLocator = new MockContainerResourceLocator();
    public ProfileStore profileStore = new DefaultProfileStore();
    private Hashtable<Object, Object> attributes = new Hashtable<Object, Object>();
    private BeanIdStore beanIdStore = new BeanIdStore();
    private List<BeanContextLifecycleObserver> beanContextObservers = new ArrayList<BeanContextLifecycleObserver>();

    /* (non-Javadoc)
      * @see org.milyn.container.ApplicationContext#getResourceLocator()
      */
	public ContainerResourceLocator getResourceLocator() {
		return containerResourceLocator;
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

	private static String STORE_KEY = MockApplicationContext.class.getName() + "#CDRStore";
	/* (non-Javadoc)
	 * @see org.milyn.container.ApplicationContext#getCdrarStore()
	 */
	public SmooksResourceConfigurationStore getStore() {
        SmooksResourceConfigurationStore cdrarStore = (SmooksResourceConfigurationStore)getAttribute(STORE_KEY);

		if(cdrarStore == null) {
			cdrarStore = new SmooksResourceConfigurationStore(this);
			setAttribute(STORE_KEY, cdrarStore);
		}

		return cdrarStore;
	}

    public ProfileStore getProfileStore() {
        return profileStore;
    }

    public void setResourceLocator(ContainerResourceLocator resourceLocator) {
        throw new UnsupportedOperationException("Can't set the locator on the Mock using this method.  Set it through the publicly accessible  property.");
    }

    public Map<Object, Object> getAttributes()
    {
    	return attributes;
    }

	public BeanIdStore getBeanIdStore() {
		return beanIdStore;
	}

    public void addBeanContextLifecycleObserver(BeanContextLifecycleObserver observer) {
        beanContextObservers.add(observer);
    }

    public Collection<BeanContextLifecycleObserver> getBeanContextLifecycleObservers() {
        return Collections.unmodifiableCollection(beanContextObservers);
    }

    public ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }
}
