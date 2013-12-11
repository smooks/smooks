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
package org.milyn.cdr.xsd11.conditiontests;

import org.milyn.commons.cdr.SmooksConfigurationException;
import org.milyn.container.ExecutionContext;
import org.milyn.expression.ExecutionContextExpressionEvaluator;
import org.milyn.expression.ExpressionEvaluationException;
import org.milyn.expression.ExpressionEvaluator;

/**
 * @author
 */
public class TestExpressionEvaluator implements ExecutionContextExpressionEvaluator {

    public static Object valVal = null;
    private String expression;

    public boolean eval(ExecutionContext context) throws ExpressionEvaluationException {
        return Boolean.parseBoolean(expression);
    }

    public Object getValue(ExecutionContext context) throws ExpressionEvaluationException {
        return valVal;
    }

    public ExpressionEvaluator setExpression(String expression) throws SmooksConfigurationException {
        this.expression = expression;
        return this;
    }

    public String getExpression() {
        return expression;
    }

    public boolean eval(Object contextObject) throws ExpressionEvaluationException {
        return Boolean.parseBoolean(expression);
    }

    public Object getValue(Object contextObject) throws ExpressionEvaluationException {
        return valVal;
    }
}
