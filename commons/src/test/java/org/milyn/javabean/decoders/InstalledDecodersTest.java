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

import junit.framework.TestCase;
import org.milyn.javabean.DataDecodeException;
import org.milyn.javabean.DataDecoder;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class InstalledDecodersTest extends TestCase {

    public void test() {
        assertTrue(DataDecoder.Factory.create(String.class) instanceof StringDecoder);
        assertTrue(DataDecoder.Factory.create(Integer.class) instanceof IntegerDecoder);
        assertTrue(DataDecoder.Factory.create(int.class) instanceof IntegerDecoder);
        assertTrue(DataDecoder.Factory.create(Float.class) instanceof FloatDecoder);
        assertTrue(DataDecoder.Factory.create(float.class) instanceof FloatDecoder);
        assertTrue(DataDecoder.Factory.create(Double.class) instanceof DoubleDecoder);
        assertTrue(DataDecoder.Factory.create(double.class) instanceof DoubleDecoder);
        assertTrue(DataDecoder.Factory.create(Character.class) instanceof CharacterDecoder);
        assertTrue(DataDecoder.Factory.create(char.class) instanceof CharacterDecoder);
        assertTrue(DataDecoder.Factory.create(BigDecimal.class) instanceof BigDecimalDecoder);
        assertTrue(DataDecoder.Factory.create(Date.class) instanceof DateDecoder);
        assertTrue(DataDecoder.Factory.create(Calendar.class) instanceof CalendarDecoder);
        assertTrue(DataDecoder.Factory.create(String[].class) instanceof CSVDecoder);
        assertTrue(DataDecoder.Factory.create(Charset.class) instanceof CharsetDecoder);
        assertTrue(DataDecoder.Factory.create(File.class) instanceof FileDecoder);
        assertTrue(DataDecoder.Factory.create(Class.class) instanceof ClassDecoder);
        assertNull(DataDecoder.Factory.create(getClass()));
    }

    public void test_CSVDecoder() {
        String[] csvArray = (String[]) new CSVDecoder().decode("a,b,c");
        assertEquals(3, csvArray.length);
        assertTrue(Arrays.equals(new String[] {"a", "b", "c"}, csvArray));
    }

    public void test_CharsetDecoder() {
        // valid charset
        new CharsetDecoder().decode("UTF-8");
        try {
            // invalid charset
            new CharsetDecoder().decode("XXXXXX");
        } catch(DataDecodeException e) {
            assertEquals("Unsupported character set 'XXXXXX'.", e.getMessage());
        }
    }
}
