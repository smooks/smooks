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

package org.smooks.container;

import java.util.Map;

/**
 * BoundAttributeStore interface definition.
 * <p/>
 * Defines methods for binding, getting and removing named objects on
 * an implementing class.
 * @author tfennelly
 */
public interface BoundAttributeStore {

	/**
	 * Binds an object to this {@link org.smooks.container.BoundAttributeStore} implementation, using the name
	 * specified. If an object of the same name is already bound, the object
	 * is replaced.
	 * <p/>
	 * @param key The key against which the object is bound; cannot be null.
	 * @param value The object to be bound; cannot be null.
	 */
	public abstract void setAttribute(Object key, Object value);

	/**
	 * Returns the object bound with the specified name in this {@link org.smooks.container.BoundAttributeStore}
	 * implementation, or null if no object is bound under the name.
     * @param key The key against which the object is bound; cannot be null.
	 * @return The object bound with the specified name in this {@link org.smooks.container.BoundAttributeStore}
	 * implementation, or null if no object is bound under the name.
	 */
	public abstract Object getAttribute(Object key);

	/**
	 * Returns the Map of attributes bound in this {@link org.smooks.container.BoundAttributeStore}
	 * @return Map of all objects bound in this {@link org.smooks.container.BoundAttributeStore}
	 */
	public abstract Map<Object, Object> getAttributes();

	/**
	 * Removes the object bound with the specified name from this {@link org.smooks.container.BoundAttributeStore}
	 * implementation. If the {@link org.smooks.container.BoundAttributeStore} implementation does
	 * not have an object bound with the specified name, this method does nothing.
     * @param key The key against which the object is bound; cannot be null.
	 */
	public abstract void removeAttribute(Object key);
}
