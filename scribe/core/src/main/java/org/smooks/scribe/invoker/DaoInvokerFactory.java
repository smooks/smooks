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

import org.smooks.annotation.AnnotatedClass;
import org.smooks.annotation.AnnotationManager;
import org.smooks.assertion.AssertArgument;
import org.smooks.scribe.*;
import org.smooks.scribe.reflection.AnnotatedDaoRuntimeInfoFactory;

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
	public static DaoInvokerFactory getInstance() {
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

			if(annotatedClass.isAnnotationPresent(org.smooks.scribe.annotation.Dao.class)) {

				final AnnotatedDaoRuntimeInfoFactory repository = getAnnotatedDAORuntimeInfoRepository(objectStore);

				return new AnnotatedDaoInvoker(dao, repository.create(dao.getClass()));

			} else {
				throw new IllegalArgumentException("The DAO object doesn't implement any of the DAO interfaces " +
						"or is annotated with the [" + org.smooks.scribe.annotation.Dao.class.getName() + "] annotation");
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
