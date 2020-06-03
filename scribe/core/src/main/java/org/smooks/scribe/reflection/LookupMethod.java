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
package org.smooks.scribe.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.smooks.annotation.AnnotatedMethod;
import org.smooks.annotation.AnnotationManager;
import org.smooks.assertion.AssertArgument;
import org.smooks.scribe.annotation.Param;

/**
 * @author maurice
 *
 * TODO: implement type checking for primative types...
 */
public class LookupMethod {

	final Method method;

	Map<String, Integer> parameterPositions;

	private boolean namedParameters = false;

	/**
	 *
	 */
	public LookupMethod(final Method method) {
		AssertArgument.isNotNull(method, "method");

		this.method = method;

		analyzeParameters();
	}

	/**
	 *
	 */
	private void analyzeParameters() {

		final AnnotatedMethod aMethod = AnnotationManager.getAnnotatedClass(method.getDeclaringClass()).getAnnotatedMethod(method);

		final Annotation[][] parameterAnnotations = aMethod.getParameterAnnotations();

		int parameterSize = aMethod.getMethod().getParameterTypes().length;

		for(int i = 0; i < parameterAnnotations.length; i++) {


			for(final Annotation annotation : parameterAnnotations[i]) {

				if(Param.class.equals(annotation.annotationType())) {
					namedParameters = true;

					final Param param = (Param) annotation;

					final String name = param.value().trim();

					if(name.length() == 0) {
						throw new RuntimeException("Illegal empty parameter value encounterd on parameter " + i
								+ " of method '" + method + "' from class '" + method.getDeclaringClass().getName() +"'.");
					}

					if(parameterPositions == null) {
						parameterPositions = new HashMap<String, Integer>();
					}

					parameterPositions.put(param.value(), i);

					break;
				}

			}

		}
		if(namedParameters && parameterPositions.size() != parameterSize) {
			throw new RuntimeException("Not all the parameters of the method '" + method.getDeclaringClass().getName() + "." + method + "' are annotated with the '" + Param.class.getName() + "' annotation."
					+ " All the parameters of the method need to be have the '" + Param.class.getName() + "' annotation when using the annotation on a method.");
		}

	}


	/* (non-Javadoc)
	 * @see org.smooks.scribe.method.DAOMethod#invoke()
	 *
	 *
	 */
	public Object invoke(final Object obj, final Map<String, ?> parameters){

		if(!namedParameters) {
			throw new IllegalStateException("This Lookup Method doesn't have name parameters and there for can't be invoked with a parameter Map.");
		}

		Object[] args = new Object[parameterPositions.size()];

		//TODO: evaluate a faster way to map the arguments but which is equally safe
		for(final Entry<String, ?> parameterEntry : parameters.entrySet()) {
			String parameterName = parameterEntry.getKey();

			final Integer position = parameterPositions.get(parameterName);

			if(position == null) {
				throw new RuntimeException("Parameter with the name " + parameterName + " isn't found on the method '" + method + "' of the class '" + method.getDeclaringClass().getName() + "'");
			}

			args[position] = parameterEntry.getValue();
		}

		return invoke(obj, args);

	}

	/**
	 * @param obj
	 * @param args
	 * @return
	 */
	public Object invoke(final Object obj, Object ... args) {
		try {
			return method.invoke(obj, args);
		} catch (final IllegalArgumentException e) {
			throw new RuntimeException("The method '" + method + "' of the class '" + method.getDeclaringClass().getName() + "' threw an exception, while invoking it with the object '" + obj + "'.", e);
		} catch (final IllegalAccessException e) {
			throw new RuntimeException("The method '" + method + "' of the class '" + method.getDeclaringClass().getName() + "' threw an exception, while invoking it with the object '" + obj + "'.", e);
		} catch (final InvocationTargetException e) {
			throw new RuntimeException("The method '" + method + "' of the class '" + method.getDeclaringClass().getName() + "' threw an exception, while invoking it with the object '" + obj + "'.", e);
		}
	}
}
