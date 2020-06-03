/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
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
package org.smooks.profile;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.SAXException;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 * @author tfennelly
 */
public class DefaultProfileConfigDigesterTest {

	@Test
	public void testParse_exception_null_stream() {
		DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();

		try {
			digester.parse(null);
			fail("failed to throw arg exception on null stream");
		} catch (IllegalArgumentException e) {
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testParse_fail_no_name() {
		DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();
		InputStream stream = getClass().getResourceAsStream(
				"profiles_fail_no_name.xml");

		try {
			digester.parse(stream);
			fail("failed to throw SAXException on no name");
		} catch (SAXException e) {
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testParse_fail_no_list() {
		DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();
		InputStream stream = getClass().getResourceAsStream(
				"profiles_fail_no_list.xml");

		try {
			digester.parse(stream);
			fail("failed to throw SAXException on no list");
		} catch (SAXException e) {
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testParse_fail_no_attribs() {
		DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();
		InputStream stream = getClass().getResourceAsStream(
				"profiles_fail_no_attribs.xml");

		try {
			digester.parse(stream);
			fail("failed to throw SAXException on no attributes");
		} catch (SAXException e) {
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testParse_fail_empty_list() {
		DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();
		InputStream stream = getClass().getResourceAsStream(
				"profiles_fail_empty_list.xml");

		try {
			digester.parse(stream);
			fail("failed to throw SAXException on empty list");
		} catch (SAXException e) {
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testParse_fail_empty_name() {
		DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();
		InputStream stream = getClass().getResourceAsStream(
				"profiles_fail_empty_name.xml");

		try {
			digester.parse(stream);
			fail("failed to throw SAXException on empty name");
		} catch (SAXException e) {
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testParse_fail_bad_list() {
		DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();
		InputStream stream = getClass().getResourceAsStream(
				"profiles_fail_bad_list.xml");

		try {
			digester.parse(stream);
			fail("failed to throw SAXException on bad list");
		} catch (SAXException e) {
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testParse_fail_no_profile() {
		DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();
		InputStream stream = getClass().getResourceAsStream(
				"profiles_fail_no_profile.xml");

		try {
			digester.parse(stream);
			fail("failed to throw SAXException on no profile");
		} catch (SAXException e) {
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testParse_success_all_list_seperators() {
		DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();
		InputStream stream = getClass().getResourceAsStream(
				"profiles_success_all_list_separators.xml");

		try {
			digester.parse(stream);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected Exception: " + e.getMessage());
		}
	}

	@Test
	public void testParse_success_two_profiles() {
		DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();
		InputStream stream = getClass().getResourceAsStream(
				"profiles_success_two_profiles.xml");
		ProfileStore store = null;

		try {
			store = digester.parse(stream);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected Exception: " + e.getMessage());
		}
		try {
			ProfileSet device = store.getProfileSet("MSIE5");
			assertTrue(device.isMember("html"));
			assertTrue(!device.isMember("wml"));
		} catch (UnknownProfileMemberException e) {
			e.printStackTrace();
			fail("Unexpected Exception: " + e.getMessage());
		}
		try {
			ProfileSet device = store.getProfileSet("EricssonA2618");
			assertTrue(device.isMember("wml"));
			assertTrue(!device.isMember("html"));
		} catch (UnknownProfileMemberException e) {
			e.printStackTrace();
			fail("Unexpected Exception: " + e.getMessage());
		}
	}

	@Test
	public void testParse_success_one_profile() {
		DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();
		InputStream stream = getClass().getResourceAsStream(
				"profiles_success_one_profile.xml");
		ProfileStore store = null;

		try {
			store = digester.parse(stream);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected Exception: " + e.getMessage());
		}

		try {
			ProfileSet MSIE5 = store.getProfileSet("MSIE5");
			assertTrue(MSIE5.isMember("html"));
			assertTrue(!MSIE5.isMember("wml"));
		} catch (UnknownProfileMemberException e) {
			e.printStackTrace();
			fail("Unexpected Exception: " + e.getMessage());
		}
	}

	@Test
	public void testParse_success_many_profiles() {
		DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();
		InputStream stream = getClass().getResourceAsStream(
				"profiles_success_many_profiles.xml");
		ProfileStore store = null;

		try {
			store = digester.parse(stream);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected Exception: " + e.getMessage());
		}

		try {
			ProfileSet MSIE5 = store.getProfileSet("MSIE5");
			assertTrue(MSIE5.isMember("html"));
			assertTrue(MSIE5.isMember("css-enabled"));
			assertTrue(MSIE5.isMember("msie"));
			assertTrue(MSIE5.isMember("large"));
			assertTrue(MSIE5.isMember("html4"));
			assertTrue(!MSIE5.isMember("wml"));
			assertTrue(!MSIE5.isMember("wml"));
			assertTrue(!MSIE5.isMember("PDA"));
		} catch (UnknownProfileMemberException e) {
			e.printStackTrace();
			fail("Unexpected Exception: " + e.getMessage());
		}
	}

	@Test
	public void testParse_success_many_profiles_nested() {
		DefaultProfileConfigDigester digester = new DefaultProfileConfigDigester();
		InputStream stream = getClass().getResourceAsStream(
				"profiles_success_many_profiles_nested.xml");

		try {
			ProfileStore store = digester.parse(stream);
			assertNotNull(store);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected Exception: " + e.getMessage());
		}
	}
}
