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

import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;
import org.milyn.edisax.model.internal.Delimiters;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author tfennelly
 */
public class BufferedSegmentReaderTest extends TestCase {

	public void test() throws IOException {
		test("111111111ff22222222222ff333333333ff4444444444f4444444fff5555555555", "ff", "|",
			new String[] {"111111111", "22222222222", "333333333", "4444444444f4444444", "f5555555555"});

		test("a", "ff", "|", new String[] {"a"});

		test("ff", "ff", "*",  new String[] {});

		test("111111111\n22222222222\n333333333\n4444444444f4444444\nf5555555555", "\n", "*",
				new String[] {"111111111", "22222222222", "333333333", "4444444444f4444444", "f5555555555"});
	}

    public void test_ignore_cr_lf() throws IOException {
        String fieldDelimiter = "*";
        String segmentDelimiter = "'!$";
        String edi1 = "SEG0*1*2**4*5'\nSEG1*1*2*3'\r\nSEG2*1*2*3*4'";
        String edi2 = "SEG0*1*2**4*5'SEG1*1*2*3'SEG2*1*2*3*4'";

        // Check that BufferedSegmentReader reads all segments when !$ exists at end of segmentdelimiter.
        BufferedSegmentReader reader = createSegmentReader(edi1, segmentDelimiter, fieldDelimiter);
        int segIndex = 0;
        while(reader.moveToNextSegment()) {
			assertEquals("Segment comparison failure.", "SEG" + segIndex, reader.getCurrentSegmentFields()[0]);
			segIndex++;
		}
        assertTrue("The number of segments read should be three", segIndex-1 == 2);

        // Check that BufferedSegmentReader reads all segments when !$ exists at end of segmentdelimiter and there are no newlines.
        reader = createSegmentReader(edi2, segmentDelimiter, fieldDelimiter);
        segIndex = 0;
        while(reader.moveToNextSegment()) {
			assertEquals("Segment comparison failure.", "SEG" + segIndex, reader.getCurrentSegmentFields()[0]);
			segIndex++;
		}
        assertTrue("The number of segments read should be three", segIndex-1 == 2);
        
    }

    public void test_not_ignore_cr_lf() throws IOException {
        String fieldDelimiter = "*";
        String segmentDelimiter = "'";
        String edi1 = "SEG0*1*2**4*5'\nSEG1*1*2*3'\r\nSEG2*1*2*3*4'";
        String edi2 = "SEG0*1*2**4*5'SEG1*1*2*3'SEG2*1*2*3*4'";

        // Check that BufferedSegmentReader reads all segments when !$ exists at end of segmentdelimiter.
        BufferedSegmentReader reader = createSegmentReader(edi1, segmentDelimiter, fieldDelimiter);
        int segIndex = 0;
        while(reader.moveToNextSegment()) {
            if (segIndex == 0) {
			    assertEquals("Segment comparison failure.", "SEG" + segIndex, reader.getCurrentSegmentFields()[0]);
            } else if (segIndex == 1) {
                assertEquals("Segment comparison failure.", "SEG" + segIndex, reader.getCurrentSegmentFields()[0]);
            } else if (segIndex == 2) {
                assertEquals("Segment comparison failure.", "SEG" + segIndex, reader.getCurrentSegmentFields()[0]);
            } else {
                assertTrue("More segments than expected in test case.", false);
            }
			segIndex++;
		}
        assertTrue("The number of segments read should be three", segIndex-1 == 2);

        // Check that BufferedSegmentReader reads all segments when !$ exists at end of segmentdelimiter and there are no newlines.
        reader = createSegmentReader(edi2, segmentDelimiter, fieldDelimiter);
        segIndex = 0;
        while(reader.moveToNextSegment()) {
			assertEquals("Segment comparison failure.", "SEG" + segIndex, reader.getCurrentSegmentFields()[0]);
			segIndex++;
		}
        assertTrue("The number of segments read should be three", segIndex-1 == 2);

    }

	private void test(String input, String segmentDelim, String fieldDelim, String[] segments) throws IOException {
        BufferedSegmentReader reader = createSegmentReader(input, segmentDelim, fieldDelim);
		int segIndex = 0;
		
		while(segIndex < segments.length && reader.moveToNextSegment()) {
			String segment = reader.getSegmentBuffer().toString();
			assertEquals("Segment comparison failure.", segments[segIndex], segment);
			segIndex++;
		}
		
		assertEquals("All segments not read.", segments.length, segIndex);
	}

    private BufferedSegmentReader createSegmentReader(String input, String segmentDelim, String fieldDelim) {
        InputSource inputSource = new InputSource(new ByteArrayInputStream(input.getBytes()));
        Delimiters delimiters = new Delimiters().setSegment(segmentDelim).setField(fieldDelim);
        BufferedSegmentReader reader = new BufferedSegmentReader(inputSource, delimiters);
        return reader;
    }

    public void test_split() {
		Arrays.asList(StringUtils.splitPreserveAllTokens("a*b***C*d", "*"));
	}
}
