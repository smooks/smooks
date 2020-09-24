/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.delivery.dom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.AbstractParser;
import org.smooks.delivery.ContentDeliveryConfig;
import org.smooks.delivery.XMLReaderHierarchyChangeListener;
import org.smooks.namespace.NamespaceDeclarationStack;
import org.smooks.xml.NamespaceManager;
import org.smooks.xml.hierarchy.HierarchyChangeReader;
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
 * If the configured parser implements the {@link org.smooks.xml.SmooksXMLReader}, the configuration will be
 * passed to the parser via {@link javax.inject.Inject} annotations on config properties
 * defined on the implementation.
 *
 * <h3 id="parserconfig">.cdrl Configuration</h3>
 * <pre>
 * &lt;smooks-resource selector="org.xml.sax.driver" path="org.smooks.protocolx.XParser" &gt;
 * 	&lt;!--
 * 		Optional list of driver parameters for {@link org.smooks.xml.SmooksXMLReader} implementations.
 * 		See {@link org.smooks.cdr.SmooksResourceConfiguration} for how to add configuration parameters.
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
	   	DOMBuilder contentHandler = new DOMBuilder(getExecutionContext());

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
  	   	DOMBuilder contentHandler = new DOMBuilder(getExecutionContext());

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
  		ExecutionContext executionContext = getExecutionContext();
  		
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
                NamespaceManager.setNamespaceDeclarationStack(namespaceDeclarationStack, executionContext);
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
