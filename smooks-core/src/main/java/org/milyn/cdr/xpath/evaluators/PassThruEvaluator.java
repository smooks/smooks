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

import org.milyn.cdr.xpath.evaluators.XPathExpressionEvaluator;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.container.ExecutionContext;
import org.w3c.dom.Element;

/**
 * Simple Pass-thru predicate evaluator.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class PassThruEvaluator extends XPathExpressionEvaluator {

    public static final PassThruEvaluator INSTANCE = new PassThruEvaluator();

    private PassThruEvaluator() {
    }

    public boolean evaluate(SAXElement element, ExecutionContext executionContext) {
        return true;
    }

    public boolean evaluate(Element element, ExecutionContext executionContext) {
        return true;
    }

    public String toString() {
        return "";
    }
}
