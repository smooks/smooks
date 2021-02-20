/*-
 * ========================LICENSE_START=================================
 * Smooks Core
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
package org.smooks.engine.expression;

import org.mvel2.integration.VariableResolverFactory;


/**
 * The MVELVariables is utility class for MVEL. It can be used to
 * inspect the MVEL Variable Context.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class MVELVariables {

	private final VariableResolverFactory variableResolverFactory;

	public MVELVariables(VariableResolverFactory variableResolverFactory) {

		this.variableResolverFactory = variableResolverFactory;
	}

	/**
	 * Returns true if the variable is defined within the MVEL Context.
	 *
	 * @param var
	 * @return
	 */
	public boolean isdef(String var) {
		return variableResolverFactory.isResolveable(var);
	}

	/**
	 * Returns true if the variable is defined within the MVEL Context.
	 *
	 * @param var
	 * @return
	 */
	public boolean isResolveable(String var) {
		return isdef(var);
	}

	/**
	 * Returns true if the variable is not defined within the MVEL Context.
	 *
	 * @param var
	 * @return
	 */
	public boolean isUnresolveable(String var) {
		return !isResolveable(var);
	}

	/**
	 * Returns the value of the variable or null if it is not defined.
	 *
	 * @param var
	 * @return
	 */
	public Object get(String var) {
		if(isdef(var)) {
			return variableResolverFactory.getVariableResolver(var).getValue();
		}
		return null;
	}

}
