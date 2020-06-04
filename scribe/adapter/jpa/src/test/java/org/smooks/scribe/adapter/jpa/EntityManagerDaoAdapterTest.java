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

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.smooks.scribe.adapter.jpa.test.util.BaseTestCase;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class EntityManagerDaoAdapterTest extends BaseTestCase {

	@Mock
	private EntityManager entityManager;

	@Mock
	private Query query;

	private EntityManagerDaoAdapter adapter;

	@Test( groups = "unit" )
	public void test_persist() {

		// EXECUTE

		Object toPersist = new Object();

		// VERIFY

		adapter.insert(toPersist);

		verify(entityManager).persist(same(toPersist));

	}

	@Test( groups = "unit" )
	public void test_merge() {

		// EXECUTE

		Object toPersist = new Object();

		Object merged = adapter.update(toPersist);

		// VERIFY

		verify(entityManager).merge(same(toPersist));

	}

	@Test( groups = "unit" )
	public void test_flush() {

		// EXECUTE

		adapter.flush();

		// VERIFY

		verify(entityManager).flush();

	}

	@Test( groups = "unit" )
	public void test_lookupByQuery_map_parameters() {

		// STUB

		List<?> listResult = Collections.emptyList();

		stub(entityManager.createQuery(anyString())).toReturn(query);
		stub(query.getResultList()).toReturn(listResult);

		// EXECUTE

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("key1", "value1");
		params.put("key2", "value2");

		Collection<Object> result = adapter.lookupByQuery("query", params);

		// VERIFY

		assertSame(listResult, result);

		verify(entityManager).createQuery(eq("query"));

		verify(query).setParameter(eq("key1"), eq("value1"));
		verify(query).setParameter(eq("key2"), eq("value2"));
		verify(query).getResultList();

	}

	@Test( groups = "unit" )
	public void test_lookupByQuery_array_parameters() {

		// STUB

		List<?> listResult = Collections.emptyList();

		stub(entityManager.createQuery(anyString())).toReturn(query);
		stub(query.getResultList()).toReturn(listResult);

		// EXECUTE

		Object[] params = new Object[2];
		params[0] = "value1";
		params[1] = "value2";

		Collection<Object> result = adapter.lookupByQuery("query", params);

		// VERIFY

		assertSame(listResult, result);

		verify(entityManager).createQuery(eq("query"));

		verify(query).setParameter(eq(1), eq("value1"));
		verify(query).setParameter(eq(2), eq("value2"));
		verify(query).getResultList();

	}

	@Test( groups = "unit" )
	public void test_lookup_map_parameters() {

		// STUB

		List<?> listResult = Collections.emptyList();

		stub(entityManager.createNamedQuery(anyString())).toReturn(query);
		stub(query.getResultList()).toReturn(listResult);

		// EXECUTE

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("key1", "value1");
		params.put("key2", "value2");

		Collection<Object> result = adapter.lookup("name", params);

		// VERIFY

		assertSame(listResult, result);

		verify(entityManager).createNamedQuery(eq("name"));

		verify(query).setParameter(eq("key1"), eq("value1"));
		verify(query).setParameter(eq("key2"), eq("value2"));
		verify(query).getResultList();

	}

	@Test( groups = "unit" )
	public void test_lookup_array_parameters() {

		// STUB

		List<?> listResult = Collections.emptyList();

		stub(entityManager.createNamedQuery(anyString())).toReturn(query);
		stub(query.getResultList()).toReturn(listResult);

		// EXECUTE

		Object[] params = new Object[2];
		params[0] = "value1";
		params[1] = "value2";

		Collection<Object> result = adapter.lookup("name", params);

		// VERIFY

		assertSame(listResult, result);

		verify(entityManager).createNamedQuery(eq("name"));

		verify(query).setParameter(eq(1), eq("value1"));
		verify(query).setParameter(eq(2), eq("value2"));
		verify(query).getResultList();

	}


	/* (non-Javadoc)
	 * @see org.smooks.scribe.test.util.BaseTestCase#beforeMethod()
	 */
	@BeforeMethod(alwaysRun = true)
	@Override
	public void beforeMethod() {
		super.beforeMethod();

		adapter = new EntityManagerDaoAdapter(entityManager);
	}


}
