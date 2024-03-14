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
package org.smooks.support;

import org.junit.jupiter.api.Test;
import org.smooks.api.ExecutionContext;
import org.smooks.testkit.MockExecutionContext;
import org.smooks.api.TypedKey;
import org.smooks.io.payload.FilterResult;
import org.smooks.io.payload.JavaResult;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

	public class MultiLineToStringBuilderTestCase {

	private static final String NL = System.getProperty("line.separator");

	@Test
	public void test() {

		ExecutionContext context = new MockExecutionContext();

		JavaResult dummyResult = new JavaResult(true);

		FilterResult.setResults(context, dummyResult);

		context.getBeanContext().addBean("string", "blaat", null);
		context.getBeanContext().addBean("emptyMap", Collections.emptyMap(), null);
		context.getBeanContext().addBean("emptyList", Collections.emptyList(), null);
		context.getBeanContext().addBean("emptyArray", new String[0], null);

		Map<String, String> stringMap = new LinkedHashMap<String, String>();
		stringMap.put("v1", "some text");
		stringMap.put("v2", "other text");
		stringMap.put("v3", null);

		context.getBeanContext().addBean("stringMap", stringMap, null);

		List<Integer> integerList = new ArrayList<Integer>();
		integerList.add(1);
		integerList.add(2);
		integerList.add(null);

		context.getBeanContext().addBean("integerList", integerList, null);

		context.getBeanContext().addBean("stringArray", new String[] {"a1", "a2", "a3", null}, null);

		Map<String, Object> objectMap = new LinkedHashMap<String, Object>();
		objectMap.put("self", objectMap);

		Map<String, Object> object2Map = new LinkedHashMap<String, Object>();
		object2Map.put("parent", objectMap);

		objectMap.put("map", object2Map);

		List<Object> list = new ArrayList<Object>();

		list.add(list);
		list.add(objectMap);

		object2Map.put("list", list);

		context.getBeanContext().addBean("objectMap", objectMap, null);

		context.put(TypedKey.of("multiline"), "hello\nworld");

		String actual = MultiLineToStringBuilder.toString(context);

		System.out.println(actual);

		String expected =
			"BeanContext : {" + NL +
			"   \"string\" : \"blaat\"," + NL +
			"   \"emptyMap\" : {}," + NL +
			"   \"emptyList\" : []," + NL +
			"   \"emptyArray\" : []," + NL +
			"   \"stringMap\" : {" + NL +
			"      \"v1\" : \"some text\"," + NL +
			"      \"v2\" : \"other text\"," + NL +
			"      \"v3\" : NULL" + NL +
			"   }," + NL +
			"   \"integerList\" : [" + NL +
			"      1," + NL +
			"      2," + NL +
			"      NULL" + NL +
			"   ]," + NL +
			"   \"stringArray\" : [" + NL +
			"      \"a1\"," + NL +
			"      \"a2\"," + NL +
			"      \"a3\"," + NL +
			"      NULL" + NL +
			"   ]," + NL +
			"   \"objectMap\" : {" + NL +
			"      \"self\" : THIS," + NL +
			"      \"map\" : {" + NL +
			"         \"parent\" : PARENT-1," + NL +
			"         \"list\" : [" + NL +
			"            THIS," + NL +
			"            PARENT-2" + NL +
			"         ]" + NL +
			"      }" + NL +
			"   }" + NL +
			"}" + NL +
			NL +
			"Attributes : {" + NL +
			"   \"multiline\" : \"hello" + NL +
			"               world\"," + NL +
			"}";


		assertEquals(expected, actual);
	}

}
