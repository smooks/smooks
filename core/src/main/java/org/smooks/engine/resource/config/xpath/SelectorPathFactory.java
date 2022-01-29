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
        return newSelectorPath(selectorPath.getSelector(), selectorPath);
    }

    public static SelectorPath newSelectorPath(final String selector, SelectorPath selectorPath) {
        SelectorPath newSelectorPath = newSelectorPath(selector, selectorPath.getNamespaces());
        newSelectorPath.setSelectorNamespaceURI(selectorPath.getSelectorNamespaceURI());
        newSelectorPath.setConditionEvaluator(selectorPath.getConditionEvaluator());

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
        final SelectorPath selectorPath;
        if (ResourceConfig.SELECTOR_NONE.equals(selector)) {
            selectorPath =  _buildSelectorPath(ResourceConfig.SELECTOR_NONE, namespaces);
        } else {
            selectorPath = _buildSelectorPath(selector, namespaces);
        }

        return selectorPath;
    }

    private static SelectorPath _buildSelectorPath(String selector, Properties namespaces) {
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
                return new SelectorPath() {
                    @Override
                    public String getSelector() {
                        return selector;
                    }

                    @Override
                    public String getSelectorNamespaceURI() {
                        return null;
                    }

                    @Override
                    public void setConditionEvaluator(ExpressionEvaluator expressionEvaluator) {

                    }

                    @Override
                    public ExpressionEvaluator getConditionEvaluator() {
                        return null;
                    }

                    @Override
                    public void setSelectorNamespaceURI(String namespaceURI) {

                    }

                    @Override
                    public Properties getNamespaces() {
                        return new Properties();
                    }

                    @Override
                    public void setNamespaces(Properties namespaces) {

                    }

                    @Override
                    public int size() {
                        return 0;
                    }

                    @Override
                    public boolean isEmpty() {
                        return false;
                    }

                    @Override
                    public boolean contains(Object o) {
                        return false;
                    }

                    @Override
                    public Iterator<SelectorStep> iterator() {
                        return null;
                    }

                    @Override
                    public Object[] toArray() {
                        return new Object[0];
                    }

                    @Override
                    public <T> T[] toArray(T[] a) {
                        return null;
                    }

                    @Override
                    public boolean add(SelectorStep selectorStep) {
                        return false;
                    }

                    @Override
                    public boolean remove(Object o) {
                        return false;
                    }

                    @Override
                    public boolean containsAll(Collection<?> c) {
                        return false;
                    }

                    @Override
                    public boolean addAll(Collection<? extends SelectorStep> c) {
                        return false;
                    }

                    @Override
                    public boolean addAll(int index, Collection<? extends SelectorStep> c) {
                        return false;
                    }

                    @Override
                    public boolean removeAll(Collection<?> c) {
                        return false;
                    }

                    @Override
                    public boolean retainAll(Collection<?> c) {
                        return false;
                    }

                    @Override
                    public void clear() {

                    }

                    @Override
                    public SelectorStep get(int index) {
                        return null;
                    }

                    @Override
                    public SelectorStep set(int index, SelectorStep element) {
                        return null;
                    }

                    @Override
                    public void add(int index, SelectorStep element) {

                    }

                    @Override
                    public SelectorStep remove(int index) {
                        return null;
                    }

                    @Override
                    public int indexOf(Object o) {
                        return 0;
                    }

                    @Override
                    public int lastIndexOf(Object o) {
                        return 0;
                    }

                    @Override
                    public ListIterator<SelectorStep> listIterator() {
                        return null;
                    }

                    @Override
                    public ListIterator<SelectorStep> listIterator(int index) {
                        return null;
                    }

                    @Override
                    public List<SelectorStep> subList(int fromIndex, int toIndex) {
                        return null;
                    }
                };
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
}
