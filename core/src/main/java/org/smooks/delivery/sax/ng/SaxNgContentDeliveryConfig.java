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
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.cdr.ResourceConfig;
import org.smooks.cdr.xpath.SelectorStep;
import org.smooks.cdr.xpath.evaluators.equality.ElementIndexCounter;
import org.smooks.cdr.xpath.evaluators.equality.IndexEvaluator;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.*;
import org.smooks.delivery.ordering.Sorter;
import org.smooks.delivery.sax.SAXVisitor;

import javax.xml.namespace.QName;
import java.util.*;

public class SaxNgContentDeliveryConfig extends AbstractContentDeliveryConfig {

    private final Map<String, SaxNgVisitorBindings> optimizedVisitorConfig = new HashMap<>();
    private final ContentHandlerBindings<ChildrenVisitor> childVisitors = new ContentHandlerBindings<>();
    private final ContentHandlerBindings<BeforeVisitor> beforeVisitors = new ContentHandlerBindings<>();
    private final ContentHandlerBindings<AfterVisitor> afterVisitors = new ContentHandlerBindings<>();
    private int maxNodeDepth;
    private boolean rewriteEntities;
    private boolean maintainElementStack;
    private boolean reverseVisitOrderOnVisitAfter;
    private boolean terminateOnVisitorException;
    private FilterBypass filterBypass;

    public ContentHandlerBindings<BeforeVisitor> getBeforeVisitors() {
        return beforeVisitors;
    }

    public ContentHandlerBindings<ChildrenVisitor> getChildVisitors() {
        return childVisitors;
    }

    public ContentHandlerBindings<AfterVisitor> getAfterVisitors() {
        return afterVisitors;
    }

    public Map<String, SaxNgVisitorBindings> getOptimizedVisitorConfig() {
        return optimizedVisitorConfig;
    }

