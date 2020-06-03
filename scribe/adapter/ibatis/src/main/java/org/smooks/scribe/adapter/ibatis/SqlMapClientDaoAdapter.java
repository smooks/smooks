/*-
 * ========================LICENSE_START=================================
 * Scribe :: Ibatis adapter
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
package org.smooks.scribe.adapter.ibatis;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import org.smooks.scribe.DaoException;
import org.smooks.scribe.Locator;
import org.smooks.scribe.MappingDao;

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
	 * @see org.smooks.scribe.NamedDAO#merge(java.lang.String, java.lang.Object)
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
	 * @see org.smooks.scribe.NamedDAO#persist(java.lang.String, java.lang.Object)
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
	 * @see org.smooks.scribe.MappingDao#delete(java.lang.String, java.lang.Object)
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
	 * @see org.smooks.scribe.Finder#findBy(java.lang.String, java.util.Map)
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
	 * @see org.smooks.scribe.Finder#findBy(java.lang.String, java.util.Map)
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
