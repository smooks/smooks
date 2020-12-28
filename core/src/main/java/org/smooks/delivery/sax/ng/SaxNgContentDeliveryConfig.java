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

import org.smooks.cdr.ParameterAccessor;
import org.smooks.cdr.ResourceConfig;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.cdr.xpath.SelectorStep;
import org.smooks.cdr.xpath.evaluators.equality.ElementIndexCounter;
import org.smooks.cdr.xpath.evaluators.equality.IndexEvaluator;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.*;
import org.smooks.delivery.ordering.Sorter;

import javax.xml.namespace.QName;
import java.util.*;

public class SaxNgContentDeliveryConfig extends AbstractContentDeliveryConfig {
    
    private final SelectorTable<ChildrenVisitor> childVisitors = new SelectorTable<>();
    private final SelectorTable<BeforeVisitor> beforeVisitors = new SelectorTable<>();
    private final SelectorTable<AfterVisitor> afterVisitors = new SelectorTable<>();
    private Map<String, SaxNgVisitorBindings> reducedSelectorTable;
    private Integer maxNodeDepth;
    private Boolean rewriteEntities;
    private Boolean maintainElementStack;
    private Boolean reverseVisitOrderOnVisitAfter;
    private Boolean terminateOnVisitorException;
    private FilterBypass filterBypass;

    public SelectorTable<BeforeVisitor> getBeforeVisitorSelectorTable() {
        return beforeVisitors;
    }

    public SelectorTable<ChildrenVisitor> getChildVisitorSelectorTable() {
        return childVisitors;
    }

    public SelectorTable<AfterVisitor> getAfterVisitorSelectorTable() {
        return afterVisitors;
    }
    
    @Override
    public FilterBypass getFilterBypass() {
        if (filterBypass == null) {
            filterBypass = getFilterBypass(beforeVisitors, afterVisitors);
        }
        return filterBypass;
    }

    @Override
    public Filter newFilter(final ExecutionContext executionContext) {
        return new SaxNgFilter(executionContext);
    }

    @Override
    public void sort() throws SmooksConfigurationException {
        beforeVisitors.sort(Sorter.SortOrder.PRODUCERS_FIRST);
        childVisitors.sort(Sorter.SortOrder.PRODUCERS_FIRST);
        afterVisitors.sort(Sorter.SortOrder.CONSUMERS_FIRST);
    }

    public void addToExecutionLifecycleSets() throws SmooksConfigurationException {
        addToExecutionLifecycleSets(beforeVisitors);
        addToExecutionLifecycleSets(afterVisitors);
    }

    public SaxNgVisitorBindings get(String selector) {
        if (reducedSelectorTable == null) {
            this.reducedSelectorTable = reduceSelectorTables();
        }

        return reducedSelectorTable.get(selector);
    }
    
    protected Map<String, SaxNgVisitorBindings> reduceSelectorTables() {
        final Map<String, SaxNgVisitorBindings> reducedSelectorTable = new HashMap<>();

        final List<ContentHandlerBinding<BeforeVisitor>> starVBs = new ArrayList<>();
        final List<ContentHandlerBinding<ChildrenVisitor>> starVCs = new ArrayList<>();
        final List<ContentHandlerBinding<AfterVisitor>> starVAs = new ArrayList<>();

        if (beforeVisitors.get("*") != null) {
            starVBs.addAll(beforeVisitors.get("*"));
        }
        if (beforeVisitors.get("**") != null) {
            starVBs.addAll(beforeVisitors.get("**"));
        }
        if (childVisitors.get("*") != null) {
            starVCs.addAll(childVisitors.get("*"));
        }
        if (childVisitors.get("**") != null) {
            starVCs.addAll(childVisitors.get("**"));
        }
        if (afterVisitors.get("*") != null) {
            starVAs.addAll(afterVisitors.get("*"));
        }
        if (afterVisitors.get("**") != null) {
            starVAs.addAll(afterVisitors.get("**"));
        }

        // Now extract the before, child and after visitors for all configured elements...
        Set<String> selectors = new HashSet<>();
        selectors.addAll(beforeVisitors.keySet());
        selectors.addAll(afterVisitors.keySet());

        for (String selector : selectors) {
            final SaxNgVisitorBindings visitorBindings = new SaxNgVisitorBindings();
            final List<ContentHandlerBinding<BeforeVisitor>> beforeVisitors = this.beforeVisitors.getOrDefault(selector, new ArrayList<>());
            final List<ContentHandlerBinding<ChildrenVisitor>> childVisitors = this.childVisitors.getOrDefault(selector, new ArrayList<>());
            final List<ContentHandlerBinding<AfterVisitor>> afterVisitors = this.afterVisitors.getOrDefault(selector, new ArrayList<>());
            final boolean isStar = (selector.equals("*") || selector.equals("**"));

            // So what's going on with the "*" and "**" resources here?  Basically, we are adding
            // these resources to all targeted elements, accept for "*" and "**" themselves.

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

            reducedSelectorTable.put(selector, visitorBindings);
        }

        if (ParameterAccessor.getParameterValue(ContentDeliveryConfig.SMOOKS_VISITORS_SORT, Boolean.class, true, this)) {
            sort();
        }
        addToExecutionLifecycleSets();

        addIndexCounters(reducedSelectorTable);

        return reducedSelectorTable;
    }

    public void assertSelectorsNotAccessingText() {
        assertSelectorsNotAccessingText(beforeVisitors);
        assertSelectorsNotAccessingText(childVisitors);
    }

