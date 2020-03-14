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

import org.smooks.Smooks;
import org.smooks.SmooksException;
import org.smooks.container.ExecutionContext;
import org.smooks.csv.CSVRecordParserConfigurator;
import org.smooks.event.report.HtmlReportGenerator;
import org.smooks.flatfile.Binding;
import org.smooks.flatfile.BindingType;
import org.smooks.io.StreamUtils;
import org.smooks.payload.JavaResult;
import org.smooks.payload.StringSource;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Simple example main class.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Main {

    private static String messageIn = readInputMessage();

    protected static List runSmooksTransform() throws IOException, SAXException, SmooksException {

        Smooks smooks = new Smooks();

        try {
            // ****
            // And here's the configuration... configuring the CSV reader and the direct
            // binding config to create a List of Person objects (List<Person>)...
            // ****
            smooks.setReaderConfig(new CSVRecordParserConfigurator("firstName,lastName,gender,age,country")
                    .setBinding(new Binding("customerList", Customer.class, BindingType.LIST)));

            // Configure the execution context to generate a report...
            ExecutionContext executionContext = smooks.createExecutionContext();
            executionContext.setEventListener(new HtmlReportGenerator("target/report/report.html"));

            JavaResult javaResult = new JavaResult();
            smooks.filterSource(executionContext, new StringSource(messageIn), javaResult);

            return (List) javaResult.getBean("customerList");
        } finally {
            smooks.close();
        }
    }

    public static void main(String[] args) throws IOException, SAXException, SmooksException {
        System.out.println("\n\n==============Message In==============");
        System.out.println(new String(messageIn));
        System.out.println("======================================\n");

        List messageOut = Main.runSmooksTransform();

        System.out.println("==============Message Out=============");
        System.out.println(messageOut);
        System.out.println("======================================\n\n");
    }

    private static String readInputMessage() {
        try {
            return StreamUtils.readStreamAsString(new FileInputStream("input-message.csv"));
        } catch (IOException e) {
            e.printStackTrace();
            return "<no-message/>";
        }
    }
}
