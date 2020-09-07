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
package org.smooks.event.types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.cdr.annotation.AnnotationConstants;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.ContentHandler;
import org.smooks.delivery.ContentHandlerBinding;
import org.smooks.delivery.Filter;
import org.smooks.delivery.VisitSequence;
import org.smooks.event.ElementProcessingEvent;
import org.smooks.event.ResourceBasedEvent;
import org.smooks.event.report.annotation.VisitAfterReport;
import org.smooks.event.report.annotation.VisitBeforeReport;
import org.smooks.expression.MVELExpressionEvaluator;
import org.smooks.util.FreeMarkerTemplate;
import org.smooks.util.MultiLineToStringBuilder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Element Visit Event.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ElementVisitEvent extends ElementProcessingEvent implements ResourceBasedEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElementVisitEvent.class);

    private final ContentHandlerBinding configMapping;
    private final VisitSequence sequence;
    private String executionContextState;
    private Throwable error;
    private String reportSummary;
    private String reportDetail;

    public ElementVisitEvent(Object element, ContentHandlerBinding configMapping, VisitSequence sequence) {
        super(element);
        this.configMapping = configMapping;
        this.sequence = sequence;
        ExecutionContext executionContext = Filter.getCurrentExecutionContext();
        try {
            executionContextState = MultiLineToStringBuilder.toString(executionContext);

        } catch (Exception e) {
            StringWriter exceptionWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(exceptionWriter));
            executionContextState = "Execution Context Serialization Failure:\n" + exceptionWriter.toString();
        }
        initReport(executionContext);
    }

    public ElementVisitEvent(Object element, ContentHandlerBinding configMapping, VisitSequence sequence, Throwable error) {
        this(element, configMapping, sequence);
        this.error = error;
    }

    public SmooksResourceConfiguration getResourceConfig() {
        return configMapping.getSmooksResourceConfiguration();
    }

    public ContentHandlerBinding getConfigMapping() {
        return configMapping;
    }

    public VisitSequence getSequence() {
        return sequence;
    }

    public String getExecutionContextState() {
        return executionContextState;
    }

    public Throwable getError() {
        return error;
    }

    public String getReportSummary() {
        return reportSummary;
    }

    public String getReportDetail() {
        return reportDetail;
    }

    private void initReport(ExecutionContext executionContext) {
        ContentHandler handler = configMapping.getContentHandler();
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
            reportDetail = executionContextState;
        }
    }

    private boolean evalReportCondition(String condition) {
        MVELExpressionEvaluator conditionEval = new MVELExpressionEvaluator();
        conditionEval.setExpression(condition);
        return conditionEval.eval(configMapping.getSmooksResourceConfiguration());
    }

    private void applyReportTemplates(String summary, String detailTemplate, Class handlerClass, ExecutionContext executionContext) {
        Map<String, Object> templateParams = new HashMap<String, Object>();

        templateParams.put("resource", configMapping.getSmooksResourceConfiguration());
        templateParams.put("execContext", executionContext);
        templateParams.put("event", this);

        if (!summary.equals(AnnotationConstants.NULL_STRING)) {
            FreeMarkerTemplate template = new FreeMarkerTemplate(summary);
            try {
                reportSummary = template.apply(templateParams);
            } catch (Exception e) {
                reportSummary = "Report Template Summary Error: " + e.getMessage();
                LOGGER.debug("Failed to apply Summary Template.", e);
            }
        }

        if (!detailTemplate.equals(AnnotationConstants.NULL_STRING)) {
            FreeMarkerTemplate template = new FreeMarkerTemplate(detailTemplate, handlerClass);
            try {
                reportDetail = template.apply(templateParams);
            } catch (Exception e) {
                reportSummary = "Report Template Detail Error: " + e.getMessage();
                LOGGER.debug("Failed to apply Detail Template.", e);
            }
        }
    }
}
