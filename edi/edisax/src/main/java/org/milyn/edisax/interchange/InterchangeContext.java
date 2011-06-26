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

import org.milyn.assertion.AssertArgument;
import org.milyn.edisax.BufferedSegmentReader;
import org.milyn.edisax.EDIParser;
import org.milyn.edisax.model.EdifactModel;
import org.milyn.edisax.model.internal.Delimiters;
import org.milyn.edisax.model.internal.Description;
import org.milyn.edisax.model.internal.Edimap;
import org.milyn.edisax.model.internal.Segment;
import org.milyn.edisax.registry.MappingsRegistry;
import org.milyn.lang.MutableInt;
import org.milyn.namespace.NamespaceDeclarationStack;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.Map;

/**
 * EDI message interchange context object.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class InterchangeContext {

    public static final String INTERCHANGE_MESSAGE_BLOCK_ELEMENT_NAME = "interchangeMessage";

	private BufferedSegmentReader segmentReader;
	private ContentHandler contentHandler;
    private Map<String,Boolean> features;
    private EDIParser controlSegmentParser;
    public MutableInt indentDepth = new MutableInt(0);
    private ControlBlockHandlerFactory controlBlockHandlerFactory;
    private boolean validate;
    private MappingsRegistry registry;
    private NamespaceDeclarationStack namespaceDeclarationStack;
    private boolean containerManagedNamespaceStack = false;

    /**
	 * Public constructor.
	 *
     * @param segmentReader The interchange {@link org.milyn.edisax.BufferedSegmentReader} instance.
     * @param registry The {@link org.milyn.edisax.model.EdifactModel Mapping Models} registry.
     * @param contentHandler The {@link org.xml.sax.ContentHandler content handler} instance to receive the interchange events.
     * @param parserFeatures Parser features.
     * @param controlBlockHandlerFactory Control Block Handler Factory.
     * @param validate Validate the data types of the EDI message data as defined in the mapping model.
     */
	public InterchangeContext(BufferedSegmentReader segmentReader, MappingsRegistry registry, ContentHandler contentHandler, Map<String, Boolean> parserFeatures, ControlBlockHandlerFactory controlBlockHandlerFactory, NamespaceDeclarationStack namespaceDeclarationStack, boolean validate) {
		AssertArgument.isNotNull(segmentReader, "segmentReader");
		AssertArgument.isNotNull(registry, "registry");
		AssertArgument.isNotNull(contentHandler, "contentHandler");
        AssertArgument.isNotNull(controlBlockHandlerFactory, "controlBlockHandlerFactory");
		this.segmentReader = segmentReader;
		this.registry = registry;
		this.contentHandler = contentHandler;
        this.features = parserFeatures;
        this.controlBlockHandlerFactory = controlBlockHandlerFactory;
		this.validate = validate;
        this.namespaceDeclarationStack = namespaceDeclarationStack;

		controlSegmentParser = new EDIParser();
		controlSegmentParser.setBufferedSegmentReader(segmentReader);
		controlSegmentParser.setContentHandler(contentHandler);
		controlSegmentParser.setIndentDepth(indentDepth);

        if (this.namespaceDeclarationStack == null) {
            this.namespaceDeclarationStack= new NamespaceDeclarationStack();
        } else {
            this.containerManagedNamespaceStack = true;
        }
        controlSegmentParser.setNamespaceDeclarationStack(this.namespaceDeclarationStack);

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

    public ContentHandler getContentHandler() {
		return contentHandler;
	}

    public boolean isValidate() {
		return validate;
	}

    public String getNamespace() {
        return controlBlockHandlerFactory.getNamespace();
    }

    public NamespaceDeclarationStack getNamespaceDeclarationStack() {
        return namespaceDeclarationStack;
    }

    public EDIParser newParser(EdifactModel mappingModel) {
		EDIParser parser = new EDIParser();

		parser.setContentHandler(contentHandler);
		parser.setMappingModel(mappingModel);
		parser.setBufferedSegmentReader(segmentReader);
		parser.setIndentDepth(indentDepth);
        parser.getFeatures().putAll(features);
		parser.setFeature(EDIParser.FEATURE_VALIDATE, validate);
        parser.setNamespaceDeclarationStack(namespaceDeclarationStack);

		return parser;
	}

    public EDIParser getControlSegmentParser() {
		return controlSegmentParser;
	}

    public void mapControlSegment(Segment controlSegment, boolean clearSegmentBuffer) throws SAXException {
		controlSegmentParser.startElement(controlSegment, true);
		controlSegmentParser.mapFields(segmentReader.getCurrentSegmentFields(), controlSegment);
		controlSegmentParser.endElement(controlSegment, true);

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

    /**
     * Returns an instance of {@link MappingsRegistry}
     *
     * @return
     */
    public MappingsRegistry getRegistry() {
		return registry;
	}

    public boolean isContainerManagedNamespaceStack() {
        return containerManagedNamespaceStack;
    }
}
