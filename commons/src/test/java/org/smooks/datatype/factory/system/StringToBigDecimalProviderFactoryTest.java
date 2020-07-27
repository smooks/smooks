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
package org.smooks.datatype.factory.system;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class StringToBigDecimalProviderFactoryTest {

    private Locale defaultLocale;

    @Before
    public void setUp() {
        defaultLocale = Locale.getDefault();
		Locale.setDefault( new Locale("en", "IE") );
	}

    @After
    public void tearDown() {
        Locale.setDefault(defaultLocale);
    }

    @Test
    public void test_decode_no_config() {
        StringToBigDecimalProviderFactory bigDecimalProviderFactory = new StringToBigDecimalProviderFactory();

        BigDecimal bigD = bigDecimalProviderFactory.createProvider("1234.45").get();
        assertEquals(new BigDecimal("1234.45"), bigD);
    }

    @Test
    public void test_decode_locale_config() {
        StringToBigDecimalProviderFactory bigDecimalProviderFactory = new StringToBigDecimalProviderFactory();
        Properties config = new Properties();

        config.setProperty(NumberProviderFactory.LOCALE, "de-DE");
        bigDecimalProviderFactory.setConfiguration(config);

        BigDecimal bigD = bigDecimalProviderFactory.createProvider("1234,45").get();   // comma for decimal point
        assertEquals(new BigDecimal("1234.45").toBigInteger(), bigD.toBigInteger());
    }

    @Test
    public void test_decode_format_config() {
        StringToBigDecimalProviderFactory bigDecimalProviderFactory = new StringToBigDecimalProviderFactory();
        Properties config = new Properties();

        config.setProperty(NumberProviderFactory.FORMAT, "#,###.##");
        bigDecimalProviderFactory.setConfiguration(config);

        BigDecimal bigD = bigDecimalProviderFactory.createProvider("1,234.45").get();
        assertEquals(new BigDecimal("1234.45").toBigInteger(), bigD.toBigInteger());
    }
}
