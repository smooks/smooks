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
package org.milyn.cartridge.javabean;

import junit.framework.TestCase;
import org.milyn.Smooks;
import org.milyn.payload.JavaResult;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 * @author <a href="mailto:dbevenius@redhat.com">Daniel Bevenius</a>
 */
public class ListOfListsTest extends TestCase {

    public void test() throws IOException, SAXException {              
        Smooks smooks = new Smooks(getClass().getResourceAsStream("list-of-lists-config.xml"));
        JavaResult javaResult = new JavaResult();

        smooks.filterSource(smooks.createExecutionContext(), new StreamSource(getClass().getResourceAsStream("list-of-lists-message.xml")), javaResult);
        assertEquals(getExpectedOrderArray(), javaResult.getBean("order"));
    }
    
    private ArrayList<HashMap<String, Object>> getExpectedOrderArray()
    {
    	final ArrayList<HashMap<String,Object>> order = new ArrayList<HashMap<String, Object>>();
    	ArrayList<HashMap<String,Object>> orderItems = new ArrayList<HashMap<String,Object>>();
    	
    	// Create blockNum 1
    	HashMap<String, Object> orderItemBlock = new HashMap<String, Object>();
    	orderItemBlock.put("blockNum", 1);
    	createOrderItem(111, 2, 8.9, orderItems);
    	createOrderItem(222, 7, 5.2, orderItems);
    	orderItemBlock.put("orderItems", orderItems);
    	order.add(orderItemBlock);
    	
    	// Create blockNum 2
    	orderItemBlock = new HashMap<String, Object>();
    	orderItems = new ArrayList<HashMap<String,Object>>();
    	orderItemBlock.put("blockNum", 2);
    	createOrderItem(333, 2, 8.9, orderItems);
    	createOrderItem(444, 7, 5.2, orderItems);
    	orderItemBlock.put("orderItems", orderItems);
    	order.add(orderItemBlock);
    	
    	return order;
    }

	private HashMap<String, Object> createOrderItem(
			final long productId, 
			final int quantity, 
			final double price,
			ArrayList<HashMap<String,Object>> orderItems)
	{
		HashMap<String, Object> orderItem = new HashMap<String, Object>();
    	orderItem.put("productId", productId);
    	orderItem.put("quantity", quantity);
    	orderItem.put("price", price);
    	orderItems.add(orderItem);
		return orderItem;
	}
}
