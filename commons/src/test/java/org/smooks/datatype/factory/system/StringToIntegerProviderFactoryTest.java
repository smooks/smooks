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
import org.smooks.javabean.decoders.IntegerDecoder;

import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class StringToIntegerProviderFactoryTest {

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
        StringToIntegerProviderFactory stringToIntegerProviderFactory = new StringToIntegerProviderFactory();
        assertEquals(new Integer(1), stringToIntegerProviderFactory.createProvider("1").get());
    }

    @Test
    public void test_empty_data_string() {
        StringToIntegerProviderFactory stringToIntegerProviderFactory = new StringToIntegerProviderFactory();
        try {
            stringToIntegerProviderFactory.createProvider("").get();
            fail("Expected DataDecodeException");
        } catch (DataDecodeException e) {
            assertEquals("Failed to decode Integer value ''.", e.getMessage());
        }
    }

    @Test
    public void test_decode_locale_config() {
        StringToIntegerProviderFactory stringToIntegerProviderFactory = new StringToIntegerProviderFactory();
        Properties config = new Properties();

        config.setProperty(IntegerDecoder.LOCALE, "de-DE");
        stringToIntegerProviderFactory.setConfiguration(config);

        Integer integerVal = (Integer) stringToIntegerProviderFactory.createProvider("1234,45").get();   // comma for decimal point
        assertEquals(new Integer(1234), integerVal);
    }

    @Test
    public void test_decode_format_config_01() {
        StringToIntegerProviderFactory stringToIntegerProviderFactory = new StringToIntegerProviderFactory();
        Properties config = new Properties();

        config.setProperty(IntegerDecoder.FORMAT, "#,###.##");
        stringToIntegerProviderFactory.setConfiguration(config);

        Integer integerVal = (Integer) stringToIntegerProviderFactory.createProvider("1,234.45").get();
        assertEquals(new Integer(1234), integerVal);
    }

    @Test
    public void test_decode_format_config_02() {
        StringToIntegerProviderFactory stringToIntegerProviderFactory = new StringToIntegerProviderFactory();
        Properties config = new Properties();

        config.setProperty(IntegerDecoder.FORMAT, "#%");
        stringToIntegerProviderFactory.setConfiguration(config);

        Integer integerVal = (Integer) stringToIntegerProviderFactory.createProvider("30%").get();
        assertEquals(new Integer(30), integerVal);
    }
}
