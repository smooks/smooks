/*
	Milyn - Copyright (C) 2008

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
package org.smooks.yaml;

import org.smooks.javabean.DataDecodeException;

/**
 * Defines the strategy how to handle anchors and aliasses.
 *
 * @author maurice_zeijen
 */
public enum AliasStrategy {
	/**
	 * Adds a 'id' attribute to the element with the anchor and the 'ref'
	 * attribute to the elements with the alias. The value of these attributes
	 * is the name of the anchor. The reference needs to be handled within the
	 * Smooks config. The attribute names can be set via the
	 * 'anchorAttributeName' and 'aliasAttributeName' properties.
	 */
	REFER,

	/**
	 * The elements or value from the anchor are resolved (copied) under the
	 * element with the alias. Smooks doesn't see that there was a reference.
	 */
	RESOLVE,

	/**
	 * A combination of REFER and RESOLVE. The element of the anchor are
	 * resolved and the attributes are set. You should use this if you want to
	 * resolve the element but also need the alias name because it has a
	 * business meaning.
	 */
	REFER_RESOLVE;

	public static final String REFER_STR = "REFER";
	public static final String RESOLVE_STR = "RESOLVE";
	public static final String REFER_RESOLVE_STR = "REFER_RESOLVE";

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