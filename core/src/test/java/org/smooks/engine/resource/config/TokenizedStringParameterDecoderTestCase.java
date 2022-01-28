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
package org.smooks.engine.resource.config;

import org.junit.jupiter.api.Test;
import org.smooks.api.resource.config.Parameter;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.tck.MockApplicationContext;
import org.smooks.engine.injector.Scope;
import org.smooks.engine.lifecycle.PostConstructLifecyclePhase;
import org.smooks.engine.lookup.LifecycleManagerLookup;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TokenizedStringParameterDecoderTestCase {

	/*
	 * Class under test for Object decodeValue(String)
	 */
	@Test
	public void testDecodeValue_string_list() {
		Collection collection = getParameter("string-list", "a,b,c,d ,");
		assertTrue((collection instanceof List), "Expected to get back a java.util.List parameter");
		List paramsList = (List)collection;
		assertTrue(paramsList.contains("a"), "Expected java.util.List to contain value.");
		assertTrue(paramsList.contains("b"), "Expected java.util.List to contain value.");
		assertTrue(paramsList.contains("c"), "Expected java.util.List to contain value.");
		assertTrue(paramsList.contains("d"), "Expected java.util.List to contain value.");
		assertFalse(paramsList.contains("e"), "Expected java.util.List to NOT contain value.");
	}

	/*
	 * Class under test for Object decodeValue(String)
	 */
	@Test
	public void testDecode_string_hashset() {
		Collection collection = getParameter("string-hashset", "a,b,c,d ,");
		assertTrue((collection instanceof HashSet), "Expected to get back a java.util.List parameter");
		HashSet paramsHashSet = (HashSet)collection;
		assertTrue(paramsHashSet.contains("a"), "Expected java.util.HashSet to contain value.");
		assertTrue(paramsHashSet.contains("b"), "Expected java.util.HashSet to contain value.");
		assertTrue(paramsHashSet.contains("c"), "Expected java.util.HashSet to contain value.");
		assertTrue(paramsHashSet.contains("d"), "Expected java.util.HashSet to contain value.");
		assertFalse(paramsHashSet.contains("e"), "Expected java.util.HashSet to NOT contain value.");
	}
	
	public Collection getParameter(String type, String value) {
        ResourceConfig decoderConfig = new DefaultResourceConfig(Parameter.PARAM_TYPE_PREFIX + type, new Properties(), "org.smooks.engine.resource.config.TokenizedStringParameterDecoder");
        decoderConfig.setParameter(Parameter.PARAM_TYPE_PREFIX, type);
		TokenizedStringParameterDecoder tokenizedStringParameterDecoder = new TokenizedStringParameterDecoder();
		MockApplicationContext mockApplicationContext = new MockApplicationContext();
		mockApplicationContext.getRegistry().lookup(new LifecycleManagerLookup()).applyPhase(tokenizedStringParameterDecoder, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry(), decoderConfig, tokenizedStringParameterDecoder)));
        
		return (Collection)tokenizedStringParameterDecoder.decodeValue(value);
	}

}
