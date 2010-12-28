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
package org.milyn.scribe;

import java.util.HashMap;
import java.util.Map;

/**
 * A Map implementation of the {@link ObjectStore}.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class MapObjectStore implements ObjectStore {

	HashMap<Object, Object> store = new HashMap<Object, Object>();

	/* (non-Javadoc)
	 * @see org.milyn.scribe.ObjectStore#get(java.lang.Object)
	 */
	public Object get(Object key) {
		return store.get(key);
	}

	/* (non-Javadoc)
	 * @see org.milyn.scribe.ObjectStore#getAll()
	 */
	@SuppressWarnings("unchecked")
	public Map<Object, Object> getAll() {
		return (Map<Object, Object>) store.clone();
	}

	/* (non-Javadoc)
	 * @see org.milyn.scribe.ObjectStore#remove(java.lang.Object)
	 */
	public void remove(Object key) {
		store.remove(key);
	}

	/* (non-Javadoc)
	 * @see org.milyn.scribe.ObjectStore#set(java.lang.Object, java.lang.Object)
	 */
	public void set(Object key, Object value) {
		store.put(key, value);
	}


}
