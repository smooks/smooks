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
package org.milyn.edisax.unedifact.handlers;

import java.io.IOException;

import org.milyn.edisax.BufferedSegmentReader;
import org.milyn.edisax.interchange.ControlBlockHandler;
import org.milyn.edisax.interchange.InterchangeContext;
import org.xml.sax.SAXException;

/**
 * Generic Control Segment Handler.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class GenericHandler implements ControlBlockHandler {

	public void process(InterchangeContext interchangeContext) throws IOException, SAXException {
		BufferedSegmentReader segmentReader = interchangeContext.getSegmentReader();
		
		segmentReader.moveToNextSegment(false);
		
		String[] fields = segmentReader.getCurrentSegmentFields();
		StringBuffer segBuffer = segmentReader.getSegmentBuffer();
		char[] segChars = new char[segBuffer.length()];
		
		segBuffer.getChars(0, segBuffer.length(), segChars, 0);
		
		interchangeContext.getControlSegmentParser().startElement(fields[0], ControlBlockHandler.NAMESPACE, true);
		interchangeContext.getControlSegmentParser().getContentHandler().characters(segChars, 0, segChars.length);
		interchangeContext.getControlSegmentParser().endElement(fields[0], ControlBlockHandler.NAMESPACE, false);		

		// And clear out the buffer...
		segmentReader.getSegmentBuffer().setLength(0);
	}
}
