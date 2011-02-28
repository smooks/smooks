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
package org.milyn.event.types;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.AnnotationConstants;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.ContentHandler;
import org.milyn.delivery.ContentHandlerConfigMap;
import org.milyn.delivery.Filter;
import org.milyn.delivery.VisitSequence;
import org.milyn.event.ElementProcessingEvent;
import org.milyn.event.ResourceBasedEvent;
import org.milyn.event.report.annotation.VisitAfterReport;
import org.milyn.event.report.annotation.VisitBeforeReport;
import org.milyn.expression.MVELExpressionEvaluator;
import org.milyn.util.CollectionsUtil;
import org.milyn.util.MultiLineToStringBuilder;
import org.milyn.util.FreeMarkerTemplate;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;

/**
 * Element Visit Event.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ElementVisitEvent extends ElementProcessingEvent implements ResourceBasedEvent {

    private static Log logger = LogFactory.getLog(ElementVisitEvent.class);

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
        Map templateParams = new HashMap();

        templateParams.put("resource", configMapping.getResourceConfig());
        templateParams.put("execContext", executionContext);
        templateParams.put("event", this);

        if (!summary.equals(AnnotationConstants.NULL_STRING)) {
            FreeMarkerTemplate template = new FreeMarkerTemplate(summary);
            try {
                reportSummary = template.apply(templateParams);
            } catch (Exception e) {
                reportSummary = "Report Template Summary Error: " + e.getMessage();
                logger.debug("Failed to apply Summary Template.", e);
            }
        }

        if (!detailTemplate.equals(AnnotationConstants.NULL_STRING)) {
            FreeMarkerTemplate template = new FreeMarkerTemplate(detailTemplate, handlerClass);
            try {
                reportDetail = template.apply(templateParams);
            } catch (Exception e) {
                reportSummary = "Report Template Detail Error: " + e.getMessage();
                logger.debug("Failed to apply Detail Template.", e);
            }
        }
    }
}