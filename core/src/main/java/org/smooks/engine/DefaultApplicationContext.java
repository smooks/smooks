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
package org.smooks.engine;

import org.smooks.api.SmooksException;
import org.smooks.api.profile.ProfileStore;
import org.smooks.classpath.IsAnnotationPresentFilter;
import org.smooks.classpath.Scanner;
import org.smooks.api.ApplicationContext;
import org.smooks.api.delivery.ContentDeliveryRuntimeFactory;
import org.smooks.api.bean.context.BeanIdStore;
import org.smooks.engine.bean.context.DefaultBeanIdStore;
import org.smooks.api.bean.lifecycle.BeanContextLifecycleObserver;
import org.smooks.engine.profile.DefaultProfileStore;
import org.smooks.api.Registry;
import org.smooks.api.resource.ContainerResourceLocator;
import org.smooks.resource.URIResourceLocator;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Standalone container execution context for Smooks.
 * <p/>
 * This context allows Smooks to be executed outside the likes of a
 * Servlet Container.
 * @author tfennelly
 */
public class DefaultApplicationContext implements ApplicationContext {

	private ContainerResourceLocator resourceLocator;
	private Registry registry;
	private final DefaultProfileStore profileStore = new DefaultProfileStore();
	private final BeanIdStore beanIdStore = new DefaultBeanIdStore();
    private final List<BeanContextLifecycleObserver> beanContextObservers = new ArrayList<BeanContextLifecycleObserver>();
    private ClassLoader classLoader;
    private ContentDeliveryRuntimeFactory contentDeliveryConfigBuilderFactory;
    
    /**
     * Private constructor.
     */
    DefaultApplicationContext() {
		IsAnnotationPresentFilter isAnnotationPresentFilter = new IsAnnotationPresentFilter(Resource.class);
		Scanner scanner = new Scanner(isAnnotationPresentFilter);
		try {
			scanner.scanClasspath(this.getClass().getClassLoader());
		} catch (IOException e) {
			throw new SmooksException(e.getMessage(), e);
		}
		List<Class<?>> classes = isAnnotationPresentFilter.getClasses();
		resourceLocator = new URIResourceLocator();
        ((URIResourceLocator)resourceLocator).setBaseURI(URI.create(URIResourceLocator.SCHEME_CLASSPATH + ":/"));
    }

	@Override
	public ContainerResourceLocator getResourceLocator() {
		return resourceLocator;
	}

	@Override
	public void setResourceLocator(ContainerResourceLocator resourceLocator) {
        this.resourceLocator = resourceLocator;
    }

    @Override
	public Registry getRegistry() {
		return registry;
	}

    /**
	 * Get the ProfileStore in use within the Standalone Context.
	 * @return The ProfileStore.
	 */
	@Override
	public ProfileStore getProfileStore() {
		return profileStore;
	}

	@Override
	public BeanIdStore getBeanIdStore() {
		return beanIdStore;
	}

    public void addBeanContextLifecycleObserver(BeanContextLifecycleObserver observer) {
        beanContextObservers.add(observer);
    }

	@Override
	public Collection<BeanContextLifecycleObserver> getBeanContextLifecycleObservers() {
        return Collections.unmodifiableCollection(beanContextObservers);
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

	@Override
	public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setContentDeliveryConfigBuilderFactory(final ContentDeliveryRuntimeFactory contentDeliveryConfigBuilderFactory) {
		this.contentDeliveryConfigBuilderFactory = contentDeliveryConfigBuilderFactory;
	}
    
	@Override
	public ContentDeliveryRuntimeFactory getContentDeliveryConfigBuilderFactory() {
		return contentDeliveryConfigBuilderFactory;
	}

	void setRegistry(Registry registry) {
        this.registry = registry;
    }
}
