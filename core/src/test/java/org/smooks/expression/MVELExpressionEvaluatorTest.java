/*-
 * ========================LICENSE_START=================================
 * Smooks Core
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
package org.smooks.expression;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MVELExpressionEvaluatorTest {

	MVELExpressionEvaluator evaluator;

	@Test
	public void test_getValue() {

		Object expected = new Object();

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("test", expected);

		evaluator.setExpression("return test");
		Object result = evaluator.getValue(map);

		assertSame("Expected object is not same as the result", expected, result);
	}

	@Test
	public void test_eval() {

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("value1", 1);
		map.put("value2", 2);
		map.put("value3", 3);

		evaluator.setExpression("value1 + value2 == value3");
		boolean result = evaluator.eval(map);

		assertTrue("Expected true", result);

		map.put("value3", 4);

		result = evaluator.eval(map);

		assertFalse("Expected false", result);
	}

	@Test
	public void test_vars_isdef() {

		Map<String, Object> map = new HashMap<String, Object>();

		evaluator.setExpression("VARS.isdef('value')");
		boolean result = evaluator.eval(map);

		assertFalse("Expected false", result);

		map.put("value", new Object());

		result = evaluator.eval(map);

		assertTrue("Expected true", result);

	}

	@Test
	public void test_vars_resolvable() {

		Map<String, Object> map = new HashMap<String, Object>();

		evaluator.setExpression("VARS.isResolveable('value')");
		boolean result = evaluator.eval(map);

		assertFalse("Expected false", result);

		map.put("value", new Object());

		result = evaluator.eval(map);

		assertTrue("Expected true", result);

	}

	@Test
	public void test_vars_unresolvable() {

		Map<String, Object> map = new HashMap<String, Object>();

		evaluator.setExpression("VARS.isUnresolveable('value')");
		boolean result = evaluator.eval(map);

		assertTrue("Expected true", result);

		map.put("value", new Object());

		result = evaluator.eval(map);

		assertFalse("Expected false", result);
	}

	@Test
	public void test_vars_get() {
		Object var = new Object();

		Map<String, Object> map = new HashMap<String, Object>();

		evaluator.setExpression("VARS.get('value') == null");
		boolean result = evaluator.eval(map);

		assertTrue("Expected true", result);

		map.put("value", var);

		result = evaluator.eval(map);

		assertFalse("Expected false", result);

		evaluator.setExpression("VARS.get('value')");

		Object resultObj = evaluator.getValue(map);

		assertSame(var, resultObj);

	}

	@Before
	public void setUp() throws Exception {
		evaluator = new MVELExpressionEvaluator();
	}

}
