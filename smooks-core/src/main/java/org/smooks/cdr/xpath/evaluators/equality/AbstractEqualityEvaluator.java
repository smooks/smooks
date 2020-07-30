/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.cdr.xpath.evaluators.equality;

import org.jaxen.expr.BinaryExpr;
import org.jaxen.expr.Expr;
import org.jaxen.expr.NumberExpr;
import org.jaxen.saxpath.SAXPathException;
import org.smooks.cdr.xpath.evaluators.XPathExpressionEvaluator;
import org.smooks.cdr.xpath.evaluators.value.Value;
import org.smooks.converter.TypeConverter;
import org.smooks.converter.TypeConverterException;
import org.smooks.converter.factory.TypeConverterFactory;
import org.smooks.converter.factory.system.StringConverterFactory;
import org.smooks.converter.factory.system.StringToDoubleConverterFactory;

import java.util.Properties;

/**
 * Simple equality predicate evaluator.
 * <p/>
 * Works for element text or attributes. Covers Equality and Relational XPath expressions.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public abstract class AbstractEqualityEvaluator extends XPathExpressionEvaluator {

    private static final TypeConverter<String, String> STRING_CONVERTER_FACTORY = new StringConverterFactory().createTypeConverter();
    private static final TypeConverter<String, Object> NUMBER_CONVERTER_FACTORY = new XPathNumberConverterFactory().createTypeConverter();

    protected Value lhs;
    private String op;
    protected Value rhs;

    public AbstractEqualityEvaluator(BinaryExpr expr, Properties namespaces) throws SAXPathException {
        Expr lhsExpr = expr.getLHS();
        Expr rhsExpr = expr.getRHS();

        if(lhsExpr instanceof NumberExpr || rhsExpr instanceof NumberExpr) {
            lhs = Value.getValue(lhsExpr, NUMBER_CONVERTER_FACTORY, namespaces);
            rhs = Value.getValue(rhsExpr, NUMBER_CONVERTER_FACTORY, namespaces);
        } else {
            lhs = Value.getValue(lhsExpr, STRING_CONVERTER_FACTORY, namespaces);
            rhs = Value.getValue(rhsExpr, STRING_CONVERTER_FACTORY, namespaces);
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

    private static class XPathNumberConverterFactory implements TypeConverterFactory<String, Object> {
        @Override
        public TypeConverter<String, Object> createTypeConverter() {
            return value -> {
                if (value.length() == 0) {
                    // This will force the equals op to fail...
                    return FailEquals.INSTANCE;
                } else {
                    try {
                        return new StringToDoubleConverterFactory().createTypeConverter().convert(value);
                    } catch (TypeConverterException e) {
                        // This will force the equals op to fail...
                        return FailEquals.INSTANCE;
                    }
                }
            };
        }
    }

    static class FailEquals {
        static final FailEquals INSTANCE = new FailEquals();

        public boolean equals(Object obj) {
            return false;
        }
    }
}
