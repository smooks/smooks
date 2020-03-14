/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License (version 2.1) as published by the Free Software
 *  Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  See the GNU Lesser General Public License for more details:
 *  http://www.gnu.org/licenses/lgpl.txt
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
public class IntegerDecoderTest {

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
        IntegerDecoder decoder = new IntegerDecoder();
        assertEquals(new Integer(1), decoder.decode("1"));
    }

    @Test
    public void test_empty_data_string() {
        IntegerDecoder decoder = new IntegerDecoder();
        try {
            decoder.decode("");
            fail("Expected DataDecodeException");
        } catch (DataDecodeException e) {
            assertEquals("Failed to decode Integer value ''.", e.getMessage());
        }
    }

    @Test
    public void test_decode_locale_config() {
        IntegerDecoder decoder = new IntegerDecoder();
        Properties config = new Properties();

        config.setProperty(IntegerDecoder.LOCALE, "de-DE");
        decoder.setConfiguration(config);

        Integer integerVal = (Integer) decoder.decode("1234,45");   // comma for decimal point
        assertEquals(new Integer(1234), integerVal);
    }

    @Test
    public void test_decode_format_config_01() {
        IntegerDecoder decoder = new IntegerDecoder();
        Properties config = new Properties();

        config.setProperty(IntegerDecoder.FORMAT, "#,###.##");
        decoder.setConfiguration(config);

        Integer integerVal = (Integer) decoder.decode("1,234.45");
        assertEquals(new Integer(1234), integerVal);
    }

    @Test
    public void test_decode_format_config_02() {
        IntegerDecoder decoder = new IntegerDecoder();
        Properties config = new Properties();

        config.setProperty(IntegerDecoder.FORMAT, "#%");
        decoder.setConfiguration(config);

        Integer integerVal = (Integer) decoder.decode("30%");
        assertEquals(new Integer(30), integerVal);
    }

    @Test
    public void test_encode_format_config() {
        IntegerDecoder decoder = new IntegerDecoder();
        Properties config = new Properties();

        config.setProperty(IntegerDecoder.FORMAT, "#,###.##");
        decoder.setConfiguration(config);

        assertEquals("1,234", decoder.encode(1234));
    }

    @Test
    public void test_encode_locale_config() {
        IntegerDecoder decoder = new IntegerDecoder();
        Properties config = new Properties();

        config.setProperty(IntegerDecoder.LOCALE, "de-DE");
        decoder.setConfiguration(config);

        assertEquals("1234", decoder.encode(1234));
    }
}
