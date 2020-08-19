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
package org.smooks.converter.factory.system;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.smooks.config.Configurable;
import org.smooks.converter.TypeConverter;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * Tests for the Calendar and Date decoders.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 * @author <a href="mailto:daniel.bevenius@gmail.com">daniel.bevenius@gmail.com</a>
 */
public class CalendarConverterFactoryTest {

	@Test
	public void test_CalendarDecoder() {
        Properties config = new Properties();
	    config.setProperty(DateLocaleAwareTypeConverter.FORMAT, "EEE MMM dd HH:mm:ss z yyyy");

		CalendarConverterFactory calendarConverterFactory = new CalendarConverterFactory();
	    config.setProperty(DateLocaleAwareTypeConverter.LOCALE_LANGUAGE_CODE, "en");
	    config.setProperty(DateLocaleAwareTypeConverter.LOCALE_COUNTRY_CODE, "IE");

		TypeConverter<String, Calendar> typeConverter = calendarConverterFactory.createTypeConverter();
		((Configurable) typeConverter).setConfiguration(config);

		Calendar cal_a = typeConverter.convert("Wed Nov 15 13:45:28 EST 2006");

		assertEquals(1163616328000L, cal_a.getTimeInMillis());
	    assertEquals("Eastern Standard Time", cal_a.getTimeZone().getDisplayName( new Locale("en", "IE") ) );
	    Calendar cal_b = typeConverter.convert("Wed Nov 15 13:45:28 EST 2006");
	    assertNotSame(cal_a, cal_b);
	}

	@Test
	public void test_CalendarDecoder_with_swedish_local() throws ParseException {
		final String dateFormat = "EEE MMM dd HH:mm:ss z yyyy";
		final String dateString = "ti mar 04 15:25:07 CET 2008";

        Properties config = new Properties();
	    CalendarConverterFactory calendarConverterFactory = new CalendarConverterFactory();

	    config.setProperty(DateLocaleAwareTypeConverter.FORMAT, dateFormat );
	    config.setProperty(DateLocaleAwareTypeConverter.LOCALE_LANGUAGE_CODE, "sv");
	    config.setProperty(DateLocaleAwareTypeConverter.LOCALE_COUNTRY_CODE, "SE");
	    config.setProperty(DateLocaleAwareTypeConverter.VERIFY_LOCALE, "true");

		TypeConverter<String, Calendar> typeConverter = calendarConverterFactory.createTypeConverter();
		((Configurable) typeConverter).setConfiguration(config);
		Calendar cal_a = typeConverter.convert(dateString);
	    assertEquals("Centraleuropeisk tid", cal_a.getTimeZone().getDisplayName( new Locale("sv", "SE") ) );

		Calendar cal_b = typeConverter.convert(dateString);
	    assertNotSame(cal_a, cal_b);
	}

    private Locale defaultLocale;
    private TimeZone defaultTimeZone;
    private String defaultEncoding;

    @Before
    public void setUp() {
        defaultEncoding = System.getProperty("file.encoding");
        System.setProperty("file.encoding", "UTF-8");

        defaultLocale = Locale.getDefault();
        Locale.setDefault( new Locale("de", "DE") );

        defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("ECT"));
	}

    @After
    public void tearDown() throws Exception {
        // Reset the defaults...
        System.setProperty("file.encoding",defaultEncoding);
        Locale.setDefault(defaultLocale);
        TimeZone.setDefault(defaultTimeZone);
    }
}

