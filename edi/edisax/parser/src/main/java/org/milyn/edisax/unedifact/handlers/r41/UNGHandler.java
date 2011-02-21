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
package org.milyn.edisax.unedifact.handlers.r41;

import java.io.IOException;

import org.milyn.edisax.BufferedSegmentReader;
import org.milyn.edisax.interchange.ControlBlockHandler;
import org.milyn.edisax.interchange.InterchangeContext;
import org.milyn.edisax.model.internal.Component;
import org.milyn.edisax.model.internal.Field;
import org.milyn.edisax.model.internal.Segment;
import org.xml.sax.SAXException;

/**
 * UNG Segment Handler.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
class UNGHandler implements ControlBlockHandler {

	private static Segment ungSegment;
	private static Segment uneSegment;

    static {
		createSegmentsDefs();
	}

    public void process(InterchangeContext interchangeContext) throws IOException, SAXException {
		BufferedSegmentReader segmentReader = interchangeContext.getSegmentReader();

		interchangeContext.getControlSegmentParser().startElement("group", true);

		segmentReader.moveToNextSegment(false);
		interchangeContext.mapControlSegment(ungSegment, true);

        while(true) {
	        String segCode = segmentReader.peek(3, true);

	        if(segCode.equals("UNE")) {
	    		segmentReader.moveToNextSegment(false);
	    		interchangeContext.mapControlSegment(uneSegment, true);
	    		break;
	        } else {
	        	ControlBlockHandler handler = interchangeContext.getControlBlockHandler(segCode);
	        	handler.process(interchangeContext);
	        }
        }

        interchangeContext.getControlSegmentParser().endElement("group", true);
	}

    private static void createSegmentsDefs() {
		// UNG Segment Definition...
		// http://www.gefeg.com/jswg/v41/se/se15.htm
		ungSegment = new Segment();
		ungSegment.setSegcode("UNG");
		ungSegment.setXmltag("UNG");
		ungSegment.setDescription("UNG - Group Header");
		ungSegment.setTruncatable(true);
		ungSegment.addField(new Field("groupId", false));
		ungSegment.addField(new Field("senderApp",  false).
                addComponent(new Component("id",            true)).
                addComponent(new Component("codeQualifier", false)));
		ungSegment.addField(new Field("recipientApp", false).
                addComponent(new Component("id",              true)).
                addComponent(new Component("codeQualifier",   false)));
		ungSegment.addField(new Field("dateTime",  false).
                addComponent(new Component("date", true)).
                addComponent(new Component("time", true)));
		ungSegment.addField(new Field("groupRef", true));
		ungSegment.addField(new Field("controllingAgencyCode", false));
		ungSegment.addField(new Field("messageVersion",        false).
                addComponent(new Component("versionNum",       true)).
                addComponent(new Component("releaseNum",       true)).
                addComponent(new Component("associationCode", false)));
		ungSegment.addField(new Field("applicationPassword",  false));

		// UNE Segment Definition...
		// http://www.gefeg.com/jswg/v41/se/se14.htm
		uneSegment = new Segment();
		uneSegment.setSegcode("UNE");
		uneSegment.setXmltag("UNE");
		uneSegment.setDescription("UNE - Group Trailer");
		uneSegment.setTruncatable(true);
		uneSegment.addField(new Field("controlCount", true));
		uneSegment.addField(new Field("groupRef", true));
	}
}
