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
package org.milyn.scribe.invoker;

import java.util.Map;

/**
 * The DAO Invoker interface
 * <p>
 * A DAO invoker is a bridge between a DAO and the object that needs to invoke the DAO without knowing
 * the actual DAO.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public interface DaoInvoker {

	Object insert(Object entity);

	Object insert(String name, Object entity);

	Object update(Object entity);

	Object update(String name, Object entity);

	Object delete(Object entity);

	Object delete(String name, Object entity);

	void flush();

	Object lookupByQuery(String query, Object ... parameters);

	Object lookupByQuery(String query, Map<String, ?> parameters);

	Object lookup(String name, Map<String, ?> parameters);

	Object lookup(String name, Object ... parameters);

}
