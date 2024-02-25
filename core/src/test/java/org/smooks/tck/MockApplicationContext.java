/*-
 * ========================LICENSE_START=================================
 * Core
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
package org.smooks.tck;

import org.smooks.api.ApplicationContext;
import org.smooks.api.delivery.ContentDeliveryRuntimeFactory;
import org.smooks.api.bean.context.BeanIdStore;
import org.smooks.api.bean.lifecycle.BeanContextLifecycleObserver;
import org.smooks.api.Registry;
import org.smooks.api.profile.ProfileStore;
import org.smooks.api.resource.ContainerResourceLocator;
import org.smooks.engine.DefaultApplicationContextBuilder;
import org.smooks.tck.resource.MockContainerResourceLocator;

import java.util.Collection;

/**
 * @author tfennelly
 */
public class MockApplicationContext implements ApplicationContext {

	private final ApplicationContext applicationContext;
	public final MockContainerResourceLocator containerResourceLocator = new MockContainerResourceLocator();

	public MockApplicationContext() {
		this.applicationContext = new DefaultApplicationContextBuilder().build();
	}
	
	/* (non-Javadoc)
	 * @see org.smooks.engine.container.ApplicationContext#getResourceLocator()
	 */
	@Override
	public ContainerResourceLocator getResourceLocator() {
		return containerResourceLocator;
	}

	/* (non-Javadoc)
	 * @see org.smooks.engine.container.ApplicationContext#getCdrarStore()
	 */
	@Override
	public Registry getRegistry() {
		return applicationContext.getRegistry();
	}

	@Override
	public ProfileStore getProfileStore() {
		return applicationContext.getProfileStore();
	}

	@Override
	public BeanIdStore getBeanIdStore() {
		return applicationContext.getBeanIdStore();
	}

	@Override
	public void addBeanContextLifecycleObserver(BeanContextLifecycleObserver observer) {
		applicationContext.addBeanContextLifecycleObserver(observer);
	}

	@Override
	public Collection<BeanContextLifecycleObserver> getBeanContextLifecycleObservers() {
		return applicationContext.getBeanContextLifecycleObservers();
	}

	@Override
	public ClassLoader getClassLoader() {
		return applicationContext.getClassLoader();
	}

	@Override
	public ContentDeliveryRuntimeFactory getContentDeliveryRuntimeFactory() {
		return applicationContext.getContentDeliveryRuntimeFactory();
	}
}
