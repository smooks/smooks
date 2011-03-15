/*
	Milyn - Copyright (C) 2006 - 2011

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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Set;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.junit.BeforeClass;
import org.junit.Test;
import org.milyn.ect.formats.unedifact.UnEdifactSpecificationReader;
import org.milyn.edisax.EDIConfigurationException;
import org.milyn.edisax.EDIParser;
import org.milyn.edisax.model.internal.Edimap;
import org.milyn.edisax.unedifact.handlers.r41.UNEdifact41ControlBlockHandlerFactory;
import org.milyn.io.StreamUtils;
import org.milyn.util.ClassUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * UnEdifactSpecificationReaderTest.
 * 
 * @author bardl
 */
public class UnEdifactSpecificationReaderTest  {

	private static UnEdifactSpecificationReader d08AReader;
	private XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());;

	@BeforeClass
	public static void parseD08A() throws Exception {
        InputStream inputStream = UnEdifactSpecificationReaderTest.class.getResourceAsStream("D08A.zip");
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        d08AReader = new UnEdifactSpecificationReader(zipInputStream, false);
	}
	
    public void _disabled_test_D08A_Messages() throws InstantiationException, IllegalAccessException, IOException, EdiParseException {
        test("BANSTA", d08AReader);
        test("CASRES", d08AReader);
        test("INVOIC", d08AReader);
        test("PAYMUL", d08AReader);
        test("TPFREP", d08AReader);
    }

    @Test
    public void test_getMessages() throws InstantiationException, IllegalAccessException, IOException {
        Set<String> messages = d08AReader.getMessageNames();
        for(String message : messages) {
            Edimap model = d08AReader.getMappingModel(message);
            StringWriter writer = new StringWriter();
            model.write(writer);
        }
    }

    @Test
    public void test_D08A_Segments() throws InstantiationException, IllegalAccessException, IOException, EdiParseException, ParserConfigurationException, SAXException, JDOMException {

        Edimap edimap = d08AReader.getDefinitionModel();

        StringWriter stringWriter = new StringWriter();
        edimap.write(stringWriter);

        Document document = new SAXBuilder().build(new StringReader(stringWriter.toString()));
        
        testSegment("BGM", document);
        testSegment("DTM", document);
        testSegment("NAD", document);
        testSegment("PRI", document);        
    }

    @Test
    public void testRealLifeInputFilesD08A() throws IOException, InstantiationException, IllegalAccessException, EDIConfigurationException, SAXException {
        //Test INVOIC
        String mappingModel = getEdiMessageAsString(d08AReader, "INVOIC");
        testPackage("d96a-invoic-1", mappingModel);
    }

    @Test
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
        parser.setNamespaceResolver(UNEdifact41ControlBlockHandlerFactory.new41NamespaceResolver());
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

	private void testSegment(final String segmentCode, Document doc) throws IOException, SAXException, JDOMException {
        String expected = new String(StreamUtils.readStream(getClass().getResourceAsStream("d08a/segment/expected-" + segmentCode.toLowerCase() + ".xml"))).trim();
        XPath lookup = XPath.newInstance("//medi:segment[@segcode='" + segmentCode + "']");
        lookup.addNamespace("medi", "http://www.milyn.org/schema/edi-message-mapping-1.5.xsd");
        Element node = (Element) lookup.selectSingleNode(doc);
        assertNotNull("Node with segment code " + segmentCode + " wasn't found", node);
        XMLUnit.setIgnoreWhitespace( true );
        XMLUnit.setIgnoreAttributeOrder(true);
    	XMLAssert.assertXMLEqual("Failed to compare XMLs for " + segmentCode, new StringReader(expected), new StringReader(out.outputString(node)));
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
