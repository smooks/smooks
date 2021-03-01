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

import static org.junit.Assert.assertSame;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class AbstractDaoAdapterRegisterTestCase {
	
	@Test
	public void test_default_adaptable() {
		Object adaptable = new Object();

		Mock mock = new Mock(adaptable);

		assertSame(adaptable, mock.getDefaultDao());
		assertSame(adaptable, mock.getDao(null));
	}

	@Test
	public void test_mapped_adaptable() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("1", new Object());
		map.put("2", new Object());

		Mock mock = new Mock(map);

		assertSame(map.get("1"), mock.getDao("1"));
		assertSame(map.get("2"), mock.getDao("2"));
	}

	@Test
	public void test_mapped_and_default_adaptable() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("1", new Object());

		Object adaptable = new Object();

		Mock mock = new Mock(adaptable, map);

		assertSame(map.get("1"), mock.getDao("1"));
		assertSame(adaptable, mock.getDefaultDao());
		assertSame(adaptable, mock.getDao(null));
	}

	@Test(expected = IllegalStateException.class)
	public void test_no_default_adaptable() {
		Mock mock = new Mock(new HashMap<String, Object>());

		mock.getDefaultDao();
	}

	@Test(expected = IllegalStateException.class)
	public void test_unknown_mapped_adaptable() {
		Mock mock = new Mock(new HashMap<String, Object>());

		mock.getDao("");
	}

	private static class Mock extends AbstractDaoAdapterRegister<Object, Object> {

		public Mock(Map<String, ?> adaptableMap) {
			super(adaptableMap);
		}

		public Mock(Object defaultAdaptable,
					Map<String, ?> adaptableMap) {
			super(defaultAdaptable, adaptableMap);
		}

		public Mock(Object defaultAdaptable) {
			super(defaultAdaptable);
		}

		/* (non-Javadoc)
		 * @see org.smooks.scribe.register.AbstractDaoAdapterRegister#createAdapter(java.lang.Object)
		 */
		@Override
		protected Object createAdapter(Object adaptable) {
			return adaptable;
		}

	}

}
