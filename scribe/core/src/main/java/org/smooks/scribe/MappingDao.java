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
