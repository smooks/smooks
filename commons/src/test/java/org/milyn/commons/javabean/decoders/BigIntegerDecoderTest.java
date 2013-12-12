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

import java.math.BigInteger;
import java.util.Locale;
import java.util.Properties;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class BigIntegerDecoderTest extends TestCase {

    private Locale defaultLocale;

    public void setUp() {
        defaultLocale = Locale.getDefault();
		Locale.setDefault( new Locale("en", "IE") );
	}

    protected void tearDown() throws Exception {
        Locale.setDefault(defaultLocale);
    }

    public void test_decode_no_config() {
        BigIntegerDecoder decoder = new BigIntegerDecoder();

        BigInteger bigI = (BigInteger) decoder.decode("1234");
        assertEquals(new BigInteger("1234"), bigI);
    }

    public void test_decode_locale_config() {
        BigIntegerDecoder decoder = new BigIntegerDecoder();
        Properties config = new Properties();

        config.setProperty(BigIntegerDecoder.LOCALE, "de-DE");
        decoder.setConfiguration(config);

        BigInteger bigI = (BigInteger) decoder.decode("1234,45");
        assertEquals(new BigInteger("1234"), bigI);
    }

    public void test_decode_format_config() {
        BigIntegerDecoder decoder = new BigIntegerDecoder();
        Properties config = new Properties();

        config.setProperty(BigIntegerDecoder.FORMAT, "#,###.##");
        decoder.setConfiguration(config);

        BigInteger bigI = (BigInteger) decoder.decode("1,234.45");
        assertEquals(new BigInteger("1234"), bigI);
    }

    public void test_encode_format_config() {
        BigIntegerDecoder decoder = new BigIntegerDecoder();
        Properties config = new Properties();

        config.setProperty(BigIntegerDecoder.FORMAT, "#,###.##");
        decoder.setConfiguration(config);

        assertEquals("1,234", decoder.encode(new BigInteger("1234")));
    }

    public void test_encode_locale_config() {
        BigIntegerDecoder decoder = new BigIntegerDecoder();
        Properties config = new Properties();

        config.setProperty(BigIntegerDecoder.LOCALE, "de-DE");
        decoder.setConfiguration(config);

        assertEquals("1234", decoder.encode(new BigInteger("1234")));
    }
}