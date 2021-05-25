/*-
 * ========================LICENSE_START=================================
 * Core
 * %%
 * Copyright (C) 2020 - 2021 Smooks
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
package org.smooks.engine.delivery;

import org.smooks.api.Registry;
import org.smooks.api.delivery.ContentHandler;
import org.smooks.api.delivery.ContentHandlerBinding;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.engine.injector.FieldInjector;
import org.smooks.engine.injector.Scope;
import org.smooks.engine.lifecycle.PostConstructLifecyclePhase;
import org.smooks.engine.lookup.LifecycleManagerLookup;
import org.smooks.engine.lookup.NamespaceManagerLookup;
import org.smooks.engine.resource.config.DefaultResourceConfig;

import java.util.Objects;

public class DefaultContentHandlerBinding<T extends ContentHandler> implements ContentHandlerBinding<T> {
    private final T contentHandler;
    private final ResourceConfig resourceConfig;
    private int hash;

    /**
     * Public constructor.
     * @param contentHandler The content handler instance.
     * @param resourceConfig The defining resource configuration.
     */
    public DefaultContentHandlerBinding(final T contentHandler, final ResourceConfig resourceConfig) {
        this.contentHandler = contentHandler;
        this.resourceConfig = resourceConfig;
    }

    public DefaultContentHandlerBinding(final T contentHandler, final String targetSelector, @Deprecated final String targetSelectorNS, final Registry registry) {
        this.contentHandler = contentHandler;
        resourceConfig = new DefaultResourceConfig(targetSelector, registry.lookup(new NamespaceManagerLookup()), contentHandler.getClass().getName());
        resourceConfig.getSelectorPath().setSelectorNamespaceURI(targetSelectorNS);

        final FieldInjector fieldInjector = new FieldInjector(contentHandler, new Scope(registry, resourceConfig, contentHandler));
        fieldInjector.inject();
        registry.lookup(new LifecycleManagerLookup()).applyPhase(contentHandler, new PostConstructLifecyclePhase());
        registry.registerObject(contentHandler);
    }

    /**
     * Get the content handler.
     * @return The {@link ContentHandler}.
     */
    @Override
    public T getContentHandler() {
        return contentHandler;
    }

    /**
     * Get the resource configuration.
     * @return The {@link ResourceConfig}.
     */
    @Override
    public ResourceConfig getResourceConfig() {
        return resourceConfig;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContentHandlerBinding)) {
            return false;
        }
        final ContentHandlerBinding<?> that = (ContentHandlerBinding<?>) o;
        return Objects.equals(contentHandler, that.getContentHandler()) &&
                Objects.equals(resourceConfig, that.getResourceConfig());
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = Objects.hash(contentHandler, resourceConfig);
        }

        return hash;
    }
}
