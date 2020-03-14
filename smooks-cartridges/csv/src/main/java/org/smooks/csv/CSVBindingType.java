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
package org.smooks.csv;

import java.util.List;
import java.util.Map;

/**
 * CSV Binding type.
 * 
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 * @deprecated Use the {@link org.smooks.flatfile.FlatFileReader} configured with the {@link CSVRecordParserFactory}.
 */
public enum CSVBindingType {
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
