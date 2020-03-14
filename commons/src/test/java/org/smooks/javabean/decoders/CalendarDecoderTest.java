/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package org.smooks.javabean.decoders;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Tests for the Calendar and Date decoders.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 * @author <a href="mailto:daniel.bevenius@gmail.com">daniel.bevenius@gmail.com</a>
 */
public class CalendarDecoderTest {

	@Test
	public void test_CalendarDecoder() {
        Properties config = new Properties();
	    config.setProperty(CalendarDecoder.FORMAT, "EEE MMM dd HH:mm:ss z yyyy");

	    CalendarDecoder decoder = new CalendarDecoder();
	    config.setProperty(LocaleAwareDecoder.LOCALE_LANGUAGE_CODE, "en");
	    config.setProperty(LocaleAwareDecoder.LOCALE_COUNTRY_CODE, "IE");
	    decoder.setConfiguration(config);

	    Calendar cal_a = (Calendar) decoder.decode("Wed Nov 15 13:45:28 EST 2006");
	    assertEquals(1163616328000L, cal_a.getTimeInMillis());
	    assertEquals("Eastern Standard Time", cal_a.getTimeZone().getDisplayName( new Locale("en", "IE") ) );
	    Calendar cal_b = (Calendar) decoder.decode("Wed Nov 15 13:45:28 EST 2006");
	    assertNotSame(cal_a, cal_b);
	}

	@Test
	public void test_CalendarDecoder_with_swedish_local() throws ParseException {
		final String dateFormat = "EEE MMM dd HH:mm:ss z yyyy";
		final String dateString = "ti mar 04 15:25:07 CET 2008";

        Properties config = new Properties();
	    CalendarDecoder decoder = new CalendarDecoder();

	    config.setProperty(CalendarDecoder.FORMAT, dateFormat );
	    config.setProperty(LocaleAwareDecoder.LOCALE_LANGUAGE_CODE, "sv");
	    config.setProperty(LocaleAwareDecoder.LOCALE_COUNTRY_CODE, "SE");
	    config.setProperty(LocaleAwareDecoder.VERIFY_LOCALE, "true");
	    decoder.setConfiguration(config);

	    Calendar cal_a = (Calendar) decoder.decode( dateString );
	    assertEquals("Centraleuropeisk tid", cal_a.getTimeZone().getDisplayName( new Locale("sv", "SE") ) );

	    Calendar cal_b = (Calendar) decoder.decode( dateString );
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

