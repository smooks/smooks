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

import org.junit.Test;
import static org.junit.Assert.*;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.javabean.DataDecodeException;

import java.util.Properties;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class EnumDecoderTest {

	@Test
    public void test_bad_config() {
        EnumDecoder decoder = new EnumDecoder();
        Properties config = new Properties();

        // type not defined...
        try {
            decoder.setConfiguration(config);
            fail("Expected SmooksConfigurationException");
        } catch(SmooksConfigurationException e) {
            assertEquals("Invalid EnumDecoder configuration. 'enumType' param not specified.", e.getMessage());
        }

        // Not an enum...
        config.setProperty("enumType", String.class.getName());
        try {
            decoder.setConfiguration(config);
            fail("Expected SmooksConfigurationException");
        } catch(SmooksConfigurationException e) {
            assertEquals("Invalid Enum decoder configuration.  Resolved 'enumType' 'java.lang.String' is not a Java Enum Class.", e.getMessage());
        }
    }

	@Test
    public void test_good_config() {
        EnumDecoder decoder = new EnumDecoder();
        Properties config = new Properties();

        config.setProperty("enumType", MyEnum.class.getName());
        config.setProperty("val-A", "ValA");
        config.setProperty("val-B", "ValB");
        decoder.setConfiguration(config);

        assertEquals(MyEnum.ValA, decoder.decode("ValA"));
        assertEquals(MyEnum.ValA, decoder.decode("val-A"));
        assertEquals(MyEnum.ValB, decoder.decode("ValB"));
        assertEquals(MyEnum.ValB, decoder.decode("val-B"));

        try {
            decoder.decode("xxxxx");
            fail("Expected DataDecodeException");
        } catch(DataDecodeException e) {
            assertEquals("Failed to decode 'xxxxx' as a valid Enum constant of type 'org.milyn.javabean.decoders.MyEnum'.", e.getMessage());
        }
    }
}
