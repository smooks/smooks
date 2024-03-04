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

import org.jaxen.saxpath.SAXPathException;
import org.jaxen.saxpath.XPathReader;
import org.jaxen.saxpath.helpers.XPathReaderFactory;
import org.smooks.api.SmooksConfigException;
import org.smooks.api.SmooksException;
import org.smooks.api.expression.ExpressionEvaluator;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.config.xpath.SelectorPath;
import org.smooks.api.resource.config.xpath.SelectorStep;
import org.smooks.assertion.AssertArgument;

import java.util.*;

public final class SelectorPathFactory {

    private SelectorPathFactory() {

    }

    public static SelectorPath newSelectorPath(final String selector) {
        return newSelectorPath(selector, new Properties());
    }

    public static SelectorPath newSelectorPath(final SelectorPath selectorPath) {
        return newSelectorPath(selectorPath.getSelector(), selectorPath.getNamespaces(), selectorPath.getConditionEvaluator());
    }

    public static SelectorPath newSelectorPath(final String selector, Properties namespaces, ExpressionEvaluator conditionEvaluator) {
        SelectorPath newSelectorPath = newSelectorPath(selector, namespaces);
        newSelectorPath.setConditionEvaluator(conditionEvaluator);

        return newSelectorPath;
    }

    public static SelectorPath newSelectorPath(final String selector, Properties namespaces) {
        AssertArgument.isNotEmpty(selector, "selector");
        int docSelectorIndex = selector.trim().indexOf(ResourceConfig.DOCUMENT_FRAGMENT_SELECTOR);
        if (docSelectorIndex > 0) {
            throw new SmooksConfigException("Invalid selector '" + selector + "'.  '" + ResourceConfig.DOCUMENT_FRAGMENT_SELECTOR + "' token can only exist at the start of the selector.");
        }

        SelectorPath selectorPath = buildSelectorPath(selector, namespaces);

        if (namespaces != null) {
            selectorPath.setNamespaces(namespaces);
        }

        return selectorPath;
    }

    private static SelectorPath buildSelectorPath(String selector, Properties namespaces) {
        AssertArgument.isNotNull(selector, "selector");

        String xpathExpression = selector.replaceAll(ResourceConfig.DOCUMENT_FRAGMENT_SELECTOR, "/*");
        final XPathReader reader;
        try {
            reader = XPathReaderFactory.createReader();
        } catch (SAXPathException e) {
            throw new SmooksException(e);
        }

        try {
            reader.parse(xpathExpression);
        } catch (SAXPathException e) {
            if (selector.split(",").length > 1) {
                return new CompositeSelectorPath(selector, namespaces);
            } else {
                throw new SmooksException(e);
            }
        }

        SelectorPathJaxenHandler xpathHandler = new SelectorPathJaxenHandler(xpathExpression, namespaces);
        if (xpathExpression.trim().length() > 0) {
            reader.setXPathHandler(xpathHandler);
            try {
                reader.parse(xpathExpression);
            } catch (SAXPathException e) {
                throw new SmooksException(e);
            }
        }

        return xpathHandler.getSelectorPath();
    }

    private static class CompositeSelectorPath implements SelectorPath {

        private final String selector;
        private Properties namespaces;

        public CompositeSelectorPath(String selector, Properties namespaces) {
            this.selector = selector;
            this.namespaces = namespaces;
        }

        @Override
        public String getSelector() {
            return selector;
        }

        @Override
        public void setConditionEvaluator(ExpressionEvaluator expressionEvaluator) {
        }

        @Override
        public ExpressionEvaluator getConditionEvaluator() {
            return null;
        }

        @Override
        public Properties getNamespaces() {
            return namespaces;
        }

        @Override
        public void setNamespaces(Properties namespaces) {
            this.namespaces = namespaces;
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEmpty() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<SelectorStep> iterator() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object[] toArray() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean add(SelectorStep selectorStep) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends SelectorStep> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(int index, Collection<? extends SelectorStep> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SelectorStep get(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SelectorStep set(int index, SelectorStep element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(int index, SelectorStep element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SelectorStep remove(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int indexOf(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int lastIndexOf(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ListIterator<SelectorStep> listIterator() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ListIterator<SelectorStep> listIterator(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<SelectorStep> subList(int fromIndex, int toIndex) {
            throw new UnsupportedOperationException();
        }
    }
}
