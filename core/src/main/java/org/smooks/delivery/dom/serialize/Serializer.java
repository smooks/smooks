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
package org.smooks.delivery.dom.serialize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.SmooksException;
import org.smooks.cdr.ParameterAccessor;
import org.smooks.cdr.ResourceConfigurationNotFoundException;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.ContentHandlerBinding;
import org.smooks.delivery.ContentHandlerBindings;
import org.smooks.delivery.Filter;
import org.smooks.delivery.dom.DOMContentDeliveryConfig;
import org.smooks.event.ExecutionEventListener;
import org.smooks.event.types.DOMFilterLifecycleEvent;
import org.smooks.event.types.ElementPresentEvent;
import org.smooks.event.types.ResourceTargetingEvent;
import org.smooks.xml.DocType;
import org.smooks.xml.DomUtils;
import org.w3c.dom.*;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Optional;

/**
 * Node serializer.
 * <p/>
 * This class uses the {@link org.smooks.delivery.ContentDeliveryConfig} and the
 * {@link org.smooks.delivery.dom.serialize.SerializationUnit} instances defined there on
 * to perform the serialization.
 * @author tfennelly
 */
public class Serializer {

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Serializer.class);
	/**
	 * Node to be serialized.
	 */
	private final Node node;
	/**
	 * Target device context.
	 */
	private final ExecutionContext executionContext;
  /**
	 * Target content delivery context SerializationUnit definitions.
	 */
	private final ContentHandlerBindings<SerializationUnit> serializationUnits;
  /**
     * Default serialization unit.
     */
	private DefaultSerializationUnit defaultSerializationUnit;
    /**
	 * Global SerializationUnits.
	 */
	private final List globalSUs;
    /**
     * Event Listener.
     */
    private final ExecutionEventListener eventListener;
    private final boolean terminateOnVisitorException;

    /**
	 * Public constructor.
	 * @param node Node to be serialized.
	 * @param executionContext Target device context.
	 */
	public Serializer(Node node, ExecutionContext executionContext) {
		if(node == null) {
			throw new IllegalArgumentException("null 'node' arg passed in method call.");
		} else if(executionContext == null) {
			throw new IllegalArgumentException("null 'executionContext' arg passed in method call.");
		}
		this.node = node;
		this.executionContext = executionContext;
        eventListener = executionContext.getEventListener();
		// Get the delivery context for the device.
    /*
      Target content delivery config.
     */
    DOMContentDeliveryConfig deliveryConfig = (DOMContentDeliveryConfig) executionContext.getDeliveryConfig();
		// Initialise the serializationUnits member
		serializationUnits = deliveryConfig.getSerializationVisitors();

        globalSUs = serializationUnits.getMappings(new String[] {"*", "**"});

        // Set the default SerializationUnit
    /*
      Turn default serialization on/off.  Default is "true".
     */
    boolean defaultSerializationOn = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.DEFAULT_SERIALIZATION_ON, String.class, "true", executionContext.getDeliveryConfig()));
        if(defaultSerializationOn) {
            defaultSerializationUnit = new DefaultSerializationUnit();
            boolean rewriteEntities = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.ENTITIES_REWRITE, String.class, "true", executionContext.getDeliveryConfig()));
            defaultSerializationUnit.setRewriteEntities(Optional.of(rewriteEntities));
        }
        terminateOnVisitorException = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.TERMINATE_ON_VISITOR_EXCEPTION, String.class, "true", executionContext.getDeliveryConfig()));
	}

	/**
	 * Serialise the document to the supplied output writer instance.
	 * <p/>
	 * Adds the DOCTYPE decl if one defined in the Content Delivery Configuration.
	 * <p/>
	 * If the node is a Document (or DocumentFragment) node the whole node is serialised.
	 * Otherwise, only the node child elements are serialised i.e. the node itself is skipped.
	 * @param writer Output writer.
	 * @throws ResourceConfigurationNotFoundException DOM Serialiser exception.
	 * @throws IOException Unable to write to output writer.
	 */
	public void serialize(Writer writer) throws ResourceConfigurationNotFoundException, IOException {
    if(writer == null) {
			throw new IllegalArgumentException("null 'writer' arg passed in method call.");
		}

        // Register the DOM phase events...
        if(eventListener != null) {
            eventListener.onEvent(new DOMFilterLifecycleEvent(DOMFilterLifecycleEvent.DOMEventType.SERIALIZATION_STARTED));
        }

        if(node instanceof Document) {
			Document doc = (Document)node;
			Element rootElement = doc.getDocumentElement();

            DocType.DocumentTypeData docTypeData = DocType.getDocType(executionContext);
            if(docTypeData != null) {
                DocType.serializeDoctype(docTypeData, writer);
                if(docTypeData.getXmlns() != null) {
                    rootElement.setAttribute("xmlns", docTypeData.getXmlns());
                } else {
                    rootElement.removeAttribute("xmlns");
                }
            }

            recursiveDOMWrite(rootElement, writer, true);
		} else {
            // Write the DOM, the child elements of the node
            NodeList deliveryNodes = node.getChildNodes();
            int nodeCount = deliveryNodes.getLength();
            boolean isRoot = (node == node.getOwnerDocument().getDocumentElement());
            for(int i = 0; i < nodeCount; i++) {
                Node childNode = deliveryNodes.item(i);
                if(childNode.getNodeType() == Node.ELEMENT_NODE) {
                    recursiveDOMWrite((Element)childNode, writer, isRoot);
                }
            }
        }
    }

  /**
	 * Recursively write the DOM tree to the supplied writer.
	 * @param element Element to write.
	 * @param writer Writer to use.
     * @param isRoot Is the supplied element the document root element.
	 */
	private void recursiveDOMWrite(Element element, Writer writer, boolean isRoot) {
		SerializationUnit elementSU;
		NodeList children = element.getChildNodes();

		elementSU = getSerializationUnit(element, isRoot);
		try {
            if(elementSU != null) {
                elementSU.writeElementStart(element, writer, executionContext);
            }

            if(children != null && children.getLength() > 0) {
				int childCount = children.getLength();

				for(int i = 0; i < childCount; i++) {
					Node childNode = children.item(i);

                    if(elementSU != null) {
                        switch(childNode.getNodeType()) {
                            case Node.CDATA_SECTION_NODE: {
                                elementSU.writeElementCDATA((CDATASection)childNode, writer, executionContext);
                                break;
                            }
                            case Node.COMMENT_NODE: {
                                elementSU.writeElementComment((Comment)childNode, writer, executionContext);
                                break;
                            }
                            case Node.ELEMENT_NODE: {
                                if(elementSU.writeChildElements()) {
                                    recursiveDOMWrite((Element)childNode, writer, false);
                                }
                                break;
                            }
                            case Node.ENTITY_REFERENCE_NODE: {
                                elementSU.writeElementEntityRef((EntityReference)childNode, writer, executionContext);
                                break;
                            }
                            case Node.TEXT_NODE: {
                                elementSU.writeElementText((Text)childNode, writer, executionContext);
                                break;
                            }
                            default: {
                                elementSU.writeElementNode(childNode, writer, executionContext);
                                break;
                            }
                        }
                    } else if(childNode.getNodeType() == Node.ELEMENT_NODE) {
                        recursiveDOMWrite((Element)childNode, writer, false);
                    }
                }
			}
            if(elementSU != null) {
    			elementSU.writeElementEnd(element, writer, executionContext);
            }
        } catch(Throwable thrown) {
            String error = "Failed to apply serialization unit [" + elementSU.getClass().getName() + "] to [" + executionContext.getDocumentSource() + ":" + DomUtils.getXPath(element) + "].";

            if(terminateOnVisitorException) {
                if(thrown instanceof SmooksException) {
                    throw (SmooksException) thrown;
                } else {
                    throw new SmooksException(error, thrown);
                }
            } else {
                LOGGER.debug(error, thrown);
            }
		}
	}

	/**
	 * Get the first matching serialisation unit for the supplied element.
	 * @param element Element to be serialized.
     * @param isRoot Is the supplied element the document root element.
     * @return SerializationUnit.
	 */
	@SuppressWarnings("unchecked")
  private SerializationUnit getSerializationUnit(Element element, boolean isRoot) {
		String elementName = DomUtils.getName(element);
        List<ContentHandlerBinding<SerializationUnit>> elementSUs;

        // Register the "presence" of the element...
        if(eventListener != null) {
            eventListener.onEvent(new ElementPresentEvent(element));
        }

        if(isRoot) {
            // The document as a whole (root node) can also be targeted through the "#document" selector.
            elementSUs = serializationUnits.getMappings(new String[] {SmooksResourceConfiguration.DOCUMENT_FRAGMENT_SELECTOR, elementName});
        } else {
            elementSUs = serializationUnits.getMappings(elementName);
        }

        if(elementSUs == null || elementSUs.isEmpty()) {
			elementSUs = globalSUs;
		}

        if(elementSUs != null) {
          for (final ContentHandlerBinding<SerializationUnit> elementSU : elementSUs)
          {
            SmooksResourceConfiguration config = elementSU.getSmooksResourceConfiguration();

            // Make sure the serialization unit is targeted at this element.
            if (!config.getSelectorPath().isTargetedAtElement(element, executionContext))
            {
              continue;
            }

            // Register the targeting event...
            if (eventListener != null)
            {
              eventListener.onEvent(new ResourceTargetingEvent(element, config));
            }

            if (LOGGER.isDebugEnabled())
            {
              LOGGER.debug("Applying serialisation resource [" + config + "] to element [" + DomUtils.getXPath(element) + "].");
            }

            // This is the one, return it...
            return (SerializationUnit) ((ContentHandlerBinding) elementSU).getContentHandler();
          }
        }

        return defaultSerializationUnit;
	}

    private static final DefaultSerializationUnit defaultSerializer = new DefaultSerializationUnit();

    static {
        defaultSerializer.setCloseEmptyElements(Optional.of(true));
    }

    /**
     * Recursively write the DOM tree to the supplied writer.
     * @param element Element to write.
     * @param writer Writer to use.
     */
    @SuppressWarnings({ "unused", "WeakerAccess" })
    public static void recursiveDOMWrite(Element element, Writer writer) {
        NodeList children = element.getChildNodes();

        try {
            defaultSerializer.writeElementStart(element, writer);

            if(children != null && children.getLength() > 0) {
                int childCount = children.getLength();

                for(int i = 0; i < childCount; i++) {
                    Node childNode = children.item(i);

                    switch(childNode.getNodeType()) {
                        case Node.CDATA_SECTION_NODE: {
                            defaultSerializer.writeElementCDATA((CDATASection)childNode, writer, null);
                            break;
                        }
                        case Node.COMMENT_NODE: {
                            defaultSerializer.writeElementComment((Comment)childNode, writer, null);
                            break;
                        }
                        case Node.ELEMENT_NODE: {
                            recursiveDOMWrite((Element)childNode, writer);
                            break;
                        }
                        case Node.ENTITY_REFERENCE_NODE: {
                            defaultSerializer.writeElementEntityRef((EntityReference)childNode, writer, null);
                            break;
                        }
                        case Node.TEXT_NODE: {
                            defaultSerializer.writeElementText((Text)childNode, writer, null);
                            break;
                        }
                        default: {
                            defaultSerializer.writeElementNode(childNode, writer, null);
                            break;
                        }
                    }
                }
            }
            defaultSerializer.writeElementEnd(element, writer);
        } catch(Throwable thrown) {
            if(thrown instanceof SmooksException) {
                throw (SmooksException) thrown;
            } else {
                throw new SmooksException("Serailization Error.", thrown);
            }
        }
    }
}
