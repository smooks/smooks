/*-
 * ========================LICENSE_START=================================
 * Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
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
package org.smooks.engine.resource.xsd20.conditiontests;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.smooks.Smooks;
import org.smooks.api.SmooksConfigException;
import org.smooks.io.payload.StringSource;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Config20ConditionTestCase {

    /**
     * Normal conditions defined on the resource
     */
	@Test
    public void test_01() throws IOException, SAXException {
        testConfig("config01.xml", "[config1, config3]");
    }

    /**
     * One of the conditions defined globally and referenced from one of the resources
     */
	@Test
    public void test_02() throws IOException, SAXException {
        testConfig("config02.xml", "[config1, config3]");
    }

    /**
     * Application of a default condition ref
     */
	@Test
    public void test_03() throws IOException, SAXException {
        testConfig("config03.1.xml", "[config1, config3]"); // No default defined
        testConfig("config03.2.xml", "[config1]"); // Has a default defined
    }

    /**
     * config04.xml with an imported config04.1.xml config which inherits
     * conditions from config04.xml
     */
	@Test
    public void test_04() throws IOException, SAXException {
        testConfig("config04.xml", "[config1, config3]");
    }

    /**
     * config06.xml importing config06.1.xml and config06.2.xml. config06.1.xml inheriting
     * conditions from config06.xml and config06.2.xml redefining the same conditions locally.
     * So... this is basically a scoping test on the conditions...
     */
	@Test
    public void test_06() throws IOException, SAXException {
        testConfig("config06.xml", "[config1, config3, 2nd_config1, 2nd_config2]");
    }

    /**
     * Undefined condition ref
     */
	@Test
    public void test_05() throws IOException, SAXException {
        try {
            testConfig("config05.xml", "n/a");
            fail("Expected SmooksConfigurationException");
        } catch(SmooksConfigException e) {
            assertEquals("Unknown condition idRef 'config3'.", e.getMessage());
        }
    }

    /**
     * Duplicate condition ids
     */
	@Test
    public void test_07() throws IOException, SAXException {
        try {
            testConfig("config07.xml", "n/a");
            fail("Expected SmooksConfigurationException");
        } catch(SmooksConfigException e) {
            assertEquals("Duplicate condition ID 'config3'.", e.getMessage());
        }
    }

    private void testConfig(String config, String expected) throws IOException, SAXException {
        Smooks smooks = new Smooks("/org/smooks/engine/resource/xsd20/conditiontests/" + config);

        smooks.filterSource(new StringSource("<a/>"), null);
        assertEquals(expected, ConditionTestVisitor.messagesUsed.toString());
        ConditionTestVisitor.messagesUsed.clear();
    }
}
