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
package org.smooks.servlet;

import com.mockobjects.servlet.MockServletConfig;
import com.mockobjects.servlet.MockServletContext;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 * @author tfennelly
 */
public class ServletParamUtilsTest {

	private MockServletConfig getServletConfig() {
		MockServletConfig config = new MockServletConfig();
		config.setServletContext(new MockServletContext());
		return config;
	}

	@Test
	public void testGetResource_exceptions() {
		try {
			ServletParamUtils.getParameterValue(null, null);
			fail("no IllegalArgumentException on null 'paramName' param");
		} catch (IllegalArgumentException e) {
		}
		try {
			ServletParamUtils.getParameterValue("", null);
			fail("no IllegalArgumentException on empty 'paramName' param");
		} catch (IllegalArgumentException e) {
		}
		try {
			ServletParamUtils.getParameterValue(" ", null);
			fail("no IllegalArgumentException on whitespace 'paramName' param");
		} catch (IllegalArgumentException e) {
		}
		try {
			ServletParamUtils.getParameterValue("paramName", null);
			fail("no IllegalStateException on null 'config' param");
		} catch (IllegalArgumentException e) {
		}
	}

	/**
	 * Test that getResource loads based on the servlet config param -
	 * <init-param>
	 */
	@Test
	public void testGetResource_initparam() {
		MockServletConfig config = getServletConfig();

		config.setInitParameter("paraX", "paraX-config");
		((MockServletContext) config.getServletContext()).setInitParameter(
				"paraX", "paraX-context");
		try {
			assertEquals("paraX-config", ServletParamUtils.getParameterValue(
					"paraX", config));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test that getResource loads based on the servlet context param -
	 * <context-param>
	 */
	@Test
	public void testGetResource_contextparam() {
		MockServletConfig config = getServletConfig();

		// don't set the param in the servlet config
		((MockServletContext) config.getServletContext()).setInitParameter(
				"paraX", "paraX-context");
		try {
			assertEquals("paraX-context", ServletParamUtils.getParameterValue(
					"paraX", config));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

}
