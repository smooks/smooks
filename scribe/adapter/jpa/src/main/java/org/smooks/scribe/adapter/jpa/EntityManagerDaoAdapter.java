/*-
 * ========================LICENSE_START=================================
 * Scribe :: JPA Adapter
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
package org.smooks.scribe.adapter.jpa;

import java.util.Collection;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.smooks.assertion.AssertArgument;
import org.smooks.scribe.Dao;
import org.smooks.scribe.Flushable;
import org.smooks.scribe.Locator;
import org.smooks.scribe.Queryable;


/**
 * This is an adapter for the EntityManager. This enables
 * simple queries on a EntityManager.<br>
 * <br>
 * Prefixing a query with a @ makes sure that
 * the query is handled as a named query. The @
 * is off course removed before the named query is called
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
class EntityManagerDaoAdapter implements Dao<Object>, Locator, Queryable, Flushable {

	private final EntityManager entityManager;

	/**
	 * @param entityManager
	 */
	public EntityManagerDaoAdapter(final EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.DAO#flush()
	 */
	@Override
	public void flush() {
		entityManager.flush();
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.DAO#merge(java.lang.Object)
	 */
	@Override
	public Object update(final Object entity) {
		AssertArgument.isNotNull(entity, "entity");

		return entityManager.merge(entity);
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.DAO#persist(java.lang.Object)
	 */
	@Override
	public Object insert(final Object entity) {
		AssertArgument.isNotNull(entity, "entity");

		entityManager.persist(entity);

		return null;
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.Dao#delete(java.lang.Object)
	 */
	@Override
	public Object delete(Object entity) {
		AssertArgument.isNotNull(entity, "entity");

		entityManager.remove(entity);

		return null;
	}


	/* (non-Javadoc)
	 * @see org.smooks.scribe.Finder#findBy(java.lang.String, java.util.Map)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Collection<Object> lookup(final String name, final Object ... parameters) {
		AssertArgument.isNotNullAndNotEmpty(name, "name");
		AssertArgument.isNotNull(parameters, "parameters");

		final Query emQuery = entityManager.createNamedQuery(name);

		for(int i = 0; i < parameters.length; i++) {

			emQuery.setParameter(i+1, parameters[i]);

		}

		return emQuery.getResultList();
	}


	/* (non-Javadoc)
	 * @see org.smooks.scribe.Finder#findBy(java.lang.String, java.util.Map)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Collection<Object> lookup(final String name, final Map<String, ?> parameters) {
		AssertArgument.isNotNullAndNotEmpty(name, "name");
		AssertArgument.isNotNull(parameters, "parameters");

		final Query emQuery = entityManager.createNamedQuery(name);

		for(final String key : parameters.keySet()) {

			emQuery.setParameter(key, parameters.get(key));

		}
		return emQuery.getResultList();
	}


	/* (non-Javadoc)
	 * @see org.smooks.scribe.QueryFinder#findByQuery(java.lang.String, java.util.List)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Collection<Object> lookupByQuery(final String query, final Object ... parameters) {
		AssertArgument.isNotNullAndNotEmpty(query, "query");
		AssertArgument.isNotNull(parameters, "parameters");

		// Is this useful?
		if(query.startsWith("@")) {
			return lookup(query.substring(1), parameters);
		}

		final Query emQuery = entityManager.createQuery(query);

		for(int i = 0; i < parameters.length; i++) {

			emQuery.setParameter(i+1, parameters[i]);

		}

		return emQuery.getResultList();
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.QueryFinder#findByQuery(java.lang.String, java.util.Map)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Collection<Object> lookupByQuery(final String query, final Map<String, ?> parameters) {
		AssertArgument.isNotNullAndNotEmpty(query, "query");
		AssertArgument.isNotNull(parameters, "parameters");

		// Is this useful?
		if(query.startsWith("@")) {
			return lookup(query.substring(1), parameters);
		}

		final Query emQuery = entityManager.createQuery(query);

		for(final String key : parameters.keySet()) {

			emQuery.setParameter(key, parameters.get(key));

		}
		return emQuery.getResultList();
	}

	/**
	 * @return the entityManager
	 */
	public EntityManager getEntityManager() {
		return entityManager;
	}



}
