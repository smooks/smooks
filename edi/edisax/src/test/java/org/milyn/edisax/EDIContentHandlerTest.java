package org.milyn.edisax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;
import org.milyn.edisax.model.EdifactModel;
import org.milyn.edisax.model.internal.Delimiters;
import org.milyn.io.StreamUtils;
import org.milyn.resource.URIResourceLocator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.io.StringReader;

/**
 * Test cases covering EDIContentHandler.
 */
public class EDIContentHandlerTest {

    @Test
    public void test_escape_character() throws IOException {
        test("test_escape_character");
    }

    @Test
    public void test_mapping_01() throws IOException {
        test("test01");
    }

    @Test
    public void test_mapping_02() throws IOException {
        test("test02");
    }

    @Test
    public void test_mapping_05() throws IOException {
        test("test05");
    }

    @Test
    public void test_mapping_06() throws IOException {
        test("test06");
    }

    @Test
    public void test_mapping_07() throws IOException {
        test("test07");
    }

    @Test
    public void test_mapping_08() throws IOException {
        test("test08");
    }

    @Test
    public void test_mapping_09() throws IOException {
        test("test09");
    }

    @Test
    public void test_mapping_15() throws IOException {
        test("test15");
    }

    // Tests Segment Truncation
    @Test
    public void test_MILYN_108_01() throws IOException {
        test("test-MILYN-108-01");
    }

    // Tests Segment Truncation
    @Test
    public void test_MILYN_108_02() throws IOException {
        test("test-MILYN-108-02");
    }

    // Tests Segment Truncation
    @Test
    public void test_MILYN_108_04() throws IOException {
        test("test-MILYN-108-04");
    }

    // Tests Component Truncation
    @Test
    public void test_MILYN_108_05() throws IOException {
        test("test-MILYN-108-05");
    }

    // Tests Component Truncation
    @Test
    public void test_MILYN_108_06() throws IOException {
        test("test-MILYN-108-06");
    }

    // Tests Component Truncation
    @Test
    public void test_MILYN_108_07() throws IOException {
        test("test-MILYN-108-07");

    }

    // Tests Field Truncation
    @Test
    public void test_MILYN_108_08() throws IOException {
        test("test-MILYN-108-08");
    }

    // Tests Field Truncation
    @Test
    public void test_MILYN_108_09() throws IOException {
        test("test-MILYN-108-09");
    }

    // Tests Field and Component Truncation
    @Test
    public void test_MILYN_108_10() throws IOException {
        String testpack = "test-MILYN-108-10";

        // Truncate trailing separator
        String expectedEdi = readExpectedEdifact(testpack);
        assertEquals('*', expectedEdi.charAt(expectedEdi.length() - 1));
        expectedEdi = expectedEdi.substring(0, expectedEdi.length() - 1);
        test(testpack, expectedEdi);
    }

    // Tests Field and Component Truncation
    @Test
    public void test_MILYN_108_11() throws IOException {
        test("test-MILYN-108-11");
    }

    @Test
    public void test_newlines_whitespaces() throws IOException {
        test("test_empty_rows/test_newlines_whitespaces");
    }

    @Test
    public void v11_groups_01() throws IOException {
        test("v1_1/groups/test_groups_01");
    }

    @Test
    public void v11_groups_02() throws IOException {
        test("v1_1/groups/test_groups_02");
    }

    @Test
    public void v11_groups_03() throws IOException {
        test("v1_1/groups/test_groups_03");
    }

    @Test
    public void v11_groups_04() throws IOException {
        test("v1_1/groups/test_groups_04");
    }

    @Test
    public void v11_groups_06() throws IOException {
        test("v1_1/groups/test_groups_06");
    }

    @Test
    public void v11_groups_07() throws IOException {
        test("v1_1/groups/test_groups_07");
    }

    @Test
    public void v11_groups_08() throws IOException {
        test("v1_1/groups/test_groups_08");
    }

    @Test
    public void v11_import_01() throws IOException {
        test("v1_1/imports/test_imports_01");
    }

