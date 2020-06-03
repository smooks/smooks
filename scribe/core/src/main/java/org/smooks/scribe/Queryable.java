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

import java.util.Collection;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

/**
 * The queryable interface
 * <p>
 * Provides methods for locating entities via a query. The syntax
 * of the query depends on the implementation.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
public interface Queryable {

	/**
	 * Looks up one or more entities via a query string.
	 * <p>If one entity is located then the object should directly be returned.
	 * If multiple are located then a '{@link Collection}', containing the entities,
	 * should be returned. If no entities are located then <code>null</code> should
	 * be returned.
	 *
	 * @param query the query
	 * @param parameters the array of parameters
	 * @return the located entity, a collection of located entities or null if no
	 * entities are located.
	 * @throws OperationNotSupportedException if the operation is not supported
	 */
	Object lookupByQuery(String query, Object ... parameters);

	/**
	 * Looks up one or more entities via a query string.
	 * <p>If one entity is located then the object should directly be returned.
	 * If multiple are located then a '{@link Collection}', containing the entities,
	 * should be returned. If no entities are located then <code>null</code> should
	 * be returned.
	 *
	 * @param query the query
	 * @param parameters the map of parameters
	 * @return the located entity, a collection of located entities or null if no
	 * entities are located.
	 * @throws OperationNotSupportedException if the operation is not supported
	 */
	Object lookupByQuery(String query, Map<String, ?> parameters);

}
