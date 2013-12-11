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

package org.milyn.commons.javabean.decoders;

import junit.framework.TestCase;
import org.milyn.commons.javabean.DataDecodeException;

import java.util.Locale;
import java.util.Properties;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class LongDecoderTest extends TestCase {

    private Locale defaultLocale;

    public void setUp() {
        defaultLocale = Locale.getDefault();
        Locale.setDefault(new Locale("en", "IE"));
    }

    protected void tearDown() throws Exception {
        Locale.setDefault(defaultLocale);
    }

    public void test_empty_ok_value() {
        LongDecoder decoder = new LongDecoder();
        assertEquals(new Long(1), decoder.decode("1"));
    }

    public void test_empty_data_string() {
        LongDecoder decoder = new LongDecoder();
        try {
            decoder.decode("");
            fail("Expected DataDecodeException");
        } catch (DataDecodeException e) {
            assertEquals("Failed to decode Long value ''.", e.getMessage());
        }
    }

    public void test_decode_locale_config() {
        LongDecoder decoder = new LongDecoder();
        Properties config = new Properties();

        config.setProperty(LongDecoder.LOCALE, "de-DE");
        decoder.setConfiguration(config);

        Long longVal = (Long) decoder.decode("1234,45");   // comma for decimal point
        assertEquals(new Long(1234), longVal);
    }

    public void test_decode_format_config_01() {
        LongDecoder decoder = new LongDecoder();
        Properties config = new Properties();

        config.setProperty(LongDecoder.FORMAT, "#,###.##");
        decoder.setConfiguration(config);

        Long longVal = (Long) decoder.decode("1,234.45");
        assertEquals(new Long(1234), longVal);
    }

    public void test_decode_format_config_02() {
        LongDecoder decoder = new LongDecoder();
        Properties config = new Properties();

        config.setProperty(LongDecoder.FORMAT, "#%");
        decoder.setConfiguration(config);

        Long longVal = (Long) decoder.decode("30%");
        assertEquals(new Long(30), longVal);
    }

    public void test_encode_format_config() {
        LongDecoder decoder = new LongDecoder();
        Properties config = new Properties();

        config.setProperty(LongDecoder.FORMAT, "#,###.##");
        decoder.setConfiguration(config);

        assertEquals("1,234", decoder.encode(1234L));
    }

    public void test_encode_locale_config() {
        LongDecoder decoder = new LongDecoder();
        Properties config = new Properties();

        config.setProperty(LongDecoder.LOCALE, "de-DE");
        decoder.setConfiguration(config);

        assertEquals("1234", decoder.encode(1234L));
    }
}