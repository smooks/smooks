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

import org.smooks.api.Registry;
import org.smooks.api.delivery.Filter;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.config.ResourceConfigSeq;
import org.smooks.engine.DefaultFilterSettings;
import org.smooks.engine.delivery.dom.DOMFilterType;
import org.smooks.engine.delivery.sax.ng.SaxNgFilterType;
import org.smooks.engine.lookup.ResourceConfigSeqsLookup;
import org.smooks.engine.resource.config.DefaultResourceConfig;

import java.util.Properties;

import static org.smooks.api.resource.config.ResourceConfig.GLOBAL_PARAMETERS;

/**
 * Smooks filter settings for programmatic configuration of the {@link Smooks} instance.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 * @deprecated Use {@link org.smooks.engine.DefaultFilterSettings} instead.
 */
@Deprecated
public class FilterSettings {

    public static final FilterSettings DEFAULT_DOM = new FilterSettings(StreamFilterType.DOM);
    public static final FilterSettings DEFAULT_SAX_NG = new FilterSettings(StreamFilterType.SAX_NG);

    private final org.smooks.api.FilterSettings v2FilterSettings = new DefaultFilterSettings();

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
        if (filterType.equals(StreamFilterType.DOM)) {
            v2FilterSettings.setFilterType(new DOMFilterType());
        } else {
            v2FilterSettings.setFilterType(new SaxNgFilterType());
        }
    }

    public FilterSettings setFilterType(StreamFilterType filterType) {
        assertNonStaticDecl();
        if (filterType.equals(StreamFilterType.DOM)) {
            v2FilterSettings.setFilterType(new DOMFilterType());
        } else {
            v2FilterSettings.setFilterType(new SaxNgFilterType());
        }
        return this;
    }

    public FilterSettings setRewriteEntities(boolean rewriteEntities) {
        assertNonStaticDecl();
        v2FilterSettings.setRewriteEntities(rewriteEntities);
        return this;
    }

    public FilterSettings setDefaultSerializationOn(boolean defaultSerializationOn) {
        assertNonStaticDecl();
        v2FilterSettings.setDefaultSerializationOn(defaultSerializationOn);
        return this;
    }

    public FilterSettings setTerminateOnException(boolean terminateOnException) {
        assertNonStaticDecl();
        v2FilterSettings.setTerminateOnException(terminateOnException);
        return this;
    }

    public FilterSettings setMaintainElementStack(boolean maintainElementStack) {
        assertNonStaticDecl();
        v2FilterSettings.setMaintainElementStack(maintainElementStack);
        return this;
    }

    public FilterSettings setCloseSource(boolean closeSource) {
        assertNonStaticDecl();
        v2FilterSettings.setCloseSource(closeSource);
        return this;
    }

    public FilterSettings setCloseSink(boolean closeSink) {
        assertNonStaticDecl();
        v2FilterSettings.setCloseSink(closeSink);
        return this;
    }

    public FilterSettings setReaderPoolSize(int readerPoolSize) {
        assertNonStaticDecl();
        v2FilterSettings.setReaderPoolSize(readerPoolSize);
        return this;
    }

    public FilterSettings setMaxNodeDepth(final int maxNodeDepth) {
        assertNonStaticDecl();
        v2FilterSettings.setMaxNodeDepth(maxNodeDepth);
        return this;
    }

    public StreamFilterType getFilterType() {
        org.smooks.api.delivery.StreamFilterType streamFilterType = v2FilterSettings.getFilterType();
        if (streamFilterType instanceof DOMFilterType) {
            return StreamFilterType.DOM;
        } else {
            return StreamFilterType.SAX_NG;
        }
    }

    public Boolean isRewriteEntities() {
        return v2FilterSettings.isRewriteEntities();
    }

    public Boolean isDefaultSerializationOn() {
        return v2FilterSettings.isDefaultSerializationOn();
    }

    public Boolean isTerminateOnException() {
        return v2FilterSettings.isTerminateOnException();
    }

    public Boolean isMaintainElementStack() {
        return v2FilterSettings.isMaintainElementStack();
    }

    public Boolean isCloseSource() {
        return v2FilterSettings.isCloseSource();
    }

    public Boolean isCloseSink() {
        return v2FilterSettings.isCloseSink();
    }

    public Integer getReaderPoolSize() {
        return v2FilterSettings.getReaderPoolSize();
    }

    public Integer getMaxNodeDepth() {
        return v2FilterSettings.getMaxNodeDepth();
    }

    protected void applySettings(Registry registry) {
        // Remove the old params...
        removeParameter(Filter.STREAM_FILTER_TYPE, registry);
        removeParameter(Filter.ENTITIES_REWRITE, registry);
        removeParameter(Filter.DEFAULT_SERIALIZATION_ON, registry);
        removeParameter(Filter.TERMINATE_ON_VISITOR_EXCEPTION, registry);
        removeParameter(Filter.MAINTAIN_ELEMENT_STACK, registry);
        removeParameter(Filter.CLOSE_SOURCE, registry);
        removeParameter(Filter.CLOSE_SINK, registry);
        removeParameter(Filter.READER_POOL_SIZE, registry);
        removeParameter(Filter.MAX_NODE_DEPTH, registry);

        // Set the params...
        setParameter(Filter.STREAM_FILTER_TYPE, v2FilterSettings.getFilterType().getName(), registry);
        setParameter(Filter.ENTITIES_REWRITE, Boolean.toString(v2FilterSettings.isRewriteEntities()), registry);
        setParameter(Filter.DEFAULT_SERIALIZATION_ON, Boolean.toString(v2FilterSettings.isDefaultSerializationOn()), registry);
        setParameter(Filter.TERMINATE_ON_VISITOR_EXCEPTION, Boolean.toString(v2FilterSettings.isTerminateOnException()), registry);
        setParameter(Filter.MAINTAIN_ELEMENT_STACK, Boolean.toString(v2FilterSettings.isMaintainElementStack()), registry);
        setParameter(Filter.CLOSE_SOURCE, Boolean.toString(v2FilterSettings.isCloseSource()), registry);
        setParameter(Filter.CLOSE_SINK, Boolean.toString(v2FilterSettings.isCloseSink()), registry);
        setParameter(Filter.READER_POOL_SIZE, Integer.toString(v2FilterSettings.getReaderPoolSize()), registry);
        setParameter(Filter.MAX_NODE_DEPTH, Integer.toString(v2FilterSettings.getMaxNodeDepth()), registry);
    }

    private void assertNonStaticDecl() {
        if (this == DEFAULT_DOM || this == DEFAULT_SAX_NG) {
            throw new UnsupportedOperationException("Invalid attempt to modify static filter type declaration.");
        }
    }

    private void setParameter(String name, Object value, Registry registry) {
        ResourceConfig resourceConfig = new DefaultResourceConfig(GLOBAL_PARAMETERS, new Properties());
        resourceConfig.setParameter(name, value);
        registry.registerResourceConfig(resourceConfig);
    }

    private void removeParameter(String name, Registry registry) {
        for (ResourceConfigSeq resourceConfigSeq : registry.lookup(new ResourceConfigSeqsLookup())) {
            for (int i = 0; i < resourceConfigSeq.size(); i++) {
                ResourceConfig nextResourceConfig = resourceConfigSeq.get(i);
                if (GLOBAL_PARAMETERS.equals(nextResourceConfig.getSelectorPath().getSelector())) {
                    nextResourceConfig.removeParameter(name);
                }
            }
        }
    }
}