/*-
 * ========================LICENSE_START=================================
 * Smooks Core
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
package org.smooks.cdr.xsd11.importtests;

import org.junit.Test;
import static org.junit.Assert.*;
import org.smooks.Smooks;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.delivery.dom.serialize.SimpleDOMVisitor;
import org.smooks.payload.StringSource;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ImportTest {

	@Test
    public void test_11_imports_10() throws IOException, SAXException {
        testConfig("11_import_10.xml");
    }

	@Test
    public void test_10_imports_11() throws IOException, SAXException {
        try {
            testConfig("10_import_11.xml");
            fail("Expected SmooksConfigurationException.");
        } catch(SmooksConfigurationException e) {
            assertEquals("Unsupported import of a v1.1 configuration from inside a v1.0 configuration.  Path to configuration: '/[root-config]/[11Config.xml]'.", e.getMessage());
        }
    }

	@Test
    public void test_paramaterized_import() throws IOException, SAXException {
        SimpleDOMVisitor.visited = false;
        testConfig("paramaterized_import_main.xml");
        assertTrue("Parameters not properly injected into import.", SimpleDOMVisitor.visited);
    }

    private void testConfig(String config) throws IOException, SAXException {
        Smooks smooks = new Smooks("/org/smooks/cdr/xsd11/importtests/" + config);
        smooks.filterSource(new StringSource("<a/>"), null);
    }
}
