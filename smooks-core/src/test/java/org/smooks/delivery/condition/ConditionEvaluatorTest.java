/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package org.smooks.delivery.condition;

import org.junit.Test;
import static org.junit.Assert.*;
import org.smooks.Smooks;
import org.smooks.expression.ExpressionEvaluator;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.SAXAndDOMVisitor;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ConditionEvaluatorTest {

	@Test
    public void test_Factory() {
        try {
            ExpressionEvaluator.Factory.createInstance(InvalidEvaluator.class.getName(), "blah");
            fail("Expected SmooksConfigurationException");
        } catch(SmooksConfigurationException e) {
            assertEquals("Unsupported ExpressionEvaluator type 'org.smooks.delivery.condition.InvalidEvaluator'.  Currently only support 'org.smooks.expression.ExecutionContextExpressionEvaluator' implementations.", e.getMessage());
        }

        TestExecutionContextExpressionEvaluator evaluator = (TestExecutionContextExpressionEvaluator) ExpressionEvaluator.Factory.createInstance(TestExecutionContextExpressionEvaluator.class.getName(), "blah");
        assertNotNull(evaluator.condition);
    }

	@Test
    public void test_DOM() throws IOException, SAXException {
        Smooks smooks;
        ExecutionContext execContext;

        SAXAndDOMVisitor.visited = false;
        smooks = new Smooks(getClass().getResourceAsStream("test-config-DOM-01.xml"));
        execContext = smooks.createExecutionContext();
        smooks.filterSource(execContext, new StreamSource(new StringReader("<a/>")), null);
        assertEquals(execContext, TestExecutionContextExpressionEvaluator.context);
        assertTrue(SAXAndDOMVisitor.visited);

        SAXAndDOMVisitor.visited = false;
        smooks = new Smooks(getClass().getResourceAsStream("test-config-DOM-02.xml"));
        execContext = smooks.createExecutionContext();
        smooks.filterSource(execContext, new StreamSource(new StringReader("<a/>")), null);
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
        smooks.filterSource(execContext, new StreamSource(new StringReader("<a/>")), null);
        assertEquals(execContext, TestExecutionContextExpressionEvaluator.context);
        assertTrue(SAXAndDOMVisitor.visited);

        SAXAndDOMVisitor.visited = false;
        smooks = new Smooks(getClass().getResourceAsStream("test-config-SAX-02.xml"));
        execContext = smooks.createExecutionContext();
        smooks.filterSource(execContext, new StreamSource(new StringReader("<a/>")), null);
        assertEquals(execContext, TestExecutionContextExpressionEvaluator.context);
        assertFalse(SAXAndDOMVisitor.visited);
    }
}
