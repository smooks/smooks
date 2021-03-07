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
package org.smooks.engine.delivery.sax;

import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.config.xpath.SelectorPath;
import org.smooks.api.resource.config.xpath.SelectorStep;
import org.smooks.api.ExecutionContext;
import org.smooks.api.delivery.ContentDeliveryConfig;
import org.smooks.api.delivery.ContentHandlerBinding;
import org.smooks.api.delivery.Filter;
import org.smooks.api.delivery.FilterBypass;
import org.smooks.api.resource.visitor.sax.SAXVisitAfter;
import org.smooks.api.resource.visitor.sax.SAXVisitBefore;
import org.smooks.api.resource.visitor.sax.SAXVisitChildren;
import org.smooks.api.resource.visitor.sax.SAXVisitor;
import org.smooks.api.SmooksConfigException;
import org.smooks.engine.resource.config.ParameterAccessor;
import org.smooks.engine.resource.config.DefaultResourceConfig;
import org.smooks.engine.resource.config.xpath.evaluators.equality.ElementIndexCounter;
import org.smooks.engine.resource.config.xpath.evaluators.equality.IndexEvaluator;
import org.smooks.engine.delivery.*;
import org.smooks.engine.delivery.ordering.Sorter;
import org.smooks.api.lifecycle.VisitLifecycleCleanable;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.util.*;

