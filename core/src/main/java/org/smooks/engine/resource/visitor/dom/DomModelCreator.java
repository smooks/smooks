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
package org.smooks.engine.resource.visitor.dom;

import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.api.TypedKey;
import org.smooks.api.delivery.event.ExecutionEvent;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.delivery.ordering.Producer;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.visitor.sax.ng.AfterVisitor;
import org.smooks.api.resource.visitor.sax.ng.BeforeVisitor;
import org.smooks.engine.delivery.event.EndFragmentEvent;
import org.smooks.engine.delivery.event.StartFragmentEvent;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.smooks.engine.delivery.sax.ng.CharDataFragmentEvent;
import org.smooks.engine.delivery.sax.ng.session.SessionAwareExecutionEventListener;
import org.smooks.engine.resource.config.xpath.IndexedSelectorPath;
import org.smooks.engine.resource.config.xpath.step.ElementSelectorStep;
import org.smooks.support.DomUtils;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Collections;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * DOM Node Model creator.
 * <p/>
 * Adds the visited element as a node model.
 *
 * <h2>Mixing DOM and SAX</h2>
 * When used with SAX filtering, this visitor will construct a DOM Fragment of the visited
 * element.  This allows DOM utilities to be used in a Streaming environment.
 * <p/>
 * When 1+ model are nested inside each other, outer model will never contain data from the
 * inner model i.e. the same fragments will never cooexist inside two model.
 * <p/>
 * Take the following message as an example:
 * <pre>
 * &lt;order id='332'&gt;
 *     &lt;header&gt;
 *         &lt;customer number="123"&gt;Joe&lt;/customer&gt;
 *     &lt;/header&gt;
 *     &lt;order-items&gt;
 *         &lt;order-item id='1'&gt;
 *             &lt;product&gt;1&lt;/product&gt;
 *             &lt;quantity&gt;2&lt;/quantity&gt;
 *             &lt;price&gt;8.80&lt;/price&gt;
 *         &lt;/order-item&gt;
 *         &lt;order-item id='2'&gt;
 *             &lt;product&gt;2&lt;/product&gt;
 *             &lt;quantity&gt;2&lt;/quantity&gt;
 *             &lt;price&gt;8.80&lt;/price&gt;
 *         &lt;/order-item&gt;
 *         &lt;order-item id='3'&gt;
 *             &lt;product&gt;3&lt;/product&gt;
 *             &lt;quantity&gt;2&lt;/quantity&gt;
 *             &lt;price&gt;8.80&lt;/price&gt;
 *         &lt;/order-item&gt;
 *    &lt;/order-items&gt;
 * &lt;/order&gt;
 * </pre>
 * The {@link DomModelCreator} can be configured to create model for the "order" and "order-item"
 * message fragments:
 * <pre>
 * &lt;resource-config selector="order,order-item"&gt;
 *     &lt;resource&gt;org.smooks.engine.delivery.DomModelCreator&lt;/resource&gt;
 * &lt;/resource-config&gt;
 * </pre>
 * In this case, the "order" model will never contain "order-item" model data (order-item elements are nested
 * inside the order element).  The in memory model for the "order" will simply be:
 * <pre>
 * &lt;order id='332'&gt;
 *     &lt;header&gt;
 *         &lt;customer number="123"&gt;Joe&lt;/customer&gt;
 *     &lt;/header&gt;
 *     &lt;order-items /&gt;
 * &lt;/order&gt;
 * </pre>
 * Added to this is the fact that there will only ever be 0 or 1 "order-item" model in memory
 * at any given time, with each new "order-item" model overwriting the previous "order-item" model.
 * All this ensures that the memory footprint is kept to a minimum.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DomModelCreator implements BeforeVisitor, AfterVisitor, Producer {
    private static final TypedKey<Stack<DOMCreator>> DOM_CREATOR_STACK_TYPED_KEY = new TypedKey<>();
    private final DocumentBuilder documentBuilder;

    @Inject
    private ResourceConfig resourceConfig;

    public DomModelCreator() throws ParserConfigurationException {
        documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    @Override
    public Set<String> getProducts() {
        if (resourceConfig.getSelectorPath() instanceof IndexedSelectorPath) {
            return Stream.of(((ElementSelectorStep) ((IndexedSelectorPath) resourceConfig.getSelectorPath()).getTargetSelectorStep()).getQName().getLocalPart()).collect(Collectors.toSet());
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        // Push a new DOMCreator onto the DOMCreator stack and install it in the
        // Dynamic Vistor list in the SAX handler...
        pushCreator(new DOMCreator(executionContext), executionContext);
    }

    private void addNodeModel(Element element, ExecutionContext executionContext) {
        DOMModel nodeModel = DOMModel.getModel(executionContext);
        nodeModel.getModels().put(DomUtils.getName(element), element);
    }

    private void pushCreator(DOMCreator domCreator, ExecutionContext executionContext) {
        Stack<DOMCreator> domCreatorStack = executionContext.get(DOM_CREATOR_STACK_TYPED_KEY);

        if (domCreatorStack == null) {
            domCreatorStack = new Stack<>();
            executionContext.put(DOM_CREATOR_STACK_TYPED_KEY, domCreatorStack);
        } else if (!domCreatorStack.isEmpty()) {
            // We need to remove the current DOMCreator from the dynamic visitor list because
            // we want to stop nodes being added to it and instead, have them added to the new
            // DOM.  This prevents a single huge DOM being created for a huge message (being processed
            // via SAX) because it maintains a hierarchy of model. Inner model can represent collection
            // entry instances, with a single model for a single collection entry only being held in memory
            // at any point in time i.e. old ones are overwritten and so freed for GC.
            executionContext.getContentDeliveryRuntime().removeExecutionEventListener(domCreatorStack.peek());
        }

        executionContext.getContentDeliveryRuntime().addExecutionEventListener(domCreator);
        domCreatorStack.push(domCreator);
    }

    public Document popCreator(ExecutionContext executionContext) {
        Stack<DOMCreator> domCreatorStack = executionContext.get(DOM_CREATOR_STACK_TYPED_KEY);

        if (domCreatorStack == null) {
            throw new IllegalStateException("No DOM Creator Stack available.");
        } else {
            try {
                // Remove the current DOMCreators from the dynamic visitor list...
                if (!domCreatorStack.isEmpty()) {
                    DOMCreator removedCreator = domCreatorStack.pop();
                    executionContext.getContentDeliveryRuntime().removeExecutionEventListener(removedCreator);

                    return removedCreator.document;
                } else {
                    return null;
                }
            } finally {
                // Reinstate parent DOMCreators in the dynamic visitor list...
                if (!domCreatorStack.isEmpty()) {
                    executionContext.getContentDeliveryRuntime().addExecutionEventListener(domCreatorStack.peek());
                }
            }
        }
    }

    @Override
    public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
        // Pop the DOMCreator off the DOMCreator stack and uninstall it from the
        // Dynamic Vistor list in the SAX handler...
        popCreator(executionContext);
    }

    private class DOMCreator extends SessionAwareExecutionEventListener {

        private final Document document;
        private final ExecutionContext executionContext;
        private Node currentNode;

        private DOMCreator(ExecutionContext executionContext) {
            super(executionContext);
            document = documentBuilder.newDocument();
            document.setStrictErrorChecking(false);
            currentNode = document;
            this.executionContext = executionContext;
        }

        @Override
        public void doOnEvent(ExecutionEvent executionEvent) {
            if (executionEvent instanceof StartFragmentEvent) {
                StartFragmentEvent<NodeFragment> startFragmentEvent = (StartFragmentEvent<NodeFragment>) executionEvent;
                Fragment<NodeFragment> fragment = startFragmentEvent.getFragment();
                Element importNode = (Element) document.importNode((Node) fragment.unwrap(), true);

                if (currentNode == document) {
                    addNodeModel(importNode, executionContext);
                }

                currentNode.appendChild(importNode);
                currentNode = importNode;
            } else if (executionEvent instanceof CharDataFragmentEvent) {
                if (currentNode == document) {
                    // Just ignore for now...
                    return;
                }

                CharacterData characterData = (CharacterData) ((CharDataFragmentEvent) executionEvent).getFragment().unwrap();
                String textContent = characterData.getTextContent();
                if (textContent.trim().isEmpty()) {
                    // Ignore pure whitespace...
                    return;
                }

                switch (characterData.getNodeType()) {
                    case Node.TEXT_NODE:
                    case Node.ENTITY_NODE:
                        currentNode.appendChild(document.createTextNode(textContent));
                        break;
                    case Node.CDATA_SECTION_NODE:
                        currentNode.appendChild(document.createCDATASection(textContent));
                        break;
                    case Node.COMMENT_NODE:
                        currentNode.appendChild(document.createComment(textContent));
                        break;
                }
            } else if (executionEvent instanceof EndFragmentEvent) {
                currentNode = currentNode.getParentNode();
            }
        }
    }
}