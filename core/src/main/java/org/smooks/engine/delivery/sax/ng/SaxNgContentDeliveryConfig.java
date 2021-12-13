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
package org.smooks.engine.delivery.sax.ng;

import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksConfigException;
import org.smooks.api.SmooksException;
import org.smooks.api.delivery.ContentDeliveryConfig;
import org.smooks.api.delivery.ContentHandlerBinding;
import org.smooks.api.delivery.Filter;
import org.smooks.api.delivery.FilterBypass;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.config.xpath.Predicate;
import org.smooks.api.resource.config.xpath.SelectorStep;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.api.resource.visitor.sax.ng.AfterVisitor;
import org.smooks.api.resource.visitor.sax.ng.BeforeVisitor;
import org.smooks.api.resource.visitor.sax.ng.ChildrenVisitor;
import org.smooks.engine.delivery.AbstractContentDeliveryConfig;
import org.smooks.engine.delivery.ContentHandlerBindingIndex;
import org.smooks.engine.delivery.DefaultContentHandlerBinding;
import org.smooks.engine.delivery.ordering.Sorter;
import org.smooks.engine.resource.config.DefaultResourceConfig;
import org.smooks.engine.resource.config.ParameterAccessor;
import org.smooks.engine.resource.config.xpath.ElementPositionCounter;
import org.smooks.engine.resource.config.xpath.predicate.PositionPredicateEvaluator;
import org.smooks.engine.resource.config.xpath.step.ElementSelectorStep;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SaxNgContentDeliveryConfig extends AbstractContentDeliveryConfig {
    
    private final Map<String, SaxNgVisitorBindings> saxNgVisitorBindingsCache = new ConcurrentHashMap<>();
    private final ThreadLocal<DocumentBuilder> cachedDocumentBuilder = new ThreadLocal<>();
    private final ContentHandlerBindingIndex<ChildrenVisitor> childVisitorIndex = new ContentHandlerBindingIndex<>();
    private final ContentHandlerBindingIndex<BeforeVisitor> beforeVisitorIndex = new ContentHandlerBindingIndex<>();
    private final ContentHandlerBindingIndex<AfterVisitor> afterVisitorIndex = new ContentHandlerBindingIndex<>();
    private Map<String, SaxNgVisitorBindings> reducedIndex;
    private Integer maxNodeDepth;
    private Boolean rewriteEntities;
    private Boolean maintainElementStack;
    private Boolean reverseVisitOrderOnVisitAfter;
    private Boolean terminateOnVisitorException;
    private Optional<FilterBypass> filterBypass;
    
    public ContentHandlerBindingIndex<BeforeVisitor> getBeforeVisitorIndex() {
        return beforeVisitorIndex;
    }

    public ContentHandlerBindingIndex<ChildrenVisitor> getChildVisitorIndex() {
        return childVisitorIndex;
    }

    public ContentHandlerBindingIndex<AfterVisitor> getAfterVisitorIndex() {
        return afterVisitorIndex;
    }

    @Override
    public FilterBypass getFilterBypass() {
        if (filterBypass == null) {
            filterBypass = Optional.ofNullable(getFilterBypass(beforeVisitorIndex, afterVisitorIndex));
        }
        return filterBypass.orElse(null);
    }

    @Override
    public Filter newFilter(final ExecutionContext executionContext) {
        DocumentBuilder documentBuilder = cachedDocumentBuilder.get();
        if (documentBuilder == null) {
            try {
                documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                cachedDocumentBuilder.set(documentBuilder);
            } catch (ParserConfigurationException e) {
                throw new SmooksException(e);
            }
        }

        return new SaxNgFilter(executionContext, documentBuilder, getCloseSource(), getCloseResult());
    }

    @Override
    public void sort() throws SmooksConfigException {
        beforeVisitorIndex.sort(Sorter.SortOrder.PRODUCERS_FIRST);
        childVisitorIndex.sort(Sorter.SortOrder.PRODUCERS_FIRST);
        afterVisitorIndex.sort(Sorter.SortOrder.CONSUMERS_FIRST);
    }

    @Override
    public void addToExecutionLifecycleSets() throws SmooksConfigException {
        addToExecutionLifecycleSets(beforeVisitorIndex);
        addToExecutionLifecycleSets(afterVisitorIndex);
    }

    public SaxNgVisitorBindings get(String selector) {
        if (reducedIndex == null) {
            synchronized (this) {
                if (reducedIndex == null) {
                    reducedIndex = reduceIndex();
                }
            }
        }

        return reducedIndex.get(selector);
    }
    
    protected Map<String, SaxNgVisitorBindings> reduceIndex() {
        final Map<String, SaxNgVisitorBindings> reducedIndex = new HashMap<>();

        final List<ContentHandlerBinding<BeforeVisitor>> starVBs = new ArrayList<>();
        final List<ContentHandlerBinding<ChildrenVisitor>> starVCs = new ArrayList<>();
        final List<ContentHandlerBinding<AfterVisitor>> starVAs = new ArrayList<>();

        if (beforeVisitorIndex.get("*") != null) {
            starVBs.addAll(beforeVisitorIndex.get("*"));
        }
        if (beforeVisitorIndex.get("//") != null) {
            starVBs.addAll(beforeVisitorIndex.get("//"));
        }
        if (childVisitorIndex.get("*") != null) {
            starVCs.addAll(childVisitorIndex.get("*"));
        }
        if (childVisitorIndex.get("//") != null) {
            starVCs.addAll(childVisitorIndex.get("//"));
        }
        if (afterVisitorIndex.get("*") != null) {
            starVAs.addAll(afterVisitorIndex.get("*"));
        }
        if (afterVisitorIndex.get("//") != null) {
            starVAs.addAll(afterVisitorIndex.get("//"));
        }

        // Now extract the before, child and after visitors for all configured elements...
        Set<String> selectors = new HashSet<>();
        selectors.addAll(beforeVisitorIndex.keySet());
        selectors.addAll(afterVisitorIndex.keySet());

        for (String selector : selectors) {
            final SaxNgVisitorBindings visitorBindings = new SaxNgVisitorBindings();
            final List<ContentHandlerBinding<BeforeVisitor>> beforeVisitors = this.beforeVisitorIndex.getOrDefault(selector, new ArrayList<>());
            final List<ContentHandlerBinding<ChildrenVisitor>> childVisitors = this.childVisitorIndex.getOrDefault(selector, new ArrayList<>());
            final List<ContentHandlerBinding<AfterVisitor>> afterVisitors = this.afterVisitorIndex.getOrDefault(selector, new ArrayList<>());
            final boolean isStar = selector.equals("*") || selector.equals("//");

            // So what's going on with the "*" and "//" resources here?  Basically, we are adding
            // these resources to all targeted elements, accept for "*" and "//" themselves.

            if (!isStar) {
                beforeVisitors.addAll(starVBs);
            }
            visitorBindings.setBeforeVisitors(beforeVisitors);

            if (!isStar) {
                childVisitors.addAll(starVCs);
            }
            visitorBindings.setChildVisitors(childVisitors);

            if (!isStar) {
                afterVisitors.addAll(starVAs);
            }
            visitorBindings.setAfterVisitors(afterVisitors);

            reducedIndex.put(selector, visitorBindings);
        }

        if (ParameterAccessor.getParameterValue(ContentDeliveryConfig.SMOOKS_VISITORS_SORT, Boolean.class, true, this)) {
            sort();
        }

        addPositionCounters(reducedIndex);

        return reducedIndex;
    }

    protected void addPositionCounters(Map<String, SaxNgVisitorBindings> reducedIndex) {
        final Map<String, SaxNgVisitorBindings> reducedIndexCopy = new LinkedHashMap<>(reducedIndex);
        Collection<SaxNgVisitorBindings> elementVisitorMaps = reducedIndexCopy.values();

        for (SaxNgVisitorBindings elementVisitorMap : elementVisitorMaps) {
            addPositionCounters(elementVisitorMap.getBeforeVisitors(), reducedIndex);
            addPositionCounters(elementVisitorMap.getChildVisitors(), reducedIndex);
            addPositionCounters(elementVisitorMap.getAfterVisitors(), reducedIndex);
        }
    }

    private <T extends Visitor> void addPositionCounters(final List<ContentHandlerBinding<T>> contentHandlerBindings, Map<String, SaxNgVisitorBindings> reducedIndex) {
        if (contentHandlerBindings == null) {
            return;
        }

        for (ContentHandlerBinding<? extends Visitor> contentHandlerBinding : contentHandlerBindings) {
            for (SelectorStep selectorStep : contentHandlerBinding.getResourceConfig().getSelectorPath()) {
                if (selectorStep instanceof ElementSelectorStep) {
                    for (Predicate predicate : selectorStep.getPredicates()) {
                        if (predicate instanceof PositionPredicateEvaluator) {
                            final ElementPositionCounter elementPositionCounter = new ElementPositionCounter(selectorStep);

                            ((PositionPredicateEvaluator) predicate).setCounter(elementPositionCounter);
                            addPositionCounter(elementPositionCounter, reducedIndex, (ElementSelectorStep) selectorStep);
                        }
                    }
                }
            }
        }
    }

    private void addPositionCounter(ElementPositionCounter positionCounter, Map<String, SaxNgVisitorBindings> reducedIndex, ElementSelectorStep elementSelectorStep) {
        String targetElementName = elementSelectorStep.getQName().getLocalPart();
        SaxNgVisitorBindings visitorBindings = reducedIndex.get(targetElementName);

        if (visitorBindings == null) {
            visitorBindings = new SaxNgVisitorBindings();
            reducedIndex.put(targetElementName, visitorBindings);
        }

        List<ContentHandlerBinding<BeforeVisitor>> vbs = visitorBindings.getBeforeVisitors();

        if (vbs == null) {
            vbs = new ArrayList<>();
            visitorBindings.setBeforeVisitors(vbs);
        }

        ResourceConfig resourceConfig = new DefaultResourceConfig(targetElementName, new Properties());

        vbs.add(0, new DefaultContentHandlerBinding(positionCounter, resourceConfig));
    }

    public SaxNgVisitorBindings get(String... selectors) {
        SaxNgVisitorBindings saxNgVisitorBindings = saxNgVisitorBindingsCache.get(String.join(":", selectors));
        if (saxNgVisitorBindings == null) {
            saxNgVisitorBindings = new SaxNgVisitorBindings();
            saxNgVisitorBindings.setBeforeVisitors(new ArrayList<>());
            saxNgVisitorBindings.setChildVisitors(new ArrayList<>());
            saxNgVisitorBindings.setAfterVisitors(new ArrayList<>());

            for (String selector : selectors) {
                final SaxNgVisitorBindings visitorBindings = get(selector);

                if (visitorBindings != null) {
                    final List<ContentHandlerBinding<BeforeVisitor>> beforeVisitorBindings = visitorBindings.getBeforeVisitors();
                    final List<ContentHandlerBinding<ChildrenVisitor>> childVisitorBindings = visitorBindings.getChildVisitors();
                    final List<ContentHandlerBinding<AfterVisitor>> afterVisitorBindings = visitorBindings.getAfterVisitors();

                    if (beforeVisitorBindings != null) {
                        for (ContentHandlerBinding<BeforeVisitor> beforeVisitorBinding : beforeVisitorBindings) {
                            final boolean isStar = beforeVisitorBinding.getResourceConfig().getSelectorPath().getSelector().equals("*") || beforeVisitorBinding.getResourceConfig().getSelectorPath().getSelector().equals("//");
                            if (!isStar) {
                                saxNgVisitorBindings.getBeforeVisitors().add(beforeVisitorBinding);
                            }
                        }

                    }
                    if (childVisitorBindings != null) {
                        for (ContentHandlerBinding<ChildrenVisitor> childVisitorBinding : childVisitorBindings) {
                            final boolean isStar = childVisitorBinding.getResourceConfig().getSelectorPath().getSelector().equals("*") || childVisitorBinding.getResourceConfig().getSelectorPath().getSelector().equals("//");
                            if (!isStar) {
                                saxNgVisitorBindings.getChildVisitors().add(childVisitorBinding);
                            }
                        }
                    }
                    if (afterVisitorBindings != null) {
                        for (ContentHandlerBinding<AfterVisitor> afterVisitorBinding : afterVisitorBindings) {
                            final boolean isStar = afterVisitorBinding.getResourceConfig().getSelectorPath().getSelector().equals("*") || afterVisitorBinding.getResourceConfig().getSelectorPath().getSelector().equals("//");
                            if (!isStar) {
                                saxNgVisitorBindings.getAfterVisitors().add(afterVisitorBinding);
                            }
                        }
                    }
                }
            }

            SaxNgVisitorBindings starVisitorBindings = get("*");
            if (starVisitorBindings != null) {
                saxNgVisitorBindings.getBeforeVisitors().addAll(starVisitorBindings.getBeforeVisitors());
                saxNgVisitorBindings.getChildVisitors().addAll(starVisitorBindings.getChildVisitors());
                saxNgVisitorBindings.getAfterVisitors().addAll(starVisitorBindings.getAfterVisitors());
            }
            SaxNgVisitorBindings starStarVisitorBindings = get("//");
            if (starStarVisitorBindings != null) {
                saxNgVisitorBindings.getBeforeVisitors().addAll(starStarVisitorBindings.getBeforeVisitors());
                saxNgVisitorBindings.getChildVisitors().addAll(starStarVisitorBindings.getChildVisitors());
                saxNgVisitorBindings.getAfterVisitors().addAll(starStarVisitorBindings.getAfterVisitors());
            }

            saxNgVisitorBindingsCache.put(String.join(":", selectors), saxNgVisitorBindings);
        }
        
        if (saxNgVisitorBindings.getBeforeVisitors().isEmpty() && saxNgVisitorBindings.getChildVisitors().isEmpty() && saxNgVisitorBindings.getAfterVisitors().isEmpty()) {
            return null;
        } else {
            return saxNgVisitorBindings;
        }
    }

    public boolean isRewriteEntities() {
        if (rewriteEntities == null) {
            rewriteEntities = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.ENTITIES_REWRITE, String.class, "true", this));
        }
        return rewriteEntities;
    }

    public boolean isMaintainElementStack() {
        if (maintainElementStack == null) {
            maintainElementStack = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.MAINTAIN_ELEMENT_STACK, String.class, "true", this));
        }
        return maintainElementStack;
    }

    public boolean isReverseVisitOrderOnVisitAfter() {
        if (reverseVisitOrderOnVisitAfter == null) {
            reverseVisitOrderOnVisitAfter = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.REVERSE_VISIT_ORDER_ON_VISIT_AFTER, String.class, "true", this));
        }
        return reverseVisitOrderOnVisitAfter;
    }

    public boolean isTerminateOnVisitorException() {
        if (terminateOnVisitorException == null) {
            terminateOnVisitorException = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.TERMINATE_ON_VISITOR_EXCEPTION, String.class, "true", this));
        }
        return terminateOnVisitorException;
    }

    public int getMaxNodeDepth() {
        if (maxNodeDepth == null) {
            maxNodeDepth = Integer.parseInt(ParameterAccessor.getParameterValue(Filter.MAX_NODE_DEPTH, String.class, "1", this));
        }
        return maxNodeDepth;
    }
}