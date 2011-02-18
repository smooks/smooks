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
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.camel.CamelContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Simple example main class.
 * 
 * @author Daniel Bevenius
 */
public class Main
{
    private static final String camelConfig = "META-INF/spring/camel-context.xml";

    public static void main(String... args) throws Exception
    {
        CamelContext camelContext = configureAndStartCamel(camelConfig);
        // Give Camel time to process the file.
        Thread.sleep(3000);
        camelContext.stop();
        printEndMessage();
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

    private static CamelContext configureAndStartCamel(String camelConfig) throws Exception
    {
        ApplicationContext springContext = new ClassPathXmlApplicationContext(camelConfig);
        return (CamelContext) springContext.getBean("camelContext");
    }

    private static void printEndMessage()
    {
        System.out.println("\n\n");
        pause("And that's it!  Press 'enter' to finish...");
    }

}
