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
package org.smooks.engine.lookup;

import org.smooks.api.ExecutionContext;
import org.smooks.api.delivery.ContentDeliveryConfig;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.config.ResourceConfigSeq;
import org.smooks.engine.resource.config.ParameterDecoder;
import org.smooks.engine.resource.config.TokenizedStringParameterDecoder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Accessor class for looking up global parameters.
 * <p id="decode"/>
 * Profile specific parameters are stored under the "global-parameters" selector
 * (see {@link ResourceConfig}).  The parameter values are
 * stored in the &lt;param&gt; elements within this Content Delivery Resource definition.
 * This class iterates over the list of {@link ResourceConfig}
 * elements targeted at the {@link ExecutionContext} profile.  It looks for a definition of the named
 * parameter.  If the &lt;param&gt; has a type attribute the
 * {@link ParameterDecoder} for that type can be applied to the attribute
 * value through the {@link #getParameterObject(String, ContentDeliveryConfig)} method,
 * returning whatever Java type defined by the {@link ParameterDecoder}
 * implementation.  As an example, see {@link TokenizedStringParameterDecoder}.
 *
 * @author tfennelly
 */
public class GlobalParamsLookup implements Function<Map<Object, Object>, GlobalParamsLookup.ParameterAccessor> {

    @Override
    public ParameterAccessor apply(final Map<Object, Object> registryEntries) {
        final Map<String, Object> globalParams = new ConcurrentHashMap<>();
        final List<ResourceConfigSeq> resourceConfigSeqs = new ResourceConfigSeqsLookup().apply(Collections.unmodifiableMap(registryEntries));

        for (final ResourceConfigSeq resourceConfigSeq : resourceConfigSeqs) {
            for (int i = 0; i < resourceConfigSeq.size(); i++) {
                final ResourceConfig nextResourceConfig = resourceConfigSeq.get(i);
                if (ResourceConfig.GLOBAL_PARAMETERS.equals(nextResourceConfig.getSelectorPath().getSelector())) {
                    for (Map.Entry<String, Object> globalParameter : nextResourceConfig.getParameters().entrySet()) {
                        if (globalParams.get(globalParameter.getKey()) == null) {
                            String systemProperty = System.getProperty(globalParameter.getKey());
                            if (systemProperty == null) {
                                globalParams.put(globalParameter.getKey(), nextResourceConfig.getParameterValue(globalParameter.getKey()));
                            } else {
                                globalParams.put(globalParameter.getKey(), systemProperty);
                            }
                        } else {
                            if (!nextResourceConfig.isSystem()) {
                                globalParams.put(globalParameter.getKey(), nextResourceConfig.getParameterValue(globalParameter.getKey()));
                            }
                        }
                    }
                }
            }
        }


        return new ParameterAccessor(globalParams);
    }

    public static class ParameterAccessor {
        private final Map<String, Object> globalParams;

        private ParameterAccessor(Map<String, Object> globalParams) {
            this.globalParams = new ConcurrentHashMap<>(globalParams);
        }

        public <T> T getParameterValue(String name) {
            T globalParam = (T) globalParams.get(name);
            if (globalParam == null) {
                globalParam = (T) System.getProperty(name);
            }
            return globalParam;
        }

        public Map<String, Object> getParameters() {
            return new HashMap<>(globalParams);
        }
    }
}
