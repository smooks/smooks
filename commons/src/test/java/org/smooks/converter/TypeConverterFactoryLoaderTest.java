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
package org.smooks.converter;

import org.junit.Test;
import org.smooks.converter.factory.TypeConverterFactory;
import org.smooks.converter.factory.system.*;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class TypeConverterFactoryLoaderTest {

    private final Set<TypeConverterFactory<?, ?>> typeConverterFactories = new TypeConverterFactoryLoader().load();

    @Test
    public void testLoad() {

        assertTrue(get(new TypeConverterDescriptor<>(String.class, String.class)) instanceof StringConverterFactory);
        assertTrue(get(new TypeConverterDescriptor<>(String.class, Integer.class)) instanceof StringToIntegerConverterFactory);
        assertTrue(get(new TypeConverterDescriptor<>(Integer.class, String.class)) instanceof IntegerToStringConverterFactory);
        assertTrue(get(new TypeConverterDescriptor<>(String.class, Float.class)) instanceof StringToFloatConverterFactory);
        assertTrue(get(new TypeConverterDescriptor<>(Float.class, String.class)) instanceof FloatToStringConverterFactory);
        assertTrue(get(new TypeConverterDescriptor<>(String.class, Double.class)) instanceof StringToDoubleConverterFactory);
        assertTrue(get(new TypeConverterDescriptor<>(Double.class, String.class)) instanceof DoubleToStringConverterFactory);
        assertTrue(get(new TypeConverterDescriptor<>(String.class, Character.class)) instanceof CharacterConverterFactory);
        assertTrue(get(new TypeConverterDescriptor<>(String.class, BigDecimal.class)) instanceof StringToBigDecimalConverterFactory);
        assertTrue(get(new TypeConverterDescriptor<>(BigDecimal.class, String.class)) instanceof BigDecimalToStringConverterFactory);
        assertTrue(get(new TypeConverterDescriptor<>(String.class, Date.class)) instanceof StringToDateConverterFactory);
        assertTrue(get(new TypeConverterDescriptor<>(Date.class, String.class)) instanceof DateToStringConverterFactory);
        assertTrue(get(new TypeConverterDescriptor<>(String.class, Calendar.class)) instanceof CalendarConverterFactory);
        assertTrue(get(new TypeConverterDescriptor<>(String.class, String[].class)) instanceof CsvConverterFactory);
        assertTrue(get(new TypeConverterDescriptor<>(String.class, Charset.class)) instanceof CharsetConverterFactory);
        assertTrue(get(new TypeConverterDescriptor<>(String.class, File.class)) instanceof FileConverterFactory);
        assertTrue(get(new TypeConverterDescriptor<>(String.class, Class.class)) instanceof ClassConverterFactory);
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
