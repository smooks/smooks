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
package org.milyn.javabean.expression;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import static org.junit.Assert.*;

import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.expression.ExpressionEvaluationException;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class BeanMapExpressionEvaluatorTest {

	@Test
    public void test() throws IOException, SAXException {
        Smooks smooks;
        ExecutionContext execContext;
        Map<String, Object> bean = new HashMap<String, Object>();

        DOMVisitor.visited = false;
        smooks = new Smooks(getClass().getResourceAsStream("smooks-config-01.xml"));
        
        execContext = smooks.createExecutionContext();
        execContext.getBeanContext().addBean("aBean", bean, null);
        bean.put("a", "hello");
        
        smooks.filterSource(execContext, new StreamSource(new StringReader("<a/>")), null);
        assertTrue(DOMVisitor.visited);

        DOMVisitor.visited = false;
        smooks = new Smooks(getClass().getResourceAsStream("smooks-config-01.xml"));

        execContext = smooks.createExecutionContext();
        execContext.getBeanContext().addBean("aBean", bean, null);
        bean.put("a", "goodbye");        
        
        smooks.filterSource(execContext, new StreamSource(new StringReader("<a/>")), null);
        assertFalse(DOMVisitor.visited);
    }

	@Test
    public void testInvalidExpression() {
        // Just eval on an unbound variable...
        try {
            new BeanMapExpressionEvaluator("x.y").eval(new HashMap());
            fail("Expected ExpressionEvaluationException");
        } catch(Exception e) {
            assertTrue(e instanceof ExpressionEvaluationException);
        }
    }
}
