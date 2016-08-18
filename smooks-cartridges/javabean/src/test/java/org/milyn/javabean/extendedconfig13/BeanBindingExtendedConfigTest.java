/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License (version 2.1) as published by the Free Software
 *  Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  See the GNU Lesser General Public License for more details:
 *  http://www.gnu.org/licenses/lgpl.txt
 */
package org.milyn.javabean.extendedconfig13;

import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.*;
import org.milyn.Smooks;
import org.milyn.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.javabean.B;
import org.milyn.javabean.Header;
import org.milyn.javabean.OrderItem;
import org.milyn.javabean.extendedconfig.ExtendedOrder;
import org.milyn.payload.JavaResult;
import org.milyn.util.ClassUtil;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class BeanBindingExtendedConfigTest {

    @Ignore
    @Test
    public void test() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test_bean_01.xml"));
        JavaResult result = new JavaResult();
        ExecutionContext execContext = smooks.createExecutionContext();

        //execContext.setEventListener(new HtmlReportGenerator("/zap/report.html"));
        smooks.filterSource(execContext, new StreamSource(getInput("order-01.xml")), result);

        ExtendedOrder order = (ExtendedOrder) result.getBean("order");
        assertOrderOK(order, true);

        Map<String,String> headerHash = (Map) result.getBean("headerBeanHash");
        assertThat(headerHash, hasEntry("date","Wed Nov 15 13:45:28 EST 2006"));
        assertThat(headerHash, hasEntry("privatePerson",""));
        assertThat(headerHash, hasEntry("customer","Joe"));
    }

	/**
	 * @return
	 */
	private InputStream getInput(String file) {
		return ClassUtil.getResourceAsStream("/org/milyn/javabean/extendedconfig/" + file, this.getClass());
	}

    public static void assertOrderOK(ExtendedOrder order, boolean checkArrays) {

        // Order total...
        assertEquals(54.2d, order.getTotal(), 0d);

        // Header...
        assertEquals("Joe", order.getHeader().getCustomerName());
        assertEquals(new Long(123123), order.getHeader().getCustomerNumber());
        assertEquals(1163616328000L, order.getHeader().getDate().getTime());
        assertEquals(true, order.getHeader().getPrivatePerson());
        assertTrue(order == order.getHeader().getOrder());

        // OrderItems list...
        assertEquals(2, order.getOrderItems().size());
        assertTrue(order == order.getOrderItems().get(0).getOrder());
        assertEquals(8.9d, order.getOrderItems().get(0).getPrice(), 0d);
        assertEquals(111, order.getOrderItems().get(0).getProductId());
        assertEquals(new Integer(2), order.getOrderItems().get(0).getQuantity());
        assertTrue(order == order.getOrderItems().get(1).getOrder());
        assertEquals(5.2d, order.getOrderItems().get(1).getPrice(), 0d);
        assertEquals(222, order.getOrderItems().get(1).getProductId());
        assertEquals(new Integer(7), order.getOrderItems().get(1).getQuantity());

        if(checkArrays) {
	        // OrderItems array...
	        assertEquals(2, order.getOrderItemsArray().length);
	        assertTrue(order == order.getOrderItemsArray()[0].getOrder());
	        assertEquals(8.9d, order.getOrderItemsArray()[0].getPrice(), 0d);
	        assertEquals(111, order.getOrderItemsArray()[0].getProductId());
	        assertEquals(new Integer(2), order.getOrderItemsArray()[0].getQuantity());
	        assertTrue(order == order.getOrderItemsArray()[1].getOrder());
	        assertEquals(5.2d, order.getOrderItemsArray()[1].getPrice(), 0d);
	        assertEquals(222, order.getOrderItemsArray()[1].getProductId());
	        assertEquals(new Integer(7), order.getOrderItemsArray()[1].getQuantity());
        }
    }

    @Test
    public void test_error_for_List_property() throws IOException, SAXException {
        try {
            new Smooks(getClass().getResourceAsStream("test_bean_02.xml"));
            fail("Expected SmooksException");
        } catch(SmooksException e) {
            assertEquals("'wiring' binding specifies a 'property' attribute.  This is not valid for a Collection target.", e.getCause().getMessage());
        }
    }

    @Test
    public void test_error_for_Array_property() throws IOException, SAXException {
        try {
            new Smooks(getClass().getResourceAsStream("test_bean_03.xml"));
            fail("Expected SmooksException");
        } catch(SmooksException e) {
            assertEquals("'wiring' binding specifies a 'property' attribute.  This is not valid for an Array target.", e.getCause().getMessage());
        }
    }

    @Test
    public void test_error_for_no_property_on_non_list_or_array() throws IOException, SAXException {
        try {
            new Smooks(getClass().getResourceAsStream("test_bean_04.xml"));
            fail("Expected SmooksException");
        } catch(SmooksException e) {
            assertEquals("'wiring' binding for bean class 'org.milyn.javabean.extendedconfig.ExtendedOrder' must specify a 'property' or 'setterMethod' attribute.", e.getCause().getMessage());
        }
    }

    @Test
    public void test_error_for_property_and_setterMethod() throws IOException, SAXException {
        try {
            new Smooks(getClass().getResourceAsStream("test_bean_10.xml"));
            fail("Expected SmooksException");
        } catch(SmooksException e) {
            assertEquals("'wiring' binding specifies a 'property' and a 'setterMethod' attribute.  Only one of both may be set.", e.getCause().getMessage());
        }
    }

    @Test
    public void test_order_update() throws IOException, SAXException {
    	 Smooks smooks = new Smooks(getClass().getResourceAsStream("test_bean_05.xml"));

    	 ExtendedOrder inExtendedOrder = new ExtendedOrder();
    	 List<OrderItem> inOrderItems = new ArrayList<OrderItem>();
    	 Header inHeader = new Header();

         JavaResult result = new JavaResult();
    	 result.getResultMap().put("order", inExtendedOrder);
    	 result.getResultMap().put("orderItemList", inOrderItems);
    	 result.getResultMap().put("headerBean", inHeader);

         ExecutionContext execContext = smooks.createExecutionContext();

         //execContext.setEventListener(new HtmlReportGenerator("/target/report.html"));
         smooks.filterSource(execContext, new StreamSource(getInput("order-01.xml")), result);

         ExtendedOrder order = (ExtendedOrder) result.getBean("order");

         assertSame(inExtendedOrder, order);
         assertSame(inOrderItems, order.getOrderItems());
         assertSame(inHeader, order.getHeader());

         assertOrderOK(order, false);
    }

    @Test
    public void test_error_for_no_wireOnElement() throws IOException, SAXException {
        try {
            new Smooks(getClass().getResourceAsStream("test_bean_06.xml"));
            fail("Expected SmooksException");
        } catch(SmooksException e) {
            assertEquals("The bindings attribute 'createOnElement' and wiring attribute 'wireOnElement' are both not set. " +
            		"One of them must at least be set. If the result of this binding should be a new populated Object then " +
            		"you need to set the 'createOnElement' bindings attribute. If you want to update an existing object in " +
            		"the bean context then you must set the 'wireOnElement' attribute.", e.getCause().getMessage());
        }
    }

    @Test
    public void test_flat_xml_set_in_binding() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("test_bean_07.xml"));

		JavaResult result = new JavaResult();

		ExecutionContext execContext = smooks.createExecutionContext();

		//execContext.setEventListener(new HtmlReportGenerator("target/report.html"));
		smooks.filterSource(execContext, new StreamSource(getInput("flat-01.xml")), result);

		assertFlatResult(result);
	}

    @Test
    public void test_flat_xml_set_global() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("test_bean_08.xml"));

		JavaResult result = new JavaResult();

		ExecutionContext execContext = smooks.createExecutionContext();

		//execContext.setEventListener(new HtmlReportGenerator("target/report.html"));
		smooks.filterSource(execContext, new StreamSource(getInput("flat-01.xml")), result);

		assertFlatResult(result);
	}

	public void assertFlatResult(JavaResult result) {
		@SuppressWarnings("unchecked")
    	ArrayList<ArrayList<B>> root = (ArrayList<ArrayList<B>>) result.getBean("root");

		assertNotNull("root should not be null", root);

		assertEquals(2, root.size());
		assertEquals(3, root.get(0).size());
		assertEquals(3, root.get(1).size());
    }

        @Test
	public void test_profile() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("test_bean_09.xml"));

		JavaResult result = new JavaResult();

		ExecutionContext execContext = smooks.createExecutionContext("A");

		//execContext.setEventListener(new HtmlReportGenerator("target/report.html"));
		smooks.filterSource(execContext, new StreamSource(getInput("order-01.xml")), result);

		ExtendedOrder order =  (ExtendedOrder) result.getBean("order");
		assertEquals(2d, order.getTotal(), 0d);

		execContext = smooks.createExecutionContext("B");

		//execContext.setEventListener(new HtmlReportGenerator("target/report.html"));
		smooks.filterSource(execContext, new StreamSource(getInput("order-01.xml")), result);

		order =  (ExtendedOrder) result.getBean("order");
		assertEquals(4d, order.getTotal(), 0d);

	}

        @Test
	public void test_condition() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("test_bean_11.xml"));

		JavaResult result = new JavaResult();

		ExecutionContext execContext = smooks.createExecutionContext();

		//execContext.setEventListener(new HtmlReportGenerator("target/report.html"));
		smooks.filterSource(execContext, new StreamSource(getInput("order-01.xml")), result);

		ExtendedOrder order =  (ExtendedOrder) result.getBean("order");
		assertEquals(2d, order.getTotal(), 0d);
	}

    @Test
    public void test_expression_initVal() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test_bean_12.xml"));

        JavaResult result = new JavaResult();

        ExecutionContext execContext = smooks.createExecutionContext();

        //execContext.setEventListener(new HtmlReportGenerator("target/report.html"));
        smooks.filterSource(execContext, new StreamSource(getInput("order-01.xml")), result);

        Map order =  (Map) result.getBean("orderItem");
        assertEquals(154.2d, ((Double)order.get("total")).doubleValue(), 0d);
    }

    @Test
    public void test_factory() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test_bean_13.xml"));

        JavaResult result = new JavaResult();

        ExecutionContext execContext = smooks.createExecutionContext();

        //execContext.setEventListener(new HtmlReportGenerator("target/report.html"));
        smooks.filterSource(execContext, new StreamSource(getInput("order-01.xml")), result);

        Map<?, ?> order =  (Map<?, ?>) result.getBean("order");

        assertTrue(order instanceof HashMap);
    }

    @Test
    public void test_factory_alias() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test_bean_14.xml"));

        JavaResult result = new JavaResult();

        ExecutionContext execContext = smooks.createExecutionContext();

        //execContext.setEventListener(new HtmlReportGenerator("target/report.html"));
        smooks.filterSource(execContext, new StreamSource(getInput("order-01.xml")), result);

        Map<?, ?> order1 =  (Map<?, ?>) result.getBean("default_order");
        Map<?, ?> order2 =  (Map<?, ?>) result.getBean("mvel_order");
        Map<?, ?> order3 =  (Map<?, ?>) result.getBean("mvel_class_order");
        Map<?, ?> order4 =  (Map<?, ?>) result.getBean("basic_order");

        assertTrue(order1 instanceof HashMap);
        assertTrue(order2 instanceof HashMap);
        assertTrue(order3 instanceof HashMap);
        assertTrue(order4 instanceof HashMap);
    }

    @Test
    public void test_factory_global_mvel() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test_bean_15.xml"));

        JavaResult result = new JavaResult();

        ExecutionContext execContext = smooks.createExecutionContext();

        //execContext.setEventListener(new HtmlReportGenerator("target/report.html"));
        smooks.filterSource(execContext, new StreamSource(getInput("order-01.xml")), result);

        Map<?, ?> order =  (Map<?, ?>) result.getBean("order");

        assertTrue(order instanceof HashMap);
    }
}
