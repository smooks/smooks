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
package org.milyn.routing.db;

import org.milyn.container.ExecutionContext;
import org.milyn.container.ApplicationContext;
import org.milyn.Smooks;

/**
 * Database Resultset lifecycle scope for Resultsets created by the
 * {@link SQLExecutor} class.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public enum ResultSetScope {
    /**
     * The Resultset is scoped around the Smooks {@link ExecutionContext}, and so only
     * lives for the duration of the {@link Smooks#filter(org.milyn.container.ExecutionContext,javax.xml.transform.Source,javax.xml.transform.Result)}
     * call.
     */
    EXECUTION,
    /**
     * The Resultset is scoped around the Smooks {@link ApplicationContext}. In this case,
     * the ResultSet can outlive the lifetime of the {@link Smooks#filter(org.milyn.container.ExecutionContext,javax.xml.transform.Source,javax.xml.transform.Result)}
     * that created it.  It's expiry is governed by the .
     */
    APPLICATION;
}
