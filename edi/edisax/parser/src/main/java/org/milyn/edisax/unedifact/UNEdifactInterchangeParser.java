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
package org.milyn.edisax.unedifact;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;

import org.milyn.assertion.AssertArgument;
import org.milyn.edisax.BufferedSegmentReader;
import org.milyn.edisax.EDIParser;
import org.milyn.edisax.interchange.ControlBlockHandler;
import org.milyn.edisax.interchange.ControlBlockHandlerFactory;
import org.milyn.edisax.interchange.InterchangeContext;
import org.milyn.edisax.model.internal.Delimiters;
import org.milyn.edisax.unedifact.handlers.r41.UNEdifact41ControlBlockHandlerFactory;
import org.milyn.edisax.unedifact.registry.LazyMappingsRegistry;
import org.milyn.edisax.unedifact.registry.MappingsRegistry;
import org.milyn.namespace.NamespaceResolver;
import org.milyn.xml.hierarchy.HierarchyChangeListener;
import org.milyn.xml.hierarchy.HierarchyChangeReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

/**
 * UN/EDIFACT Interchange Envelope parser.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UNEdifactInterchangeParser implements XMLReader, HierarchyChangeReader {

    private Map<String, Boolean> features = new HashMap<String, Boolean>();
	
	public static final Delimiters defaultUNEdifactDelimiters = new Delimiters().setSegment("'").setField("+").setComponent(":").setEscape("?").setDecimalSeparator(".");
	
	/**
	 * By default we are using {@link LazyMappingsRegistry} instance
	 */
	protected MappingsRegistry registry = new LazyMappingsRegistry();
	private ContentHandler contentHandler;
    private HierarchyChangeListener hierarchyChangeListener;
    private InterchangeContext interchangeContext;

    public void parse(InputSource unedifactInterchange) throws IOException, SAXException {
		AssertArgument.isNotNull(unedifactInterchange, "unedifactInterchange");

        if(contentHandler == null) {
            throw new IllegalStateException("'contentHandler' not set.  Cannot parse EDI stream.");
        }

        if(registry == null) {
            throw new IllegalStateException("'mappingsRegistry' not set.  Cannot parse EDI stream.");
        }

        try {
            ControlBlockHandlerFactory handlerFactory = new UNEdifact41ControlBlockHandlerFactory(hierarchyChangeListener);
	        BufferedSegmentReader segmentReader = new BufferedSegmentReader(unedifactInterchange, defaultUNEdifactDelimiters);
	        boolean validate = getFeature(EDIParser.FEATURE_VALIDATE);
	        String segCode;
	        
	        segmentReader.mark();
	        segmentReader.setIgnoreNewLines(getFeature(EDIParser.FEATURE_IGNORE_NEWLINES));

            NamespaceResolver namespaceResolver = handlerFactory.newNamespaceResolver();
            String envelopePrefix = namespaceResolver.getPrefix(handlerFactory.getNamespace());

	        contentHandler.startDocument();
	        contentHandler.startPrefixMapping(envelopePrefix, handlerFactory.getNamespace());
	        AttributesImpl attrs = new AttributesImpl();
	        attrs.addAttribute(XMLConstants.NULL_NS_URI, "xmlns:" + envelopePrefix, "xmlns:" + envelopePrefix, "CDATA", handlerFactory.getNamespace());
	        contentHandler.startElement(handlerFactory.getNamespace(), "unEdifact", envelopePrefix + ":unEdifact", attrs);
	
	        while(true) {
		        segCode = segmentReader.peek(3, true);
		        if(segCode.length() == 3) {
                    interchangeContext = createInterchangeContext(segmentReader, validate, handlerFactory, namespaceResolver);

                    ControlBlockHandler handler = handlerFactory.getControlBlockHandler(segCode);

					interchangeContext.indentDepth.value++;
		        	handler.process(interchangeContext);
					interchangeContext.indentDepth.value--;
		        } else {
		        	break;
		        }
	        }
	        
	        contentHandler.characters(new char[] {'\n'}, 0, 1);
	        contentHandler.endElement(handlerFactory.getNamespace(), "unEdifact", envelopePrefix + ":unEdifact");
	        contentHandler.endPrefixMapping(envelopePrefix);
	        contentHandler.endDocument();
        } finally {
        	contentHandler = null;
        }
	}

    protected InterchangeContext createInterchangeContext(BufferedSegmentReader segmentReader, boolean validate, ControlBlockHandlerFactory controlBlockHandlerFactory, NamespaceResolver namespaceResolver) {
        return new InterchangeContext(segmentReader, registry, contentHandler, controlBlockHandlerFactory, namespaceResolver, validate);
    }

    public InterchangeContext getInterchangeContext() {
        return interchangeContext;
    }

    /**
	 * Set the EDI mapping model to be used in all subsequent parse operations.
	 * <p/>
	 * The model can be generated through a call to the {@link EDIParser}.
	 *
	 * @param registry The mapping model registry.
	 * @return This parser instance.
	 */
	public UNEdifactInterchangeParser setMappingsRegistry(MappingsRegistry registry) {
		AssertArgument.isNotNull(registry, "mappingsRegistry");
		this.registry = registry;
		return this;
	}

	public ContentHandler getContentHandler() {
		return contentHandler;
	}

	public void setContentHandler(ContentHandler contentHandler) {
		this.contentHandler = contentHandler;
	}

	public void ignoreNewLines(boolean ignoreNewLines) {
		setFeature(EDIParser.FEATURE_IGNORE_NEWLINES, ignoreNewLines);
	}

	public void validate(boolean validate) {
		setFeature(EDIParser.FEATURE_VALIDATE, validate);
	}
    
    public void setFeature(String name, boolean value) {
    	features.put(name, value);
    }

    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
    	Boolean feature = features.get(name);
    	if(feature == null) {
    		return false;
    	}
    	return feature;
    }

    public void setHierarchyChangeListener(HierarchyChangeListener listener) {
        this.hierarchyChangeListener = listener;
    }

    /****************************************************************************
     *
     * The following methods are currently unimplemnted...
     *
     ****************************************************************************/

    public void parse(String systemId) throws IOException, SAXException {
    	throw new UnsupportedOperationException("Operation not supports by this reader.");
    }

    public DTDHandler getDTDHandler() {
    	return null;
    }

    public void setDTDHandler(DTDHandler arg0) {
    }

    public EntityResolver getEntityResolver() {
    	return null;
    }

    public void setEntityResolver(EntityResolver arg0) {
    }

    public ErrorHandler getErrorHandler() {
    	return null;
    }

    public void setErrorHandler(ErrorHandler arg0) {
    }

    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
    	return null;
    }

    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
    }

}
