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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public abstract class ParameterIndex<K extends Object, P extends Parameter<?>> {


	private final Map<K, P> indexMap = new HashMap<K, P>();

	public P register(K value) {

		P parameter = indexMap.get(value);
		if(parameter == null) {
			parameter = createParameter(value);

			indexMap.put(value, parameter);
		}
		return parameter;
	}

	protected abstract P createParameter(K value) ;

	public P getParameter(Object keyValue) {
		return indexMap.get(keyValue);
	}

	public boolean containsKey(Object key) {
		return indexMap.containsKey(key);
	}

	public boolean containsParameter(Object parameter) {
		return indexMap.containsValue(parameter);
	}

	public Map<K, P> getIndexMap() {
		return Collections.unmodifiableMap(indexMap) ;
	}

	public int size() {
		return indexMap.size();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return indexMap.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(ParameterIndex<?, ?> rhs) {
		return indexMap.equals(rhs.indexMap);
	}

}
