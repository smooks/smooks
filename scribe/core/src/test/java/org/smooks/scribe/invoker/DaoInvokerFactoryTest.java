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

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.smooks.scribe.Dao;
import org.smooks.scribe.MapObjectStore;
import org.smooks.scribe.MappingDao;
import org.smooks.scribe.ObjectStore;
import org.smooks.scribe.test.dao.FullAnnotatedDao;
import org.smooks.scribe.test.util.BaseTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
@Test(groups = "unit")
public class DaoInvokerFactoryTest extends BaseTestCase {

	ObjectStore objectStore;

	public void test_getInstance() {

		DaoInvokerFactory factory  = DaoInvokerFactory.getInstance();

		assertNotNull(factory);

		DaoInvokerFactory factory2  = DaoInvokerFactory.getInstance();

		assertSame(factory, factory2);

	}

	public void test_create_with_dao_interface() {

		DaoInvokerFactory factory  = DaoInvokerFactory.getInstance();

		@SuppressWarnings("unchecked")
		Dao<Object> daoMock = mock(Dao.class);

		DaoInvoker daoInvoker = factory.create(daoMock, objectStore);

		assertNotNull(daoInvoker);

		Object entity = new Object();

		daoInvoker.insert(entity);

		verify(daoMock).insert(same(entity));

	}

	public void test_create_with_mapping_dao_interface() {

		DaoInvokerFactory factory  = DaoInvokerFactory.getInstance();

		@SuppressWarnings("unchecked")
		MappingDao<Object> daoMock = mock(MappingDao.class);

		DaoInvoker daoInvoker = factory.create(daoMock, objectStore);

		assertNotNull(daoInvoker);

		Object entity = new Object();

		daoInvoker.insert("myInsert", entity);

		verify(daoMock).insert(eq("myInsert"), same(entity));

	}

	public void test_create_with_annotated_dao() {

		DaoInvokerFactory factory  = DaoInvokerFactory.getInstance();

		FullAnnotatedDao daoMock = mock(FullAnnotatedDao.class);

		DaoInvoker daoInvoker = factory.create(daoMock, objectStore);

		assertNotNull(daoInvoker);

		Object entity = new Object();

		daoInvoker.insert(entity);

		verify(daoMock).insertIt(same(entity));

		assertNotNull(objectStore.get(DaoInvokerFactory.REPOSITORY_KEY));

	}

	@BeforeMethod
	public void setup() {
		objectStore = new MapObjectStore();
	}
}
