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
package org.milyn.persistence;

import org.milyn.commons.javabean.DataDecodeException;

/**
 * @author maurice
 */
public enum OnNoResult {

    NULLIFY,
    EXCEPTION;

    public static final String NULLIFY_STR = "NULLIFY";
    public static final String EXCEPTION_STR = "EXCEPTION";

    /**
     * A Data decoder for this Enum
     *
     * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
     */
    public static class DataDecoder implements org.milyn.commons.javabean.DataDecoder {

        /* (non-Javadoc)
         * @see org.milyn.commons.javabean.DataDecoder#decode(java.lang.String)
         */
        public Object decode(final String data) throws DataDecodeException {
            final String value = data.toUpperCase();

            return valueOf(value);
        }

    }
}
