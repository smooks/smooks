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
package org.milyn.delivery;

import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.delivery.ordering.Sorter;

import java.util.*;

/**
 * Simple table for storing {@link ContentHandlerConfigMap} lists against a selector string.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ContentHandlerConfigMapTable<T extends ContentHandler> {

    private Map<String, List<ContentHandlerConfigMap<T>>> table = new LinkedHashMap<String, List<ContentHandlerConfigMap<T>>>();
    private List<ContentHandlerConfigMap<T>> list = new ArrayList<ContentHandlerConfigMap<T>>();
    private int count = 0;
    private int userConfiguredCount = 0;

    /**
     * Add a delivery unit mapping for the specified selector.
     *
     * @param elementName The target element for the content handler.
     * @param resourceConfig Resource configuration.
     * @param contentHandler The delivery unit.
     */
    public void addMapping(String elementName, SmooksResourceConfiguration resourceConfig, T contentHandler) {
        addMapping(elementName, new ContentHandlerConfigMap<T>(contentHandler, resourceConfig));
    }

    /**
     * Add a mapping for the specified element.
     * @param elementName The element name.
     * @param mapInst The mapping instance to be added.
     */
    private void addMapping(String elementName, ContentHandlerConfigMap<T> mapInst) {
        List<ContentHandlerConfigMap<T>> elementMappings = table.get(elementName);

        if(elementMappings == null) {
            elementMappings = new Vector<ContentHandlerConfigMap<T>>();
            table.put(elementName, elementMappings);
        }
        elementMappings.add(mapInst);
        list.add(mapInst);
        count++;
        
        if(!mapInst.getResourceConfig().isDefaultResource()) {
        	userConfiguredCount++;
        }
    }

    /**
     * Add all the content handlers defined in the supplied configMap.
     * @param configMap The config map.
     */
    public void addAll(ContentHandlerConfigMapTable<T> configMap) {
        Set<Map.Entry<String, List<ContentHandlerConfigMap<T>>>> mappingsES = configMap.table.entrySet();

        for (Map.Entry<String, List<ContentHandlerConfigMap<T>>> elementMappings : mappingsES) {
            String elementName = elementMappings.getKey();
            List<ContentHandlerConfigMap<T>> mappingList = elementMappings.getValue();

            for (ContentHandlerConfigMap<T> mapping : mappingList) {
                addMapping(elementName, mapping);
            }
        }
    }

    public Map<String, List<ContentHandlerConfigMap<T>>> getTable() {
        return Collections.unmodifiableMap(table);
    }

    /**
     * Get the {@link ContentHandlerConfigMap} list for the supplied selector string.
     * @param selector The lookup selector.
     * @return It's list of {@link ContentHandlerConfigMap} instances, or null if there are none.
     */
    public List<ContentHandlerConfigMap<T>> getMappings(String selector) {
        return table.get(selector);
    }

    /**
     * Get the combined {@link ContentHandlerConfigMap} list for the supplied list of selector strings.
     * @param selectors The lookup selectors.
     * @return The combined {@link ContentHandlerConfigMap} list for the supplied list of selector strings,
     * or an empty list if there are none.
     */
    public List<ContentHandlerConfigMap<T>> getMappings(String[] selectors) {
        List<ContentHandlerConfigMap<T>> combinedList = new ArrayList<ContentHandlerConfigMap<T>>();

        for(String selector : selectors) {
            List<ContentHandlerConfigMap<T>> selectorList = table.get(selector);
            if(selectorList != null) {
                combinedList.addAll(selectorList);
            }
        }

        return combinedList;
    }

    public List<ContentHandlerConfigMap<T>> getAllMappings() {
        return list;
    }

    /**
     * Is the table empty.
     * @return True if the table is empty, otherwise false.
     */
    public boolean isEmpty() {
        return table.isEmpty();
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
        Set<Map.Entry<String,List<ContentHandlerConfigMap<T>>>> tableEntries = table.entrySet();

        for(Map.Entry<String, List<ContentHandlerConfigMap<T>>> tableEntry : tableEntries) {
            Sorter.sort(tableEntry.getValue(), sortOrder);
        }
    }
}
