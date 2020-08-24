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
package org.smooks.delivery.dom.serialize;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;

import org.xml.sax.SAXException;
import org.smooks.Smooks;
import org.smooks.payload.StringResult;
import org.smooks.payload.StringSource;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DefaultSerializationTest {


	@Test
    public void test_default_writing_off_no_serializers() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("DefaultWritingOff_No_Serializers_Test.xml"));

        StringSource stringSource = new StringSource("<a>aa<b>bbb<c />bbb</b>aaa</a>");
        StringResult stringResult = new StringResult();

        smooks.filterSource(smooks.createExecutionContext(), stringSource, stringResult);

        // The "default.serialization.on" global param is set to "false" in the config, so
        // nothing should get writen to the result because there are no configured
        // serialization Visitors.
        assertEquals("", stringResult.getResult());

        assertTrue(SimpleDOMVisitor.visited);
    }

	@Test
    public void test_default_writing_off_one_serializer() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("DefaultWritingOff_One_Serializer_Test.xml"));

        StringSource stringSource = new StringSource("<a>aa<b>bbb<c />bbb</b>aaa</a>");
        StringResult stringResult = new StringResult();

        smooks.filterSource(smooks.createExecutionContext(), stringSource, stringResult);

        // The "default.serialization.on" global param is set to "false" in the config.
        // There's just a single result writing visitor configured on the "b" element...
        assertEquals("<b>bbbbbb</b>", stringResult.getResult());

        assertTrue(SimpleDOMVisitor.visited);
    }
}
