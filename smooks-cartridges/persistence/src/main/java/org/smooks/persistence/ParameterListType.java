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
package org.smooks.persistence;

import org.smooks.javabean.DataDecodeException;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public enum ParameterListType {

	NAMED,
	POSITIONAL;

	public static final String NAMED_STR = "NAMED";
	public static final String POSITIONAL_STR = "POSITIONAL";

	/**
	 * A Data decoder for this Enum
	 *
	 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
	 *
	 */
	public static class DataDecoder implements org.smooks.javabean.DataDecoder {

		/* (non-Javadoc)
		 * @see org.smooks.javabean.DataDecoder#decode(java.lang.String)
		 */
		public Object decode(final String data) throws DataDecodeException {
			final String value = data.toUpperCase();

			return valueOf(value);
		}

	}
}
