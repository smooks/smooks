/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 - 2024 Smooks
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

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.DOMWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.api.delivery.ContentHandlerBinding;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.api.resource.visitor.sax.ng.ElementVisitor;
import org.smooks.engine.delivery.interceptor.ExceptionInterceptor;
import org.smooks.engine.resource.config.DefaultResourceConfig;
import org.smooks.support.XmlUtils;
import org.smooks.testkit.MockApplicationContext;
import org.smooks.testkit.MockExecutionContext;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExceptionInterceptorTestCase {
    private Element elementUnderTest;
    private ExceptionInterceptor exceptionInterceptor;

    private final ContentHandlerBinding<Visitor> contentHandlerBinding = new DefaultContentHandlerBinding<>(new ElementVisitor() {
        @Override
        public void visitAfter(Element element, ExecutionContext executionContext) {
            throw new RuntimeException();
        }

        @Override
        public void visitBefore(Element element, ExecutionContext executionContext) {
            throw new RuntimeException();
        }

        @Override
        public void visitChildText(CharacterData characterData, ExecutionContext executionContext) {
            throw new RuntimeException();
        }

        @Override
        public void visitChildElement(Element childElement, ExecutionContext executionContext) {
            throw new RuntimeException();
        }
    }, new DefaultResourceConfig());

    @BeforeEach
    public void beforeEach() throws DocumentException {
        exceptionInterceptor = new ExceptionInterceptor();
        exceptionInterceptor.setApplicationContext(new MockApplicationContext());
        exceptionInterceptor.setVisitorBinding(contentHandlerBinding);
        exceptionInterceptor.postConstruct();

        Document doc = new DOMWriter().write(DocumentHelper.parseText("<a><b><c><d><e>foo</e></d></c></b></a>"));
        elementUnderTest = (Element) XmlUtils.getNode(doc, "a/b/c/d/e");
    }

    @Test
    public void testVisitBefore() {
        SmooksException smooksException = assertThrows(SmooksException.class, () -> exceptionInterceptor.visitBefore(elementUnderTest, new MockExecutionContext()));
        assertEquals("Error while processing start event\n" +
                "\n" +
                "Error Context\n" +
                "---------------------------------------------------------------------------------------------------------------------------------------\n" +
                "Event => /a/b/c/d/e\n" +
                "Selector => none\n" +
                "Content handler => org.smooks.engine.delivery.ExceptionInterceptorTestCase$1\n" +
                "---------------------------------------------------------------------------------------------------------------------------------------\n", smooksException.getMessage());
    }

    @Test
    public void testVisitAfter() {
        SmooksException smooksException = assertThrows(SmooksException.class, () -> exceptionInterceptor.visitAfter(elementUnderTest, new MockExecutionContext()));
        assertEquals("Error while processing end event\n" +
                "\n" +
                "Error Context\n" +
                "---------------------------------------------------------------------------------------------------------------------------------------\n" +
                "Event => /a/b/c/d/e\n" +
                "Selector => none\n" +
                "Content handler => org.smooks.engine.delivery.ExceptionInterceptorTestCase$1\n" +
                "---------------------------------------------------------------------------------------------------------------------------------------\n", smooksException.getMessage());
    }

    @Test
    public void testVisitChildEvent() {
        SmooksException smooksException = assertThrows(SmooksException.class, () -> exceptionInterceptor.visitChildElement(elementUnderTest, new MockExecutionContext()));
        assertEquals("Error while processing child event\n" +
                "\n" +
                "Error Context\n" +
                "---------------------------------------------------------------------------------------------------------------------------------------\n" +
                "Event => /a/b/c/d/e\n" +
                "Selector => none\n" +
                "Content handler => org.smooks.engine.delivery.ExceptionInterceptorTestCase$1\n" +
                "---------------------------------------------------------------------------------------------------------------------------------------\n", smooksException.getMessage());
    }

    @Test
    public void testVisitChildText() {
        SmooksException smooksException = assertThrows(SmooksException.class, () -> exceptionInterceptor.visitChildText((CharacterData) elementUnderTest.getFirstChild(), new MockExecutionContext()));
        assertEquals("Error while processing text event\n" +
                "\n" +
                "Error Context\n" +
                "---------------------------------------------------------------------------------------------------------------------------------------\n" +
                "Event => /a/b/c/d/e/#text\n" +
                "Selector => none\n" +
                "Content handler => org.smooks.engine.delivery.ExceptionInterceptorTestCase$1\n" +
                "---------------------------------------------------------------------------------------------------------------------------------------\n", smooksException.getMessage());
    }
}
