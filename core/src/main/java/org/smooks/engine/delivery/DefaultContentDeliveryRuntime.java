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

import org.smooks.api.ExecutionContext;
import org.smooks.api.delivery.ContentDeliveryConfig;
import org.smooks.api.delivery.ContentDeliveryRuntime;
import org.smooks.api.delivery.ReaderPool;
import org.smooks.api.delivery.event.ExecutionEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultContentDeliveryRuntime implements ContentDeliveryRuntime {
    private final ReaderPool readerPool;
    private final ContentDeliveryConfig contentDeliveryConfig;
    private final List<ExecutionEventListener> executionEventListeners = new ArrayList<>();

    public DefaultContentDeliveryRuntime(final ReaderPool readerPool, final ContentDeliveryConfig contentDeliveryConfig) {
        this.readerPool = readerPool;
        this.contentDeliveryConfig = contentDeliveryConfig;
    }

    @Override
    public ReaderPool getReaderPool() {
        return readerPool;
    }

    @Override
    public ContentDeliveryConfig getContentDeliveryConfig() {
        return contentDeliveryConfig;
    }

    /**
     * Add an ExecutionEventListener for the {@link ExecutionContext}.
     * <p/>
     * Allows calling code to listen to (and capture data on) specific
     * context execution events e.g. the targeting of resources.
     * <p/>
     * Note, this is not a logging facility and should be used with care.
     * It's overuse should be avoided as it can have a serious negative effect
     * on performance.  By default, no listenrs are applied and so no overhead
     * is incured.
     *
     * @param listener The listener instance.
     * @see org.smooks.engine.delivery.event.BasicExecutionEventListener
     */
    @Override
    public void addExecutionEventListener(final ExecutionEventListener executionEventListener) {
        this.executionEventListeners.add(executionEventListener);
    }

    @Override
    public void removeExecutionEventListener(final ExecutionEventListener executionEventListener) {
        this.executionEventListeners.remove(executionEventListener);
    }

    /**
     * Get the ExecutionEventListener for the {@link ExecutionContext}.
     * @return The listener instance.
     * @see #addExecutionEventListener(ExecutionEventListener)
     */
    @Override
    public List<ExecutionEventListener> getExecutionEventListeners() {
        return Collections.unmodifiableList(executionEventListeners);
    }
}