    @Override
    public FilterBypass getFilterBypass() {
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

    public void optimizeConfig() {
        final List<ContentHandlerBinding<BeforeVisitor>> starVBs = new ArrayList<>();
        final List<ContentHandlerBinding<ChildrenVisitor>> starVCs = new ArrayList<>();
        final List<ContentHandlerBinding<AfterVisitor>> starVAs = new ArrayList<>();

        if (beforeVisitors.getTable().get("*") != null) {
            starVBs.addAll(beforeVisitors.getTable().get("*"));
        }
        if (beforeVisitors.getTable().get("**") != null) {
            starVBs.addAll(beforeVisitors.getTable().get("**"));
        }
        if (childVisitors.getTable().get("*") != null) {
            starVCs.addAll(childVisitors.getTable().get("*"));
        }
        if (childVisitors.getTable().get("**") != null) {
            starVCs.addAll(childVisitors.getTable().get("**"));
        }
        if (afterVisitors.getTable().get("*") != null) {
            starVAs.addAll(afterVisitors.getTable().get("*"));
        }
        if (afterVisitors.getTable().get("**") != null) {
            starVAs.addAll(afterVisitors.getTable().get("**"));
        }

        // Now extract the before, child and after visitors for all configured elements...
        Set<String> elementNames = new HashSet<String>();
        elementNames.addAll(beforeVisitors.getTable().keySet());
        elementNames.addAll(afterVisitors.getTable().keySet());

        for (String elementName : elementNames) {
            final SaxNgVisitorBindings elementVisitorMap = new SaxNgVisitorBindings();
            final List<ContentHandlerBinding<BeforeVisitor>> beforeVisitors = this.beforeVisitors.getTable().getOrDefault(elementName, new ArrayList<>());
            final List<ContentHandlerBinding<ChildrenVisitor>> childVisitors = this.childVisitors.getTable().getOrDefault(elementName, new ArrayList<>());
            final List<ContentHandlerBinding<AfterVisitor>> afterVisitors = this.afterVisitors.getTable().getOrDefault(elementName, new ArrayList<>());
            final boolean isStar = (elementName.equals("*") || elementName.equals("**"));

            // So what's going on with the "*" and "**" resources here?  Basically, we are adding
            // these resources to all targeted elements, accept for "*" and "**" themselves.

            if (!isStar) {
                beforeVisitors.addAll(starVBs);
            }
            elementVisitorMap.setBeforeVisitors(beforeVisitors);

            if (!isStar) {
                childVisitors.addAll(starVCs);
            }
            elementVisitorMap.setChildVisitors(childVisitors);

            if (!isStar) {
                afterVisitors.addAll(starVAs);
            }
            elementVisitorMap.setAfterVisitors(afterVisitors);

            optimizedVisitorConfig.put(elementName, elementVisitorMap);
        }

        rewriteEntities = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.ENTITIES_REWRITE, String.class, "true", this));
        maintainElementStack = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.MAINTAIN_ELEMENT_STACK, String.class, "true", this));
        reverseVisitOrderOnVisitAfter = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.REVERSE_VISIT_ORDER_ON_VISIT_AFTER, String.class, "true", this));
        terminateOnVisitorException = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.TERMINATE_ON_VISITOR_EXCEPTION, String.class, "true", this));
        maxNodeDepth = Integer.parseInt(ParameterAccessor.getParameterValue(Filter.MAX_NODE_DEPTH, String.class, "1", this));

        filterBypass = getFilterBypass(beforeVisitors, afterVisitors);
    }

    public void assertSelectorsNotAccessingText() {
        assertSelectorsNotAccessingText(beforeVisitors);
        assertSelectorsNotAccessingText(childVisitors);
    }

    private void assertSelectorsNotAccessingText(ContentHandlerBindings contentHandlerBindings) {
        Map<String, List<ContentHandlerBinding<? extends SAXVisitor>>> table = contentHandlerBindings.getTable();
        Collection<List<ContentHandlerBinding<? extends SAXVisitor>>> contentHandlerMaps = table.values();

        for (List<ContentHandlerBinding<? extends SAXVisitor>> contentHandlerMapList : contentHandlerMaps) {
            for (ContentHandlerBinding<? extends SAXVisitor> contentHandlerMap : contentHandlerMapList) {
                ResourceConfig resourceConfig = contentHandlerMap.getResourceConfig();
                SelectorStep selectorStep = resourceConfig.getSelectorPath().getTargetSelectorStep();

                if (selectorStep.accessesText()) {
                    throw new SmooksConfigurationException("Unsupported selector '" + selectorStep.getXPathExpression() + "' on resource '" + resourceConfig + "'.  The 'text()' XPath token is only supported on SAX Visitor implementations that implement the " + AfterVisitor.class.getName() + " interface only.  Class '" + resourceConfig.getResource() + "' implements other SAX Visitor interfaces.");
                }
            }
        }
    }

    public void addIndexCounters() {
        final Map<String, SaxNgVisitorBindings> optimizedVisitorConfigCopy = new LinkedHashMap<>(optimizedVisitorConfig);
        Collection<SaxNgVisitorBindings> elementVisitorMaps = optimizedVisitorConfigCopy.values();

        for (SaxNgVisitorBindings elementVisitorMap : elementVisitorMaps) {
            addIndexCounters(elementVisitorMap.getBeforeVisitors());
            addIndexCounters(elementVisitorMap.getChildVisitors());
            addIndexCounters(elementVisitorMap.getAfterVisitors());
        }
    }

    private <T extends Visitor> void addIndexCounters(final List<ContentHandlerBinding<T>> contentHandlerBindings) {
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
                        addIndexCounter(elementIndexCounter);
                    }
                }
            }
        }
    }

    private void addIndexCounter(ElementIndexCounter indexCounter) {
        SelectorStep selectorStep = indexCounter.getSelectorStep();
        QName targetElement = selectorStep.getElement();
        String targetElementName = targetElement.getLocalPart();
        String targetNS = targetElement.getNamespaceURI();
        SaxNgVisitorBindings visitorMap = optimizedVisitorConfig.get(targetElementName);

        if (visitorMap == null) {
            visitorMap = new SaxNgVisitorBindings();
            optimizedVisitorConfig.put(targetElementName, visitorMap);
        }

        List<ContentHandlerBinding<BeforeVisitor>> vbs = visitorMap.getBeforeVisitors();

        if (vbs == null) {
            vbs = new ArrayList<>();
            visitorMap.setBeforeVisitors(vbs);
        }

        ResourceConfig resourceConfig = new ResourceConfig(targetElementName);

        vbs.add(0, new ContentHandlerBinding(indexCounter, resourceConfig));
    }

    public SaxNgVisitorBindings getCombinedOptimizedConfig(String[] elementNames) {
        SaxNgVisitorBindings combinedConfig = new SaxNgVisitorBindings();

        combinedConfig.setBeforeVisitors(new ArrayList<>());
        combinedConfig.setChildVisitors(new ArrayList<>());
        combinedConfig.setAfterVisitors(new ArrayList<>());

        for (String elementName : elementNames) {
            final SaxNgVisitorBindings elementVisitorMap = optimizedVisitorConfig.get(elementName);

            if (elementVisitorMap != null) {
                final List<ContentHandlerBinding<BeforeVisitor>> beforeVisitorBindings = elementVisitorMap.getBeforeVisitors();
                final List<ContentHandlerBinding<ChildrenVisitor>> childVisitorBindings = elementVisitorMap.getChildVisitors();
                final List<ContentHandlerBinding<AfterVisitor>> afterVisitorBindings = elementVisitorMap.getAfterVisitors();

                if (beforeVisitorBindings != null) {
                    combinedConfig.getBeforeVisitors().addAll(beforeVisitorBindings);
                }
                if (childVisitorBindings != null) {
                    combinedConfig.getChildVisitors().addAll(childVisitorBindings);
                }
                if (afterVisitorBindings != null) {
                    combinedConfig.getAfterVisitors().addAll(afterVisitorBindings);
                }
            }
        }

        if (combinedConfig.getBeforeVisitors().isEmpty()) {
            combinedConfig.setBeforeVisitors(null);
        }
        if (combinedConfig.getChildVisitors().isEmpty()) {
            combinedConfig.setChildVisitors(null);
        }
        if (combinedConfig.getAfterVisitors().isEmpty()) {
            combinedConfig.setAfterVisitors(null);
        }

        if (combinedConfig.getBeforeVisitors() == null && combinedConfig.getChildVisitors() == null && combinedConfig.getAfterVisitors() == null) {
            return null;
        } else {
            return combinedConfig;
        }
    }

    public boolean isRewriteEntities() {
        return rewriteEntities;
    }

    public boolean isMaintainElementStack() {
        return maintainElementStack;
    }

    public boolean isReverseVisitOrderOnVisitAfter() {
        return reverseVisitOrderOnVisitAfter;
    }

    public boolean isTerminateOnVisitorException() {
        return terminateOnVisitorException;
    }

    public int getMaxNodeDepth() {
        return maxNodeDepth;
    }
}