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
package org.smooks.engine.resource.config;

import org.junit.Test;
import org.smooks.api.resource.config.ConfigSearch;
import org.smooks.api.resource.config.ResourceConfig;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DefaultConfigSearchTest {

	@Test
	public void test() {
		ResourceConfig resourceConfig = new DefaultResourceConfig();
		ConfigSearch search = new DefaultConfigSearch();
		
		assertTrue(search.matches(resourceConfig));

		resourceConfig.setSelector("a");
		assertTrue(search.matches(resourceConfig));
		assertTrue(search.selector("a").matches(resourceConfig));
		assertFalse(search.selector("b").matches(resourceConfig));
		search.selector(null); // clear it
		assertTrue(search.matches(resourceConfig));

		resourceConfig.setExtendedConfigNS("http://x/y");
		assertTrue(search.matches(resourceConfig));
		assertTrue(search.configNS("http://x/y").matches(resourceConfig));
		assertTrue(search.configNS("http://x").matches(resourceConfig));
		assertFalse(search.configNS("http://x/z").matches(resourceConfig));
		search.configNS(null); // clear it
		assertTrue(search.matches(resourceConfig));

		resourceConfig.getSelectorPath().setSelectorNamespaceURI("http://x/y");
		assertTrue(search.selectorNS("http://x/y").matches(resourceConfig));
		assertFalse(search.selectorNS("http://x/z").matches(resourceConfig));
		search.selectorNS(null); // clear it
		assertTrue(search.matches(resourceConfig));

		resourceConfig.setResource("a");
		assertTrue(search.resource("a").matches(resourceConfig));
		assertFalse(search.resource("b").matches(resourceConfig));
		search.resource(null); // clear it
		assertTrue(search.matches(resourceConfig));
		
		search.param("a", "1");
		assertFalse(search.matches(resourceConfig));
		resourceConfig.setParameter("a", "1");
		assertTrue(search.matches(resourceConfig));		
	}
}
