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
package org.milyn.cartridge.javabean.factory;

import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.milyn.cartridge.javabean.factory.Factory;
import org.milyn.cartridge.javabean.factory.MVELFactory;
import org.milyn.container.ExecutionContext;
import org.milyn.container.MockExecutionContext;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class MVELFactoryTest  extends TestCase {

	public void test_create() throws Exception {

		ExecutionContext context = new MockExecutionContext();

		Factory<Map<?, ?>> factory = new MVELFactory<Map<?, ?>>("new java.util.TreeMap");

		Map<?, ?> map = factory.create(context);


		assertNotNull(map);
		assertTrue(map instanceof TreeMap);
	}

}
