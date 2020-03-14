/*
 * Milyn - Copyright (C) 2006 - 2010
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License (version 2.1) as published
 * by the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * 
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */
package org.smooks.camel.routing;

import org.smooks.assertion.AssertArgument;
import org.smooks.expression.ExecutionContextExpressionEvaluator;
import org.smooks.javabean.context.BeanContext;
import org.smooks.javabean.lifecycle.BeanContextLifecycleEvent;
import org.smooks.javabean.lifecycle.BeanContextLifecycleObserver;
import org.smooks.javabean.lifecycle.BeanLifecycle;

/**
 * BeanRouterObserver is a {@link BeanContextLifecycleObserver} that will route 
 * a specified bean to the configured endpoint. 
 * </p>
 * 
 * @author Daniel Bevenius
 */
public class BeanRouterObserver implements BeanContextLifecycleObserver
{
    private BeanRouter beanRouter;
    private final String beanId;
    private ExecutionContextExpressionEvaluator conditionEvaluator;

    /**
     * Sole contructor.
     * @param beanRouter The bean router instance to be used for routing beans.
     * @param beanId The beanId which is the beanId in the Smooks {@link BeanContext}.
     */
    public BeanRouterObserver(final BeanRouter beanRouter, final String beanId)
    {
        AssertArgument.isNotNull(beanRouter, "beanRouter");
        AssertArgument.isNotNull(beanId, "beanId");
        
        this.beanRouter = beanRouter;
        this.beanId = beanId;
    }

    /**
     * Set the condition evaluator for performing the routing.
     * <p/>
     * Used to test if the routing is to be performed based on the
     * user configured condition.
     * @param conditionEvaluator The routing condition evaluator.
     */
    public void setConditionEvaluator(ExecutionContextExpressionEvaluator conditionEvaluator) {
        this.conditionEvaluator = conditionEvaluator;
    }

    /**
     * Will route to the endpoint if the BeanLifecycle is of type BeanLifecycle.REMOVE and
     * the beanId is equals to the beanId that was configured for this instance.
     */
    public void onBeanLifecycleEvent(final BeanContextLifecycleEvent event)
    {
        if (endEventAndBeanIdMatch(event) && conditionsMatch(event))
        {
            beanRouter.sendBean(event.getBean(), event.getExecutionContext());
        }
    }

    private boolean endEventAndBeanIdMatch(final BeanContextLifecycleEvent event)
    {
        return event.getLifecycle() == BeanLifecycle.END_FRAGMENT && event.getBeanId().getName().equals(beanId);

    }

    public boolean conditionsMatch(BeanContextLifecycleEvent event) {
        if(conditionEvaluator == null) {
            return true;
        }

        try {
            return conditionEvaluator.eval(event.getExecutionContext());
        } catch (Exception e) {
            return false;
        }
    }
}
