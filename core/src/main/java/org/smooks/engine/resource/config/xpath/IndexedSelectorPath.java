/*-
 * ========================LICENSE_START=================================
 * Core
 * %%
 * Copyright (C) 2020 - 2021 Smooks
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

import org.jaxen.pattern.Pattern;
import org.jaxen.saxpath.SAXPathException;
import org.smooks.api.SmooksException;
import org.smooks.api.expression.ExecutionContextExpressionEvaluator;
import org.smooks.api.expression.ExpressionEvaluator;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.config.xpath.SelectorStep;
import org.smooks.assertion.AssertArgument;
import org.smooks.engine.resource.config.xpath.step.ElementSelectorStep;

import java.util.*;

public class IndexedSelectorPath implements JaxenPatternSelectorPath {

    protected final Pattern pattern;
    protected final List<SelectorStep> selectorSteps = new ArrayList<>();
    protected final Properties namespaces = new Properties();
    protected final String selector;

    /**
     * The XML namespace of the tag to which this config
     * should only be applied.
     */
    @Deprecated
    private String namespaceURI;
    private ExpressionEvaluator expressionEvaluator;

    @Override
    public String getSelector() {
        return selector;
    }

    public IndexedSelectorPath() {
        this(ResourceConfig.SELECTOR_NONE);
    }

    public IndexedSelectorPath(final String selector) {
        this.selector = selector;
        try {
            this.pattern = PatternParser.parse(selector);
        } catch (SAXPathException e) {
            throw new SmooksException(e);
        }
    }

    @Override
    public SelectorStep getTargetSelectorStep() {
        return selectorSteps.isEmpty() ? null : selectorSteps.get(selectorSteps.size() - 1);
    }

    @Override
    @Deprecated
    public String getSelectorNamespaceURI() {
        return namespaceURI;
    }

    @Override
    public void setConditionEvaluator(ExpressionEvaluator expressionEvaluator) {
        if (expressionEvaluator != null && !(expressionEvaluator instanceof ExecutionContextExpressionEvaluator)) {
            throw new UnsupportedOperationException("Unsupported ExpressionEvaluator type '" + expressionEvaluator.getClass().getName() + "'.  Currently only support '" + ExecutionContextExpressionEvaluator.class.getName() + "' implementations.");
        }
        this.expressionEvaluator = expressionEvaluator;
    }

    @Override
    public ExpressionEvaluator getConditionEvaluator() {
        return expressionEvaluator;
    }

    @Override
    @Deprecated
    public void setSelectorNamespaceURI(final String namespaceURI) {
        this.namespaceURI = namespaceURI;
    }

    @Override
    public Properties getNamespaces() {
        return namespaces;
    }

    @Override
    public void setNamespaces(Properties namespaces) {
        AssertArgument.isNotNull(namespaces, "namespaces");

        this.namespaces.putAll(namespaces);

        for (int i = 0; i < selectorSteps.size(); i++) {
            SelectorStep step = selectorSteps.get(i);
            try {
                step.getNamespaces().putAll(this.namespaces);
            } catch (Exception e) {
                throw new SmooksException("Error compiling PredicatesEvaluator.", e);
            }

            if (i < selectorSteps.size() - 1 && step instanceof ElementSelectorStep && ((ElementSelectorStep) step).accessesText()) {
                throw new SmooksException("Unsupported XPath selector expression '" + selector + "'.  XPath 'text()' tokens are only supported in the last step.");
            }
        }
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
        return selectorSteps.removeAll(c);
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

    @Override
    public String toString() {
        return selector;
    }

    @Override
    public Pattern getPattern() {
        return pattern;
    }
}
