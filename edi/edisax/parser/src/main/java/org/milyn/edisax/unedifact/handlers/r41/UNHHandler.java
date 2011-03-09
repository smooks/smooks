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

import org.milyn.edisax.BufferedSegmentListener;
import org.milyn.edisax.BufferedSegmentReader;
import org.milyn.edisax.EDIParser;
import org.milyn.edisax.interchange.ControlBlockHandler;
import org.milyn.edisax.interchange.InterchangeContext;
import org.milyn.edisax.model.EdifactModel;
import org.milyn.edisax.model.internal.Component;
import org.milyn.edisax.model.internal.Field;
import org.milyn.edisax.model.internal.Segment;
import org.milyn.edisax.unedifact.registry.MappingsRegistry;
import org.milyn.xml.hierarchy.HierarchyChangeListener;
import org.xml.sax.SAXException;

/**
 * UNH Segment Handler.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
class UNHHandler implements ControlBlockHandler {

    private static Segment unhSegment;
    private static Segment untSegment;
    private static UNTSegmentListener untSegmentListener = new UNTSegmentListener();

    static {
        createSegmentsDefs();
    }

    private HierarchyChangeListener hierarchyChangeListener;

    public UNHHandler(HierarchyChangeListener hierarchyChangeListener) {
        this.hierarchyChangeListener = hierarchyChangeListener;
    }

    public void process(InterchangeContext interchangeContext) throws IOException, SAXException {
		BufferedSegmentReader segmentReader = interchangeContext.getSegmentReader();
		MappingsRegistry registry = interchangeContext.getRegistry();

		interchangeContext.getControlSegmentParser().startElement(InterchangeContext.INTERCHANGE_MESSAGE_BLOCK_ELEMENT_NAME, InterchangeContext.INTERCHANGE_MESSAGE_BLOCK_NAMESPACE, true);

		// Move to the end of the UNH segment and map it's fields..
		segmentReader.moveToNextSegment(false);
		interchangeContext.mapControlSegment(unhSegment, false);

		// Select the mapping model to use for this message...
		String[] fields = segmentReader.getCurrentSegmentFields();
		String messageName = fields[2];
		EdifactModel mappingModel = registry.getMappingModel(messageName, segmentReader.getDelimiters());

		// Map the message... stopping at the UNT segment...
		try {
			EDIParser parser = interchangeContext.newParser(mappingModel);

			segmentReader.setSegmentListener(untSegmentListener);

            if(hierarchyChangeListener != null) {
                hierarchyChangeListener.attachXMLReader(parser);
            }

            parser.parse();
		} finally {
			segmentReader.setSegmentListener(null);
            if(hierarchyChangeListener != null) {
                hierarchyChangeListener.detachXMLReader();
            }
		}

		// We're at the end of the UNT segment now.  See the UNTSegmentListener below.

		// Map the UNT segment...
		interchangeContext.mapControlSegment(untSegment, true);
		segmentReader.getSegmentBuffer().setLength(0);

		interchangeContext.getControlSegmentParser().endElement(InterchangeContext.INTERCHANGE_MESSAGE_BLOCK_ELEMENT_NAME, InterchangeContext.INTERCHANGE_MESSAGE_BLOCK_NAMESPACE, true);
	}

    private static void createSegmentsDefs() {
		// UNH Segment Definition...
		// http://www.gefeg.com/jswg/v41/se/se16.htm
		unhSegment = new Segment();
		unhSegment.setSegcode("UNH");
		unhSegment.setXmltag("UNH");
		unhSegment.setDescription("UNH - Message Header");
		unhSegment.setNamespace(ControlBlockHandler.NAMESPACE);
		unhSegment.setTruncatable(true);
		unhSegment.addField(new Field("messageRefNum",ControlBlockHandler.NAMESPACE, true));
		unhSegment.addField(new Field("messageIdentifier",ControlBlockHandler.NAMESPACE, true).
                addComponent(new Component("id",ControlBlockHandler.NAMESPACE, true)).
                addComponent(new Component("versionNum",ControlBlockHandler.NAMESPACE, true)).
                addComponent(new Component("releaseNum",ControlBlockHandler.NAMESPACE, true)).
                addComponent(new Component("controllingAgencyCode",ControlBlockHandler.NAMESPACE, true)).
                addComponent(new Component("associationAssignedCode",ControlBlockHandler.NAMESPACE, false)).
                addComponent(new Component("codeListDirVersionNum",ControlBlockHandler.NAMESPACE, false)).
                addComponent(new Component("typeSubFunctionId",ControlBlockHandler.NAMESPACE, false)));
		unhSegment.addField(new Field("commonAccessRef",ControlBlockHandler.NAMESPACE, false));
		unhSegment.addField(new Field("transferStatus",ControlBlockHandler.NAMESPACE, false).
                addComponent(new Component("sequence",ControlBlockHandler.NAMESPACE, true)).
                addComponent(new Component("firstAndLast",ControlBlockHandler.NAMESPACE, false)));
		unhSegment.addField(new Field("subset",ControlBlockHandler.NAMESPACE, false).
                addComponent(new Component("id",ControlBlockHandler.NAMESPACE, true)).
                addComponent(new Component("versionNum",ControlBlockHandler.NAMESPACE, false)).
                addComponent(new Component("releaseNum",ControlBlockHandler.NAMESPACE, false)).
                addComponent(new Component("controllingAgencyCode",ControlBlockHandler.NAMESPACE, false)));
		unhSegment.addField(new Field("implementationGuideline",ControlBlockHandler.NAMESPACE, false).
                addComponent(new Component("id",ControlBlockHandler.NAMESPACE, true)).
                addComponent(new Component("versionNum",ControlBlockHandler.NAMESPACE, false)).
                addComponent(new Component("releaseNum",ControlBlockHandler.NAMESPACE, false)).
                addComponent(new Component("controllingAgencyCode",ControlBlockHandler.NAMESPACE, false)));
		unhSegment.addField(new Field("scenario",ControlBlockHandler.NAMESPACE, false).
                addComponent(new Component("id",ControlBlockHandler.NAMESPACE, true)).
                addComponent(new Component("versionNum",ControlBlockHandler.NAMESPACE, false)).
                addComponent(new Component("releaseNum",ControlBlockHandler.NAMESPACE, false)).
                addComponent(new Component("controllingAgencyCode",ControlBlockHandler.NAMESPACE, false)));

		// UNT Segment Definition...
		// http://www.gefeg.com/jswg/v41/se/se20.htm
		untSegment = new Segment();
		untSegment.setSegcode("UNT");
		untSegment.setXmltag("UNT");
		untSegment.setNamespace(ControlBlockHandler.NAMESPACE);
		untSegment.setDescription("UNT - Message Trailer");
		untSegment.setTruncatable(true);
		untSegment.addField(new Field("segmentCount",ControlBlockHandler.NAMESPACE, true));
		untSegment.addField(new Field("messageRefNum",ControlBlockHandler.NAMESPACE, true));
	}

    private static class UNTSegmentListener implements BufferedSegmentListener {

        public boolean onSegment(BufferedSegmentReader bufferedSegmentReader) {
            String[] fields = bufferedSegmentReader.getCurrentSegmentFields();

            // Stop the current segment consumer if we have reached the UNT segment i.e.
            // only return true if it's not UNT...
            return !fields[0].equals("UNT");
        }
    }
}
