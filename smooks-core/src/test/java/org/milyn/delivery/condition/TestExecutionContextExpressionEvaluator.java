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
package org.milyn.delivery.condition;

import org.milyn.commons.cdr.SmooksConfigurationException;
import org.milyn.container.ExecutionContext;
import org.milyn.expression.ExecutionContextExpressionEvaluator;
import org.milyn.expression.ExpressionEvaluationException;
import org.milyn.expression.ExpressionEvaluator;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class TestExecutionContextExpressionEvaluator implements ExecutionContextExpressionEvaluator {

    public String condition;
    public boolean evalResult = true;
    public static ExecutionContext context;

    public ExpressionEvaluator setExpression(String conditionExpression) throws SmooksConfigurationException {
        condition = conditionExpression;
        evalResult = conditionExpression.trim().equals("true");
        return this;
    }

    public String getExpression() {
        return condition;
    }

    public boolean eval(Object contextObject) throws ExpressionEvaluationException {
        return false;
    }

    public Object getValue(Object contextObject) throws ExpressionEvaluationException {
        return null;
    }

    public boolean eval(ExecutionContext context) {
        TestExecutionContextExpressionEvaluator.context = context;
        return evalResult;
    }

    public Object getValue(ExecutionContext context) throws ExpressionEvaluationException {
        return null;
    }
}
