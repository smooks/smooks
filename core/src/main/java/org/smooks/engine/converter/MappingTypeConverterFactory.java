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

import org.smooks.api.SmooksConfigException;
import org.smooks.api.resource.config.Configurable;
import org.smooks.api.converter.TypeConverter;
import org.smooks.api.converter.TypeConverterDescriptor;
import org.smooks.api.converter.TypeConverterException;
import org.smooks.api.converter.TypeConverterFactory;

import jakarta.annotation.Resource;

import java.util.Properties;

@Resource(name = "Mapping")
public class MappingTypeConverterFactory implements TypeConverterFactory<String, String> {
    @Override
    public MappingTypeConverter createTypeConverter() {
        return new MappingTypeConverter();
    }

    @Override
    public TypeConverterDescriptor<Class<String>, Class<String>> getTypeConverterDescriptor() {
        return new DefaultTypeConverterDescriptor(String.class, String.class, (short) 0);
    }

    public static class MappingTypeConverter implements TypeConverter<String, String>, Configurable {

        private Properties properties;
        private boolean strict;

        @Override
        public void setConfiguration(Properties properties) throws SmooksConfigException {
            this.properties = properties;
            strict = properties.getProperty("strict", "true").equals("true");
        }

        @Override
        public Properties getConfiguration() {
            return properties;
        }

        @Override
        public String convert(final String value) {
            if (value != null) {
                final String mappingValue = properties.getProperty(value);

                if (mappingValue == null) {
                    if (strict) {
                        throw new TypeConverterException("Mapping <param> for data '" + value + "' not defined.");
                    } else {
                        return value;
                    }
                }

                return mappingValue;
            } else {
                return null;
            }
        }
    }
}
