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
package org.smooks.JIRAs.MILYN_560;

import org.junit.jupiter.api.Test;
import org.smooks.Smooks;
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.api.delivery.sax.TextConsumer;
import org.smooks.api.resource.visitor.dom.DOMVisitAfter;
import org.smooks.api.resource.visitor.sax.ng.AfterVisitor;
import org.smooks.io.sink.StringSink;
import org.smooks.io.source.StringSource;
import org.w3c.dom.Element;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MILYN_560_TestCase {

	@Test
    public void test_DOM() {
        Smooks smooks = new Smooks();

        smooks.addVisitor((DOMVisitAfter) (element, executionContext) -> {
            assertEquals("&tomfennelly", element.getAttribute("attrib"));
            assertEquals("&tomfennelly", element.getTextContent());
        }, "element");

        StringSink serializedRes = new StringSink();
        smooks.filterSource(new StringSource("<element attrib=\"&amp;tomfennelly\">&amp;tomfennelly</element>"), serializedRes);

        assertEquals("<element attrib=\"&amp;tomfennelly\">&amp;tomfennelly</element>", serializedRes.getResult());
    }

	@Test
    public void test_SAX() {
        Smooks smooks = new Smooks();

        smooks.addVisitor(new MockSAX(), "element");

        StringSink serializedRes = new StringSink();
        smooks.filterSource(new StringSource("<element attrib=\"&amp;tomfennelly\">&amp;tomfennelly</element>"), serializedRes);

        assertEquals("<element attrib=\"&amp;tomfennelly\">&amp;tomfennelly</element>", serializedRes.getResult());
    }

    @TextConsumer
    private static class MockSAX implements AfterVisitor {
        public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
            assertEquals("&tomfennelly", element.getAttribute("attrib"));
            assertEquals("&tomfennelly", element.getTextContent());
        }
    }
}
