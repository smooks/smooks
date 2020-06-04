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
package org.smooks.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.smooks.cdr.SmooksResourceConfigurationStore;
import org.smooks.javabean.lifecycle.BeanContextLifecycleObserver;
import org.smooks.resource.ContainerResourceLocator;
import org.smooks.javabean.context.BeanIdStore;
import org.smooks.profile.ProfileStore;
import org.smooks.profile.DefaultProfileStore;

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
      * @see org.smooks.container.ApplicationContext#getResourceLocator()
      */
	public ContainerResourceLocator getResourceLocator() {
		return containerResourceLocator;
	}

	/* (non-Javadoc)
	 * @see org.smooks.container.BoundAttributeStore#setAttribute(java.lang.Object, java.lang.Object)
	 */
	public void setAttribute(Object key, Object value) {
		attributes.put(key, value);
	}

	/* (non-Javadoc)
	 * @see org.smooks.container.BoundAttributeStore#getAttribute(java.lang.Object)
	 */
	public Object getAttribute(Object key) {
		return attributes.get(key);
	}

	/* (non-Javadoc)
	 * @see org.smooks.container.BoundAttributeStore#removeAttribute(java.lang.Object)
	 */
	public void removeAttribute(Object key) {
		attributes.remove(key);
	}

	private static String STORE_KEY = MockApplicationContext.class.getName() + "#CDRStore";
	/* (non-Javadoc)
	 * @see org.smooks.container.ApplicationContext#getCdrarStore()
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
