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

import junit.framework.TestCase;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class JavabeanTest extends TestCase {

    public void test() throws IOException, SAXException {
        Map order = Main.runSmooks();

        assertNotNull(order);
        assertNotNull(order.get("header"));
        assertNotNull(order.get("orderItems"));
        assertEquals(2, ((List)order.get("orderItems")).size());

        assertEquals(1163616328000L, ((Date)((Map)order.get("header")).get("date")).getTime());
        assertEquals("Joe", ((Map)order.get("header")).get("customerName"));
        assertEquals(new Long(123123), ((Map)order.get("header")).get("customerNumber"));

        Map orderItem = (Map) ((List)order.get("orderItems")).get(0);
        assertEquals(8.90d, orderItem.get("price"));
        assertEquals(111L, orderItem.get("productId"));
        assertEquals(new Integer(2), orderItem.get("quantity"));

        orderItem = (Map) ((List)order.get("orderItems")).get(1);
        assertEquals(5.20d, orderItem.get("price"));
        assertEquals(222L, orderItem.get("productId"));
        assertEquals(new Integer(7), orderItem.get("quantity"));
    }
}
