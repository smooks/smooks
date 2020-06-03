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
package org.smooks.scribe.invoker;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.smooks.scribe.Dao;
import org.smooks.scribe.test.dao.FullInterfaceDao;
import org.smooks.scribe.test.util.BaseTestCase;
import org.mockito.Mock;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
@Test(groups = "unit")
public class InterfaceDaoInvokerTest extends BaseTestCase {

	@Mock
	private FullInterfaceDao<Object> fullDao;

	@Mock
	private Dao<Object> minimumDao;

	@Test(groups = "unit")
	public void test_insert() {

		DaoInvoker invoker = new InterfaceDaoInvoker(fullDao);

		Object toPersist = new Object();

		invoker.insert(toPersist);

		verify(fullDao).insert(same(toPersist));

	}

	@Test
	public void test_insert_named() {

		DaoInvoker invoker = new InterfaceDaoInvoker(fullDao);

		Object toPersist = new Object();

		invoker.insert("myInsert", toPersist);

		verify(fullDao).insert(eq("myInsert"), same(toPersist));

	}

	@Test
	public void test_update() {


		DaoInvoker invoker = new InterfaceDaoInvoker(fullDao);

		Object toMerge = new Object();

		invoker.update(toMerge);

		verify(fullDao).update(same(toMerge));

	}


	@Test
	public void test_update_named() {


		DaoInvoker invoker = new InterfaceDaoInvoker(fullDao);

		Object toMerge = new Object();

		invoker.update("myMerge", toMerge);

		verify(fullDao).update(eq("myMerge") ,same(toMerge));

	}

	@Test
	public void test_delete() {


		DaoInvoker invoker = new InterfaceDaoInvoker(fullDao);

		Object toDelete = new Object();

		invoker.delete(toDelete);

		verify(fullDao).delete(same(toDelete));

	}

	@Test
	public void test_delete_named() {


		DaoInvoker invoker = new InterfaceDaoInvoker(fullDao);

		Object toDelete = new Object();

		invoker.delete("myDelete", toDelete);

		verify(fullDao).delete(eq("myDelete"), same(toDelete));

	}

	@Test
	public void test_flush() {

		DaoInvoker invoker = new InterfaceDaoInvoker(fullDao);

		invoker.flush();

		verify(fullDao).flush();

	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void test_flush_non_flushable_dao() {

		DaoInvoker invoker = new InterfaceDaoInvoker(minimumDao);

		invoker.flush();

	}

	@Test
	public void test_lookup() {

		DaoInvoker invoker = new InterfaceDaoInvoker(fullDao);

		Map<String, Object> params = new HashMap<String, Object>();

		invoker.lookup("id", params);

		verify(fullDao).lookup(eq("id"), same(params));

	}


	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void test_findBy_non_finder_dao() {

		DaoInvoker invoker = new InterfaceDaoInvoker(minimumDao);

		Map<String, Object> params = new HashMap<String, Object>();

		invoker.lookup("id", params);

	}

	@Test
	public void test_lookupByQuery_map_params() {

		DaoInvoker invoker = new InterfaceDaoInvoker(fullDao);

		Map<String, Object> params = new HashMap<String, Object>();

		invoker.lookupByQuery("query", params);

		verify(fullDao).lookupByQuery(eq("query"), same(params));

	}

	@Test
	public void test_lookupByQuery_array_params() {

		DaoInvoker invoker = new InterfaceDaoInvoker(fullDao);

		invoker.lookupByQuery("query", "test", "test2");

		verify(fullDao).lookupByQuery(eq("query"), eq("test"), eq("test2"));

	}


	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void test_lookupByQuery_non_query_finder_dao_map_params() {

		DaoInvoker invoker = new InterfaceDaoInvoker(minimumDao);

		Map<String, Object> params = new HashMap<String, Object>();

		invoker.lookupByQuery("id", params);

	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void test_lookupByQuery_non_query_finder_dao_array_params() {

		DaoInvoker invoker = new InterfaceDaoInvoker(minimumDao);

		invoker.lookupByQuery("id", "test", "test2");

	}

}
