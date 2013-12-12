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

import org.jaxen.expr.LiteralExpr;
import org.jaxen.expr.NumberExpr;
import org.milyn.delivery.sax.SAXElement;
import org.w3c.dom.Element;

/**
 * Absolute value getter.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class AbsoluteValue extends Value {

    private Object value;

    public AbsoluteValue(LiteralExpr literal) {
        value = literal.getLiteral();
    }

    public AbsoluteValue(NumberExpr number) {
        value = number.getNumber();
    }

    public Object getValue(SAXElement element) {
        return value;
    }

    public Object getValue(Element element) {
        return value;
    }

    public String toString() {
        if (value instanceof String) {
            return "'" + value + "'";
        } else {
            return value.toString();
        }
    }
}