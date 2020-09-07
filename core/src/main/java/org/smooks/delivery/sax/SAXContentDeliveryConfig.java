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
package org.smooks.delivery.sax;

import org.smooks.cdr.ParameterAccessor;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.cdr.xpath.SelectorStep;
import org.smooks.cdr.xpath.evaluators.equality.ElementIndexCounter;
import org.smooks.cdr.xpath.evaluators.equality.IndexEvaluator;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.*;
import org.smooks.delivery.ordering.Sorter;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.util.*;

/**
 * SAX specific {@link org.smooks.delivery.ContentDeliveryConfig} implementation.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@SuppressWarnings({ "WeakerAccess", "unused", "unchecked" })
public class SAXContentDeliveryConfig extends AbstractContentDeliveryConfig {

    private ContentHandlerBindings<SAXVisitBefore> visitBefores = new ContentHandlerBindings<>();
    private final ContentHandlerBindings<SAXVisitChildren> childVisitors = new ContentHandlerBindings<>();
    private ContentHandlerBindings<SAXVisitAfter> visitAfters = new ContentHandlerBindings<>();
    private ContentHandlerBindings<VisitLifecycleCleanable> visitCleanables = new ContentHandlerBindings<>();
    private boolean rewriteEntities;
    private boolean maintainElementStack;
    private boolean reverseVisitOrderOnVisitAfter;
    private boolean terminateOnVisitorException;
    private FilterBypass filterBypass;

    private final Map<String, SAXElementVisitorMap> optimizedVisitorConfig = new HashMap<String, SAXElementVisitorMap>();

    public ContentHandlerBindings<SAXVisitBefore> getVisitBefores() {
        return visitBefores;
    }

    public void setVisitBefores(ContentHandlerBindings<SAXVisitBefore> visitBefores) {
        this.visitBefores = visitBefores;
    }

    public ContentHandlerBindings<SAXVisitChildren> getChildVisitors() {
        return childVisitors;
    }

    public ContentHandlerBindings<SAXVisitAfter> getVisitAfters() {
        return visitAfters;
    }

    public void setVisitAfters(ContentHandlerBindings<SAXVisitAfter> visitAfters) {
        this.visitAfters = visitAfters;
    }

    public ContentHandlerBindings<VisitLifecycleCleanable> getVisitCleanables() {
        return visitCleanables;
    }

    public void setVisitCleanables(ContentHandlerBindings<VisitLifecycleCleanable> visitCleanables) {
        this.visitCleanables = visitCleanables;
    }

    public Map<String, SAXElementVisitorMap> getOptimizedVisitorConfig() {
        return optimizedVisitorConfig;
    }

    public FilterBypass getFilterBypass() {
    	return filterBypass;
    }

    public Filter newFilter(ExecutionContext executionContext) {
        return new SmooksSAXFilter(executionContext);
    }

    public void sort() throws SmooksConfigurationException {
        visitBefores.sort(Sorter.SortOrder.PRODUCERS_FIRST);
        childVisitors.sort(Sorter.SortOrder.PRODUCERS_FIRST);
        visitAfters.sort(Sorter.SortOrder.CONSUMERS_FIRST);
    }

    public void addToExecutionLifecycleSets() throws SmooksConfigurationException {
        addToExecutionLifecycleSets(visitBefores);
        addToExecutionLifecycleSets(visitAfters);
    }

    public void optimizeConfig() {
        if(visitBefores == null || visitAfters == null) {
            throw new IllegalStateException("Illegal call to setChildVisitors() before setVisitBefores() and setVisitAfters() are called.");
        }

        extractChildVisitors();

        List<ContentHandlerBinding<SAXVisitBefore>> starVBs = new ArrayList<>();
        List<ContentHandlerBinding<SAXVisitChildren>> starVCs = new ArrayList<>();
        List<ContentHandlerBinding<SAXVisitAfter>> starVAs = new ArrayList<>();
        List<ContentHandlerBinding<VisitLifecycleCleanable>> starCleanables = new ArrayList<>();

        if(visitBefores.getTable().get("*") != null) {
        	starVBs.addAll(visitBefores.getTable().get("*"));
        }
        if(visitBefores.getTable().get("**") != null) {
        	starVBs.addAll(visitBefores.getTable().get("**"));
        }
        if(childVisitors.getTable().get("*") != null) {
        	starVCs.addAll(childVisitors.getTable().get("*"));
        }
        if(childVisitors.getTable().get("**") != null) {
        	starVCs.addAll(childVisitors.getTable().get("**"));
        }
        if(visitAfters.getTable().get("*") != null) {
        	starVAs.addAll(visitAfters.getTable().get("*"));
        }
        if(visitAfters.getTable().get("**") != null) {
        	starVAs.addAll(visitAfters.getTable().get("**"));
        }
        if(visitCleanables.getTable().get("*") != null) {
        	starCleanables.addAll(visitCleanables.getTable().get("*"));
        }
        if(visitCleanables.getTable().get("**") != null) {
        	starCleanables.addAll(visitCleanables.getTable().get("**"));
        }

        // Now extract the before, child and after visitors for all configured elements...
        Set<String> elementNames = new HashSet<String>();
        elementNames.addAll(visitBefores.getTable().keySet());
        elementNames.addAll(visitAfters.getTable().keySet());

        for (String elementName : elementNames) {
            SAXElementVisitorMap entry = new SAXElementVisitorMap();
            List<ContentHandlerBinding<SAXVisitBefore>> befores = visitBefores.getTable().get(elementName);
            List<ContentHandlerBinding<SAXVisitChildren>> children = childVisitors.getTable().get(elementName);
            List<ContentHandlerBinding<SAXVisitAfter>> afters = visitAfters.getTable().get(elementName);
            List<ContentHandlerBinding<VisitLifecycleCleanable>> cleanables = visitCleanables.getTable().get(elementName);
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

		filterBypass = getFilterBypass(visitBefores, visitAfters);
    }

    public void assertSelectorsNotAccessingText() {
        assertSelectorsNotAccessingText(visitBefores);
        assertSelectorsNotAccessingText(childVisitors);
    }

    private void assertSelectorsNotAccessingText(ContentHandlerBindings saxVisitorMap) {
        Map<String, List<ContentHandlerBinding<? extends SAXVisitor>>> table = saxVisitorMap.getTable();
        Collection<List<ContentHandlerBinding<? extends SAXVisitor>>> contentHandlerMaps = table.values();

        for(List<ContentHandlerBinding<? extends SAXVisitor>> contentHandlerMapList : contentHandlerMaps) {
            for(ContentHandlerBinding<? extends SAXVisitor> contentHandlerMap : contentHandlerMapList) {
                SmooksResourceConfiguration resourceConfig = contentHandlerMap.getSmooksResourceConfiguration();
                SelectorStep selectorStep = resourceConfig.getSelectorStep();

                if(selectorStep.accessesText()) {
                    throw new SmooksConfigurationException("Unsupported selector '" + selectorStep.getXPathExpression() + "' on resource '" + resourceConfig + "'.  The 'text()' XPath token is only supported on SAX Visitor implementations that implement the " + SAXVisitAfter.class.getName() + " interface only.  Class '" + resourceConfig.getResource() + "' implements other SAX Visitor interfaces.");
                }
            }
        }
    }

    public void addIndexCounters() {
        Map<String, SAXElementVisitorMap> optimizedVisitorConfigCopy = new LinkedHashMap<String, SAXElementVisitorMap>(optimizedVisitorConfig);
        Collection<SAXElementVisitorMap> visitorMaps = optimizedVisitorConfigCopy.values();

        for(SAXElementVisitorMap visitorMap : visitorMaps) {
            addIndexCounters(visitorMap.getVisitBefores());
            addIndexCounters(visitorMap.getChildVisitors());
            addIndexCounters(visitorMap.getVisitAfters());
        }
    }

    private <T extends SAXVisitor> void addIndexCounters(List<ContentHandlerBinding<T>> saxVisitorMap) {
        if(saxVisitorMap == null) {
            return;
        }

        for(ContentHandlerBinding<? extends SAXVisitor> contentHandlerMap : saxVisitorMap) {
            SmooksResourceConfiguration resourceConfig = contentHandlerMap.getSmooksResourceConfiguration();
            SelectorStep[] selectorSteps = resourceConfig.getSelectorSteps();
            List<IndexEvaluator> indexEvaluators = new ArrayList<IndexEvaluator>();

            for(SelectorStep selectorStep : selectorSteps) {
                indexEvaluators.clear();
                selectorStep.getEvaluators(IndexEvaluator.class, indexEvaluators);
                for(IndexEvaluator indexEvaluator : indexEvaluators) {
                    if(indexEvaluator.getCounter() == null) {
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
        QName targetElement = selectorStep.getTargetElement();
        String targetElementName = targetElement.getLocalPart();
        String targetNS = targetElement.getNamespaceURI();
        SAXElementVisitorMap visitorMap = optimizedVisitorConfig.get(targetElementName);

        if(visitorMap == null) {
            visitorMap = new SAXElementVisitorMap();
            optimizedVisitorConfig.put(targetElementName, visitorMap);
        }

        List<ContentHandlerBinding<SAXVisitBefore>> vbs = visitorMap.getVisitBefores();

        if(vbs == null) {
            vbs = new ArrayList<>();
            visitorMap.setVisitBefores(vbs);
        }

        SmooksResourceConfiguration resourceConfig = new SmooksResourceConfiguration(targetElementName);

        if(!XMLConstants.NULL_NS_URI.equals(targetNS)) {
            resourceConfig.setSelectorNamespaceURI(targetNS);
        }

        vbs.add(0, new ContentHandlerBinding(indexCounter, resourceConfig));
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

        Set<Map.Entry<String, List<ContentHandlerBinding<SAXVisitBefore>>>> beforeMappings = visitBefores.getTable().entrySet();
        for (Map.Entry<String, List<ContentHandlerBinding<SAXVisitBefore>>> beforeMapping : beforeMappings) {
            List<ContentHandlerBinding<SAXVisitBefore>> elementMappings = beforeMapping.getValue();
            for (ContentHandlerBinding<SAXVisitBefore> elementMapping : elementMappings) {
                String elementName = beforeMapping.getKey();
                SAXVisitBefore handler = elementMapping.getContentHandler();

                // Wanna make sure we don't add the same handler twice, so if it also impls SAXVisitAfter, leave
                // that until we process the SAXVisitAfter handlers...
                if(handler instanceof SAXVisitChildren && !(handler instanceof SAXVisitAfter)) {
                    childVisitors.addBinding(elementName, elementMapping.getSmooksResourceConfiguration(), (SAXVisitChildren) handler);
                }
            }
        }

        Set<Map.Entry<String, List<ContentHandlerBinding<SAXVisitAfter>>>> afterMappings = visitAfters.getTable().entrySet();
        for (Map.Entry<String,List<ContentHandlerBinding<SAXVisitAfter>>> afterMapping : afterMappings) {
            List<ContentHandlerBinding<SAXVisitAfter>> elementMappings = afterMapping.getValue();
            for (ContentHandlerBinding<SAXVisitAfter> elementMapping : elementMappings) {
                String elementName = afterMapping.getKey();
                SAXVisitAfter handler = elementMapping.getContentHandler();

                if(handler instanceof SAXVisitChildren) {
                    childVisitors.addBinding(elementName, elementMapping.getSmooksResourceConfiguration(), (SAXVisitChildren) handler);
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
