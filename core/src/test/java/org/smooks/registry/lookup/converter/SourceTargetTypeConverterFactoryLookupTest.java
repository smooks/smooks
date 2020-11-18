/*-
 * ========================LICENSE_START=================================
 * Smooks Core
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
package org.smooks.registry.lookup.converter;

import org.junit.Test;
import org.smooks.converter.TypeConverter;
import org.smooks.converter.TypeConverterDescriptor;
import org.smooks.converter.factory.TypeConverterFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertNotNull;

public class SourceTargetTypeConverterFactoryLookupTest {
    
    @Test
    public void testApply() {
        Map<Object, Object> registryEntries = new HashMap<>();
        registryEntries.put(TypeConverterFactoryLookup.TYPE_CONVERTER_FACTORY_REGISTRY_KEY, new HashSet() {{
            this.add(new TypeConverterFactory<String, Integer>() {
                @Override
                public TypeConverter<String, Integer> createTypeConverter() {
                    return Integer::valueOf;
                }

                @Override
                public TypeConverterDescriptor<Class<String>, Class<Integer>> getTypeConverterDescriptor() {
                    return new TypeConverterDescriptor<>(String.class, Integer.class);
                }
            });

            this.add(new TypeConverterFactory<String, String>() {
                @Override
                public TypeConverter<String, String> createTypeConverter() {
                    return value -> value;
                }

                @Override
                public TypeConverterDescriptor<Class<String>, Class<String>> getTypeConverterDescriptor() {
                    return new TypeConverterDescriptor<>(String.class, String.class);
                }
            });
        }});

        SourceTargetTypeConverterFactoryLookup<String, Integer> sourceTargetTypeConverterFactoryLookup = new SourceTargetTypeConverterFactoryLookup<>(String.class, Integer.class);
        TypeConverter<? super String, ? extends Integer> typeConverter = sourceTargetTypeConverterFactoryLookup.apply(registryEntries).createTypeConverter();
        assertNotNull(typeConverter);
        assertNotNull(typeConverter.convert("1"));
    }

    @Test
    public void testApplyGivenTypeConverterFactoriesWithMatchingSourceAndTargetTypesButDifferentPriorities() {
        Map<Object, Object> registryEntries = new HashMap<>();
        registryEntries.put(TypeConverterFactoryLookup.TYPE_CONVERTER_FACTORY_REGISTRY_KEY, new HashSet<>());

        TypeConverterFactory<String, Integer> lowPriorityTypeConverterFactory = new TypeConverterFactory<String, Integer>() {
            @Override
            public TypeConverter<String, Integer> createTypeConverter() {
                throw new AssertionError();
            }

            @Override
            public TypeConverterDescriptor<Class<String>, Class<Integer>> getTypeConverterDescriptor() {
                return new TypeConverterDescriptor<>(String.class, Integer.class);
            }
        };

        TypeConverterFactory<String, Integer> mediumPriorityTypeConverterFactory = new TypeConverterFactory<String, Integer>() {
            @Override
            public TypeConverter<String, Integer> createTypeConverter() {
                throw new AssertionError();
            }

            @Override
            public TypeConverterDescriptor<Class<String>, Class<Integer>> getTypeConverterDescriptor() {
                return new TypeConverterDescriptor<>(String.class, Integer.class, new Integer(Short.MAX_VALUE / 2).shortValue());
            }
        };

        TypeConverterFactory<String, Integer> highPriorityTypeConverterFactory = new TypeConverterFactory<String, Integer>() {
            @Override
            public TypeConverter<String, Integer> createTypeConverter() {
                return Integer::valueOf;
            }

            @Override
            public TypeConverterDescriptor<Class<String>, Class<Integer>> getTypeConverterDescriptor() {
                return new TypeConverterDescriptor<>(String.class, Integer.class, Short.MAX_VALUE);
            }
        };

        ((Set<TypeConverterFactory<?, ?>>) registryEntries.get(TypeConverterFactoryLookup.TYPE_CONVERTER_FACTORY_REGISTRY_KEY)).add(lowPriorityTypeConverterFactory);
        ((Set<TypeConverterFactory<?, ?>>) registryEntries.get(TypeConverterFactoryLookup.TYPE_CONVERTER_FACTORY_REGISTRY_KEY)).add(highPriorityTypeConverterFactory);
        ((Set<TypeConverterFactory<?, ?>>) registryEntries.get(TypeConverterFactoryLookup.TYPE_CONVERTER_FACTORY_REGISTRY_KEY)).add(mediumPriorityTypeConverterFactory);

        SourceTargetTypeConverterFactoryLookup<String, Integer> sourceTargetTypeConverterFactoryLookup = new SourceTargetTypeConverterFactoryLookup<>(String.class, Integer.class);
        TypeConverter<? super String, ? extends Integer> typeConverter = sourceTargetTypeConverterFactoryLookup.apply(registryEntries).createTypeConverter();
        assertNotNull(typeConverter.convert("1"));
    }
}
