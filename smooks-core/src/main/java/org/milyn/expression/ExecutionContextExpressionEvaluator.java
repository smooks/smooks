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
package org.milyn.expression;

import org.milyn.container.ExecutionContext;


/**
 * {@link org.milyn.container.ExecutionContext} based expression evaluator.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface ExecutionContextExpressionEvaluator extends ExpressionEvaluator {

    /**
     * Evaluate an expression based on the supplied {@link org.milyn.container.ExecutionContext}.
     * @param context The context.
     * @return True if the condition evaluates successfully, otherwise false.
     * @throws ExpressionEvaluationException Invalid expression evaluation (implementation specific).
     */
    public boolean eval(ExecutionContext context) throws ExpressionEvaluationException;

    /**
     * Evaluate an expression based on the supplied {@link org.milyn.container.ExecutionContext}
     * and return the value.
     * @param context The context.
     * @return Expression evaluation result.
     * @throws ExpressionEvaluationException Invalid expression evaluation (implementation specific).
     */
    public Object getValue(ExecutionContext context) throws ExpressionEvaluationException;
}
