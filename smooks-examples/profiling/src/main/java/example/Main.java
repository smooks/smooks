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
import org.smooks.event.report.HtmlReportGenerator;
import org.smooks.io.StreamUtils;
import org.smooks.container.ExecutionContext;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * Simple example main class.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Main {

    private static byte[] messageIn = readInputMessage();
    private Smooks smooks = null;

    protected Main() throws IOException, SAXException {
        // Instantiate Smooks with the config...
        smooks = new Smooks("smooks-config.xml");
    }

    protected String runSmooksTransform(String targetProfile) throws IOException {
        try {
            // Create an exec context for the target profile....
            ExecutionContext executionContext = smooks.createExecutionContext(targetProfile);
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

    public static void main(String[] args) throws IOException, SAXException, SmooksException {
        System.out.println("\n\n==============Message In==============");
        System.out.println(new String(messageIn));
        System.out.println("======================================\n");

        Main smooksMain = new Main();
        transformForProfile("message-exchange-1", smooksMain);
        transformForProfile("message-exchange-2", smooksMain);
        transformForProfile("message-exchange-3", smooksMain);
        transformForProfile("message-exchange-4", smooksMain);
        transformForProfile("message-exchange-5", smooksMain);
    }

    private static void transformForProfile(String targetProfile, Main smooksMain) throws IOException {
        readCommandPrompt(targetProfile);
        System.out.println("\n");
        System.out.println(smooksMain.runSmooksTransform(targetProfile));
        System.out.println("\n");
    }

    private static void readCommandPrompt(String targetProfile) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("> Press 'enter' to see the transform for target profile '" + targetProfile + "':");
            in.readLine();
        } catch (IOException e) {
        }
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
