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
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.javabean.DataDecodeException;

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

    /**
     * DOCUMENT ME!
     */
    @Test public void test_good_config_strict_false() {
        final EnumDecoder decoder = new EnumDecoder();
        final Properties config = new Properties();

        config.setProperty("enumType", MyEnum.class.getName());
        config.setProperty("strict", "false");
        config.setProperty("val-A", "ValA");
        config.setProperty("val-B", "ValB");
        decoder.setConfiguration(config);

        assertEquals(MyEnum.ValA, decoder.decode("ValA"));
        assertEquals(MyEnum.ValA, decoder.decode("val-A"));
        assertEquals(MyEnum.ValB, decoder.decode("ValB"));
        assertEquals(MyEnum.ValB, decoder.decode("val-B"));
        assertNull(decoder.decode("xxxxx"));
    }

    /**
     * DOCUMENT ME!
     */
    @Test public void test_good_config_strict_true() {
        final EnumDecoder decoder = new EnumDecoder();
        final Properties config = new Properties();

        config.setProperty("enumType", MyEnum.class.getName());
        config.setProperty("strict", "true");
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
        } catch (DataDecodeException e) {
            assertEquals(
                    "Failed to decode 'xxxxx' as a valid Enum constant of type 'org.smooks.javabean.decoders.MyEnum'.",
                    e.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     */
    @Test public void test_good_config_strict_undefined() {
        final EnumDecoder decoder = new EnumDecoder();
        final Properties config = new Properties();

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
        } catch (DataDecodeException e) {
            assertEquals(
                    "Failed to decode 'xxxxx' as a valid Enum constant of type 'org.smooks.javabean.decoders.MyEnum'.",
                    e.getMessage());
        }
    }
}
