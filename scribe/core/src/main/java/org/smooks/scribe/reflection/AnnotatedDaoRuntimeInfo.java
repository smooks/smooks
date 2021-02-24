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
package org.smooks.scribe.reflection;

import org.smooks.annotation.AnnotatedClass;
import org.smooks.annotation.AnnotatedMethod;
import org.smooks.annotation.AnnotationManager;
import org.smooks.assertion.AssertArgument;
import org.smooks.scribe.AnnotationNotFoundException;
import org.smooks.scribe.IllegalAnnotationUsageException;
import org.smooks.scribe.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.smooks.support.ClassUtil.containsAssignableClass;
import static org.smooks.support.ClassUtil.indexOfFirstAssignableClass;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class AnnotatedDaoRuntimeInfo {

	private final Class<?> daoClass;

	private EntityMethod defaultInsertMethod;

	private EntityMethod defaultUpdateMethod;

	private FlushMethod flushMethod;

	private EntityMethod defaultDeleteMethod;

	private LookupWithNamedQueryMethod lookupWithNamedQueryMethod;

	private LookupWithPositionalQueryMethod lookupWithPositionalQueryMethod;

	private final Map<String, EntityMethod> insertMethods = new HashMap<String, EntityMethod>();

	private final Map<String, EntityMethod> updateMethods = new HashMap<String, EntityMethod>();

	private final Map<String, EntityMethod> deleteMethods = new HashMap<String, EntityMethod>();

	private final Map<String, LookupMethod> lookupWithNamedParameters = new HashMap<String, LookupMethod>();


	/**
	 *
	 * @param daoClass
	 */
	AnnotatedDaoRuntimeInfo(final Class<?> daoClass) {
		AssertArgument.isNotNull(daoClass, "daoClass");

		this.daoClass = daoClass;

		analyze();
	}

	/**
	 * @return the daoClass
	 */
	public Class<?> getDaoClass() {
		return daoClass;
	}

	/**
	 * @return the defaultInsertMethod
	 */
	public EntityMethod getDefaultInsertMethod() {
		return defaultInsertMethod;
	}

	/**
	 *
	 * @param name
	 * @return
	 */
	public EntityMethod getInsertMethod(String name) {
		return insertMethods.get(name);
	}

	/**
	 * @return the defaultUpdateMethod
	 */
	public EntityMethod getDefaultUpdateMethod() {
		return defaultUpdateMethod;
	}

	/**
	 *
	 * @param name
	 * @return
	 */
	public EntityMethod getUpdateMethod(String name) {
		return updateMethods.get(name);
	}

	/**
	 * @return the defaultFlushMethod
	 */
	public FlushMethod getFlushMethod() {
		return flushMethod;
	}

	/**
	 * @return the defaultFlushMethod
	 */
	public EntityMethod getDefaultDeleteMethod() {
		return defaultDeleteMethod;
	}


	/**
	 *
	 * @param name
	 * @return
	 */
	public EntityMethod getDeleteMethod(String name) {
		return deleteMethods.get(name);
	}


	public LookupWithNamedQueryMethod getLookupByNamedQueryMethod() {
		return lookupWithNamedQueryMethod;
	}

	public LookupWithPositionalQueryMethod getLookupByPositionalQueryMethod() {
		return lookupWithPositionalQueryMethod;
	}

	public LookupMethod getLookupWithNamedParametersMethod(final String name) {
		return lookupWithNamedParameters.get(name);
	}

	/**
	 *
	 */
	private void analyze() {

		AnnotatedClass annotatedClass =  AnnotationManager.getAnnotatedClass(daoClass);

		if(annotatedClass.getAnnotation(Dao.class) == null) {
			throw new AnnotationNotFoundException("The class '"+ daoClass.getName() +"' isn't annotated with the '"+ Dao.class.getName() +"' annotation. Only class annotated with that annotation can be used as annotated DAO.");
		}

		AnnotatedMethod[] annotatedMethods = annotatedClass.getAnnotatedMethods();
		for(final AnnotatedMethod method : annotatedMethods) {
			if(method.getAllAnnotations().length > 0) {
				if(method.isAnnotationPresent(Insert.class)) {

					analyzeInsertMethod(method);

				} else if(method.isAnnotationPresent(Update.class)) {

					analyzeUpdateMethod(method);

				} else if(method.isAnnotationPresent(Delete.class)) {

					analyzeDeleteMethod(method);

				} else if(method.isAnnotationPresent(Flush.class)) {

					analyzeFlushMethod(method);

				} else if(method.isAnnotationPresent(Lookup.class)) {

					analyzeFindByMethod(method);

				} else if(method.isAnnotationPresent(LookupByQuery.class)) {

					analyzeFindByQueryMethod(method);

				}
			}
		}
		if(defaultInsertMethod == null && insertMethods.size() == 1) {
			defaultInsertMethod = insertMethods.values().iterator().next();
		}
		if(defaultUpdateMethod == null && updateMethods.size() == 1) {
			defaultUpdateMethod = updateMethods.values().iterator().next();
		}
		if(defaultDeleteMethod == null && deleteMethods.size() == 1) {
			defaultDeleteMethod = deleteMethods.values().iterator().next();
		}
	}

	/**
	 * @param method
	 */
	private void analyzeFlushMethod(final AnnotatedMethod aMethod) {
		Method method = aMethod.getMethod();

		if(flushMethod != null) {
			throw new IllegalAnnotationUsageException("At least two methods are annotated with the '"+ Flush.class.getName() +"'. Only one method per class is allowed to be the flush method.");
		}
		if(method.getParameterTypes().length > 0) {
			throw new IllegalAnnotationUsageException("The Flush annotated method '"+ method +"' of the DAO class '"+ daoClass.getName() +"' has parameters, which isn't allowed.");
		}

		flushMethod = new FlushMethod(method);
	}

	/**
	 * @param method
	 */
	private void analyzeUpdateMethod(final AnnotatedMethod aMethod) {
		Method method = aMethod.getMethod();

		Update annotation = aMethod.getAnnotation(Update.class);

		String name = annotation.name();
		if(name.length() == 0) {
			name = method.getName();
		}

		assertUniqueName(updateMethods, Update.class, name);

		if(annotation.isDefault() && defaultUpdateMethod != null) {
			throw new IllegalAnnotationUsageException("At least two methods are annotated with the '"+ Update.class.getName() +"' having the isDefault on true. Only one method per class is allowed to be the default update method.");
		}
		if(method.getParameterTypes().length == 0) {
			throw new IllegalAnnotationUsageException("The Update annotated method '"+ method +"' of the DAO class '"+ daoClass.getName() +"' doesn't have any parameters.");
		}
		if(method.getParameterTypes().length > 1) {
			throw new IllegalAnnotationUsageException("The Update annotated method '"+ method +"' of the DAO class '"+ daoClass.getName() +"' has more then 1 parameter, which isn't allowed.");
		}


		boolean returnsEntity  = !method.isAnnotationPresent(ReturnsNoEntity.class);

		EntityMethod updateMethod = new EntityMethod(method, returnsEntity);

		if(annotation.isDefault()) {
			defaultUpdateMethod = updateMethod;
		}
		updateMethods.put(name, updateMethod);
	}

	/**
	 * @param method
	 */
	private void analyzeInsertMethod(final AnnotatedMethod aMethod) {
		Method method = aMethod.getMethod();

		Insert annotation = aMethod.getAnnotation(Insert.class);

		String name = annotation.name();
		if(name.length() == 0) {
			name = method.getName();
		}

		assertUniqueName(insertMethods, Insert.class, name);

		if(annotation.isDefault() && defaultInsertMethod != null) {
			throw new IllegalAnnotationUsageException("At least two methods are annotated with the '"+ Insert.class.getName() +"'annotation having the isDefault on true. Only one method per class is allowed to be the default insert method.");
		}
		if(method.getParameterTypes().length == 0) {
			throw new IllegalAnnotationUsageException("The Insert annotated method '"+ method +"' of the DAO class '"+ daoClass.getName() +"'  doesn't have any parameters.");
		}
		if(method.getParameterTypes().length > 1) {
			throw new IllegalAnnotationUsageException("The Insert annotated method '"+ method +"' of the DAO class '"+ daoClass.getName() +"' has more then 1 parameter, which isn't allowed.");
		}

		boolean returnsEntity  = !method.isAnnotationPresent(ReturnsNoEntity.class);

		EntityMethod insertMethod = new EntityMethod(method, returnsEntity);

		if(annotation.isDefault()) {
			defaultInsertMethod = insertMethod;
		}
		insertMethods.put(name, insertMethod);
	}


	/**
	 * @param method
	 */
	private void analyzeDeleteMethod(final AnnotatedMethod aMethod) {
		Method method = aMethod.getMethod();

		Delete annotation = aMethod.getAnnotation(Delete.class);

		String name = annotation.name();
		if(name.length() == 0) {
			name = method.getName();
		}

		assertUniqueName(deleteMethods, Delete.class, name);

		if(annotation.isDefault() && defaultDeleteMethod != null) {
			throw new IllegalAnnotationUsageException("At least two methods are annotated with the '"+ Delete.class.getName() +"' annotation having the isDefault on true. Only one method per class is allowed to be the default delete method.");
		}
		if(method.getParameterTypes().length == 0) {
			throw new IllegalAnnotationUsageException("The Delete annotated method '"+ method +"' of the DAO class '"+ daoClass.getName() +"' doesn't have a parameter, which it needs.");
		}
		if(method.getParameterTypes().length > 1) {
			throw new IllegalAnnotationUsageException("The Delete annotated method '"+ method +"'  the DAO class '"+ daoClass.getName() +"' has more then 1 parameter, which isn't allowed.");
		}


		boolean returnsEntity  = !method.isAnnotationPresent(ReturnsNoEntity.class);

		EntityMethod deleteMethod = new EntityMethod(method, returnsEntity);

		if(annotation.isDefault()) {
			defaultDeleteMethod = deleteMethod;
		}
		deleteMethods.put(name, deleteMethod);
	}


	/**
	 *
	 * @param method
	 */
	private void analyzeFindByQueryMethod(final AnnotatedMethod aMethod) {
		Method method = aMethod.getMethod();

		Class<?>[] parameters = method.getParameterTypes();

		if(method.getParameterTypes().length != 2) {
			throw new IllegalAnnotationUsageException("The FindByQuery annotated method '"+ method +"' of the DAO class '"+ daoClass.getName() +"' " +
					"doesn't have exactly two parameters.");
		}

		if(!Collection.class.isAssignableFrom(method.getReturnType())) {
			throw new IllegalAnnotationUsageException("The FindByQuery annotated method '"+ method +"' of the DAO class '"+ daoClass.getName() +"' " +
				"doesn't return an instance of Collection.");

		}

		int queryIndex = indexOfFirstAssignableClass(String.class, parameters);
		if(queryIndex == -1) {
			throw new IllegalAnnotationUsageException("The FindByQuery annotated method '"+ method +"' of the DAO class '"+ daoClass.getName() +"' " +
				"doesn't have a String parameter. This parameter is needed to receive the query string.");
		}

		int parameterIndex = (queryIndex == 0) ? 1 : 0;

		if(containsAssignableClass(List.class, parameters) || containsAssignableClass(Object[].class, parameters)) {

			if(lookupWithPositionalQueryMethod != null) {
				throw new IllegalAnnotationUsageException("A second method annotated with the '"+ LookupByQuery.class.getName() +"' annotation is found for a Positional query. " +
						"Only one method, with a List or Object array parameter, per class is allowed to be annotated with this annotation.");
			}

			lookupWithPositionalQueryMethod = new LookupWithPositionalQueryMethod(method, queryIndex, parameterIndex);

		} else if(containsAssignableClass(Map.class, parameters)){

			if(lookupWithNamedQueryMethod != null) {
				throw new IllegalAnnotationUsageException("A second method annotated with the '"+ LookupByQuery.class.getName() +"' annotation is found for a Positional query. " +
						"Only one method, with a Map parameter, per class is allowed to be annotated with this annotation.");
			}

			lookupWithNamedQueryMethod = new LookupWithNamedQueryMethod(method, queryIndex, parameterIndex);

		} else {
			throw new IllegalAnnotationUsageException("The FindByQuery annotated method '"+ method +"' of the DAO class '"+ daoClass.getName() +"' " +
				"doesn't have a List, Object array or Map parameter. This parameter is needed to receive the query parameters.");
		}


	}

	/**
	 * @param method
	 */
	private void analyzeFindByMethod(final AnnotatedMethod aMethod) {
		Method method = aMethod.getMethod();

		Lookup findByAnnotation = aMethod.getAnnotation(Lookup.class);
		String name = findByAnnotation.name();

		if(name.trim().length() == 0) {
			name = method.getName();
		}

		assertUniqueName(lookupWithNamedParameters, Lookup.class, name);

		if(void.class.equals(method.getReturnType())){
			throw new IllegalAnnotationUsageException("The FindBy annotated method '"+ method +"' of the DAO class '"+ daoClass.getName() +"' " +
					"returns void, which isn't allowed. The method must return something.");
		}

		lookupWithNamedParameters.put(name, new LookupMethod(method));
	}


	/**
	 * @param name
	 */
	private void assertUniqueName(Map<String, ?> methods, Class<? extends Annotation> annotation, String name) {
		if(methods.containsKey(name)) {
			throw new IllegalAnnotationUsageException("A second method annotated with the '"+ annotation.getName() +"' annotation and the name '"+ name +"' is found." +
					"If you have defined a name on the annotation then please define a different one. If you haven't defined a name then please define one that is unique.");
		}
	}

}
