/*-
 * ========================LICENSE_START=================================
 * Core
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
package org.smooks.engine.bean.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smooks.api.ExecutionContext;
import org.smooks.api.bean.context.BeanContext;
import org.smooks.api.bean.context.BeanIdStore;
import org.smooks.tck.MockExecutionContext;
import org.smooks.api.bean.lifecycle.BeanContextLifecycleEvent;
import org.smooks.api.bean.lifecycle.BeanContextLifecycleObserver;
import org.smooks.api.bean.repository.BeanId;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author maurice_zeijen
 */
public class BeanContextTestCase {

	private ExecutionContext executionContext;

	/**
	 * Tests adding a bean
	 */
	@Test
	public void test_add_bean() {
		Object bean1 = new MyGoodBean();
		Object bean2 = new MyGoodBean();

		BeanId beanId1 = getBeanIdStore().register("bean1");
		BeanId beanId2 = getBeanIdStore().register("bean2");

		BeanContext BeanContext = getBeanContext();

		assertEquals(2, BeanContext.getBeanMap().size());

		assertNull(BeanContext.getBean(beanId1));
		assertNull(BeanContext.getBean(beanId2));

		BeanContext.addBean(beanId1, bean1, null);
		BeanContext.addBean(beanId2, bean2, null);

		assertEquals(bean1, BeanContext.getBean(beanId1));
		assertEquals(bean2, BeanContext.getBean(beanId2));


		assertEquals(bean1, BeanContext.getBeanMap().get("bean1"));
		assertEquals(bean2, BeanContext.getBeanMap().get("bean2"));
	}


	/**
	 * Test adding and replacing a bean
	 */
	@Test
	public void test_add_and_overwrite_bean() {
		Object bean1 = new MyGoodBean();
		Object newBean1 = new MyGoodBean();

		BeanId beanId1 = getBeanIdStore().register("bean1");

		BeanContext BeanContext = getBeanContext();

		assertNull(BeanContext.getBean(beanId1));

		BeanContext.addBean(beanId1, bean1, null);

		assertEquals(bean1, BeanContext.getBean(beanId1));

		BeanContext.addBean(beanId1, newBean1, null);

		assertEquals(newBean1, BeanContext.getBean(beanId1));
	}

	/**
	 * Test adding and changing a bean
	 */
	@Test
	public void test_change_bean() {
		Object bean1 = new MyGoodBean();
		Object newBean1 = new MyGoodBean();

		BeanId beanId1 = getBeanIdStore().register("bean1");
		BeanId beanIdNE = getBeanIdStore().register("notExisting");


		BeanContext BeanContext = getBeanContext();

		BeanContext.addBean(beanId1, bean1, null);

		assertEquals(bean1, BeanContext.getBean(beanId1));

		BeanContext.changeBean(beanId1, newBean1, null);

		assertEquals(newBean1, BeanContext.getBean(beanId1));

		boolean fired = false;

		try {
			BeanContext.changeBean(beanIdNE, new Object(), null);
		} catch (IllegalStateException e) {
			fired = true;
		}
		assertTrue(fired, "The exception did not fire");
	}

	/**
	 * Test adding and replacing a bean
	 */
	@Test
	public void test_bean_map() {
		Object bean1 = new Object();
		Object bean2 = new Object();
		Object bean3 = new Object();
		Object bean4 = new Object();

		BeanId beanId1 = getBeanIdStore().register("bean1");

		BeanContext BeanContext = getBeanContext();
		Map<String, Object> beanMap = BeanContext.getBeanMap();

		BeanContext.addBean(beanId1, bean1, null);

		assertEquals(1, beanMap.size());
		assertEquals(bean1, beanMap.get(beanId1.getName()));

		beanMap.put("bean2", bean2);

		BeanId beanId2 = BeanContext.getBeanId("bean2");

		assertEquals(bean2, BeanContext.getBean(beanId2));
		assertEquals(bean2, beanMap.get(beanId2.getName()));

		assertTrue(beanMap.containsKey("bean2"));
		assertFalse(beanMap.containsKey("x"));

		assertTrue(beanMap.containsValue(bean1));
		assertFalse(beanMap.containsValue(new Object()));

		assertFalse(beanMap.isEmpty());

		// Mark bean as being "out of context" so we can remove it...
		BeanContext.setBeanInContext(beanId1, false);

		beanMap.remove("bean1");

		assertNull(beanMap.get("bean1"));
		assertNull(BeanContext.getBean("bean1"));

		assertEquals(2, beanMap.entrySet().size());
		assertEquals(2, beanMap.keySet().size());
		assertEquals(2, beanMap.values().size());

		Map<String, Object> toPut = new HashMap<String, Object>();
		toPut.put("bean3", bean3);
		toPut.put("bean4", bean4);

		beanMap.putAll(toPut);

		assertEquals(4, beanMap.size());
		assertEquals(bean3, BeanContext.getBean("bean3"));
		assertEquals(bean4, BeanContext.getBean("bean4"));

		beanMap.clear();

		assertNull(BeanContext.getBean("bean1"));
		assertNull(BeanContext.getBean("bean2"));
		assertNull(BeanContext.getBean("bean3"));
		assertNull(BeanContext.getBean("bean4"));
	}

	@BeforeEach
	public void setUp() throws Exception {
		executionContext = new MockExecutionContext();
	}


	/**
	 *
	 */
	private BeanIdStore getBeanIdStore() {
		return executionContext.getApplicationContext().getBeanIdStore();
	}

	/**
	 *
	 */
	private BeanContext getBeanContext() {
		return executionContext.getBeanContext();
	}


	public static class MockRepositoryBeanLifecycleObserver implements BeanContextLifecycleObserver {

		private boolean fired;

		public boolean isFired() {
			return fired;
		}

		public void reset() {
			fired = false;
		}

		@Override
		public void onBeanLifecycleEvent(BeanContextLifecycleEvent event) {
			fired = true;
		}
	}

}
