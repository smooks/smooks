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
package org.smooks.engine.delivery.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.ExecutionContext;
import org.smooks.api.delivery.ContentHandler;
import org.smooks.api.delivery.ContentHandlerBinding;
import org.smooks.api.delivery.event.ResourceBasedEvent;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.visitor.VisitAfterReport;
import org.smooks.api.resource.visitor.VisitBeforeReport;
import org.smooks.api.resource.visitor.VisitReport;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.engine.expression.MVELExpressionEvaluator;
import org.smooks.support.FreeMarkerTemplate;
import org.smooks.support.MultiLineToStringBuilder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Element Visit Event.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class VisitEvent<F, T extends Visitor> extends FragmentEvent<F> implements ResourceBasedEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(VisitEvent.class);

    private final ContentHandlerBinding<T> visitorBinding;
    private final VisitSequence sequence;
    private final ExecutionContext executionContext;
    private String executionContextState;
    private Throwable error;
    private String reportSummary;
    private String reportDetail;

    public VisitEvent(Fragment<F> fragment, ContentHandlerBinding<T> visitorBinding, VisitSequence sequence, ExecutionContext executionContext) {
        super(fragment);
        this.visitorBinding = visitorBinding;
        this.sequence = sequence;
        this.executionContext = executionContext;
    }

    public VisitEvent(Fragment<F> fragment, ContentHandlerBinding<T> visitorBinding, VisitSequence sequence, ExecutionContext executionContext, Throwable error) {
        this(fragment, visitorBinding, sequence, executionContext);
        this.error = error;
    }

    @Override
    public ResourceConfig getResourceConfig() {
        return visitorBinding.getResourceConfig();
    }

    public ContentHandlerBinding<T> getVisitorBinding() {
        return visitorBinding;
    }

    public VisitSequence getSequence() {
        return sequence;
    }

    public String getExecutionContextState() {
        if (executionContextState == null) {
            try {
                executionContextState = MultiLineToStringBuilder.toString(executionContext);
            } catch (Exception e) {
                StringWriter exceptionWriter = new StringWriter();
                e.printStackTrace(new PrintWriter(exceptionWriter));
                executionContextState = "Execution Context Serialization Failure:\n" + exceptionWriter.toString();
            }
        }
        return executionContextState;
    }

    public Throwable getError() {
        return error;
    }

    public String getReportSummary() {
        if (reportSummary == null) {
            initReport(executionContext);
        }
        return reportSummary;
    }

    public String getReportDetail() {
        if (reportDetail == null) {
            initReport(executionContext);
        }
        return reportDetail;
    }

    private void initReport(ExecutionContext executionContext) {
        ContentHandler handler = visitorBinding.getContentHandler();
        if (getSequence() == VisitSequence.BEFORE) {
            VisitBeforeReport reportAnnotation = handler.getClass().getAnnotation(VisitBeforeReport.class);
            if (reportAnnotation != null && evalReportCondition(reportAnnotation.condition())) {
                applyReportTemplates(reportAnnotation.summary(), reportAnnotation.detailTemplate(), handler.getClass(), executionContext);
            }
        } else {
            VisitAfterReport reportAnnotation = handler.getClass().getAnnotation(VisitAfterReport.class);
            if (reportAnnotation != null && evalReportCondition(reportAnnotation.condition())) {
                applyReportTemplates(reportAnnotation.summary(), reportAnnotation.detailTemplate(), handler.getClass(), executionContext);
            }
        }

        if (reportDetail == null) {
            // No template ...
            reportDetail = getExecutionContextState();
        }
    }

    private boolean evalReportCondition(String condition) {
        MVELExpressionEvaluator conditionEval = new MVELExpressionEvaluator();
        conditionEval.setExpression(condition);
        return conditionEval.eval(visitorBinding.getResourceConfig());
    }

    private void applyReportTemplates(String summary, String detailTemplate, Class<?> handlerClass, ExecutionContext executionContext) {
        Map<String, Object> templateParams = new HashMap<>();

        templateParams.put("resource", visitorBinding.getResourceConfig());
        templateParams.put("execContext", executionContext);
        templateParams.put("event", this);

        if (!summary.equals(VisitReport.NULL_STRING)) {
            FreeMarkerTemplate template = new FreeMarkerTemplate(summary);
            try {
                reportSummary = template.apply(templateParams);
            } catch (Exception e) {
                reportSummary = "Report Template Summary Error: " + e.getMessage();
                LOGGER.warn("Failed to apply Summary Template.", e);
            }
        }

        if (!detailTemplate.equals(VisitReport.NULL_STRING)) {
            FreeMarkerTemplate template = new FreeMarkerTemplate(detailTemplate, handlerClass);
            try {
                reportDetail = template.apply(templateParams);
            } catch (Exception e) {
                reportSummary = "Report Template Detail Error: " + e.getMessage();
                LOGGER.warn("Failed to apply Detail Template.", e);
            }
        }
    }
}
