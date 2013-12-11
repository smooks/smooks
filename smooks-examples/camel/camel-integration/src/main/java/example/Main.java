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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.milyn.commons.io.StreamUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Simple example main class.
 * 
 * @author Daniel Bevenius
 * 
 */
public class Main
{
    public static void main(String... args) throws Exception
    {
        printStartMessage();
        CamelContext camelContext = configureAndStartCamel(getDSLType(args));
        camelContext.stop();
        printEndMessage();
    }

    private static String getDSLType(String... args)
    {
        if (args.length > 0)
            return args[0];
        else
            return "JavaDSL";
    }

    private static String readInputMessage()
    {
        try
        {
            byte[] bytes = StreamUtils.readStream(new FileInputStream("input-message.xml"));
            return new String(bytes);
        } catch (IOException e)
        {
            e.printStackTrace();
            return "<no-message/>";
        }
    }

    private static void printStartMessage()
    {
        String payload = readInputMessage();
        System.out.println("\n\n==============Message In==============");
        System.out.println(payload);
        System.out.println("======================================\n");
        pause("The example xml can be seen above.  Press 'enter' have this");
    }

    private static void pause(String message)
    {
        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("> " + message);
            in.readLine();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        System.out.println("\n");
    }

    private static CamelContext configureAndStartCamel(String type) throws Exception
    {
        CamelContext camelContext;
        if ("SpringDSL".equals(type))
        {
            ApplicationContext springContext = new ClassPathXmlApplicationContext("camel-context-test.xml");
            camelContext = (CamelContext) springContext.getBean("camelContext");
        } else
        {
            camelContext = new DefaultCamelContext();
            camelContext.addComponent("jms", camelContext.getComponent("mock"));
            camelContext.addRoutes(new ExampleRouteBuilder());
            camelContext.start();
        }

        Thread.sleep(3000);
        return camelContext;
    }

    private static void printEndMessage()
    {
        System.out.println("\n\n");
        pause("And that's it!  Press 'enter' to finish...");
    }

}
