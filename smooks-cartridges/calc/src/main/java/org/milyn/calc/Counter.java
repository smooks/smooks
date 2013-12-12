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
package org.milyn.calc;

import java.io.IOException;
import java.util.Set;

import org.milyn.commons.SmooksException;
import org.milyn.cdr.annotation.AppContext;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.cdr.annotation.ConfigParam.Use;
import org.milyn.container.ApplicationContext;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.Fragment;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.delivery.annotation.VisitAfterIf;
import org.milyn.delivery.annotation.VisitBeforeIf;
import org.milyn.delivery.dom.DOMVisitAfter;
import org.milyn.delivery.dom.DOMVisitBefore;
import org.milyn.delivery.ordering.Producer;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXUtil;
import org.milyn.delivery.sax.SAXVisitAfter;
import org.milyn.delivery.sax.SAXVisitBefore;
import org.milyn.expression.MVELExpressionEvaluator;
import org.milyn.javabean.context.BeanContext;
import org.milyn.javabean.repository.BeanId;
import org.milyn.commons.util.CollectionsUtil;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

/**
 * The counter can increment or decrement a value.
 * <p />
 * This counter has extended xml schema configuration. Take a look at the
 * schema {@link http://www.milyn.org/xsd/smooks/calc-1.1.xsd} for more
 * information.
 * <p />
 * Example basic configuration:
 * <pre>
 * &lt;resource-config selector="orderItems"&gt;
 *    &lt;resource&gt;org.milyn.calc.Counter&lt;/resource&gt;
 *    &lt;param name="beanId">count&lt;/param&gt;
 * &lt;/resource-config&gt;
 * <p />
 * Optional parameters:
 *    &lt;param name="start"&gt;1&lt;/param&gt;
 *    &lt;param name="amount"&gt;2&lt;/param&gt;
 *    &lt;param name="amountExpression"&gt;incrementAmount&lt;/param&gt;
 *    &lt;param name="startExpression"&gt;startValue&lt;/param&gt;
 *    &lt;param name="resetCondition"&gt;count == 10&lt;/param&gt;
 *    &lt;param name="direction"&gt;DECREMENT&lt;/param&gt;
 *    &lt;param name="executeAfter&gt;false&lt;/param&gt;
 * </pre>
 * Description of configuration properties:
 *
 * <ul>
 * <li><i>beanId</i>: The beanId in which the counter value is stored. The value is always stored as a Long type.</li>
 * <li><i>start</i>: The counter start value.</li>
 * <li><i>startExpression</i>: The result of this expression is the counter start value.
 *							   This expression is executed at the first count and every time the counter
 *							   is reset. The expression must result in an integer or a long.
 *							   If the startIndex attribute of the counter is set then this expression never gets
 *							   executed.</li>
 * <li><i>amount</i>: The amount that the counter increments or decrements the counter value.</li>
 * <li><i>amountExpression</i>: The result of this expression is the amount the counter increments or decrements.
 *								This expression is executed every time the counter counts.
 *								The expression must result in an integer.
 *								If the amount attribute of the counter is set then this expression never gets
 *								executed.</li>
 * <li><i>resetCondition</i>: When the expression is set and results in a true value then the counter is reset to
 *							  the start index. The expression must result in a boolean.</li>
 * <li><i>direction</i>: The direction that the counter counts. Can be INCREMENT (default) or DECREMENT.</li>
 * <li><i>executeAfter</i>: If the counter is executed after the element else it will execute before the element.
 *			    			Default is 'false'.</li>
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 * @since 1.1
 */
@VisitBeforeIf(	condition = "!parameters.containsKey('executeAfter') || parameters.executeAfter.value != 'true'")
@VisitAfterIf(	condition = "parameters.containsKey('executeAfter') && parameters.executeAfter.value == 'true'")
public class Counter implements SAXVisitBefore, SAXVisitAfter, DOMVisitBefore, DOMVisitAfter, Producer {

	public static final Long DEFAULT_START_INDEX = new Long(0);

	public static final int DEFAULT_AMOUNT = 1;

	@ConfigParam(name="beanId")
	private String beanIdName;

	@ConfigParam(use=Use.OPTIONAL)
	private Long start;

	@ConfigParam(use=Use.OPTIONAL)
	private Integer amount;

	@ConfigParam(use=Use.OPTIONAL)
	private MVELExpressionEvaluator amountExpression;

	@ConfigParam(use=Use.OPTIONAL)
	private MVELExpressionEvaluator startExpression;

	@ConfigParam(use=Use.OPTIONAL)
	private MVELExpressionEvaluator resetCondition;

	@ConfigParam(defaultVal="INCREMENT", choice = { "INCREMENT", "DECREMENT" })
	private String direction;

	private CountDirection countDirection;

	private BeanId beanId;

	@AppContext
	private ApplicationContext appContext;

	@Initialize
	public void initialize() {

		beanId = appContext.getBeanIdStore().register(beanIdName);

		countDirection = CountDirection.valueOf(direction);

	}

	public void visitBefore(SAXElement element,
			ExecutionContext executionContext) throws SmooksException,
			IOException {

		count(executionContext, new Fragment(element));
	}

	public void visitAfter(SAXElement element, ExecutionContext executionContext)
		throws SmooksException, IOException {

		count(executionContext, new Fragment(element));
	}

	public void visitBefore(Element element, ExecutionContext executionContext)
		throws SmooksException {

		count(executionContext, new Fragment(element));
	}

	public void visitAfter(Element element, ExecutionContext executionContext)
		throws SmooksException {

		count(executionContext, new Fragment(element));
	}

	public void count(ExecutionContext executionContext, Fragment source) {
		BeanContext beanContext = executionContext.getBeanContext();

		Long value = (Long) beanContext.getBean(beanId);

		if(value == null || (resetCondition != null && resetCondition.eval(beanContext.getBeanMap()))) {
			value = getStart(beanContext);
		} else {
			int amount = getAmount(beanContext);

			if(countDirection == CountDirection.INCREMENT) {
				value = value + amount;
			} else {
				value = value - amount;
			}
		}
		beanContext.addBean(beanId, value, source);
	}


	private Long getStart(BeanContext beanContext) {

		if(start == null && startExpression == null) {

			return DEFAULT_START_INDEX;

		} else if(start != null) {

			return start;

		} else {

			Object result = startExpression.getValue(beanContext.getBeanMap());

			if(!(result instanceof Long || result instanceof Integer)) {
				throw new SmooksException("The start expression must result in a Integer or a Long");
			}

			return new Long(result.toString());

		}
	}

	private int getAmount(BeanContext beanContext) {

		if(amount == null && amountExpression == null) {

			return DEFAULT_AMOUNT;

		} else if(amount != null) {

			return amount;

		} else {

			Object result = amountExpression.getValue(beanContext.getBeanMap());

			if(result instanceof Integer == false) {
				throw new SmooksException("The amount expression must result in a Integer");
			}

			return (Integer) result;
		}
	}

    public Set<? extends Object> getProducts() {
        return CollectionsUtil.toSet(beanIdName);
    }
}
