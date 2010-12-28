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
package org.milyn.scribe.invoker;

import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.milyn.scribe.Dao;
import org.milyn.scribe.Flushable;
import org.milyn.scribe.Locator;
import org.milyn.scribe.MappingDao;
import org.milyn.scribe.Queryable;


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
	 * @see org.milyn.scribe.invoker.DAOInvoker#persist(java.lang.Object)
	 */
	public Object insert(final Object obj) {
		if(dao == null) {
			throw new UnsupportedOperationException("The DAO '" + dao.getClass().getName() + "' doesn't implement the '" + Dao.class.getName() + "' interface and there for can't insert the entity.");
		}
		return dao.insert(obj);
	}

	/* (non-Javadoc)
	 * @see org.milyn.scribe.invoker.DaoInvoker#insert(java.lang.String, java.lang.Object)
	 */
	public Object insert(String name, Object obj) {
		if(mappingDao == null) {
			throw new UnsupportedOperationException("The DAO '" + mappingDao.getClass().getName() + "' doesn't implement the '" + MappingDao.class.getName() + "' interface and there for can't insert the entity under the name '"+ name +"'.");
		}
		return mappingDao.insert(name, obj);
	}

	/* (non-Javadoc)
	 * @see org.milyn.scribe.invoker.DAOInvoker#merge(java.lang.Object)
	 */
	public Object update(final Object obj) {
		if(dao == null) {
			throw new UnsupportedOperationException("The DAO '" + dao.getClass().getName() + "' doesn't implement the '" + Dao.class.getName() + "' interface and there for can't insert the entity.");
		}
		return dao.update(obj);
	}

	/* (non-Javadoc)
	 * @see org.milyn.scribe.invoker.DaoInvoker#update(java.lang.String, java.lang.Object)
	 */
	public Object update(String name, Object obj) {
		if(mappingDao == null) {
			throw new UnsupportedOperationException("The DAO '" + mappingDao.getClass().getName() + "' doesn't implement the '" + MappingDao.class.getName() + "' interface and there for can't update the entity under the name '"+ name +"'.");
		}
		return mappingDao.update(name, obj);
	}

	/* (non-Javadoc)
	 * @see org.milyn.scribe.invoker.DaoInvoker#delete(java.lang.String, java.lang.Object[])
	 */
	public Object delete(Object entity) {
		if(dao == null) {
			throw new UnsupportedOperationException("The DAO '" + dao.getClass().getName() + "' doesn't implement the '" + Dao.class.getName() + "' interface and there for can't delete the entity.");
		}
		return dao.delete(entity);
	}

	/* (non-Javadoc)
	 * @see org.milyn.scribe.invoker.DaoInvoker#delete(java.lang.String, java.lang.Object)
	 */
	public Object delete(String name, Object entity) {
		if(mappingDao == null) {
			throw new UnsupportedOperationException("The DAO '" + mappingDao.getClass().getName() + "' doesn't implement the '" + MappingDao.class.getName() + "' interface and there for can't delete the entity under the name '"+ name +"'.");
		}
		return mappingDao.delete(name, entity);
	}


	/* (non-Javadoc)
	 * @see org.milyn.scribe.invoker.DAOInvoker#flush()
	 */
	public void flush() {
		if(flushableDAO == null) {
			throw new UnsupportedOperationException("The DAO '" + dao.getClass().getName() + "' doesn't implement the '" + Flushable.class.getName() + "' interface and there for can't flush the DAO.");
		}
		flushableDAO.flush();
	}

	/* (non-Javadoc)
	 * @see org.milyn.scribe.invoker.DAOInvoker#findByQuery(java.lang.String, java.lang.Object[])
	 */
	public Object lookupByQuery(final String query, final Object ... parameters) {
		if(queryFinderDAO == null) {
			throw new UnsupportedOperationException("The DAO '" + dao.getClass().getName() + "' doesn't implement the '" + Queryable.class.getName() + "' interface and there for can't find by query.");
		}
		return queryFinderDAO.lookupByQuery(query, parameters);
	}

	/* (non-Javadoc)
	 * @see org.milyn.scribe.invoker.DAOInvoker#findByQuery(java.lang.String, java.util.Map)
	 */
	public Object lookupByQuery(final String query, final Map<String, ?> parameters) {
		if(queryFinderDAO == null) {
			throw new UnsupportedOperationException("The DAO '" + dao.getClass().getName() + "' doesn't implement the '" + Queryable.class.getName() + "' interface and there for can't find by query.");
		}
		return queryFinderDAO.lookupByQuery(query, parameters);
	}

	/* (non-Javadoc)
	 * @see org.milyn.scribe.invoker.DAOInvoker#findByQuery(java.lang.String, java.lang.Object[])
	 */
	public Object lookup(final String name, final Object ... parameters) {
		if(finderDAO == null) {
			throw new UnsupportedOperationException("The DAO '" + dao.getClass().getName() + "' doesn't implement the '" + Locator.class.getName() + "' interface and there for can't find by query.");
		}
		return finderDAO.lookup(name, parameters);
	}

	/* (non-Javadoc)
	 * @see org.milyn.scribe.invoker.DAOInvoker#findByQuery(java.lang.String, java.util.Map)
	 */
	public Object lookup(final String name, final Map<String, ?> parameters) {
		if(finderDAO == null) {
			throw new UnsupportedOperationException("The DAO '" + dao.getClass().getName() + "' doesn't implement the '" + Locator.class.getName() + "' interface and there for can't find by query.");
		}
		return finderDAO.lookup(name, parameters);
	}


}
