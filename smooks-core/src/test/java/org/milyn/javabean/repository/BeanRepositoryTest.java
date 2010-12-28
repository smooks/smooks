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

package org.milyn.javabean.repository;

import junit.framework.TestCase;
import org.milyn.container.ExecutionContext;
import org.milyn.container.MockExecutionContext;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tfennelly
 */
@SuppressWarnings("deprecation")
public class BeanRepositoryTest extends TestCase {

	private ExecutionContext executionContext;

	/**
	 * Tests adding a bean
	 */
	public void test_add_bean() {
        Object bean1 = new MyGoodBean();
        Object bean2 = new MyGoodBean();

        BeanId beanId1 = getBeanIdRegister().register("bean1");
        BeanId beanId2 = getBeanIdRegister().register("bean2");

        BeanRepository beanRepository = getBeanRepository();

        assertEquals(2, beanRepository.getBeanMap().size());

        assertNull(beanRepository.getBean(beanId1));
        assertNull(beanRepository.getBean(beanId2));

        beanRepository.addBean(beanId1, bean1);
        beanRepository.addBean(beanId2, bean2);

        assertEquals(bean1, beanRepository.getBean(beanId1));
        assertEquals(bean2, beanRepository.getBean(beanId2));


        assertEquals(bean1, beanRepository.getBeanMap().get("bean1"));
        assertEquals(bean2, beanRepository.getBeanMap().get("bean2"));
    }




	/**
	 * Test adding and replacing a bean
	 */
	public void test_add_and_overwrite_bean() {
        Object bean1 = new MyGoodBean();
        Object newBean1 = new MyGoodBean();

        BeanId beanId1 = getBeanIdRegister().register("bean1");

        BeanRepository beanRepository = getBeanRepository();

        assertNull(beanRepository.getBean(beanId1));

        beanRepository.addBean( beanId1, bean1);

        assertEquals(bean1, beanRepository.getBean(beanId1));

        beanRepository.addBean( beanId1, newBean1);

        assertEquals(newBean1, beanRepository.getBean(beanId1));
    }

	/**
	 * Test adding and changing a bean
	 */
	public void test_change_bean() {
        Object bean1 = new MyGoodBean();
        Object newBean1 = new MyGoodBean();

        BeanId beanId1 = getBeanIdRegister().register("bean1");
        BeanId beanIdNE = getBeanIdRegister().register("notExisting");


        BeanRepository beanRepository = getBeanRepository();

        beanRepository.addBean(beanId1, bean1);

        assertEquals(bean1, beanRepository.getBean(beanId1));

        beanRepository.changeBean(beanId1, newBean1);

        assertEquals(newBean1, beanRepository.getBean(beanId1));

        boolean fired = false;

        try {
        	beanRepository.changeBean(beanIdNE, new Object());
        } catch (IllegalStateException e) {
        	fired = true;
		}
        assertTrue("The exception did not fire", fired);
    }

	/**
	 * Test adding and replacing a bean
	 */
	public void test_bean_map() {
		Object bean1 = new Object();
		Object bean2 = new Object();
		Object bean3 = new Object();
		Object bean4 = new Object();

		BeanId beanId1 = getBeanIdRegister().register("bean1");

		BeanRepository beanRepository = getBeanRepository();
		Map<String, Object> beanMap = beanRepository.getBeanMap();

		beanRepository.addBean(beanId1, bean1);

		assertEquals(1, beanMap.size());
		assertEquals(bean1, beanMap.get(beanId1.getName()));

		beanMap.put("bean2", bean2);

		BeanId beanId2 = beanRepository.getBeanId("bean2");

		assertEquals(bean2, beanRepository.getBean(beanId2));
		assertEquals(bean2, beanMap.get(beanId2.getName()));

		assertTrue(beanMap.containsKey("bean2"));
		assertFalse(beanMap.containsKey("x"));

		assertTrue(beanMap.containsValue(bean1));
		assertFalse(beanMap.containsValue(new Object()));

		assertFalse(beanMap.isEmpty());

        // Mark bean as being "out of context" so we can remove it...
        beanRepository.setBeanInContext(beanId1, false);

		beanMap.remove("bean1");

		assertNull(beanMap.get("bean1"));
		assertNull(beanRepository.getBean("bean1"));

		assertEquals(2, beanMap.entrySet().size());
		assertEquals(2, beanMap.keySet().size());
		assertEquals(2, beanMap.values().size());

		Map<String, Object> toPut = new HashMap<String, Object>();
		toPut.put("bean3", bean3);
		toPut.put("bean4", bean4);

		beanMap.putAll(toPut);

		assertEquals(4, beanMap.size());
		assertEquals(bean3, beanRepository.getBean("bean3"));
		assertEquals(bean4, beanRepository.getBean("bean4"));

		beanMap.clear();

		assertNull(beanRepository.getBean("bean1"));
		assertNull(beanRepository.getBean("bean2"));
		assertNull(beanRepository.getBean("bean3"));
		assertNull(beanRepository.getBean("bean4"));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		executionContext = new MockExecutionContext();
	}


	/**
	 *
	 */
	private BeanIdRegister getBeanIdRegister() {
		BeanRepositoryManager beanRepositoryManager = getRepositoryManager();

        return beanRepositoryManager.getBeanIdRegister();
	}

	/**
	 *
	 */
	private BeanRepository getBeanRepository() {
        return BeanRepositoryManager.getBeanRepository(executionContext);
	}

	/**
	 * @return
	 */
	private BeanRepositoryManager getRepositoryManager() {
		return BeanRepositoryManager.getInstance(executionContext.getContext());
	}
}
