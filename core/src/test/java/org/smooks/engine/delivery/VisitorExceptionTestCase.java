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
package org.smooks.engine.delivery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.smooks.Smooks;
import org.smooks.api.SmooksException;
import org.smooks.io.source.ReaderSource;
import org.smooks.io.source.StreamSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class VisitorExceptionTestCase {

	@BeforeEach
    public void setUp() throws Exception {
        ExceptionVisitor.beforeException = null;
        ExceptionVisitor.afterException = null;
    }

	@Test
    public void test_terminate_before() throws IOException, SAXException {
        ExceptionVisitor.beforeException = new SmooksException("Terminate Exception");
        test_exception("exception-config.xml", true);
        test_exception("exception-config-sax.xml", true);
    }

	@Test
    public void test_terminate_after() throws IOException, SAXException {
        ExceptionVisitor.afterException = new SmooksException("Terminate Exception");
        test_exception("exception-config.xml", true);
        test_exception("exception-config-sax.xml", true);
    }

	@Test
    public void test_no_terminate_before() throws IOException, SAXException {
        ExceptionVisitor.beforeException = new SmooksException("Terminate Exception");
        test_exception("no-exception-config.xml", false);
        test_exception("no-exception-config-sax.xml", false);
    }

	@Test
    public void test_no_terminate_after() throws IOException, SAXException {
        ExceptionVisitor.afterException = new SmooksException("Terminate Exception");
        test_exception("no-exception-config.xml", false);
        test_exception("no-exception-config-sax.xml", false);
    }

    private void test_exception(String config, boolean expectException) throws IOException, SAXException {
        Smooks smooks = new Smooks("/org/smooks/engine/delivery/" + config);

        if(expectException) {
            try {
                smooks.filterSource(smooks.createExecutionContext(), new ReaderSource(new StringReader("<doc/>")));
                fail("Expected SmooksException");
            } catch(SmooksException e) {
                assertEquals("Terminate Exception", e.getCause().getMessage());
            }
        } else {
            smooks.filterSource(smooks.createExecutionContext(), new ReaderSource(new StringReader("<doc/>")));
        }
    }
}
