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
package org.milyn.javabean.programatic;

import junit.framework.TestCase;
import org.milyn.Smooks;
import org.milyn.commons.javabean.decoders.DoubleDecoder;
import org.milyn.commons.javabean.decoders.IntegerDecoder;
import org.milyn.container.ExecutionContext;
import org.milyn.event.ExecutionEvent;
import org.milyn.event.ExecutionEventListener;
import org.milyn.event.types.ElementPresentEvent;
import org.milyn.event.types.ElementVisitEvent;
import org.milyn.javabean.Bean;
import org.milyn.javabean.Header;
import org.milyn.javabean.Order;
import org.milyn.javabean.OrderItem;
import org.milyn.javabean.factory.Factory;
import org.milyn.javabean.factory.MVELFactory;
import org.milyn.payload.JavaResult;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Programmatic Binding config test for the Bean class.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class ProgrammaticBeanConfigTest extends TestCase {

    public void test_01_fluent() {
        Smooks smooks = new Smooks();
        Bean orderBean = new Bean(Order.class, "order", "/order");

        orderBean.bindTo("header",
                orderBean.newBean(Header.class, "/order")
                        .bindTo("order", orderBean)
                        .bindTo("customerNumber", "header/customer/@number")
                        .bindTo("customerName", "header/customer")
                        .bindTo("privatePerson", "header/privatePerson")
        ).bindTo("orderItems",
                orderBean.newBean(ArrayList.class, "/order")
                        .bindTo(orderBean.newBean(OrderItem.class, "order-item")
                                .bindTo("productId", "order-item/product")
                                .bindTo("quantity", "order-item/quantity")
                                .bindTo("price", "order-item/price"))
        ).bindTo("orderItems",
                orderBean.newBean(OrderItem[].class, "/order")
                        .bindTo(orderBean.newBean(OrderItem.class, "order-item")
                                .bindTo("productId", "order-item/product")
                                .bindTo("quantity", "order-item/quantity")
                                .bindTo("price", "order-item/price")));

        smooks.addVisitor(orderBean);

        execute_01_test(smooks);
    }

    public void test_01_factory() {
        Smooks smooks = new Smooks();
        Bean orderBean = new Bean(Order.class, "order", "/order", new Factory<Order>() {

            public Order create(ExecutionContext executionContext) {
                return new Order();
            }

        });

        orderBean.bindTo("header",
                orderBean.newBean(Header.class, "/order")
                        .bindTo("order", orderBean)
                        .bindTo("customerNumber", "header/customer/@number")
                        .bindTo("customerName", "header/customer")
                        .bindTo("privatePerson", "header/privatePerson")
        ).bindTo("orderItems",
                orderBean.newBean(Collection.class, "/order", new MVELFactory<Collection>("new java.util.ArrayList()"))
                        .bindTo(orderBean.newBean(OrderItem.class, "order-item")
                                .bindTo("productId", "order-item/product")
                                .bindTo("quantity", "order-item/quantity")
                                .bindTo("price", "order-item/price"))
        ).bindTo("orderItems",
                orderBean.newBean(OrderItem[].class, "/order")
                        .bindTo(orderBean.newBean(OrderItem.class, "order-item")
                                .bindTo("productId", "order-item/product")
                                .bindTo("quantity", "order-item/quantity")
                                .bindTo("price", "order-item/price")));

        smooks.addVisitor(orderBean);

        execute_01_test(smooks);
    }

    public void test_invalid_bindTo() {
        Smooks smooks = new Smooks();
        Bean orderBean = new Bean(Order.class, "order", "/order");

        smooks.addVisitor(orderBean);

        try {
            // invalid attempt to bindTo after it has been added to the Smooks instance...
            orderBean.bindTo("header",
                    orderBean.newBean(Header.class, "/order")
                            .bindTo("privatePerson", "header/privatePerson"));

            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("Unexpected attempt to bindTo Bean instance after the Bean instance has been added to a Smooks instance.", e.getMessage());
        }
    }

    public void test_01_flat() {
        Smooks smooks = new Smooks();
        Bean orderBean = new Bean(Order.class, "order", "/order");

        Bean headerBean = new Bean(Header.class, "header", "/order")
                .bindTo("order", orderBean)
                .bindTo("customerNumber", "header/customer/@number")
                .bindTo("customerName", "header/customer")
                .bindTo("privatePerson", "header/privatePerson");

        orderBean.bindTo("header", headerBean);
        orderBean.bindTo("orderItems", orderBean.newBean(ArrayList.class, "/order")
                .bindTo(orderBean.newBean(OrderItem.class, "order-item")
                        .bindTo("productId", "order-item/product")
                        .bindTo("quantity", "order-item/quantity")
                        .bindTo("price", "order-item/price")));
        orderBean.bindTo("orderItems", orderBean.newBean(OrderItem[].class, "/order")
                .bindTo(orderBean.newBean(OrderItem.class, "order-item")
                        .bindTo("productId", "order-item/product")
                        .bindTo("quantity", "order-item/quantity")
                        .bindTo("price", "order-item/price")));

        smooks.addVisitor(orderBean);

        execute_01_test(smooks);
    }

    private void execute_01_test(Smooks smooks) {
        JavaResult result = new JavaResult();
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("../order-01.xml")), result);

        Order order = (Order) result.getBean("order");
        int identity = System.identityHashCode(order);

        assertEquals("Order:" + identity + "[header[null, 123123, Joe, false, Order:" + identity + "]\n" +
                "orderItems[[{productId: 111, quantity: 2, price: 8.9}, {productId: 222, quantity: 7, price: 5.2}]]\n" +
                "norderItemsArray[[{productId: 111, quantity: 2, price: 8.9}, {productId: 222, quantity: 7, price: 5.2}]]]", order.toString());
    }

    public void test_02_Map_fluid() {
        Smooks smooks = new Smooks();

        Bean orderBean = new Bean(HashMap.class, "order", "/order");

        orderBean.bindTo("header",
                orderBean.newBean(HashMap.class, "/order")
                        .bindTo("customerNumber", "header/customer/@number", new IntegerDecoder())
                        .bindTo("customerName", "header/customer")
                        .bindTo("privatePerson", "header/privatePerson")
        ).bindTo("orderItems",
                orderBean.newBean(ArrayList.class, "/order")
                        .bindTo(orderBean.newBean(HashMap.class, "order-item")
                                .bindTo("productId", "order-item/product")
                                .bindTo("quantity", "order-item/quantity")
                                .bindTo("price", "order-item/price", new DoubleDecoder()))
        );

        smooks.addVisitor(orderBean);

        JavaResult result = new JavaResult();
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("../order-01.xml")), result);

        Map order = (Map) result.getBean("order");

        HashMap headerMap = (HashMap) order.get("header");
        assertEquals("Joe", headerMap.get("customerName"));
        assertEquals(123123, headerMap.get("customerNumber"));
        assertEquals("", headerMap.get("privatePerson"));

        ArrayList<HashMap> orderItems = (ArrayList<HashMap>) order.get("orderItems");
        for (HashMap orderItem : orderItems) {
            String quantity = (String) orderItem.get("quantity");
            if (quantity.equals("2")) {
                assertEquals("111", orderItem.get("productId"));
                assertEquals(8.9, orderItem.get("price"));
            } else {
                assertEquals("222", orderItem.get("productId"));
                assertEquals(5.2, orderItem.get("price"));
            }
        }
    }

    public void test_02_arrays_programmatic() {
        Smooks smooks = new Smooks();

        Bean orderBean = new Bean(Order.class, "order", "order");
        Bean orderItemArray = new Bean(OrderItem[].class, "orderItemsArray", "order");
        Bean orderItem = new Bean(OrderItem.class, "orderItem", "order-item");

        orderItem.bindTo("productId", "order-item/product");
        orderItemArray.bindTo(orderItem);
        orderBean.bindTo("orderItems", orderItemArray);

        smooks.addVisitor(orderBean);

        execSmooksArrays(smooks);
    }

    public void test_02_arrays_xml() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("xmlconfig_01.xml"));
        execSmooksArrays(smooks);
    }

    private void execSmooksArrays(Smooks smooks) {
        JavaResult result = new JavaResult();
        ExecutionContext execContext = smooks.createExecutionContext();

        //execContext.setEventListener(new ExecListener());
        smooks.filterSource(execContext, new StreamSource(getClass().getResourceAsStream("order-01.xml")), result);

        Order order = (Order) result.getBean("order");
        int identity = System.identityHashCode(order);

        assertEquals("Order:" + identity + "[header[null]\n" +
                "orderItems[null]\n" +
                "norderItemsArray[[{productId: 111, quantity: null, price: 0.0}, {productId: 222, quantity: null, price: 0.0}]]]", order.toString());
    }

    private class ExecListener implements ExecutionEventListener {
        public void onEvent(ExecutionEvent event) {
            if (event instanceof ElementPresentEvent || event instanceof ElementVisitEvent) {
                return;
            }

            System.out.println(event);
        }
    }
}
