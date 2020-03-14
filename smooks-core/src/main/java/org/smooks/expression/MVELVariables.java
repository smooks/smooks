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
package org.smooks.expression;

import org.mvel2.integration.VariableResolverFactory;


/**
 * The MVELVariables is utility class for MVEL. It can be used to
 * inspect the MVEL Variable Context.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class MVELVariables {

	private VariableResolverFactory variableResolverFactory;

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
