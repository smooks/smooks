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
package org.smooks.engine.delivery;

import org.smooks.api.delivery.ContentDeliveryConfigBuilder;
import org.smooks.api.delivery.ContentDeliveryRuntime;
import org.smooks.api.delivery.ContentDeliveryRuntimeFactory;
import org.smooks.api.delivery.ContentHandlerBinding;
import org.smooks.api.delivery.Filter;
import org.smooks.api.delivery.ReaderPool;
import org.smooks.api.profile.ProfileSet;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.engine.delivery.dom.DOMFilterProvider;
import org.smooks.engine.delivery.sax.ng.SaxNgFilterProvider;
import org.smooks.api.Registry;
import org.smooks.engine.lookup.GlobalParamsLookup;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultContentDeliveryRuntimeFactory implements ContentDeliveryRuntimeFactory {
    private final Map<ContentDeliveryConfigBuilder, ReaderPool> readerPools = new HashMap<>();
    private final Map<String, ContentDeliveryConfigBuilder> contentDeliveryConfigBuilders = new HashMap<>();
    private final Registry registry;

    public DefaultContentDeliveryRuntimeFactory(final Registry registry) {
        this.registry = registry;
    }
    
    @Override
    public ContentDeliveryRuntime create(final ProfileSet profileSet, final List<ContentHandlerBinding<Visitor>> extendedContentHandlerBindings) {
        ContentDeliveryConfigBuilder contentDeliveryConfigBuilder = contentDeliveryConfigBuilders.get(profileSet.getBaseProfile());
        if (contentDeliveryConfigBuilder == null) {
            synchronized (this) {
                if (contentDeliveryConfigBuilders.get(profileSet.getBaseProfile()) == null) {
                    contentDeliveryConfigBuilder = new DefaultContentDeliveryConfigBuilder(profileSet, registry, Arrays.asList(new SaxNgFilterProvider(), new DOMFilterProvider()));
                    final int readerPoolSize = Integer.parseInt(registry.lookup(new GlobalParamsLookup(registry)).getParameterValue(Filter.READER_POOL_SIZE, String.class, "0"));
                    if (readerPoolSize == -1) {
                        readerPools.put(contentDeliveryConfigBuilder, new DynamicReaderPool());
                    } else {
                        readerPools.put(contentDeliveryConfigBuilder, new DefaultReaderPool(readerPoolSize));
                    }
                    contentDeliveryConfigBuilders.put(profileSet.getBaseProfile(), contentDeliveryConfigBuilder);
                } else {
                    contentDeliveryConfigBuilder = contentDeliveryConfigBuilders.get(profileSet.getBaseProfile());
                }
            }
        }
        return new DefaultContentDeliveryRuntime(readerPools.get(contentDeliveryConfigBuilder), contentDeliveryConfigBuilder.build(extendedContentHandlerBindings));
    }
}
