/*-
 * ========================LICENSE_START=================================
 * Core
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
package org.smooks.engine.resource.config.xpath.evaluators;

import org.jaxen.expr.*;
import org.jaxen.saxpath.SAXPathException;
import org.smooks.api.resource.config.xpath.SelectorStep;
import org.smooks.api.resource.config.xpath.XPathExpressionEvaluator;
import org.smooks.assertion.AssertArgument;
import org.smooks.engine.resource.config.xpath.evaluators.equality.*;
import org.smooks.engine.resource.config.xpath.evaluators.logical.AndEvaluator;
import org.smooks.engine.resource.config.xpath.evaluators.logical.OrEvaluator;

import java.util.Properties;

public class XPathExpressionEvaluatorFactory {

    /**
     * {@link XPathExpressionEvaluatorFactory} factory method.
     * @param expr Jaxen XPath expression.
     * @param selectorStep Selector Step.
     * @param namespaces Namespace set.
     * @return The {@link XPathExpressionEvaluatorFactory} for the Jaxen expression.
     */
    public XPathExpressionEvaluator create(Expr expr, SelectorStep selectorStep, Properties namespaces) throws SAXPathException {
        AssertArgument.isNotNull(expr, "expr");

        if (expr instanceof LogicalExpr) {
            LogicalExpr logicalExpr = (LogicalExpr) expr;
            if (logicalExpr.getOperator().equalsIgnoreCase("and")) {
                return new AndEvaluator(logicalExpr, selectorStep, namespaces);
            } else if (logicalExpr.getOperator().equalsIgnoreCase("or")) {
                return new OrEvaluator(logicalExpr, selectorStep, namespaces);
            }
        } else if (expr instanceof EqualityExpr) {
            EqualityExpr equalityExpr = (EqualityExpr) expr;
            if (equalityExpr.getOperator().equalsIgnoreCase("=")) {
                return new EqualsEvaluator(equalityExpr, namespaces);
            } else if (equalityExpr.getOperator().equalsIgnoreCase("!=")) {
                return new NotEqualsEvaluator(equalityExpr, namespaces);
            }
        } else if (expr instanceof RelationalExpr) {
            RelationalExpr relationalExpr = (RelationalExpr) expr;
            if (relationalExpr.getOperator().equalsIgnoreCase("<")) {
                return new LessThanEvaluator(relationalExpr, namespaces);
            } else if (relationalExpr.getOperator().equalsIgnoreCase(">")) {
                return new GreaterThanEvaluator(relationalExpr, namespaces);
            }
        } else if (expr instanceof NumberExpr) {
            return new PositionEvaluator(((NumberExpr) expr).getNumber().intValue(), selectorStep);
        }

        throw new SAXPathException("Unsupported XPath expr token '" + expr.getText() + "'.");
    }
}
