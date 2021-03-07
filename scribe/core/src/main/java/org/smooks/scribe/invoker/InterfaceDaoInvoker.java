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
package org.smooks.scribe.invoker;

import java.util.Map;

import org.smooks.scribe.Dao;
import org.smooks.scribe.Flushable;
import org.smooks.scribe.Locator;
import org.smooks.scribe.MappingDao;
import org.smooks.scribe.Queryable;


/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class InterfaceDaoInvoker implements DaoInvoker  {

	private final Dao<Object> dao;

	private final MappingDao<Object> mappingDao;

	private final Queryable  queryFinderDAO;

	private final Locator  finderDAO;

	private final Flushable flushableDAO;

	/**
	 *
	 */
	@SuppressWarnings("unchecked")
	InterfaceDaoInvoker(final Object dao) {
		this.dao = dao instanceof Dao ? (Dao<Object>) dao : null;
		mappingDao = dao instanceof MappingDao ? (MappingDao<Object>) dao : null;
		queryFinderDAO = dao instanceof Queryable ? (Queryable) dao : null;
		finderDAO = dao instanceof Locator ? (Locator) dao : null;
		flushableDAO = dao instanceof Flushable ? (Flushable) dao : null;
	}


	/* (non-Javadoc)
	 * @see org.smooks.scribe.invoker.DAOInvoker#persist(java.lang.Object)
	 */
	@Override
	public Object insert(final Object obj) {
		if(dao == null) {
			throw new UnsupportedOperationException("The DAO '" + dao.getClass().getName() + "' doesn't implement the '" + Dao.class.getName() + "' interface and there for can't insert the entity.");
		}
		return dao.insert(obj);
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.invoker.DaoInvoker#insert(java.lang.String, java.lang.Object)
	 */
	@Override
	public Object insert(String name, Object obj) {
		if(mappingDao == null) {
			throw new UnsupportedOperationException("The DAO '" + mappingDao.getClass().getName() + "' doesn't implement the '" + MappingDao.class.getName() + "' interface and there for can't insert the entity under the name '"+ name +"'.");
		}
		return mappingDao.insert(name, obj);
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.invoker.DAOInvoker#merge(java.lang.Object)
	 */
	@Override
	public Object update(final Object obj) {
		if(dao == null) {
			throw new UnsupportedOperationException("The DAO '" + dao.getClass().getName() + "' doesn't implement the '" + Dao.class.getName() + "' interface and there for can't insert the entity.");
		}
		return dao.update(obj);
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.invoker.DaoInvoker#update(java.lang.String, java.lang.Object)
	 */
	@Override
	public Object update(String name, Object obj) {
		if(mappingDao == null) {
			throw new UnsupportedOperationException("The DAO '" + mappingDao.getClass().getName() + "' doesn't implement the '" + MappingDao.class.getName() + "' interface and there for can't update the entity under the name '"+ name +"'.");
		}
		return mappingDao.update(name, obj);
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.invoker.DaoInvoker#delete(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Object delete(Object entity) {
		if(dao == null) {
			throw new UnsupportedOperationException("The DAO '" + dao.getClass().getName() + "' doesn't implement the '" + Dao.class.getName() + "' interface and there for can't delete the entity.");
		}
		return dao.delete(entity);
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.invoker.DaoInvoker#delete(java.lang.String, java.lang.Object)
	 */
	@Override
	public Object delete(String name, Object entity) {
		if(mappingDao == null) {
			throw new UnsupportedOperationException("The DAO '" + mappingDao.getClass().getName() + "' doesn't implement the '" + MappingDao.class.getName() + "' interface and there for can't delete the entity under the name '"+ name +"'.");
		}
		return mappingDao.delete(name, entity);
	}


	/* (non-Javadoc)
	 * @see org.smooks.scribe.invoker.DAOInvoker#flush()
	 */
	@Override
	public void flush() {
		if(flushableDAO == null) {
			throw new UnsupportedOperationException("The DAO '" + dao.getClass().getName() + "' doesn't implement the '" + Flushable.class.getName() + "' interface and there for can't flush the DAO.");
		}
		flushableDAO.flush();
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.invoker.DAOInvoker#findByQuery(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Object lookupByQuery(final String query, final Object ... parameters) {
		if(queryFinderDAO == null) {
			throw new UnsupportedOperationException("The DAO '" + dao.getClass().getName() + "' doesn't implement the '" + Queryable.class.getName() + "' interface and there for can't find by query.");
		}
		return queryFinderDAO.lookupByQuery(query, parameters);
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.invoker.DAOInvoker#findByQuery(java.lang.String, java.util.Map)
	 */
	@Override
	public Object lookupByQuery(final String query, final Map<String, ?> parameters) {
		if(queryFinderDAO == null) {
			throw new UnsupportedOperationException("The DAO '" + dao.getClass().getName() + "' doesn't implement the '" + Queryable.class.getName() + "' interface and there for can't find by query.");
		}
		return queryFinderDAO.lookupByQuery(query, parameters);
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.invoker.DAOInvoker#findByQuery(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Object lookup(final String name, final Object ... parameters) {
		if(finderDAO == null) {
			throw new UnsupportedOperationException("The DAO '" + dao.getClass().getName() + "' doesn't implement the '" + Locator.class.getName() + "' interface and there for can't find by query.");
		}
		return finderDAO.lookup(name, parameters);
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.invoker.DAOInvoker#findByQuery(java.lang.String, java.util.Map)
	 */
	@Override
	public Object lookup(final String name, final Map<String, ?> parameters) {
		if(finderDAO == null) {
			throw new UnsupportedOperationException("The DAO '" + dao.getClass().getName() + "' doesn't implement the '" + Locator.class.getName() + "' interface and there for can't find by query.");
		}
		return finderDAO.lookup(name, parameters);
	}


}
