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
package org.smooks.fixedlength;

import java.util.List;
import java.util.Map;

/**
 *  Fixed length Binding type.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
public enum FixedLengthBindingType {
    /**
     * Bind a single instance of the binding class.
     */
    SINGLE,
    /**
     * Bind a {@link List} of instances of the binding class.
     * <p/>
     * Creates a {@link List} under the binding 'beanId' name.
     */
    LIST,
    /**
     * Bind a {@link Map} of instances of the binding class.
     * <p/>
     * Creates a {@link Map} under the binding 'beanId' name, with the
     * Map entry keys coming from the 'keyField' name on the
     * {@link CSVBinding} instance.
     */
    MAP
}
