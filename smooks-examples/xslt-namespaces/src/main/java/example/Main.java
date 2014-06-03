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
import org.milyn.io.StreamUtils;
import org.milyn.container.ExecutionContext;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Simple example main class.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Main {

    private static ClassLoader loader = Thread.currentThread().getContextClassLoader() == null ? Thread.currentThread().getContextClassLoader() : Main.class.getClassLoader();

    private static byte[] messageIn = readInputMessage();

    protected static String runSmooksTransform() throws IOException, SAXException, SmooksException, URISyntaxException {

        // Instantiate Smooks with the config...
        URL smooksConfig = loader.getResource("smooks-config.xml");
        Smooks smooks = new Smooks(smooksConfig.toURI().toString());

        try {
             // Create an exec context - no profiles....
            ExecutionContext executionContext = smooks.createExecutionContext();
            CharArrayWriter outputWriter = new CharArrayWriter();

            // Configure the execution context to generate a report...
            executionContext.setEventListener(new HtmlReportGenerator("target/report/report.html"));

            // Filter the input message to the outputWriter, using the execution context...
            smooks.filterSource(executionContext, new StreamSource(new ByteArrayInputStream(messageIn)), new StreamResult(outputWriter));

            return outputWriter.toString();
        } finally {
            smooks.close();
        }
    }

    public static void main(String[] args) throws IOException, SAXException, SmooksException, URISyntaxException {
        System.out.println("\n\n==============Message In==============");
        System.out.println(new String(messageIn));
        System.out.println("======================================\n");

        String messageOut = Main.runSmooksTransform();

        System.out.println("==============Message Out=============");
        System.out.println(messageOut);
        System.out.println("======================================\n\n");
    }

    private static byte[] readInputMessage() {
        try {
            return StreamUtils.readStream(loader.getResourceAsStream("input-message.xml"));
        } catch (IOException e) {
            e.printStackTrace();
            return "<no-message/>".getBytes();
        }
    }
}
