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
package org.smooks.delivery.sax.MILYN_271;

import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import static org.junit.Assert.*;
import org.smooks.FilterSettings;
import org.smooks.Smooks;
import org.smooks.delivery.sax.MockVisitBefore;

/**
 * http://jira.codehaus.org/browse/MILYN-271
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MILYN_271_Test {
	
	@Test
	public void test_01_DOM() {
		test_01(FilterSettings.DEFAULT_DOM);
	}
	
	@Test
	public void test_01_SAX() {
		test_01(FilterSettings.DEFAULT_SAX);
	}
	
	public void test_01(FilterSettings filterSettings) {
		Smooks smooks = new Smooks();		
		MockVisitBefore visitor = new MockVisitBefore();
		
		smooks.setFilterSettings(filterSettings);
		
		smooks.addVisitor(visitor, "order-item/*");		
		smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
		
		assertEquals("[product, quantity, price]", visitor.getElements().toString());
	}
		
	@Test
	public void test_02_DOM() {
		test_02(FilterSettings.DEFAULT_DOM);
	}
	
	@Test
	public void test_02_SAX() {
		test_02(FilterSettings.DEFAULT_SAX);
	}
	
	public void test_02(FilterSettings filterSettings) {
		Smooks smooks = new Smooks();		
		MockVisitBefore visitor1 = new MockVisitBefore();
		MockVisitBefore visitor2 = new MockVisitBefore();
		
		smooks.setFilterSettings(filterSettings);

		smooks.addVisitor(visitor1, "order-item/*");
		smooks.addVisitor(visitor2, "order-item/price");
		
		smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
		
		assertEquals("[product, quantity, price]", visitor1.getElements().toString());
		assertEquals("[price]", visitor2.getElements().toString());
	}
}
