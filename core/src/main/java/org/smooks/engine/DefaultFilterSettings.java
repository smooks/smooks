/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 - 2024 Smooks
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

import org.smooks.api.FilterSettings;
import org.smooks.api.delivery.StreamFilterType;
import org.smooks.engine.delivery.AnyFilterType;

public class DefaultFilterSettings implements FilterSettings {

    private StreamFilterType filterType = new AnyFilterType();
    private Boolean rewriteEntities = true;
    private Boolean defaultSerializationOn = true;
    private Boolean terminateOnException = true;
    private Boolean maintainElementStack = true;
    private Boolean closeSource = true;
    private Boolean closeSink = true;
    private Integer readerPoolSize = 0;
    private Integer maxNodeDepth = 1;

    public DefaultFilterSettings() {
    }

    @Override
    public FilterSettings setFilterType(StreamFilterType streamFilterType) {
        this.filterType = streamFilterType;
        return this;
    }

    @Override
    public FilterSettings setRewriteEntities(boolean rewriteEntities) {
        this.rewriteEntities = rewriteEntities;
        return this;
    }

    @Override
    public FilterSettings setDefaultSerializationOn(boolean defaultSerializationOn) {
        this.defaultSerializationOn = defaultSerializationOn;
        return this;
    }

    @Override
    public FilterSettings setTerminateOnException(boolean terminateOnException) {
        this.terminateOnException = terminateOnException;
        return this;
    }

    @Override
    public FilterSettings setMaintainElementStack(boolean maintainElementStack) {
        this.maintainElementStack = maintainElementStack;
        return this;
    }

    @Override
    public FilterSettings setCloseSource(boolean closeSource) {
        this.closeSource = closeSource;
        return this;
    }

    @Override
    public FilterSettings setCloseSink(boolean closeSink) {
        this.closeSink = closeSink;
        return this;
    }

    @Override
    public FilterSettings setReaderPoolSize(int readerPoolSize) {
        this.readerPoolSize = readerPoolSize;
        return this;
    }

    @Override
    public FilterSettings setMaxNodeDepth(final int maxNodeDepth) {
        this.maxNodeDepth = maxNodeDepth;
        return this;
    }

    @Override
    public StreamFilterType getFilterType() {
        return filterType;
    }

    @Override
    public Boolean isRewriteEntities() {
        return rewriteEntities;
    }

    @Override
    public Boolean isDefaultSerializationOn() {
        return defaultSerializationOn;
    }

    @Override
    public Boolean isTerminateOnException() {
        return terminateOnException;
    }

    @Override
    public Boolean isMaintainElementStack() {
        return maintainElementStack;
    }

    @Override
    public Boolean isCloseSource() {
        return closeSource;
    }

    @Override
    public Boolean isCloseSink() {
        return closeSink;
    }

    @Override
    public Integer getReaderPoolSize() {
        return readerPoolSize;
    }

    @Override
    public Integer getMaxNodeDepth() {
        return maxNodeDepth;
    }
}
