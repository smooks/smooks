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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.transform.stream.StreamSource;

import org.milyn.Smooks;
import org.milyn.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.event.report.HtmlReportGenerator;
import org.milyn.routing.db.StatementExec;
import org.milyn.io.StreamUtils;
import org.milyn.util.HsqlServer;
import org.xml.sax.SAXException;

/**
 * Simple example main class.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Main {

    private HsqlServer dbServer;

    public static byte[] messageIn = readInputMessage();

    public static void main(String[] args) throws Exception {
        Main main = new Main();

        System.out.println("\n\nThis sample will use Smooks to extract data from an EDI message an load it into a Database (Hypersonic)\n");
        Main.pause("Press return to see the sample EDI message...");

        System.out.println("\n" + new String(messageIn) + "\n");

        Main.pause("Press return to start the database...");
        main.startDatabase();
        try {
            System.out.println();
            Main.pause("The database is started now. Press return to see its contents.  It should be empty...");
            main.printOrders();
            System.out.println();
            Main.pause("Now press return to execute Smooks over the EDI message to load the database...");
            main.runSmooksTransform();
            System.out.println();
            Main.pause("Smooks has processed the message.  Now press return to view the contents of the database again.  This time there should be rows...");
            main.printOrders();
            System.out.println();
            Main.pause("And that's it! Press return exit...");
        } finally {
            main.stopDatabase();
        }
    }

    protected void runSmooksTransform() throws IOException, SAXException, SmooksException {
    	Locale defaultLocale = Locale.getDefault();

    	
    	Smooks smooks = new Smooks("./smooks-configs/smooks-config.xml");

        try {
            ExecutionContext executionContext = smooks.createExecutionContext();

            // Configure the execution context to generate a report...
            executionContext.setEventListener(new HtmlReportGenerator("target/report/report.html"));

            smooks.filterSource(executionContext, new StreamSource(new ByteArrayInputStream(messageIn)), null);

            Locale.setDefault(defaultLocale);
        } finally {
            smooks.close();
        }
    }

    public void printOrders() throws SQLException {
        List<Map<String, Object>> orders = getOrders();
        List<Map<String, Object>> orderItems = getOrderItems();

        printResultSet("Orders", orders);
        printResultSet("Order Items", orderItems);
    }

    public List<Map<String, Object>> getOrders() throws SQLException {
        StatementExec execOrders = new StatementExec("select * from ORDERS");
        List<Map<String, Object>> orders = execOrders.executeUnjoinedQuery(dbServer.getConnection());
        return orders;
    }

    public List<Map<String, Object>> getOrderItems() throws SQLException {
        StatementExec exec1OrderItems = new StatementExec("select * from ORDERITEMS");
        List<Map<String, Object>> orderItems = exec1OrderItems.executeUnjoinedQuery(dbServer.getConnection());
        return orderItems;
    }

    private void printResultSet(String name, List<Map<String, Object>> resultSet) {
        System.out.println(("---- " + name + " -------------------------------------------------------------------------------------------------").substring(0, 80));
        if(resultSet.isEmpty()) {
            System.out.println("(No rows)");
        } else {
            for(int i = 0; i < resultSet.size(); i++) {
                Set<Map.Entry<String, Object>> row = resultSet.get(i).entrySet();

                System.out.println("Row " + i + ":");
                for (Map.Entry<String, Object> field : row) {
                    System.out.println("\t" + field.getKey() + ":\t" + field.getValue());
                }
            }
        }
        System.out.println(("---------------------------------------------------------------------------------------------------------------------").substring(0, 80));
    }

    public void startDatabase() throws Exception {
        InputStream schema = new FileInputStream("db-create.script");

        try {
            dbServer = new HsqlServer(9201);
            dbServer.execScript(schema);
        } finally {
            schema.close();
        }
    }

    void stopDatabase() throws Exception {
        dbServer.stop();
    }

    private static byte[] readInputMessage() {
        try {
            return StreamUtils.readStream(new FileInputStream("input-message.edi"));
        } catch (IOException e) {
            e.printStackTrace();
            return "<no-message/>".getBytes();
        }
    }

    static void pause(String message) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("> " + message);
            in.readLine();
        } catch (IOException e) {
        }
        System.out.println("\n");
    }
}
