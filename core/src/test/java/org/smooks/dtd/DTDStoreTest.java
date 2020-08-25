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
package org.smooks.dtd;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.profile.DefaultProfileSet;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 
 * @author tfennelly
 */
public class DTDStoreTest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DTDStore.class);

	@Test
	public void testGetEmptyAnyMixedEtcElements() {
        DefaultProfileSet profileSet = new DefaultProfileSet("device3");

        profileSet.addProfiles(new String[] {"prof1", "prof2", "prof3"});
		DTDStore.addDTD(profileSet, getClass().getResourceAsStream("xhtml1-transitional.dtd"));
		DTDStore.DTDObjectContainer dtdContainer = DTDStore.getDTDObject(profileSet);
		
		String[] elements = dtdContainer.getEmptyElements();
        assertArrayEquals(new String[]{"basefont", "area", "link", "isindex", "col", "base", "meta", "img", "br", "hr", "param", "input"}, elements);
		elements = dtdContainer.getAnyElements();
        assertArrayEquals(new String[]{}, elements);
		elements = dtdContainer.getMixedElements();
        assertArrayEquals(new String[]{"button", "textarea", "em", "small", "noframes", "bdo", "form", "label", "dt", "span", "title", "strong", "script", "div", "blockquote", "kbd", "body", "ins", "dd", "fieldset", "big", "code", "option", "u", "s", "q", "p", "i", "pre", "caption", "b", "a", "style", "applet", "tt", "th", "center", "td", "samp", "font", "dfn", "noscript", "object", "sup", "h6", "h5", "h4", "h3", "h2", "h1", "iframe", "strike", "sub", "acronym", "del", "li", "cite", "var", "legend", "abbr", "address"}, elements);
		elements = dtdContainer.getPCDataElements();
        assertArrayEquals(new String[]{}, elements);
	}

	@Test
	public void testChildElements() {
        DefaultProfileSet profileSet = new DefaultProfileSet("device3");

        profileSet.addProfiles(new String[] {"prof1", "prof2", "prof3"});
		DTDStore.addDTD(profileSet, getClass().getResourceAsStream("xhtml1-transitional.dtd"));
		DTDStore.DTDObjectContainer dtdContainer = DTDStore.getDTDObject(profileSet);
		List l1 = dtdContainer.getChildElements("html");
		List l2 = dtdContainer.getChildElements("html");

        assertSame(l1, l2);
		assertEquals(2, dtdContainer.getChildElements("html").size());
		assertEquals(64, dtdContainer.getChildElements("body").size());
		assertEquals(0, dtdContainer.getChildElements("img").size());
		assertEquals(40, dtdContainer.getChildElements("b").size());
        assertNull(dtdContainer.getChildElements("xxxx"));
	}

	@Test
	public void testElementAttributes() {
        DefaultProfileSet profileSet = new DefaultProfileSet("device3");

        profileSet.addProfiles(new String[] {"prof1", "prof2", "prof3"});
		DTDStore.addDTD(profileSet, getClass().getResourceAsStream("xhtml1-transitional.dtd"));
		DTDStore.DTDObjectContainer dtdContainer = DTDStore.getDTDObject(profileSet);
		List l1 = null;
		List l2 = null;
		try {
			l1 = dtdContainer.getElementAttributes("html");
			l2 = dtdContainer.getElementAttributes("html");
		} catch (ElementNotDefined e) {
			LOGGER.debug( "ElementNotDefined exception was throws: ", e);
			fail(e.getMessage());
		}

        assertSame(l1, l2);
		assertEquals(5, l1.size());
		assertTrue(l1.contains("dir"));
		assertTrue(l1.contains("xml:lang"));
		assertTrue(l1.contains("lang"));
		assertTrue(l1.contains("xmlns"));
		assertTrue(l1.contains("id"));

		try {
			dtdContainer.getElementAttributes("xxxxx");
			fail("Expected ElementNotDefined");
		} catch (ElementNotDefined e) {
			// OK
		}
	}
	
	private void print(String name) {
        DefaultProfileSet profileSet = new DefaultProfileSet("device3");

        profileSet.addProfiles(new String[] {"prof1", "prof2", "prof3"});
		DTDStore.addDTD(profileSet, getClass().getResourceAsStream(name));
		DTDStore.DTDObjectContainer dtdContainer = DTDStore.getDTDObject(profileSet);
		
		LOGGER.debug("-------- " + name + " ---------");
		LOGGER.debug(Arrays.asList(dtdContainer.getEmptyElements()).toString());
	}
	
	@Test
	public void testPrint() {
		print("xhtml1-transitional.dtd");
		print("html32.xml.dtd");
		print("wml_1_1.dtd");
		print("wml12.dtd");
		print("wml13.dtd");
	}
}
