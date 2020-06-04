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
package org.smooks.db;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.smooks.Smooks;
import org.smooks.SmooksException;
import org.smooks.payload.StringSource;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DatasourceCleanupTest {

    @Before
    public void setUp() throws Exception {
        MockDatasource.cleanupCallCount = 0;
    }

    @Test
    public void test_normal() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("normal-ds-lifecycle.xml"));

        // Cleanup should get called twice.  Once for the visitAfter event and once for the
        // executeExecutionLifecycleCleanup event...
        smooks.filterSource(new StringSource("<a></a>"));
        assertEquals(2, MockDatasource.cleanupCallCount);
        assertTrue(MockDatasource.committed);
    }

    @Test
    public void test_exception() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("exception-ds-lifecycle.xml"));

        try {
            smooks.filterSource(new StringSource("<a><b/><c/></a>"));
            fail("Expected exception...");
        } catch(SmooksException e) {
            // Expected
        }

        // Test that even after an exception is thrown, the DataSource cleanup takes place...
        // Cleanup should only get called once for the executeExecutionLifecycleCleanup event.
        // The visitAfter event doesn't call it because of the exception thrown by a nested
        // visitor...
        assertTrue(ExceptionVisitor.exceptionThrown);
        assertEquals(1, MockDatasource.cleanupCallCount);
        assertTrue(MockDatasource.rolledBack);
    }
}
