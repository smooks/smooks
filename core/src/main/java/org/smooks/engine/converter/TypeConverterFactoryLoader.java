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
package org.smooks.engine.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.SmooksConfigException;
import org.smooks.api.converter.TypeConverterDescriptor;
import org.smooks.api.converter.TypeConverterFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

public class TypeConverterFactoryLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(TypeConverterFactoryLoader.class);

    public Set<TypeConverterFactory<?, ?>> load(ClassLoader classLoader) {
        final Iterator<TypeConverterFactory> typeConverterFactoryIterator = ServiceLoader.load(TypeConverterFactory.class, classLoader).iterator();
        final Set<TypeConverterFactory<?, ?>> typeConverterFactories = new HashSet<>();

        while (typeConverterFactoryIterator.hasNext()) {
            final TypeConverterFactory<?, ?> typeConverterFactory = typeConverterFactoryIterator.next();
            assertTypeConverterFactoryInterface(typeConverterFactory);

            final Type sourceType = ((ParameterizedType) typeConverterFactory.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
            final Type targetType = ((ParameterizedType) typeConverterFactory.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[1];

            final TypeConverterDescriptor<?, ?> typeConverterDescriptor;
 
            if (typeConverterFactories.contains(typeConverterFactory.getTypeConverterDescriptor())) {
                LOGGER.warn("More than one TypeConverter for type '" + targetType.getTypeName() + "' is installed on the classpath.  You must manually configure decoding of this type, where required.");
                typeConverterFactories.add(null); // We don't remove, because we need to maintain a record of this!
            } else {
                typeConverterFactories.add(typeConverterFactory);
            }
        }
        
        return typeConverterFactories;
    }
    
    private void assertTypeConverterFactoryInterface(TypeConverterFactory<? , ?> typeConverterFactory) {
        Type[] genericInterfaces = typeConverterFactory.getClass().getGenericInterfaces();
        boolean assertion = false;
        if (genericInterfaces.length >= 1) {
            for (Type type : genericInterfaces) {
                if ((type instanceof ParameterizedType) && ((ParameterizedType) type).getRawType().equals(TypeConverterFactory.class)) {
                    assertion = true;
                }
            }
            
            if (!assertion) {
                throw new SmooksConfigException(String.format("%s must immediately extend org.smooks.converter.factory.TypeConverterFactory", typeConverterFactory.getClass()));
            }
        } else {
            throw new SmooksConfigException(String.format("%s must extend org.smooks.converter.factory.TypeConverterFactory", typeConverterFactory.getClass()));
        }
    }
}
