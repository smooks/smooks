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
package org.smooks.converter.factory.system;

import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.config.Configurable;
import org.smooks.converter.TypeConverter;
import org.smooks.converter.TypeConverterDescriptor;
import org.smooks.converter.TypeConverterException;
import org.smooks.converter.factory.TypeConverterFactory;
import org.smooks.util.ClassUtil;

import javax.annotation.Resource;
import java.util.Properties;

@Resource(name = "Enum")
public class EnumTypeConverterFactory implements TypeConverterFactory<String, Enum> {

    @Override
    public TypeConverter<String, Enum> createTypeConverter() {
        return new EnumTypeConverter();
    }

    @Override
    public TypeConverterDescriptor<Class<String>, Class<Enum>> getTypeConverterDescriptor() {
        return new TypeConverterDescriptor(String.class, Enum.class);
    }
    
    public static class EnumTypeConverter implements TypeConverter<String, Enum>, Configurable {
        private final MappingTypeConverterFactory.MappingTypeConverter mappingTypeConverter = new MappingTypeConverterFactory().createTypeConverter();

        private Properties properties;
        private Class enumType;
        private boolean strict = true;

        @Override
        public Enum<?> convert(final String value) {
            final String mappedValue = mappingTypeConverter.convert(value);

            try {
                return Enum.valueOf(enumType, mappedValue.trim());
            } catch (IllegalArgumentException e) {
                if (strict) {
                    throw new TypeConverterException("Failed to convert '" + mappedValue + "' as a valid Enum constant of type '" + enumType.getName() + "'.");
                } else {
                    return null;
                }
            }
        }

        @Override
        public void setConfiguration(final Properties properties) throws SmooksConfigurationException {
            this.properties = properties;
            final String enumTypeName = properties.getProperty("enumType");

            if (enumTypeName == null || enumTypeName.trim().equals("")) {
                throw new SmooksConfigurationException("Invalid EnumDecoder configuration. 'enumType' param not specified.");
            }

            try {
                enumType = ClassUtil.forName(enumTypeName.trim(), EnumTypeConverter.class);
            } catch (ClassNotFoundException e) {
                throw new SmooksConfigurationException("Invalid Enum decoder configuration.  Failed to resolve '" + enumTypeName + "' as a Java Enum Class.", e);
            }

            if (!Enum.class.isAssignableFrom(enumType)) {
                throw new SmooksConfigurationException("Invalid Enum decoder configuration.  Resolved 'enumType' '" + enumTypeName + "' is not a Java Enum Class.");
            }
            
            strict = properties.getProperty("strict", "true").equals("true");
            mappingTypeConverter.setConfiguration(properties);
        }

        @Override
        public Properties getConfiguration() {
            return properties;
        }
    }
}
