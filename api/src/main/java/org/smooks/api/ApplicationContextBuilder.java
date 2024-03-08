/*-
 * ========================LICENSE_START=================================
 * API
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
package org.smooks.api;

import org.smooks.api.delivery.ContentDeliveryRuntimeFactory;
import org.smooks.api.delivery.ReaderPoolFactory;
import org.smooks.api.resource.ContainerResourceLocator;
import org.smooks.api.resource.config.loader.ResourceConfigLoader;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Constructs an {@link ApplicationContext}.
 */
@ThreadSafe
public interface ApplicationContextBuilder {

    /**
     * Sets the class loader for the application context.
     *
     * @param classLoader class loader to use for the application context
     * @return a shallow copy of <code>this</code> with <code>classLoader</code> set to the given value
     */
    ApplicationContextBuilder withClassLoader(ClassLoader classLoader);

    /**
     * Sets the registry for the application context.
     *
     * @param registry registry to use for the application context
     * @return a shallow copy of <code>this</code> with <code>registry</code> set to the given value
     */
    ApplicationContextBuilder withRegistry(Registry registry);

    /**
     * Sets the content delivery runtime factory for the application context.
     *
     * @param contentDeliveryRuntimeFactory content delivery runtime factory to use for the application context
     * @return a shallow copy of <code>this</code> with <code>contentDeliveryRuntimeFactory</code> set to the given value
     */
    ApplicationContextBuilder withContentDeliveryRuntimeFactory(ContentDeliveryRuntimeFactory contentDeliveryRuntimeFactory);

    /**
     * Sets the resource locator for the application context.
     *
     * @param resourceLocator resource locator to use for the application context
     * @return a shallow copy of <code>this</code> with <code>resourceLocator</code> set to the given value
     */
    ApplicationContextBuilder withResourceLocator(ContainerResourceLocator resourceLocator);


    /**
     * Sets the resource config loader for the application context.
     *
     * @param resourceConfigLoader resource config loader to use for the application context
     * @return a shallow copy of <code>this</code> with <code>resourceConfigLoader</code> set to the given value
     */
    ApplicationContextBuilder withResourceConfigLoader(ResourceConfigLoader resourceConfigLoader);

    /**
     * Sets reader pool factory for the application context.
     *
     * @param readerPoolFactory reader pool factory to use for the application context
     * @return a shallow copy of <code>this</code> with <code>readerPoolFactory</code> set to the given value
     */
    ApplicationContextBuilder withReaderPoolFactory(ReaderPoolFactory readerPoolFactory);


    /**
     * @return a new application context
     */
    ApplicationContext build();

}
