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
package org.milyn.smooks.camel.routing;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.AppContext;
import org.milyn.cdr.annotation.Config;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.commons.SmooksException;
import org.milyn.commons.assertion.AssertArgument;
import org.milyn.commons.cdr.SmooksConfigurationException;
import org.milyn.commons.util.FreeMarkerTemplate;
import org.milyn.container.ApplicationContext;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.ExecutionLifecycleCleanable;
import org.milyn.delivery.ExecutionLifecycleInitializable;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.delivery.annotation.Uninitialize;
import org.milyn.delivery.ordering.Consumer;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXVisitAfter;
import org.milyn.expression.ExecutionContextExpressionEvaluator;
import org.milyn.util.FreeMarkerUtils;

import java.io.IOException;

/**
 * Camel bean routing visitor.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 * @author <a href="mailto:daniel.bevenius@gmail.com">daniel.bevenius@gmail.com</a>
 */
public class BeanRouter implements SAXVisitAfter, Consumer, ExecutionLifecycleInitializable, ExecutionLifecycleCleanable {

    @ConfigParam
    private String beanId;

    @ConfigParam
    private String toEndpoint;

    @ConfigParam(use = ConfigParam.Use.OPTIONAL)
    private String correlationIdName;

    @ConfigParam(use = ConfigParam.Use.OPTIONAL)
    private FreeMarkerTemplate correlationIdPattern;

    @AppContext
    private ApplicationContext applicationContext;

    @Config
    SmooksResourceConfiguration routingConfig;

    private ProducerTemplate producerTemplate;
    private BeanRouterObserver camelRouterObserable;
    private CamelContext camelContext;

    public BeanRouter() {
    }

    public BeanRouter(final CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @Initialize
    public void initialize() {
        if (routingConfig == null) {
            routingConfig = new SmooksResourceConfiguration();
        }

        producerTemplate = getCamelContext().createProducerTemplate();
        if (isBeanRoutingConfigured()) {
            camelRouterObserable = new BeanRouterObserver(this, beanId);
            camelRouterObserable.setConditionEvaluator((ExecutionContextExpressionEvaluator) routingConfig.getConditionEvaluator());
        }

        if (correlationIdName != null && correlationIdPattern == null) {
            throw new SmooksConfigurationException("Camel router component configured with a 'correlationIdName', but 'correlationIdPattern' is not configured.");
        }
        if (correlationIdName == null && correlationIdPattern != null) {
            throw new SmooksConfigurationException("Camel router component configured with a 'correlationIdPattern', but 'correlationIdName' is not configured.");
        }
    }

    /**
     * Set the beanId of the bean to be routed.
     *
     * @param beanId the beanId to set
     * @return This router instance.
     */
    public BeanRouter setBeanId(final String beanId) {
        this.beanId = beanId;
        return this;
    }

    /**
     * Set the Camel endpoint to which the bean is to be routed.
     *
     * @param toEndpoint the toEndpoint to set
     * @return This router instance.
     */
    public BeanRouter setToEndpoint(final String toEndpoint) {
        this.toEndpoint = toEndpoint;
        return this;
    }

    /**
     * Set the correlationId header name.
     *
     * @return This router instance.
     */
    public BeanRouter setCorrelationIdName(String correlationIdName) {
        AssertArgument.isNotNullAndNotEmpty(correlationIdName, "correlationIdName");
        this.correlationIdName = correlationIdName;
        return this;
    }

    /**
     * Set the correlationId pattern used to generate correlationIds.
     *
     * @param correlationIdPattern The pattern generator template.
     * @return This router instance.
     */
    public BeanRouter setCorrelationIdPattern(final String correlationIdPattern) {
        this.correlationIdPattern = new FreeMarkerTemplate(correlationIdPattern);
        return this;
    }

    public void visitAfter(final SAXElement element, final ExecutionContext execContext) throws SmooksException, IOException {
        final Object bean = getBeanFromExecutionContext(execContext, beanId);

        sendBean(bean, execContext);
    }

    /**
     * Send the bean to the target endpoint.
     *
     * @param bean        The bean to be sent.
     * @param execContext The execution context.
     */
    protected void sendBean(final Object bean, final ExecutionContext execContext) {
        try {
            if (correlationIdPattern != null) {
                Processor processor = new Processor() {
                    public void process(Exchange exchange) {
                        Message in = exchange.getIn();
                        in.setBody(bean);
                        in.setHeader(correlationIdName, correlationIdPattern.apply(FreeMarkerUtils.getMergedModel(execContext)));
                    }
                };
                producerTemplate.send(toEndpoint, processor);
            } else {
                producerTemplate.sendBodyAndHeaders(toEndpoint, bean, execContext.getBeanContext().getBeanMap());
            }
        } catch (final Exception e) {
            throw new SmooksException("Exception routing beanId '" + beanId + "' to endpoint '" + toEndpoint + "'.", e);
        }
    }

    private Object getBeanFromExecutionContext(final ExecutionContext execContext, final String beanId) {
        final Object bean = execContext.getBeanContext().getBean(beanId);
        if (bean == null) {
            throw new SmooksException("Exception routing beanId '" + beanId
                    + "'. The bean was not found in the Smooks ExceutionContext.");
        }

        return bean;
    }

    private CamelContext getCamelContext() {
        if (camelContext == null)
            return (CamelContext) applicationContext.getAttribute(CamelContext.class);
        else
            return camelContext;
    }

    private boolean isBeanRoutingConfigured() {
        return "none".equals(routingConfig.getSelector());
    }

    @Uninitialize
    public void uninitialize() {
        try {
            producerTemplate.stop();
        } catch (final Exception e) {
            throw new SmooksException(e.getMessage(), e);
        }
    }

    public boolean consumes(final Object object) {
        return beanId.equals(object);
    }

    public void executeExecutionLifecycleInitialize(final ExecutionContext executionContext) {
        if (isBeanRoutingConfigured()) {
            executionContext.getBeanContext().addObserver(camelRouterObserable);
        }
    }

    public void executeExecutionLifecycleCleanup(ExecutionContext executionContext) {
        if (isBeanRoutingConfigured()) {
            executionContext.getBeanContext().removeObserver(camelRouterObserable);
        }
    }

}
