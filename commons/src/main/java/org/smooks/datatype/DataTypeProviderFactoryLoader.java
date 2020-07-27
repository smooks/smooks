/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
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
package org.smooks.datatype;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.datatype.factory.DataTypeProviderFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class DataTypeProviderFactoryLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataTypeProviderFactoryLoader.class);
    private static DataTypeProviderFactoryLoader instance;
    
    private Map<DataTypeProviderFactoryKey<?, ?>, DataTypeProviderFactory<?, ?>> systemDataTypeProviderFactories = new HashMap<>();

    private static class DataTypeProviderFactoryKey<S extends Type, T extends Type> {

        private final S sourceType;
        private final T targetType;

        public DataTypeProviderFactoryKey(final S sourceType, final T targetType) {
            this.sourceType = sourceType;
            this.targetType = targetType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DataTypeProviderFactoryKey)) {
                return false;
            }
            final DataTypeProviderFactoryKey<?, ?> that = (DataTypeProviderFactoryKey<?, ?>) o;
            return Objects.equals(sourceType, that.sourceType) &&
                    Objects.equals(targetType, that.targetType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourceType, targetType);
        }
    }
    
    private DataTypeProviderFactoryLoader() {
        final Iterator<DataTypeProviderFactory> dataTypeProviderFactoryIterator = ServiceLoader.load(DataTypeProviderFactory.class).iterator();

        while (dataTypeProviderFactoryIterator.hasNext()) {
            final DataTypeProviderFactory<?, ?> dataTypeProviderFactory = dataTypeProviderFactoryIterator.next();
            assertDataTypeProviderFactoryInterface(dataTypeProviderFactory);
            
            final Type sourceType = ((ParameterizedType) dataTypeProviderFactory.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
            final Type targetType = ((ParameterizedType) dataTypeProviderFactory.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[1];

            final DataTypeProviderFactoryKey dataTypeProviderFactoryKey = new DataTypeProviderFactoryKey(sourceType, targetType);
            if (systemDataTypeProviderFactories.containsKey(dataTypeProviderFactoryKey)) {
                LOGGER.warn("More than one DataTypeProvider for type '" + targetType.getTypeName() + "' is installed on the classpath.  You must manually configure decoding of this type, where required.");
                systemDataTypeProviderFactories.put(dataTypeProviderFactoryKey , null); // We don't remove, because we need to maintain a record of this!

            } else {
                systemDataTypeProviderFactories.put(dataTypeProviderFactoryKey, dataTypeProviderFactory);
            }
        }
    }

    public <S, T> DataTypeProviderFactory<S, T> get(final Class<S> sourceDataType, final Class<T> targetDataType) {
        final DataTypeProviderFactoryKey<?, ?> dataTypeProviderFactoryKey = new DataTypeProviderFactoryKey<>(sourceDataType, targetDataType);
        final DataTypeProviderFactory<?, ?> systemDataTypeProviderFactory = systemDataTypeProviderFactories.get(dataTypeProviderFactoryKey);
        return systemDataTypeProviderFactory == null ? (DataTypeProviderFactory<S, T>) systemDataTypeProviderFactories.get(new DataTypeProviderFactoryKey<>(Object.class, Object.class)) : (DataTypeProviderFactory<S, T>) systemDataTypeProviderFactory;
    }
    
    private void assertDataTypeProviderFactoryInterface(DataTypeProviderFactory<? , ?> dataTypeProviderFactory) {
        Type[] genericInterfaces = dataTypeProviderFactory.getClass().getGenericInterfaces();
        boolean assertion = false;
        if (genericInterfaces.length >= 1) {
            for (Type type : Arrays.asList(genericInterfaces)) {
                if ((type instanceof ParameterizedType) && ((ParameterizedType) type).getRawType().equals(DataTypeProviderFactory.class)) {
                    assertion = true;
                }
            }
            
            if (!assertion) {
                throw new SmooksConfigurationException(String.format("%s must immediately extend org.smooks.datatype.factory.DataTypeProviderFactory", dataTypeProviderFactory.getClass()));
            }
        } else {
            throw new SmooksConfigurationException(String.format("%s must extend org.smooks.datatype.factory.DataTypeProviderFactory", dataTypeProviderFactory.getClass()));
        }
    }
    
    public static DataTypeProviderFactoryLoader getInstance() {
        if (instance == null) {
            synchronized (DataTypeProviderFactoryLoader.class) {
                if (instance == null) {
                    instance = new DataTypeProviderFactoryLoader();
                }
            }
        }
        
        return instance;
    }
}
