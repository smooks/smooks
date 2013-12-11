/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (version 2.1) as published by the Free Software
 * Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */
package example;

import org.milyn.commons.SmooksException;
import org.milyn.javabean.binding.xml.XMLBinding;
import org.milyn.commons.io.StreamUtils;
import org.milyn.payload.StringSource;
import org.xml.sax.SAXException;

import example.model.Order;
import example.model.OrderItem;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Simple example of the XMLBinding utility.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Main {

    public static String orderXMLMessage = readInputMessage();

    public static void main(String[] args) throws IOException, SAXException, SmooksException {

        // Create and initilise the XMLBinding instance...
        XMLBinding xmlBinding = new XMLBinding().add("smooks-config.xml");
        xmlBinding.intiailize();

        // Read the order XML into the Order Object model...
        Order order = xmlBinding.fromXML(new StringSource(orderXMLMessage), Order.class);

        // Do something with the order....

        // Write the Order object model instance back out to XML...
        String outXML = xmlBinding.toXML(order);  // (Note: There's also a version of toXML() that takes a Writer)

        // Display read/write info to the example user...
        displayInputMessage(orderXMLMessage);
        displayReadJavaObjects(order);
        displayWriteXML(outXML);
    }

    private static void displayInputMessage(String orderXMLMessage) {
        userMessage("\n\n** Press enter to see the Order input sample message (input-message.xml):");
        System.out.println("\n\n");
        System.out.println("==============Source Order XML Message==============");
        System.out.println(new String(orderXMLMessage));
        System.out.println("====================================================");
    }

    private static void displayReadJavaObjects(Order order) {
        userMessage("\n\n** Press enter to see the Order message as bound into the Order object model (XML reading):");
        System.out.println("============Order Java Bean===========");
        System.out.println("Header - Customer Name: " + order.getHeader().getCustomerName());
        System.out.println("       - Customer Num:  " + order.getHeader().getCustomerNumber());
        System.out.println("       - Order Date:    " + order.getHeader().getDate());
        System.out.println("\n");
        System.out.println("Order Items:");
        for(int i = 0; i < order.getOrderItems().size(); i++) {
            OrderItem orderItem = order.getOrderItems().get(i);
            System.out.println("       (" + (i + 1) + ") Product ID:  " + orderItem.getProductId());
            System.out.println("       (" + (i + 1) + ") Quantity:    " + orderItem.getQuantity());
            System.out.println("       (" + (i + 1) + ") Price:       " + orderItem.getPrice());
        }
        System.out.println("======================================");
    }

    private static void displayWriteXML(String xml) {
        userMessage("\n\n** Press enter to see the Order object model serialized back out to XML  (XML writing):");
        System.out.println("==============Serialized Order Bean XML==============");
        System.out.println(xml);
        System.out.println("=====================================================\n");
    }

    private static String readInputMessage() {
        try {
            return StreamUtils.readStreamAsString(new FileInputStream("input-message.xml"));
        } catch (IOException e) {
            e.printStackTrace();
            return "<no-message/>";
        }
    }

    private static void userMessage(String message) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("> " + message);
            in.readLine();
        } catch (IOException e) {
        }
        System.out.println("\n");
    }
}
