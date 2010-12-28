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

import com.acme.order.model.*;
import com.acme.order.model.field.Name;

import java.io.IOException;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.List;

import org.xml.sax.SAXException;
import org.milyn.io.FileUtils;

/**
 * Simple example of how to use an EJC generated Factory class.
 *
 * @author bardl
 */
public class Main {

    public static void main(String[] args) throws IOException, SAXException {
        String ediMessage = new String(FileUtils.readFile(new File("input-message.edi")));
        StringReader ediStream = new StringReader(ediMessage);

        // Create the Factory class instance.  This should normally be cached
        // and reused...
        OrderFactory orderFactory = OrderFactory.getInstance();

        // Bind the EDI message stream data into the EJC generated Order model...
        Order order = orderFactory.fromEDI(ediStream);

        // Print some of the populated Order object...
        Header header = order.getHeader();
        Name name = header.getCustomerDetails().getName();
        List<OrderItem> orderItems = order.getOrderItems().getOrderItem();
        OrderItem orderItem1 = orderItems.get(0);
        OrderItem orderItem2 = orderItems.get(1);

        System.out.println();
        System.out.println("Input EDI Message:");
        System.out.println(ediMessage);

        System.out.println();
        System.out.println("Populated EJC Generated Model:");
        System.out.println("\tName:       " + name.getLastname() + ", " + name.getFirstname());
        System.out.println("\tEmail:      " + header.getCustomerDetails().getEmail());
        System.out.println("\tDate:       " + header.getDate());
        System.out.println("\tProduct 1:  " + orderItem1.getProductId() + ", " + orderItem1.getTitle() + ", " + orderItem1.getPrice());
        System.out.println("\tProduct 2:  " + orderItem2.getProductId() + ", " + orderItem2.getTitle() + ", " + orderItem2.getPrice());
        System.out.println();

        System.out.println();
        System.out.println("Make some modifications to the model and add an additional OrderItem ...");
        System.out.println();

        header.setStatusCode(2);
        header.getCustomerDetails().setEmail("me@smooks.com");
        orderItems.add(new OrderItem().setPosition(3).setPrice(new BigDecimal(27.98f)).setProductId("S45").setQuantity(4L).setTitle("Baby Rattlers"));

        System.out.println();
        System.out.println("Write the modified model to System.out ...");
        System.out.println();

        orderFactory.toEDI(order, new PrintWriter(System.out));

        System.out.println();
    }
}
