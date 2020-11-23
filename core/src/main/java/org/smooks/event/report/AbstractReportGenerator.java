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
package org.smooks.event.report;

import org.smooks.SmooksException;
import org.smooks.assertion.AssertArgument;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.ContentDeliveryConfig;
import org.smooks.delivery.ContentHandlerBinding;
import org.smooks.delivery.Filter;
import org.smooks.delivery.VisitSequence;
import org.smooks.delivery.dom.DOMContentDeliveryConfig;
import org.smooks.delivery.dom.serialize.DefaultDOMSerializerVisitor;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.event.BasicExecutionEventListener;
import org.smooks.event.ElementProcessingEvent;
import org.smooks.event.ExecutionEvent;
import org.smooks.event.ResourceBasedEvent;
import org.smooks.event.report.model.*;
import org.smooks.event.types.*;
import org.smooks.payload.FilterResult;
import org.smooks.payload.JavaResult;
import org.smooks.payload.StringResult;
import org.smooks.xml.DomUtils;
import org.w3c.dom.Element;

import javax.xml.transform.Result;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Abstract execution report generator.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class AbstractReportGenerator extends BasicExecutionEventListener {

    private final ReportConfiguration reportConfiguration;

    private Report report;

    private ExecutionContext executionContext;
    private int messageNodeCounter = 0;
    private int reportInfoNodeCounter = 0;
    private final List<ExecutionEvent> preProcessingEvents = new ArrayList<>();
    private final List<ExecutionEvent> processingEvents = new ArrayList<>();
    private final Stack<ReportNode> reportNodeStack = new Stack<>();
    private final List<ReportNode> allNodes = new ArrayList<>();
    protected static final DefaultDOMSerializerVisitor DOM_SERIALIZER = new DefaultDOMSerializerVisitor();

    protected AbstractReportGenerator(ReportConfiguration reportConfiguration) {
        AssertArgument.isNotNull(reportConfiguration, "reportConfiguration");
        this.reportConfiguration = reportConfiguration;
        setFilterEvents(reportConfiguration.getFilterEvents());
    }

    public ReportConfiguration getReportConfiguration() {
        return reportConfiguration;
    }

    public Writer getOutputWriter() {
        return reportConfiguration.getOutputWriter();
    }

    /**
     * Process the {@link org.smooks.event.ExecutionEvent}.
     *
     * @param event The {@link org.smooks.event.ExecutionEvent}.
     */
    public void onEvent(ExecutionEvent event) {
        AssertArgument.isNotNull(event, "event");

        if(ignoreEvent(event)) {
            // Don't capture this event...
            return;
        }

        if (event instanceof FilterLifecycleEvent) {
            processLifecycleEvent((FilterLifecycleEvent) event);
        } else if (event instanceof ElementPresentEvent) {
            ReportNode node = new ReportNode((ElementPresentEvent) event);
            allNodes.add(node);
            processNewElementEvent(node);
        } else {
            if (reportNodeStack.isEmpty()) {
                // We haven't started to process the message/phase yet....
                preProcessingEvents.add(event);
            } else if (event instanceof ElementProcessingEvent) {
                // We have started processing the message/phase, so attach the event to the ReportNode
                // associated with the event element...
                ReportNode reportNode = getReportNode(((ElementProcessingEvent) event).getElement());

                if (reportNode != null) {
                    reportNode.elementProcessingEvents.add(event);
                }
            } else {
                processingEvents.add(event);
            }
        }
    }

    protected boolean ignoreEvent(ExecutionEvent event) {
        if(!super.ignoreEvent(event)) {
            if (event instanceof ResourceBasedEvent) {
                if (!reportConfiguration.showDefaultAppliedResources()) {
                    return ((ResourceBasedEvent) event).getResourceConfig().isDefaultResource();
                }
            }

            return false;
        }

        return true;
    }

    private void processLifecycleEvent(FilterLifecycleEvent event) {

        try {
            if (event.getEventType() != FilterLifecycleEvent.EventType.FINISHED) {
                ContentDeliveryConfig deliveryConfig = Filter.getCurrentExecutionContext().getDeliveryConfig();
                if (event instanceof DOMFilterLifecycleEvent) {
                    DOMFilterLifecycleEvent domEvent = (DOMFilterLifecycleEvent) event;
                    if (domEvent.getDOMEventType() == DOMFilterLifecycleEvent.DOMEventType.PROCESSING_STARTED) {
                        // Assembly phase is done... output assembly report just at the start of the
                        // processing phase...
                        mapMessageNodeVists(((DOMReport)report).getAssemblies());
                    } else if (domEvent.getDOMEventType() == DOMFilterLifecycleEvent.DOMEventType.SERIALIZATION_STARTED) {
                        // Processing phase is done (if it was)... output processing report just at the start of the
                        // serialization phase...
                        mapMessageNodeVists(report.getProcessings());
                    }
                } else if (event.getEventType() == FilterLifecycleEvent.EventType.STARTED) {
                    executionContext = Filter.getCurrentExecutionContext();

                    if(deliveryConfig instanceof DOMContentDeliveryConfig) {
                        report = new DOMReport();
                    } else {
                        report = new Report();
                    }
                    // Output the configuration builder events...
                    mapConfigBuilderEvents(deliveryConfig.getConfigBuilderEvents());
                }
            } else {
                processFinishEvent();
            }
        } catch (IOException e) {
            throw new SmooksException("Failed to write report.", e);
        }
    }

    private void processFinishEvent() throws IOException {
        if(report instanceof DOMReport) {
            if(report.getProcessings().isEmpty()) {
                mapMessageNodeVists(report.getProcessings());
            } else {
                mapMessageNodeVists(((DOMReport)report).getSerializations());
            }
        } else {
            mapMessageNodeVists(report.getProcessings());
        }

        List<ResultNode> resultNodes = new ArrayList<ResultNode>();
        Result[] results = FilterResult.getResults(executionContext);
        report.setResults(resultNodes);
        if(results != null) {
            for(Result result : results) {
                if(result != null) {
                	ResultNode resultNode = new ResultNode();
	                resultNodes.add(resultNode);
	                if(result instanceof JavaResult) {
	                    resultNode.setSummary("This Smooks Filtering operation produced a JavaResult.  The following is an XML serialization of the JavaResult bean Map entries.");
	                } else if(result instanceof StringResult) {
	                    resultNode.setSummary("This Smooks Filtering operation produced the following StreamResult.");
	                } else {
	                    resultNode.setSummary("Cannot show Smooks Filtering Result.  Modify the code and use a '" + StringResult.class.getName() + "' Result in the call to the Smooks.filter() method.");
	                }

                	resultNode.setDetail(result.toString());
                }
            }
        }

        try {
            applyTemplate(report);
        } finally {
            Writer writer = reportConfiguration.getOutputWriter();
            try {
                writer.flush();
            } finally {
                if(reportConfiguration.autoCloseWriter()) {
                    writer.close();
                }
            }
        }
    }

    private void processNewElementEvent(ReportNode node) {
        if (reportNodeStack.isEmpty()) {
            reportNodeStack.push(node);
        } else {
            ReportNode head = reportNodeStack.peek();

            while (head != null && node.depth <= head.depth) {
                // element associated with the current head node on the stack is closed. Drop back
                // a level in the report model before adding the new node...
                reportNodeStack.pop();
                if (!reportNodeStack.isEmpty()) {
                    head = reportNodeStack.peek();
                } else {
                    head = null;
                }
            }

            node.parent = head;
            if (node.parent != null) {
                node.parent.children.add(node);
            }
            reportNodeStack.push(node);
        }
    }

    private void mapConfigBuilderEvents(List<ConfigBuilderEvent> configBuilderEvents) {
    }

    private void mapMessageNodeVists(List<MessageNode> visits) throws IOException {
        if (!allNodes.isEmpty()) {
            mapNode(reportNodeStack.elementAt(0), visits);
        }

        // And clear everything...
        preProcessingEvents.clear();
        processingEvents.clear();
        reportNodeStack.clear();
        allNodes.clear();
    }

    private void mapNode(ReportNode reportNode, List<MessageNode> visits) throws IOException {
        List<ReportNode> children;
        MessageNode messageNode;

        messageNode = new MessageNode();
        messageNode.setNodeId(messageNodeCounter);
        messageNode.setElementName(reportNode.getElementName());
        messageNode.setVisitBefore(true);
        messageNode.setDepth(reportNode.getDepth());
        mapNodeEvents(VisitSequence.BEFORE, reportNode, messageNode);
        visits.add(messageNode);
        messageNodeCounter++;

        children = reportNode.children;
        for (ReportNode child : children) {
            mapNode(child, visits);
        }

        messageNode = new MessageNode();
        messageNode.setNodeId(messageNodeCounter);
        messageNode.setElementName(reportNode.getElementName());
        messageNode.setVisitBefore(false);
        messageNode.setDepth(reportNode.getDepth());
        mapNodeEvents(VisitSequence.AFTER, reportNode, messageNode);
        visits.add(messageNode);
        messageNodeCounter++;
    }

    private void mapNodeEvents(VisitSequence visitSequence, ReportNode reportNode, MessageNode messageNode) {
        List<ExecutionEvent> events = reportNode.getElementProcessingEvents();

        for (ExecutionEvent event : events) {
            if (event instanceof ElementVisitEvent) {
                ElementVisitEvent visitEvent = (ElementVisitEvent) event;

                if (visitEvent.getSequence() == visitSequence) {
                    ReportInfoNode reportInfoNode = new ReportInfoNode();
                    ContentHandlerBinding configMapping = ((ElementVisitEvent) event).getVisitorBinding();

                    messageNode.addExecInfoNode(reportInfoNode);

                    reportInfoNode.setNodeId(reportInfoNodeCounter);
                    reportInfoNode.setSummary(configMapping.getContentHandler().getClass().getSimpleName() + ": " + visitEvent.getReportSummary());
                    reportInfoNode.setDetail(visitEvent.getReportDetail());
                    reportInfoNode.setResourceXML(configMapping.getResourceConfig().toXML());
                    reportInfoNode.setContextState(visitEvent.getExecutionContextState());

                    reportInfoNodeCounter++;
                }
            }
        }
    }

    public abstract void applyTemplate(Report report) throws IOException;

    private ReportNode getReportNode(Object element) {
        for (ReportNode node : allNodes) {
            if (node.element == element) {
                return node;
            }
        }

        return null;
    }

    public class ReportNode {

        private ReportNode parent;
        private final List<ReportNode> children = new ArrayList<ReportNode>();
        private final Object element;
        private final int depth;
        private final List<ExecutionEvent> elementProcessingEvents = new ArrayList<ExecutionEvent>();

        public ReportNode(ElementPresentEvent eventPresentEvent) {
            this.element = eventPresentEvent.getElement();
            this.depth = eventPresentEvent.getDepth();
        }

        public String getElementName() {
            if(element instanceof SAXElement) {
                return ((SAXElement)element).getName().getLocalPart();
            } else {
                return DomUtils.getName((Element)element);
            }
        }

        public String toString() {
            return (element + " (depth " + depth + ")");
        }

        public ReportNode getParent() {
            return parent;
        }

        public List<ReportNode> getChildren() {
            return children;
        }

        public Object getElement() {
            return element;
        }

        public int getDepth() {
            return depth;
        }

        public List<ExecutionEvent> getElementProcessingEvents() {
            return elementProcessingEvents;
        }
    }
}
