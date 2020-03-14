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

import java.util.Collection;
import java.util.Map;

import org.smooks.scribe.annotation.Dao;
import org.smooks.scribe.annotation.Delete;
import org.smooks.scribe.annotation.Flush;
import org.smooks.scribe.annotation.Insert;
import org.smooks.scribe.annotation.Lookup;
import org.smooks.scribe.annotation.LookupByQuery;
import org.smooks.scribe.annotation.Param;
import org.smooks.scribe.annotation.Update;


/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
@Dao
public interface FullAnnotatedDao {

	@Insert(isDefault = true)
	Object insertIt(final Object entity);

	@Insert
	Object insertIt2(final Object entity);

	@Insert(name = "insertIt3")
	Object insertItDiff(final Object entity);

	@Update(isDefault = true)
	Object updateIt(final Object entity);

	@Update
	Object updateIt2(final Object entity);

	@Update(name = "updateIt3")
	Object updateItDiff(final Object entity);

	@Delete(isDefault = true)
	Object deleteIt(Object entity);

	@Delete
	Object deleteIt2(Object entity);

	@Delete(name = "deleteIt3")
	Object deleteItDiff(Object entity);

	@Flush
	void flushIt();

	@LookupByQuery
	Collection<?> findByQuery(String query, Object[] parameters);

	@LookupByQuery
	Collection<?> findByQuery(String query, Map<String, Object> parameters);

	@Lookup(name="id")
	Collection<?> findById(@Param("id") Long id);

	@Lookup(name="name")
	Collection<?> findByName(@Param("last") String surname, @Param("first") String firstname);

	@Lookup(name="positional")
	Collection<?> findBySomething(String param1, int param2, boolean param3);

	@Lookup
	Collection<?> findBy(String param);
}