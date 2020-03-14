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
package org.smooks.ect;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPath;
import org.junit.BeforeClass;
import org.junit.Test;
import org.smooks.ect.formats.unedifact.UnEdifactSpecificationReader;
import org.smooks.edisax.EDIConfigurationException;
import org.smooks.edisax.EDIParser;
import org.smooks.edisax.model.internal.Edimap;
import org.smooks.io.StreamUtils;
import org.smooks.util.ClassUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Set;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * UnEdifactSpecificationReaderTest.
 *
 * @author bardl
 */
@SuppressWarnings("deprecation")
public class UnEdifactSpecificationReaderTest {

	private static UnEdifactSpecificationReader d08AReader_longnames;
    private static UnEdifactSpecificationReader d08AReader_shortnames;
	private XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());

    @BeforeClass
	public static void parseD08A() throws Exception {
        InputStream inputStream = UnEdifactSpecificationReaderTest.class.getResourceAsStream("D08A.zip");
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        d08AReader_longnames = new UnEdifactSpecificationReader(zipInputStream, false, false);

        inputStream = UnEdifactSpecificationReaderTest.class.getResourceAsStream("D08A.zip");
        zipInputStream = new ZipInputStream(inputStream);
        d08AReader_shortnames = new UnEdifactSpecificationReader(zipInputStream, false, true);
	}

    public void _disabled_test_D08A_Messages() throws IOException, EdiParseException {
        test("BANSTA", d08AReader_longnames);
        test("CASRES", d08AReader_longnames);
        test("INVOIC", d08AReader_longnames);
        test("PAYMUL", d08AReader_longnames);
        test("TPFREP", d08AReader_longnames);
    }

    @Test
    public void test_getMessagesLongName() throws IOException {
        Set<String> messages = d08AReader_longnames.getMessageNames();
        for(String message : messages) {
            Edimap model = d08AReader_longnames.getMappingModel(message);
            StringWriter writer = new StringWriter();
            model.write(writer);
        }
    }

    @Test
    public void test_getMessagesShortName() throws IOException {
        Set<String> messages = d08AReader_shortnames.getMessageNames();
        for(String message : messages) {
            Edimap model = d08AReader_shortnames.getMappingModel(message);
            StringWriter writer = new StringWriter();
            model.write(writer);
        }
    }

    @Test
    public void test_D08A_SegmentsLongName() throws IOException, EdiParseException, SAXException, JDOMException {

        Edimap edimap = d08AReader_longnames.getDefinitionModel();

        StringWriter stringWriter = new StringWriter();
        edimap.write(stringWriter);

        Document document = new SAXBuilder().build(new StringReader(stringWriter.toString()));

        testSegment("BGM", document, false);
        testSegment("DTM", document, false);
        testSegment("NAD", document, false);
        testSegment("PRI", document, false);
    }

    @Test
    public void test_D08A_Segments_ShortName() throws IOException, EdiParseException, SAXException, JDOMException {

        Edimap edimap = d08AReader_shortnames.getDefinitionModel();

        StringWriter stringWriter = new StringWriter();
        edimap.write(stringWriter);

        Document document = new SAXBuilder().build(new StringReader(stringWriter.toString()));

        testSegment("BGM", document, true);
        testSegment("DTM", document, true);
        testSegment("NAD", document, true);
        testSegment("PRI", document, true);
    }

    @Test
    public void testRealLifeInputFilesD96ALongName() throws IOException, InstantiationException, IllegalAccessException, EDIConfigurationException, SAXException {
        //Test INVOIC
        String mappingModel = getEdiMessageAsString(d08AReader_longnames, "INVOIC");
        testPackage("d96a-invoic-1", mappingModel);
    }

    @Test
    public void testRealLifeInputFilesD96AShortName() throws IOException, InstantiationException, IllegalAccessException, EDIConfigurationException, SAXException {
        //Test INVOIC
        String mappingModel = getEdiMessageAsString(d08AReader_shortnames, "INVOIC");
        testPackage("d96a-invoic-shortname", mappingModel);
    }

    @Test
    public void testRealLifeInputFilesD93ALongName() throws IOException, InstantiationException, IllegalAccessException, EDIConfigurationException, SAXException {
        InputStream inputStream = ClassUtil.getResourceAsStream("d93a.zip", this.getClass());
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);

        EdiSpecificationReader ediSpecificationReader = new UnEdifactSpecificationReader(zipInputStream, false, false);

        //Test INVOIC
        String mappingModel = getEdiMessageAsString(ediSpecificationReader, "INVOIC");
        testPackage("d93a-invoic-1", mappingModel);
    }

    @Test
    public void testRealLifeInputFilesD93AShortName() throws IOException, InstantiationException, IllegalAccessException, EDIConfigurationException, SAXException {
        InputStream inputStream = ClassUtil.getResourceAsStream("d93a.zip", this.getClass());
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);

        EdiSpecificationReader ediSpecificationReader = new UnEdifactSpecificationReader(zipInputStream, false, true);

        //Test INVOIC
        String mappingModel = getEdiMessageAsString(ediSpecificationReader, "INVOIC");
        testPackage("d93a-invoic-shortname", mappingModel);
    }

    public void testPackage(String packageName, String mappingModel) throws IOException, SAXException, EDIConfigurationException {
        InputStream testFileInputStream = getClass().getResourceAsStream("testfiles/" + packageName + "/input.edi");

        MockContentHandler contentHandler = new MockContentHandler();
        EDIParser parser = new EDIParser();
        parser.setContentHandler(contentHandler);
        parser.setMappingModel(EDIParser.parseMappingModel(new StringReader(mappingModel)));
        parser.parse(new InputSource(testFileInputStream));

        String expected = new String(StreamUtils.readStream(getClass().getResourceAsStream("testfiles/" + packageName + "/expected-result.xml"))).trim();
        String actual = contentHandler.xmlMapping.toString();

//        System.out.println(actual);

        XMLUnit.setIgnoreWhitespace(true);
        try {
            XMLAssert.assertXMLEqual(new StringReader(expected), new StringReader(actual));
        } catch (SAXException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private String getEdiMessageAsString(EdiSpecificationReader ediSpecificationReader, String messageType) throws IOException {
        Edimap edimap = ediSpecificationReader.getMappingModel(messageType);
        StringWriter sw = new StringWriter();
        edimap.write(sw);
        return sw.toString();
    }

	private void testSegment(final String segmentCode, Document doc, boolean useShortName) throws IOException, SAXException, JDOMException {
        String expected = new String(StreamUtils.readStream(getClass().getResourceAsStream("d08a/segment/expected-" + (useShortName ? "shortname-" : "") + segmentCode.toLowerCase() + ".xml"))).trim();
        XPath lookup = XPath.newInstance("//medi:segment[@segcode='" + segmentCode + "']");
        lookup.addNamespace("medi", "http://www.milyn.org/schema/edi-message-mapping-1.5.xsd");
        Element node = (Element) lookup.selectSingleNode(doc);
        assertNotNull("Node with segment code " + segmentCode + " wasn't found", node);

//        System.out.println(out.outputString(node));

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


//        System.out.println(result);
        XMLUnit.setIgnoreWhitespace( true );
        try {
            XMLAssert.assertXMLEqual(new StringReader(expected), new StringReader(result.toString()));
        } catch (SAXException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /************************************************************************
     * Private class MockContentHandler                                     *
     ************************************************************************/
    private class MockContentHandler extends DefaultHandler {

        protected StringBuffer xmlMapping = new StringBuffer();

        public void startDocument()
        {
            xmlMapping.setLength(0);
        }

        public void characters(char[] ch, int start, int length)
        {
            xmlMapping.append(ch, start, length);
        }

        public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
        {
            xmlMapping.append("<").append(localName).append(">");
        }

        public void endElement(String namespaceURI, String localName, String qName)
        {
            xmlMapping.append("</").append(localName).append(">");
		}
	}
}
