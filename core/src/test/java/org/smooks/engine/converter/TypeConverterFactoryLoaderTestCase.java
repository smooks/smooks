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

import org.junit.jupiter.api.Test;
import org.smooks.api.converter.TypeConverterDescriptor;
import org.smooks.api.converter.TypeConverterFactory;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class TypeConverterFactoryLoaderTestCase {

    private final Set<TypeConverterFactory<?, ?>> typeConverterFactories = new TypeConverterFactoryLoader().load(getClass().getClassLoader());

    @Test
    public void testLoad() {
        assertInstanceOf(StringConverterFactory.class, get(new DefaultTypeConverterDescriptor<>(String.class, String.class)));
        assertInstanceOf(StringToIntegerConverterFactory.class, get(new DefaultTypeConverterDescriptor<>(String.class, Integer.class)));
        assertInstanceOf(IntegerToStringConverterFactory.class, get(new DefaultTypeConverterDescriptor<>(Integer.class, String.class)));
        assertInstanceOf(StringToFloatConverterFactory.class, get(new DefaultTypeConverterDescriptor<>(String.class, Float.class)));
        assertInstanceOf(FloatToStringConverterFactory.class, get(new DefaultTypeConverterDescriptor<>(Float.class, String.class)));
        assertInstanceOf(StringToDoubleConverterFactory.class, get(new DefaultTypeConverterDescriptor<>(String.class, Double.class)));
        assertInstanceOf(DoubleToStringConverterFactory.class, get(new DefaultTypeConverterDescriptor<>(Double.class, String.class)));
        assertInstanceOf(CharacterConverterFactory.class, get(new DefaultTypeConverterDescriptor<>(String.class, Character.class)));
        assertInstanceOf(StringToBigDecimalConverterFactory.class, get(new DefaultTypeConverterDescriptor<>(String.class, BigDecimal.class)));
        assertInstanceOf(BigDecimalToStringConverterFactory.class, get(new DefaultTypeConverterDescriptor<>(BigDecimal.class, String.class)));
        assertInstanceOf(StringToDateConverterFactory.class, get(new DefaultTypeConverterDescriptor<>(String.class, Date.class)));
        assertInstanceOf(DateToStringConverterFactory.class, get(new DefaultTypeConverterDescriptor<>(Date.class, String.class)));
        assertInstanceOf(CalendarConverterFactory.class, get(new DefaultTypeConverterDescriptor<>(String.class, Calendar.class)));
        assertInstanceOf(CsvConverterFactory.class, get(new DefaultTypeConverterDescriptor<>(String.class, String[].class)));
        assertInstanceOf(CharsetConverterFactory.class, get(new DefaultTypeConverterDescriptor<>(String.class, Charset.class)));
        assertInstanceOf(FileConverterFactory.class, get(new DefaultTypeConverterDescriptor<>(String.class, File.class)));
        assertInstanceOf(ClassConverterFactory.class, get(new DefaultTypeConverterDescriptor<>(String.class, Class.class)));
    }
    
    private TypeConverterFactory<?, ?> get(TypeConverterDescriptor<?, ?> typeConverterDescriptor) {
        for (TypeConverterFactory<?, ?> typeConverterFactory : typeConverterFactories) {
            if (typeConverterFactory.getTypeConverterDescriptor().equals(typeConverterDescriptor)) {
                return typeConverterFactory;
            }
        }

        return null;
    }
}
