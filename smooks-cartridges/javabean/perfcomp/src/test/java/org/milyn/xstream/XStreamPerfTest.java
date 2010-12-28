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
package org.milyn.xstream;

import junit.framework.TestCase;

import org.milyn.Order;
import org.milyn.OrderItem;
import org.milyn.TestConstants;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class XStreamPerfTest extends TestCase {

    public void test() {
        XStream xstream = new XStream(new StaxDriver());

        xstream.alias("order", Order.class);
        xstream.alias("orderItem", OrderItem.class);
        
        for(int i = 0; i < TestConstants.NUM_WARMUPS; i++) {
            xstream.fromXML(TestConstants.getMessageReader());
        }

        long start = System.currentTimeMillis();
        for(int i = 0; i < TestConstants.NUM_ITERATIONS; i++) {
            xstream.fromXML(TestConstants.getMessageReader());
        }
        System.out.println("XStream Took: " + (System.currentTimeMillis() - start));

        Order order = (Order) xstream.fromXML(TestConstants.getMessageReader());
        System.out.println("Num order items: " + order.getOrderItems().size());
    }
}
