/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
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
package org.smooks.assertion;

import java.util.Collection;
import java.util.Map;

/**
 * Argument assertion utilities.
 *
 * @author tfennelly
 */
public abstract class AssertArgument {

	/**
	 * Assert that the argument is not null.
	 *
	 * @param arg
	 *            Argument.
	 * @param argName
	 *            Argument name.
	 * @throws IllegalArgumentException
	 *             Argument is null.
	 */
	public static void isNotNull(Object arg, String argName)
			throws IllegalArgumentException {
		if (arg == null) {
			throw new IllegalArgumentException("Undefined '" + argName + "' argument in method call.");
		}
	}

	/**
	 * Assert that the argument is not empty.
	 *
	 * @param arg
	 *            Argument.
	 * @param argName
	 *            Argument name.
	 * @throws IllegalArgumentException
	 *             Argument is not null, but is empty.
	 */
	public static void isNotEmpty(String arg, String argName)
			throws IllegalArgumentException {
		if (arg != null && arg.trim().length() == 0) {
			throw new IllegalArgumentException("Not undefined, but empty '" + argName + "' argument in method call.");
		}
	}

	/**
	 * Assert that the argument is neither null nor empty.
	 *
	 * @param arg
	 *            Argument.
	 * @param argName
	 *            Argument name.
	 * @throws IllegalArgumentException
	 *             Argument is null or empty.
	 */
	public static void isNotNullAndNotEmpty(String arg, String argName)
			throws IllegalArgumentException {
		if (arg == null || arg.trim().length() == 0) {
			throw new IllegalArgumentException("Undefined or empty '" + argName + "' argument in method call.");
		}
	}

	/**
	 * Assert that the argument is neither null nor empty.
	 *
	 * @param arg Argument.
	 * @param argName Argument name.
	 * @throws IllegalArgumentException Argument is null or empty.
	 */
	public static void isNotNullAndNotEmpty(Object[] arg, String argName) throws IllegalArgumentException {
		if (arg == null || arg.length == 0) {
			throw new IllegalArgumentException("Undefined or empty '" + argName + "' argument in method call.");
		}
	}

	/**
	 * Assert that the argument is neither null nor empty.
	 *
	 * @param arg Argument.
	 * @param argName Argument name.
	 * @throws IllegalArgumentException Argument is null or empty.
	 */
	public static void isNotNullAndNotEmpty(Collection<?> arg, String argName) throws IllegalArgumentException {
		if (arg == null || arg.isEmpty()) {
			throw new IllegalArgumentException("Undefined or empty '" + argName + "' argument in method call.");
		}
	}

	/**
	 * Assert that the argument is neither null nor empty.
	 *
	 * @param arg Argument.
	 * @param argName Argument name.
	 * @throws IllegalArgumentException Argument is null or empty.
	 */
	public static void isNotNullAndNotEmpty(Map<?, ?> arg, String argName) throws IllegalArgumentException {
		if (arg == null || arg.isEmpty()) {
			throw new IllegalArgumentException("Undefined or empty '" + argName + "' argument in method call.");
		}
	}

	/**
	 * Assert that the argument is an instance of the specified class.
	 *
	 * @param arg Argument.
	 * @param clazz The Class type to check.
	 * @param argName Argument name.
	 * @throws IllegalArgumentException Argument is null or empty.
	 */
	@SuppressWarnings("unused")
	public static void isInstanceOf(Object arg, Class<?> clazz, String argName) throws IllegalArgumentException {
		if (clazz.isAssignableFrom(arg.getClass())) {
			throw new IllegalArgumentException("Argument '" + argName + "' is not an instance of '" + clazz.getName() + "'.");
		}
	}
}
