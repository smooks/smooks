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

import org.milyn.SmooksException;
import org.milyn.javabean.binding.xml.XMLBinding;
import org.milyn.io.StreamUtils;
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

    public static String orderV1XMLMessage = readInputMessage("v1-message.xml");
    public static String orderV2XMLMessage = readInputMessage("v2-message.xml");

    public static void main(String[] args) throws IOException, SAXException, SmooksException {

        // Create and initilise the XMLBinding instances for v1 and v2 of the XMLs...
        XMLBinding xmlBindingV1 = new XMLBinding().add("v1-binding-config.xml");
        XMLBinding xmlBindingV2 = new XMLBinding().add("v2-binding-config.xml");
        xmlBindingV1.intiailize();
        xmlBindingV2.intiailize();

        // Read the v1 order XML into the Order Object model...
        Order order = xmlBindingV1.fromXML(new StringSource(orderV1XMLMessage), Order.class);

        // Write the Order object model instance back out to XML using the v2 XMLBinding instance...
        String outXML = xmlBindingV2.toXML(order);  // (Note: There's also a version of toXML() that takes a Writer)

        // Display read/write info to the example user...
        displayInputMessage(orderV1XMLMessage);
        displayReadJavaObjects(order);
        displayWriteXML(outXML);
    }

    private static void displayInputMessage(String orderXMLMessage) {
        userMessage("\n\n** Press enter to see the Order input v1 sample message (v1-message.xml):");
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
        userMessage("\n\n** Press enter to see the Order object model serialized back out to XML as v2 (XML writing):");
        System.out.println("==============Serialized Order Bean XML==============");
        System.out.println(xml);
        System.out.println("=====================================================\n");
    }

    private static String readInputMessage(String fileName) {
        try {
            return StreamUtils.readStreamAsString(new FileInputStream(fileName));
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
