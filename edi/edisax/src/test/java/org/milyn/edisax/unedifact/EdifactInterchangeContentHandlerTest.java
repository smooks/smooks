package org.milyn.edisax.unedifact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.milyn.edisax.EDIParser;
import org.milyn.edisax.model.EdifactModel;
import org.milyn.edisax.model.internal.Delimiters;
import org.milyn.edisax.registry.DefaultMappingsRegistry;
import org.milyn.edisax.registry.MappingsRegistry;
import org.milyn.io.StreamUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.net.URL;

/**
 * Test cases covering EdifactInterchangeContentHandler.
 */
public class EdifactInterchangeContentHandlerTest {
    private static final Delimiters DELIMITERS_WITHOUT_NEWLINE = new Delimiters().setSegment("'").setField("+").setComponent(":").setEscape("?").setDecimalSeparator(".");
    private static final Delimiters DELIMITERS_WITH_NEWLINE = new Delimiters().setSegment("'\n").setField("+").setComponent(":").setEscape("?").setDecimalSeparator(".");

    private MappingsRegistry registry;

    @Before
    public void setUp() throws IOException, SAXException {
        EdifactModel model1 = EDIParser.parseMappingModel(getClass().getResourceAsStream("MSG1-model.xml"));
        EdifactModel model2 = EDIParser.parseMappingModel(getClass().getResourceAsStream("MSG2-model.xml"));

        registry = new DefaultMappingsRegistry(model1, model2);
    }


    @Test
    public void noUng() throws IOException, SAXException {
        URL xmlInput = getClass().getResource("no_ung/unedifact-msg-expected.xml");
        String expectedEdi = readResourceAsString("no_ung/unedifact-msg-01.edi");
        assertNotNull(xmlInput);
        test(registry, xmlInput, expectedEdi);
    }

    @Test
    public void withUngWithGroupRef() throws IOException, SAXException {
        URL xmlInput = getClass().getResource("with_ung/unedifact-msg-expected-01.xml");
        String expectedEdi = readResourceAsString("with_ung/unedifact-msg-01.edi");
        expectedEdi = expectedEdi.replace("AAAA+f1+f2'BB+f11'", "AAAA+f1+f2'\nBB+f11'");
        assertNotNull(xmlInput);
        test(registry, xmlInput, expectedEdi);
    }

    @Test
    public void withUngWithFullGroupHeader() throws IOException, SAXException {
        URL xmlInput = getClass().getResource("with_ung/unedifact-msg-expected-02.xml");
        String expectedEdi = readResourceAsString("with_ung/unedifact-msg-04.edi");
        assertNotNull(xmlInput);
        test(registry, xmlInput, expectedEdi);
    }

    @Test
    public void withUngWithUnknownUcd() throws IOException, SAXException {
        URL xmlInput = getClass().getResource("with_ung/unedifact-msg-expected-03.xml");
        String expectedEdi = readResourceAsString("with_ung/unedifact-msg-05.edi");
        assertNotNull(xmlInput);
        test(registry, xmlInput, expectedEdi);
    }

    private void test(MappingsRegistry registry, URL xmlInput, String expectedEdi) throws SAXException, IOException {
        StringBuilder result = new StringBuilder();
        Delimiters delimiters = DELIMITERS_WITHOUT_NEWLINE;
        int offset = expectedEdi.indexOf(delimiters.getSegment());
        if (Character.isWhitespace(expectedEdi.charAt(offset + 1))) {
            delimiters = DELIMITERS_WITH_NEWLINE;
        }
        EdifactInterchangeContentHandler handler = new EdifactInterchangeContentHandler(registry, delimiters, result);
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.setContentHandler(handler);
        xmlReader.parse(new InputSource(xmlInput.toExternalForm()));
        assertEquals("XML back to EDI", expectedEdi, result.toString().trim());
    }

    private String readResourceAsString(String name) throws IOException {
        return StreamUtils
                .readStreamAsString(getClass().getResourceAsStream(name)).trim().replace("\r", "");
    }
}