    private void assertSelectorsNotAccessingText(SelectorTable selectorTable) {
        Collection<List<ContentHandlerBinding<? extends SaxNgVisitor>>> selectorTableValues = selectorTable.values();

        for (List<ContentHandlerBinding<? extends SaxNgVisitor>> contentHandlerBindings : selectorTableValues) {
            for (ContentHandlerBinding<? extends SaxNgVisitor> contentHandlerBinding : contentHandlerBindings) {
                ResourceConfig resourceConfig = contentHandlerBinding.getResourceConfig();
                SelectorStep selectorStep = resourceConfig.getSelectorPath().getTargetSelectorStep();

                if (selectorStep.accessesText()) {
                    throw new SmooksConfigurationException("Unsupported selector '" + selectorStep.getXPathExpression() + "' on resource '" + resourceConfig + "'.  The 'text()' XPath token is only supported on SAX Visitor implementations that implement the " + AfterVisitor.class.getName() + " interface only.  Class '" + resourceConfig.getResource() + "' implements other SAX Visitor interfaces.");
                }
            }
        }
    }

    public void addIndexCounters(Map<String, SaxNgVisitorBindings> reducedSelectorTable) {
        final Map<String, SaxNgVisitorBindings> reducedSelectorTableCopy = new LinkedHashMap<>(reducedSelectorTable);
        Collection<SaxNgVisitorBindings> elementVisitorMaps = reducedSelectorTableCopy.values();

        for (SaxNgVisitorBindings elementVisitorMap : elementVisitorMaps) {
            addIndexCounters(elementVisitorMap.getBeforeVisitors(), reducedSelectorTable);
            addIndexCounters(elementVisitorMap.getChildVisitors(), reducedSelectorTable);
            addIndexCounters(elementVisitorMap.getAfterVisitors(), reducedSelectorTable);
        }
    }

    private <T extends Visitor> void addIndexCounters(final List<ContentHandlerBinding<T>> contentHandlerBindings, Map<String, SaxNgVisitorBindings> reducedSelectorTable) {
        if (contentHandlerBindings == null) {
            return;
        }

        for (ContentHandlerBinding<? extends Visitor> contentHandlerBinding : contentHandlerBindings) {
            final List<IndexEvaluator> indexEvaluators = new ArrayList<>();

            for (SelectorStep selectorStep : contentHandlerBinding.getResourceConfig().getSelectorPath()) {
                indexEvaluators.clear();
                selectorStep.getEvaluators(IndexEvaluator.class, indexEvaluators);
                for (IndexEvaluator indexEvaluator : indexEvaluators) {
                    if (indexEvaluator.getCounter() == null) {
                        final ElementIndexCounter elementIndexCounter = new ElementIndexCounter(selectorStep);

                        indexEvaluator.setCounter(elementIndexCounter);
                        addIndexCounter(elementIndexCounter, reducedSelectorTable);
                    }
                }
            }
        }
    }

    private void addIndexCounter(ElementIndexCounter indexCounter, Map<String, SaxNgVisitorBindings> reducedSelectorTable) {
        SelectorStep selectorStep = indexCounter.getSelectorStep();
        QName targetElement = selectorStep.getElement();
        String targetElementName = targetElement.getLocalPart();
        SaxNgVisitorBindings visitorBindings = reducedSelectorTable.get(targetElementName);

        if (visitorBindings == null) {
            visitorBindings = new SaxNgVisitorBindings();
            reducedSelectorTable.put(targetElementName, visitorBindings);
        }

        List<ContentHandlerBinding<BeforeVisitor>> vbs = visitorBindings.getBeforeVisitors();

        if (vbs == null) {
            vbs = new ArrayList<>();
            visitorBindings.setBeforeVisitors(vbs);
        }

        ResourceConfig resourceConfig = new ResourceConfig(targetElementName);

        vbs.add(0, new ContentHandlerBinding(indexCounter, resourceConfig));
    }

    public SaxNgVisitorBindings get(String... selectors) {
        SaxNgVisitorBindings combinedVisitorBindings = new SaxNgVisitorBindings();

        combinedVisitorBindings.setBeforeVisitors(new ArrayList<>());
        combinedVisitorBindings.setChildVisitors(new ArrayList<>());
        combinedVisitorBindings.setAfterVisitors(new ArrayList<>());

        for (String selector : selectors) {
            final SaxNgVisitorBindings visitorBindings = get(selector);

            if (visitorBindings != null) {
                final List<ContentHandlerBinding<BeforeVisitor>> beforeVisitorBindings = visitorBindings.getBeforeVisitors();
                final List<ContentHandlerBinding<ChildrenVisitor>> childVisitorBindings = visitorBindings.getChildVisitors();
                final List<ContentHandlerBinding<AfterVisitor>> afterVisitorBindings = visitorBindings.getAfterVisitors();

                if (beforeVisitorBindings != null) {
                    combinedVisitorBindings.getBeforeVisitors().addAll(beforeVisitorBindings);
                }
                if (childVisitorBindings != null) {
                    combinedVisitorBindings.getChildVisitors().addAll(childVisitorBindings);
                }
                if (afterVisitorBindings != null) {
                    combinedVisitorBindings.getAfterVisitors().addAll(afterVisitorBindings);
                }
            }
        }

        if (combinedVisitorBindings.getBeforeVisitors().isEmpty()) {
            combinedVisitorBindings.setBeforeVisitors(null);
        }
        if (combinedVisitorBindings.getChildVisitors().isEmpty()) {
            combinedVisitorBindings.setChildVisitors(null);
        }
        if (combinedVisitorBindings.getAfterVisitors().isEmpty()) {
            combinedVisitorBindings.setAfterVisitors(null);
        }

        if (combinedVisitorBindings.getBeforeVisitors() == null && combinedVisitorBindings.getChildVisitors() == null && combinedVisitorBindings.getAfterVisitors() == null) {
            return null;
        } else {
            return combinedVisitorBindings;
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