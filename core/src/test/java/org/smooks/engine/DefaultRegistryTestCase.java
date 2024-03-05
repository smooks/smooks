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
package org.smooks.engine;

import org.junit.jupiter.api.Test;
import org.smooks.api.Registry;
import org.smooks.engine.lookup.converter.SourceTargetTypeConverterFactoryLookup;
import org.smooks.engine.lookup.converter.TypeConverterFactoryLookup;
import org.smooks.engine.profile.DefaultProfileStore;
import org.smooks.engine.resource.config.loader.xml.XmlResourceConfigLoader;
import org.smooks.tck.resource.MockContainerResourceLocator;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Enumeration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DefaultRegistryTestCase {

    @Test
    public void testLookupWithNonClassClassLoader() {
        Registry registry = new DefaultRegistry(new ClassLoader() {
            @Override
            public Enumeration<URL> getResources(String name) throws IOException {
                return new Enumeration<URL>() {
                    @Override
                    public boolean hasMoreElements() {
                        return false;
                    }

                    @Override
                    public URL nextElement() {
                        return null;
                    }
                };
            }
        }, new XmlResourceConfigLoader(), new DefaultProfileStore());
        assertNull(registry.lookup(new SourceTargetTypeConverterFactoryLookup<>(BigDecimal.class, String.class)));
    }

    @Test
    public void testLookupWithClassClassLoader() {
        Registry registry = new DefaultRegistry(getClass().getClassLoader(), new XmlResourceConfigLoader(), new DefaultProfileStore());
        assertNotNull(registry.lookup(new SourceTargetTypeConverterFactoryLookup<>(BigDecimal.class, String.class)));
    }
}
