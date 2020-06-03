/*-
 * ========================LICENSE_START=================================
 * Smooks Core
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
package org.smooks.cdr;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.smooks.cdr.annotation.Configurator;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokenizedStringParameterDecoderTest {

	/*
	 * Class under test for Object decodeValue(String)
	 */
	@Test
	public void testDecodeValue_string_list() {
		Collection collection = getParameter("string-list", "a,b,c,d ,");
		assertTrue("Expected to get back a java.util.List parameter", (collection instanceof List));
		List paramsList = (List)collection;
		assertTrue("Expected java.util.List to contain value.", paramsList.contains("a"));
		assertTrue("Expected java.util.List to contain value.", paramsList.contains("b"));
		assertTrue("Expected java.util.List to contain value.", paramsList.contains("c"));
		assertTrue("Expected java.util.List to contain value.", paramsList.contains("d"));
		assertFalse("Expected java.util.List to NOT contain value.", paramsList.contains("e"));
	}

	/*
	 * Class under test for Object decodeValue(String)
	 */
	@Test
	public void testDecode_string_hashset() {
		Collection collection = getParameter("string-hashset", "a,b,c,d ,");
		assertTrue("Expected to get back a java.util.List parameter", (collection instanceof HashSet));
		HashSet paramsHashSet = (HashSet)collection;
		assertTrue("Expected java.util.HashSet to contain value.", paramsHashSet.contains("a"));
		assertTrue("Expected java.util.HashSet to contain value.", paramsHashSet.contains("b"));
		assertTrue("Expected java.util.HashSet to contain value.", paramsHashSet.contains("c"));
		assertTrue("Expected java.util.HashSet to contain value.", paramsHashSet.contains("d"));
		assertFalse("Expected java.util.HashSet to NOT contain value.", paramsHashSet.contains("e"));
	}
	
	public Collection getParameter(String type, String value) {
        SmooksResourceConfiguration decoderConfig;
        TokenizedStringParameterDecoder decoder;
		
        decoderConfig = new SmooksResourceConfiguration(Parameter.PARAM_TYPE_PREFIX + type, "org.smooks.cdr.TokenizedStringParameterDecoder");
        decoderConfig.setParameter(Parameter.PARAM_TYPE_PREFIX, type);
        decoder = Configurator.configure(new TokenizedStringParameterDecoder(), decoderConfig);
        
		return (Collection)decoder.decodeValue(value);
	}

}
