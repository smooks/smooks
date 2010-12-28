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
package org.milyn.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.delivery.ContentHandler;
import org.milyn.delivery.VisitSequence;
import org.milyn.event.report.annotation.VisitAfterReport;
import org.milyn.event.report.annotation.VisitBeforeReport;
import org.milyn.event.types.ElementVisitEvent;
import org.milyn.event.types.FilterLifecycleEvent;
import org.milyn.event.types.ElementPresentEvent;
import org.milyn.expression.MVELExpressionEvaluator;
import org.milyn.io.StreamUtils;
import org.milyn.util.ClassUtil;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Basic {@link ExecutionEventListener}.
 * <p/>
 * This event listener listens to and captures published events.
 * The list of captured events can be filtered by setting a list of
 * {@link #setFilterEvents filter event types}. 
 * <p/>
 * This listener should be used with great care.  It could quite easily consume
 * large amounts of memory if not used properly.  If access to this information
 * is required in a production environment, consider writing and using a more
 * specialized implementation of the {@link ExecutionEventListener} interface
 * i.e. an implementation that captures the information in a more memory-friendly way.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class BasicExecutionEventListener implements ExecutionEventListener {

    private static Log logger = LogFactory.getLog(BasicExecutionEventListener.class);
    
    private List<ExecutionEvent> events = new ArrayList<ExecutionEvent>();
    private List<? extends Class<? extends ExecutionEvent>> filterEvents;

    /**
     * Set a list of {@link ExecutionEvent event} types on which to filter.
     * <p/>
     * The listener will only capture {@link ExecutionEvent event} types
     * provided in this list.  If not set, all events will be captured.
     *
     * @param filterEvents Filter events.
     */
    public void setFilterEvents(Class<? extends ExecutionEvent>... filterEvents) {
        if(filterEvents != null) {
            this.filterEvents = Arrays.asList(filterEvents);
        } else {
            this.filterEvents = null;
        }
    }

    /**
     * Process the {@link ExecutionEvent}.
     * @param event The {@link ExecutionEvent}.
     */
    public void onEvent(ExecutionEvent event) {
        if(ignoreEvent(event)) {
            // Don't capture this event...
            return;
        }

        if(event != null) {
            events.add(event);
        } else {
            logger.warn("Invalid call to onEvent method.  null 'event' arg.");
        }
    }

    protected boolean ignoreEvent(ExecutionEvent event) {
        if(event instanceof FilterLifecycleEvent) {
            return false;
        } else if(event instanceof ElementPresentEvent) {
            return false;
        }

        if(filterEvents != null && !filterEvents.contains(event.getClass())) {
            return true;
        }

        if(event instanceof ElementVisitEvent) {
            ElementVisitEvent visitEvent = (ElementVisitEvent) event;
            ContentHandler handler = visitEvent.getConfigMapping().getContentHandler();
            if(visitEvent.getSequence() == VisitSequence.BEFORE) {
                VisitBeforeReport reportAnnotation = handler.getClass().getAnnotation(VisitBeforeReport.class);
                if(reportAnnotation != null) {
                    return !evalReportCondition(visitEvent, reportAnnotation.condition());
                }
            } else {
                VisitAfterReport reportAnnotation = handler.getClass().getAnnotation(VisitAfterReport.class);
                if(reportAnnotation != null) {
                    return !evalReportCondition(visitEvent, reportAnnotation.condition());
                }
            }
        }

        return false;
    }

    private boolean evalReportCondition(ElementVisitEvent visitEvent, String condition) {
        MVELExpressionEvaluator conditionEval = new MVELExpressionEvaluator();
        conditionEval.setExpression(condition);
        return conditionEval.eval(visitEvent.getResourceConfig());
    }

    /**
     * Get the {@link ExecutionEvent} list.
     * @return The {@link ExecutionEvent} list.
     */
    public List<ExecutionEvent> getEvents() {
        return events;
    }
}
