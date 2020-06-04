/*-
 * ========================LICENSE_START=================================
 * Scribe :: Hibernate adapter
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
package org.smooks.scribe.adapter.hibernate;

import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Query;
import org.hibernate.Session;
import org.smooks.assertion.AssertArgument;
import org.smooks.scribe.Dao;
import org.smooks.scribe.Flushable;
import org.smooks.scribe.Locator;
import org.smooks.scribe.Queryable;


/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
class SessionDaoAdapter implements Dao<Object>, Locator, Queryable, Flushable {

	private final Session session;

	/**
	 *
	 */
	public SessionDaoAdapter(final Session session) {
		AssertArgument.isNotNull(session, "session");

		this.session = session;
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.DAO#flush()
	 */
	public void flush() {
		session.flush();
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.DAO#merge(java.lang.Object)
	 */
	public Object update(final Object entity) {
		AssertArgument.isNotNull(entity, "entity");

		session.update(entity);

		return entity;
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.DAO#persist(java.lang.Object)
	 */
	public Object insert(final Object entity) {
		AssertArgument.isNotNull(entity, "entity");

		session.save(entity);

		return null;
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.Dao#delete(java.lang.Object)
	 */
	public Object delete(Object entity) {
		AssertArgument.isNotNull(entity, "entity");

		session.delete(entity);

		return null;
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.Finder#findBy(java.lang.String, java.lang.Object[])
	 */
	public Object lookup(final String name, final Object ... parameters) {

		AssertArgument.isNotNullAndNotEmpty(name, "name");
		AssertArgument.isNotNull(parameters, "parameters");

		final Query query = session.getNamedQuery(name);

		for(int i = 0; i < parameters.length; i++) {

			query.setParameter(i+1, parameters[i]);

		}

		return query.list();

	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.Finder#findBy(java.lang.String, java.util.Map)
	 */
	public Object lookup(final String name, final Map<String, ?> parameters) {
		AssertArgument.isNotNullAndNotEmpty(name, "name");
		AssertArgument.isNotNull(parameters, "parameters");

		final Query query = session.getNamedQuery(name);

		for(Entry<String, ?> entry : parameters.entrySet()) {

			query.setParameter(entry.getKey(), entry.getValue());

		}
		return query.list();
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.QueryFinder#findByQuery(java.lang.String, java.lang.Object[])
	 */
	public Object lookupByQuery(final String query, final Object ... parameters) {
		AssertArgument.isNotNullAndNotEmpty(query, "query");
		AssertArgument.isNotNull(parameters, "parameters");

		// Is this useful?
		if(query.startsWith("@")) {
			return lookup(query.substring(1), parameters);
		}

		final Query sesQuery = session.createQuery(query);

		for(int i = 0; i < parameters.length; i++) {

			sesQuery.setParameter(i+1, parameters[i]);

		}

		return sesQuery.list();
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.QueryFinder#findByQuery(java.lang.String, java.util.Map)
	 */
	public Object lookupByQuery(final String query,
			final Map<String, ?> parameters) {

		AssertArgument.isNotNullAndNotEmpty(query, "query");
		AssertArgument.isNotNull(parameters, "parameters");

		// Is this useful?
		if(query.startsWith("@")) {
			return lookup(query.substring(1), parameters);
		}

		final Query sesQuery = session.createQuery(query);

		for(Entry<String, ?> entry : parameters.entrySet()) {

			sesQuery.setParameter(entry.getKey(), entry.getValue());

		}
		return sesQuery.list();
	}

	/**
	 * @return the session
	 */
	public Session getSession() {
		return session;
	}



}
