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

import static junit.framework.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
@Test(groups = "unit")
public class AbstractDaoAdapterRegisterTest {


	public void test_default_adaptable() {
		Object adaptable = new Object();

		Mock mock = new Mock(adaptable);

		assertSame(adaptable, mock.getDefaultDao());
		assertSame(adaptable, mock.getDao(null));
	}

	public void test_mapped_adaptable() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("1", new Object());
		map.put("2", new Object());

		Mock mock = new Mock(map);

		assertSame(map.get("1"), mock.getDao("1"));
		assertSame(map.get("2"), mock.getDao("2"));
	}

	public void test_mapped_and_default_adaptable() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("1", new Object());

		Object adaptable = new Object();

		Mock mock = new Mock(adaptable, map);

		assertSame(map.get("1"), mock.getDao("1"));
		assertSame(adaptable, mock.getDefaultDao());
		assertSame(adaptable, mock.getDao(null));
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void test_no_default_adaptable() {
		Mock mock = new Mock(new HashMap<String, Object>());

		mock.getDefaultDao();
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void test_unknown_mapped_adaptable() {
		Mock mock = new Mock(new HashMap<String, Object>());

		mock.getDao("");
	}

	private static class Mock extends AbstractDaoAdapterRegister<Object, Object> {

		public Mock(Map<String, ? extends Object> adaptableMap) {
			super(adaptableMap);
		}

		public Mock(Object defaultAdaptable,
				Map<String, ? extends Object> adaptableMap) {
			super(defaultAdaptable, adaptableMap);
		}

		public Mock(Object defaultAdaptable) {
			super(defaultAdaptable);
		}

		/* (non-Javadoc)
		 * @see org.milyn.scribe.register.AbstractDaoAdapterRegister#createAdapter(java.lang.Object)
		 */
		@Override
		protected Object createAdapter(Object adaptable) {
			return adaptable;
		}

	}

}
