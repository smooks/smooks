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
package org.milyn.scribe.register;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.milyn.assertion.AssertArgument;
import org.milyn.scribe.register.MultiDaoRegister.Builder;



/**
 * A immutable map based DAO register
 * <p>
 * A {@link MapDaoRegister} can be created via the static {@link #newInstance(Map)} method
 * or via the {@link Builder} object. The Builder object can be created via it's constructor
 * or the static {@link #builder()} or {@link #builder(Map)} methods.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 * @param <T> The DAO type
 */
public class MapDaoRegister<T> extends AbstractDaoRegister<T> {

	/**
	 * Creates a new {@link MapDaoRegister} and fills it with the provided map.
	 *
	 * @param <T> the type of the DAO
	 * @param map the map that fills the new {@link MapDaoRegister}
	 * @return the new {@link MapDaoRegister}
	 */
	public static <T> MapDaoRegister<T> newInstance(Map<String, ? extends T> map) {
		AssertArgument.isNotNull(map, "map");

		return new MapDaoRegister<T>(new HashMap<String, T>(map));
	}

	/**
	 * Creates a Builder object that can build a {@link MapDaoRegister}
	 *
	 * @param <T> The type of the DAO
	 * @return The builder
	 */
	public static <T> Builder<T> builder() {
		return new Builder<T>();
	}

	/**
	 * Creates a Builder object that can build a {@link MapDaoRegister}.
	 * The builder will be instantiated with the provided map.
	 *
	 * @param <T> The type of the DAO
	 * @param map The map that is added to the builder
	 * @return The builder
	 */
	public static <T> Builder<T> builder(Map<String, ? extends T> map) {
		return new Builder<T>(map);
	}

	private final HashMap<String, ? extends T> map;

	/**
	 *
	 */
	private MapDaoRegister(HashMap<String, ? extends T> map) {
		this.map = map;
	}

	public boolean containsKey(final String key) {

		return map.containsKey(key);

	}

	public boolean containsDAO(final T dao) {

		return map.containsValue(dao);

	}

	/**
	 * Returns a clone of the underlying map.
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, T> getAll() {

		return (Map<String, T>) map.clone();
	}

	/* (non-Javadoc)
	 * @see org.milyn.scribe.DAORegistery#getDAO(java.lang.String)
	 */
	@Override
	public T getDao(final String key) {
		return map.get(key);
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return map.hashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("daoMap", map, true)
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
		if(obj instanceof MapDaoRegister == false) {
			return false;
		}

		@SuppressWarnings("unchecked")
		final MapDaoRegister<T> other = (MapDaoRegister<T>) obj;

		return map.equals(other.map);
	}

	public int size() {
		return map.size();
	}

	/**
	 * Builds a MapDaoRegister object.
	 *
	 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
	 *
	 * @param <T> The DAO type
	 */
	public static class Builder<T> {

		private final HashMap<String, T> map;

		/**
		 * creates an empty Builder
		 */
		public Builder() {
			map = new HashMap<String, T>();
		}

		/**
		 * Creates an Builder and copies all of the mappings for the
		 * specified map to this builder
		 */
		public Builder(Map<String, ? extends T> map) {
			AssertArgument.isNotNull(map, "map");

			this.map = new HashMap<String, T>(map);
		}

		/**
		 * Adds a DAO under a specified name
		 *
		 * @param name the name of the DAO
		 * @param dao the DAO
		 * @return the builder
		 */
		public Builder<T> put(String name, T dao) {
			AssertArgument.isNotNull(name, "name");
			AssertArgument.isNotNull(dao, "dao");

			map.put(name, dao);

			return this;
		}

		/**
		 * Copies all of the mappings for the specified map to this builder
		 *
		 * @param map mapping to be stored in this builder
		 * @return the builder
		 */
		public Builder<T> putAll(Map<String, ? extends T> map) {
			AssertArgument.isNotNull(map, "map");

			this.map.putAll(map);

			return this;
		}

		/**
		 * Creates the MapDaoRegister and provides it with the
		 * DAO map
		 *
		 * @return the created {@link MapDaoRegister}
		 */
		public MapDaoRegister<T> build() {
			return new MapDaoRegister<T>(map);
		}

	}

}
