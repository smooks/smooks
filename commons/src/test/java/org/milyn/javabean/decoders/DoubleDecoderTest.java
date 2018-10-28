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

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.milyn.javabean.DataDecodeException;

import java.util.Locale;
import java.util.Properties;

/**
 * Tests for the DoubleDecoder
 * 
 * @author <a href="mailto:daniel.bevenius@gmail.com">daniel.bevenius@gmail.com</a>
 */
public class DoubleDecoderTest {

	private DoubleDecoder decoder = new DoubleDecoder();
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
        assertEquals(new Double(1.0d), decoder.decode("1.0"));
    }

    @Test
    public void test_empty_data_string() {
        try {
            decoder.decode("");
            fail("Expected DataDecodeException");
        } catch (DataDecodeException e) {
            assertEquals("Failed to decode Double value ''.", e.getMessage());
        }
    }

    @Test
    public void test_decode_locale_config() {
        DoubleDecoder decoder = new DoubleDecoder();
        Properties config = new Properties();

        config.setProperty(DoubleDecoder.LOCALE, "de_DE");
        decoder.setConfiguration(config);

        Double doubleVal = (Double) decoder.decode("1234,45");   // comma for decimal point
        assertEquals(1234.45d, doubleVal.doubleValue(), 0d);
    }

    @Test
    public void test_decode_format_config_01() {
        DoubleDecoder decoder = new DoubleDecoder();
        Properties config = new Properties();

        config.setProperty(DoubleDecoder.FORMAT, "#,###.##");
        decoder.setConfiguration(config);

        Double doubleVal = (Double) decoder.decode("1,234.45");
        assertEquals(1234.45d, doubleVal.doubleValue(), 0d);
    }

    @Test
    public void test_decode_format_config_02() {
        DoubleDecoder decoder = new DoubleDecoder();
        Properties config = new Properties();

        config.setProperty(DoubleDecoder.FORMAT, "#%");
        decoder.setConfiguration(config);

        double doubleVal = (Double) decoder.decode("30%");
        assertEquals(0.3d, doubleVal, 0d);
    }

    @Test
    public void test_decode_format_config_03() {
        DoubleDecoder decoder = new DoubleDecoder();
        Properties config = new Properties();

        config.setProperty(DoubleDecoder.TYPE, NumberDecoder.NumberType.CURRENCY.toString());
        config.setProperty(DoubleDecoder.LOCALE, "en_US");
        decoder.setConfiguration(config);

        double doubleVal = (Double) decoder.decode("$29.99");
        assertEquals(29.99d, doubleVal, 0d);
    }

    @Test
    public void test_encode_format_config() {
        DoubleDecoder decoder = new DoubleDecoder();
        Properties config = new Properties();

        config.setProperty(DoubleDecoder.FORMAT, "#,###.##");
        decoder.setConfiguration(config);

        assertEquals("1,234.45", decoder.encode(1234.45d));
    }

    @Test
    public void test_encode_locale_config() {
        DoubleDecoder decoder = new DoubleDecoder();
        Properties config = new Properties();

        config.setProperty(DoubleDecoder.LOCALE, "de-DE");
        decoder.setConfiguration(config);

        assertEquals("1234,45", decoder.encode(1234.45d));
    }
}
