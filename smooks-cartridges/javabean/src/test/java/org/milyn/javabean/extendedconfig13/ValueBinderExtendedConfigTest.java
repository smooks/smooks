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
package org.milyn.javabean.extendedconfig13;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.event.report.HtmlReportGenerator;
import org.milyn.payload.JavaResult;
import org.milyn.commons.util.ClassUtil;
import org.xml.sax.SAXException;

public class ValueBinderExtendedConfigTest extends TestCase {

	public void test_01() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("test_value_01.xml"));
		JavaResult result = new JavaResult();
		ExecutionContext execContext = smooks.createExecutionContext();

		//execContext.setEventListener(new HtmlReportGenerator("target/report/ValueBinderExtendedConfigTest-report.html"));

		smooks.filterSource(execContext, new StreamSource(getInput("order-01.xml")), result);

		assertEquals("Joe", result.getBean("customerName"));
		assertEquals(123123, result.getBean("customerNumber"));
		assertEquals(Boolean.TRUE, result.getBean("privatePerson"));
		assertEquals(1163616328000L, ((Date) result.getBean("date")).getTime());

		assertNull(result.getBean("product"));
	}

	public void test_01_other() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("test_value_01.xml"));

		JavaResult result = new JavaResult();
		ExecutionContext execContext = smooks.createExecutionContext("other");

		smooks.filterSource(execContext, new StreamSource(getInput("order-01.xml")), result);

		assertEquals(222, result.getBean("product"));
	}


	/**
	 * @return
	 */
	private InputStream getInput(String file) {
		return ClassUtil.getResourceAsStream("/org/milyn/javabean/extendedconfig/" + file, this.getClass());
	}
}
