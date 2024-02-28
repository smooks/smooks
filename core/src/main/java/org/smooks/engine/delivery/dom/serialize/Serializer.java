/*-
 * ========================LICENSE_START=================================
 * Core
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
package org.smooks.engine.delivery.dom.serialize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.ExecutionContext;
import org.smooks.api.Registry;
import org.smooks.api.SmooksException;
import org.smooks.api.delivery.ContentDeliveryConfig;
import org.smooks.api.delivery.ContentDeliveryRuntime;
import org.smooks.api.delivery.ContentHandlerBinding;
import org.smooks.api.delivery.Filter;
import org.smooks.api.delivery.event.ExecutionEventListener;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.lifecycle.ContentDeliveryConfigLifecycle;
import org.smooks.api.lifecycle.DOMFilterLifecycle;
import org.smooks.api.lifecycle.LifecycleManager;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.visitor.SerializerVisitor;
import org.smooks.engine.delivery.ContentHandlerBindingIndex;
import org.smooks.engine.delivery.dom.DOMContentDeliveryConfig;
import org.smooks.engine.delivery.event.ResourceTargetingExecutionEvent;
import org.smooks.engine.delivery.event.StartFragmentExecutionEvent;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.smooks.engine.lifecycle.SerializationStartedDOMFilterLifecyclePhase;
import org.smooks.engine.lookup.InstanceLookup;
import org.smooks.engine.lookup.LifecycleManagerLookup;
import org.smooks.engine.resource.config.ParameterAccessor;
import org.smooks.engine.resource.config.ResourceConfigurationNotFoundException;
import org.smooks.engine.xml.DocType;
import org.smooks.support.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Optional;

/**
 * Node serializer.
 * <p/>
 * This class uses the {@link ContentDeliveryConfig} and the
 * {@link SerializerVisitor} instances defined there on
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
	private final ContentHandlerBindingIndex<SerializerVisitor> serializerVisitorIndex;
    private final ContentDeliveryRuntime contentDeliveryRuntime;
    /**
     * Default serialization unit.
     */
	private DefaultDOMSerializerVisitor defaultSerializationUnit;
    /**
	 * Global SerializationUnits.
	 */
	private final List globalSUs;
    /**
     * Event Listener.
     */
    private final boolean terminateOnVisitorException;

    /**
	 * Public constructor.
	 * @param node Node to be serialized.
	 * @param executionContext Target device context.
	 */
	public Serializer(Node node, ExecutionContext executionContext) {
        if (node == null) {
            throw new IllegalArgumentException("null 'node' arg passed in method call.");
        } else if (executionContext == null) {
            throw new IllegalArgumentException("null 'executionContext' arg passed in method call.");
        }
        this.node = node;
        this.executionContext = executionContext;
        contentDeliveryRuntime = executionContext.getContentDeliveryRuntime();
        // Get the delivery context for the device.
    /*
      Target content delivery config.
     */
        DOMContentDeliveryConfig deliveryConfig = (DOMContentDeliveryConfig) contentDeliveryRuntime.getContentDeliveryConfig();
        // Initialise the serializationUnits member
        serializerVisitorIndex = deliveryConfig.getSerializerVisitorIndex();

        globalSUs = serializerVisitorIndex.get("*", "//");

        // Set the default SerializationUnit
    /*
      Turn default serialization on/off.  Default is "true".
     */
        boolean defaultSerializationOn = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.DEFAULT_SERIALIZATION_ON, String.class, "true", deliveryConfig));
        if (defaultSerializationOn) {
            defaultSerializationUnit = new DefaultDOMSerializerVisitor();
            boolean rewriteEntities = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.ENTITIES_REWRITE, String.class, "true", deliveryConfig));
            defaultSerializationUnit.setRewriteEntities(Optional.of(rewriteEntities));
            defaultSerializationUnit.postConstruct();
        }
        terminateOnVisitorException = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.TERMINATE_ON_VISITOR_EXCEPTION, String.class, "true", deliveryConfig));
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
        if (writer == null) {
            throw new IllegalArgumentException("null 'writer' arg passed in method call.");
        }

        // Register the DOM phase events...
        Registry registry = executionContext.getApplicationContext().getRegistry();
        LifecycleManager lifecycleManager = registry.lookup(new LifecycleManagerLookup());
        SerializationStartedDOMFilterLifecyclePhase serializationStartedDOMFilterLifecyclePhase = new SerializationStartedDOMFilterLifecyclePhase(executionContext);
        for (DOMFilterLifecycle domFilterLifecycle : registry.lookup(new InstanceLookup<>(DOMFilterLifecycle.class)).values()) {
            lifecycleManager.applyPhase(domFilterLifecycle, serializationStartedDOMFilterLifecyclePhase);
        }

        for (DOMFilterLifecycle domFilterLifecycle : executionContext.getApplicationContext().getRegistry().lookup(new InstanceLookup<>(DOMFilterLifecycle.class)).values()) {
            domFilterLifecycle.onSerializationStarted(executionContext);
        }

        if (node instanceof Document) {
            Document doc = (Document) node;
            Element rootElement = doc.getDocumentElement();

            DocType.DocumentTypeData docTypeData = DocType.getDocType(executionContext);
            if (docTypeData != null) {
                DocType.serializeDoctype(docTypeData, writer);
                if (docTypeData.getXmlns() != null) {
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
            for (int i = 0; i < nodeCount; i++) {
                Node childNode = deliveryNodes.item(i);
                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    recursiveDOMWrite((Element) childNode, writer, isRoot);
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
		SerializerVisitor elementSU;
		NodeList children = element.getChildNodes();

		elementSU = getSerializationUnit(element, isRoot);
		try {
            if(elementSU != null) {
                elementSU.writeStartElement(element, writer, executionContext);
            }
            
            if(children != null && children.getLength() > 0) {
				int childCount = children.getLength();

				for(int i = 0; i < childCount; i++) {
					Node childNode = children.item(i);

                    if(elementSU != null) {
                        if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                            if (elementSU.writeChildElements()) {
                                recursiveDOMWrite((Element) childNode, writer, false);
                            }
                        } else {
                            elementSU.writeCharacterData(childNode, writer, executionContext);
                        }
                    } else if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                        recursiveDOMWrite((Element) childNode, writer, false);
                    }
                }
			}
            if(elementSU != null) {
    			elementSU.writeEndElement(element, writer, executionContext);
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
    private SerializerVisitor getSerializationUnit(Element element, boolean isRoot) {
        String elementName = DomUtils.getName(element);
        List<ContentHandlerBinding<SerializerVisitor>> serializerVisitorBindings;

        final Fragment<Node> nodeFragment = new NodeFragment(element);
        // Register the "presence" of the element...
        for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
            executionEventListener.onEvent(new StartFragmentExecutionEvent(nodeFragment));
        }

        if (isRoot) {
            // The document as a whole (root node) can also be targeted through the "#document" selector.
            serializerVisitorBindings = serializerVisitorIndex.get(new String[]{ResourceConfig.DOCUMENT_FRAGMENT_SELECTOR, elementName});
        } else {
            serializerVisitorBindings = serializerVisitorIndex.get(elementName);
        }

        if (serializerVisitorBindings == null || serializerVisitorBindings.isEmpty()) {
            serializerVisitorBindings = globalSUs;
        }

        if (serializerVisitorBindings != null) {
            for (final ContentHandlerBinding<SerializerVisitor> serializerVisitorBinding : serializerVisitorBindings) {
                final ResourceConfig resourceConfig = serializerVisitorBinding.getResourceConfig();

                // Make sure the serialization unit is targeted at this element.
                if (!nodeFragment.isMatch(resourceConfig.getSelectorPath(), executionContext)) {
                    continue;
                }

                // Register the targeting event...
                for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
                    executionEventListener.onEvent(new ResourceTargetingExecutionEvent(nodeFragment, resourceConfig));
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Applying serialisation resource [" + resourceConfig + "] to element [" + DomUtils.getXPath(element) + "].");
                }

                // This is the one, return it...
                return serializerVisitorBinding.getContentHandler();
            }
        }

        return defaultSerializationUnit;
    }

    private static final DefaultDOMSerializerVisitor defaultSerializer = new DefaultDOMSerializerVisitor();

    static {
        defaultSerializer.setCloseEmptyElements(Optional.of(true));
        defaultSerializer.postConstruct();
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
            defaultSerializer.writeStartElement(element, writer, null);

            if(children != null && children.getLength() > 0) {
                int childCount = children.getLength();

                for(int i = 0; i < childCount; i++) {
                    Node childNode = children.item(i);
                    if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                        recursiveDOMWrite((Element)childNode, writer);
                    } else {
                        defaultSerializer.writeCharacterData(childNode, writer, null);
                    }
                }
            }
            defaultSerializer.writeEndElement(element, writer, null);
        } catch(Throwable thrown) {
            if(thrown instanceof SmooksException) {
                throw (SmooksException) thrown;
            } else {
                throw new SmooksException("Serailization Error.", thrown);
            }
        }
    }
}
