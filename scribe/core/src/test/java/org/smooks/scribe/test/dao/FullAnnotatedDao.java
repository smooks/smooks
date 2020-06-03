/*-
 * ========================LICENSE_START=================================
 * Scribe :: Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0 or,
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
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
