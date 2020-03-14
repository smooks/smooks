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
import org.smooks.container.ExecutionContext;
import org.smooks.io.StreamUtils;
import org.smooks.payload.ByteSource;
import org.xml.sax.SAXException;

import java.io.*;

/**
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 *
 */
public class Main
{
    private static byte[] messageIn = readInputMessage();

    public static void main(String[] args) throws IOException, SAXException, SmooksException, InterruptedException
    {
        pause("Press 'enter' to display the input xml Order message...");
        System.out.println("\n");
        System.out.println( new String( messageIn ) );
        System.out.println("\n\n");

        pause("Press 'enter' to split the Order message and route the Order Items (plus header info) to the JMS Queue...");

        Smooks smooks = new Smooks("smooks-config.xml");
        try {
            ExecutionContext execContext = smooks.createExecutionContext();

            execContext.setEventListener(new HtmlReportGenerator("target/report.html"));
            smooks.filterSource(execContext, new ByteSource(messageIn), null);
        } finally {
            smooks.close();
        }

        System.out.println("\n\n");
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

    private static byte[] readInputMessage()
    {
        try {
            return StreamUtils.readStream(new FileInputStream("input-message.xml"));
        }
        catch (IOException e) {
            e.printStackTrace();
            return "<no-message/>".getBytes();
        }
    }

}