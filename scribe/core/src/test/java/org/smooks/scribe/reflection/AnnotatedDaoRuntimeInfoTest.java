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
package org.smooks.scribe.reflection;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.smooks.scribe.IllegalAnnotationUsageException;
import org.smooks.scribe.annotation.Dao;
import org.smooks.scribe.annotation.Delete;
import org.smooks.scribe.annotation.Insert;
import org.smooks.scribe.annotation.Lookup;
import org.smooks.scribe.annotation.Update;
import org.smooks.scribe.test.dao.FullAnnotatedDao;
import org.smooks.scribe.test.dao.MinimumAnnotatedDao;
import org.smooks.scribe.test.dao.OnlyDefaultAnnotatedDao;
import org.smooks.scribe.test.util.BaseTestCase;
import org.mockito.Mock;
import org.testng.annotations.Test;

/**
 * TODO: Write more extensive tests to verify different method declarations
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
@Test( groups = "unit" )
public class AnnotatedDaoRuntimeInfoTest extends BaseTestCase{

	private final AnnotatedDaoRuntimeInfo fullAnnotatedDaoRuntimeInfo = new AnnotatedDaoRuntimeInfo(FullAnnotatedDao.class);

	private final AnnotatedDaoRuntimeInfo minimumAnnotatedDaoRuntimeInfo = new AnnotatedDaoRuntimeInfo(MinimumAnnotatedDao.class);

	@Mock
	private FullAnnotatedDao fullAnnotatedDao;


	public void test_getDaoClass() {

		Class<?> daoClass = fullAnnotatedDaoRuntimeInfo.getDaoClass();

		assertNotNull(daoClass);
		assertSame(FullAnnotatedDao.class, daoClass);

	}




	public void test_getInsertMethod() {

		EntityMethod method = fullAnnotatedDaoRuntimeInfo.getDefaultInsertMethod();

		assertNotNull(method);

		Object toPersist = new Object();

		method.invoke(fullAnnotatedDao, toPersist);

		verify(fullAnnotatedDao).insertIt(same(toPersist));

		assertNull(minimumAnnotatedDaoRuntimeInfo.getDefaultInsertMethod());

	}


	public void test_getUpdateMethod() {

		EntityMethod method = fullAnnotatedDaoRuntimeInfo.getDefaultUpdateMethod();

		assertNotNull(method);

		Object toMerge = new Object();

		method.invoke(fullAnnotatedDao, toMerge);

		verify(fullAnnotatedDao).updateIt(same(toMerge));

		assertNull(minimumAnnotatedDaoRuntimeInfo.getDefaultUpdateMethod());

	}


	public void test_getDeleteMethod() {

		EntityMethod method = fullAnnotatedDaoRuntimeInfo.getDefaultDeleteMethod();

		assertNotNull(method);

		Object toDelete = new Object();

		method.invoke(fullAnnotatedDao, toDelete);

		verify(fullAnnotatedDao).deleteIt(same(toDelete));

		assertNull(minimumAnnotatedDaoRuntimeInfo.getDefaultDeleteMethod());

	}


	public void test_getFlushMethod() {

		FlushMethod method = fullAnnotatedDaoRuntimeInfo.getFlushMethod();

		assertNotNull(method);

		method.invoke(fullAnnotatedDao);

		verify(fullAnnotatedDao).flushIt();

		assertNull(minimumAnnotatedDaoRuntimeInfo.getFlushMethod());

	}


	public void test_getFindByNamedQueryMethod() {

		LookupWithNamedQueryMethod method = fullAnnotatedDaoRuntimeInfo.getLookupByNamedQueryMethod();

		assertNotNull(method);

		Map<String, Object> params = new HashMap<String, Object>();

		method.invoke(fullAnnotatedDao, "query", params);

		verify(fullAnnotatedDao).findByQuery(eq("query"), same(params));

		assertNull(minimumAnnotatedDaoRuntimeInfo.getLookupByNamedQueryMethod());

	}


	public void test_getFindByPositionalQueryMethod() {

		LookupWithPositionalQueryMethod method = fullAnnotatedDaoRuntimeInfo.getLookupByPositionalQueryMethod();

		assertNotNull(method);

		Object[] params = new Object[0];

		method.invoke(fullAnnotatedDao, "query", params);

		verify(fullAnnotatedDao).findByQuery(eq("query"), same(params));

		assertNull(minimumAnnotatedDaoRuntimeInfo.getLookupByPositionalQueryMethod());

	}


	public void test_getFindByMethod() {

		LookupMethod method = fullAnnotatedDaoRuntimeInfo.getLookupWithNamedParametersMethod("id");

		assertNotNull(method);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", 1L);

		method.invoke(fullAnnotatedDao, params);

		verify(fullAnnotatedDao).findById(eq(1L));

		assertNull(minimumAnnotatedDaoRuntimeInfo.getLookupWithNamedParametersMethod("id"));
	}

	public void test_default_annotated_methods() {
		AnnotatedDaoRuntimeInfo runtimeInfo = new AnnotatedDaoRuntimeInfo(OnlyDefaultAnnotatedDao.class);

		assertNotNull(runtimeInfo.getDefaultInsertMethod());
		assertNotNull(runtimeInfo.getDefaultUpdateMethod());
		assertNotNull(runtimeInfo.getDefaultDeleteMethod());
	}

	@Test(expectedExceptions = IllegalAnnotationUsageException.class)
	public void test_exception_on_same_named_insert_method() {
		new AnnotatedDaoRuntimeInfo(IncorrectInsertDao.class);
	}

	@Test(expectedExceptions = IllegalAnnotationUsageException.class)
	public void test_exception_on_same_named_update_method() {
		new AnnotatedDaoRuntimeInfo(IncorrectUpdateDao.class);
	}

	@Test(expectedExceptions = IllegalAnnotationUsageException.class)
	public void test_exception_on_same_named_delete_method() {
		new AnnotatedDaoRuntimeInfo(IncorrectDeleteDao.class);
	}

	@Test(expectedExceptions = IllegalAnnotationUsageException.class)
	public void test_exception_on_same_named_locator_method() {
		new AnnotatedDaoRuntimeInfo(IncorrectLocatorDao.class);
	}


	@Dao
	private class IncorrectInsertDao {

		@Insert
		public void insert(Object entity) {
		}

		@Insert(name="insert")
		public void insert2(Object entity)  {
		}
	}

	@Dao
	private class IncorrectUpdateDao {

		@Update
		public void update(Object entity) {
		}

		@Update(name="update")
		public void update2(Object entity)  {
		}
	}

	@Dao
	private class IncorrectDeleteDao {

		@Delete
		public void delete(Object entity) {
		}

		@Delete(name="delete")
		public void delete2(Object entity)  {
		}
	}

	@Dao
	private class IncorrectLocatorDao {

		@Lookup
		public void findBy(Object entity) {
		}

		@Lookup(name="findBy")
		public void findBy2(Object entity)  {
		}
	}
}
