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
import org.smooks.api.resource.config.Configurable;
import org.smooks.api.converter.TypeConverter;

import java.sql.Time;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the SqlTimeDecoder class
 * 
 * @author <a href="mailto:daniel.bevenius@gmail.com">daniel.bevenius@gmail.com</a>
 */
public class SqlTimeConverterFactoryTestCase {

	@Test
    public void test_DateDecoder() {
        Properties config = new Properties();
        config.setProperty(DateToStringLocaleAwareConverter.FORMAT, "EEE MMM dd HH:mm:ss z yyyy");

        SqlTimeConverterFactory sqlTimeConverterFactory = new SqlTimeConverterFactory();
        config.setProperty(DateToStringLocaleAwareConverter.LOCALE_LANGUAGE_CODE, "en");
	    config.setProperty(DateToStringLocaleAwareConverter.LOCALE_COUNTRY_CODE, "IE");
        
	    TypeConverter<String, Time> typeConverter = sqlTimeConverterFactory.createTypeConverter();
        ((Configurable) typeConverter).setConfiguration(config);

        Object object = typeConverter.convert("Wed Nov 15 13:45:28 EST 2006");
        assertTrue(object instanceof Time);
        
        Time time_a = typeConverter.convert("Wed Nov 15 13:45:28 EST 2006");
        assertEquals(1163616328000L, time_a.getTime());
        Time date_b = typeConverter.convert("Wed Nov 15 13:45:28 EST 2006");
        assertNotSame(time_a, date_b);
    }
    
}
