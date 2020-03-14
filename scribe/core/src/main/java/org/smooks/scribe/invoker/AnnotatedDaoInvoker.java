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
package org.smooks.scribe.invoker;

import java.util.Map;

import org.smooks.assertion.AssertArgument;
import org.smooks.scribe.NoMethodWithAnnotationFoundException;
import org.smooks.scribe.annotation.Delete;
import org.smooks.scribe.annotation.Flush;
import org.smooks.scribe.annotation.Insert;
import org.smooks.scribe.annotation.Lookup;
import org.smooks.scribe.annotation.LookupByQuery;
import org.smooks.scribe.annotation.Update;
import org.smooks.scribe.reflection.AnnotatedDaoRuntimeInfo;
import org.smooks.scribe.reflection.EntityMethod;
import org.smooks.scribe.reflection.FlushMethod;
import org.smooks.scribe.reflection.LookupMethod;
import org.smooks.scribe.reflection.LookupWithNamedQueryMethod;
import org.smooks.scribe.reflection.LookupWithPositionalQueryMethod;


/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class AnnotatedDaoInvoker implements DaoInvoker {

	final private Object dao;

	final private AnnotatedDaoRuntimeInfo daoRuntimeInfo;

	/**
	 * @param dao
	 */
	public AnnotatedDaoInvoker(final Object dao, final AnnotatedDaoRuntimeInfo daoRuntimeInfo) {
		AssertArgument.isNotNull(dao, "dao");
		AssertArgument.isNotNull(daoRuntimeInfo, "daoRuntimeInfo");

		this.dao = dao;
		this.daoRuntimeInfo = daoRuntimeInfo;
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.invoker.DAOInvoker#flush()
	 */
	public void flush() {
		final FlushMethod method = daoRuntimeInfo.getFlushMethod();

		assertMethod(method, Flush.class);

		method.invoke(dao);
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.invoker.DAOInvoker#merge(java.lang.Object)
	 */
	public Object update(final Object entity) {
		final EntityMethod method = daoRuntimeInfo.getDefaultUpdateMethod();

		assertMethod(method, Update.class);

		return method.invoke(dao, entity);
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.invoker.DaoInvoker#update(java.lang.String, java.lang.Object)
	 */
	public Object update(String name, Object entity) {
		final EntityMethod method = daoRuntimeInfo.getUpdateMethod(name);

		assertMethod(method, name, Update.class);

		return method.invoke(dao, entity);
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.invoker.DAOInvoker#persist(java.lang.Object)
	 */
	public Object insert(final Object entity) {
		final EntityMethod method = daoRuntimeInfo.getDefaultInsertMethod();

		assertMethod(method, Insert.class);

		return method.invoke(dao, entity);
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.invoker.DaoInvoker#insert(java.lang.String, java.lang.Object)
	 */
	public Object insert(String name, Object entity) {
		final EntityMethod method = daoRuntimeInfo.getInsertMethod(name);

		assertMethod(method, name, Insert.class);

		return method.invoke(dao, entity);
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.invoker.DaoInvoker#delete(java.lang.Object[])
	 */
	public Object delete(final Object entity) {
		final EntityMethod method = daoRuntimeInfo.getDefaultDeleteMethod();

		assertMethod(method, Delete.class);

		return method.invoke(dao, entity);
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.invoker.DaoInvoker#delete(java.lang.String, java.lang.Object)
	 */
	public Object delete(String name, Object entity) {
		final EntityMethod method = daoRuntimeInfo.getDeleteMethod(name);

		assertMethod(method, name, Delete.class);

		return method.invoke(dao, entity);
	}


	/* (non-Javadoc)
	 * @see org.smooks.scribe.invoker.DAOInvoker#findByQuery(java.lang.String, java.lang.Object[])
	 */
	public Object lookupByQuery(final String query, final Object ... parameters) {

		final LookupWithPositionalQueryMethod method = daoRuntimeInfo.getLookupByPositionalQueryMethod();

		if(method == null) {
			throw new NoMethodWithAnnotationFoundException("No method found in DAO class '" + dao.getClass().getName() + "' that is annotated " +
					"with '" + LookupByQuery.class.getSimpleName() + "' annotation and has an Array argument for the positional parameters.");
		}

		return method.invoke(dao, query, parameters);

	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.invoker.DAOInvoker#findByQuery(java.lang.String, java.util.Map)
	 */
	public Object lookupByQuery(final String query, final Map<String, ?> parameters) {

		final LookupWithNamedQueryMethod method = daoRuntimeInfo.getLookupByNamedQueryMethod();

		if(method == null) {
			throw new NoMethodWithAnnotationFoundException("No method found in DAO class '" + dao.getClass().getName() + "' that is annotated " +
					"with '" + LookupByQuery.class.getSimpleName() + "' annotation and has a Map argument for the named parameters.");
		}

		return method.invoke(dao, query, parameters);
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.invoker.DAOInvoker#findBy(java.lang.String, java.util.Map)
	 */
	public Object lookup(final String name, final Map<String, ?> parameters) {

		final LookupMethod method = daoRuntimeInfo.getLookupWithNamedParametersMethod(name);

		assertMethod(method, name, Lookup.class);

		return method.invoke(dao, parameters);

	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.invoker.DAOInvoker#findBy(java.lang.String, java.util.Map)
	 */
	public Object lookup(final String name, final Object ... parameters) {

		final LookupMethod method = daoRuntimeInfo.getLookupWithNamedParametersMethod(name);

		assertMethod(method, name, Lookup.class);

		return method.invoke(dao, parameters);

	}

	private void assertMethod(final Object method, final Class<?> annotation) {

		if(method == null) {
			throw new NoMethodWithAnnotationFoundException("No method found in DAO class '" + dao.getClass().getName() + "' that is annotated with the '" + annotation.getSimpleName() + "' annotation.");
		}

	}

	private void assertMethod(final Object method, String name, final Class<?> annotation) {

		if(method == null) {
			throw new NoMethodWithAnnotationFoundException("No method found in DAO class '" + dao.getClass().getName() + "' that is annotated with the '" + annotation.getSimpleName() + "' annotation and has the name '"+ name +"'.");
		}

	}


}
