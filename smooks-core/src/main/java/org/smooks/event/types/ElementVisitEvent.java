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
package org.smooks.event.types;

import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.cdr.annotation.AnnotationConstants;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.ContentHandler;
import org.smooks.delivery.ContentHandlerConfigMap;
import org.smooks.delivery.Filter;
import org.smooks.delivery.VisitSequence;
import org.smooks.event.ElementProcessingEvent;
import org.smooks.event.ResourceBasedEvent;
import org.smooks.event.report.annotation.VisitAfterReport;
import org.smooks.event.report.annotation.VisitBeforeReport;
import org.smooks.expression.MVELExpressionEvaluator;
import org.smooks.util.FreeMarkerTemplate;
import org.smooks.util.MultiLineToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private ContentHandlerConfigMap configMapping;
    private VisitSequence sequence;
    private String executionContextState;
    private Throwable error;
    private String reportSummary;
    private String reportDetail;

    public ElementVisitEvent(Object element, ContentHandlerConfigMap configMapping, VisitSequence sequence) {
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

    public ElementVisitEvent(Object element, ContentHandlerConfigMap configMapping, VisitSequence sequence, Throwable error) {
        this(element, configMapping, sequence);
        this.error = error;
    }

    public SmooksResourceConfiguration getResourceConfig() {
        return configMapping.getResourceConfig();
    }

    public ContentHandlerConfigMap getConfigMapping() {
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
        return conditionEval.eval(configMapping.getResourceConfig());
    }

    private void applyReportTemplates(String summary, String detailTemplate, Class handlerClass, ExecutionContext executionContext) {
        Map<String, Object> templateParams = new HashMap<String, Object>();

        templateParams.put("resource", configMapping.getResourceConfig());
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
