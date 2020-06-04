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
package org.smooks.javabean.decoders;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.smooks.javabean.DataDecodeException;

import java.util.Locale;
import java.util.Properties;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class LongDecoderTest {

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
        LongDecoder decoder = new LongDecoder();
        assertEquals(new Long(1), decoder.decode("1"));
    }

    @Test
    public void test_empty_data_string() {
        LongDecoder decoder = new LongDecoder();
        try {
            decoder.decode("");
            fail("Expected DataDecodeException");
        } catch (DataDecodeException e) {
            assertEquals("Failed to decode Long value ''.", e.getMessage());
        }
    }

    @Test
    public void test_decode_locale_config() {
        LongDecoder decoder = new LongDecoder();
        Properties config = new Properties();

        config.setProperty(LongDecoder.LOCALE, "de-DE");
        decoder.setConfiguration(config);

        Long longVal = (Long) decoder.decode("1234,45");   // comma for decimal point
        assertEquals(new Long(1234), longVal);
    }

    @Test
    public void test_decode_format_config_01() {
        LongDecoder decoder = new LongDecoder();
        Properties config = new Properties();

        config.setProperty(LongDecoder.FORMAT, "#,###.##");
        decoder.setConfiguration(config);

        Long longVal = (Long) decoder.decode("1,234.45");
        assertEquals(new Long(1234), longVal);
    }

    @Test
    public void test_decode_format_config_02() {
        LongDecoder decoder = new LongDecoder();
        Properties config = new Properties();

        config.setProperty(LongDecoder.FORMAT, "#%");
        decoder.setConfiguration(config);

        Long longVal = (Long) decoder.decode("30%");
        assertEquals(new Long(30), longVal);
    }

    @Test
    public void test_encode_format_config() {
        LongDecoder decoder = new LongDecoder();
        Properties config = new Properties();

        config.setProperty(LongDecoder.FORMAT, "#,###.##");
        decoder.setConfiguration(config);

        assertEquals("1,234", decoder.encode(1234L));
    }

    @Test
    public void test_encode_locale_config() {
        LongDecoder decoder = new LongDecoder();
        Properties config = new Properties();

        config.setProperty(LongDecoder.LOCALE, "de-DE");
        decoder.setConfiguration(config);

        assertEquals("1234", decoder.encode(1234L));
    }
}
