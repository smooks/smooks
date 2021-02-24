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
package org.smooks.engine.converter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.smooks.api.resource.config.Configurable;
import org.smooks.api.converter.TypeConverter;

import java.math.BigInteger;
import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class StringToBigIntegerConverterFactoryTest {

    private Locale defaultLocale;

    @Before
    public void setUp() {
        defaultLocale = Locale.getDefault();
		Locale.setDefault( new Locale("en", "IE") );
	}

    @After
    public void tearDown() throws Exception {
        Locale.setDefault(defaultLocale);
    }

    @Test
    public void test_decode_no_config() {
        StringToBigIntegerConverterFactory stringToBigIntegerConverterFactory = new StringToBigIntegerConverterFactory();

        BigInteger bigI = stringToBigIntegerConverterFactory.createTypeConverter().convert("1234");
        assertEquals(new BigInteger("1234"), bigI);
    }

    @Test
    public void test_decode_locale_config() {
        StringToBigIntegerConverterFactory stringToBigIntegerConverterFactory = new StringToBigIntegerConverterFactory();
        Properties config = new Properties();

        config.setProperty(NumberTypeConverter.LOCALE, "de-DE");
        TypeConverter<String, BigInteger> typeConverter = stringToBigIntegerConverterFactory.createTypeConverter();
        ((Configurable) typeConverter).setConfiguration(config);
        
        BigInteger bigI = typeConverter.convert("1234,45");
        assertEquals(new BigInteger("1234"), bigI);
    }

    @Test
    public void test_decode_format_config() {
        StringToBigIntegerConverterFactory stringToBigIntegerConverterFactory = new StringToBigIntegerConverterFactory();
        Properties config = new Properties();

        config.setProperty(NumberTypeConverter.FORMAT, "#,###.##");
        TypeConverter<String, BigInteger> typeConverter = stringToBigIntegerConverterFactory.createTypeConverter();
        ((Configurable) typeConverter).setConfiguration(config);
        
        BigInteger bigI = typeConverter.convert("1,234.45");
        assertEquals(new BigInteger("1234"), bigI);
    }
}
