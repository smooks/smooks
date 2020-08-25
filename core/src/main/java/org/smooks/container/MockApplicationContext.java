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

import org.smooks.cdr.registry.Registry;
import org.smooks.container.standalone.DefaultApplicationContextBuilder;
import org.smooks.container.standalone.StandaloneApplicationContext;
import org.smooks.javabean.context.BeanIdStore;
import org.smooks.javabean.lifecycle.BeanContextLifecycleObserver;
import org.smooks.profile.ProfileStore;
import org.smooks.resource.ContainerResourceLocator;

import java.util.Collection;

/**
 * @author tfennelly
 */
public class MockApplicationContext implements ApplicationContext {

	private final StandaloneApplicationContext standaloneApplicationContext;
	public final MockContainerResourceLocator containerResourceLocator = new MockContainerResourceLocator();

	public MockApplicationContext() {
		this.standaloneApplicationContext = new DefaultApplicationContextBuilder().build();
	}
	
	/* (non-Javadoc)
	 * @see org.smooks.container.ApplicationContext#getResourceLocator()
	 */
	public ContainerResourceLocator getResourceLocator() {
		return containerResourceLocator;
	}

	/* (non-Javadoc)
	 * @see org.smooks.container.ApplicationContext#getCdrarStore()
	 */
	public Registry getRegistry() {
		return standaloneApplicationContext.getRegistry();
	}

	public ProfileStore getProfileStore() {
		return standaloneApplicationContext.getProfileStore();
	}

	public void setResourceLocator(ContainerResourceLocator resourceLocator) {
		throw new UnsupportedOperationException("Can't set the locator on the Mock using this method.  Set it through the publicly accessible  property.");
	}

	public BeanIdStore getBeanIdStore() {
		return standaloneApplicationContext.getBeanIdStore();
	}

	public void addBeanContextLifecycleObserver(BeanContextLifecycleObserver observer) {
		standaloneApplicationContext.addBeanContextLifecycleObserver(observer);
	}

	public Collection<BeanContextLifecycleObserver> getBeanContextLifecycleObservers() {
		return standaloneApplicationContext.getBeanContextLifecycleObservers();
	}

	public ClassLoader getClassLoader() {
		return standaloneApplicationContext.getClassLoader();
	}
}
