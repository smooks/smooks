/*-
 * ========================LICENSE_START=================================
 * Scribe :: Core
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
	@Override
	public D getDefaultDao() {
		if(defaultDao == null) {
			throw new IllegalStateException("No default DAO is set on this '" + this.getClass().getName() + "' DaoRegister.");
		}
		return defaultDao;
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.register.AbstractDaoRegister#getDao(java.lang.String)
	 */
	@Override
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
