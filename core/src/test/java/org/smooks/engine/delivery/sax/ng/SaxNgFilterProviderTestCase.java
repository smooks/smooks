/*-
 * ========================LICENSE_START=================================
 * Core
 * %%
 * Copyright (C) 2020 - 2022 Smooks
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
package org.smooks.engine.delivery.sax.ng;

import org.junit.jupiter.api.Test;
import org.smooks.api.Registry;
import org.smooks.api.delivery.ContentHandlerBinding;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.config.ResourceConfigSeq;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.api.resource.visitor.sax.ng.BeforeVisitor;
import org.smooks.engine.delivery.DefaultContentHandlerBinding;
import org.smooks.engine.delivery.interceptor.InterceptorVisitorChainFactory;
import org.smooks.engine.lookup.InterceptorVisitorFactoryLookup;
import org.smooks.engine.lookup.NamespaceManagerLookup;
import org.smooks.engine.resource.config.DefaultResourceConfig;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SaxNgFilterProviderTestCase {

    @Test
    public void testCreateContentDeliveryConfigGivenNonIndexableSelector() {
        ContentHandlerBinding<Visitor> contentHandlerBinding = new DefaultContentHandlerBinding<>((BeforeVisitor) (element, executionContext) -> {

        }, new DefaultResourceConfig("not(self::a)", new Properties()));
        SaxNgFilterProvider saxNgFilterProvider = new SaxNgFilterProvider();
        SaxNgContentDeliveryConfig contentDeliveryConfig = saxNgFilterProvider.createContentDeliveryConfig(Collections.singletonList(contentHandlerBinding), new Registry() {
            @Override
            public void registerObject(Object value) {

            }

            @Override
            public void registerObject(Object key, Object value) {

            }

            @Override
            public void deRegisterObject(Object key) {

            }

            @Override
            public <R> R lookup(Function<Map<Object, Object>, R> function) {
                if (function instanceof InterceptorVisitorFactoryLookup) {
                    return (R) new InterceptorVisitorChainFactory();
                } else if (function instanceof NamespaceManagerLookup) {
                    return (R) new Properties();
                }
                return null;
            }

            @Override
            public <T> T lookup(Object key) {
                return null;
            }

            @Override
            public ResourceConfigSeq registerResources(String baseURI, InputStream resourceConfigStream) throws SAXException, IOException, URISyntaxException {
                return null;
            }

            @Override
            public void registerResourceConfig(ResourceConfig resourceConfig) {

            }

            @Override
            public void registerResourceConfigSeq(ResourceConfigSeq resourceConfigSeq) {

            }

            @Override
            public void close() {

            }

            @Override
            public ClassLoader getClassLoader() {
                return null;
            }
        }, new HashMap<>(), new ArrayList<>());

        assertEquals(1, contentDeliveryConfig.getBeforeVisitorIndex().size());
        assertEquals("not(self::a)", contentDeliveryConfig.getBeforeVisitorIndex().get("*").get(0).getResourceConfig().getSelectorPath().getSelector());
    }
}
