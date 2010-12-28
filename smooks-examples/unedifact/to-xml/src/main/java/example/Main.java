/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (version 2.1) as published by the Free Software
 * Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */
package example;

import org.milyn.Smooks;
import org.milyn.SmooksException;
import org.milyn.io.StreamUtils;
import org.milyn.smooks.edi.unedifact.UNEdifactReaderConfigurator;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Main class that uses a Smooks XML configuration to configure the UN/EDIFACT
 * reader.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Main {

    protected static String runSmooksTransform() throws IOException, SAXException, SmooksException {

        // Configure Smooks using a Smooks config...
        //Smooks smooks = new Smooks("smooks-config.xml");
        
        // Or, configure Smooks programmatically...
        Smooks smooks = new Smooks();
        smooks.setReaderConfig(new UNEdifactReaderConfigurator("urn:org.milyn.edi.unedifact:d03b-mapping:1.0-SNAPSHOT"));

        try {
            StringWriter writer = new StringWriter();

            smooks.filterSource(new StreamSource(new FileInputStream("PAXLST.edi")), new StreamResult(writer));

            return writer.toString();
        } finally {
            smooks.close();
        }
    }

    public static void main(String[] args) throws IOException, SAXException, SmooksException {
        System.out.println("\n\n==============Message In==============");
        System.out.println(readInputMessage());
        System.out.println("======================================\n");

        String messageOut = Main.runSmooksTransform();

        System.out.println("==============Message Out=============");
        System.out.println(messageOut);
        System.out.println("======================================\n\n");
    }

    private static String readInputMessage() throws IOException {
        return StreamUtils.readStreamAsString(new FileInputStream("PAXLST.edi"));
    }
}