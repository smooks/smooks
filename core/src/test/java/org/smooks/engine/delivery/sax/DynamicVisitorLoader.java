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
package org.smooks.engine.delivery.sax;

import org.smooks.api.SmooksException;
import org.smooks.api.ExecutionContext;
import org.smooks.api.delivery.sax.SAXElement;
import org.smooks.api.delivery.sax.SAXText;
import org.smooks.api.resource.visitor.sax.SAXElementVisitor;
import org.smooks.api.resource.visitor.sax.SAXVisitAfter;
import org.smooks.api.resource.visitor.sax.SAXVisitBefore;

import java.io.IOException;

/**
 * @author
 */
public class DynamicVisitorLoader implements SAXVisitBefore, SAXVisitAfter {

    public static final DynamicVisitor visitor = new DynamicVisitor();

    @Override
    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        visitor.stuff.setLength(0);
        DynamicSAXElementVisitorList.addDynamicVisitor(visitor, executionContext);
    }

    @Override
    public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        DynamicSAXElementVisitorList.removeDynamicVisitor(visitor, executionContext);
    }

    public static class DynamicVisitor implements SAXElementVisitor {

        public final StringBuilder stuff = new StringBuilder();

        @Override
        public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
            stuff.append("<").append(element.getName()).append(">");
        }

        @Override
        public void onChildText(SAXElement element, SAXText childText, ExecutionContext executionContext) throws SmooksException, IOException {
            stuff.append(childText.getText());
        }

        @Override
        public void onChildElement(SAXElement element, SAXElement childElement, ExecutionContext executionContext) throws SmooksException, IOException {
        }

        @Override
        public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
            stuff.append("</").append(element.getName()).append(">");
        }
    }
}
