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
import org.milyn.edisax.model.internal.Delimiters;
import org.xml.sax.SAXException;

/**
 * UNA Segment Handler.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UNAHandler implements ControlBlockHandler {

	public void process(InterchangeContext interchangeContext) throws IOException, SAXException {
		Delimiters delimiters = new Delimiters();
		BufferedSegmentReader segmentReader = interchangeContext.getSegmentReader();

		// The UNA segment code is still in the segment buffer... clear it before 
		// reading the segment delimiters...
		segmentReader.getSegmentBuffer().setLength(0);
		
		// Read the delimiter chars one-by-one and set in the Delimiters instance...
		
		// 1st char is the component ("sub-element") delimiter...
		delimiters.setComponent( segmentReader.read(1));
		// 2nd char is the field ("data-element") delimiter...
		delimiters.setField(     segmentReader.read(1));
		// 3rd char is the decimal point indicator...
		delimiters.setDecimalSeparator(segmentReader.read(1));
		// 4th char is the escape char ("release")...
		delimiters.setEscape(    segmentReader.read(1));
		// 5th char is reserved for future use...
		segmentReader.read(1);
		// 6th char is the segment delimiter...
		delimiters.setSegment(   segmentReader.read(1));

		interchangeContext.pushDelimiters(delimiters);
	}
}
