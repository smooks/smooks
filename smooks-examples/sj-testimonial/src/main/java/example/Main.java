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
import org.milyn.payload.JavaResult;
import org.milyn.event.report.HtmlReportGenerator;
import org.milyn.container.ExecutionContext;
import org.milyn.io.StreamUtils;
import org.xml.sax.SAXException;
import se.sj.ipl.rollingstock.domain.RollingStockList;
import se.sj.ipl.rollingstock.domain.Rollingstock;
import se.sj.ipl.rollingstock.domain.Vehicle;

import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Simple example main class.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Main {

    public static byte[] messageIn = readInputMessage();

    protected static Map runSmooksTransform(String config) throws IOException, SAXException, SmooksException {
        // Instantiate Smooks with the config...
        Smooks smooks = new Smooks(config);

        try {
             // Create an exec context - no profiles....
            ExecutionContext executionContext = smooks.createExecutionContext();
            // The result of this transform is a set of Java objects...
            JavaResult result = new JavaResult();

            // Configure the execution context to generate a report...
            executionContext.setEventListener(new HtmlReportGenerator("target/report/report.html"));

            // Filter the input message to the outputWriter, using the execution context...
            smooks.filterSource(executionContext, new StreamSource(new ByteArrayInputStream(messageIn)), result);

            return result.getResultMap();
        } finally {
            smooks.close();
        }
    }

    public static void main(String[] args) throws IOException, SAXException, SmooksException {
        System.out.println("\n\n==============Message In==============");
        System.out.println(new String(messageIn));
        System.out.println("======================================\n");

        pause("The EDI input stream can be seen above.  Press 'enter' to see this stream transformed into the Java Object model...");

        Map beans = Main.runSmooksTransform("smooks-config-sax.xml");

        System.out.println("==============Message Out=============");
        
		RollingStockList rollingstocks = (RollingStockList) beans.get( "rollingstocks" );
		for( int i = 0; i < rollingstocks.size() ; i ++ )
		{
			Rollingstock rollingstock = rollingstocks.get( i );
			System.out.println( "" );
			System.out.println( "RollingstockId : " + rollingstock.getRollingstockId() );
			System.out.println( "Schedule : " + rollingstock.getSchedule() );
			List<Vehicle> vehicles = rollingstock.getVehicles();
			for ( int y = 0 ; y < vehicles.size() ; y ++ )
			{
				Vehicle vehicle = vehicles.get( y );
				System.out.println( "Vehicle : " + y  + ": " + vehicle );
			}
			System.out.println( "Route : " + rollingstock.getRoute() );
		}
		System.out.println( "======================================\n\n");

        pause("And that's it!  Press 'enter' to finish...");
    }

    private static byte[] readInputMessage() {
        try {
            return StreamUtils.readStream(new FileInputStream("input-message.edi"));
        } catch (IOException e) {
            e.printStackTrace();
            return "<no-message/>".getBytes();
        }
    }

    private static void pause(String message) {
        System.out.println("> " + message);
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            in.readLine();
        } catch (IOException e) {
        }
        System.out.println("\n");
    }
}
