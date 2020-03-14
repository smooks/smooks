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

import java.util.Collection;
import java.util.Map;

import javax.naming.OperationNotSupportedException;



/**
 * The Locator interface
 * <p>
 * Provides methods for locating entities. A name references the
 * method of how to locate the entity.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public interface Locator {

	/**
	 * Looks up one or more entities.
	 * <p>If one entity is located then the object should directly be returned.
	 * If multiple are located then a '{@link Collection}', containing the entities,
	 * should be returned. If no entities are located then <code>null</code> should
	 * be returned.
	 *
	 * @param name the name that indicates how to do the lookup
	 * @param parameters the array of parameters
	 * @return the located entity, a collection of located entities or null if no
	 * entities are located.
	 * @throws OperationNotSupportedException if the operation is not supported
	 */
	Object lookup(String name, Object ... parameters);

	/**
	 * Looks up one or more entities.
	 * <p>If one entity is located then the object should directly be returned.
	 * If multiple are located then a '{@link Collection}', containing the entities,
	 * should be returned. If no entities are located then <code>null</code> should
	 * be returned.
	 *
	 * @param name the name that indicates how to do the lookup
	 * @param parameters the map of parameters
	 * @return the located entity, a collection of located entities or null if no
	 * entities are located.
	 * @throws OperationNotSupportedException if the operation is not supported
	 */
	Object lookup(String name, Map<String, ?> parameters);

}
