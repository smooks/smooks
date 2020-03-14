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
package org.smooks.javabean.context;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import static org.junit.Assert.*;

import org.smooks.Smooks;
import org.smooks.container.ExecutionContext;
import org.smooks.javabean.expression.BeanMapExpressionEvaluator;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class StaticVariableBinderTest {

    @Test
    public void test() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("staticvar-config.xml"));
        ExecutionContext execContext = smooks.createExecutionContext();

        smooks.filterSource(execContext, new StreamSource(new StringReader("<x/>")), null);

        Map<String, Object> beanMap = execContext.getBeanContext().getBeanMap();

        //assertEquals("{statvar={variable3=Hi Var3, variable1=Hi Var1, variable2=Hi Var2}}", BeanAccessor.getBeanMap(execContext).toString());
        assertEquals("Hi Var1", new BeanMapExpressionEvaluator("statvar.variable1").getValue(beanMap));
        assertEquals("Hi Var2", new BeanMapExpressionEvaluator("statvar.variable2").getValue(beanMap));
        assertEquals("Hi Var3", new BeanMapExpressionEvaluator("statvar.variable3").getValue(beanMap));
    }
}
