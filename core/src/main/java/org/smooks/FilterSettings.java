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
package org.smooks;

import org.smooks.api.delivery.Filter;
import org.smooks.engine.resource.config.ParameterAccessor;

/**
 * Smooks filter settings for programmatic configuration of the {@link Smooks} instance.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class FilterSettings {

    public static final FilterSettings DEFAULT_DOM = new FilterSettings(StreamFilterType.DOM);
    public static final FilterSettings DEFAULT_SAX_NG = new FilterSettings(StreamFilterType.SAX_NG);

    private StreamFilterType filterType = StreamFilterType.DOM;
    private boolean rewriteEntities = true;
    private boolean defaultSerializationOn = true;
    private boolean terminateOnException = true;
    private boolean maintainElementStack = true;
    private boolean closeSource = true;
    private boolean closeSink = true;
    private int readerPoolSize;
    private int maxNodeDepth = 1;

    public FilterSettings() {
    }

    public static FilterSettings newDOMSettings() {
        return new FilterSettings(StreamFilterType.DOM);
    }

    public static FilterSettings newSaxNgSettings() {
        return new FilterSettings(StreamFilterType.SAX_NG);
    }

    public FilterSettings(StreamFilterType filterType) {
        assertNonStaticDecl();
        this.filterType = filterType;
    }

    public FilterSettings setFilterType(StreamFilterType filterType) {
        assertNonStaticDecl();
        this.filterType = filterType;
        return this;
    }

    public FilterSettings setRewriteEntities(boolean rewriteEntities) {
        assertNonStaticDecl();
        this.rewriteEntities = rewriteEntities;
        return this;
    }

    public FilterSettings setDefaultSerializationOn(boolean defaultSerializationOn) {
        assertNonStaticDecl();
        this.defaultSerializationOn = defaultSerializationOn;
        return this;
    }

    public FilterSettings setTerminateOnException(boolean terminateOnException) {
        assertNonStaticDecl();
        this.terminateOnException = terminateOnException;
        return this;
    }

    public FilterSettings setMaintainElementStack(boolean maintainElementStack) {
        assertNonStaticDecl();
        this.maintainElementStack = maintainElementStack;
        return this;
    }

    public FilterSettings setCloseSource(boolean closeSource) {
        assertNonStaticDecl();
        this.closeSource = closeSource;
        return this;
    }

    public FilterSettings setCloseSink(boolean closeSink) {
        assertNonStaticDecl();
        this.closeSink = closeSink;
        return this;
    }

    public FilterSettings setReaderPoolSize(int readerPoolSize) {
        assertNonStaticDecl();
        this.readerPoolSize = readerPoolSize;
        return this;
    }

    public FilterSettings setMaxNodeDepth(final int maxNodeDepth) {
        assertNonStaticDecl();
        this.maxNodeDepth = maxNodeDepth;
        return this;
    }

    protected void applySettings(Smooks smooks) {
        // Remove the old params...
        ParameterAccessor.removeParameter(Filter.STREAM_FILTER_TYPE, smooks);
        ParameterAccessor.removeParameter(Filter.ENTITIES_REWRITE, smooks);
        ParameterAccessor.removeParameter(Filter.DEFAULT_SERIALIZATION_ON, smooks);
        ParameterAccessor.removeParameter(Filter.TERMINATE_ON_VISITOR_EXCEPTION, smooks);
        ParameterAccessor.removeParameter(Filter.MAINTAIN_ELEMENT_STACK, smooks);
        ParameterAccessor.removeParameter(Filter.CLOSE_SOURCE, smooks);
        ParameterAccessor.removeParameter(Filter.CLOSE_SINK, smooks);
        ParameterAccessor.removeParameter(Filter.READER_POOL_SIZE, smooks);
        ParameterAccessor.removeParameter(Filter.MAX_NODE_DEPTH, smooks);

        // Set the params...
        ParameterAccessor.setParameter(Filter.STREAM_FILTER_TYPE, filterType.toString(), smooks);
        ParameterAccessor.setParameter(Filter.ENTITIES_REWRITE, Boolean.toString(rewriteEntities), smooks);
        ParameterAccessor.setParameter(Filter.DEFAULT_SERIALIZATION_ON, Boolean.toString(defaultSerializationOn), smooks);
        ParameterAccessor.setParameter(Filter.TERMINATE_ON_VISITOR_EXCEPTION, Boolean.toString(terminateOnException), smooks);
        ParameterAccessor.setParameter(Filter.MAINTAIN_ELEMENT_STACK, Boolean.toString(maintainElementStack), smooks);
        ParameterAccessor.setParameter(Filter.CLOSE_SOURCE, Boolean.toString(closeSource), smooks);
        ParameterAccessor.setParameter(Filter.CLOSE_SINK, Boolean.toString(closeSink), smooks);
        ParameterAccessor.setParameter(Filter.READER_POOL_SIZE, Integer.toString(readerPoolSize), smooks);
        ParameterAccessor.setParameter(Filter.MAX_NODE_DEPTH, Integer.toString(maxNodeDepth), smooks);
    }

    private void assertNonStaticDecl() {
        if (this == DEFAULT_DOM || this == DEFAULT_SAX_NG) {
            throw new UnsupportedOperationException("Invalid attempt to modify static filter type declaration.");
        }
    }
}

