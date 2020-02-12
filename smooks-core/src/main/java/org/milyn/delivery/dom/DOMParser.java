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

package org.milyn.delivery.dom;

import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.AbstractParser;
import org.milyn.delivery.ContentDeliveryConfig;
import org.milyn.delivery.XMLReaderHierarchyChangeListener;
import org.milyn.namespace.NamespaceDeclarationStack;
import org.milyn.xml.NamespaceMappings;
import org.milyn.xml.hierarchy.HierarchyChangeReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.transform.Source;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Smooks DOM data stream parser.
 * <p/>
 * This parser can be configured to use a SAX Parser targeted at a specific data stream type.
 * This lets you parse a stream of any type, convert it to a stream of SAX event and so treat the stream
 * as an XML data stream, even when the stream is non-XML.
 * <p/>
 * If the configured parser implements the {@link org.milyn.xml.SmooksXMLReader}, the configuration will be
 * passed to the parser via {@link org.milyn.cdr.annotation.ConfigParam} annotaions on config properties
 * defined on the implementation.
 *
 * <h3 id="parserconfig">.cdrl Configuration</h3>
 * <pre>
 * &lt;smooks-resource selector="org.xml.sax.driver" path="org.milyn.protocolx.XParser" &gt;
 * 	&lt;!--
 * 		Optional list of driver parameters for {@link org.milyn.xml.SmooksXMLReader} implementations.
 * 		See {@link org.milyn.cdr.SmooksResourceConfiguration} for how to add configuration parameters.
 * 	--&gt;
 * &lt;/smooks-resource&gt;
 * </pre>
 *
 * @author tfennelly
 */
public class DOMParser extends AbstractParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(DOMParser.class);

	/**
	 * Public constructor.
	 * <p/>
	 * This constructor attempts to lookup a SAX Parser config under the "org.xml.sax.driver" selector string.
	 * See <a href="#parserconfig">.cdrl Configuration</a>.
	 * @param execContext The execution context that the parser is being instantiated on behalf of.
	 */
	public DOMParser(ExecutionContext execContext) {
        super(execContext);
	}

	/**
	 * Public constructor.
	 * @param execContext The Smooks Container Request that the parser is being instantiated on behalf of.
	 * @param saxDriverConfig SAX Parser configuration. See <a href="#parserconfig">.cdrl Configuration</a>.
	 */
    public DOMParser(ExecutionContext execContext, SmooksResourceConfiguration saxDriverConfig) {
        super(execContext, saxDriverConfig);
    }

    /**
	 * Document parser.
	 * @param source Source content stream to be parsed.
	 * @return W3C ownerDocument.
	 * @throws SAXException Unable to parse the content.
	 * @throws IOException Unable to read the input stream.
	 */
	public Document parse(Source source) throws IOException, SAXException {
	   	DOMBuilder contentHandler = new DOMBuilder(getExecContext());

	   	parse(source, contentHandler);

		return contentHandler.getDocument();
	}

      /**
  	 * Append the content, behind the supplied input stream, to suplied
  	 * document element.
  	 * <p/>
  	 * Used to merge document fragments into a document.
  	 * @param source Source content stream to be parsed.
  	 * @param appendElement DOM element to which the content fragment is to
  	 * be added.
  	 * @throws SAXException Unable to parse the content.
  	 * @throws IOException Unable to read the input stream.
  	 */
  	public void append(Source source, Element appendElement) throws IOException, SAXException {
  	   	DOMBuilder contentHandler = new DOMBuilder(getExecContext());

  		contentHandler.setAppendElement(appendElement);
  	   	parse(source, contentHandler);
  	}

      /**
  	 * Perform the actual parse into the supplied content handler.
  	 * @param source Source content stream to be parsed.
  	 * @param contentHandler Content handler instance that will build/append-to the DOM.
  	 * @throws SAXException Unable to parse the content.
  	 * @throws IOException Unable to read the input stream.
  	 */
  	private void parse(Source source, DOMBuilder contentHandler) throws SAXException, IOException {
  		ExecutionContext executionContext = getExecContext();
  		
  		if(executionContext != null) {
			ContentDeliveryConfig deliveryConfig = executionContext.getDeliveryConfig();

	  		XMLReader domReader = getXMLReader(executionContext);

	  		try {
                if(domReader == null) {
                    domReader = deliveryConfig.getXMLReader();
                }
                if(domReader == null) {
                    domReader = createXMLReader();
                }

                if(domReader instanceof HierarchyChangeReader) {
                    ((HierarchyChangeReader)domReader).setHierarchyChangeListener(new XMLReaderHierarchyChangeListener(executionContext));
                }

                NamespaceDeclarationStack namespaceDeclarationStack = new NamespaceDeclarationStack();
                NamespaceMappings.setNamespaceDeclarationStack(namespaceDeclarationStack, executionContext);
                attachNamespaceDeclarationStack(domReader, executionContext);

                attachXMLReader(domReader, executionContext);
                configureReader(domReader, contentHandler, executionContext, source);
		        domReader.parse(createInputSource(source, executionContext.getContentEncoding()));
	  		} finally {
                try {
                    if(domReader instanceof HierarchyChangeReader) {
                        ((HierarchyChangeReader)domReader).setHierarchyChangeListener(null);
                    }
                } finally {
                    try {
                        try {
                            detachXMLReader(executionContext);
                        } finally {
                            if(domReader != null) {
                                deliveryConfig.returnXMLReader(domReader);
                            }
                        }
                    } finally {
                        contentHandler.detachHandler();
                    }
                }
	  		}
  		} else {
	  		XMLReader domReader = createXMLReader();
	        configureReader(domReader, contentHandler, null, source);
	        domReader.parse(createInputSource(source, Charset.defaultCharset().name()));
  		}
  	}
}
