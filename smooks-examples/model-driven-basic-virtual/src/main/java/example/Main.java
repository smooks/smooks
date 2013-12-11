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
import org.milyn.commons.SmooksException;
import org.milyn.payload.StringResult;
import org.milyn.event.report.HtmlReportGenerator;
import org.milyn.container.ExecutionContext;
import org.milyn.payload.StringSource;
import org.milyn.commons.io.StreamUtils;
import org.xml.sax.SAXException;

import java.io.*;

/**
 * Simple example main class.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Main {

    public static byte[] inputMessage = readInputMessage("input-message.xml");
    private Smooks smooks;

    protected Main() throws IOException, SAXException {
        smooks = new Smooks("smooks-config.xml");
    }

    /**
     * Run the transform for the request or response.
     * @param message The request/response input message.
     * @return The transformed request/response.
     */
    protected String runSmooksTransform(byte[] message) throws IOException {
        try {
            // Create an exec context for the target profile....
            ExecutionContext executionContext = smooks.createExecutionContext();
            StringSource stringSource = new StringSource(new String(message));
            StringResult stringResult = new StringResult();

            // Configure the execution context to generate a report...
            executionContext.setEventListener(new HtmlReportGenerator("target/report/report.html"));

            // Filter the message to the outputWriter, using the execution context...
            smooks.filterSource(executionContext, stringSource, stringResult);

            return stringResult.toString();
        } finally {
            smooks.close();
        }
    }

    public static void main(String[] args) throws IOException, SAXException, SmooksException {
        Main smooksMain = new Main();
        String transResult;

        pause("Press 'enter' to display the input message...");
        System.out.println("\n");
        System.out.println(new String(inputMessage));
        System.out.println("\n\n");

        System.out.println("This needs to be transformed.");
        pause("Press 'enter' to display the transformed message...");
        transResult = smooksMain.runSmooksTransform(inputMessage);
        System.out.println("\n");
        System.out.println(transResult);
        System.out.println("\n\n");

        pause("And that's it!");
        System.out.println("\n\n");
    }

    private static byte[] readInputMessage(String messageFile) {
        try {
            return StreamUtils.readStream(new FileInputStream(messageFile));
        } catch (IOException e) {
            e.printStackTrace();
            return "<no-message/>".getBytes();
        }
    }

    private static void pause(String message) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("> " + message);
            in.readLine();
        } catch (IOException e) {
        }
        System.out.println("\n");
    }
}
