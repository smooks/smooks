/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0 or,
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
package org.smooks.cdr.xpath.evaluators.value;

import org.jaxen.expr.*;
import org.jaxen.saxpath.Axis;
import org.jaxen.saxpath.SAXPathException;
import org.smooks.assertion.AssertArgument;
import org.smooks.cdr.xpath.evaluators.PredicatesEvaluatorBuilder;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.javabean.DataDecoder;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Properties;

/**
 * Element Value.
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public abstract class Value {

    public abstract Object getValue(SAXElement element);

    public abstract Object getValue(Element element);

    @SuppressWarnings("unchecked")
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
