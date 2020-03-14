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
package org.smooks.delivery;

/**
 * Vistor appender.
 * <p/>
 * A visitor appender is somewhat like a {@link ConfigurationExpander},
 * accept it appends fully configured Visitor instances.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public interface VisitorAppender {

    /**
     * Add visitors to the supplied Cisitor map.
     * @param visitorMap The visitor map to be added to. 
     */
    void addVisitors(VisitorConfigMap visitorMap);
}
