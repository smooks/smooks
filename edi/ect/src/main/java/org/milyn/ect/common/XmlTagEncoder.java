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
package org.milyn.ect.common;

import java.util.regex.Pattern;

/**
 * XmlTagEncoder removes illegal characters from values used as xml element names.
 * @author bardl
 */
public class XmlTagEncoder {
    private static Pattern ESCAPE_PATTERN = Pattern.compile("[^\\w\\.]");

    public static String encode(String name) {
        return ESCAPE_PATTERN.matcher(name).replaceAll("_");
    }
}
