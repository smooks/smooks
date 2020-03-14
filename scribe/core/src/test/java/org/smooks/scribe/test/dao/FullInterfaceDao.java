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
package org.smooks.scribe.test.dao;

import org.smooks.scribe.Dao;
import org.smooks.scribe.Flushable;
import org.smooks.scribe.Locator;
import org.smooks.scribe.MappingDao;
import org.smooks.scribe.Queryable;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public interface FullInterfaceDao<T> extends Dao<T>, MappingDao<T>, Flushable, Locator, Queryable {

}
