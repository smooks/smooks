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
public class NamedParameterIndex extends ParameterIndex<String, NamedParameter> {

	private int index = 0;


	/* (non-Javadoc)
	 * @see org.smooks.scribe.parameter.Index#createParameter(int, java.lang.Object)
	 */
	@Override
	protected NamedParameter createParameter(String value) {
		return new NamedParameter(this, index++, value);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof NamedParameterIndex == false) {
			return false;
		}
		return equals((ParameterIndex<?,?>)obj);
	}
}
