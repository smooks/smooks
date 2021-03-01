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

import org.junit.Test;
import org.mockito.Mock;
import org.smooks.scribe.NoMethodWithAnnotationFoundException;
import org.smooks.scribe.reflection.AnnotatedDaoRuntimeInfo;
import org.smooks.scribe.reflection.AnnotatedDaoRuntimeInfoFactory;
import org.smooks.scribe.test.dao.AnnotatedDaoNoEntityReturned;
import org.smooks.scribe.test.dao.FullAnnotatedDao;
import org.smooks.scribe.test.dao.MinimumAnnotatedDao;
import org.smooks.scribe.test.dao.OnlyDefaultAnnotatedDao;
import org.smooks.scribe.test.util.BaseTestCase;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class AnnotatedDaoInvokerTestCase extends BaseTestCase {

	@Mock
	FullAnnotatedDao fullDao;

	@Mock
	OnlyDefaultAnnotatedDao onlyDefaultAnnotatedDao;

	@Mock
	AnnotatedDaoNoEntityReturned daoNoEntityReturned;

	@Mock
	MinimumAnnotatedDao minimumDao;

	final AnnotatedDaoRuntimeInfoFactory runtimeInfoFactory = new AnnotatedDaoRuntimeInfoFactory();

	final AnnotatedDaoRuntimeInfo fullDaoRuntimeInfo = runtimeInfoFactory.create(FullAnnotatedDao.class);

	final AnnotatedDaoRuntimeInfo daoNoEntityReturnedRuntimeInfo = runtimeInfoFactory.create(AnnotatedDaoNoEntityReturned.class);

	final AnnotatedDaoRuntimeInfo minimumDaoRuntimeInfo = runtimeInfoFactory.create(MinimumAnnotatedDao.class);

	@Test
	public void test_insert_with_entity_return() {

		DaoInvoker invoker = new AnnotatedDaoInvoker(fullDao, fullDaoRuntimeInfo);

		Object toPersist = new Object();

		Object expectedResult = new Object();

		when(fullDao.insertIt(toPersist)).thenReturn(expectedResult);

		Object result = invoker.insert(toPersist);

		verify(fullDao).insertIt(same(toPersist));

		assertSame(expectedResult, result);
	}

	@Test
	public void test_insert_with_null_return() {

		DaoInvoker invoker = new AnnotatedDaoInvoker(fullDao, fullDaoRuntimeInfo);

		Object toPersist = new Object();

		when(fullDao.insertIt(toPersist)).thenReturn(null);

		Object result = invoker.insert(toPersist);

		verify(fullDao).insertIt(same(toPersist));

		assertNull(result);
	}

	@Test
	public void test_insert_with_named_method() {

		DaoInvoker invoker = new AnnotatedDaoInvoker(fullDao, fullDaoRuntimeInfo);

		Object toPersist = new Object();

		invoker.insert("insertIt", toPersist);
		invoker.insert("insertIt2", toPersist);
		invoker.insert("insertIt3", toPersist);

		verify(fullDao).insertIt(same(toPersist));
		verify(fullDao).insertIt2(same(toPersist));
		verify(fullDao).insertItDiff(same(toPersist));
	}

	@Test
	public void test_insert_noEntityReturned() {

		DaoInvoker invoker = new AnnotatedDaoInvoker(daoNoEntityReturned, daoNoEntityReturnedRuntimeInfo);

		Object toPersist = new Object();

		when(daoNoEntityReturned.persistIt(toPersist)).thenReturn(toPersist);

		Object result = invoker.insert(toPersist);

		verify(daoNoEntityReturned).persistIt(same(toPersist));

		assertNull(result);
	}

	@Test(expected = NoMethodWithAnnotationFoundException.class)
	public void test_insert_no_annotation() {

		DaoInvoker invoker = new AnnotatedDaoInvoker(minimumDao, minimumDaoRuntimeInfo);

		Object toPersist = new Object();

		invoker.insert(toPersist);


	}

	@Test
	public void test_update_with_entity_return() {


		DaoInvoker invoker = new AnnotatedDaoInvoker(fullDao, fullDaoRuntimeInfo);

		Object toUpdate = new Object();

		Object expectedResult = new Object();

		when(fullDao.updateIt(toUpdate)).thenReturn(expectedResult);

		Object result = invoker.update(toUpdate);

		verify(fullDao).updateIt(same(toUpdate));

		assertSame(expectedResult, result);

	}

	@Test
	public void test_update_with_null_return() {

		DaoInvoker invoker = new AnnotatedDaoInvoker(fullDao, fullDaoRuntimeInfo);

		Object toUpdate = new Object();

		when(fullDao.updateIt(toUpdate)).thenReturn(null);

		Object result = invoker.update(toUpdate);

		verify(fullDao).updateIt(same(toUpdate));

		assertNull(result);
	}

	@Test
	public void test_update_with_named_method() {

		DaoInvoker invoker = new AnnotatedDaoInvoker(fullDao, fullDaoRuntimeInfo);

		Object toUpdate = new Object();

		invoker.update("updateIt", toUpdate);
		invoker.update("updateIt2", toUpdate);
		invoker.update("updateIt3", toUpdate);

		verify(fullDao).updateIt(same(toUpdate));
		verify(fullDao).updateIt2(same(toUpdate));
		verify(fullDao).updateItDiff(same(toUpdate));
	}

	@Test(expected = NoMethodWithAnnotationFoundException.class)
	public void test_update_no_annotation() {


		DaoInvoker invoker = new AnnotatedDaoInvoker(minimumDao, minimumDaoRuntimeInfo);

		Object toMerge = new Object();

		invoker.update(toMerge);

	}

	@Test
	public void test_update_noEntityReturned() {

		DaoInvoker invoker = new AnnotatedDaoInvoker(daoNoEntityReturned, daoNoEntityReturnedRuntimeInfo);

		Object toDelete = new Object();

		when(daoNoEntityReturned.deleteIt(toDelete)).thenReturn(toDelete);

		Object result = invoker.delete(toDelete);

		verify(daoNoEntityReturned).deleteIt(same(toDelete));

		assertNull(result);
	}

	@Test
	public void test_delete_with_entity_return() {


		DaoInvoker invoker = new AnnotatedDaoInvoker(fullDao, fullDaoRuntimeInfo);

		Object toDelete = new Object();

		Object expectedResult = new Object();

		when(fullDao.deleteIt(toDelete)).thenReturn(expectedResult);

		Object result = invoker.delete(toDelete);

		verify(fullDao).deleteIt(same(toDelete));

		assertSame(expectedResult, result);

	}

	@Test
	public void test_delete_with_null_return() {

		DaoInvoker invoker = new AnnotatedDaoInvoker(fullDao, fullDaoRuntimeInfo);

		Object toDelete = new Object();

		when(fullDao.deleteIt(toDelete)).thenReturn(null);

		Object result = invoker.delete(toDelete);

		verify(fullDao).deleteIt(same(toDelete));

		assertNull(result);
	}

	@Test
	public void test_delete_with_named_method() {

		DaoInvoker invoker = new AnnotatedDaoInvoker(fullDao, fullDaoRuntimeInfo);

		Object toDelete = new Object();

		invoker.delete("deleteIt", toDelete);
		invoker.delete("deleteIt2", toDelete);
		invoker.delete("deleteIt3", toDelete);

		verify(fullDao).deleteIt(same(toDelete));
		verify(fullDao).deleteIt2(same(toDelete));
		verify(fullDao).deleteItDiff(same(toDelete));
	}

	@Test(expected = NoMethodWithAnnotationFoundException.class)
	public void test_delete_no_annotation() {


		DaoInvoker invoker = new AnnotatedDaoInvoker(minimumDao, minimumDaoRuntimeInfo);

		Object toDelete = new Object();

		invoker.delete(toDelete);

	}

	@Test
	public void test_delete_noEntityReturned() {

		DaoInvoker invoker = new AnnotatedDaoInvoker(daoNoEntityReturned, daoNoEntityReturnedRuntimeInfo);

		Object toDelete = new Object();

		when(daoNoEntityReturned.deleteIt(toDelete)).thenReturn(toDelete);

		Object result = invoker.delete(toDelete);

		verify(daoNoEntityReturned).deleteIt(same(toDelete));

		assertNull(result);
	}

	@Test
	public void test_flush() {

		DaoInvoker invoker = new AnnotatedDaoInvoker(fullDao, fullDaoRuntimeInfo);

		invoker.flush();

		verify(fullDao).flushIt();

	}

	@Test(expected = NoMethodWithAnnotationFoundException.class)
	public void test_flush_no_annotation() {

		DaoInvoker invoker = new AnnotatedDaoInvoker(minimumDao, minimumDaoRuntimeInfo);

		invoker.flush();

	}

	@Test
	public void test_lookup() {

		DaoInvoker invoker = new AnnotatedDaoInvoker(fullDao, fullDaoRuntimeInfo);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", 1L);

		invoker.lookup("id", params);

		verify(fullDao).findById(1L);

	}

	@Test
	public void test_lookup_with_method_name() {

		DaoInvoker invoker = new AnnotatedDaoInvoker(fullDao, fullDaoRuntimeInfo);

		invoker.lookup("findBy", "param");

		verify(fullDao).findBy(eq("param"));
	}

	@Test
	public void test_lookup_with_name_param() {

		DaoInvoker invoker = new AnnotatedDaoInvoker(fullDao, fullDaoRuntimeInfo);

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("first", "henk");
		param.put("last", "janssen");

		invoker.lookup("name", param);

		verify(fullDao).findByName(eq("janssen"), eq("henk"));
	}

	@Test
	public void test_lookup_with_positional_param() {

		DaoInvoker invoker = new AnnotatedDaoInvoker(fullDao, fullDaoRuntimeInfo);

		invoker.lookup("positional", "param1", 2, true);

		verify(fullDao).findBySomething(eq("param1"), eq(2), eq(true));
	}


	@Test(expected = NoMethodWithAnnotationFoundException.class)
	public void test_lookup_no_annotation() {

		DaoInvoker invoker = new AnnotatedDaoInvoker(minimumDao, minimumDaoRuntimeInfo);

		invoker.lookup("id", Collections.emptyMap());

	}

	@Test
	public void test_lookupByQuery_map_params() {

		DaoInvoker invoker = new AnnotatedDaoInvoker(fullDao, fullDaoRuntimeInfo);

		Map<String, Object> params = new HashMap<String, Object>();

		invoker.lookupByQuery("query", params);

		verify(fullDao).findByQuery(eq("query"), same(params));

	}

	@Test
	public void test_lookupByQuery_array_params() {

		DaoInvoker invoker = new AnnotatedDaoInvoker(fullDao, fullDaoRuntimeInfo);

		Object[] params = new Object[0];

		invoker.lookupByQuery("query", params);

		verify(fullDao).findByQuery(eq("query"), same(params));

	}

	@Test(expected = NoMethodWithAnnotationFoundException.class)
	public void test_lookupByQuery_non_query_finder_dao_map_params() {

		DaoInvoker invoker = new AnnotatedDaoInvoker(minimumDao, minimumDaoRuntimeInfo);

		Map<String, Object> params = new HashMap<String, Object>();

		invoker.lookupByQuery("id", params);

	}

	@Test(expected = NoMethodWithAnnotationFoundException.class)
	public void test_lookupByQuery_non_query_finder_dao_array_params() {

		DaoInvoker invoker = new AnnotatedDaoInvoker(minimumDao, minimumDaoRuntimeInfo);

		Object[] params = new Object[0];

		invoker.lookupByQuery("id", params);

	}
}