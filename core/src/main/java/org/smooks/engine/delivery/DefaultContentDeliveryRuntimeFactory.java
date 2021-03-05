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

import org.smooks.api.delivery.*;
import org.smooks.api.profile.ProfileSet;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.engine.delivery.dom.DOMFilterProvider;
import org.smooks.engine.delivery.sax.SAXFilterProvider;
import org.smooks.engine.delivery.sax.ng.SaxNgFilterProvider;
import org.smooks.api.Registry;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultContentDeliveryRuntimeFactory implements ContentDeliveryRuntimeFactory {
    private final Map<ContentDeliveryConfigBuilder, ReaderPool> readerPools = new ConcurrentHashMap<>();
    private final Map<String, ContentDeliveryConfigBuilder> contentDeliveryConfigBuilders = new ConcurrentHashMap<>();
    private final Registry registry;

    public DefaultContentDeliveryRuntimeFactory(final Registry registry) {
        this.registry = registry;
    }
    
    @Override
    public ContentDeliveryRuntime create(final ProfileSet profileSet, final List<ContentHandlerBinding<Visitor>> extendedContentHandlerBindings) {
        final ContentDeliveryConfigBuilder contentDeliveryConfigBuilder = contentDeliveryConfigBuilders.computeIfAbsent(profileSet.getBaseProfile(), p -> new DefaultContentDeliveryConfigBuilder(profileSet, registry, Arrays.asList(new SaxNgFilterProvider(), new SAXFilterProvider(), new DOMFilterProvider())));
        final ContentDeliveryConfig contentDeliveryConfig = contentDeliveryConfigBuilder.build(extendedContentHandlerBindings);
        final ReaderPool readerPool = readerPools.computeIfAbsent(contentDeliveryConfigBuilder, c -> new DefaultReaderPool(contentDeliveryConfig));

        return new DefaultContentDeliveryRuntime(readerPool, contentDeliveryConfig);
    }
}
