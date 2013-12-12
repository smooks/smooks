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

import java.util.Map;

import org.milyn.commons.assertion.AssertArgument;
import org.milyn.scribe.register.AbstractDaoAdapterRegister;
import org.milyn.scribe.register.AbstractDaoRegister;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class SqlMapClientRegister extends AbstractDaoAdapterRegister<SqlMapClientDaoAdapter, SqlMapClient>{

	/**
	 *
	 */
	public SqlMapClientRegister(final SqlMapClient sqlMapClient) {
		super(sqlMapClient);
	}

	/**
	 *
	 */
	public SqlMapClientRegister(final SqlMapClient sqlMapClient, Map<String, ? extends SqlMapClient> sqlMapClientMap) {
		super(sqlMapClient, sqlMapClientMap);
	}


	public SqlMapClientRegister(Map<String, ? extends SqlMapClient> sqlMapClientMap) {
		super(sqlMapClientMap);
	}

	/* (non-Javadoc)
	 * @see org.milyn.scribe.register.AbstractDaoAdapterRegister#createAdapter(java.lang.Object)
	 */
	@Override
	protected SqlMapClientDaoAdapter createAdapter(SqlMapClient sqlMapClient) {
		return new SqlMapClientDaoAdapter(sqlMapClient);
	}

}
