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

import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.jaxen.expr.NameStep;
import org.jaxen.expr.Predicate;
import org.jaxen.expr.Step;
import org.jaxen.saxpath.Axis;
import org.jaxen.saxpath.SAXPathException;
import org.milyn.cdr.xpath.SelectorStep;
import org.milyn.xml.Namespace;

import java.util.List;
import java.util.Properties;

/**
 * {@link PredicatesEvaluator} builder.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class PredicatesEvaluatorBuilder {

    private Step step;
    private Step attributeStep;
    private SelectorStep selectorStep;
    private Properties namespaces;

    public PredicatesEvaluatorBuilder(Step step, Step attributeStep, SelectorStep selectorStep, Properties namespaces) {
        this.step = step;
        this.attributeStep = attributeStep;
        this.selectorStep = selectorStep;
        this.namespaces = namespaces;

        if(attributeStep != null && attributeStep.getAxis() != Axis.ATTRIBUTE) {
            throw new IllegalStateException("Unexpected 'attributeStep' arg '" + attributeStep.getText() + "'.  Must be an ATTRIBUTE Axis step.");
        }
    }

    public XPathExpressionEvaluator build() throws SAXPathException, NotFoundException, CannotCompileException, InstantiationException, IllegalAccessException {
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