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

import org.milyn.commons.cdr.SmooksConfigurationException;
import org.milyn.commons.util.ClassUtil;

/**
 * Abstract expression evaluator interface.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface ExpressionEvaluator {

    /**
     * Set the condition expression for the evaluator implementation.
     *
     * @param expression The expression to be evaluated by the evaluator implementation.
     * @throws SmooksConfigurationException Invalid expression configuration.
     */
    public ExpressionEvaluator setExpression(String expression) throws SmooksConfigurationException;

    /**
     * Get the String representation of the active expression on the evaluator instance.
     *
     * @return The active expression String representation.
     */
    public String getExpression();

    /**
     * Evaluate a conditional expression against the supplied object (can be a Map).
     *
     * @param contextObject The object against which the expression is to be evaluated.
     * @return True if the expression evaluates to true, otherwise false.
     * @throws ExpressionEvaluationException Invalid expression evaluation condition (implementation specific).
     */
    public boolean eval(Object contextObject) throws ExpressionEvaluationException;

    /**
     * Evaluate an expression against the supplied Map variable, returning the eval result.
     *
     * @param contextObject
     * @return Expression evaluation result.
     * @throws ExpressionEvaluationException Invalid expression evaluation (implementation specific).
     */
    public Object getValue(Object contextObject) throws ExpressionEvaluationException;

    /**
     * Factory method for creating ExpressionEvaluator instances.
     */
    public static class Factory {

        public static ExpressionEvaluator createInstance(String className, String conditionExpression) {
            try {
                ExpressionEvaluator evaluator = (ExpressionEvaluator) ClassUtil.forName(className, Factory.class).newInstance();

                if (!(evaluator instanceof ExecutionContextExpressionEvaluator)) {
                    throw new SmooksConfigurationException("Unsupported ExpressionEvaluator type '" + className + "'.  Currently only support '" + ExecutionContextExpressionEvaluator.class.getName() + "' implementations.");
                }
                evaluator.setExpression(conditionExpression);

                return evaluator;
            } catch (ClassNotFoundException e) {
                throw new SmooksConfigurationException("Failed to load ExpressionEvaluator Class '" + className + "'.", e);
            } catch (ClassCastException e) {
                throw new SmooksConfigurationException("Class '" + className + "' is not a valid ExpressionEvaluator.  It doesn't implement " + ExpressionEvaluator.class.getName());
            } catch (IllegalAccessException e) {
                throw new SmooksConfigurationException("Failed to load ExpressionEvaluator Class '" + className + "'.", e);
            } catch (InstantiationException e) {
                throw new SmooksConfigurationException("Failed to load ExpressionEvaluator Class '" + className + "'.", e);
            }
        }
    }
}
