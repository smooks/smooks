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

import org.milyn.annotation.AnnotatedClass;
import org.milyn.annotation.AnnotationManager;
import org.milyn.assertion.AssertArgument;
import org.milyn.scribe.Dao;
import org.milyn.scribe.Flushable;
import org.milyn.scribe.Locator;
import org.milyn.scribe.MappingDao;
import org.milyn.scribe.ObjectStore;
import org.milyn.scribe.Queryable;
import org.milyn.scribe.reflection.AnnotatedDaoRuntimeInfo;
import org.milyn.scribe.reflection.AnnotatedDaoRuntimeInfoFactory;

/**
 * Manages the creation of DaoInvokers
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class DaoInvokerFactory {

	private static final DaoInvokerFactory instance = new DaoInvokerFactory();

	public static final String REPOSITORY_KEY = DaoInvokerFactory.class.getName() + "#REPOSITORY_KEY";

	/**
	 * Returns the DaoInvokerFactory instance
	 *
	 * @return the DaoInvokerFactory instance
	 */
	public static final DaoInvokerFactory getInstance() {
		return instance;
	}

	private DaoInvokerFactory() {
	}

	/**
	 * Creates a DaoInvoker depending on the DAO object.
	 * If the DAO object is a instance of {@link Dao}, {@link MappingDao},
	 * {@link Queryable}, {@link Locator} or {@link Flushable} then a {@link InterfaceDaoInvoker}
	 * is created and returned. If the DAO class is annotated with the {@link Dao} annotation
	 * then a {@link AnnotatedDaoInvoker} is created and returned. If neither is the case then a
	 * {@link IllegalArgumentException} exception is thrown.
	 *
	 * @param dao The DAO for which the invoker instantiated
	 * @param objectStore An object store for caching and retrieving a cached {@link AnnotatedDaoRuntimeInfoFactory} object.
	 * @return the DaoInvoker for the specified DAO
	 * @throws IllegalArgumentException if the DAO object doesn't match for a {@link InterfaceDaoInvoker} or {@link AnnotatedDaoInvoker}.
	 */
	public DaoInvoker create(final Object dao, final ObjectStore objectStore) {
		AssertArgument.isNotNull(dao, "dao");
		AssertArgument.isNotNull(objectStore, "objectStore");

		if(dao instanceof Dao
			|| dao instanceof MappingDao
			|| dao instanceof Queryable
			|| dao instanceof Locator
			|| dao instanceof Flushable) {

			return new InterfaceDaoInvoker(dao);

		} else {

			final AnnotatedClass annotatedClass =  AnnotationManager.getAnnotatedClass(dao.getClass());

			if(annotatedClass.isAnnotationPresent(org.milyn.scribe.annotation.Dao.class)) {

				final AnnotatedDaoRuntimeInfoFactory repository = getAnnotatedDAORuntimeInfoRepository(objectStore);

				return new AnnotatedDaoInvoker(dao, repository.create(dao.getClass()));

			} else {
				throw new IllegalArgumentException("The DAO object doesn't implement any of the DAO interfaces " +
						"or is annotated with the [" + org.milyn.scribe.annotation.Dao.class.getName() + "] annotation");
			}
		}
	}

	/**
	 * @param attributestore
	 * @return
	 */
	private AnnotatedDaoRuntimeInfoFactory getAnnotatedDAORuntimeInfoRepository(final ObjectStore objectStore) {
		AnnotatedDaoRuntimeInfoFactory repository = (AnnotatedDaoRuntimeInfoFactory) objectStore.get(REPOSITORY_KEY);

		if(repository == null) {
			repository = new AnnotatedDaoRuntimeInfoFactory();

			objectStore.set(REPOSITORY_KEY, repository);
		}
		return repository;
	}

}
