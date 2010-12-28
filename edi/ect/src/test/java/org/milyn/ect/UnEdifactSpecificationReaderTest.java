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
package org.milyn.ect;

import junit.framework.TestCase;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.milyn.ect.formats.unedifact.UnEdifactSpecificationReader;
import org.milyn.edisax.EDIConfigurationException;
import org.milyn.edisax.EDIParser;
import org.milyn.edisax.model.internal.Edimap;
import org.milyn.io.StreamUtils;
import org.milyn.util.ClassUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Set;
import java.util.zip.ZipInputStream;

/**
 * UnEdifactSpecificationReaderTest.
 * 
 * @author bardl
 */
public class UnEdifactSpecificationReaderTest extends TestCase {

    public void _disabled_test_D08A_Messages() throws InstantiationException, IllegalAccessException, IOException, EdiParseException {

        InputStream inputStream = getClass().getResourceAsStream("D08A.zip");
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);

        EdiSpecificationReader ediSpecificationReader = new UnEdifactSpecificationReader(zipInputStream, false);

        test("BANSTA", ediSpecificationReader);
        test("CASRES", ediSpecificationReader);
        test("INVOIC", ediSpecificationReader);
        test("PAYMUL", ediSpecificationReader);
        test("TPFREP", ediSpecificationReader);
    }

    public void test_getMessages() throws InstantiationException, IllegalAccessException, IOException {
        InputStream inputStream = getClass().getResourceAsStream("D08A.zip");
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);

        EdiSpecificationReader ediSpecificationReader = new UnEdifactSpecificationReader(zipInputStream, false);

        Set<String> messages = ediSpecificationReader.getMessageNames();
        for(String message : messages) {
            Edimap model = ediSpecificationReader.getMappingModel(message);
            StringWriter writer = new StringWriter();
            model.write(writer);
        }
    }

    public void test_D08A_Segments() throws InstantiationException, IllegalAccessException, IOException, EdiParseException, ParserConfigurationException, SAXException {

        InputStream inputStream = getClass().getResourceAsStream("D08A.zip");
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);

        UnEdifactSpecificationReader ediSpecificationReader = new UnEdifactSpecificationReader(zipInputStream, false);
        Edimap edimap = ediSpecificationReader.getDefinitionModel();

        StringWriter stringWriter = new StringWriter();
        edimap.write(stringWriter);

        String result = stringWriter.toString();

        testSegment("BGM", result);
        testSegment("DTM", result);
        testSegment("NAD", result);
        testSegment("PRI", result);        
    }

    public void testRealLifeInputFilesD08A() throws IOException, InstantiationException, IllegalAccessException, EDIConfigurationException, SAXException {
        InputStream inputStream = ClassUtil.getResourceAsStream("D08A.zip", this.getClass());
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);

        EdiSpecificationReader ediSpecificationReader = new UnEdifactSpecificationReader(zipInputStream, false);

        //Test INVOIC
        String mappingModel = getEdiMessageAsString(ediSpecificationReader, "INVOIC");
        testPackage("d96a-invoic-1", mappingModel);
    }

    public void testRealLifeInputFilesD93A() throws IOException, InstantiationException, IllegalAccessException, EDIConfigurationException, SAXException {
        InputStream inputStream = ClassUtil.getResourceAsStream("d93a.zip", this.getClass());
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);

        EdiSpecificationReader ediSpecificationReader = new UnEdifactSpecificationReader(zipInputStream, false);

        //Test INVOIC
        String mappingModel = getEdiMessageAsString(ediSpecificationReader, "INVOIC");
        testPackage("d93a-invoic-1", mappingModel);
    }

    public void testPackage(String packageName, String mappingModel) throws IOException, InstantiationException, IllegalAccessException, SAXException, EDIConfigurationException {
        InputStream testFileInputStream = getClass().getResourceAsStream("testfiles/" + packageName + "/input.edi");

        MockContentHandler contentHandler = new MockContentHandler();
        EDIParser parser = new EDIParser();
        parser.setContentHandler(contentHandler);
        parser.setMappingModel(EDIParser.parseMappingModel(new StringReader(mappingModel)));
        parser.parse(new InputSource(testFileInputStream));

        String expected = new String(StreamUtils.readStream(getClass().getResourceAsStream("testfiles/" + packageName + "/expected-result.txt"))).trim();

        String result = removeCRLF(contentHandler.xmlMapping.toString().trim());
		expected = removeCRLF(expected);

        if(!result.equals(expected)) {
            System.out.println("Expected: \n[" + expected + "]");
            System.out.println("Actual: \n[" + result + "]");
            assertEquals("Message [" + packageName + "] failed.", expected, result);
        }
    }

    private String getEdiMessageAsString(EdiSpecificationReader ediSpecificationReader, String messageType) throws IllegalAccessException, InstantiationException, IOException {
        Edimap edimap = ediSpecificationReader.getMappingModel(messageType);
        StringWriter sw = new StringWriter();
        edimap.write(sw);
        return sw.toString();
    }

    private void testSegment(String segmentCode, String definitions) throws IOException {
        String expected = new String(StreamUtils.readStream(getClass().getResourceAsStream("d08a/segment/expected-" + segmentCode.toLowerCase() + ".xml"))).trim();
        String result = removeCRLF(definitions);
        expected = removeCRLF(expected);

        if(!result.contains(expected)) {
            System.out.println("Expected: \n[" + expected + "]");
            System.out.println("Actual: \n[" + result + "]");
        }

//        XMLUnit.setIgnoreWhitespace( true );
//        try {
//            XMLAssert.assertXMLEqual(new StringReader(expected), new StringReader(definitions));
//        } catch (SAXException e) {
//            e.printStackTrace();
//            fail(e.getMessage());
//        }
    }
    
    private void test(String messageName, EdiSpecificationReader ediSpecificationReader) throws IOException {
    	Edimap edimap = ediSpecificationReader.getMappingModel(messageName);

        StringWriter stringWriter = new StringWriter();
        edimap.write(stringWriter);
//		String expected = new String(StreamUtils.readStream(getClass().getResourceAsStream("d08a/message/expected-" + messageName.toLowerCase() + ".xml"))).trim();
//
//        String result = removeCRLF(stringWriter.toString());
//		expected = removeCRLF(expected);
//
//        if(!result.equals(expected)) {
//            System.out.println("Expected: \n[" + expected + "]");
//            System.out.println("Actual: \n[" + result + "]");
//            assertEquals("Message [" + messageName + "] failed.", expected, result);
//        }

        StringWriter result = new StringWriter();
        edimap.write(result);
		String expected = new String(StreamUtils.readStream(getClass().getResourceAsStream("d08a/message/expected-" + messageName.toLowerCase() + ".xml"))).trim();


        System.out.println(result);
        XMLUnit.setIgnoreWhitespace( true );
        try {
            XMLAssert.assertXMLEqual(new StringReader(expected), new StringReader(result.toString()));
        } catch (SAXException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private String removeCRLF(String string) throws IOException {
        return string.replaceAll("\r","").replaceAll("\n", "").replaceAll("\t", "").replaceAll(" ", "");
	}

    /************************************************************************
     * Private class MockContentHandler                                     *
     ************************************************************************/    
    private class MockContentHandler extends DefaultHandler {

        protected StringBuffer xmlMapping = new StringBuffer();

        public void startDocument() throws SAXException {
            xmlMapping.setLength(0);
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            xmlMapping.append(ch, start, length);
        }

        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            xmlMapping.append("<").append(localName).append(">");
        }

        public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
            xmlMapping.append("</").append(localName).append(">");
        }
    }
}
