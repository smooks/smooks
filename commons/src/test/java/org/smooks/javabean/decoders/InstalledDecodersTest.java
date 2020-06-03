/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0 or,
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.javabean.decoders;

import org.junit.Test;
import static org.junit.Assert.*;
import org.smooks.javabean.DataDecodeException;
import org.smooks.javabean.DataDecoder;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class InstalledDecodersTest {

	@Test
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

	@Test
    public void test_CSVDecoder() {
        String[] csvArray = (String[]) new CSVDecoder().decode("a,b,c");
        assertEquals(3, csvArray.length);
        assertTrue(Arrays.equals(new String[] {"a", "b", "c"}, csvArray));
    }

	@Test
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
