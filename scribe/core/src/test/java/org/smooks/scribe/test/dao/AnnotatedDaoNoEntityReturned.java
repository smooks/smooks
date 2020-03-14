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

import org.smooks.scribe.annotation.Dao;
import org.smooks.scribe.annotation.Delete;
import org.smooks.scribe.annotation.Insert;
import org.smooks.scribe.annotation.ReturnsNoEntity;
import org.smooks.scribe.annotation.Update;


/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
@Dao
public interface AnnotatedDaoNoEntityReturned {

	@Insert
	@ReturnsNoEntity
	Object persistIt(final Object entity);

	@Update
	@ReturnsNoEntity
	Object mergeIt(final Object merge);

	@Delete
	@ReturnsNoEntity
	Object deleteIt(final Object delete);

}