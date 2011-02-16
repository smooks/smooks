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

		interchangeContext.getControlSegmentParser().startElement("group", ControlBlockHandler.NAMESPACE, true);

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

        interchangeContext.getControlSegmentParser().endElement("group",ControlBlockHandler.NAMESPACE, true);
	}

    private static void createSegmentsDefs() {
		// UNG Segment Definition...
		// http://www.gefeg.com/jswg/v41/se/se15.htm
		ungSegment = new Segment();
		ungSegment.setSegcode("UNG");
		ungSegment.setXmltag("UNG");
		ungSegment.setNamespace(ControlBlockHandler.NAMESPACE);
		ungSegment.setDescription("UNG - Group Header");
		ungSegment.setTruncatable(true);
		ungSegment.addField(new Field("groupId",ControlBlockHandler.NAMESPACE, false));
		ungSegment.addField(new Field("senderApp",ControlBlockHandler.NAMESPACE,  false).
                addComponent(new Component("id",ControlBlockHandler.NAMESPACE,            true)).
                addComponent(new Component("codeQualifier",ControlBlockHandler.NAMESPACE, false)));
		ungSegment.addField(new Field("recipientApp",ControlBlockHandler.NAMESPACE, false).
                addComponent(new Component("id",ControlBlockHandler.NAMESPACE,              true)).
                addComponent(new Component("codeQualifier",ControlBlockHandler.NAMESPACE,   false)));
		ungSegment.addField(new Field("dateTime",ControlBlockHandler.NAMESPACE,  false).
                addComponent(new Component("date",ControlBlockHandler.NAMESPACE, true)).
                addComponent(new Component("time",ControlBlockHandler.NAMESPACE, true)));
		ungSegment.addField(new Field("groupRef",ControlBlockHandler.NAMESPACE, true));
		ungSegment.addField(new Field("controllingAgencyCode",ControlBlockHandler.NAMESPACE, false));
		ungSegment.addField(new Field("messageVersion",ControlBlockHandler.NAMESPACE,        false).
                addComponent(new Component("versionNum",ControlBlockHandler.NAMESPACE,       true)).
                addComponent(new Component("releaseNum",ControlBlockHandler.NAMESPACE,       true)).
                addComponent(new Component("associationCode",ControlBlockHandler.NAMESPACE, false)));
		ungSegment.addField(new Field("applicationPassword",ControlBlockHandler.NAMESPACE,  false));

		// UNE Segment Definition...
		// http://www.gefeg.com/jswg/v41/se/se14.htm
		uneSegment = new Segment();
		uneSegment.setSegcode("UNE");
		uneSegment.setXmltag("UNE");
		uneSegment.setNamespace(ControlBlockHandler.NAMESPACE);
		uneSegment.setDescription("UNE - Group Trailer");
		uneSegment.setTruncatable(true);
		uneSegment.addField(new Field("controlCount",ControlBlockHandler.NAMESPACE, true));
		uneSegment.addField(new Field("groupRef",ControlBlockHandler.NAMESPACE, true));
	}
}
