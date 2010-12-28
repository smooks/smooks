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

package org.milyn.javabean.decoders;

import junit.framework.TestCase;
import org.milyn.javabean.DataDecodeException;

import java.util.Locale;
import java.util.Properties;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class FloatDecoderTest extends TestCase {

    private Locale defaultLocale;

    public void setUp() {
        defaultLocale = Locale.getDefault();
		Locale.setDefault( new Locale("en", "IE") );
	}

    protected void tearDown() throws Exception {
        Locale.setDefault(defaultLocale);
    }

    public void test_empty_ok_value() {
        FloatDecoder decoder = new FloatDecoder();
        assertEquals(new Float(1.0d), decoder.decode("1.0"));
    }

    public void test_empty_data_string() {
        FloatDecoder decoder = new FloatDecoder();
        try {
            decoder.decode("");
            fail("Expected DataDecodeException");
        } catch (DataDecodeException e) {
            assertEquals("Failed to decode float value ''.", e.getMessage());
        }
    }

    public void test_decode_locale_config() {
        FloatDecoder decoder = new FloatDecoder();
        Properties config = new Properties();

        config.setProperty(FloatDecoder.LOCALE, "de-DE");
        decoder.setConfiguration(config);

        Float floatVal = (Float) decoder.decode("1234,45");   // comma for decimal point
        assertEquals(1234.45f, floatVal);
    }

    public void test_decode_format_config() {
        FloatDecoder decoder = new FloatDecoder();
        Properties config = new Properties();

        config.setProperty(FloatDecoder.FORMAT, "#,###.##");
        decoder.setConfiguration(config);

        Float floatVal = (Float) decoder.decode("1,234.45");
        assertEquals(1234.45f, floatVal);
    }

    public void test_encode_format_config() {
        FloatDecoder decoder = new FloatDecoder();
        Properties config = new Properties();

        config.setProperty(FloatDecoder.FORMAT, "#,###.##");
        decoder.setConfiguration(config);

        assertEquals("1,234.45", decoder.encode(1234.45f));
    }

    public void test_encode_locale_config() {
        FloatDecoder decoder = new FloatDecoder();
        Properties config = new Properties();

        config.setProperty(FloatDecoder.LOCALE, "de-DE");
        decoder.setConfiguration(config);

        assertEquals("1234,45", decoder.encode(1234.45f));
    }
}
