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
package org.milyn.edisax.interchange;

import java.util.Map;

import org.milyn.assertion.AssertArgument;
import org.milyn.edisax.BufferedSegmentReader;
import org.milyn.edisax.EDIParser;
import org.milyn.edisax.model.EdifactModel;
import org.milyn.edisax.model.internal.Delimiters;
import org.milyn.edisax.model.internal.Description;
import org.milyn.edisax.model.internal.Edimap;
import org.milyn.edisax.model.internal.Segment;
import org.milyn.lang.MutableInt;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * EDI message interchange context object.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class InterchangeContext {

    public static final String INTERCHANGE_MESSAGE_BLOCK_ELEMENT_NAME = "interchangeMessage";

	private BufferedSegmentReader segmentReader; 
	private Map<Description, EdifactModel> mappingModels;
	private ContentHandler contentHandler;
	private EDIParser controlSegmentParser;
    public MutableInt indentDepth = new MutableInt(0);
    private ControlBlockHandlerFactory controlBlockHandlerFactory;
    private boolean validate;

    /**
	 * Public constructor.
	 * 
	 * @param segmentReader The interchange {@link org.milyn.edisax.BufferedSegmentReader} instance.
     * @param mappingModels The {@link org.milyn.edisax.model.EdifactModel Mapping Models} to be used for translating the interchange.
     * @param contentHandler The {@link org.xml.sax.ContentHandler content handler} instance to receive the interchange events.
     * @param controlBlockHandlerFactory Control Block Handler Factory.
     * @param validate Validate the data types of the EDI message data as defined in the mapping model.
	 */
	public InterchangeContext(BufferedSegmentReader segmentReader, Map<Description, EdifactModel> mappingModels, ContentHandler contentHandler, ControlBlockHandlerFactory controlBlockHandlerFactory, boolean validate) {
		AssertArgument.isNotNull(segmentReader, "segmentReader");
		AssertArgument.isNotNull(mappingModels, "mappingModels");
		AssertArgument.isNotNull(contentHandler, "contentHandler");
        AssertArgument.isNotNull(controlBlockHandlerFactory, "controlBlockHandlerFactory");
		this.segmentReader = segmentReader;
		this.mappingModels = mappingModels;
		this.contentHandler = contentHandler;
        this.controlBlockHandlerFactory = controlBlockHandlerFactory;
		this.validate = validate;
		
		controlSegmentParser = new EDIParser();
		controlSegmentParser.setBufferedSegmentReader(segmentReader);
		controlSegmentParser.setContentHandler(contentHandler);
		controlSegmentParser.setIndentDepth(indentDepth);

        Edimap controlMap = new Edimap();
        EdifactModel controlModel = new EdifactModel(controlMap);

        controlMap.setDescription(new Description().setName("EDI Message Interchange Control Model").setVersion("N/A"));
        controlSegmentParser.setMappingModel(controlModel);
    }

    public ControlBlockHandler getControlBlockHandler(String segCode) throws SAXException {
        return controlBlockHandlerFactory.getControlBlockHandler(segCode);
    }

    public BufferedSegmentReader getSegmentReader() {
		return segmentReader;
	}

    public Map<Description, EdifactModel> getMappingModels() {
		return mappingModels;
	}

    public ContentHandler getContentHandler() {
		return contentHandler;
	}

    public boolean isValidate() {
		return validate;
	}

    public EDIParser newParser(EdifactModel mappingModel) {
		EDIParser parser = new EDIParser();

		parser.setContentHandler(contentHandler);
		parser.setMappingModel(mappingModel);
		parser.setBufferedSegmentReader(segmentReader);
		parser.setIndentDepth(indentDepth);
		parser.setFeature(EDIParser.FEATURE_VALIDATE, validate);

		return parser;
	}

    public EDIParser getControlSegmentParser() {
		return controlSegmentParser;
	}

    public void mapControlSegment(Segment controlSegment, boolean clearSegmentBuffer) throws SAXException {
		controlSegmentParser.startElement(controlSegment.getXmltag(), true);
		controlSegmentParser.mapFields(segmentReader.getCurrentSegmentFields(), controlSegment);
		controlSegmentParser.endElement(controlSegment.getXmltag(), true);

		// And clear the buffer... we're finished with this data...
		if(clearSegmentBuffer) {
			segmentReader.getSegmentBuffer().setLength(0);
		}
	}

    public void pushDelimiters(Delimiters delimiters) {
        segmentReader.pushDelimiters(delimiters);
    }

    public void popDelimiters() {
        segmentReader.popDelimiters();
    }
}
