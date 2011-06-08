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

import org.milyn.edisax.BufferedSegmentListener;
import org.milyn.edisax.BufferedSegmentReader;
import org.milyn.edisax.EDIParser;
import org.milyn.edisax.interchange.ControlBlockHandler;
import org.milyn.edisax.interchange.InterchangeContext;
import org.milyn.edisax.model.EdifactModel;
import org.milyn.edisax.model.internal.Description;
import org.milyn.edisax.model.internal.Edimap;
import org.milyn.edisax.model.internal.Segment;
import org.milyn.edisax.unedifact.registry.MappingsRegistry;
import org.milyn.xml.hierarchy.HierarchyChangeListener;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.XMLConstants;

/**
 * UNH Segment Handler.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UNHHandler implements ControlBlockHandler {

    private Segment unhSegment;
    private Segment untSegment;
    private static UNTSegmentListener untSegmentListener = new UNTSegmentListener();

    private HierarchyChangeListener hierarchyChangeListener;

    public UNHHandler(Segment unhSegment, Segment untSegment, HierarchyChangeListener hierarchyChangeListener) {
        this.unhSegment = unhSegment;
        this.untSegment = untSegment;
        this.hierarchyChangeListener = hierarchyChangeListener;
    }

    public void process(InterchangeContext interchangeContext) throws IOException, SAXException {
		BufferedSegmentReader segmentReader = interchangeContext.getSegmentReader();
		MappingsRegistry registry = interchangeContext.getRegistry();

		// Move to the end of the UNH segment and map it's fields..
		segmentReader.moveToNextSegment(false);

		// Select the mapping model to use for this message...
		String[] fields = segmentReader.getCurrentSegmentFields();
		String messageName = fields[2];
		EdifactModel mappingModel = registry.getMappingModel(messageName, segmentReader.getDelimiters());
        Edimap ediMap = mappingModel.getEdimap();

        Description description = ediMap.getDescription();
        AttributesImpl attrs = new AttributesImpl();
        String namespace = description.getNamespace();
        String commonNS = null;
        String messageNSPrefix = null;
        if(namespace != null && !namespace.equals(XMLConstants.NULL_NS_URI)) {
            int nameComponentIndex = namespace.lastIndexOf(":");
            if(nameComponentIndex != -1) {
                commonNS = namespace.substring(0, nameComponentIndex) + ":common";
                messageNSPrefix = description.getName().toLowerCase();
                attrs.addAttribute(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "c", "xmlns:c", "CDATA", commonNS);
                attrs.addAttribute(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, messageNSPrefix, "xmlns:" + messageNSPrefix, "CDATA", namespace);
            }
        }

		interchangeContext.getControlSegmentParser().startElement(InterchangeContext.INTERCHANGE_MESSAGE_BLOCK_ELEMENT_NAME, unhSegment.getNamespace(), true, attrs);
        interchangeContext.mapControlSegment(unhSegment, false);

		// Map the message... stopping at the UNT segment...
		try {
			EDIParser parser = interchangeContext.newParser(mappingModel);

            if(commonNS != null) {
                parser.getNamespaceResolver().addNamespace(commonNS, "c");
                parser.getNamespaceResolver().addNamespace(namespace, messageNSPrefix);
            }

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

		interchangeContext.getControlSegmentParser().endElement(InterchangeContext.INTERCHANGE_MESSAGE_BLOCK_ELEMENT_NAME, unhSegment.getNamespace(), true);
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
