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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

import javax.xml.transform.stream.StreamSource;

import org.smooks.Smooks;
import org.smooks.SmooksException;
import org.smooks.container.ExecutionContext;
import org.smooks.event.report.HtmlReportGenerator;
import org.smooks.io.StreamUtils;
import org.smooks.payload.StringResult;
import org.xml.sax.SAXException;

/**
 * Simple example main class.
 *
 * @author <a href="mailto:maurice@zeijen.net">maurice@zeijen.net</a>
 */
public class Main {

    private static byte[] messageIn = readInputMessage();

    private final Smooks smooks;

    protected Main() throws IOException, SAXException {
        // Instantiate Smooks with the config...
        smooks = new Smooks("smooks-config.xml");
    }

    protected String runSmooksTransform(ExecutionContext executionContext) throws IOException, SAXException, SmooksException {
        try {
            Locale defaultLocale = Locale.getDefault();
            Locale.setDefault(new Locale("en", "IE"));

            StringResult result = new StringResult();

            // Configure the execution context to generate a report...
            executionContext.setEventListener(new HtmlReportGenerator("target/report/report.html"));

            // Filter the input message to the outputWriter, using the execution context...
            smooks.filterSource(executionContext, new StreamSource(new ByteArrayInputStream(messageIn)), result);

            Locale.setDefault(defaultLocale);

            return result.toString();
        } finally {
            smooks.close();
        }
    }

    public static void main(String[] args) throws IOException, SAXException, SmooksException {
        System.out.println("\n\n==============Message In==============");
        System.out.println(new String(messageIn));
        System.out.println("======================================\n");

        pause("The JSON input stream can be seen above.  Press 'enter' to see how this stream is transformed into DOM representation...");

        Main smooksMain = new Main();
        ExecutionContext executionContext = smooksMain.smooks.createExecutionContext();
        System.out.println("==============JSON as XML=============");
        System.out.println(smooksMain.runSmooksTransform(executionContext));
        System.out.println("======================================\n\n");

        pause("Now press 'enter' to see how this XML loads into the Order Object graph...");

        System.out.println("==============JSON as Java Object Graph=============");
        System.out.println(executionContext.getBeanContext().getBean("order"));
        System.out.println("======================================\n\n");

        pause("And that's it!  Press 'enter' to finish...");
    }

    private static byte[] readInputMessage() {
        try {
            return StreamUtils.readStream(new FileInputStream("input-message.jsn"));
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

    public String runSmooksTransform() throws IOException, SAXException {
        ExecutionContext executionContext = smooks.createExecutionContext();
        return runSmooksTransform(executionContext);
    }
}
