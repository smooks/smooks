/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 - 2025 Smooks
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
package org.smooks.engine.injector;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.smooks.api.Registry;
import org.smooks.api.resource.config.Parameter;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.engine.DefaultRegistry;
import org.smooks.engine.profile.DefaultProfileStore;
import org.smooks.engine.resource.config.DefaultResourceConfig;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FieldInjectorTestCase {

    private static class QuuzWithoutDefaultFieldValue {
        @Inject
        protected List<String> foo;
    }

    private static class QuuzWithDefaultFieldValue {
        @Inject
        protected List<String> foo = new Vector<>();
    }

    private static List<ResourceConfig> provideResourceConfigs() {
        List<ResourceConfig> resourceConfigs = new ArrayList<>();

        ResourceConfig resourceConfigWithMultipleValuesForParam = new DefaultResourceConfig();
        resourceConfigWithMultipleValuesForParam.setParameter("foo", "test1");
        resourceConfigWithMultipleValuesForParam.setParameter("foo", "test2");

        ResourceConfig resourceConfigWithSingValueForParam = new DefaultResourceConfig();
        resourceConfigWithSingValueForParam.setParameter("foo", "test1");

        resourceConfigs.add(resourceConfigWithMultipleValuesForParam);
        resourceConfigs.add(resourceConfigWithSingValueForParam);

        return resourceConfigs;
    }

    @ParameterizedTest
    @MethodSource("provideResourceConfigs")
    public void testInjectWhenFieldIsListWithoutValue(ResourceConfig resourceConfig) {
        QuuzWithoutDefaultFieldValue quuz = new QuuzWithoutDefaultFieldValue();
        Registry registry = new DefaultRegistry(this.getClass().getClassLoader(), (inputStream, baseURI, classLoader) -> null, new DefaultProfileStore());

        FieldInjector fieldInjector = new FieldInjector(quuz, new Scope(registry, resourceConfig, null));
        fieldInjector.inject();

        List<Parameter<?>> parameters = resourceConfig.getParameters("foo");
        assertEquals(ArrayList.class, quuz.foo.getClass());
        assertEquals(parameters.size(), quuz.foo.size());
        for (Parameter<?> parameter : parameters) {
            assertTrue(quuz.foo.contains(parameter.getValue()));
        }
    }

    @ParameterizedTest
    @MethodSource("provideResourceConfigs")
    public void testInjectWhenFieldIsListWithValue(ResourceConfig resourceConfig) {
        QuuzWithDefaultFieldValue quuz = new QuuzWithDefaultFieldValue();
        Registry registry = new DefaultRegistry(this.getClass().getClassLoader(), (inputStream, baseURI, classLoader) -> null, new DefaultProfileStore());

        FieldInjector fieldInjector = new FieldInjector(quuz, new Scope(registry, resourceConfig, null));
        fieldInjector.inject();

        List<Parameter<?>> parameters = resourceConfig.getParameters("foo");
        assertEquals(Vector.class, quuz.foo.getClass());
        assertEquals(parameters.size(), quuz.foo.size());
        for (Parameter<?> parameter : parameters) {
            assertTrue(quuz.foo.contains(parameter.getValue()));
        }
    }

}
