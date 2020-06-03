/*-
 * ========================LICENSE_START=================================
 * Scribe :: Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0 or,
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

/**
 * The Mapping DAO interface
 * <p>
 * Provides the most basic DAO operations to manipulate a data source.
 * The difference to the {@link Dao} interface is that this
 * intefaces adds a extra id parameter to each method. This
 * id determines how the implementation should execute the
 * operation.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public interface MappingDao<E> {

	/**
	 * Inserts the entity instance in to the datasource
	 *
	 * @param id The id
	 * @param entity The entity object to insert
	 * @return The inserted entity. The Dao should only return an entity that represents the
	 *         inserted entity but is a different object then the one that was used as the parameter.
	 *         If this is not the case then NULL should be returned.
	 * @throws UnsupportedOperationException Indicates that this Dao doesn't support
	 *         the insert operation.
	 */
	E insert(String id, E entity);

	/**
	 * Updates the entity instance in the datasource
	 *
 	 * @param id The id
	 * @param entity The entity object to update
	 * @return The updated entity. The Dao should only return an entity that represents the
	 *         updated entity but is a different object then the one that was used as the parameter.
	 *         If this is not the case then NULL should be returned.
	 * @throws UnsupportedOperationException Indicates that this Dao doesn't support
	 *         the update operation.
	 */
	E update(String id, E entity);

	/**
	 * Deletes the entity instance from the datasource
	 *
	 * @param id The id
	 * @param entity The entity object to delete
	 * @return The deleted entity. The Dao should only return an entity that represents the
	 *         deleted entity but is a different object then the one that was used as the parameter.
	 *         If this is not the case then NULL should be returned.
	 * @throws UnsupportedOperationException Indicates that this Dao doesn't support
	 *         the delete operation.
	 */
	E delete(String id, E entity);

}
