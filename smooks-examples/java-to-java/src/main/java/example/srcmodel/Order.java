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
package example.srcmodel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Order {
    private Header header;
    private List<OrderItem> orderItems;

    public Order() {
        header = new Header();
        orderItems =  new ArrayList<OrderItem>();
        orderItems.add(new OrderItem());
        orderItems.add(new OrderItem());

        orderItems.get(0).setProductId(111);
        orderItems.get(0).setQuantity(2);
        orderItems.get(0).setPrice(10.99);

        orderItems.get(1).setProductId(222);
        orderItems.get(1).setQuantity(4);
        orderItems.get(1).setPrice(25.50);
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    @Override
	public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Class: " + getClass().getName() + "\n");
        stringBuilder.append("\theader: " + header + "\n");
        stringBuilder.append("\torderItems: " + orderItems);

        return stringBuilder.toString();
    }
}