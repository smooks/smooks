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

package org.milyn.ejc.util;

import junit.framework.TestCase;
import org.milyn.edisax.model.internal.Delimiters;
import org.milyn.edisax.unedifact.UNEdifactInterchangeParser;
import org.milyn.io.StreamUtils;
import org.milyn.smooks.edi.unedifact.model.UNEdifactInterchange;
import org.milyn.smooks.edi.unedifact.model.UNEdifactInterchangeFactory;
import org.milyn.smooks.edi.unedifact.model.r41.UNB41;
import org.milyn.smooks.edi.unedifact.model.r41.UNEdifactInterchange41;
import org.milyn.smooks.edi.unedifact.model.r41.UNEdifactMessage41;
import org.milyn.smooks.edi.unedifact.model.r41.types.MessageIdentifier;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.List;

/**
 * Interchange test utilities.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class InterchangeTestUtil {

    private static MessageBuilder defaultUNAMessageBuilder = new MessageBuilder("org.milyn", UNEdifactInterchangeParser.defaultUNEdifactDelimiters.getField(), UNEdifactInterchangeParser.defaultUNEdifactDelimiters);
    private static MessageBuilder commaDecimalSepUNAMessageBuilder = new MessageBuilder("org.milyn", UNEdifactInterchangeParser.defaultUNEdifactDelimiters.getField(), UNEdifactInterchangeParser.defaultUNEdifactDelimiters);

    static {
        Delimiters delimiters = ((Delimiters)UNEdifactInterchangeParser.defaultUNEdifactDelimiters.clone()).setDecimalSeparator(",");
        commaDecimalSepUNAMessageBuilder = new MessageBuilder("org.milyn", delimiters.getField(), delimiters);
    }

    public static void testJavaBinding(UNEdifactInterchangeFactory factory, String messageInFile, boolean dumpResult) throws IOException, SAXException {
        // Deserialize the a UN/EDIFACT interchange stream to Java...
        InputStream ediStream = InterchangeTestUtil.class.getResourceAsStream(messageInFile);

        if(ediStream == null) {
            throw new IOException("EDI input file '" + messageInFile + "' not on classpath."); 
        }

        UNEdifactInterchange interchange = factory.fromUNEdifact(ediStream);

        // Serialize it back to EDI....
        StringWriter writer = new StringWriter();
        factory.toUNEdifact(interchange, writer);

        if(dumpResult) {
            System.out.println(writer.toString());
        }

        // We expect the result to be the same as the input...
        String expected = StreamUtils.readStreamAsString(InterchangeTestUtil.class.getResourceAsStream(messageInFile));
        TestCase.assertEquals(StreamUtils.normalizeLines(expected, false), StreamUtils.normalizeLines(writer.toString(), false));
    }

    public static void test_Interchange(UNEdifactInterchangeFactory factory, boolean dump, Class<?>... messageTypes) throws IOException {
        UNEdifactInterchange41 interchange41 = buildInterchange(messageTypes);
        test_Interchange(factory, dump, interchange41);
    }

    public static void test_Interchange_Comma_Decimal(UNEdifactInterchangeFactory factory, boolean dump, Class<?>... messageTypes) throws IOException {
        UNEdifactInterchange41 interchange41 = buildInterchange(commaDecimalSepUNAMessageBuilder, messageTypes);
        test_Interchange(factory, dump, interchange41);
    }

    public static void test_Interchange(UNEdifactInterchangeFactory factory, boolean dump, UNEdifactInterchange41 interchange41) throws IOException {
        StringWriter writer = new StringWriter();

        // serialize it...
        factory.toUNEdifact(interchange41, writer);

        String messageV1 = writer.toString();

        // reconstruct from the serialized form...
        interchange41 = (UNEdifactInterchange41) factory.fromUNEdifact(new InputSource(new ByteArrayInputStream(messageV1.getBytes("UTF-8"))));

        // serialize again...
        writer.getBuffer().setLength(0);
        factory.toUNEdifact(interchange41, writer);

        String messageV2 = writer.toString();

        TestCase.assertEquals(messageV1, messageV2);

        if(dump) {
            System.out.println(messageV1);
        }
    }

    public static UNEdifactInterchange41 buildInterchange(Class<?>... messageTypes) {
        return buildInterchange(defaultUNAMessageBuilder, messageTypes);
    }

    public static UNEdifactInterchange41 buildInterchange(MessageBuilder builder, Class<?>... messageTypes) {
        return buildInterchange("D", "03B", messageTypes, builder);
    }

    public static UNEdifactInterchange41 buildInterchange(String versionNum, String releaseNum, Class<?>[] messageTypes, MessageBuilder messageBuilder) {
        UNEdifactInterchange41 interchange41 = messageBuilder.buildMessage(UNEdifactInterchange41.class);
        UNB41 unb = interchange41.getInterchangeHeader();
        List<UNEdifactMessage41> messages = interchange41.getMessages();

        interchange41.setInterchangeDelimiters(messageBuilder.getDelimiters());
        unb.getSyntaxIdentifier().setId("UNOW"); // UNOW is UTF-8.... as encoded above
        unb.getSyntaxIdentifier().setCodedCharacterEncoding("UNOW"); // UNOW is UTF-8.... as encoded above
        messages.clear();

        for(Class<?> messageType : messageTypes) {
            UNEdifactMessage41 message41 = messageBuilder.buildMessage(UNEdifactMessage41.class);
            Object messageInstance = messageBuilder.buildMessage(messageType);

            MessageIdentifier messageIdentifier = message41.getMessageHeader().getMessageIdentifier();
            messageIdentifier.setControllingAgencyCode("UN");
            messageIdentifier.setId(messageType.getSimpleName().toUpperCase());
            messageIdentifier.setVersionNum(versionNum);
            messageIdentifier.setReleaseNum(releaseNum);
            message41.setMessage(messageInstance);
            messages.add(message41);
        }

        return interchange41;
    }

    public static void test_loads(UNEdifactInterchangeFactory factory, boolean dump, String ejcClassListFile, int numMessages) throws IOException, ClassNotFoundException {
        InputStream stream = InterchangeTestUtil.class.getResourceAsStream(ejcClassListFile);

        if(stream == null) {
            TestCase.fail("Unable to load EJC list file '" + ejcClassListFile + "' from classpath.");
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            String ejcClassName = reader.readLine();
            int i = 1;

            long start = System.currentTimeMillis();
            while(ejcClassName != null) {
                //System.out.println(i + ": " + ejcClassName);
                test_Interchange(factory, dump, Class.forName(ejcClassName));
                ejcClassName = reader.readLine();

                if(i == numMessages) {
                    break;
                }

                i++;
            }
            System.out.println("Took: " + (System.currentTimeMillis() - start));
        } finally {
            stream.close();
        }
    }
}
