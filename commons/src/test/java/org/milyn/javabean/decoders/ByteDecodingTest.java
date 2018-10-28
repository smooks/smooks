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

import org.milyn.javabean.DataDecodeException;


/**
 * Test for the ByteDecoder
 * 
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class ByteDecodingTest {
	private final ByteDecoder decoder = new ByteDecoder();

	@Test
    public void test_empty_ok_value() {
        assertEquals(new Byte((byte)1), decoder.decode("1"));
    }

	@Test
    public void test_empty_data_string() {
        try {
            decoder.decode("");
            fail("Expected DataDecodeException");
        } catch (DataDecodeException e) {
            assertEquals("Failed to decode Byte value ''.", e.getMessage());
        }
    }
}
