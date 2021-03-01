/*-
 * ========================LICENSE_START=================================
 * Scribe :: Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
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

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class MultiDaoRegisterTestCase {

	@Test
	public void test_builder() {
		Map<String, SingleDaoRegister<Object>> map = new HashMap<String, SingleDaoRegister<Object>>();
		map.put("1", new SingleDaoRegister<Object>(new Object()));
		map.put("2", new SingleDaoRegister<Object>(new Object()));

		MultiDaoRegister<Object> multiRegister = MultiDaoRegister.builder(map).build();

		assertEquals(2, multiRegister.size());
		assertSame(map.get("1").getDefaultDao(), multiRegister.getDao("1"));
		assertSame(map.get("2").getDefaultDao(), multiRegister.getDao("2"));

		Map<String, SingleDaoRegister<Object>> map2 = new HashMap<String, SingleDaoRegister<Object>>();
		map2.put("3", new SingleDaoRegister<Object>(new Object()));
		map2.put("4", new SingleDaoRegister<Object>(new Object()));

		multiRegister = MultiDaoRegister.builder(map).putAll(map2).build();

		assertEquals(4, multiRegister.size());
		assertSame(map.get("1").getDefaultDao(), multiRegister.getDao("1"));
		assertSame(map.get("2").getDefaultDao(), multiRegister.getDao("2"));
		assertSame(map2.get("3").getDefaultDao(), multiRegister.getDao("3"));
		assertSame(map2.get("4").getDefaultDao(), multiRegister.getDao("4"));

		DaoRegister<Object> daoRegister = new SingleDaoRegister<Object>(new Object());

		multiRegister = MultiDaoRegister.builder(map).put("r1", daoRegister).build();

		assertSame(daoRegister.getDefaultDao(), multiRegister.getDao("r1"));

	}

	@Test
	public void test_getDAO() {
		MapDaoRegister<Object> mapRegister1 = MapDaoRegister.builder().put("1", new Object()).build();
		MapDaoRegister<Object> mapRegister2 = MapDaoRegister.builder().put("1", new Object()).build();

		MultiDaoRegister<Object> multiRegister = MultiDaoRegister.builder()
													.put("mr1", mapRegister1)
													.put("mr2", mapRegister2)
													.build();

		assertSame(mapRegister1.getDao("1"), multiRegister.getDao("mr1.1"));
		assertSame(mapRegister2.getDao("1"), multiRegister.getDao("mr2.1"));
		assertNull(multiRegister.getDao("mr2.2"));
		assertNull(multiRegister.getDao("mr2."));
		assertNull(multiRegister.getDao("mr3.1"));
		assertNull(multiRegister.getDao(".1"));
	}

	@Test
	public void test_get() {
		DaoRegister<Object> register1 = new SingleDaoRegister<Object>(new Object());
		DaoRegister<Object> register2 = new SingleDaoRegister<Object>(new Object());

		Map<String, DaoRegister<Object>> in = new HashMap<String, DaoRegister<Object>>();
		in.put("1", register1);
		in.put("2", register2);

		MultiDaoRegister<Object> multiRegister = MultiDaoRegister.newInstance(in);

		Map<String, DaoRegister<Object>> regMap = multiRegister.getDaoRegisterMap();

		assertEquals(in, regMap);
	}

	@Test
	public void test_equals() {
		DaoRegister<Object> register1 = new SingleDaoRegister<Object>(new Object());
		DaoRegister<Object> register2 = new SingleDaoRegister<Object>(new Object());
		DaoRegister<Object> register3 = new SingleDaoRegister<Object>(new Object());

		MultiDaoRegister<Object> multiRegister1 = MultiDaoRegister.builder()
													.put("1", register1)
													.put("2", register2)
													.build();

		MultiDaoRegister<Object> multiRegister2 = MultiDaoRegister.builder()
													.put("1", register1)
													.put("2", register2)
													.build();

		MultiDaoRegister<Object> multiRegister3 = MultiDaoRegister.builder().put("3", register3).build();

		assertEquals(multiRegister1, multiRegister2);
		assertNotEquals(multiRegister1, multiRegister3);
	}
}
