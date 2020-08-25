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
package org.smooks.scribe;

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
    void set(Object key, Object value);

	/**
	 * Returns the object bound with the specified name in this {@link ObjectStore}
	 * implementation, or null if no object is bound under the name.
     * @param key The key against which the object is bound; cannot be null.
	 * @return The object bound with the specified name in this {@link ObjectStore}
	 * implementation, or null if no object is bound under the name.
	 */
    Object get(Object key);

	/**
	 * Returns the Map of attributes bound in this {@link ObjectStore}
	 * @return Map of all objects bound in this {@link ObjectStore}
	 */
    Map<Object, Object> getAll();

	/**
	 * Removes the object bound with the specified name from this {@link ObjectStore}
	 * implementation. If the {@link ObjectStoree} implementation does
	 * not have an object bound with the specified name, this method does nothing.
     * @param key The key against which the object is bound; cannot be null.
	 */
    void remove(Object key);
}
