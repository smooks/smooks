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

package org.milyn.javabean.repository;

import org.milyn.javabean.context.BeanIdStore;

import java.util.Map;

/**
 * Bean Id List
 * <p/>
 * Represents a map of BeanId's. Every BeanId has it own unique index. The index
 * is incremental. The index starts with zero.
 * <p/>
 * Once a BeanId is registered it can never be unregistered.
 *
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 * @deprecated Use the BeanIdStore
 */
@Deprecated
public class BeanIdRegister {

	private final BeanIdStore beanIdStore;

	public BeanIdRegister() {
		beanIdStore = new BeanIdStore();
	}

	BeanIdRegister(BeanIdStore beanIdStore) {
		this.beanIdStore = beanIdStore;
	}

	public boolean containsBeanId(String beanId) {
		return beanIdStore.containsBeanId(beanId);
	}

	public boolean equals(Object obj) {
		return beanIdStore.equals(obj);
	}

	public BeanId getBeanId(String beanId) {
		return beanIdStore.getBeanId(beanId);
	}

	public Map<String, BeanId> getBeanIdMap() {
		return beanIdStore.getBeanIdMap();
	}

	public int hashCode() {
		return beanIdStore.hashCode();
	}

	public BeanId register(String beanIdName) {
		return beanIdStore.register(beanIdName);
	}

	public int size() {
		return beanIdStore.size();
	}

	BeanIdStore getBeanIdStore() {
		return beanIdStore;
	}
}
