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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.smooks.assertion.AssertArgument;

import java.util.HashMap;
import java.util.Map;

/**
 * Enables multiple {@link DaoRegister} objects to be used as one DaoRegister.
 * Each DaoRegister gets a name. To get the correct DAO the following name notation
 * is used "{DaoRegister name}.{Dao name}".
 * <p>
 * A {@link MultiDaoRegister} can be created via the static {@link #newInstance(Map)} method
 * or via the {@link Builder} object. The Builder object can be created via it's constructor
 * or the static {@link #builder()} or {@link #builder(Map)} methods.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class MultiDaoRegister<T> extends AbstractDaoRegister<T> {

	/**
	 * Creates a new {@link MultiDaoRegister} and fills it with the provided map.
	 *
	 * @param <T> the type of the DAO
	 * @param map the map that fills the new {@link MultiDaoRegister}
	 * @return the new {@link MultiDaoRegister}
	 */
	public static <T> MultiDaoRegister<T> newInstance(Map<String, ? extends DaoRegister<T>> map) {
		AssertArgument.isNotNull(map, "map");

		return new MultiDaoRegister<T>(new HashMap<String, DaoRegister<T>>(map));
	}

	/**
	 * Creates a Builder object that can build a {@link MultiDaoRegister}
	 *
	 * @param <T> The type of the DAO
	 * @return The builder
	 */
	public static <T> Builder<T> builder() {
		return new Builder<T>();
	}


	/**
	 * Creates a Builder object that can build a {@link MultiDaoRegister}.
	 * The builder will be instantiated with the provided map.
	 *
	 * @param <T> The type of the DAO
	 * @param map The map that is added to the builder
	 * @return The builder
	 */
	public static <T> Builder<T> builder(Map<String, ? extends DaoRegister<T>> map) {
		return new Builder<T>(map);
	}

	private Map<String, DaoRegister<T>> daoRegisterMap = new HashMap<String, DaoRegister<T>>();

	/**
	 * @param hashMap
	 */
	private MultiDaoRegister(Map<String, DaoRegister<T>> daoRegisterMap) {
		this.daoRegisterMap = daoRegisterMap;
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.register.AbstractDaoRegister#getDao(java.lang.String)
	 */
	@Override
	public T getDao(String name) {

		int sepIndex = name.indexOf(".");
		String key = name;
		String subName = null;
		if(sepIndex >= 0) {
			key = name.substring(0, sepIndex);
			subName = name.substring(sepIndex+1);
		}

		DaoRegister<T> subDaoRegister = daoRegisterMap.get(key);

		if(subDaoRegister == null) {
			return null;
		}
		if(subName == null) {
			return subDaoRegister.getDefaultDao();
		} else {
			return subDaoRegister.getDao(subName);
		}
	}

	public int size() {
		return daoRegisterMap.size();
	}

	public Map<String, DaoRegister<T>> getDaoRegisterMap() {
		return new HashMap<String, DaoRegister<T>>(daoRegisterMap);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return daoRegisterMap.hashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("daoRegisterMap", daoRegisterMap, true)
				.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {

		if(obj == null) {
			return false;
		}
		if(!(obj instanceof MultiDaoRegister)) {
			return false;
		}

		@SuppressWarnings("unchecked")
		final MultiDaoRegister<T> other = (MultiDaoRegister<T>) obj;

		return daoRegisterMap.equals(other.daoRegisterMap);
	}

	/**
	 * Builds a MapDaoRegister object.
	 *
	 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
	 *
	 * @param <T> The DAO type
	 */
	static class Builder<T> {

		private final Map<String, DaoRegister<T>> map;

		/**
		 * creates an empty Builder
		 */
		public Builder() {
			map = new HashMap<String, DaoRegister<T>>();
		}

		/**
		 * Creates an Builder and copies all of the mappings for the
		 * specified map to this builder
		 */
		public Builder(Map<String, ? extends DaoRegister<T>> map) {
			AssertArgument.isNotNull(map, "map");

			this.map = new HashMap<String, DaoRegister<T>>(map);
		}

		/**
		 * Adds a DAO register under a specified name
		 *
		 * @param name the name of the DAO
		 * @param dao the DAO
		 * @return the builder
		 */
		public Builder<T> put(String name, DaoRegister<T> daoRegister) {
			AssertArgument.isNotNull(name, "name");
			AssertArgument.isNotNull(daoRegister, "daoRegister");

			map.put(name, daoRegister);

			return this;
		}

		/**
		 * Copies all of the mappings for the specified map to this builder
		 *
		 * @param map mapping to be stored in this builder
		 * @return the builder
		 */
		public Builder<T> putAll(Map<String, ? extends DaoRegister<T>> map) {
			AssertArgument.isNotNull(map, "map");

			this.map.putAll(map);

			return this;
		}

		/**
		 * Creates the {@link MultiDaoRegister} and provides it with the
		 * DAO register mapping
		 *
		 * @return the created {@link MultiDaoRegister}
		 */
		public MultiDaoRegister<T> build() {
			return new MultiDaoRegister<T>(map);
		}

	}


}
