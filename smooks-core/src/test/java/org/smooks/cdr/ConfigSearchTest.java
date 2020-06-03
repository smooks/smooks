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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ConfigSearchTest {

	@Test
	public void test() {
		SmooksResourceConfiguration config = new SmooksResourceConfiguration();
		ConfigSearch search = new ConfigSearch();
		
		assertTrue(search.matches(config));

		config.setSelector("a");
		assertTrue(search.matches(config));
		assertTrue(search.selector("a").matches(config));
		assertFalse(search.selector("b").matches(config));
		search.selector(null); // clear it
		assertTrue(search.matches(config));

		config.setExtendedConfigNS("http://x/y");
		assertTrue(search.matches(config));
		assertTrue(search.configNS("http://x/y").matches(config));
		assertTrue(search.configNS("http://x").matches(config));
		assertFalse(search.configNS("http://x/z").matches(config));
		search.configNS(null); // clear it
		assertTrue(search.matches(config));

		config.setSelectorNamespaceURI("http://x/y");
		assertTrue(search.selectorNS("http://x/y").matches(config));
		assertFalse(search.selectorNS("http://x/z").matches(config));
		search.selectorNS(null); // clear it
		assertTrue(search.matches(config));

		config.setResource("a");
		assertTrue(search.resource("a").matches(config));
		assertFalse(search.resource("b").matches(config));
		search.resource(null); // clear it
		assertTrue(search.matches(config));
		
		search.param("a", "1");
		assertFalse(search.matches(config));
		config.setParameter("a", "1");
		assertTrue(search.matches(config));		
	}
}
