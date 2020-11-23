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
package org.smooks.delivery.sax.ng;

import org.smooks.SmooksException;
import org.smooks.cdr.ResourceConfig;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.ContentHandlerBinding;
import org.smooks.delivery.Fragment;
import org.smooks.delivery.SmooksContentHandler;
import org.smooks.delivery.Visitor;
import org.smooks.delivery.replay.EndElementEvent;
import org.smooks.delivery.replay.StartElementEvent;
import org.smooks.delivery.sax.SAXUtil;
import org.smooks.delivery.sax.TextType;
import org.smooks.event.ExecutionEventListener;
import org.smooks.event.types.ElementPresentEvent;
import org.smooks.lifecycle.LifecycleManager;
import org.smooks.lifecycle.phase.VisitCleanupPhase;
import org.smooks.registry.lookup.LifecycleManagerLookup;
import org.smooks.xml.DocType;
import org.smooks.xml.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SaxNgHandler extends SmooksContentHandler {
    
    private final StringBuilder cdataNodeBuilder = new StringBuilder();
    private final ExecutionContext executionContext;
    private final Writer writer;
    private final SaxNgContentDeliveryConfig deliveryConfig;
    private final Map<String, SaxNgVisitorBindings> elementVisitorMapByElementName;
    private final SaxNgVisitorBindings globalVisitorBindings;
    private final ExecutionEventListener executionEventListener;
    private final DynamicSaxNgElementVisitorList dynamicVisitorList;
    private final int globalMaxNodeDepth;
    private final boolean maintainElementStack;
    private final boolean reverseVisitOrderOnVisitAfter;
    private final boolean rewriteEntities;
    private final LifecycleManager lifecycleManager;

    private NodeState currentNodeState = null;
    private Document factory;

    private static class CopyUserDataHandler implements UserDataHandler {
        @Override
        public void handle(final short operation, final String key, final Object data, final Node src, final Node dst) {
            dst.setUserData(key, data, new CopyUserDataHandler());
        }
    }

    @SuppressWarnings("WeakerAccess")
    public SaxNgHandler(final ExecutionContext executionContext) {
        this(executionContext, null);
    }

    public SaxNgHandler(final ExecutionContext executionContext, final SmooksContentHandler parentContentHandler) {
        super(executionContext, parentContentHandler);

        this.executionContext = executionContext;
        this.writer = executionContext.getWriter();
        executionEventListener = executionContext.getEventListener();
        lifecycleManager = executionContext.getApplicationContext().getRegistry().lookup(new LifecycleManagerLookup());
        deliveryConfig = ((SaxNgContentDeliveryConfig) executionContext.getDeliveryConfig());
        elementVisitorMapByElementName = deliveryConfig.getOptimizedVisitorConfig();

        
        final SaxNgContentDeliveryConfig contentDeliveryConfig = (SaxNgContentDeliveryConfig) executionContext.getDeliveryConfig();
        final SaxNgVisitorBindings starVisitorConfigs = elementVisitorMapByElementName.get("*");
        final SaxNgVisitorBindings starStarVisitorConfigs = elementVisitorMapByElementName.get("**");

        globalVisitorBindings = starVisitorConfigs != null ? starVisitorConfigs.merge(starStarVisitorConfigs) : starStarVisitorConfigs;
        rewriteEntities = contentDeliveryConfig.isRewriteEntities();
        maintainElementStack = contentDeliveryConfig.isMaintainElementStack();
        globalMaxNodeDepth = contentDeliveryConfig.getMaxNodeDepth() == 0 ? Integer.MAX_VALUE : contentDeliveryConfig.getMaxNodeDepth();
        reverseVisitOrderOnVisitAfter = contentDeliveryConfig.isReverseVisitOrderOnVisitAfter();
        
        final DynamicSaxNgElementVisitorList dynamicVisitorList = DynamicSaxNgElementVisitorList.getList(executionContext);
        this.dynamicVisitorList = dynamicVisitorList == null ? new DynamicSaxNgElementVisitorList(executionContext) : dynamicVisitorList;
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
        currentNodeState = new NodeState();
    }

    @Override
    public void startElement(final StartElementEvent startEvent) {
        final boolean isRoot = (currentNodeState.getParentNodeState() == null);
        final QName elementQName = SAXUtil.toQName(startEvent.uri, startEvent.localName, startEvent.qName);
        final String elementName = elementQName != null ? elementQName.getLocalPart() : null;

        SaxNgVisitorBindings visitorBindings;
        if (isRoot) {
            visitorBindings = deliveryConfig.getCombinedOptimizedConfig(new String[]{ResourceConfig.DOCUMENT_FRAGMENT_SELECTOR, elementName});
        } else {
            visitorBindings = elementVisitorMapByElementName.get(elementName);
        }

        if (visitorBindings == null) {
            visitorBindings = globalVisitorBindings;
        }

        if (!maintainElementStack && visitorBindings == null) {
            final NodeState nodeState = new NodeState();
            nodeState.setNullProcessor(true);
            nodeState.setParentNodeState(currentNodeState);
            currentNodeState = nodeState;
            if (executionEventListener != null) {
                executionEventListener.onEvent(new ElementPresentEvent(currentNodeState.getElement()));
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
            element.setUserData("id", UUID.randomUUID().toString(), new CopyUserDataHandler());
            
            if (!isRoot) {
                currentNodeState.getElement().appendChild(element);
                onChildElement(element);
            } else {
                if (currentNodeState.getElement() != null) {
                    factory.removeChild(currentNodeState.getElement());
                }
                factory.appendChild(element);
            }

            if (executionEventListener != null) {
                executionEventListener.onEvent(new ElementPresentEvent(element));
            }

            visitBefore(element, visitorBindings);
        }
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public void endElement(final EndElementEvent endEvent) throws SAXException {
        for (final AfterVisitor dynamicAfterVisitor : dynamicVisitorList.getVisitAfters()) {
            dynamicAfterVisitor.visitAfter(currentNodeState.getElement(), executionContext);
        }

        if (currentNodeState.getVisitorBindings() != null) {
            List<ContentHandlerBinding<AfterVisitor>> afterVisitorBindings = currentNodeState.getVisitorBindings().getAfterVisitors();

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

        if (currentNodeState.getVisitorBindings() != null) {
            final List<ContentHandlerBinding<? extends Visitor>> visitorBindings = currentNodeState.getVisitorBindings().getVisitorBindings();
            final VisitCleanupPhase visitCleanupPhase = new VisitCleanupPhase(new Fragment(currentNodeState.getElement()), executionContext);
            for (final ContentHandlerBinding<? extends Visitor> visitorBinding : visitorBindings) {
                if (visitorBinding.getResourceConfig().getSelectorPath().isTargetedAtElement(currentNodeState.getElement(), executionContext)) {
                    lifecycleManager.applyPhase(visitorBinding.getContentHandler(), visitCleanupPhase);
                }
            }
        }

        executionContext.getMementoCaretaker().forget(currentNodeState.getElement());
        
        final NodeState parentNodeState = currentNodeState.getParentNodeState();
        if (parentNodeState != null && parentNodeState.getElement() != null && DomUtils.getDepth(currentNodeState.getElement()) >= Math.max(globalMaxNodeDepth, findMaxNodeDepth(currentNodeState))) {
            parentNodeState.getElement().removeChild(currentNodeState.getElement());
        }
        currentNodeState = parentNodeState;
    }
    
    private int findMaxNodeDepth(final NodeState nodeState) {
        NodeState previousNodeState = nodeState;
        int maxNodeDepth = 0;
        while (previousNodeState != null) {
            maxNodeDepth = Math.max(previousNodeState.getMaxDepth(), maxNodeDepth);
            previousNodeState = previousNodeState.getParentNodeState();
        }
        
        return maxNodeDepth;
    }

    private void visitBefore(final Element element, final SaxNgVisitorBindings saxNgElementVisitorMap) {
        final NodeState nodeState = new NodeState();
        nodeState.setParentNodeState(currentNodeState);
        nodeState.setElement(element);
        nodeState.setVisitorBindings(saxNgElementVisitorMap);

        currentNodeState = nodeState;
        if (currentNodeState.getVisitorBindings() != null) {
            List<ContentHandlerBinding<BeforeVisitor>> visitBeforeBindings = currentNodeState.getVisitorBindings().getBeforeVisitors();

            if (visitBeforeBindings == null) {
                visitBeforeBindings = globalVisitorBindings.getBeforeVisitors();
            }

            if (visitBeforeBindings != null) {
                int maxNodeDepth = 1;
                for (final ContentHandlerBinding<BeforeVisitor> visitBeforeBinding : visitBeforeBindings) {
                    if (visitBeforeBinding.getResourceConfig().getSelectorPath().isTargetedAtElement(currentNodeState.getElement(), executionContext)) {
                        if (visitBeforeBinding.getContentHandler() instanceof ParameterizedVisitor) {
                            maxNodeDepth = Math.max(maxNodeDepth, ((ParameterizedVisitor) visitBeforeBinding.getContentHandler()).getMaxNodeDepth());
                        }
                        visitBeforeBinding.getContentHandler().visitBefore(currentNodeState.getElement(), executionContext);
                    }
                }
                currentNodeState.setMaxDepth(maxNodeDepth);
            }
        }
        
        for (final BeforeVisitor dynamicBeforeVisitor : dynamicVisitorList.getVisitBefores()) {
            dynamicBeforeVisitor.visitBefore(currentNodeState.getElement(), executionContext);
        }
    }

    private void onChildElement(final Element childElement) {
        if (currentNodeState.getVisitorBindings() != null) {
            List<ContentHandlerBinding<ChildrenVisitor>> childVisitorBindings = currentNodeState.getVisitorBindings().getChildVisitors();
            if (childVisitorBindings == null && globalVisitorBindings != null) {
                childVisitorBindings = globalVisitorBindings.getChildVisitors();
            }
            
            if (childVisitorBindings != null) {
                for (final ContentHandlerBinding<ChildrenVisitor> contentHandlerBinding : childVisitorBindings) {
                    if (contentHandlerBinding.getResourceConfig().getSelectorPath().isTargetedAtElement(currentNodeState.getElement(), executionContext)) {
                        contentHandlerBinding.getContentHandler().visitChildElement(childElement, executionContext);
                    }
                }
            }
        }

        for (final ChildrenVisitor dynamicChildrenVisitor : dynamicVisitorList.getChildVisitors()) {
            dynamicChildrenVisitor.visitChildElement(childElement, executionContext);
        }
    }

    private void visitAfter(final ContentHandlerBinding<AfterVisitor> afterVisitorBinding) {
        if (afterVisitorBinding.getResourceConfig().getSelectorPath().isTargetedAtElement(currentNodeState.getElement(), executionContext)) {
            afterVisitorBinding.getContentHandler().visitAfter(currentNodeState.getElement(), executionContext);
        }
    }
    
    @Override
    public void characters(final char[] ch, final int start, final int length) {
        if (currentNodeState.getTextType() != TextType.CDATA) {
            doCharacters(ch, start, length);
        } else {
            cdataNodeBuilder.append(ch, start, length);
        }
    }

    private final StringBuilder entityBuilder = new StringBuilder(10);

    private void doCharacters(final char[] ch, final int start, final int length) {
        if (!rewriteEntities && currentNodeState.getTextType() == TextType.ENTITY) {
            entityBuilder.setLength(0);

            entityBuilder.append("&#").append((int) ch[start]).append(';');
            char[] newBuf = new char[entityBuilder.length()];
            entityBuilder.getChars(0, newBuf.length, newBuf, 0);
        }

        if (currentNodeState != null && currentNodeState.getElement() != null) {
            final Node node;
            switch (currentNodeState.getTextType()) {
                case CDATA:
                    node = factory.createCDATASection(new String(ch, start, length));
                    break;
                case COMMENT:
                    node = factory.createComment(new String(ch, start, length));
                    break;
                case ENTITY:
                    if (!rewriteEntities) {
                        entityBuilder.setLength(0);

                        entityBuilder.append("&#").append((int) ch[start]).append(';');
                        char[] newBuf = new char[entityBuilder.length()];
                        entityBuilder.getChars(0, newBuf.length, newBuf, 0);

                        node = factory.createTextNode(new String(newBuf, 0, newBuf.length));
                        break;
                    }
                default:
                    node = factory.createTextNode(new String(ch, start, length));
            }
            
            final Element clonedParentElement = (Element) currentNodeState.getElement().cloneNode(false);
            clonedParentElement.appendChild(node.cloneNode(false));

            if ((DomUtils.getDepth(currentNodeState.getElement()) + 1) < Math.max(globalMaxNodeDepth, findMaxNodeDepth(currentNodeState))) {
                currentNodeState.getElement().appendChild(node);
            }
            
            if (!currentNodeState.isNullProcessor() && currentNodeState.getVisitorBindings() != null) {
                final List<ContentHandlerBinding<ChildrenVisitor>> childVisitorBindings = currentNodeState.getVisitorBindings().getChildVisitors();

                if (childVisitorBindings != null) {
                    for (final ContentHandlerBinding<ChildrenVisitor> childrenVisitorBinding : childVisitorBindings) {
                        if (childrenVisitorBinding.getResourceConfig().getSelectorPath().isTargetedAtElement(currentNodeState.getElement(), executionContext)) {
                            childrenVisitorBinding.getContentHandler().visitChildText(clonedParentElement, executionContext);
                        }
                    }
                }
            }

            for (ChildrenVisitor dynamicChildrenVisitor : dynamicVisitorList.getChildVisitors()) {
                dynamicChildrenVisitor.visitChildText(clonedParentElement, executionContext);
            }
        }
    }
    
    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length) {
        characters(ch, start, length);
    }

    @Override
    public void comment(final char[] ch, final int start, final int length) {
        currentNodeState.setTextType(TextType.COMMENT);
        characters(ch, start, length);
        currentNodeState.setTextType(TextType.TEXT);
    }

    @Override
    public void startCDATA() {
        currentNodeState.setTextType(TextType.CDATA);
        cdataNodeBuilder.setLength(0);
    }

    @Override
    public void endCDATA() {
        try {
            final char[] chars = new char[cdataNodeBuilder.length()];

            cdataNodeBuilder.getChars(0, chars.length, chars, 0);
            doCharacters(chars, 0, chars.length);
            currentNodeState.setTextType(TextType.TEXT);
        } finally {
            cdataNodeBuilder.setLength(0);
        }
    }

    @Override
    public void startEntity(final String name) {
        currentNodeState.setTextType(TextType.ENTITY);
    }

    @Override
    public void endEntity(final String name) {
        currentNodeState.setTextType(TextType.TEXT);
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
