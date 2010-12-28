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
package org.milyn.jibx;

import junit.framework.TestCase;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.milyn.Order;
import org.milyn.TestConstants;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class JiBXPerfTest extends TestCase {

    public void test() throws JiBXException {
    	IBindingFactory bfact = BindingDirectory.getFactory(Order.class);
        IUnmarshallingContext uctx = bfact.createUnmarshallingContext();

        for(int i = 0; i < TestConstants.NUM_WARMUPS; i++) {
            Order order = (Order) uctx.unmarshalDocument(TestConstants.getMessageReader(), null);
        }

        long start = System.currentTimeMillis();
        for(int i = 0; i < TestConstants.NUM_ITERATIONS; i++) {
            Order order = (Order) uctx.unmarshalDocument(TestConstants.getMessageReader(), null);
        }
        
        System.out.println("JiBX took: " + (System.currentTimeMillis() - start));
        Order order = (Order) uctx.unmarshalDocument(TestConstants.getMessageReader(), null);
        if(order != null) {
        	System.out.println("Num order items: " + order.getOrderItems().size());
        }
    }
}
