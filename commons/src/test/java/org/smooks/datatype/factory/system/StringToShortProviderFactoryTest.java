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
import org.smooks.javabean.DataDecodeException;
import org.smooks.javabean.decoders.ShortDecoder;

import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test for the ShortDecoder
 * 
 * @author <a href="mailto:daniel.bevenius@gmail.com">daniel.bevenius@gmail.com</a>
 */
public class StringToShortProviderFactoryTest {

	private final StringToShortProviderFactory stringToShortProviderFactory = new StringToShortProviderFactory();
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
    public void test_empty_ok_value() {
        assertEquals(new Short((short)1), stringToShortProviderFactory.createProvider("1").get());
    }

    @Test
    public void test_empty_data_string() {
        try {
            stringToShortProviderFactory.createProvider("").get();
            fail("Expected DataDecodeException");
        } catch (DataDecodeException e) {
            assertEquals("Failed to decode Short value ''.", e.getMessage());
        }
    }

    @Test
    public void test_decode_locale_config() {
        StringToShortProviderFactory stringToShortProviderFactory = new StringToShortProviderFactory();
        Properties config = new Properties();

        config.setProperty(ShortDecoder.LOCALE, "de-DE");
        stringToShortProviderFactory.setConfiguration(config);

        Short shortVal = stringToShortProviderFactory.createProvider("1234,45").get();   // comma for decimal point
        assertEquals(new Short((short)1234), shortVal);
    }

    @Test
    public void test_decode_format_config() {
        StringToShortProviderFactory stringToShortProviderFactory = new StringToShortProviderFactory();
        Properties config = new Properties();

        config.setProperty(ShortDecoder.FORMAT, "#,###.##");
        stringToShortProviderFactory.setConfiguration(config);

        Short shortVal = stringToShortProviderFactory.createProvider("1,234.45").get();
        assertEquals(new Short((short)1234), shortVal);
    }
}
