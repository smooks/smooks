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
package org.smooks.cdr.xpath.evaluators;

import org.jaxen.expr.NameStep;
import org.jaxen.expr.Predicate;
import org.jaxen.expr.Step;
import org.jaxen.saxpath.Axis;
import org.jaxen.saxpath.SAXPathException;
import org.smooks.cdr.xpath.SelectorStep;
import org.smooks.xml.Namespace;

import java.util.List;
import java.util.Properties;

/**
 * {@link PredicatesEvaluator} builder.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class PredicatesEvaluatorBuilder {

    private final Step step;
    private final Step attributeStep;
    private final SelectorStep selectorStep;
    private final Properties namespaces;

    public PredicatesEvaluatorBuilder(Step step, Step attributeStep, SelectorStep selectorStep, Properties namespaces) {
        this.step = step;
        this.attributeStep = attributeStep;
        this.selectorStep = selectorStep;
        this.namespaces = namespaces;

        if(attributeStep != null && attributeStep.getAxis() != Axis.ATTRIBUTE) {
            throw new IllegalStateException("Unexpected 'attributeStep' arg '" + attributeStep.getText() + "'.  Must be an ATTRIBUTE Axis step.");
        }
    }

    public XPathExpressionEvaluator build() throws SAXPathException
    {
        PredicatesEvaluator evaluator = new PredicatesEvaluator();

        if (!(step instanceof NameStep)) {
            throw new SAXPathException("Unsupported step '" + step.getText() + "'.");
        }

        addEvaluators(step, evaluator);

        // Add the evaluators for the attribute step...
        if(attributeStep != null) {
            addEvaluators(attributeStep, evaluator);
        }

        return evaluator;
    }

    @SuppressWarnings("unchecked")
    private void addEvaluators(Step step, PredicatesEvaluator evaluator) throws SAXPathException {
        List<Predicate> predicates = step.getPredicates();
        for (Predicate predicate : predicates) {
            XPathExpressionEvaluator predicateEvaluator = XPathExpressionEvaluator.getInstance(predicate.getExpr(), selectorStep, namespaces);
            evaluator.addEvaluator(predicateEvaluator);
        }
    }

    public String getNamespace(String nsPrefix) throws SAXPathException {
        return getNamespace(nsPrefix, namespaces);
    }

    public static String getNamespace(String nsPrefix, Properties namespaces) throws SAXPathException {
        String namespace = namespaces.getProperty(nsPrefix);

        if(namespace == null) {
            namespace = Namespace.SMOOKS_PREFIX_MAPPINGS.getProperty(nsPrefix);
            if(namespace == null) {
                throw new SAXPathException("Unknown namespace prefix '" + nsPrefix + "'.  You must define the namespace prefix-to-uri mappings in the Smooks <core:namespaces> configuration section.");
            }
        }
        return namespace;
    }
}
