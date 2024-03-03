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
package org.smooks.engine.delivery.condition;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.smooks.Smooks;
import org.smooks.api.SmooksConfigException;
import org.smooks.api.ExecutionContext;
import org.smooks.engine.delivery.SAXAndDOMVisitor;
import org.smooks.engine.expression.ExpressionEvaluatorFactory;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ConditionEvaluatorTestCase {

	@Test
    public void test_Factory() {
        ExpressionEvaluatorFactory expressionEvaluatorFactory = new ExpressionEvaluatorFactory();
        try {
            expressionEvaluatorFactory.create(InvalidEvaluator.class.getName(), "blah");
            fail("Expected SmooksConfigurationException");
        } catch(SmooksConfigException e) {
            assertEquals("Unsupported ExpressionEvaluator type 'org.smooks.engine.delivery.condition.InvalidEvaluator'.  Currently only support 'org.smooks.api.expression.ExecutionContextExpressionEvaluator' implementations.", e.getMessage());
        }

        TestExecutionContextExpressionEvaluator evaluator = (TestExecutionContextExpressionEvaluator) expressionEvaluatorFactory.create(TestExecutionContextExpressionEvaluator.class.getName(), "blah");
        assertNotNull(evaluator.condition);
    }

	@Test
    public void test_DOM() throws IOException, SAXException {
        Smooks smooks;
        ExecutionContext execContext;

        SAXAndDOMVisitor.visited = false;
        smooks = new Smooks(getClass().getResourceAsStream("test-config-DOM-01.xml"));
        execContext = smooks.createExecutionContext();
        smooks.filterSource(execContext, new StreamSource(new StringReader("<a/>")));
        assertEquals(execContext, TestExecutionContextExpressionEvaluator.context);
        assertTrue(SAXAndDOMVisitor.visited);

        SAXAndDOMVisitor.visited = false;
        smooks = new Smooks(getClass().getResourceAsStream("test-config-DOM-02.xml"));
        execContext = smooks.createExecutionContext();
        smooks.filterSource(execContext, new StreamSource(new StringReader("<a/>")));
        assertEquals(execContext, TestExecutionContextExpressionEvaluator.context);
        assertFalse(SAXAndDOMVisitor.visited);
    }

	@Test
    public void test_SAX() throws IOException, SAXException {
        Smooks smooks;
        ExecutionContext execContext;

        SAXAndDOMVisitor.visited = false;
        smooks = new Smooks(getClass().getResourceAsStream("test-config-SAX-01.xml"));
        execContext = smooks.createExecutionContext();
        smooks.filterSource(execContext, new StreamSource(new StringReader("<a/>")));
        assertEquals(execContext, TestExecutionContextExpressionEvaluator.context);
        assertTrue(SAXAndDOMVisitor.visited);

        SAXAndDOMVisitor.visited = false;
        smooks = new Smooks(getClass().getResourceAsStream("test-config-SAX-02.xml"));
        execContext = smooks.createExecutionContext();
        smooks.filterSource(execContext, new StreamSource(new StringReader("<a/>")));
        assertEquals(execContext, TestExecutionContextExpressionEvaluator.context);
        assertFalse(SAXAndDOMVisitor.visited);
    }
}
