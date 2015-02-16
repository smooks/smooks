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

import com.ibatis.sqlmap.client.SqlMapClient;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import org.milyn.scribe.adapter.ibatis.test.util.BaseTestCase;
import org.mockito.Mock;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
public class SqlMapClientRegisterTest extends BaseTestCase {

    @Mock
    SqlMapClient sqlMapClient;

    @Test(groups = "unit")
    public void test_getDao() {

        SqlMapClientRegister register = new SqlMapClientRegister(sqlMapClient);

        SqlMapClientDaoAdapter entityManagerDaoAdapter = register.getDefaultDao();

        assertNotNull(entityManagerDaoAdapter);

        assertSame(sqlMapClient, entityManagerDaoAdapter.getSqlMapClient());

    }

}
