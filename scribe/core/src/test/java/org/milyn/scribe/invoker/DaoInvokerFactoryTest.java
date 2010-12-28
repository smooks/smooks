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

import static junit.framework.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.milyn.scribe.Dao;
import org.milyn.scribe.MapObjectStore;
import org.milyn.scribe.MappingDao;
import org.milyn.scribe.ObjectStore;
import org.milyn.scribe.invoker.DaoInvoker;
import org.milyn.scribe.invoker.DaoInvokerFactory;
import org.milyn.scribe.test.dao.FullAnnotatedDao;
import org.milyn.scribe.test.util.BaseTestCase;
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
