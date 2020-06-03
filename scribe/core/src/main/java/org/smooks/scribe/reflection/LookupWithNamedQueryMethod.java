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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.smooks.assertion.AssertArgument;

/**
 * @author maurice
 *
 */
public class LookupWithNamedQueryMethod {

	final Method method;

	final int queryIndex;
	final int parameterIndex;

	/**
	 *
	 */
	public LookupWithNamedQueryMethod(final Method method, final int queryIndex, final int parameterIndex) {
		AssertArgument.isNotNull(method, "method");

		if(queryIndex < 0 ) {
			throw new IllegalArgumentException("queryIndex can't be smaller then zero");
		}
		if(queryIndex > 1 ) {
			throw new IllegalArgumentException("queryIndex can't be bigger then one");
		}
		if(parameterIndex < 0 ) {
			throw new IllegalArgumentException("queryIndex can't be smaller then zero");
		}
		if(parameterIndex > 1 ) {
			throw new IllegalArgumentException("queryIndex can't be bigger then one");
		}
		if(queryIndex == parameterIndex) {
			throw new IllegalArgumentException("queryIndex and parameterIndex can't be the same");
		}

		this.method = method;
		this.queryIndex = queryIndex;
		this.parameterIndex = parameterIndex;
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.method.DAOMethod#invoke()
	 */
	public Collection<?> invoke(final Object obj, final String query, final Map<String, ?> parameters){
		final Object[] args = new Object[2];
		args[queryIndex] = query;
		args[parameterIndex] = parameters;


		try {
			return (Collection<?>) method.invoke(obj, args);
		} catch (final IllegalArgumentException e) {
			throw new RuntimeException("The method [" + method + "] of the class [" + method.getDeclaringClass().getName() + "] threw an exception, while invoking it with the object [" + obj + "].", e);
		} catch (final IllegalAccessException e) {
			throw new RuntimeException("The method [" + method + "] of the class [" + method.getDeclaringClass().getName() + "] threw an exception, while invoking it with the object [" + obj + "].", e);
		} catch (final InvocationTargetException e) {
			throw new RuntimeException("The method [" + method + "] of the class [" + method.getDeclaringClass().getName() + "] threw an exception, while invoking it with the object [" + obj + "].", e);
		}
	}

}
