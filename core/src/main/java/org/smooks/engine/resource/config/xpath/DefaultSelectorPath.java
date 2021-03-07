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
package org.smooks.engine.resource.config.xpath;

import org.jaxen.saxpath.SAXPathException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.SmooksException;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.config.xpath.SelectorPath;
import org.smooks.api.resource.config.xpath.SelectorStep;
import org.smooks.api.resource.config.xpath.XPathExpressionEvaluator;
import org.smooks.api.expression.ExecutionContextExpressionEvaluator;
import org.smooks.api.expression.ExpressionEvaluator;
import org.smooks.assertion.AssertArgument;
import org.smooks.api.SmooksConfigException;
import org.smooks.engine.resource.config.xpath.evaluators.PassThruEvaluator;

import javax.xml.namespace.QName;
import java.util.*;

public class DefaultSelectorPath implements SelectorPath {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSelectorPath.class);

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

    private final List<SelectorStep> selectorSteps = new ArrayList<>();

    /**
     * Get the selector definition for this ResourceConfig.
     *
     * @return The selector definition.
     */
    @Override
    public String getSelector() {
        return selector;
    }

    /**
     * Set the config selector.
     *
     * @param selector The selector definition.
     */
    @Override
    public void setSelector(final String selector) {
        AssertArgument.isNotEmpty(selector, "selector");
        this.selector = selector;

        // If there's a "#document" token in the selector, but it's not at the very start,
        // then we have an invalid selector...
        int docSelectorIndex = selector.trim().indexOf(ResourceConfig.DOCUMENT_FRAGMENT_SELECTOR);
        if (docSelectorIndex > 0) {
            throw new SmooksConfigException("Invalid selector '" + selector + "'.  '" + ResourceConfig.DOCUMENT_FRAGMENT_SELECTOR + "' token can only exist at the start of the selector.");
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

        SelectorPath selectorPath = new DefaultSelectorPath();
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

    private static class LegacySelectorStep extends DefaultSelectorStep {
        public LegacySelectorStep(String selector, String targetElementName) {
            super(selector, targetElementName);
        }

        public LegacySelectorStep(String xpathExpression, String targetElementName, String targetAttributeName) {
            super(xpathExpression, targetElementName, targetAttributeName);
        }

        @Override
        public XPathExpressionEvaluator getPredicatesEvaluator() {
            return PassThruEvaluator.INSTANCE;
        }

        @Override
        public void buildPredicatesEvaluator(Properties namespaces) {
            // Ignore this.
        }
    }

    protected String[] parseSelector(String selector) {
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
    @Override
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
    @Override
    public String getTargetElement() {
        return getTargetSelectorStep().getElement().getLocalPart();
    }

    /**
     * Get the name of the attribute specified on the selector, if one was
     * specified.
     *
     * @return An attribute name, if one was specified on the selector, otherwise null.
     */
    @Override
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
    @Override
    @Deprecated
    public String getSelectorNamespaceURI() {
        return namespaceURI;
    }

    /**
     * Set the condition evaluator to be used in targeting of this resource.
     *
     * @param expressionEvaluator The {@link ExpressionEvaluator}, or null if no condition is to be used.
     */
    @Override
    public void setConditionEvaluator(ExpressionEvaluator expressionEvaluator) {
        if (expressionEvaluator != null && !(expressionEvaluator instanceof ExecutionContextExpressionEvaluator)) {
            throw new UnsupportedOperationException("Unsupported ExpressionEvaluator type '" + expressionEvaluator.getClass().getName() + "'.  Currently only support '" + ExecutionContextExpressionEvaluator.class.getName() + "' implementations.");
        }
        this.expressionEvaluator = expressionEvaluator;
    }

    /**
     * Get the condition evaluator used in targeting of this resource.
     *
     * @return The {@link ExpressionEvaluator}, or null if no condition is specified.
     */
    @Override
    public ExpressionEvaluator getConditionEvaluator() {
        return expressionEvaluator;
    }

    /**
     * Set the namespace URI to which the selector is associated.
     *
     * @param namespaceURI Selector namespace.
     */
    @Override
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

    @Override
    public Properties getNamespaces() {
        return namespaces;
    }

    @Override
    public SelectorPath copy() {
        DefaultSelectorPath copySelectorPath = new DefaultSelectorPath();

        copySelectorPath.selector = selector;
        copySelectorPath.namespaceURI = namespaceURI;
        copySelectorPath.expressionEvaluator = expressionEvaluator;
        copySelectorPath.namespaces = namespaces;
        for (SelectorStep selectorStep : selectorSteps) {
            copySelectorPath.selectorSteps.add(selectorStep.copy());
        }
        
        return copySelectorPath;
    }

    /**
     * Set the namespaces on the specified set of selector steps.
     * @param namespaces The set of selector steps to be updated.
     * @return The set of selector steps (as passed in the 'steps' argument).
     * @throws org.jaxen.saxpath.SAXPathException Error setting namespaces
     */
    @Override
    public void setNamespaces(Properties namespaces) {
        AssertArgument.isNotNull(namespaces, "namespaces");

        this.namespaces.putAll(namespaces);
        
        for(int i = 0; i < selectorSteps.size(); i++) {
            SelectorStep step = selectorSteps.get(i);
            try {
                step.buildPredicatesEvaluator(this.namespaces);
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
}
