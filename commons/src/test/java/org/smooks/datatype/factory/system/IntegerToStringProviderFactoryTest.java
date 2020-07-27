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
import org.smooks.javabean.decoders.IntegerDecoder;

import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class IntegerToStringProviderFactoryTest {

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
    public void test_encode_format_config() {
        IntegerToStringProviderFactory integerToStringProviderFactory = new IntegerToStringProviderFactory();
        Properties config = new Properties();

        config.setProperty(IntegerDecoder.FORMAT, "#,###.##");
        integerToStringProviderFactory.setConfiguration(config);

        assertEquals("1,234", integerToStringProviderFactory.createProvider(1234).get());
    }

    @Test
    public void test_encode_locale_config() {
        IntegerToStringProviderFactory integerToStringProviderFactory = new IntegerToStringProviderFactory();
        Properties config = new Properties();

        config.setProperty(IntegerDecoder.LOCALE, "de-DE");
        integerToStringProviderFactory.setConfiguration(config);

        assertEquals("1234", integerToStringProviderFactory.createProvider(1234).get());
    }
}
