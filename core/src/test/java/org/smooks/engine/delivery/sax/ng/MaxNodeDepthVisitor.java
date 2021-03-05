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
package org.smooks.engine.delivery.sax.ng;

import org.smooks.api.SmooksException;
import org.smooks.api.ExecutionContext;
import org.smooks.api.resource.visitor.sax.ng.ParameterizedVisitor;
import org.w3c.dom.Element;

import static org.junit.Assert.assertEquals;

public class MaxNodeDepthVisitor implements ParameterizedVisitor {
    public static Element element;

    @Override
    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
    }

    @Override
    public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
        assertEquals("a", element.getNodeName());
        assertEquals(1, element.getChildNodes().getLength());
        assertEquals("b", element.getFirstChild().getNodeName());
        assertEquals(2, element.getFirstChild().getChildNodes().getLength());
        assertEquals("c", element.getFirstChild().getFirstChild().getNodeName());
        assertEquals("e", element.getFirstChild().getLastChild().getNodeName());
        assertEquals(1, element.getFirstChild().getLastChild().getChildNodes().getLength());
        assertEquals("f", element.getFirstChild().getLastChild().getFirstChild().getNodeName());
        assertEquals(1, element.getFirstChild().getLastChild().getFirstChild().getChildNodes().getLength());
        assertEquals("g", element.getFirstChild().getLastChild().getFirstChild().getFirstChild().getNodeName());
        assertEquals(0, element.getFirstChild().getLastChild().getFirstChild().getFirstChild().getChildNodes().getLength());
        assertEquals(1, element.getFirstChild().getFirstChild().getChildNodes().getLength());
        assertEquals("d", element.getFirstChild().getFirstChild().getFirstChild().getNodeName());
        assertEquals(1, element.getFirstChild().getFirstChild().getFirstChild().getChildNodes().getLength());
        assertEquals("foo", element.getFirstChild().getFirstChild().getFirstChild().getTextContent());
       
        MaxNodeDepthVisitor.element = element;
    }

    @Override
    public int getMaxNodeDepth() {
        return 5;
    }
}
