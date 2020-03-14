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

import java.sql.Time;
import java.util.Properties;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the SqlTimeDecoder class
 * 
 * @author <a href="mailto:daniel.bevenius@gmail.com">daniel.bevenius@gmail.com</a>
 */
public class SqlTimeDecoderTest {

	@Test
    public void test_DateDecoder() {
        
        Properties config = new Properties();
        config.setProperty(CalendarDecoder.FORMAT, "EEE MMM dd HH:mm:ss z yyyy");
        
        SqlTimeDecoder decoder = new SqlTimeDecoder();
        config.setProperty(LocaleAwareDecoder.LOCALE_LANGUAGE_CODE, "en");
	    config.setProperty(LocaleAwareDecoder.LOCALE_COUNTRY_CODE, "IE");
        decoder.setConfiguration(config);

        Object object = decoder.decode("Wed Nov 15 13:45:28 EST 2006");
        assertTrue( object instanceof Time);
        
        Time time_a = (Time) decoder.decode("Wed Nov 15 13:45:28 EST 2006");
        assertEquals(1163616328000L, time_a.getTime());
        Time date_b = (Time) decoder.decode("Wed Nov 15 13:45:28 EST 2006");
        assertNotSame(time_a, date_b);
    }
    
}