    @Test
    public void v11_import_02() throws IOException {
        test("v1_1/imports/test_imports_02");
    }

    @Test
    public void v11_import_03() throws IOException {
        test("v1_1/imports/test_imports_03");
    }

    @Test
    @Ignore("SEG code pattern matching does not preserve the original SEG code")
    public void v11_patternmatching_01() throws IOException {
        test("v1_1/segcodePatternMatching/test_patternmatching_01");
    }

    @Test
    @Ignore("SEG code pattern matching does not preserve the original SEG code")
    public void v11_patternmatching_02() throws IOException {
        test("v1_1/segcodePatternMatching/test_patternmatching_02");
    }

    @Test
    public void v11_test_escape_01() throws IOException {
        String testpack = "v1_1/test_escape_01";

        // Fix bad ? escape in third segment
        String expectedEdi = readExpectedEdifact(testpack);
        expectedEdi = expectedEdi.replace("SEG*12?34*43?21**ab?cd*dc?ba", "SEG*12??34*43??21**ab??cd*dc??ba");
        test(testpack, expectedEdi);
    }

    @Test
    public void v14_fieldrepeat() throws IOException {
        test("v1_4/fieldrepeat");
    }

    @Test
    public void v14_ignoreUnmappedFields() throws IOException {
        String testpack = "v1_4/ignore_unmapped_fields";
        String expectedEdi = readExpectedEdifact(testpack);
        expectedEdi = expectedEdi
                .replace("|YYY|ZZZ", "")
                .replace("|YYY~YYY|ZZZ", "");
        test(testpack, expectedEdi);
    }

    @Test
    public void v17_componentMaxOccurs() throws IOException {
        test("v1_7/component_maxoccurs");
    }

    @Test
    public void v17_compositeMaxOccurs() throws IOException {
        test("v1_7/composite_maxoccurs");
    }

    @Test
    public void v17_fieldMaxOccurs() throws IOException {
        test("v1_7/field_maxoccurs");
    }

    private void test(String testpack) throws IOException {
        String expectedEdi = readExpectedEdifact(testpack);
        test(testpack, expectedEdi);
    }

    private void test(String testpack, String expectedEdi) throws IOException {
        String packageName = getClass().getPackage().getName().replace('.', '/');
        String mappingModel = "/" + packageName + "/" + testpack + "/edi-to-xml-mapping.xml";
        String xmlInput = readResourceAsString(testpack + "/expected.xml");

        try {
            EdifactModel edifactModel = EDIParser
                    .parseMappingModel(mappingModel, URIResourceLocator.extractBaseURI(mappingModel));
            fixSegmentDelimiter(expectedEdi, edifactModel.getDelimiters());

            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            StringBuilder result = new StringBuilder();
            xmlReader.setContentHandler(new EDIContentHandler(edifactModel, result));
            xmlReader.parse(new InputSource(new StringReader(xmlInput)));
            assertEquals("XML back to EDI", expectedEdi, result.toString().trim());
        } catch (SAXException e) {
            fail(e.getMessage() + "\n" + xmlInput);
        }
    }

    private String readExpectedEdifact(String testpack) throws IOException {
        return readResourceAsString(testpack + "/edi-input.txt");
    }

    private String readResourceAsString(String name) throws IOException {
        return StreamUtils
                    .readStreamAsString(getClass().getResourceAsStream(name)).trim().replace("\r", "");
    }

    // Add line breaks back to segment delimiters if the parsed input original had new lines
    private void fixSegmentDelimiter(String edifact, Delimiters delimiters) {
        String segment = delimiters.getSegment();
        if (segment.length() == 1) {
            if (!Character.isWhitespace(segment.charAt(0))) {
                int offset = edifact.indexOf(segment);
                if (offset + 1 < edifact.length()){
                    char ch = edifact.charAt(offset + 1);
                    if (Character.isWhitespace(ch)) {
                        delimiters.setSegment(segment + ch);
                    }
                }
            }
        }
    }
}
