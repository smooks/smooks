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
package org.smooks.cdr.xpath.evaluators.equality;

import org.smooks.cdr.xpath.evaluators.XPathExpressionEvaluator;
import org.smooks.cdr.xpath.evaluators.value.Value;
import org.smooks.javabean.DataDecoder;
import org.smooks.javabean.DataDecodeException;
import org.smooks.javabean.decoders.StringDecoder;
import org.smooks.javabean.decoders.DoubleDecoder;
import org.jaxen.expr.NumberExpr;
import org.jaxen.expr.Expr;
import org.jaxen.expr.BinaryExpr;
import org.jaxen.saxpath.SAXPathException;

import java.util.Properties;

/**
 * Simple equality predicate evaluator.
 * <p/>
 * Works for element text or attributes. Covers Equality and Relational XPath expressions.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public abstract class AbstractEqualityEvaluator extends XPathExpressionEvaluator {

    private static final DataDecoder STRING_DECODER = new StringDecoder();
    private static final DataDecoder NUMBER_DECODER = new XPathNumberDecoder();

    protected Value lhs;
    private String op;
    protected Value rhs;

    public AbstractEqualityEvaluator(BinaryExpr expr, Properties namespaces) throws SAXPathException {
        Expr lhsExpr = expr.getLHS();
        Expr rhsExpr = expr.getRHS();

        if(lhsExpr instanceof NumberExpr || rhsExpr instanceof NumberExpr) {
            lhs = Value.getValue(lhsExpr, NUMBER_DECODER, namespaces);
            rhs = Value.getValue(rhsExpr, NUMBER_DECODER, namespaces);
        } else {
            lhs = Value.getValue(lhsExpr, STRING_DECODER, namespaces);
            rhs = Value.getValue(rhsExpr, STRING_DECODER, namespaces);
        }
        op = expr.getOperator();
    }

    public Value getLhs() {
        return lhs;
    }

    public Value getRhs() {
        return rhs;
    }

    public String toString() {
        return "(" + lhs + " " + op + " " +  rhs + ")";
    }

    private static class XPathNumberDecoder extends DoubleDecoder {
        public Object decode(String data) throws DataDecodeException {
            if(data.length() == 0) {
                // This will force the equals op to fail...
                return FailEquals.INSTANCE;
            } else {
                try {
                    return super.decode(data);
                } catch(DataDecodeException e) {
                    // This will force the equals op to fail...
                    return FailEquals.INSTANCE;
                }
            }
        }
    }

    static class FailEquals {
        static final FailEquals INSTANCE = new FailEquals();

        public boolean equals(Object obj) {
            return false;
        }
    }
}