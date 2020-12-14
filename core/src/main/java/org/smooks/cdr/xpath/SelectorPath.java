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
package org.smooks.cdr.xpath;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.jaxen.saxpath.SAXPathException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.SmooksException;
import org.smooks.assertion.AssertArgument;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.cdr.ResourceConfig;
import org.smooks.cdr.xpath.evaluators.PassThruEvaluator;
import org.smooks.cdr.xpath.evaluators.XPathExpressionEvaluator;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.Filter;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.expression.ExecutionContextExpressionEvaluator;
import org.smooks.expression.ExpressionEvaluator;
import org.smooks.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.util.*;

public class SelectorPath implements List<SelectorStep> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectorPath.class);

    private Properties namespaces = new Properties();
    
    /**
     * Document target on which the resource is to be applied.
     */
    private String selector;

    /**
     * The XML namespace of the tag to which this config
     * should only be applied.
     */
    @Deprecated
    private String namespaceURI;

    /**
     * Condition evaluator used in resource targeting.
     */
    private ExpressionEvaluator expressionEvaluator;

    private List<SelectorStep> selectorSteps = new ArrayList<>();

    /**
     * Get the selector definition for this ResourceConfig.
     *
     * @return The selector definition.
     */
    public String getSelector() {
        return selector;
    }

    /**
     * Set the config selector.
     *
     * @param selector The selector definition.
     */
    public void setSelector(final String selector) {
        AssertArgument.isNotEmpty(selector, "selector");
        this.selector = selector;

        // If there's a "#document" token in the selector, but it's not at the very start,
        // then we have an invalid selector...
        int docSelectorIndex = selector.trim().indexOf(ResourceConfig.DOCUMENT_FRAGMENT_SELECTOR);
        if (docSelectorIndex > 0) {
            throw new SmooksConfigurationException("Invalid selector '" + selector + "'.  '" + ResourceConfig.DOCUMENT_FRAGMENT_SELECTOR + "' token can only exist at the start of the selector.");
        }

        selectorSteps.clear();
        try {
            selectorSteps.addAll(SelectorStepBuilder.buildSteps(selector));
        } catch (SAXPathException e) {
            selectorSteps.addAll(constructSelectorStepsFromLegacySelector(selector));
        }
    }

    private SelectorPath constructSelectorStepsFromLegacySelector(String selector) {
        // In case it's a legacy selector that we don't support...

        if (selector.startsWith("/")) {
            selector = ResourceConfig.DOCUMENT_FRAGMENT_SELECTOR + selector;
        }

        String[] contextualSelector = parseSelector(selector);

        SelectorPath selectorPath = new SelectorPath();
        for (int i = 0; i < contextualSelector.length; i++) {
            String targetElementName = contextualSelector[i];

            if (i == contextualSelector.length - 2 && contextualSelector[contextualSelector.length - 1].startsWith("@")) {
                selectorPath.add(new LegacySelectorStep(selector, targetElementName, contextualSelector[contextualSelector.length - 1]));
                break;
            } else {
                selectorPath.add(new LegacySelectorStep(selector, targetElementName));
            }
        }

        LOGGER.debug("Unable to parse selector '" + selector + "' as an XPath selector (even after normalization).  Parsing as a legacy style selector.");

        return selectorPath;
    }

    private class LegacySelectorStep extends SelectorStep {
        public LegacySelectorStep(String selector, String targetElementName) {
            super(selector, targetElementName);
        }

        public LegacySelectorStep(String xpathExpression, String targetElementName, String targetAttributeName) {
            super(xpathExpression, targetElementName, targetAttributeName);
        }

        public XPathExpressionEvaluator getPredicatesEvaluator() {
            return PassThruEvaluator.INSTANCE;
        }

        @SuppressWarnings("RedundantThrows")
        public void buildPredicatesEvaluator(Properties namespaces) throws SAXPathException, NotFoundException, CannotCompileException, IllegalAccessException, InstantiationException {
            // Ignore this.
        }
    }

    public String[] parseSelector(String selector) {
        String[] splitTokens;

        if (selector.startsWith("/")) {
            selector = selector.substring(1);
        }

        // Parse the selector in case it's a contextual selector...
        if (selector.indexOf('/') != -1) {
            // Parse it as e.g. "a/b/c" ...
            splitTokens = selector.split("/");
        } else {
            // Parse it as a CSS form selector e.g. "TD UL LI" ...
            splitTokens = selector.split(" +");
        }

        for (int i = 0; i < splitTokens.length; i++) {
            String splitToken = splitTokens[i];

            if (!splitToken.startsWith("@")) {
                splitTokens[i] = splitToken;
            }
        }

        return splitTokens;
    }

    /**
     * Get the targeting selector step.
     *
     * @return The targeting selector step.
     */
    public SelectorStep getTargetSelectorStep() {
        return selectorSteps.get(selectorSteps.size() - 1);
    }

    /**
     * Get the name of the target element where the {@link #getSelector() selector}
     * is targeting the resource at an XML element.
     * <p/>
     * Accomodates the fact that element based selectors can be contextual. This method
     * is not relevant where the selector is not targeting an XML element.
     * <p/>
     * See details about the "selector" attribute in the
     * <a href="#attribdefs">Attribute Definitions</a> section.
     *
     * @return The target XML element name.
     */
    public String getTargetElement() {
        return getTargetSelectorStep().getElement().getLocalPart();
    }

    /**
     * Get the name of the attribute specified on the selector, if one was
     * specified.
     *
     * @return An attribute name, if one was specified on the selector, otherwise null.
     */
    public String getTargetAttribute() {
        QName targetAttribute = getTargetSelectorStep().getAttribute();
        if (targetAttribute == null) {
            return null;
        }
        return targetAttribute.getLocalPart();
    }

    /**
     * The the selector namespace URI.
     *
     * @return The XML namespace URI of the element to which this configuration
     * applies, or null if not namespaced.
     */
    @Deprecated
    public String getSelectorNamespaceURI() {
        return namespaceURI;
    }

    /**
     * Is this resource configuration targets at the same namespace as the
     * specified elemnt.
     *
     * @param namespace The element to check against.
     * @return True if this resource config is targeted at the element namespace,
     * or if the resource is not targeted at any namespace (i.e. not specified),
     * otherwise false.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @Deprecated
    public boolean isTargetedAtNamespace(String namespace) {
        if (namespaceURI != null) {
            return namespaceURI.equals(namespace);
        }

        return true;
    }

    /**
     * Set the condition evaluator to be used in targeting of this resource.
     *
     * @param expressionEvaluator The {@link org.smooks.expression.ExpressionEvaluator}, or null if no condition is to be used.
     */
    public void setConditionEvaluator(ExpressionEvaluator expressionEvaluator) {
        if (expressionEvaluator != null && !(expressionEvaluator instanceof ExecutionContextExpressionEvaluator)) {
            throw new UnsupportedOperationException("Unsupported ExpressionEvaluator type '" + expressionEvaluator.getClass().getName() + "'.  Currently only support '" + ExecutionContextExpressionEvaluator.class.getName() + "' implementations.");
        }
        this.expressionEvaluator = expressionEvaluator;
    }

    /**
     * Get the condition evaluator used in targeting of this resource.
     *
     * @return The {@link org.smooks.expression.ExpressionEvaluator}, or null if no condition is specified.
     */
    public ExpressionEvaluator getConditionEvaluator() {
        return expressionEvaluator;
    }
    
    /**
     * Is this resource configuration targeted at the specified DOM element
     * in context.
     * <p/>
     * See details about the "selector" attribute in the
     * <a href="#attribdefs">Attribute Definitions</a> section.
     * <p/>
     * Note this doesn't perform any namespace checking.
     *
     * @param element          The element to check against.
     * @param executionContext The current execution context.
     * @return True if this resource configuration is targeted at the specified
     * element in context, otherwise false.
     */
    private boolean isTargetedAtElementContext(Element element, ExecutionContext executionContext) {
        Node currentNode = element;
        ContextIndex index = new ContextIndex(executionContext);

        index.i = selectorSteps.size() - 1;

        // Unless it's **, start at the parent because the current element
        // has already been tested...
        if (!selectorSteps.get(index.i).isStarStar()) {
            index.i = selectorSteps.size() - 2;
            currentNode = element.getParentNode();
        } else {
            // The target selector step is "**".  If the parent one is "#document" and we're at
            // the root now, then fail...
            if (selectorSteps.size() == 2 && selectorSteps.get(0).isRooted() && element.getParentNode() == null) {
                return false;
            }
        }

        if (currentNode == null || currentNode.getNodeType() != Node.ELEMENT_NODE) {
            return false;
        }

        // Check the element name(s).
        while (index.i >= 0) {
            Element currentElement = (Element) currentNode;
            Node parentNode;

            parentNode = currentElement.getParentNode();
            if (parentNode == null || parentNode.getNodeType() != Node.ELEMENT_NODE) {
                parentNode = null;
            }

            if (!isTargetedAtElementContext(currentElement, (Element) parentNode, index)) {
                return false;
            }

            if (parentNode == null) {
                return true;
            }

            currentNode = parentNode;
        }

        return true;
    }

    /**
     * Is this resource configuration targeted at the specified SAX element
     * in context.
     * <p/>
     * See details about the "selector" attribute in the
     * <a href="#attribdefs">Attribute Definitions</a> section.
     * <p/>
     * Note this doesn't perform any namespace checking.
     *
     * @param element          The element to check against.
     * @param executionContext The current execution context.
     * @return True if this resource configuration is targeted at the specified
     * element in context, otherwise false.
     */
    private boolean isTargetedAtElementContext(SAXElement element, ExecutionContext executionContext) {
        SAXElement currentElement = element;
        ContextIndex index = new ContextIndex(executionContext);

        index.i = selectorSteps.size() - 1;

        // Unless it's **, start at the parent because the current element
        // has already been tested...
        if (!selectorSteps.get(index.i).isStarStar()) {
            index.i = selectorSteps.size() - 2;
            currentElement = element.getParent();
        } else {
            // The target selector step is "**".  If the parent one is "#document" and we're at
            // the root now, then fail...
            if (selectorSteps.size() == 2 && selectorSteps.get(0).isRooted() && element.getParent() == null) {
                return false;
            }
        }

        if (currentElement == null) {
            return false;
        }

        // Check the element name(s).
        while (index.i >= 0) {
            SAXElement parentElement = currentElement.getParent();

            if (!isTargetedAtElementContext(currentElement, parentElement, index)) {
                return false;
            }

            if (parentElement == null) {
                return true;
            }

            currentElement = parentElement;
        }

        return true;
    }

    private boolean isTargetedAtElementContext(Element element, Element parentElement, ContextIndex index) {
        if (selectorSteps.get(index.i).isRooted() && parentElement != null) {
            return false;
        } else if (selectorSteps.get(index.i).isStar()) {
            index.i--;
        } else if (selectorSteps.get(index.i).isStarStar()) {
            if (index.i == 0) {
                // No more tokens to match and ** matches everything
                return true;
            } else if (index.i == 1) {
                SelectorStep parentStep = selectorSteps.get(index.i - 1);

                if (parentElement == null && parentStep.isRooted()) {
                    // we're at the root of the document and the only selector left is
                    // the document selector.  Pass..
                    return true;
                } else if (parentElement == null) {
                    // we're at the root of the document, yet there are still
                    // unmatched tokens in the selector.  Fail...
                    return false;
                }
            } else if (parentElement == null) {
                // we're at the root of the document, yet there are still
                // unmatched tokens in the selector.  Fail...
                return false;
            }

            SelectorStep parentStep = selectorSteps.get(index.i - 1);

            if (parentStep.isTargetedAtElement(parentElement)) {
                if (!parentStep.isStarStar()) {
                    XPathExpressionEvaluator evaluator = parentStep.getPredicatesEvaluator();
                    if (evaluator == null) {
                        LOGGER.debug("Predicate Evaluators for resource [" + this + "] is null.  XPath step predicates will not be evaluated.");
                    } else if (!evaluator.evaluate(parentElement, index.executionContext)) {
                        return false;
                    }
                }
                index.i--;
            }
        } else if (!selectorSteps.get(index.i).isTargetedAtElement(element)) {
            return false;
        } else {
            if (!selectorSteps.get(index.i).isStarStar()) {
                XPathExpressionEvaluator evaluator = selectorSteps.get(index.i).getPredicatesEvaluator();
                if (evaluator == null) {
                    LOGGER.debug("Predicate Evaluators for resource [" + this + "] is null.  XPath step predicates will not be evaluated.");
                } else if (!evaluator.evaluate(element, index.executionContext)) {
                    return false;
                }
            }
            index.i--;
        }

        if (parentElement == null) {
            if (index.i >= 0 && !selectorSteps.get(index.i).isStarStar()) {
                return selectorSteps.get(index.i).isRooted();
            }
        }

        return true;
    }

    private boolean isTargetedAtElementContext(SAXElement element, SAXElement parentElement, ContextIndex index) {
        if (selectorSteps.get(index.i).isRooted() && parentElement != null) {
            return false;
        } else if (selectorSteps.get(index.i).isStar()) {
            index.i--;
        } else if (selectorSteps.get(index.i).isStarStar()) {
            if (index.i == 0) {
                // No more tokens to match and ** matches everything
                return true;
            } else if (index.i == 1) {
                SelectorStep parentStep = selectorSteps.get(index.i - 1);

                if (parentElement == null && parentStep.isRooted()) {
                    // we're at the root of the document and the only selector left is
                    // the document selector.  Pass..
                    return true;
                } else if (parentElement == null) {
                    // we're at the root of the document, yet there are still
                    // unmatched tokens in the selector.  Fail...
                    return false;
                }
            } else if (parentElement == null) {
                // we're at the root of the document, yet there are still
                // unmatched tokens in the selector.  Fail...
                return false;
            }

            SelectorStep parentStep = selectorSteps.get(index.i - 1);

            if (parentStep.isTargetedAtElement(parentElement)) {
                if (!parentStep.isStarStar()) {
                    XPathExpressionEvaluator evaluator = parentStep.getPredicatesEvaluator();
                    if (evaluator == null) {
                        LOGGER.debug("Predicate Evaluators for resource [" + this + "] is null.  XPath step predicates will not be evaluated.");
                    } else if (!evaluator.evaluate(parentElement, index.executionContext)) {
                        return false;
                    }
                }
                index.i--;
            }
        } else if (!selectorSteps.get(index.i).isTargetedAtElement(element)) {
            return false;
        } else {
            if (!selectorSteps.get(index.i).isStarStar()) {
                XPathExpressionEvaluator evaluator = selectorSteps.get(index.i).getPredicatesEvaluator();
                if (evaluator == null) {
                    LOGGER.debug("Predicate Evaluators for resource [" + this + "] is null.  XPath step predicates will not be evaluated.");
                } else if (!evaluator.evaluate(element, index.executionContext)) {
                    return false;
                }
            }
            index.i--;
        }

        if (parentElement == null) {
            if (index.i >= 0 && !selectorSteps.get(index.i).isStarStar()) {
                return selectorSteps.get(index.i).isRooted();
            }
        }

        return true;
    }

    /**
     * Is this configuration targeted at the supplied DOM element.
     * <p/>
     * Checks that the element is in the correct namespace and is a contextual
     * match for the configuration.
     *
     * @param element          The element to be checked.
     * @param executionContext The current execution context.
     * @return True if this configuration is targeted at the supplied element, otherwise false.
     */
    public boolean isTargetedAtElement(Element element, ExecutionContext executionContext) {
        if (!assertConditionTrue(executionContext)) {
            return false;
        }

        if (namespaceURI != null) {
            if (!isTargetedAtNamespace(element.getNamespaceURI())) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Not applying resource [" + this + "] to element [" + DomUtils.getXPath(element) + "].  Element not in namespace [" + getSelectorNamespaceURI() + "].");
                }
                return false;
            }
        } else {
            // We don't test the SelectorStep namespace if a namespace is configured on the
            // resource configuration.  This is why we have this code inside the else block.
            if (!getTargetSelectorStep().isTargetedAtNamespace(element.getNamespaceURI())) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Not applying resource [" + this + "] to element [" + DomUtils.getXPath(element) + "].  Element not in namespace [" + getTargetSelectorStep().getElement().getNamespaceURI() + "].");
                }
                return false;
            }
        }

        XPathExpressionEvaluator evaluator = getTargetSelectorStep().getPredicatesEvaluator();
        if (evaluator == null) {
            LOGGER.debug("Predicate Evaluators for resource [" + this + "] is null.  XPath step predicates will not be evaluated.");
        } else if (!evaluator.evaluate(element, executionContext)) {
            return false;
        }

        if (selectorSteps.size() > 1 && !isTargetedAtElementContext(element, executionContext)) {
            // Note: If the selector is not contextual, there's no need to perform the
            // isTargetedAtElementContext check because we already know the unit is targeted at the
            // element by name - because we looked it up by name in the 1st place (at least that's the assumption).
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Not applying resource [" + this + "] to element [" + DomUtils.getXPath(element) + "].  This resource is only targeted at '" + DomUtils.getName(element) + "' when in the following context '" + getSelector() + "'.");
            }
            return false;
        }

        return true;
    }

    /**
     * Is this configuration targeted at the supplied SAX element.
     * <p/>
     * Checks that the element is in the correct namespace and is a contextual
     * match for the configuration.
     *
     * @param element          The element to be checked.
     * @param executionContext The current execution context.
     * @return True if this configuration is targeted at the supplied element, otherwise false.
     */
    public boolean isTargetedAtElement(SAXElement element, ExecutionContext executionContext) {
        if (expressionEvaluator != null && !assertConditionTrue(executionContext)) {
            return false;
        }

        if (namespaceURI != null) {
            if (!isTargetedAtNamespace(element.getName().getNamespaceURI())) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Not applying resource [" + this + "] to element [" + element.getName() + "].  Element not in namespace [" + namespaceURI + "].");
                }
                return false;
            }
        } else {
            // We don't test the SelectorStep namespace if a namespace is configured on the
            // resource configuration.  This is why we have this code inside the else block.
            if (!getTargetSelectorStep().isTargetedAtNamespace(element.getName().getNamespaceURI())) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Not applying resource [" + this + "] to element [" + element.getName() + "].  Element not in namespace [" + getTargetSelectorStep().getElement().getNamespaceURI() + "].");
                }
                return false;
            }
        }

        XPathExpressionEvaluator evaluator = getTargetSelectorStep().getPredicatesEvaluator();
        if (evaluator == null) {
            LOGGER.debug("Predicate Evaluators for resource [" + this + "] is null.  XPath step predicates will not be evaluated.");
        } else if (!evaluator.evaluate(element, executionContext)) {
            return false;
        }

        if (selectorSteps.size() > 1 && !isTargetedAtElementContext(element, executionContext)) {
            // Note: If the selector is not contextual, there's no need to perform the
            // isTargetedAtElementContext check because we already know the visitor is targeted at the
            // element by name - because we looked it up by name in the 1st place (at least that's the assumption).
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Not applying resource [" + this + "] to element [" + element.getName() + "].  This resource is only targeted at '" + element.getName().getLocalPart() + "' when in the following context '" + getSelector() + "'.");
            }
            return false;
        }

        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean assertConditionTrue(ExecutionContext executionContext) {
        if (expressionEvaluator == null) {
            return true;
        }
        
        return ((ExecutionContextExpressionEvaluator) expressionEvaluator).eval(executionContext);
    }

    /**
     * Set the namespace URI to which the selector is associated.
     *
     * @param namespaceURI Selector namespace.
     */
    @Deprecated
    public void setSelectorNamespaceURI(String namespaceURI) {
        if (namespaceURI != null) {
            if (namespaceURI.equals("*")) {
                this.namespaceURI = null;
            } else {
                this.namespaceURI = namespaceURI.intern();
            }
        }
    }

    public Properties getNamespaces() {
        return namespaces;
    }

    @Override
    public SelectorPath clone() {
        SelectorPath copySelectorPath = new SelectorPath();

        copySelectorPath.selector = selector;
        copySelectorPath.namespaceURI = namespaceURI;
        copySelectorPath.expressionEvaluator = expressionEvaluator;
        copySelectorPath.namespaces = namespaces;
        for (SelectorStep selectorStep : selectorSteps) {
            copySelectorPath.selectorSteps.add(selectorStep.clone());
        }
        
        return copySelectorPath;
    }

    /**
     * Set the namespaces on the specified set of selector steps.
     * @param namespaces The set of selector steps to be updated.
     * @return The set of selector steps (as passed in the 'steps' argument).
     * @throws org.jaxen.saxpath.SAXPathException Error setting namespaces
     */
    public void setNamespaces(Properties namespaces) {
        AssertArgument.isNotNull(namespaces, "namespaces");

        this.namespaces.putAll(namespaces);
        
        for(int i = 0; i < selectorSteps.size(); i++) {
            SelectorStep step = selectorSteps.get(i);
            try {
                step.buildPredicatesEvaluator(this.namespaces);
            } catch (SAXPathException e) {
                throw new SmooksException("Error configuring resource selector", e);
            } catch (Exception e) {
                throw new SmooksException("Error compiling PredicatesEvaluator.", e);
            }

            if(i < selectorSteps.size() - 1 && step.accessesText()) {
                throw new SmooksException("Unsupported XPath selector expression '" + step.getXPathExpression() + "'.  XPath 'text()' tokens are only supported in the last step.");
            }
        }
    }

    /**
     * Create a print friendly representation of the set of selector steps.
     * @return A print friendly representation of the set of selector steps.
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        for(int i = 0; i < size(); i++) {
            if(get(i).isRooted()) {
                stringBuilder.append("/");
            } else if(i > 0) {
                stringBuilder.append("/");
            }

            stringBuilder.append(get(i));
        }

        return stringBuilder.toString();
    }
    
    @Override
    public int size() {
        return selectorSteps.size();
    }

    @Override
    public boolean isEmpty() {
        return selectorSteps.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return selectorSteps.contains(o);
    }

    @Override
    public Iterator<SelectorStep> iterator() {
        return selectorSteps.iterator();
    }

    @Override
    public Object[] toArray() {
        return selectorSteps.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return selectorSteps.toArray(a);
    }

    @Override
    public boolean add(SelectorStep selectorStep) {
        return selectorSteps.add(selectorStep);
    }

    @Override
    public boolean remove(Object o) {
        return selectorSteps.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return selectorSteps.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends SelectorStep> c) {
        return selectorSteps.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends SelectorStep> c) {
        return selectorSteps.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return selectorSteps.remove(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return selectorSteps.retainAll(c);
    }

    @Override
    public void clear() {
        selectorSteps.clear();
    }

    @Override
    public SelectorStep get(int index) {
        return selectorSteps.get(index);
    }

    @Override
    public SelectorStep set(int index, SelectorStep element) {
        return selectorSteps.set(index, element);
    }

    @Override
    public void add(int index, SelectorStep element) {
        selectorSteps.add(index, element);
    }

    @Override
    public SelectorStep remove(int index) {
        return selectorSteps.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return selectorSteps.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return selectorSteps.lastIndexOf(o);
    }

    @Override
    public ListIterator<SelectorStep> listIterator() {
        return selectorSteps.listIterator();
    }

    @Override
    public ListIterator<SelectorStep> listIterator(int index) {
        return selectorSteps.listIterator(index);
    }

    @Override
    public List<SelectorStep> subList(int fromIndex, int toIndex) {
        return selectorSteps.subList(fromIndex, toIndex);
    }

    private static class ContextIndex {
        private int i;
        private final ExecutionContext executionContext;

        public ContextIndex(ExecutionContext executionContext) {
            this.executionContext = executionContext;
        }
    }
}
