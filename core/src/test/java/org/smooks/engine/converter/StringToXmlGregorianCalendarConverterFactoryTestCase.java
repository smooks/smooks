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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smooks.api.resource.config.Configurable;
import org.smooks.api.converter.TypeConverter;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class StringToXmlGregorianCalendarConverterFactoryTestCase {

    private Locale defaultLocale;

    @Test
    public void test_DateDecoder_01() {
        StringToXmlGregorianCalendarConverterFactory stringToXmlGregorianCalendarConverterFactory = new StringToXmlGregorianCalendarConverterFactory();
        Properties config = new Properties();

        config.setProperty(NumberTypeConverter.FORMAT, "EEE MMM dd HH:mm:ss z yyyy");
	    config.setProperty(NumberTypeConverter.LOCALE_LANGUAGE_CODE, "en");
	    config.setProperty(NumberTypeConverter.LOCALE_COUNTRY_CODE, "IE");
        TypeConverter<String, XMLGregorianCalendar> typeConverter = stringToXmlGregorianCalendarConverterFactory.createTypeConverter();
        ((Configurable) typeConverter).setConfiguration(config);

        Date date_a = typeConverter.convert("Wed Nov 15 13:45:28 EST 2006").toGregorianCalendar().getTime();
        assertEquals(1163616328000L, date_a.getTime());
        Date date_b = typeConverter.convert("Wed Nov 15 13:45:28 EST 2006").toGregorianCalendar().getTime();
        assertNotSame(date_a, date_b);
    }

    @Test
    public void test_DateDecoder_02() {
        StringToXmlGregorianCalendarConverterFactory stringToXmlGregorianCalendarConverterFactory = new StringToXmlGregorianCalendarConverterFactory();
        Properties config = new Properties();

        config.setProperty(NumberTypeConverter.FORMAT, "EEE MMM dd HH:mm:ss z yyyy");
	    config.setProperty(NumberTypeConverter.LOCALE, "en-IE");
        TypeConverter<String, XMLGregorianCalendar> typeConverter = stringToXmlGregorianCalendarConverterFactory.createTypeConverter();
        ((Configurable) typeConverter).setConfiguration(config);
        
        Date date_a = typeConverter.convert("Wed Nov 15 13:45:28 EST 2006").toGregorianCalendar().getTime();
        assertEquals(1163616328000L, date_a.getTime());
        Date date_b = typeConverter.convert("Wed Nov 15 13:45:28 EST 2006").toGregorianCalendar().getTime();
        assertNotSame(date_a, date_b);
    }

    @BeforeEach
    public void setUp() {
        defaultLocale = Locale.getDefault();
		Locale.setDefault( new Locale("de", "DE") );
	}

    @AfterEach
    public void tearDown() throws Exception {
        Locale.setDefault(defaultLocale);
    }
}
