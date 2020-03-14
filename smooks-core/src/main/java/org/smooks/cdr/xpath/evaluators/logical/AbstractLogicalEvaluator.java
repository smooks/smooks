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
package org.smooks.cdr.xpath.evaluators.logical;

import org.smooks.cdr.xpath.evaluators.XPathExpressionEvaluator;
import org.smooks.cdr.xpath.SelectorStep;
import org.jaxen.expr.LogicalExpr;
import org.jaxen.saxpath.SAXPathException;

import java.util.Properties;

/**
 * Simple "and"/"or" predicate evaluator.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public abstract class AbstractLogicalEvaluator extends XPathExpressionEvaluator {

    protected XPathExpressionEvaluator lhs;
    private String op;
    protected XPathExpressionEvaluator rhs;

    public AbstractLogicalEvaluator(LogicalExpr expr, SelectorStep selectorStep, Properties namespaces) throws SAXPathException {
        lhs = XPathExpressionEvaluator.getInstance(expr.getLHS(), selectorStep, namespaces);
        op = expr.getOperator();
        rhs = XPathExpressionEvaluator.getInstance(expr.getRHS(), selectorStep, namespaces);
    }

    public XPathExpressionEvaluator getLhs() {
        return lhs;
    }

    public XPathExpressionEvaluator getRhs() {
        return rhs;
    }

    public String toString() {
        return "(" + lhs + " " + op + " " +  rhs + ")";
    }
}