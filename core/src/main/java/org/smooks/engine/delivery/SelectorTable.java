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
package org.smooks.engine.delivery;

import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.delivery.ContentHandler;
import org.smooks.api.delivery.ContentHandlerBinding;
import org.smooks.engine.delivery.ordering.Sorter;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

/**
 * Simple table for storing {@link ContentHandlerBinding} lists against a selector string.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@NotThreadSafe
public class SelectorTable<T extends ContentHandler> implements Map<String, List<ContentHandlerBinding<T>>> {

    private final Map<String, List<ContentHandlerBinding<T>>> contentHandlerBindingsBySelector = new LinkedHashMap<>();

    /**
     * Add a delivery unit mapping for the specified selector.
     *
     * @param selector The target element for the content handler.
     * @param resourceConfig Resource configuration.
     * @param contentHandler The delivery unit.
     */
    public void put(String selector, ResourceConfig resourceConfig, T contentHandler) {
        put(selector, new DefaultContentHandlerBinding<>(contentHandler, resourceConfig));
    }

    /**
     * Add a <code>ContentHandlerBinding</code> for the specified selector.
     * 
     * @param selector The element name.
     * @param contentHandlerBinding The mapping instance to be added.
     */
    public void put(String selector, ContentHandlerBinding<T> contentHandlerBinding) {
        List<ContentHandlerBinding<T>> contentHandlerBindings = contentHandlerBindingsBySelector.computeIfAbsent(selector, k -> new ArrayList<>());
        contentHandlerBindings.add(contentHandlerBinding);
    }
    
    /**
     * Get the combined {@link ContentHandlerBinding} list for the supplied list of selector strings.
     * @param selectors The lookup selectors.
     * @return The combined {@link ContentHandlerBinding} list for the supplied list of selector strings,
     * or an empty list if there are none.
     */
    public List<ContentHandlerBinding<T>> get(String... selectors) {
        List<ContentHandlerBinding<T>> collectedContentHandlerBindings = new ArrayList<>();

        for (String selector : selectors) {
            List<ContentHandlerBinding<T>> contentHandlerBindings = contentHandlerBindingsBySelector.get(selector);
            if (contentHandlerBindings != null) {
                collectedContentHandlerBindings.addAll(contentHandlerBindings);
            }
        }

        return collectedContentHandlerBindings;
    }

    @Override
    public int size() {
        return contentHandlerBindingsBySelector.size();
    }

    /**
     * Is the table empty.
     * @return True if the table is empty, otherwise false.
     */
    @Override
    public boolean isEmpty() {
        return contentHandlerBindingsBySelector.isEmpty();
    }

    @Override
    public boolean containsKey(Object selector) {
        return contentHandlerBindingsBySelector.containsKey(selector);
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the {@link ContentHandlerBinding} list for the supplied selector string.
     * @param selector The lookup selector.
     * @return It's list of {@link ContentHandlerBinding} instances, or null if there are none.
     */
    @Override
    public List<ContentHandlerBinding<T>> get(Object selector) {
        return contentHandlerBindingsBySelector.get(selector);
    }

    @Override
    public List<ContentHandlerBinding<T>> put(String selector, List<ContentHandlerBinding<T>> contentHandlerBindings) {
        for (ContentHandlerBinding<T> contentHandlerBinding : contentHandlerBindings) {
            put(selector, contentHandlerBinding);
        }
        return contentHandlerBindings;
    }

    @Override
    public List<ContentHandlerBinding<T>> remove(Object key) {
        return contentHandlerBindingsBySelector.remove(key);
    }

    /**
     * Add all the content handlers defined in the supplied <code>Map</code>.
     * @param selectorTable The config map.
     */
    @Override
    public void putAll(Map<? extends String, ? extends List<ContentHandlerBinding<T>>> selectorTable) {
        for (Entry<? extends String, ? extends List<ContentHandlerBinding<T>>> entry : selectorTable.entrySet()) {
            String selector = entry.getKey();
            List<ContentHandlerBinding<T>> contentHandlerBindings = entry.getValue();

            for (ContentHandlerBinding<T> contentHandlerBinding : contentHandlerBindings) {
                put(selector, contentHandlerBinding);
            }
        }
    }

    @Override
    public void clear() {
        contentHandlerBindingsBySelector.clear();
    }

    @Override
    public Set<String> keySet() {
        return Collections.unmodifiableSet(contentHandlerBindingsBySelector.keySet());
    }

    @Override
    public Collection<List<ContentHandlerBinding<T>>> values() {
        return Collections.unmodifiableCollection(contentHandlerBindingsBySelector.values());
    }

    @Override
    public Set<Entry<String, List<ContentHandlerBinding<T>>>> entrySet() {
        return Collections.unmodifiableSet(contentHandlerBindingsBySelector.entrySet());
    }

    /**
     * Sort the Table in the specified sort order.
     * @param sortOrder The sort order.
     */
    public void sort(Sorter.SortOrder sortOrder) {
        Set<Map.Entry<String,List<ContentHandlerBinding<T>>>> tableEntries = contentHandlerBindingsBySelector.entrySet();

        for(Map.Entry<String, List<ContentHandlerBinding<T>>> tableEntry : tableEntries) {
            Sorter.sort(tableEntry.getValue(), sortOrder);
        }
    }
}