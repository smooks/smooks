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

import org.smooks.cdr.SmooksResourceConfiguration;

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
    private final boolean isLifecycleInitializable;
    private final boolean isLifecycleCleanable;
    private final SmooksResourceConfiguration resourceConfig;

    /**
     * Public constructor.
     * @param contentHandler The content handler instance.
     * @param resourceConfig The defining resource configuration.
     */
    public ContentHandlerBinding(T contentHandler, SmooksResourceConfiguration resourceConfig) {
        this.contentHandler = contentHandler;
        this.resourceConfig = resourceConfig;
        isLifecycleInitializable = (contentHandler instanceof ExecutionLifecycleInitializable);
        isLifecycleCleanable = (contentHandler instanceof ExecutionLifecycleCleanable);
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
     * @return The {@link SmooksResourceConfiguration}.
     */
    public SmooksResourceConfiguration getResourceConfig() {
        return resourceConfig;
    }

    /**
     * Does the ContentHandler implement {@link ExecutionLifecycleInitializable}.
     * @return True if the ContentHandler implements {@link ExecutionLifecycleInitializable}, otherwise false.
     */
    public boolean isLifecycleInitializable() {
        return isLifecycleInitializable;
    }

    /**
     * Does the ContentHandler implement {@link ExecutionLifecycleCleanable}.
     * @return True if the ContentHandler implements {@link ExecutionLifecycleCleanable}, otherwise false.
     */
    public boolean isLifecycleCleanable() {
        return isLifecycleCleanable;
    }
}
