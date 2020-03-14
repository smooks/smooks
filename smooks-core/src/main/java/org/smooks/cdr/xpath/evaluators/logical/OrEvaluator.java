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

import org.smooks.delivery.sax.SAXElement;
import org.smooks.container.ExecutionContext;
import org.smooks.cdr.xpath.SelectorStep;
import org.w3c.dom.Element;
import org.jaxen.expr.LogicalExpr;
import org.jaxen.saxpath.SAXPathException;

import java.util.Properties;

/**
 * Simple "or" predicate evaluator.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class OrEvaluator extends AbstractLogicalEvaluator {

    public OrEvaluator(LogicalExpr expr, SelectorStep selectorStep, Properties namespaces) throws SAXPathException {
        super(expr, selectorStep, namespaces);
    }

    public boolean evaluate(SAXElement element, ExecutionContext executionContext) {
        return lhs.evaluate(element, executionContext) || rhs.evaluate(element, executionContext);
    }

    public boolean evaluate(Element element, ExecutionContext executionContext) {
        return lhs.evaluate(element, executionContext) || rhs.evaluate(element, executionContext);
    }
}