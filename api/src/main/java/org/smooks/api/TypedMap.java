/*-
 * ========================LICENSE_START=================================
 * API
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
package org.smooks.api;

import java.util.Map;

/**
 * Stores key-value attributes that can be retrieved in a type-safe manner.
 */
public interface TypedMap {

	/**
	 * Associates the specified value with the specified key in this map.
	 * If the map previously contained a mapping for the key, the old
	 * value is replaced.
	 *
	 * @param key key with which the specified value is to be associated
	 * @param value value to be associated with the specified key
	 * @param <T> type of key and value
	 *
	 * @throws NullPointerException if the specified key or value is null
	 *         and this map does not permit null keys or values
	 */
    <T> void put(TypedKey<T> key, T value);

	/**
	 * Returns the value to which the specified key is mapped,
	 * or {@code null} if this map contains no mapping for the key.
	 *
	 * <p>More formally, if this map contains a mapping from a key
	 * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
	 * key.equals(k))}, then this method returns {@code v}; otherwise
	 * it returns {@code null}.  (There can be at most one such mapping.)
	 *
	 * <p>A return value of {@code null} does not <i>necessarily</i>
	 * indicate that the map contains no mapping for the key; it's also
	 * possible that the map explicitly maps the key to {@code null}.
	 *
	 * @param key key for the value to return
	 * @param <T> type of key
	 *
	 * @return the value associated with <tt>key</tt>, or
	 *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
	 *         (A <tt>null</tt> return can also indicate that the map
	 *         previously associated <tt>null</tt> with <tt>key</tt>.)
	 *
	 * @throws NullPointerException if the specified key is null and this map
	 *         does not permit null keys
	 */
    <T> T get(TypedKey<T> key);

	/**
	 * Returns the value to which the specified key is mapped, or
	 * {@code defaultValue} if this map contains no mapping for the key.
	 *
	 * @param key key whose associated value is to be returned
	 * @param defaultValue default mapping of the key
	 * @param <T> type of key and value
	 *
	 * @return value to which the specified key is mapped, or
	 * {@code defaultValue} if this map contains no mapping for the key
	 *
	 * @throws NullPointerException if the specified key is null and this map
	 * does not permit null keys
	 */
	<T> T getOrDefault(TypedKey<T> key, T defaultValue);

	/**
	 * Returns all the key-value pairs as a {@link java.util.Map}.
	 *
	 * @return all the key-value pairs
	 */
	Map<TypedKey<Object>, Object> getAll();

	/**
	 * Removes the mapping for a key from this map if it is present
	 * (optional operation).   More formally, if this map contains a mapping
	 * from key <tt>k</tt> to value <tt>v</tt> such that
	 * <code>(key==null ?  k==null : key.equals(k))</code>, that mapping
	 * is removed.  (The map can contain at most one such mapping.)
	 *
	 * <p>Returns the value to which this map previously associated the key,
	 * or <tt>null</tt> if the map contained no mapping for the key.
	 *
	 * <p>If this map permits null values, then a return value of
	 * <tt>null</tt> does not <i>necessarily</i> indicate that the map
	 * contained no mapping for the key; it's also possible that the map
	 * explicitly mapped the key to <tt>null</tt>.
	 *
	 * <p>The map will not contain a mapping for the specified key once the
	 * call returns.
	 *
	 * @param key key whose mapping is to be removed from the map
	 *
	 * @throws NullPointerException if the specified key is null and this
	 *         map does not permit null keys
	 */
    <T> void remove(TypedKey<T> key);
}
