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
package org.milyn.cdr.xpath.evaluators.value;

import org.milyn.delivery.sax.SAXElement;
import org.milyn.assertion.AssertArgument;
import org.milyn.javabean.DataDecoder;
import org.milyn.cdr.xpath.evaluators.PredicatesEvaluatorBuilder;
import org.w3c.dom.Element;
import org.jaxen.expr.*;
import org.jaxen.saxpath.Axis;
import org.jaxen.saxpath.SAXPathException;

import java.util.List;
import java.util.Properties;

/**
 * Element Value.
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public abstract class Value {

    public abstract Object getValue(SAXElement element);

    public abstract Object getValue(Element element);

    public static Value getValue(Expr expr, DataDecoder decoder, Properties namespaces) throws SAXPathException {
        AssertArgument.isNotNull(expr, "expr");

        if(expr instanceof LocationPath) {
            LocationPath locationPath = (LocationPath) expr;
            List<Step> steps = locationPath.getSteps();

            if(steps != null && steps.size() == 1) {
                Step step = steps.get(0);

                if(step.getAxis() == Axis.CHILD && step instanceof TextNodeStep) {
                    return new TextValue(decoder);
                } else if(step.getAxis() == Axis.ATTRIBUTE && step instanceof NameStep) {
                    String nsPrefix = ((NameStep)step).getPrefix();
                    String localPart = ((NameStep)step).getLocalName();

                    if(nsPrefix != null && !nsPrefix.trim().equals("")) {
                        return new AttributeValue(PredicatesEvaluatorBuilder.getNamespace(nsPrefix, namespaces), localPart, decoder);
                    } else {
                        return new AttributeValue(null, localPart, decoder);
                    }
                }
            }
        } else if(expr instanceof NumberExpr) {
            return new AbsoluteValue((NumberExpr) expr);
        } else if(expr instanceof LiteralExpr) {
            return new AbsoluteValue((LiteralExpr) expr);
        }

        throw new SAXPathException("Unsupported XPath value token '" + expr.getText() + "'.");
    }
}
