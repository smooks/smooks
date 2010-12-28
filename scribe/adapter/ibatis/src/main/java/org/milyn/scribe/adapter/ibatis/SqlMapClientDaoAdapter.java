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
package org.milyn.scribe.adapter.ibatis;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import org.milyn.scribe.DaoException;
import org.milyn.scribe.Locator;
import org.milyn.scribe.MappingDao;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
class SqlMapClientDaoAdapter implements MappingDao<Object>, Locator  {

	private final SqlMapClient sqlMapClient;

	/**
	 * @param sqlMapClient
	 */
	public SqlMapClientDaoAdapter(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}

	/* (non-Javadoc)
	 * @see org.milyn.scribe.NamedDAO#merge(java.lang.String, java.lang.Object)
	 */
	public Object update(String id, Object entity) {
		try {
			sqlMapClient.update(id, entity);
		} catch (SQLException e) {
			throw new DaoException("Exception throw while executing update with statement id '" + id + "' and entity '" + entity + "'", e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.milyn.scribe.NamedDAO#persist(java.lang.String, java.lang.Object)
	 */
	public Object insert(String id, Object entity) {
		try {
			sqlMapClient.insert(id, entity);
		} catch (SQLException e) {
			throw new DaoException("Exception throw while executing insert with statement id '" + id + "' and entity '" + entity + "'", e);
		}

		return null;
	}


	/* (non-Javadoc)
	 * @see org.milyn.scribe.MappingDao#delete(java.lang.String, java.lang.Object)
	 */
	public Object delete(String id, Object entity) {
		try {
			sqlMapClient.delete(id, entity);
		} catch (SQLException e) {
			throw new DaoException("Exception throw while executing delete with statement id '" + id + "' and entity '" + entity + "'", e);
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.milyn.scribe.Finder#findBy(java.lang.String, java.util.Map)
	 */
	@SuppressWarnings("unchecked")
	public Collection<Object> lookup(String id, Map<String, ?> parameters) {
		try {
			return sqlMapClient.queryForList(id, parameters);
		} catch (SQLException e) {
			throw new DaoException("Exception throw while executing query with statement id '" + id + "' and parameters '" + parameters + "'", e);
		}
	}

	/* (non-Javadoc)
	 * @see org.milyn.scribe.Finder#findBy(java.lang.String, java.util.Map)
	 */
	@SuppressWarnings("unchecked")
	public Collection<Object> lookup(String id, Object ... parameters) {
		try {
			return sqlMapClient.queryForList(id, parameters);
		} catch (SQLException e) {
			throw new DaoException("Exception throw while executing query with statement id '" + id + "' and parameters '" + parameters + "'", e);
		}
	}

	/**
	 * @return the sqlMapClient
	 */
	public SqlMapClient getSqlMapClient() {
		return sqlMapClient;
	}


}
