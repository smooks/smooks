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
package org.milyn.javabean.decoders;

import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for the Calendar and Date decoders.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DateDecoderTest {
    
    private Locale defaultLocale;

    @Test
    public void test_DateDecoder_01() {
        DateDecoder decoder = new DateDecoder();
        Properties config = new Properties();

        config.setProperty(DateDecoder.FORMAT, "EEE MMM dd HH:mm:ss z yyyy");
	    config.setProperty(LocaleAwareDecoder.LOCALE_LANGUAGE_CODE, "en");
	    config.setProperty(LocaleAwareDecoder.LOCALE_COUNTRY_CODE, "IE");
        decoder.setConfiguration(config);

        Date date_a = (Date) decoder.decode("Wed Nov 15 13:45:28 EST 2006");
        assertEquals(1163616328000L, date_a.getTime());
        Date date_b = (Date) decoder.decode("Wed Nov 15 13:45:28 EST 2006");
        assertNotSame(date_a, date_b);
    }

    @Test
    public void test_DateDecoder_02() {
        DateDecoder decoder = new DateDecoder();
        Properties config = new Properties();

        config.setProperty(DateDecoder.FORMAT, "EEE MMM dd HH:mm:ss z yyyy");
	    config.setProperty(LocaleAwareDecoder.LOCALE, "en-IE");
        decoder.setConfiguration(config);

        Date date_a = (Date) decoder.decode("Wed Nov 15 13:45:28 EST 2006");
        assertEquals(1163616328000L, date_a.getTime());
        Date date_b = (Date) decoder.decode("Wed Nov 15 13:45:28 EST 2006");
        assertNotSame(date_a, date_b);
    }

    @Before
    public void setUp() {
        defaultLocale = Locale.getDefault();
		Locale.setDefault( new Locale("de", "DE") );
	}

    @After
    public void tearDown() throws Exception {
        Locale.setDefault(defaultLocale);
    }
}
