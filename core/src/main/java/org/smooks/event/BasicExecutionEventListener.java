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
package org.smooks.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.delivery.ContentHandler;
import org.smooks.delivery.VisitSequence;
import org.smooks.event.report.annotation.VisitAfterReport;
import org.smooks.event.report.annotation.VisitBeforeReport;
import org.smooks.event.types.FilterLifecycleEvent;
import org.smooks.event.types.StartFragmentEvent;
import org.smooks.event.types.VisitEvent;
import org.smooks.expression.MVELExpressionEvaluator;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicExecutionEventListener.class);
    
    private final List<ExecutionEvent> events = new ArrayList<ExecutionEvent>();
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
     * @param executionEvent The {@link ExecutionEvent}.
     */
    public void onEvent(ExecutionEvent executionEvent) {
        if(ignoreEvent(executionEvent)) {
            // Don't capture this event...
            return;
        }

        if(executionEvent != null) {
            events.add(executionEvent);
        } else {
            LOGGER.warn("Invalid call to onEvent method.  null 'event' arg.");
        }
    }

    protected boolean ignoreEvent(ExecutionEvent event) {
        if(event instanceof FilterLifecycleEvent) {
            return false;
        } else if(event instanceof StartFragmentEvent) {
            return false;
        }

        if(filterEvents != null && !filterEvents.contains(event.getClass())) {
            return true;
        }

        if(event instanceof VisitEvent) {
            VisitEvent visitEvent = (VisitEvent) event;
            ContentHandler handler = visitEvent.getVisitorBinding().getContentHandler();
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

    private boolean evalReportCondition(VisitEvent visitEvent, String condition) {
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
