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
package org.smooks.engine.delivery.sax.ng;

import org.smooks.api.SmooksException;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.ExecutionContext;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.delivery.event.ExecutionEventListener;
import org.smooks.api.delivery.ContentDeliveryRuntime;
import org.smooks.api.delivery.ContentHandlerBinding;
import org.smooks.api.resource.visitor.sax.ng.AfterVisitor;
import org.smooks.api.resource.visitor.sax.ng.BeforeVisitor;
import org.smooks.api.resource.visitor.sax.ng.ChildrenVisitor;
import org.smooks.api.resource.visitor.sax.ng.ParameterizedVisitor;
import org.smooks.engine.delivery.SmooksContentHandler;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.smooks.engine.delivery.replay.EndElementEvent;
import org.smooks.engine.delivery.replay.StartElementEvent;
import org.smooks.support.SAXUtil;
import org.smooks.api.delivery.sax.TextType;
import org.smooks.engine.delivery.event.EndFragmentEvent;
import org.smooks.engine.delivery.event.StartFragmentEvent;
import org.smooks.api.lifecycle.LifecycleManager;
import org.smooks.engine.lifecycle.VisitCleanupPhase;
import org.smooks.engine.lookup.LifecycleManagerLookup;
import org.smooks.engine.xml.DocType;
import org.smooks.io.Stream;
import org.smooks.support.DomUtils;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class SaxNgHandler extends SmooksContentHandler {
    
    private final StringBuilder cdataNodeBuilder = new StringBuilder();
    private final ExecutionContext executionContext;
    private final Writer writer;
    private final SaxNgContentDeliveryConfig deliveryConfig;
    private final SaxNgVisitorBindings globalVisitorBindings;
    private final ContentDeliveryRuntime contentDeliveryRuntime;
    private final int globalMaxNodeDepth;
    private final boolean maintainElementStack;
    private final boolean reverseVisitOrderOnVisitAfter;
    private final boolean rewriteEntities;
    private final LifecycleManager lifecycleManager;
    private final StringBuilder entityBuilder = new StringBuilder(10);

    private ContentHandlerState currentContentHandlerState = null;
    private Node currentNode = null;
    private Document factory;
    
    @SuppressWarnings("WeakerAccess")
    public SaxNgHandler(final ExecutionContext executionContext) {
        this(executionContext, null);
    }

    public SaxNgHandler(final ExecutionContext executionContext, final SmooksContentHandler parentContentHandler) {
        super(executionContext, parentContentHandler);

        this.executionContext = executionContext;
        this.writer = Stream.out(executionContext);
        contentDeliveryRuntime = executionContext.getContentDeliveryRuntime();
        lifecycleManager = executionContext.getApplicationContext().getRegistry().lookup(new LifecycleManagerLookup());
        deliveryConfig = ((SaxNgContentDeliveryConfig) contentDeliveryRuntime.getContentDeliveryConfig());
        
        final SaxNgVisitorBindings starVisitorBindings = deliveryConfig.get("*");
        final SaxNgVisitorBindings starStarVisitorBindings = deliveryConfig.get("**");

        globalVisitorBindings = starVisitorBindings != null ? starVisitorBindings.merge(starStarVisitorBindings) : starStarVisitorBindings;
        rewriteEntities = deliveryConfig.isRewriteEntities();
        maintainElementStack = deliveryConfig.isMaintainElementStack();
        globalMaxNodeDepth = deliveryConfig.getMaxNodeDepth() == 0 ? Integer.MAX_VALUE : deliveryConfig.getMaxNodeDepth();
        reverseVisitOrderOnVisitAfter = deliveryConfig.isReverseVisitOrderOnVisitAfter();
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void startDocument() {
        try {
            factory = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new SmooksException(e.getMessage(), e);
        }
        currentContentHandlerState = new ContentHandlerState();
    }

    @Override
    public void startElement(final StartElementEvent startEvent) {
        final boolean isRoot = (currentContentHandlerState.getPreviousContentHandlerState() == null);
        final QName elementQName = SAXUtil.toQName(startEvent.uri, startEvent.localName, startEvent.qName);
        final String elementName = elementQName != null ? elementQName.getLocalPart() : null;

        SaxNgVisitorBindings visitorBindings;
        if (isRoot) {
            visitorBindings = deliveryConfig.get(ResourceConfig.DOCUMENT_FRAGMENT_SELECTOR, elementName);
        } else {
            visitorBindings = deliveryConfig.get(elementName);
        }

        if (visitorBindings == null) {
            visitorBindings = globalVisitorBindings;
        }

        if (!maintainElementStack && visitorBindings == null) {
            final ContentHandlerState contentHandlerState = new ContentHandlerState();
            contentHandlerState.setNullProcessor(true);
            contentHandlerState.setPreviousContentHandlerState(currentContentHandlerState);
            currentContentHandlerState = contentHandlerState;
            final StartFragmentEvent<Node> startFragmentEvent = new StartFragmentEvent<>(new NodeFragment(currentNode));
            for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
                executionEventListener.onEvent(startFragmentEvent);
            }
        } else {
            final Element element = factory.createElementNS(elementQName.getNamespaceURI(), elementQName.getPrefix().equals("") ? elementQName.getLocalPart() : elementQName.getPrefix() + ":" + elementQName.getLocalPart());
            for (int i = 0; i < startEvent.attributes.getLength(); i++) {
                if (XMLConstants.NULL_NS_URI.equals(startEvent.attributes.getURI(i))) {
                    element.setAttribute(startEvent.attributes.getLocalName(i), startEvent.attributes.getValue(i));
                } else {
                    element.setAttributeNS(startEvent.attributes.getURI(i), startEvent.attributes.getQName(i), startEvent.attributes.getValue(i));
                }
            }
            
            if (isRoot) {
                if (factory.getFirstChild() != null) {
                    factory.removeChild(factory.getFirstChild());
                }
                factory.appendChild(element);
            } else {
                currentNode.appendChild(element);
                onChildElement(element);
            }

            visitBefore(element, visitorBindings);
        }
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public void endElement(final EndElementEvent endEvent) throws SAXException {
        final Fragment<Node> currentNodeFragment = new NodeFragment(currentNode);
        final EndFragmentEvent endFragmentEvent = new EndFragmentEvent(currentNodeFragment);
        for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
            executionEventListener.onEvent(endFragmentEvent);
        }
        
        if (currentContentHandlerState.getVisitorBindings() != null) {
            List<ContentHandlerBinding<AfterVisitor>> afterVisitorBindings = currentContentHandlerState.getVisitorBindings().getAfterVisitors();

            if (afterVisitorBindings == null && globalVisitorBindings != null) {
                afterVisitorBindings = globalVisitorBindings.getAfterVisitors();
            }
            
            if (afterVisitorBindings != null) {
                if (reverseVisitOrderOnVisitAfter) {
                    // We work through the mappings in reverse order on the end element event...    
                    for (int i = afterVisitorBindings.size() - 1; i >= 0; i--) {
                        visitAfter(afterVisitorBindings.get(i));
                    }
                } else {
                    for (final ContentHandlerBinding<AfterVisitor> afterVisitorBinding : afterVisitorBindings) {
                        visitAfter(afterVisitorBinding);
                    }
                }
            }
            try {
                writer.flush();
            } catch (IOException e) {
                throw new SmooksException(e.getMessage(), e);
            }
        }

        if (currentContentHandlerState.getVisitorBindings() != null) {
            final List<ContentHandlerBinding<? extends Visitor>> visitorBindings = currentContentHandlerState.getVisitorBindings().getVisitorBindings();
            final VisitCleanupPhase visitCleanupPhase = new VisitCleanupPhase(currentNodeFragment, executionContext);
            for (final ContentHandlerBinding<? extends Visitor> visitorBinding : visitorBindings) {
                if (currentNodeFragment.isMatch(visitorBinding.getResourceConfig().getSelectorPath(), executionContext)) {
                    lifecycleManager.applyPhase(visitorBinding.getContentHandler(), visitCleanupPhase);
                }
            }
        }

        executionContext.getMementoCaretaker().forget(currentNodeFragment);
        
        final ContentHandlerState previousContentHandlerState = currentContentHandlerState.getPreviousContentHandlerState();
        final Node parentNode = currentNode.getParentNode();
        if (DomUtils.getDepth(currentNode) >= Math.max(globalMaxNodeDepth, findMaxNodeDepth(currentContentHandlerState))) {
            parentNode.removeChild(currentNode);
        }
        currentNode = parentNode;
        currentContentHandlerState = previousContentHandlerState;
    }
    
    private int findMaxNodeDepth(final ContentHandlerState contentHandlerState) {
        ContentHandlerState previousNodeState = contentHandlerState;
        int maxNodeDepth = 0;
        while (previousNodeState != null) {
            maxNodeDepth = Math.max(previousNodeState.getMaxDepth(), maxNodeDepth);
            previousNodeState = previousNodeState.getPreviousContentHandlerState();
        }
        
        return maxNodeDepth;
    }

    private void visitBefore(final Element element, final SaxNgVisitorBindings saxNgElementVisitorMap) {
        currentNode = element;
        final Fragment<Node> nodeFragment = new NodeFragment(element);
        final ContentHandlerState contentHandlerState = new ContentHandlerState();
        contentHandlerState.setPreviousContentHandlerState(currentContentHandlerState);
        contentHandlerState.setVisitorBindings(saxNgElementVisitorMap);

        currentContentHandlerState = contentHandlerState;
        if (currentContentHandlerState.getVisitorBindings() != null) {
            List<ContentHandlerBinding<BeforeVisitor>> visitBeforeBindings = currentContentHandlerState.getVisitorBindings().getBeforeVisitors();

            if (visitBeforeBindings == null && globalVisitorBindings != null) {
                visitBeforeBindings = globalVisitorBindings.getBeforeVisitors();
            }

            if (visitBeforeBindings != null) {
                int maxNodeDepth = 1;
                for (final ContentHandlerBinding<BeforeVisitor> visitBeforeBinding : visitBeforeBindings) {
                    if (nodeFragment.isMatch(visitBeforeBinding.getResourceConfig().getSelectorPath(), executionContext)) {
                        if (visitBeforeBinding.getContentHandler() instanceof ParameterizedVisitor) {
                            maxNodeDepth = Math.max(maxNodeDepth, ((ParameterizedVisitor) visitBeforeBinding.getContentHandler()).getMaxNodeDepth());
                        }
                        visitBeforeBinding.getContentHandler().visitBefore(element, executionContext);
                    }
                }
                currentContentHandlerState.setMaxDepth(maxNodeDepth);
            }
        }

        final StartFragmentEvent<Node> startFragmentEvent = new StartFragmentEvent<>(nodeFragment);
        for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
            executionEventListener.onEvent(startFragmentEvent);
        }
    }

    private void onChildElement(final Element childElement) {
        if (currentContentHandlerState.getVisitorBindings() != null) {
            List<ContentHandlerBinding<ChildrenVisitor>> childVisitorBindings = currentContentHandlerState.getVisitorBindings().getChildVisitors();
            if (childVisitorBindings == null && globalVisitorBindings != null) {
                childVisitorBindings = globalVisitorBindings.getChildVisitors();
            }
            
            if (childVisitorBindings != null) {
                final Fragment<Node> currentNodeFragment = new NodeFragment(currentNode);
                for (final ContentHandlerBinding<ChildrenVisitor> contentHandlerBinding : childVisitorBindings) {
                    if (currentNodeFragment.isMatch(contentHandlerBinding.getResourceConfig().getSelectorPath(), executionContext)) {
                        contentHandlerBinding.getContentHandler().visitChildElement(childElement, executionContext);
                    }
                }
            }
        }
    }

    private void visitAfter(final ContentHandlerBinding<AfterVisitor> afterVisitorBinding) {
        if (new NodeFragment(currentNode).isMatch(afterVisitorBinding.getResourceConfig().getSelectorPath(), executionContext)) {
            afterVisitorBinding.getContentHandler().visitAfter((Element) currentNode, executionContext);
        }
    }
    
    @Override
    public void characters(final char[] ch, final int start, final int length) {
        if (currentContentHandlerState.getTextType() != TextType.CDATA) {
            doCharacters(ch, start, length);
        } else {
            cdataNodeBuilder.append(ch, start, length);
        }
    }
    
    private void doCharacters(final char[] ch, final int start, final int length) {
        if (!rewriteEntities && currentContentHandlerState.getTextType() == TextType.ENTITY) {
            entityBuilder.setLength(0);

            entityBuilder.append("&#").append((int) ch[start]).append(';');
            char[] newBuf = new char[entityBuilder.length()];
            entityBuilder.getChars(0, newBuf.length, newBuf, 0);
        }

        if (currentNode != null) {
            final CharacterData characterData;
            switch (currentContentHandlerState.getTextType()) {
                case CDATA:
                    characterData = factory.createCDATASection(new String(ch, start, length));
                    break;
                case COMMENT:
                    characterData = factory.createComment(new String(ch, start, length));
                    break;
                case ENTITY:
                    if (!rewriteEntities) {
                        entityBuilder.setLength(0);

                        entityBuilder.append("&#").append((int) ch[start]).append(';');
                        char[] newBuf = new char[entityBuilder.length()];
                        entityBuilder.getChars(0, newBuf.length, newBuf, 0);

                        characterData = factory.createTextNode(new String(newBuf, 0, newBuf.length));
                        break;
                    }
                default:
                    characterData = factory.createTextNode(new String(ch, start, length));
            }
            
            currentNode.appendChild(characterData);

            if (!currentContentHandlerState.isNullProcessor() && currentContentHandlerState.getVisitorBindings() != null) {
                final List<ContentHandlerBinding<ChildrenVisitor>> childVisitorBindings = currentContentHandlerState.getVisitorBindings().getChildVisitors();

                if (childVisitorBindings != null) {
                    final NodeFragment currentNodeFragment = new NodeFragment(currentNode);
                    for (final ContentHandlerBinding<ChildrenVisitor> childrenVisitorBinding : childVisitorBindings) {
                        if (currentNodeFragment.isMatch(childrenVisitorBinding.getResourceConfig().getSelectorPath(), executionContext)) {
                            childrenVisitorBinding.getContentHandler().visitChildText(characterData, executionContext);
                        }
                    }
                }
            }
            
            final CharDataFragmentEvent charFragmentEvent = new CharDataFragmentEvent(new NodeFragment(characterData));
            for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
                executionEventListener.onEvent(charFragmentEvent);
            }

            if ((DomUtils.getDepth(currentNode) + 1) >= Math.max(globalMaxNodeDepth, findMaxNodeDepth(currentContentHandlerState))) {
                currentNode.removeChild(characterData);
            }
        }
    }
    
    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length) {
        characters(ch, start, length);
    }

    @Override
    public void comment(final char[] ch, final int start, final int length) {
        currentContentHandlerState.setTextType(TextType.COMMENT);
        characters(ch, start, length);
        currentContentHandlerState.setTextType(TextType.TEXT);
    }

    @Override
    public void startCDATA() {
        currentContentHandlerState.setTextType(TextType.CDATA);
        cdataNodeBuilder.setLength(0);
    }

    @Override
    public void endCDATA() {
        try {
            final char[] chars = new char[cdataNodeBuilder.length()];

            cdataNodeBuilder.getChars(0, chars.length, chars, 0);
            doCharacters(chars, 0, chars.length);
            currentContentHandlerState.setTextType(TextType.TEXT);
        } finally {
            cdataNodeBuilder.setLength(0);
        }
    }

    @Override
    public void startEntity(final String name) {
        currentContentHandlerState.setTextType(TextType.ENTITY);
    }

    @Override
    public void endEntity(final String name) {
        currentContentHandlerState.setTextType(TextType.TEXT);
    }

    @Override
    public void startDTD(final String name, final String publicId, final String systemId) throws SAXException {
        DocType.setDocType(name, publicId, systemId, null, executionContext);

        if (writer != null) {
            final DocType.DocumentTypeData docTypeData = DocType.getDocType(executionContext);
            if (docTypeData != null) {
                try {
                    DocType.serializeDoctype(docTypeData, writer);
                } catch (IOException e) {
                    throw new SAXException("Failed to serialize DOCTYPE.");
                }
            }
        }
    }
}