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
package org.smooks.delivery;

import org.smooks.cdr.ResourceConfig;
import org.smooks.injector.FieldInjector;
import org.smooks.injector.Scope;
import org.smooks.lifecycle.phase.PostConstructLifecyclePhase;
import org.smooks.registry.Registry;
import org.smooks.registry.lookup.LifecycleManagerLookup;

import java.util.Objects;

/**
 * Mapping between a resource configuration and its corresponding resource
 * configuration.
 * <p/>
 * Obviously this class is only relevant when the resource configuration refers to
 * a {@link ContentHandler}.
 * @author tfennelly
 */
public class ContentHandlerBinding<T extends ContentHandler> {

    private final T contentHandler;
    private final ResourceConfig resourceConfig;

    /**
     * Public constructor.
     * @param contentHandler The content handler instance.
     * @param resourceConfig The defining resource configuration.
     */
    public ContentHandlerBinding(final T contentHandler, final ResourceConfig resourceConfig) {
        this.contentHandler = contentHandler;
        this.resourceConfig = resourceConfig;
    }

    public ContentHandlerBinding(final T contentHandler, final String targetSelector, @Deprecated final String targetSelectorNS, final Registry registry) {
        this.contentHandler = contentHandler;
        resourceConfig = new ResourceConfig(targetSelector, contentHandler.getClass().getName());
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
    public T getContentHandler() {
        return contentHandler;
    }

    /**
     * Get the resource configuration.
     * @return The {@link ResourceConfig}.
     */
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
        return Objects.equals(contentHandler, that.contentHandler) &&
                Objects.equals(resourceConfig, that.resourceConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contentHandler, resourceConfig);
    }
}
