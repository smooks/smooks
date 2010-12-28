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
package org.milyn.cdr.xpath.evaluators;

import org.milyn.delivery.sax.SAXElement;
import org.milyn.container.ExecutionContext;
import org.milyn.assertion.AssertArgument;
import org.milyn.cdr.xpath.evaluators.logical.AndEvaluator;
import org.milyn.cdr.xpath.evaluators.logical.OrEvaluator;
import org.milyn.cdr.xpath.evaluators.equality.*;
import org.milyn.cdr.xpath.SelectorStep;
import org.w3c.dom.Element;
import org.jaxen.expr.*;
import org.jaxen.saxpath.SAXPathException;

import java.util.Properties;

/**
 * Jaxen XPath expression evaluator.
 * 
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public abstract class XPathExpressionEvaluator {

    /**
     * Does this XPath expression evaluate for the supplied {@link org.milyn.delivery.sax.SAXElement} context object.
     * <p/>
     * The implementation can update the context to a parent element if the expression targets
     * multiple contexts.
     *
     * @param element          The {@link org.milyn.delivery.sax.SAXElement} context to be evaluated against.
     * @param executionContext Smooks {@link org.milyn.container.ExecutionContext}.
     * @return True if the expression evaluates, otherwise false.
     */
    public abstract boolean evaluate(SAXElement element, ExecutionContext executionContext);

    /**
     * Does this XPath expression evaluate for the supplied {@link org.w3c.dom.Element} context object.
     * <p/>
     * The implementation can update the context to a parent element if the expression targets
     * multiple contexts.
     *
     * @param element          The {@link org.w3c.dom.Element} context to be evaluated against.
     * @param executionContext Smooks {@link org.milyn.container.ExecutionContext}.
     * @return True if the expression evaluates, otherwise false.
     */
    public abstract boolean evaluate(Element element, ExecutionContext executionContext);

    /**
     * {@link XPathExpressionEvaluator} factory method.
     * @param expr Jaxen XPath expression.
     * @param selectorStep Selector Step.
     * @param namespaces Namespace set.
     * @return The {@link XPathExpressionEvaluator} for the Jaxen expression.
     */
    public static XPathExpressionEvaluator getInstance(Expr expr, SelectorStep selectorStep, Properties namespaces) throws SAXPathException {
        AssertArgument.isNotNull(expr, "expr");

        if(expr instanceof LogicalExpr) {
            LogicalExpr logicalExpr = (LogicalExpr) expr;
            if(logicalExpr.getOperator().equalsIgnoreCase("and")) {
                return new AndEvaluator(logicalExpr, selectorStep, namespaces);
            } else if(logicalExpr.getOperator().equalsIgnoreCase("or")) {
                return new OrEvaluator(logicalExpr, selectorStep, namespaces);
            }
        } else if(expr instanceof EqualityExpr) {
            EqualityExpr equalityExpr = (EqualityExpr) expr;
            if(equalityExpr.getOperator().equalsIgnoreCase("=")) {
                return new EqualsEvaluator(equalityExpr, namespaces);
            } else if(equalityExpr.getOperator().equalsIgnoreCase("!=")) {
                return new NotEqualsEvaluator(equalityExpr, namespaces);
            }
        } else if(expr instanceof RelationalExpr) {
            RelationalExpr relationalExpr = (RelationalExpr) expr;
            if(relationalExpr.getOperator().equalsIgnoreCase("<")) {
                return new LessThanEvaluator(relationalExpr, namespaces);
            } else if(relationalExpr.getOperator().equalsIgnoreCase(">")) {
                return new GreaterThanEvaluator(relationalExpr, namespaces);
            }
        } else if(expr instanceof NumberExpr) {
            return new IndexEvaluator(((NumberExpr)expr).getNumber().intValue(), selectorStep);
        }

        throw new SAXPathException("Unsupported XPath expr token '" + expr.getText() + "'.");
    }
}
