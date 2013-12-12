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
package org.milyn.cdr.xpath.evaluators.equality;

import org.jaxen.expr.EqualityExpr;
import org.jaxen.saxpath.SAXPathException;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.sax.SAXElement;
import org.w3c.dom.Element;

import java.util.Properties;

/**
 * Simple "=" predicate evaluator.
 * <p/>
 * Works for element text or attributes.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class EqualsEvaluator extends AbstractEqualityEvaluator {

    public EqualsEvaluator(EqualityExpr expr, Properties namespaces) throws SAXPathException {
        super(expr, namespaces);
    }

    public boolean evaluate(SAXElement element, ExecutionContext executionContext) {
        return lhs.getValue(element).equals(rhs.getValue(element));
    }

    public boolean evaluate(Element element, ExecutionContext executionContext) {
        return lhs.getValue(element).equals(rhs.getValue(element));
    }
}