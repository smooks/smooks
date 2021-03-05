/*-
 * ========================LICENSE_START=================================
 * Commons
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
// HTMLEntityLookupTest.java

package org.smooks.xml;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * HTMLEntityLookupTest
 * <p>
 * Relations: HTMLEntityLookup extends java.lang.Object <br>
 *
 * @author Tom Fennelly
 * @see org.smooks.xml.HTMLEntityLookup
 */

public class HTMLEntityLookupTestCase {


	/**
	 * Test method: java.lang.Character getCharacterCode(String)
	 */
	@Test
	public void testGetCharacterCode() {
		assertEquals('\u00A4', HTMLEntityLookup.getCharacterCode("curren") .charValue());
		assertEquals('\u0026', HTMLEntityLookup.getCharacterCode("amp") .charValue());
		assertEquals('\u00A0', HTMLEntityLookup.getCharacterCode("nbsp") .charValue());
        assertEquals('\'', HTMLEntityLookup.getCharacterCode("apos").charValue());
        assertEquals('\u0022', HTMLEntityLookup.getCharacterCode("quot").charValue());
	}

	/**
	 * Test method: String getEntityRef(char)
	 */
	@Test
	public void testGetEntityRef() {
		assertEquals("curren", HTMLEntityLookup.getEntityRef('\u00A4'));
		assertEquals("amp", HTMLEntityLookup.getEntityRef('\u0026'));
		assertEquals("nbsp", HTMLEntityLookup.getEntityRef('\u00A0'));
        assertEquals("apos", HTMLEntityLookup.getEntityRef('\''));
		assertEquals("quot", HTMLEntityLookup.getEntityRef('\u0022'));
	}
}
