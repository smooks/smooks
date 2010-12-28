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

import org.milyn.Smooks;
import org.milyn.SmooksException;
import org.milyn.event.report.HtmlReportGenerator;
import org.milyn.container.ExecutionContext;
import org.milyn.payload.JavaResult;
import org.milyn.io.StreamUtils;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Simple example main class.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Main {

    private static byte[] messageIn = readInputMessage();

    protected static Map runSmooks() throws IOException, SAXException, SmooksException {

        // Instantiate Smooks with the config...
        Smooks smooks = new Smooks("smooks-config.xml");

        try {
             // Create an exec context - no profiles....
            ExecutionContext executionContext = smooks.createExecutionContext();
            // The result of this transform is a set of Java objects...
            JavaResult result = new JavaResult();

            // Configure the execution context to generate a report...
            executionContext.setEventListener(new HtmlReportGenerator("target/report/report.html"));

            // Filter the input message to extract, using the execution context...
            smooks.filterSource(executionContext, new StreamSource(new ByteArrayInputStream(messageIn)), result);

            return (Map) result.getBean("order");
        } finally {
            smooks.close();
        }
    }

    public static void main(String[] args) throws IOException, SAXException, SmooksException {
        System.out.println("\n\n");
        System.out.println("==============Message In==============");
        System.out.println(new String(messageIn));
        System.out.println("======================================\n");

        Map order = Main.runSmooks();

        System.out.println("============Order Javabeans===========");
        System.out.println("Header - Customer Name: " + ((Map)order.get("header")).get("customerName"));
        System.out.println("       - Customer Num:  " + ((Map)order.get("header")).get("customerNumber"));
        System.out.println("       - Order Date:    " + ((Map)order.get("header")).get("date"));
        System.out.println("\n");
        System.out.println("Order Items:");
        List<Map> orderItems = (List<Map>) order.get("orderItems");
        for(int i = 0; i < orderItems.size(); i++) {
            Map orderItem = orderItems.get(i);
            System.out.println("       (" + (i + 1) + ") Product ID:  " + orderItem.get("productId"));
            System.out.println("       (" + (i + 1) + ") Quantity:    " + orderItem.get("quantity"));
            System.out.println("       (" + (i + 1) + ") Price:       " + orderItem.get("price"));
        }
        System.out.println("======================================");
        System.out.println("\n\n");
    }

    private static byte[] readInputMessage() {
        try {
            return StreamUtils.readStream(new FileInputStream("input-message.xml"));
        } catch (IOException e) {
            e.printStackTrace();
            return "<no-message/>".getBytes();
        }
    }
}
