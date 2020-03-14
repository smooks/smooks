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
package org.smooks.persistence;

import java.util.Map;

import org.smooks.container.ApplicationContext;
import org.smooks.scribe.ObjectStore;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class ApplicationContextObjectStore implements ObjectStore {

	private ApplicationContext applicationContext;

	public ApplicationContextObjectStore(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.ObjectStore#get(java.lang.Object)
	 */
	public Object get(Object key) {
		return applicationContext.getAttribute(key);
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.ObjectStore#getAll()
	 */
	public Map<Object, Object> getAll() {
		return applicationContext.getAttributes();
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.ObjectStore#remove(java.lang.Object)
	 */
	public void remove(Object key) {
		applicationContext.removeAttribute(key);
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.ObjectStore#set(java.lang.Object, java.lang.Object)
	 */
	public void set(Object key, Object value) {
		applicationContext.setAttribute(key, value);
	}




}
