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
package org.smooks.persistence.parameter;


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class NamedParameterContainer implements ParameterContainer<NamedParameter>{

	private HashMap<String, Object> parameterMap;

	private Entry<String, Object>[] parameterEntries;

	@SuppressWarnings("unchecked")
	public NamedParameterContainer(final NamedParameterIndex index) {
		parameterEntries = new Entry[index.size()];
		parameterMap = new HashMap<String, Object>();

		updateParameterMap(index);
	}

	public void put(NamedParameter param, Object bean) {
		parameterEntries[param.getIndex()].setValue(bean);
	}

	public boolean containsParameter(NamedParameter param) {
		int index = param.getIndex();

		return parameterEntries.length > index && parameterEntries[index].getValue() != null;
	}


	public Object get(NamedParameter param) {
		return parameterEntries[param.getIndex()].getValue();
	}

	public Object get(String name) {
		return parameterMap.get(name);
	}

	public Object remove(NamedParameter param) {

		Object old = get(param);

		if(old != null) {
			parameterEntries[param.getIndex()].setValue(null);
		}

		return old;
	}

	public void clear() {
		for(Entry<String, Object> parameter : parameterEntries) {
			parameter.setValue(null);
		}

	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getParameterMap() {
		return (Map<String, Object>) parameterMap.clone();
	}

	private void updateParameterMap(final NamedParameterIndex index) {

		for(String name : index.getIndexMap().keySet()) {

			if(!parameterMap.containsKey(name) ) {
				parameterMap.put(name, null);
			}
		}
		updateParameterEntries(index);
	}

	private void updateParameterEntries(final NamedParameterIndex index) {

		for(Entry<String, Object> parameterMapEntry : parameterMap.entrySet()) {

			NamedParameter parameter = index.getParameter(parameterMapEntry.getKey());

			parameterEntries[parameter.getIndex()] = parameterMapEntry;
		}
	}
}
