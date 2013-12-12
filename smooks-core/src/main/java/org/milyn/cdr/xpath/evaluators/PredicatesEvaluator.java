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

import org.milyn.commons.assertion.AssertArgument;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.sax.SAXElement;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Predicates Evaluator.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class PredicatesEvaluator extends XPathExpressionEvaluator {

    private List<XPathExpressionEvaluator> evaluators = new ArrayList<XPathExpressionEvaluator>();
    private int evalCount;

    public void addEvaluator(XPathExpressionEvaluator evaluator) {
        AssertArgument.isNotNull(evaluator, "evaluator");
        evaluators.add(evaluator);
        evalCount = evaluators.size();
    }

    public List<XPathExpressionEvaluator> getEvaluators() {
        return evaluators;
    }

    public boolean evaluate(SAXElement element, ExecutionContext executionContext) {
        for (int i = 0; i < evalCount; i++) {
            if (!evaluators.get(i).evaluate(element, executionContext)) {
                return false;
            }
        }
        return true;
    }

    public boolean evaluate(Element element, ExecutionContext executionContext) {
        for (int i = 0; i < evalCount; i++) {
            if (!evaluators.get(i).evaluate(element, executionContext)) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        if (!evaluators.isEmpty()) {
            for (XPathExpressionEvaluator evaluator : evaluators) {
                if (builder.length() > 0) {
                    builder.append(" and ");
                }
                builder.append(evaluator);
            }
        }

        return builder.toString();
    }
}
