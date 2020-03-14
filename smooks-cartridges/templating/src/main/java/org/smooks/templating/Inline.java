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
package org.smooks.templating;

/**
 * Inline template usage.
 * <p/>
 * Inline the templating result relative to the target message fragment.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class Inline implements Usage {

    public static final Inline ADDTO = new Inline();
    public static final Inline REPLACE = new Inline();
    public static final Inline INSERT_BEFORE = new Inline();
    public static final Inline INSERT_AFTER = new Inline();

    private Inline() {
    }
}
