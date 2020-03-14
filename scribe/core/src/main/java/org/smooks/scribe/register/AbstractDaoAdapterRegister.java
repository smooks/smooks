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
package org.smooks.scribe.register;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.smooks.assertion.AssertArgument;

/**
 * An abstract implementation of a DAO adapter
 *
 * TODO: finish documentation
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public abstract class AbstractDaoAdapterRegister<D, A> extends AbstractDaoRegister<D> {

	private final D defaultDao;

	private final Map<String, D> daoMap = new HashMap<String, D>();

	public AbstractDaoAdapterRegister(A defaultAdaptable) {
		AssertArgument.isNotNull(defaultAdaptable, "defaultAdaptable");

		this.defaultDao = createAdapter(defaultAdaptable);
	}

	public AbstractDaoAdapterRegister(A defaultAdaptable, Map<String, ? extends A> adaptableMap) {
		AssertArgument.isNotNull(defaultAdaptable, "defaultAdaptable");
		AssertArgument.isNotNull(adaptableMap, "adaptableMap");

		this.defaultDao = createAdapter(defaultAdaptable);

		addToSessionMap(adaptableMap);
	}

	public AbstractDaoAdapterRegister(Map<String, ? extends A> adaptableMap) {
		AssertArgument.isNotNull(adaptableMap, "adaptableMap");

		this.defaultDao = null;

		addToSessionMap(adaptableMap);
	}

	protected final void addToSessionMap(Map<String, ? extends A> adaptableMap) {
		for(Entry<String, ? extends A> entry : adaptableMap.entrySet()) {
			if(entry.getValue() == null) {
				throw new NullPointerException("The entry '"+ entry.getKey() +"' contains an null value.");
			}

			this.daoMap.put(entry.getKey(), createAdapter(entry.getValue()));
		}
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.AbstractDaoRegister#getDao()
	 */
	public D getDefaultDao() {
		if(defaultDao == null) {
			throw new IllegalStateException("No default DAO is set on this '" + this.getClass().getName() + "' DaoRegister.");
		}
		return defaultDao;
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.register.AbstractDaoRegister#getDao(java.lang.String)
	 */
	public D getDao(String name) {
		if(name == null) {
			return getDefaultDao();
		}

		D dao = daoMap.get(name);
		if(dao == null) {
			throw new IllegalStateException("No DAO under the name '"+ name +"' was found in this '" + this.getClass().getName() + "' DaoRegister.");
		}
		return dao;
	}

	/**
	 * This method creates the Dao Adapter from the Adaptable.
	 * <p>
	 * <b>NOTE</b><br>
	 * This method can not reference any object fields because this
	 * method is called in the constructor of the
	 * {@link AbstractDaoAdapterRegister} class.
	 *
	 * @param adaptable The object that will be adapted by a Dao Adapter
	 * @return
	 */
	protected abstract D createAdapter(A adaptable);

}
