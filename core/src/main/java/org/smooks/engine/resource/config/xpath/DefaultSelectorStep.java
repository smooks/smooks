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
package org.smooks.engine.resource.config.xpath;

import org.jaxen.expr.NameStep;
import org.jaxen.expr.Step;
import org.jaxen.saxpath.Axis;
import org.jaxen.saxpath.SAXPathException;
import org.smooks.api.SmooksException;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.config.xpath.SelectorStep;
import org.smooks.api.resource.config.xpath.XPathExpressionEvaluator;
import org.smooks.assertion.AssertArgument;
import org.smooks.engine.resource.config.xpath.evaluators.PassThruEvaluator;
import org.smooks.engine.resource.config.xpath.evaluators.PredicatesEvaluator;
import org.smooks.engine.resource.config.xpath.evaluators.PredicatesEvaluatorBuilder;
import org.smooks.engine.resource.config.xpath.evaluators.equality.AbstractEqualityEvaluator;
import org.smooks.engine.resource.config.xpath.evaluators.logical.AbstractLogicalEvaluator;
import org.smooks.engine.resource.config.xpath.evaluators.value.TextValue;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.Properties;

/**
 * XPath Expression Evaluator.
 * <p/>
 * Implementations evaluate a single step in an XPath expression.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
@SuppressWarnings("unused")
public class DefaultSelectorStep implements SelectorStep {
    public static final long NO_ELEMENT_INDEX = -1;

    private String xpathExpression;
    private Step step;
    private Step attributeStep;
    private boolean isRooted;
    private boolean isStar;
    private boolean isStarStar;
    private QName element;
    private QName attribute;
    private XPathExpressionEvaluator predicatesEvaluator;

    /**
     * Public constructor.
     * @param xpathExpression The XPath expression of which the {@link Step} is a
     * part.
     */
    public DefaultSelectorStep(String xpathExpression)
    {
        AssertArgument.isNotNull(xpathExpression, "xpathExpression");
        this.xpathExpression = xpathExpression;
        element = new QName(xpathExpression);
        initFlags();
    }

    /**
     * Public constructor.
     * @param xpathExpression The XPath expression of which the {@link Step} is a
     * part.
     * @param step The XPath {@link Step}.
     * @throws SAXPathException Error constructing the selector step.
     */
    public DefaultSelectorStep(String xpathExpression, Step step) throws SAXPathException {
        AssertArgument.isNotNull(xpathExpression, "xpathExpression");
        AssertArgument.isNotNull(step, "step");
        this.xpathExpression = xpathExpression;
        this.step = step;
        element = toQName(step, null);
        initFlags();
    }

    /**
     *
     * Public constructor.
     * <p/>
     * Allows asssociation of an attribute step.  XPath expressions can have the
     * form "xxx/@abc", where the attribute "abc" is on the element "xxx".
     *
     * @param xpathExpression The XPath expression of which the {@link Step} is a
     * part.
     * @param step The XPath {@link Step}.
     * @param attributeStep The Attribute Step.
     * @throws SAXPathException Error constructing the selector step.
     */
    public DefaultSelectorStep(String xpathExpression, Step step, Step attributeStep) throws SAXPathException {
        this(xpathExpression, step);
        AssertArgument.isNotNull(attributeStep, "attributeStep");

        if(attributeStep.getAxis() != Axis.ATTRIBUTE) {
            throw new IllegalArgumentException("Unexpected 'attributeStep' arg '" + attributeStep.getText() + "'.  Must be an ATTRIBUTE Axis step.");
        }

        setAttributeStep(attributeStep);
        initFlags();
    }

    /**
     * Public constructor.
     * @param xpathExpression The XPath expression of which the {@link Step} is a
     * part.
     * @param targetElementName The target element name associated with this selector step.
     */
    public DefaultSelectorStep(String xpathExpression, String targetElementName) {
        AssertArgument.isNotNull(xpathExpression, "xpathExpression");
        AssertArgument.isNotNull(targetElementName, "targetElementName");

        this.xpathExpression = xpathExpression;
        element = new QName(targetElementName);
        initFlags();
    }

    /**
     * Public constructor.
     * @param xpathExpression The XPath expression of which the {@link Step} is a
     * part.
     * @param elementName The target element name associated with this selector step.
     * @param attributeName The target attribute name associated with this selector step.
     */
    public DefaultSelectorStep(String xpathExpression, String elementName, String attributeName) {
        this(xpathExpression, elementName);
        AssertArgument.isNotNull(attributeName, "targetAttributeName");

        attribute = new QName(attributeName);
    }
    
    protected void setAttributeStep(Step attributeStep) {
        this.attributeStep = attributeStep;
        attribute = toQName(attributeStep, null);
    }

    @Override
    public DefaultSelectorStep copy() {
        DefaultSelectorStep clone = new DefaultSelectorStep(xpathExpression);

        clone.xpathExpression = xpathExpression;
        clone.step = step;
        clone.attributeStep = attributeStep;
        clone.isRooted = isRooted;
        clone.isStar = isStar;
        clone.isStarStar = isStarStar;
        clone.element = element;
        clone.attribute = attribute;
        clone.predicatesEvaluator = predicatesEvaluator;

        return clone;
    }

    /**
     * Initialize the step flags.
     */
    private void initFlags() {
        isStar = element.getLocalPart().equals("*");
        isStarStar = element.getLocalPart().equals("**");
        setRooted(element.getLocalPart().equals(ResourceConfig.DOCUMENT_FRAGMENT_SELECTOR));
    }

    /**
     * Get the XPath selector expression of which this SelectorStep instance is a part.
     * @return The XPath selector expression of which this SelectorStep instance is a part.
     */
    @Override
    public String getXPathExpression() {
        return xpathExpression;
    }

    @Override
    public QName getElement() {
        return element;
    }

    @Override
    public QName getAttribute() {
        return attribute;
    }

    @Override
    public XPathExpressionEvaluator getPredicatesEvaluator() {
        return predicatesEvaluator;
    }

    @Override
    public boolean isRooted() {
        return isRooted;
    }

    @Override
    public void setRooted(boolean rooted) {
        isRooted = rooted;

        // Can't be rooted and **...
        if(isStarStar) {
            isRooted = false;
        }
    }

    @Override
    public boolean isStar() {
        return isStar;
    }

    @Override
    public boolean isStarStar() {
        return isStarStar;
    }

    @Override
    public void buildPredicatesEvaluator(Properties namespaces) {
        AssertArgument.isNotNull(namespaces, "namespaces");
        if(predicatesEvaluator != null) {
            return;
        }

        if(step != null) {
            PredicatesEvaluatorBuilder builder = new PredicatesEvaluatorBuilder(step, attributeStep, this, namespaces);
            try {
                predicatesEvaluator = builder.build();
            } catch (SAXPathException e) {
                throw new SmooksException("Error processing XPath selector expression '" + xpathExpression + "'.", e);
            }

            // And update the QNames again now that we have the namespaces...
            element = toQName(step, builder);
            if(attributeStep != null) {
                attribute = toQName(attributeStep, builder);
            }
        } else {
            predicatesEvaluator = PassThruEvaluator.INSTANCE;
        }
    }
    
    /**
     * Is this StepSelector instance targeted at the specified namespace.
     * <p/>
     * If the StepSelector namespace is null, it automatically matches and no
     * comparison is made against the supplied namespace.
     *
     * @param namespace The namespace to be tested.
     * @return True if the target namespace matches (or is null), otherwise false.
     */
    @Override
    public boolean isTargetedAtNamespace(String namespace) {
        String targetNS = element.getNamespaceURI();

        // If the target NS is null, we match, no matter what the specified namespace...
        if(targetNS == null || targetNS.equals(XMLConstants.NULL_NS_URI)) {
            return true;
        }

        return targetNS.equals(namespace);
    }

    /**
     * Does this step require access to the element text content.
     * <p/>
     * Does this step include a 'text()' predicate at any level.
     *
     * @return True if the step requires access to the element's text content, otherwise false.
     */
    @Override
    public boolean accessesText() {
        XPathExpressionEvaluator evaluator = getPredicatesEvaluator();

        if(evaluator == null) {
            return false;
        }

        return accessesText(evaluator);
    }

    /**
     * Does the supplied {@link XPathExpressionEvaluator} access the element text content.
     * @return True if the supplied {@link XPathExpressionEvaluator} accesses the element text content,
     * otherwise false.
     */
    private boolean accessesText(XPathExpressionEvaluator evaluator) {
        if (evaluator instanceof AbstractEqualityEvaluator) {
            if (((AbstractEqualityEvaluator) evaluator).getLhs() instanceof TextValue) {
                return true;
            } else {
                return ((AbstractEqualityEvaluator) evaluator).getRhs() instanceof TextValue;
            }
        } else if (evaluator instanceof AbstractLogicalEvaluator) {
            if (accessesText(((AbstractLogicalEvaluator) evaluator).getLhs())) {
                return true;
            } else {
                return accessesText(((AbstractLogicalEvaluator) evaluator).getRhs());
            }
        } else if (evaluator instanceof PredicatesEvaluator) {
            List<XPathExpressionEvaluator> evaluators = ((PredicatesEvaluator) evaluator).getEvaluators();
            for (XPathExpressionEvaluator pEvaluator : evaluators) {
                if (accessesText(pEvaluator)) {
                    return true;
                }
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends XPathExpressionEvaluator> void getEvaluators(Class<T> evaluatorClass, List<T> evaluators) {
        getEvaluators(getPredicatesEvaluator(), evaluatorClass, (List<XPathExpressionEvaluator>) evaluators);
    }

    private <T extends XPathExpressionEvaluator> void getEvaluators(XPathExpressionEvaluator evaluator, Class<T> evaluatorClass, List<XPathExpressionEvaluator> evaluators) {
        if(evaluator.getClass() == evaluatorClass) {
            evaluators.add(evaluator);
        }

        if(evaluator instanceof AbstractLogicalEvaluator) {
            getEvaluators(((AbstractLogicalEvaluator)evaluator).getLhs(), evaluatorClass, evaluators);
            getEvaluators(((AbstractLogicalEvaluator)evaluator).getRhs(), evaluatorClass, evaluators);
        } else if(evaluator instanceof PredicatesEvaluator) {
            List<XPathExpressionEvaluator> subEvaluators = ((PredicatesEvaluator) evaluator).getEvaluators();
            for(XPathExpressionEvaluator pEvaluator : subEvaluators) {
                getEvaluators(pEvaluator, evaluatorClass, evaluators);
            }
        }
    }

    private QName toQName(Step step, PredicatesEvaluatorBuilder evaluatorCompiler) throws SmooksException {
        String nsPrefix = ((NameStep) step).getPrefix();
        String localPart = ((NameStep) step).getLocalName();

        if(nsPrefix != null && !nsPrefix.trim().equals("")) {
            if(evaluatorCompiler != null) {
                try {
                    return new QName(evaluatorCompiler.getNamespace(nsPrefix), localPart, nsPrefix);
                } catch (SAXPathException e) {
                    throw new SmooksException(e);
                }
            } else {
                // Will need to update the namespace later... when we have the
                // namespace prefix-to-uri mappings...
                return new QName(null, localPart, nsPrefix);
            }
        } else {
            return new QName(localPart);
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(getElement());
        if(attribute != null) {
            stringBuilder.append("{@").append(attribute).append("}");
        }

        XPathExpressionEvaluator evaluator = getPredicatesEvaluator();
        if(evaluator != null) {
            stringBuilder.append(evaluator);
        }

        return stringBuilder.toString();
    }
}
