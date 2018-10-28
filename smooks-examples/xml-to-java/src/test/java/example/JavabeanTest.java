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
package example;

import org.junit.Test;
import static org.junit.Assert.*;
import example.model.Order;
import example.model.OrderItem;

import java.io.IOException;

import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class JavabeanTest {

	@Test
    public void test() throws IOException, SAXException {
        Order order = Main.runSmooks();

        assertNotNull(order);
        assertNotNull(order.getHeader());
        assertNotNull(order.getOrderItems());
        assertEquals(2, order.getOrderItems().size());

        assertEquals(1163616328000L, order.getHeader().getDate().getTime());
        assertEquals("Joe", order.getHeader().getCustomerName());
        assertEquals(new Long(123123), order.getHeader().getCustomerNumber());

        OrderItem orderItem = order.getOrderItems().get(0);
        assertEquals(8.90d, orderItem.getPrice(), 0d);
        assertEquals(111, orderItem.getProductId());
        assertEquals(new Integer(2), orderItem.getQuantity());

        orderItem = order.getOrderItems().get(1);
        assertEquals(5.20d, orderItem.getPrice(), 0d);
        assertEquals(222, orderItem.getProductId());
        assertEquals(new Integer(7), orderItem.getQuantity());
    }
}
