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

import org.milyn.SmooksException;
import org.milyn.edi.unedifact.d93a.D93AInterchangeFactory;
import org.milyn.edi.unedifact.d93a.INVOIC.Invoic;
import org.milyn.smooks.edi.unedifact.model.UNEdifactInterchange;
import org.milyn.smooks.edi.unedifact.model.r41.UNEdifactInterchange41;
import org.milyn.smooks.edi.unedifact.model.r41.UNEdifactMessage41;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * Sample code showing how to read and write a UN/EDIFACT Interchange using
 * the EJC generated Java classes.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Main {

    public static void main(String[] args) throws IOException, SAXException, SmooksException {
        // Create an instance of the EJC generated factory class... cache this and reuse !!!
        D93AInterchangeFactory factory = D93AInterchangeFactory.getInstance();

        // Deserialize the UN/EDIFACT interchange stream to Java...
        InputStream stream = new FileInputStream("INVOIC.edi");

        /*------------------------------------------
        Read the interchange to Java Objects...
        -------------------------------------------*/
        UNEdifactInterchange interchange;
        try {
            interchange = factory.fromUNEdifact(stream);

            // Need to test which interchange syntax version.  Supports v4.1 at the moment...
            if (interchange instanceof UNEdifactInterchange41) {
                UNEdifactInterchange41 interchange41 = (UNEdifactInterchange41) interchange;

                System.out.println("\nJava Object Values:");
                System.out.println("\tInterchange Sender ID: " + interchange41.getInterchangeHeader().getSender().getId());

                for (UNEdifactMessage41 messageWithControlSegments : interchange41.getMessages()) {
                    // Process the messages...

                    System.out.println("\tMessage Name: " + messageWithControlSegments.getMessageHeader().getMessageIdentifier().getId());

                    Object messageObj = messageWithControlSegments.getMessage();
                    if (messageObj instanceof Invoic) {
                        Invoic invoice = (Invoic) messageObj;

                        System.out.println("\tParty Name: " + invoice.getSegmentGroup2().get(0).getNameAndAddress().getPartyName().getPartyName1());
                    }
                }
            }
        } finally {
            stream.close();
        }

        /*-----------------------------------
        Write interchange to Stdout...
        ------------------------------------*/
        StringWriter ediOutStream = new StringWriter();

        factory.toUNEdifact(interchange, ediOutStream);

        System.out.println("\n\nSerialized Interchanged:");
        System.out.println("\t" + ediOutStream);

        System.out.println("\n\n**** RUN INSIDE YOUR IDE... Set a breakpoint in the example.Main Class... inspect values etc !!\n");
    }
}
