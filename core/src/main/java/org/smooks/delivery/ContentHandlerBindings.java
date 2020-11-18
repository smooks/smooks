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
package org.smooks.delivery;

import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.delivery.ordering.Sorter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Simple table for storing {@link ContentHandlerBinding} lists against a selector string.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ContentHandlerBindings<T extends ContentHandler> {

    private final Map<String, List<ContentHandlerBinding<T>>> contentHandlerBindingsByElementName = new LinkedHashMap<>();
    private int count = 0;
    private int userConfiguredCount = 0;

    /**
     * Add a delivery unit mapping for the specified selector.
     *
     * @param elementName The target element for the content handler.
     * @param resourceConfig Resource configuration.
     * @param contentHandler The delivery unit.
     */
    public void addBinding(String elementName, SmooksResourceConfiguration resourceConfig, T contentHandler) {
        addBinding(elementName, new ContentHandlerBinding<>(contentHandler, resourceConfig));
    }

    /**
     * Add a mapping for the specified element.
     * @param elementName The element name.
     * @param contentHandlerBinding The mapping instance to be added.
     */
    public void addBinding(String elementName, ContentHandlerBinding<T> contentHandlerBinding) {
        List<ContentHandlerBinding<T>> elementMappings = contentHandlerBindingsByElementName.computeIfAbsent(elementName, k -> new Vector<>());

        elementMappings.add(contentHandlerBinding);
        count++;
        
        if(!contentHandlerBinding.getSmooksResourceConfiguration().isDefaultResource()) {
        	userConfiguredCount++;
        }
    }

    /**
     * Add all the content handlers defined in the supplied configMap.
     * @param contentHandlerBindings The config map.
     */
    public void addAll(ContentHandlerBindings<T> contentHandlerBindings) {
        Set<Map.Entry<String, List<ContentHandlerBinding<T>>>> mappingsES = contentHandlerBindings.contentHandlerBindingsByElementName.entrySet();

        for (Map.Entry<String, List<ContentHandlerBinding<T>>> elementMappings : mappingsES) {
            String elementName = elementMappings.getKey();
            List<ContentHandlerBinding<T>> mappingList = elementMappings.getValue();

            for (ContentHandlerBinding<T> mapping : mappingList) {
                addBinding(elementName, mapping);
            }
        }
    }

    public Map<String, List<ContentHandlerBinding<T>>> getTable() {
        return Collections.unmodifiableMap(contentHandlerBindingsByElementName);
    }

    /**
     * Get the {@link ContentHandlerBinding} list for the supplied selector string.
     * @param selector The lookup selector.
     * @return It's list of {@link ContentHandlerBinding} instances, or null if there are none.
     */
    public List<ContentHandlerBinding<T>> getMappings(String selector) {
        return contentHandlerBindingsByElementName.get(selector);
    }

    /**
     * Get the combined {@link ContentHandlerBinding} list for the supplied list of selector strings.
     * @param selectors The lookup selectors.
     * @return The combined {@link ContentHandlerBinding} list for the supplied list of selector strings,
     * or an empty list if there are none.
     */
    public List<ContentHandlerBinding<T>> getMappings(String[] selectors) {
        List<ContentHandlerBinding<T>> combinedList = new ArrayList<ContentHandlerBinding<T>>();

        for(String selector : selectors) {
            List<ContentHandlerBinding<T>> selectorList = contentHandlerBindingsByElementName.get(selector);
            if(selectorList != null) {
                combinedList.addAll(selectorList);
            }
        }

        return combinedList;
    }

    public List<ContentHandlerBinding<T>> getAllMappings() {
        return contentHandlerBindingsByElementName.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }

    /**
     * Is the table empty.
     * @return True if the table is empty, otherwise false.
     */
    public boolean isEmpty() {
        return contentHandlerBindingsByElementName.isEmpty();
    }

    /**
     * Get the total number of mappings on this table.
     * @return The total number of mappings on this table.
     */
    public int getCount() {
        return count;
    }

    /**
     * Get the total number of user configured mappings on this table.
     * @return The total number of user configured mappings on this table.
     */
    public int getUserConfiguredCount() {
        return userConfiguredCount;
    }

    /**
     * Sort the Table in the specified sort order.
     * @param sortOrder The sort order.
     */
    public void sort(Sorter.SortOrder sortOrder) {
        Set<Map.Entry<String,List<ContentHandlerBinding<T>>>> tableEntries = contentHandlerBindingsByElementName.entrySet();

        for(Map.Entry<String, List<ContentHandlerBinding<T>>> tableEntry : tableEntries) {
            Sorter.sort(tableEntry.getValue(), sortOrder);
        }
    }
}