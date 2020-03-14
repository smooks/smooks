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

import org.smooks.javabean.DataDecodeException;

import java.util.Locale;
import java.util.Properties;

/**
 * Test for the ShortDecoder
 * 
 * @author <a href="mailto:daniel.bevenius@gmail.com">daniel.bevenius@gmail.com</a>
 */
public class ShortDecodingTest {

	private final ShortDecoder decoder = new ShortDecoder();
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
        assertEquals(new Short((short)1), decoder.decode("1"));
    }

    @Test
    public void test_empty_data_string() {
        try {
            decoder.decode("");
            fail("Expected DataDecodeException");
        } catch (DataDecodeException e) {
            assertEquals("Failed to decode Short value ''.", e.getMessage());
        }
    }

    @Test
    public void test_decode_locale_config() {
        ShortDecoder decoder = new ShortDecoder();
        Properties config = new Properties();

        config.setProperty(ShortDecoder.LOCALE, "de-DE");
        decoder.setConfiguration(config);

        Short shortVal = (Short) decoder.decode("1234,45");   // comma for decimal point
        assertEquals(new Short((short)1234), shortVal);
    }

    @Test
    public void test_decode_format_config() {
        ShortDecoder decoder = new ShortDecoder();
        Properties config = new Properties();

        config.setProperty(ShortDecoder.FORMAT, "#,###.##");
        decoder.setConfiguration(config);

        Short shortVal = (Short) decoder.decode("1,234.45");
        assertEquals(new Short((short)1234), shortVal);
    }

    @Test
    public void test_encode_format_config() {
        ShortDecoder decoder = new ShortDecoder();
        Properties config = new Properties();

        config.setProperty(ShortDecoder.FORMAT, "#,###.##");
        decoder.setConfiguration(config);

        assertEquals("1,234", decoder.encode((short)1234));
    }

    @Test
    public void test_encode_locale_config() {
        ShortDecoder decoder = new ShortDecoder();
        Properties config = new Properties();

        config.setProperty(ShortDecoder.LOCALE, "de-DE");
        decoder.setConfiguration(config);

        assertEquals("1234", decoder.encode((short)1234));
    }
}