/**
 * SAX specific {@link ContentDeliveryConfig} implementation.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@SuppressWarnings({ "WeakerAccess", "unused", "unchecked" })
public class SAXContentDeliveryConfig extends AbstractContentDeliveryConfig {

    private SelectorTable<SAXVisitBefore> visitBeforeSelectorTable = new SelectorTable<>();
    private final SelectorTable<SAXVisitChildren> childVisitorSelectorTable = new SelectorTable<>();
    private SelectorTable<SAXVisitAfter> visitAfterSelectorTable = new SelectorTable<>();
    private SelectorTable<VisitLifecycleCleanable> visitLifecycleCleanableSelectorTable = new SelectorTable<>();
    private boolean rewriteEntities;
    private boolean maintainElementStack;
    private boolean reverseVisitOrderOnVisitAfter;
    private boolean terminateOnVisitorException;
    private FilterBypass filterBypass;

    private final Map<String, SAXElementVisitorMap> optimizedVisitorConfig = new HashMap<>();

    public SelectorTable<SAXVisitBefore> getVisitBeforeSelectorTable() {
        return visitBeforeSelectorTable;
    }

    public void setVisitBeforeSelectorTable(SelectorTable<SAXVisitBefore> visitBeforeSelectorTable) {
        this.visitBeforeSelectorTable = visitBeforeSelectorTable;
    }

    public SelectorTable<SAXVisitChildren> getChildVisitorSelectorTable() {
        return childVisitorSelectorTable;
    }

    public SelectorTable<SAXVisitAfter> getVisitAfterSelectorTable() {
        return visitAfterSelectorTable;
    }

    public void setVisitAfterSelectorTable(SelectorTable<SAXVisitAfter> visitAfterSelectorTable) {
        this.visitAfterSelectorTable = visitAfterSelectorTable;
    }

    public SelectorTable<VisitLifecycleCleanable> getVisitLifecycleCleanableSelectorTable() {
        return visitLifecycleCleanableSelectorTable;
    }

    public void setVisitLifecycleCleanableSelectorTable(SelectorTable<VisitLifecycleCleanable> visitLifecycleCleanableSelectorTable) {
        this.visitLifecycleCleanableSelectorTable = visitLifecycleCleanableSelectorTable;
    }

    public Map<String, SAXElementVisitorMap> getOptimizedVisitorConfig() {
        return optimizedVisitorConfig;
    }

    @Override
    public FilterBypass getFilterBypass() {
    	return filterBypass;
    }

    @Override
    public Filter newFilter(ExecutionContext executionContext) {
        return new SmooksSAXFilter(executionContext);
    }

    @Override
    public void sort() throws SmooksConfigException {
        visitBeforeSelectorTable.sort(Sorter.SortOrder.PRODUCERS_FIRST);
        childVisitorSelectorTable.sort(Sorter.SortOrder.PRODUCERS_FIRST);
        visitAfterSelectorTable.sort(Sorter.SortOrder.CONSUMERS_FIRST);
    }

    @Override
    public void addToExecutionLifecycleSets() throws SmooksConfigException {
        addToExecutionLifecycleSets(visitBeforeSelectorTable);
        addToExecutionLifecycleSets(visitAfterSelectorTable);
    }

    public void optimizeConfig() {
        if(visitBeforeSelectorTable == null || visitAfterSelectorTable == null) {
            throw new IllegalStateException("Illegal call to setChildVisitors() before setVisitBefores() and setVisitAfters() are called.");
        }

        extractChildVisitors();

        List<ContentHandlerBinding<SAXVisitBefore>> starVBs = new ArrayList<>();
        List<ContentHandlerBinding<SAXVisitChildren>> starVCs = new ArrayList<>();
        List<ContentHandlerBinding<SAXVisitAfter>> starVAs = new ArrayList<>();
        List<ContentHandlerBinding<VisitLifecycleCleanable>> starCleanables = new ArrayList<>();

        if(visitBeforeSelectorTable.get("*") != null) {
        	starVBs.addAll(visitBeforeSelectorTable.get("*"));
        }
        if(visitBeforeSelectorTable.get("**") != null) {
        	starVBs.addAll(visitBeforeSelectorTable.get("**"));
        }
        if(childVisitorSelectorTable.get("*") != null) {
        	starVCs.addAll(childVisitorSelectorTable.get("*"));
        }
        if(childVisitorSelectorTable.get("**") != null) {
        	starVCs.addAll(childVisitorSelectorTable.get("**"));
        }
        if(visitAfterSelectorTable.get("*") != null) {
        	starVAs.addAll(visitAfterSelectorTable.get("*"));
        }
        if(visitAfterSelectorTable.get("**") != null) {
        	starVAs.addAll(visitAfterSelectorTable.get("**"));
        }
        if(visitLifecycleCleanableSelectorTable.get("*") != null) {
        	starCleanables.addAll(visitLifecycleCleanableSelectorTable.get("*"));
        }
        if(visitLifecycleCleanableSelectorTable.get("**") != null) {
        	starCleanables.addAll(visitLifecycleCleanableSelectorTable.get("**"));
        }

        // Now extract the before, child and after visitors for all configured elements...
        Set<String> elementNames = new HashSet<>();
        elementNames.addAll(visitBeforeSelectorTable.keySet());
        elementNames.addAll(visitAfterSelectorTable.keySet());

        for (String elementName : elementNames) {
            SAXElementVisitorMap entry = new SAXElementVisitorMap();
            List<ContentHandlerBinding<SAXVisitBefore>> befores = visitBeforeSelectorTable.get(elementName);
            List<ContentHandlerBinding<SAXVisitChildren>> children = childVisitorSelectorTable.get(elementName);
            List<ContentHandlerBinding<SAXVisitAfter>> afters = visitAfterSelectorTable.get(elementName);
            List<ContentHandlerBinding<VisitLifecycleCleanable>> cleanables = visitLifecycleCleanableSelectorTable.get(elementName);
        	boolean isStar = (elementName.equals("*") || elementName.equals("**"));

        	// So what's going on with the "*" and "**" resources here?  Basically, we are adding
        	// these resources to all targeted elements, accept for "*" and "**" themselves.

            if(befores != null && !isStar) {
            	befores.addAll(starVBs);
            }
            entry.setVisitBefores(befores);

            if(children != null && !isStar) {
            	children.addAll(starVCs);
            }
            entry.setChildVisitors(children);

            if(afters != null && !isStar) {
            	afters.addAll(starVAs);
            }
            entry.setVisitAfters(afters);

            if(cleanables != null && !isStar) {
            	cleanables.addAll(starCleanables);
            }
            entry.setVisitCleanables(cleanables);

            entry.initAccumulateText();
            entry.initAcquireWriterFor();

            optimizedVisitorConfig.put(elementName, entry);
        }

        rewriteEntities = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.ENTITIES_REWRITE, String.class, "true", this));
        maintainElementStack = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.MAINTAIN_ELEMENT_STACK, String.class, "true", this));
        reverseVisitOrderOnVisitAfter = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.REVERSE_VISIT_ORDER_ON_VISIT_AFTER, String.class, "true", this));
        terminateOnVisitorException = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.TERMINATE_ON_VISITOR_EXCEPTION, String.class, "true", this));

		filterBypass = getFilterBypass(visitBeforeSelectorTable, visitAfterSelectorTable);
    }

    public void assertSelectorsNotAccessingText() {
        assertSelectorsNotAccessingText(visitBeforeSelectorTable);
        assertSelectorsNotAccessingText(childVisitorSelectorTable);
    }

    private void assertSelectorsNotAccessingText(SelectorTable saxVisitorMap) {
        Collection<List<ContentHandlerBinding<? extends SAXVisitor>>> contentHandlerMaps = saxVisitorMap.values();

        for(List<ContentHandlerBinding<? extends SAXVisitor>> contentHandlerMapList : contentHandlerMaps) {
            for(ContentHandlerBinding<? extends SAXVisitor> contentHandlerMap : contentHandlerMapList) {
                ResourceConfig resourceConfig = contentHandlerMap.getResourceConfig();
                SelectorStep selectorStep = resourceConfig.getSelectorPath().getTargetSelectorStep();

                if(selectorStep.accessesText()) {
                    throw new SmooksConfigException("Unsupported selector '" + selectorStep.getXPathExpression() + "' on resource '" + resourceConfig + "'.  The 'text()' XPath token is only supported on SAX Visitor implementations that implement the " + SAXVisitAfter.class.getName() + " interface only.  Class '" + resourceConfig.getResource() + "' implements other SAX Visitor interfaces.");
                }
            }
        }
    }

    public void addIndexCounters() {
        Map<String, SAXElementVisitorMap> optimizedVisitorConfigCopy = new LinkedHashMap<>(optimizedVisitorConfig);
        Collection<SAXElementVisitorMap> visitorMaps = optimizedVisitorConfigCopy.values();

        for(SAXElementVisitorMap visitorMap : visitorMaps) {
            addIndexCounters(visitorMap.getVisitBefores());
            addIndexCounters(visitorMap.getChildVisitors());
            addIndexCounters(visitorMap.getVisitAfters());
        }
    }

    private <T extends SAXVisitor> void addIndexCounters(List<ContentHandlerBinding<T>> saxVisitorBindings) {
        if(saxVisitorBindings == null) {
            return;
        }

        for (ContentHandlerBinding<? extends SAXVisitor> saxVisitorBinding : saxVisitorBindings) {
            ResourceConfig resourceConfig = saxVisitorBinding.getResourceConfig();
            SelectorPath selectorPath = resourceConfig.getSelectorPath();
            List<IndexEvaluator> indexEvaluators = new ArrayList<>();

            for (SelectorStep selectorStep : selectorPath) {
                indexEvaluators.clear();
                selectorStep.getEvaluators(IndexEvaluator.class, indexEvaluators);
                for (IndexEvaluator indexEvaluator : indexEvaluators) {
                    if (indexEvaluator.getCounter() == null) {
                        ElementIndexCounter indexCounter = new ElementIndexCounter(selectorStep);

                        indexEvaluator.setCounter(indexCounter);
                        addIndexCounter(indexCounter);
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
        SAXElementVisitorMap visitorMap = optimizedVisitorConfig.get(targetElementName);

        if (visitorMap == null) {
            visitorMap = new SAXElementVisitorMap();
            optimizedVisitorConfig.put(targetElementName, visitorMap);
        }

        List<ContentHandlerBinding<SAXVisitBefore>> vbs = visitorMap.getVisitBefores();

        if (vbs == null) {
            vbs = new ArrayList<>();
            visitorMap.setVisitBefores(vbs);
        }

        ResourceConfig resourceConfig = new DefaultResourceConfig(targetElementName);

        if (!XMLConstants.NULL_NS_URI.equals(targetNS)) {
            resourceConfig.getSelectorPath().setSelectorNamespaceURI(targetNS);
        }

        vbs.add(0, new DefaultContentHandlerBinding<>(indexCounter, resourceConfig));
    }

    public SAXElementVisitorMap getCombinedOptimizedConfig(String[] elementNames) {
        SAXElementVisitorMap combinedConfig = new SAXElementVisitorMap();

        combinedConfig.setVisitBefores(new ArrayList<>());
        combinedConfig.setChildVisitors(new ArrayList<>());
        combinedConfig.setVisitAfters(new ArrayList<>());
        combinedConfig.setVisitCleanables(new ArrayList<>());

        for(String elementName : elementNames) {
            SAXElementVisitorMap elementConfig = optimizedVisitorConfig.get(elementName);

            if(elementConfig != null) {
                List<ContentHandlerBinding<SAXVisitBefore>> elementVisitBefores = elementConfig.getVisitBefores();
                List<ContentHandlerBinding<SAXVisitChildren>> elementChildVisitors = elementConfig.getChildVisitors();
                List<ContentHandlerBinding<SAXVisitAfter>> elementVisitAfteres = elementConfig.getVisitAfters();
                List<ContentHandlerBinding<VisitLifecycleCleanable>> elementVisitCleanables = elementConfig.getVisitCleanables();

                if(elementVisitBefores != null) {
                    combinedConfig.getVisitBefores().addAll(elementVisitBefores);
                }
                if(elementChildVisitors != null) {
                    combinedConfig.getChildVisitors().addAll(elementChildVisitors);
                }
                if(elementVisitAfteres != null) {
                    combinedConfig.getVisitAfters().addAll(elementVisitAfteres);
                }
                if(elementVisitCleanables != null) {
                    combinedConfig.getVisitCleanables().addAll(elementVisitCleanables);
                }

                combinedConfig.initAccumulateText(elementConfig);
                combinedConfig.initAcquireWriterFor(elementConfig);
            }
        }

        if(combinedConfig.getVisitBefores().isEmpty()) {
            combinedConfig.setVisitBefores(null);
        }
        if(combinedConfig.getChildVisitors().isEmpty()) {
            combinedConfig.setChildVisitors(null);
        }
        if(combinedConfig.getVisitAfters().isEmpty()) {
            combinedConfig.setVisitAfters(null);
        }
        if(combinedConfig.getVisitCleanables().isEmpty()) {
            combinedConfig.setVisitCleanables(null);
        }

        if(combinedConfig.getVisitBefores() == null && combinedConfig.getChildVisitors() == null && combinedConfig.getVisitAfters() == null ) {
            return null;
        } else {
            return combinedConfig;
        }
    }

    private void extractChildVisitors() {
        // Need to extract the child visitor impls from the visitBefores and the visitAfters.  Need to make sure that we don't add
        // the same handler twice - handlers can impl both SAXVisitBefore and SAXVisitAfter. So, we don't add child handlers from the
        // visitBefores if they also impl SAXVisitAfter (avoiding adding where it impls both).  We add from the visitafters list
        // if it impls SAXVisitAfter without checking for SAXVisitBefore (catching the case where it impls both).

        Set<Map.Entry<String, List<ContentHandlerBinding<SAXVisitBefore>>>> beforeSelectorTables = visitBeforeSelectorTable.entrySet();
        for (Map.Entry<String, List<ContentHandlerBinding<SAXVisitBefore>>> beforeSelectorTable : beforeSelectorTables) {
            List<ContentHandlerBinding<SAXVisitBefore>> contentHandlerBindings = beforeSelectorTable.getValue();
            for (ContentHandlerBinding<SAXVisitBefore> contentHandlerBinding : contentHandlerBindings) {
                String selector = beforeSelectorTable.getKey();
                SAXVisitBefore saxVisitBefore = contentHandlerBinding.getContentHandler();

                // Wanna make sure we don't add the same handler twice, so if it also impls SAXVisitAfter, leave
                // that until we process the SAXVisitAfter handlers...
                if(saxVisitBefore instanceof SAXVisitChildren && !(saxVisitBefore instanceof SAXVisitAfter)) {
                    childVisitorSelectorTable.put(selector, contentHandlerBinding.getResourceConfig(), (SAXVisitChildren) saxVisitBefore);
                }
            }
        }

        Set<Map.Entry<String, List<ContentHandlerBinding<SAXVisitAfter>>>> afterMappings = visitAfterSelectorTable.entrySet();
        for (Map.Entry<String,List<ContentHandlerBinding<SAXVisitAfter>>> afterMapping : afterMappings) {
            List<ContentHandlerBinding<SAXVisitAfter>> elementMappings = afterMapping.getValue();
            for (ContentHandlerBinding<SAXVisitAfter> elementMapping : elementMappings) {
                String elementName = afterMapping.getKey();
                SAXVisitAfter handler = elementMapping.getContentHandler();

                if(handler instanceof SAXVisitChildren) {
                    childVisitorSelectorTable.put(elementName, elementMapping.getResourceConfig(), (SAXVisitChildren) handler);
                }
            }
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
}
