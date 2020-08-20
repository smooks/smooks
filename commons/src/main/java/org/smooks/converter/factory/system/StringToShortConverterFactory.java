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

import org.smooks.converter.TypeConverter;
import org.smooks.converter.TypeConverterDescriptor;
import org.smooks.converter.TypeConverterException;
import org.smooks.converter.factory.TypeConverterFactory;

import java.text.ParseException;

/**
 * Short Decoder
 * 
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class StringToShortConverterFactory implements TypeConverterFactory<String, Short> {
    
    @Override
    public TypeConverter<String, Short> createTypeConverter() {
        return new NumberTypeConverter<String, Short>() {
            @Override
            protected Short doConvert(String value) {
                if(numberFormat != null) {
                    try {
                        final Number number = numberFormat.parse(value.trim());

                        if(isPercentage()) {
                            return (short) (number.doubleValue() * 100);
                        } else {
                            return number.shortValue();
                        }
                    } catch (ParseException e) {
                        throw new TypeConverterException("Failed to decode Short value '" + value + "' using NumberFormat instance " + numberFormat + ".", e);
                    }
                } else {
                    try {
                        return Short.parseShort(value.trim());
                    } catch(NumberFormatException e) {
                        throw new TypeConverterException("Failed to decode Short value '" + value + "'.", e);
                    }
                }
            }
        };
    }

    @Override
    public TypeConverterDescriptor<Class<String>, Class<Short>> getTypeConverterDescriptor() {
        return new TypeConverterDescriptor<>(String.class, Short.class);
    }
}
