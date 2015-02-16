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

package org.milyn.edisax;

import org.milyn.edisax.model.EDIConfigDigester;
import org.milyn.edisax.model.EdifactModel;
import org.milyn.edisax.model.internal.Segment;
import org.milyn.edisax.model.internal.SegmentGroup;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author tfennelly
 */
public class EDIParserTest extends AbstractEDIParserTestCase {

        @Test
	public void test_validation() throws IOException, SAXException {
		// Valid doc...
        try {
            EDIConfigDigester.digestConfig(getClass().getResourceAsStream("edi-mapping_01.xml"));
        } catch (EDIConfigurationException e) {
            fail("Digesting edi-mapping_01.xml should not fail.");
        }

        // Invalid doc...
		try {
			EDIConfigDigester.digestConfig(getClass().getResourceAsStream("edi-mapping_02.xml"));
			fail("Expected SAXException");
		} catch (SAXException e) {
			// OK
        } catch (EDIConfigurationException e) { 
            fail("Expected SAXException");
        }
    }

        @Test	
	public void test_parseMappingModel() throws IOException, SAXException, EDIConfigurationException {
		EdifactModel map = EDIParser.parseMappingModel(getClass().getResourceAsStream("edi-mapping_01.xml"));
		
		// Some basic checks on the model produced by xmlbeans...
		
		// Make sure xml character refs are rewritten on the delimiters
		assertEquals("\n", map.getDelimiters().getSegment());
		assertEquals("*", map.getDelimiters().getField());
		assertEquals("^", map.getDelimiters().getComponent());
		assertEquals("~", map.getDelimiters().getSubComponent());
		
		assertEquals("message-x", map.getEdimap().getSegments().getXmltag());
		List<SegmentGroup> segments = map.getEdimap().getSegments().getSegments();
		assertEquals(2, segments.size());

        Segment segment = (Segment) segments.get(0);
        assertEquals(1, segment.getSegments().size());
		assertEquals(1, segment.getFields().size());

        segment = (Segment) segments.get(1);
        assertEquals(0, segment.getSegments().size());
		assertEquals(1, segment.getFields().size());
	}

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
	public void test_mapping_03() throws IOException {
		test("test03");
	}

        @Test	
	public void test_mapping_04() throws IOException {
		test("test04");
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
	public void test_mapping_10() throws IOException {
		test("test10");
	}

        @Test	
	public void test_mapping_11() throws IOException {
		test("test11");
	}

        @Test	
	public void test_mapping_12() throws IOException {
		test("test12");
	}

        @Test	
	public void test_mapping_13() throws IOException {
		test("test13");
	}

        @Test	
	public void test_mapping_14() throws IOException {
		test("test14");
	}

        @Test	
	public void test_mapping_15() throws IOException {
		test("test15");
	}

        @Test	
	public void test_mapping_16() throws IOException {
		test("test16");
	}

        @Test	
	public void test_mapping_17() throws IOException {
		test("test17");
	}

        @Test	
	public void test_mapping_18() throws IOException {
		test("test18");
	}

    @Test
    public void test_MILYN_108() throws IOException {
        test("test-MILYN-108-01"); // Tests Segment Truncation
        test("test-MILYN-108-02"); // Tests Segment Truncation
        test("test-MILYN-108-03"); // Tests Segment Truncation
        test("test-MILYN-108-04"); // Tests Segment Truncation

        test("test-MILYN-108-05"); // Tests Component Truncation
        test("test-MILYN-108-06"); // Tests Component Truncation
        test("test-MILYN-108-07"); // Tests Component Truncation

        test("test-MILYN-108-08"); // Tests Field Truncation
        test("test-MILYN-108-09"); // Tests Field Truncation

        test("test-MILYN-108-10"); // Tests Field and Component Truncation
        test("test-MILYN-108-11"); // Tests Field and Component Truncation
    }

    @Test
    public void testCorrectEdiParseException() throws IOException, EDIParseException {
        /**
         * Test correct EDIParseException when reaching end of Edimap-model but more data exists in inputfile.
         */
        testEDIParseException("reached-end-more-segments", null, 0);

        /**
         * Test correct EDIParseException when reaching end of inputfile but more mandatory segments exists in Edimap-model.
         */
        testEDIParseException("reached-end-should-be-more-segments", "message-seg", 1);

        /**
         * Test error in segment node.
         */
        try {
        	testEDIParseException("error-segment", "message-seg", 2);
        	fail("Expected EDIParseException.");
        } catch(EDIParseException e) {
        	assertEquals("EDI message processing failed [Test Message][1.0].  Reached end of mapping model but there are more EDI segments in the incoming message.  Read 2 segment(s). Current EDI segment is [SEG*6*7**9*10]", e.getMessage());
        }

        /**
         * Test error in field node.
         */
        testEDIParseException("error-field", "message-seg", 1);

        /**
         * Test error in component node.
         */
        testEDIParseException("error-component", "field-1", 1);

        /**
         * Test error in sub component node.
         */
        testEDIParseException("error-subcomponent", "firstname", 1);

    }

}
