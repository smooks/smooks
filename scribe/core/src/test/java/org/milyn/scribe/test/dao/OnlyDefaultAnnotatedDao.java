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
package org.milyn.scribe.test.dao;

import org.milyn.scribe.annotation.Dao;
import org.milyn.scribe.annotation.Delete;
import org.milyn.scribe.annotation.Insert;
import org.milyn.scribe.annotation.Update;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
@Dao
public interface OnlyDefaultAnnotatedDao {

	@Insert
	public void insertIt(Object entity);

	@Update
	public void updateIt(Object entity);

	@Delete
	public void deleteIt(Object entity);

}
