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
package org.milyn.scribe.register;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
@Test(groups = "unit")
public class MultiDaoRegisterTest {

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

		assertTrue(multiRegister1.equals(multiRegister2));
		assertFalse(multiRegister1.equals(multiRegister3));
	}
}
