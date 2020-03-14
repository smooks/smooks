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

import org.smooks.edisax.BufferedSegmentReader;
import org.smooks.edisax.EDIParseException;
import org.smooks.edisax.interchange.ControlBlockHandler;
import org.smooks.edisax.interchange.InterchangeContext;
import org.smooks.edisax.model.internal.Segment;
import org.xml.sax.SAXException;

/**
 * UNG Segment Handler.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UNGHandler implements ControlBlockHandler {

	private Segment ungSegment;
	private Segment uneSegment;

    public UNGHandler(Segment ungSegment, Segment uneSegment) {
        this.ungSegment = ungSegment;
        this.uneSegment = uneSegment;
    }

    public void process(InterchangeContext interchangeContext) throws IOException, SAXException {
		BufferedSegmentReader segmentReader = interchangeContext.getSegmentReader();

		interchangeContext.getControlSegmentParser().startElement("group", ungSegment.getNamespace(), true);

		segmentReader.moveToNextSegment(false);
		interchangeContext.mapControlSegment(ungSegment, true);

        while(true) {
	        String segCode = segmentReader.peek(3, true);

	        if(segCode.equals("UNE")) {
	    		segmentReader.moveToNextSegment(false);
	    		interchangeContext.mapControlSegment(uneSegment, true);
	    		break;
            } else if(segCode.length() > 0) {
	        	ControlBlockHandler handler = interchangeContext.getControlBlockHandler(segCode);
	        	handler.process(interchangeContext);
            } else {
                throw new EDIParseException("Unexpected end of UN/EDIFACT data stream.  If stream was reset (e.g. after read charset was changed), please make sure underlying stream was properly reset.");
	        }
        }

        interchangeContext.getControlSegmentParser().endElement("group", ungSegment.getNamespace(), true);
	}
}
