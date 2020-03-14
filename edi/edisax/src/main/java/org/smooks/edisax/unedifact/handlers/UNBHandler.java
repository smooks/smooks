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
package org.smooks.edisax.unedifact.handlers;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.smooks.edisax.BufferedSegmentReader;
import org.smooks.edisax.EDIParseException;
import org.smooks.edisax.util.EDIUtils;
import org.smooks.edisax.interchange.ControlBlockHandler;
import org.smooks.edisax.interchange.InterchangeContext;
import org.smooks.edisax.model.internal.Segment;
import org.xml.sax.SAXException;

/**
 * UNB Segment Handler.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UNBHandler implements ControlBlockHandler {

	private Segment unbSegment;
	private Segment unzSegment;
	private Map<String, Charset> toCharsetMapping;

    public UNBHandler(Segment unbSegment, Segment unzSegment, HashMap<String, Charset> toCharsetMapping) {
        this.unbSegment = unbSegment;
        this.unzSegment = unzSegment;
        this.toCharsetMapping = toCharsetMapping;
    }

    public void process(InterchangeContext interchangeContext) throws IOException, SAXException {
		BufferedSegmentReader segmentReader = interchangeContext.getSegmentReader();
		
		segmentReader.moveToNextSegment(false);
		
		String[] fields = segmentReader.getCurrentSegmentFields();
		
		interchangeContext.mapControlSegment(unbSegment, true);
		
		String[] syntaxIdComponents = EDIUtils.split(fields[1], segmentReader.getDelimiters().getComponent(), segmentReader.getDelimiters().getEscape());

		// First component (index 0) defines the char repertoire.  Fourth 
		// component (index 3) is optional and can override...
		if(syntaxIdComponents.length < 4) {
			changeReadEncoding(syntaxIdComponents[0], interchangeContext.getSegmentReader());
		} else {
			changeReadEncoding(syntaxIdComponents[3], interchangeContext.getSegmentReader());
		}
		
        while(true) {
	        String segCode = segmentReader.peek(3, true);

	        if(segCode.equals("UNZ")) {
	    		segmentReader.moveToNextSegment(false);
	    		interchangeContext.mapControlSegment(unzSegment, true);
	    		break;
            } else if(segCode.length() > 0) {
	        	ControlBlockHandler handler = interchangeContext.getControlBlockHandler(segCode);
	        	handler.process(interchangeContext);
            } else {
                throw new EDIParseException("Unexpected end of UN/EDIFACT data stream.  If stream was reset (e.g. after read charset was changed), please make sure underlying stream was properly reset.");
	        }
        }
	}

	private void changeReadEncoding(String code, BufferedSegmentReader bufferedSegmentReader) throws EDIParseException, IOException {
		Charset charset = toCharsetMapping.get(code.toUpperCase());
		
		if(charset == null) {
			throw new EDIParseException("Unknown UN/EDIFACT character stream encoding code '" + code + "'.");
		}
		
		bufferedSegmentReader.changeEncoding(charset);
	}
}
