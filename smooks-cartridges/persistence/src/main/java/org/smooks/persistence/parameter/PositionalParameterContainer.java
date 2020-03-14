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
package org.smooks.persistence.parameter;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class PositionalParameterContainer implements ParameterContainer<PositionalParameter> {

	Object[] values;

	/**
	 *
	 */
	public PositionalParameterContainer(PositionalParameterIndex index) {
		values = new Object[index.size()];
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.parameter.ParameterContainer#clear()
	 */
	public void clear() {
		values = new Object[values.length];
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.parameter.ParameterContainer#containsParameter(org.smooks.scribe.parameter.Parameter)
	 */
	public boolean containsParameter(PositionalParameter parameter) {
		int index = parameter.getIndex();

		return values.length > index && values[index] != null;
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.parameter.ParameterContainer#get(org.smooks.scribe.parameter.Parameter)
	 */
	public Object get(PositionalParameter parameter) {
		int index = parameter.getIndex();

		return values[index];
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.parameter.ParameterContainer#put(org.smooks.scribe.parameter.Parameter, java.lang.Object)
	 */
	public void put(PositionalParameter parameter, Object bean) {
		values[parameter.getIndex()] = bean;
	}

	public Object[] getValues() {
		return values.clone();
	}

	/* (non-Javadoc)
	 * @see org.smooks.scribe.parameter.ParameterContainer#remove(org.smooks.scribe.parameter.Parameter)
	 */
	public Object remove(PositionalParameter parameter) {
		Object old = get(parameter);

		if(old != null) {
			values[parameter.getIndex()] = null;
		}

		return old;
	}

}
