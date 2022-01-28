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
package org.smooks.engine.delivery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smooks.FilterSettings;
import org.smooks.Smooks;
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.api.delivery.FilterBypass;
import org.smooks.api.resource.visitor.dom.DOMVisitAfter;
import org.smooks.api.resource.visitor.dom.DOMVisitBefore;
import org.smooks.api.resource.visitor.sax.ng.AfterVisitor;
import org.smooks.api.resource.visitor.sax.ng.BeforeVisitor;
import org.smooks.io.payload.StringResult;
import org.smooks.io.payload.StringSource;
import org.w3c.dom.Element;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class FilterBypassTestCase {

	private SimpleVisitor simpleVisitor;
	private FilterSettings filterSettings;
	
	@BeforeEach
	public void setUp() throws Exception {
		simpleVisitor = null;
		filterSettings = null;
	}

	@Test
	public void test_dom_bypass_only() {
		filterSettings = FilterSettings.DEFAULT_DOM;
		test("#document", true);
		test("/", true);
		test("x", false);
	}
	
	@Test
	public void test_dom_bypass_with_visitor() {
		simpleVisitor = new SimpleVisitor();

		filterSettings = FilterSettings.DEFAULT_DOM;
		test("#document", false);
		test("/", false);
		test("x", false);		
	}

	@Test
	public void test_sax_bypass_only() {
		filterSettings = FilterSettings.DEFAULT_SAX_NG;
		test("#document", true);
		test("/", true);
		test("x", false);
	}
	
	@Test
	public void test_sax_bypass_with_visitor() {
		simpleVisitor = new SimpleVisitor();

		filterSettings = FilterSettings.DEFAULT_SAX_NG;
		test("#document", false);
		test("/", false);
		test("x", false);		
	}

	public void test(String selector, boolean expectBypass) {
		Smooks smooks = new Smooks();
		MyVisitBypass bypassVisitor = new MyVisitBypass(!expectBypass);

		smooks.setFilterSettings(filterSettings);

		smooks.addVisitor(bypassVisitor, selector);
		if (simpleVisitor != null) {
			smooks.addVisitor(simpleVisitor, "zz");
		}

		smooks.filterSource(new StringSource("<x/>"), new StringResult());

		assertEquals(expectBypass, bypassVisitor.bypassCalled);
	}
	
	private static class MyVisitBypass implements DOMVisitBefore, BeforeVisitor, FilterBypass {
		
		private final boolean expectsVisitCall;
		private boolean bypassCalled;

		public MyVisitBypass(boolean expectsVisitCall) {
			this.expectsVisitCall = expectsVisitCall;
		}

		@Override
		public boolean bypass(ExecutionContext executionContext, Source source, Result result) {
			bypassCalled = true;
			return true;
		}

		@Override
		public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
			if(!expectsVisitCall) {
				fail("Unexpected call to filter visit method.");
			}
		}
	}
	
	private static class SimpleVisitor implements DOMVisitAfter, AfterVisitor {
		
		@Override
		public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
		}
	}
}
