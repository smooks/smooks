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

import java.util.Map;

/**
 * The ObjectStore interface
 * <p>
 * An object that stores objects.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public interface ObjectStore {

	/**
	 * Binds an object to this {@link ObjectStore} implementation, using the name
	 * specified. If an object of the same name is already bound, the object
	 * is replaced.
	 * <p/>
	 * @param key The key against which the object is bound; cannot be null.
	 * @param value The object to be bound; cannot be null.
	 */
	public abstract void set(Object key, Object value);

	/**
	 * Returns the object bound with the specified name in this {@link ObjectStore}
	 * implementation, or null if no object is bound under the name.
     * @param key The key against which the object is bound; cannot be null.
	 * @return The object bound with the specified name in this {@link ObjectStore}
	 * implementation, or null if no object is bound under the name.
	 */
	public abstract Object get(Object key);

	/**
	 * Returns the Map of attributes bound in this {@link ObjectStore}
	 * @return Map of all objects bound in this {@link ObjectStore}
	 */
	public abstract Map<Object, Object> getAll();

	/**
	 * Removes the object bound with the specified name from this {@link ObjectStore}
	 * implementation. If the {@link ObjectStoree} implementation does
	 * not have an object bound with the specified name, this method does nothing.
     * @param key The key against which the object is bound; cannot be null.
	 */
	public abstract void remove(Object key);
}
