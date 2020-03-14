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
package org.smooks.javabean.expressionbinding;

import org.junit.Test;
import org.smooks.Smooks;
import org.smooks.container.ExecutionContext;
import org.smooks.payload.JavaResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.stream.StreamSource;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ExpressionBindingTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionBindingTest.class);

    @Test
    public void test_data_variable() throws Exception {
    	Smooks smooks = new Smooks(getClass().getResourceAsStream("02_binding.xml"));

    	JavaResult result = new JavaResult();

    	ExecutionContext context = smooks.createExecutionContext();
    	//context.setEventListener(new HtmlReportGenerator("target/expression_data_variable.html"));

    	smooks.filterSource(context, new StreamSource(getClass().getResourceAsStream("02_number.xml")), result);

    	Total total = (Total) result.getBean("total");

    	assertEquals(20, (int) total.getTotal());
    	assertEquals("10,20,30,40", total.getCsv());

    }

    private void assertDateValue(JavaResult result, String beanId) {
        Map<?, ?> message = (Map<?, ?>) result.getBean(beanId);
        Date messageDate = (Date) message.get("date");
        LOGGER.debug("Date: " + messageDate);
        assertEquals(946143900000L, messageDate.getTime());
    }
}
