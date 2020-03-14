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
package org.smooks.classpath;

/**
 * Classpath resource filter.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface Filter {

    /**
     * Classpath resource filter method.
     * @param resourceName The classpath resource file name.  Needs to be converted to
     * a proper class name
     */
    public void filter(String resourceName);

    /**
     * Is this resource ignorable.
     * @param resourceName
     * @return
     */
    public boolean isIgnorable(String resourceName);
}
