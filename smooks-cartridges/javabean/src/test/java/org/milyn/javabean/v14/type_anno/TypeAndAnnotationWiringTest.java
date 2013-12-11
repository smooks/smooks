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
package org.milyn.javabean.v14.type_anno;

import java.io.IOException;

import javax.xml.transform.stream.StreamSource;

import org.milyn.Smooks;
import org.milyn.commons.SmooksException;
import org.milyn.javabean.extendedconfig.ExtendedOrder;
import org.milyn.payload.JavaResult;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class TypeAndAnnotationWiringTest extends TestCase {

	public void test_by_type() throws IOException, SAXException {
		test("test_bean_01.xml");
	}

	public void test_by_anno() throws IOException, SAXException {
		test("test_bean_02.xml");
	}

	public void test_bad_vonfig() throws IOException, SAXException {
		try {
			test("test_bean_03.xml");
		} catch (SmooksException e) {
			assertEquals("One or more of attributes 'beanIdRef', 'beanType' and 'beanAnnotation' must be specified on a bean wiring configuration.", e.getCause().getMessage());
		}
	}
	
	public void test(String config) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream(config));
        JavaResult result = new JavaResult();

        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order-01.xml")), result);

        ExtendedOrder order = (ExtendedOrder) result.getBean("order");

        assertEquals("[{productId: 111, quantity: 2, price: 8.9}, {productId: 222, quantity: 7, price: 5.2}]", order.getOrderItems().toString());
	}
}
