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

import org.junit.Test;
import static org.junit.Assert.*;
import org.smooks.javabean.DataDecodeException;

import java.util.Properties;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MappingDecoderTest {

	@Test
    public void test() {
        MappingDecoder decoder = new MappingDecoder();
        Properties config = new Properties();

        config.setProperty("a", "avalue");
        config.setProperty("b", "bvalue");
        decoder.setConfiguration(config);

        assertEquals("avalue", decoder.decode("a"));
        assertEquals("bvalue", decoder.decode("b"));

        try {
            decoder.decode("blah");
            fail("Expected DataDecodeException.");
        } catch(DataDecodeException e) {
            assertEquals("Mapping <param> for data 'blah' not defined.", e.getMessage());
        }
    }
}
