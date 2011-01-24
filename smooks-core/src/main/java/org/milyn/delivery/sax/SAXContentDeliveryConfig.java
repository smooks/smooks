/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package org.milyn.delivery.sax;

import org.milyn.container.ExecutionContext;
import org.milyn.delivery.*;
import org.milyn.delivery.ordering.Sorter;
import org.milyn.cdr.ParameterAccessor;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.xpath.SelectorStep;
import org.milyn.cdr.xpath.evaluators.equality.IndexEvaluator;
import org.milyn.cdr.xpath.evaluators.equality.ElementIndexCounter;

import javax.xml.namespace.QName;
import javax.xml.XMLConstants;
import java.util.*;

/**
 * SAX specific {@link org.milyn.delivery.ContentDeliveryConfig} implementation.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SAXContentDeliveryConfig extends AbstractContentDeliveryConfig {
    
    private ContentHandlerConfigMapTable<SAXVisitBefore> visitBefores;
    private ContentHandlerConfigMapTable<SAXVisitChildren> childVisitors = new ContentHandlerConfigMapTable<SAXVisitChildren>();
    private ContentHandlerConfigMapTable<SAXVisitAfter> visitAfters;
    private ContentHandlerConfigMapTable<VisitLifecycleCleanable> visitCleanables;
    private boolean rewriteEntities;
    private boolean maintainElementStack;
    private boolean reverseVisitOrderOnVisitAfter;
    private boolean terminateOnVisitorException;
    private FilterBypass filterBypass;

    private Map<String, SAXElementVisitorMap> optimizedVisitorConfig = new HashMap<String, SAXElementVisitorMap>();

    public ContentHandlerConfigMapTable<SAXVisitBefore> getVisitBefores() {
        return visitBefores;
    }

    public void setVisitBefores(ContentHandlerConfigMapTable<SAXVisitBefore> visitBefores) {
        this.visitBefores = visitBefores;
    }

    public ContentHandlerConfigMapTable<SAXVisitChildren> getChildVisitors() {
        return childVisitors;
    }

    public ContentHandlerConfigMapTable<SAXVisitAfter> getVisitAfters() {
        return visitAfters;
    }

    public void setVisitAfters(ContentHandlerConfigMapTable<SAXVisitAfter> visitAfters) {
        this.visitAfters = visitAfters;
    }

    public ContentHandlerConfigMapTable<VisitLifecycleCleanable> getVisitCleanables() {
        return visitCleanables;
    }

    public void setVisitCleanables(ContentHandlerConfigMapTable<VisitLifecycleCleanable> visitCleanables) {
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
        
        List<ContentHandlerConfigMap<SAXVisitBefore>> starVBs = new ArrayList<ContentHandlerConfigMap<SAXVisitBefore>>();
        List<ContentHandlerConfigMap<SAXVisitChildren>> starVCs = new ArrayList<ContentHandlerConfigMap<SAXVisitChildren>>();
        List<ContentHandlerConfigMap<SAXVisitAfter>> starVAs = new ArrayList<ContentHandlerConfigMap<SAXVisitAfter>>();
        List<ContentHandlerConfigMap<VisitLifecycleCleanable>> starCleanables = new ArrayList<ContentHandlerConfigMap<VisitLifecycleCleanable>>();
        
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
            List<ContentHandlerConfigMap<SAXVisitBefore>> befores = visitBefores.getTable().get(elementName);
            List<ContentHandlerConfigMap<SAXVisitChildren>> children = childVisitors.getTable().get(elementName);
            List<ContentHandlerConfigMap<SAXVisitAfter>> afters = visitAfters.getTable().get(elementName);
            List<ContentHandlerConfigMap<VisitLifecycleCleanable>> cleanables = visitCleanables.getTable().get(elementName);
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

        rewriteEntities = ParameterAccessor.getBoolParameter(Filter.ENTITIES_REWRITE, true, this);
        maintainElementStack = ParameterAccessor.getBoolParameter(Filter.MAINTAIN_ELEMENT_STACK, true, this);
        reverseVisitOrderOnVisitAfter = ParameterAccessor.getBoolParameter(Filter.REVERSE_VISIT_ORDER_ON_VISIT_AFTER, true, this);
        terminateOnVisitorException = ParameterAccessor.getBoolParameter(Filter.TERMINATE_ON_VISITOR_EXCEPTION, true, this);
        
		filterBypass = getFilterBypass(visitBefores, visitAfters);
    }

    public void assertSelectorsNotAccessingText() {
        assertSelectorsNotAccessingText(visitBefores);
        assertSelectorsNotAccessingText(childVisitors);
    }

    private void assertSelectorsNotAccessingText(ContentHandlerConfigMapTable saxVisitorMap) {
        Map<String, List<ContentHandlerConfigMap<? extends SAXVisitor>>> table = saxVisitorMap.getTable();
        Collection<List<ContentHandlerConfigMap<? extends SAXVisitor>>> contentHandlerMaps = table.values();

        for(List<ContentHandlerConfigMap<? extends SAXVisitor>> contentHandlerMapList : contentHandlerMaps) {
            for(ContentHandlerConfigMap<? extends SAXVisitor> contentHandlerMap : contentHandlerMapList) {
                SmooksResourceConfiguration resourceConfig = contentHandlerMap.getResourceConfig();
                SelectorStep selectorStep = resourceConfig.getSelectorStep();

                if(selectorStep.accessesText()) {
                    throw new SmooksConfigurationException("Unsupported selector '" + selectorStep.getXPathExpression() + "' on resource '" + resourceConfig + "'.  The 'text()' XPath token is only supported on SAX Visitor implementations that implement the " + SAXVisitAfter.class.getName() + " interface only.  Class '" + resourceConfig.getResource() + "' implements other SAX Visitor interfaces.");
                }
            }
        }
    }

    public void addIndexCounters() {
        Map<String, SAXElementVisitorMap> optimizedVisitorConfigCopy = new LinkedHashMap(optimizedVisitorConfig);
        Collection<SAXElementVisitorMap> visitorMaps = optimizedVisitorConfigCopy.values();

        for(SAXElementVisitorMap visitorMap : visitorMaps) {
            addIndexCounters(visitorMap.getVisitBefores());
            addIndexCounters(visitorMap.getChildVisitors());
            addIndexCounters(visitorMap.getVisitAfters());
        }
    }

    private <T extends SAXVisitor> void addIndexCounters(List<ContentHandlerConfigMap<T>> saxVisitorMap) {
        if(saxVisitorMap == null) {
            return;
        }

        for(ContentHandlerConfigMap<? extends SAXVisitor> contentHandlerMap : saxVisitorMap) {
            SmooksResourceConfiguration resourceConfig = contentHandlerMap.getResourceConfig();
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

        List<ContentHandlerConfigMap<SAXVisitBefore>> vbs = visitorMap.getVisitBefores();

        if(vbs == null) {
            vbs = new ArrayList<ContentHandlerConfigMap<SAXVisitBefore>>();
            visitorMap.setVisitBefores(vbs);
        }

        SmooksResourceConfiguration resourceConfig = new SmooksResourceConfiguration(targetElementName);

        if(targetNS != null && targetNS != XMLConstants.NULL_NS_URI) {
            resourceConfig.setSelectorNamespaceURI(targetNS);
        }

        vbs.add(0, new ContentHandlerConfigMap(indexCounter, resourceConfig));
    }

    public SAXElementVisitorMap getCombinedOptimizedConfig(String[] elementNames) {
        SAXElementVisitorMap combinedConfig = new SAXElementVisitorMap();

        combinedConfig.setVisitBefores(new ArrayList<ContentHandlerConfigMap<SAXVisitBefore>>());
        combinedConfig.setChildVisitors(new ArrayList<ContentHandlerConfigMap<SAXVisitChildren>>());
        combinedConfig.setVisitAfters(new ArrayList<ContentHandlerConfigMap<SAXVisitAfter>>());
        combinedConfig.setVisitCleanables(new ArrayList<ContentHandlerConfigMap<VisitLifecycleCleanable>>());

        for(String elementName : elementNames) {
            SAXElementVisitorMap elementConfig = optimizedVisitorConfig.get(elementName);

            if(elementConfig != null) {
                List<ContentHandlerConfigMap<SAXVisitBefore>> elementVisitBefores = elementConfig.getVisitBefores();
                List<ContentHandlerConfigMap<SAXVisitChildren>> elementChildVisitors = elementConfig.getChildVisitors();
                List<ContentHandlerConfigMap<SAXVisitAfter>> elementVisitAfteres = elementConfig.getVisitAfters();
                List<ContentHandlerConfigMap<VisitLifecycleCleanable>> elementVisitCleanables = elementConfig.getVisitCleanables();

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

        Set<Map.Entry<String, List<ContentHandlerConfigMap<SAXVisitBefore>>>> beforeMappings = visitBefores.getTable().entrySet();
        for (Map.Entry<String, List<ContentHandlerConfigMap<SAXVisitBefore>>> beforeMapping : beforeMappings) {
            List<ContentHandlerConfigMap<SAXVisitBefore>> elementMappings = beforeMapping.getValue();
            for (ContentHandlerConfigMap<SAXVisitBefore> elementMapping : elementMappings) {
                String elementName = beforeMapping.getKey();
                SAXVisitBefore handler = elementMapping.getContentHandler();

                // Wanna make sure we don't add the same handler twice, so if it also impls SAXVisitAfter, leave
                // that until we process the SAXVisitAfter handlers...
                if(handler instanceof SAXVisitChildren && !(handler instanceof SAXVisitAfter)) {
                    childVisitors.addMapping(elementName, elementMapping.getResourceConfig(), (SAXVisitChildren) handler);
                }
            }
        }

        Set<Map.Entry<String, List<ContentHandlerConfigMap<SAXVisitAfter>>>> afterMappings = visitAfters.getTable().entrySet();
        for (Map.Entry<String,List<ContentHandlerConfigMap<SAXVisitAfter>>> afterMapping : afterMappings) {
            List<ContentHandlerConfigMap<SAXVisitAfter>> elementMappings = afterMapping.getValue();
            for (ContentHandlerConfigMap<SAXVisitAfter> elementMapping : elementMappings) {
                String elementName = afterMapping.getKey();
                SAXVisitAfter handler = elementMapping.getContentHandler();

                if(handler instanceof SAXVisitChildren) {
                    childVisitors.addMapping(elementName, elementMapping.getResourceConfig(), (SAXVisitChildren) handler);
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