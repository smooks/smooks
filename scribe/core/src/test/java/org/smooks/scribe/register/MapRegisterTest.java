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
package org.smooks.scribe.register;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
@Test(groups = "unit")
public class MapRegisterTest {


	public void test_builder() {
		Map<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("1", new Object());
		hashMap.put("2", new Object());

		MapDaoRegister<Object> mapRegister = MapDaoRegister.builder(hashMap).build();

		assertEquals(2, mapRegister.size());
		assertSame(hashMap.get("1"), mapRegister.getDao("1"));
		assertSame(hashMap.get("2"), mapRegister.getDao("2"));

		Map<String, Object> hashMap2 = new HashMap<String, Object>();
		hashMap2.put("3", new Object());
		hashMap2.put("4", new Object());

		mapRegister = MapDaoRegister.builder(hashMap).putAll(hashMap2).build();

		assertEquals(4, mapRegister.size());
		assertSame(hashMap.get("1"), mapRegister.getDao("1"));
		assertSame(hashMap.get("2"), mapRegister.getDao("2"));
		assertSame(hashMap2.get("3"), mapRegister.getDao("3"));
		assertSame(hashMap2.get("4"), mapRegister.getDao("4"));
	}

	public void test_getDAO() {
		Map<String, Object> hashMap = new HashMap<String, Object>();
		hashMap.put("1", new Object());
		hashMap.put("2", new Object());

		MapDaoRegister<Object> mapRegister = MapDaoRegister.newInstance(hashMap);

		assertEquals(2, mapRegister.size());
		assertSame(hashMap.get("1"), mapRegister.getDao("1"));
		assertSame(hashMap.get("2"), mapRegister.getDao("2"));
	}


	public void test_put_and_getDAO() {
		Object bean = new Object();

		MapDaoRegister<Object> mapRegister = MapDaoRegister.builder().put("1", bean).build();

		assertEquals(1, mapRegister.size());
		assertSame(bean, mapRegister.getDao("1"));
	}


	public void test_containsKey() {
		MapDaoRegister<Object> mapRegister = MapDaoRegister.builder().put("1", new Object()).build();

		assertTrue(mapRegister.containsKey("1"));
		assertFalse(mapRegister.containsKey("2"));
	}


	public void test_containsDAO() {
		Object bean1 = new Object();
		Object bean2 = new Object();

		MapDaoRegister<Object> mapRegister = MapDaoRegister.builder().put("1", bean1).build();

		assertTrue(mapRegister.containsDAO(bean1));
		assertFalse(mapRegister.containsDAO(bean2));
	}


	public void test_getAll() {
		Object bean1 = new Object();
		Object bean2 = new Object();

		MapDaoRegister<Object> mapRegister = MapDaoRegister.builder()
											.put("1", bean1)
											.put("2", bean2)
											.build();

		Map<String, Object> all = mapRegister.getAll();

		assertEquals(2, all.size());
		assertSame(bean1, all.get("1"));
		assertSame(bean2, all.get("2"));
	}


	public void test_equals() {
		Object bean1 = new Object();
		Object bean2 = new Object();
		Object bean3 = new Object();

		MapDaoRegister<Object> mapRegister1 = MapDaoRegister.builder()
												.put("1", bean1)
												.put("2", bean2)
												.build();

		MapDaoRegister<Object> mapRegister2 = MapDaoRegister.builder()
												.put("1", bean1)
												.put("2", bean2)
												.build();

		MapDaoRegister<Object> mapRegister3 = MapDaoRegister.builder().put("3", bean3).build();

		assertTrue(mapRegister1.equals(mapRegister2));
		assertFalse(mapRegister1.equals(mapRegister3));
	}


}
