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
package org.smooks.engine.resource.visitor.smooks;

import org.junit.jupiter.api.Test;
import org.smooks.Smooks;
import org.smooks.api.ExecutionContext;
import org.smooks.io.payload.StringResult;
import org.smooks.io.payload.StringSource;
import org.smooks.api.Registry;
import org.smooks.engine.lookup.InstanceLookup;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NestedSmooksVisitorFunctionalTestCase {

	@Test
	public void test() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("nested-smooks-visitor-config.xml"));
		StringResult stringResult = new StringResult();
		smooks.filterSource(new StringSource("<a><b><c></c></b></a>"), stringResult);
		NestedSmooksVisitor smooksVisitor = smooks.getApplicationContext().getRegistry().lookup(new InstanceLookup<>(NestedSmooksVisitor.class)).values().stream().findFirst().get();
		Registry registry = smooksVisitor.getNestedSmooks().getApplicationContext().getRegistry();
		assertEquals(0, registry.lookup(new InstanceLookup<>(BarBeforeVisitor.class)).values().stream().findFirst().get().getCountDownLatch().getCount());
		assertEquals(0, registry.lookup(new InstanceLookup<>(FooVisitor.class)).values().stream().findFirst().get().getCountDownLatch().getCount());
		assertEquals(0, registry.lookup(new InstanceLookup<>(QuxVisitor.class)).values().stream().findFirst().get().getCountDownLatch().getCount());
		assertEquals(1, registry.lookup(new InstanceLookup<>(QuuzVisitor.class)).values().stream().findFirst().get().getCountDownLatch().getCount());
	}
	
	@Test
	public void testReplaceAction() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("replace-nested-smooks-visitor-config.xml"));
		StringResult stringResult = new StringResult();
		smooks.filterSource(new StringSource("<a><b><c/></b></a>"), stringResult);
		assertEquals("<a>Hello World!</a>", stringResult.getResult());
	}
	
	@Test
	public void testPrependAfterAction() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("prependAfter-nested-smooks-visitor-config.xml"));
		StringResult stringResult = new StringResult();
		smooks.filterSource(new StringSource("<a><b>c<d/></b></a>"), stringResult);
		assertEquals("<a><b>Hello World!c<d></d></b></a>", stringResult.getResult());
	}

	@Test
	public void testAppendAfterAction() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("appendAfter-nested-smooks-visitor-config.xml"));
		StringResult stringResult = new StringResult();
		smooks.filterSource(new StringSource("<a><b>c<d/></b></a>"), stringResult);
		assertEquals("<a><b>c<d/></b>Hello World!</a>", stringResult.getResult());
	}

	@Test
	public void testAppendBeforeAction() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("appendBefore-nested-smooks-visitor-config.xml"));
		StringResult stringResult = new StringResult();
		smooks.filterSource(new StringSource("<a><b>c<d/></b></a>"), stringResult);
		assertEquals("<a><b>c<d/>Hello World!</b></a>", stringResult.getResult());
	}
	
	@Test
	public void testPrependBeforeAction() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("prependBefore-nested-smooks-visitor-config.xml"));
		StringResult stringResult = new StringResult();
		smooks.filterSource(new StringSource("<a><b>c<d/></b></a>"), stringResult);
		assertEquals("<a>Hello World!<b>c<d></d></b></a>", stringResult.getResult());
	}
	
	@Test
	public void testBindToAction() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("bindTo-nested-smooks-visitor-config.xml"));
		ExecutionContext executionContext = smooks.createExecutionContext();
		StringResult stringResult = new StringResult();
		smooks.filterSource(executionContext, new StringSource("<a><b><c/></b></a>"), stringResult);
		assertEquals("<a><b><c/></b></a>", stringResult.getResult());
		assertEquals("Hello World!", executionContext.getBeanContext().getBean("output"));
	}
	
	@Test
	public void testOutputToAction() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("outputTo-nested-smooks-visitor-config.xml"));
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		smooks.getApplicationContext().getRegistry().registerObject("Output Stream", outputStream);
		StringResult stringResult = new StringResult();
		smooks.filterSource(new StringSource("<a><b><c/></b></a>"), stringResult);
		assertEquals("<a><b><c/></b></a>", stringResult.getResult());
		assertEquals("Hello World!", outputStream.toString());
	}

	@Test
	public void testNoOp() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("no-op-nested-smooks-visitor-config.xml"));
		StringResult stringResult = new StringResult();
		smooks.filterSource(new StringSource("<a><b><c/></b></a>"), stringResult);
		assertEquals("<a><a><b><b><c></c><c/></b></b></a></a>", stringResult.getResult());
	}
}
