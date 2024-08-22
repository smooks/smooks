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
package org.smooks.JIRAs.MILYN_367;

import org.junit.jupiter.api.Test;
import org.smooks.FilterSettings;
import org.smooks.Smooks;
import org.smooks.StreamFilterType;
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.api.resource.visitor.dom.DOMVisitAfter;
import org.smooks.api.resource.visitor.dom.DOMVisitBefore;
import org.smooks.engine.delivery.dom.serialize.DefaultDOMSerializerVisitor;
import org.smooks.engine.delivery.sax.ng.ConsumeSerializerVisitor;
import org.smooks.io.sink.StringSink;
import org.smooks.io.source.StreamSource;
import org.smooks.support.StreamUtils;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class MILYN_367_TestCase {

	@Test
	public void test_SAX_01() throws SAXException, IOException {
		Smooks smooks = new Smooks();
		StringSink sink = new StringSink();
		
		smooks.addVisitor(new ConsumeSerializerVisitor(), "#document");
		smooks.addVisitor(new ConsumeSerializerVisitor(), "//*");
		smooks.setFilterSettings(new FilterSettings().setDefaultSerializationOn(false).setFilterType(StreamFilterType.SAX_NG));
		
		smooks.filterSource(new StreamSource<>(getClass().getResourceAsStream("order.xml")), sink);
		
		assertOK("expected_01.xml", sink);        
	}

	@Test
	public void test_SAX_02() throws SAXException, IOException {
		Smooks smooks = new Smooks();
		StringSink sink = new StringSink();
		
		smooks.addVisitor(new ConsumeSerializerVisitor(), "customer");
		smooks.addVisitor(new ConsumeSerializerVisitor(), "descendant-or-self::customer/*");
		smooks.setFilterSettings(new FilterSettings().setDefaultSerializationOn(false).setFilterType(StreamFilterType.SAX_NG));
		
		smooks.filterSource(new StreamSource<>(getClass().getResourceAsStream("order.xml")), sink);
		
		assertOK("expected_02.xml", sink);        
	}

	@Test
	public void test_SAX_03() throws SAXException, IOException {
		Smooks smooks = new Smooks();
		StringSink sink = new StringSink();
		
		smooks.addVisitor(new ConsumeSerializerVisitor(), "items");
		smooks.addVisitor(new ConsumeSerializerVisitor(), "descendant-or-self::items/*");
		smooks.setFilterSettings(new FilterSettings().setDefaultSerializationOn(false).setFilterType(StreamFilterType.SAX_NG));
		
		smooks.filterSource(new StreamSource<>(getClass().getResourceAsStream("order.xml")), sink);
		
		assertOK("expected_03.xml", sink);        
	}

	@Test
	public void test_DOM_01() throws SAXException, IOException {
		Smooks smooks = new Smooks();
		StringSink sink = new StringSink();
		
		smooks.addVisitor(new DefaultDOMSerializerVisitor(), "#document");
		smooks.addVisitor(new DefaultDOMSerializerVisitor(), "//*");
		smooks.setFilterSettings(new FilterSettings().setDefaultSerializationOn(false).setFilterType(StreamFilterType.DOM));
		
		smooks.filterSource(new StreamSource<>(getClass().getResourceAsStream("order.xml")), sink);
		
		assertOK("expected_01.xml", sink);        
	}

	@Test
	public void test_DOM_02() throws SAXException, IOException {
		Smooks smooks = new Smooks();
		StringSink sink = new StringSink();
		
		smooks.addVisitor(new DefaultDOMSerializerVisitor(), "customer");
		smooks.addVisitor(new DefaultDOMSerializerVisitor(), "descendant-or-self::customer/*");
		smooks.setFilterSettings(new FilterSettings().setDefaultSerializationOn(false).setFilterType(StreamFilterType.DOM));
		
		smooks.filterSource(new StreamSource<>(getClass().getResourceAsStream("order.xml")), sink);

		assertOK("expected_02.xml", sink);
	}

	@Test
	public void test_DOM_03() throws SAXException, IOException {
		Smooks smooks = new Smooks();
		StringSink sink = new StringSink();
		
		smooks.addVisitor(new DefaultDOMSerializerVisitor(), "items");
		smooks.addVisitor(new DefaultDOMSerializerVisitor(), "descendant-or-self::items/*");
		smooks.setFilterSettings(new FilterSettings().setDefaultSerializationOn(false).setFilterType(StreamFilterType.DOM));
		
		smooks.filterSource(new StreamSource<>(getClass().getResourceAsStream("order.xml")), sink);
		
		assertOK("expected_03.xml", sink);
	}

	@Test
	public void test_DOM_04() throws SAXException, IOException {
		Smooks smooks = new Smooks();		
		DOMVBefore customerVisitor = new DOMVBefore();
		DOMVBefore itemsVisitor = new DOMVBefore();
		
		smooks.addVisitor(customerVisitor, "customer");
		smooks.addVisitor(customerVisitor, "descendant-or-self::customer/*");
		smooks.addVisitor(itemsVisitor, "items");
		smooks.addVisitor(itemsVisitor, "descendant-or-self::items/*");
		smooks.filterSource(new StreamSource<>(getClass().getResourceAsStream("order.xml")));
		
		assertEquals("customer-user-fname-x-lname-", customerVisitor.stringBuilder.toString());
		assertEquals("items-item-units-name-price-item-units-name-price-", itemsVisitor.stringBuilder.toString());
	}

	@Test
	public void test_DOM_05() throws SAXException, IOException {
		Smooks smooks = new Smooks();		
		DOMVAfter customerVisitor = new DOMVAfter();
		DOMVAfter itemsVisitor = new DOMVAfter();
		
		smooks.addVisitor(customerVisitor, "customer");
		smooks.addVisitor(customerVisitor, "descendant-or-self::customer/*");
		smooks.addVisitor(itemsVisitor, "items");
		smooks.addVisitor(itemsVisitor, "descendant-or-self::items/*");
		smooks.filterSource(new StreamSource<>(getClass().getResourceAsStream("order.xml")));
		
		assertEquals("user-x-fname-lname-customer-", customerVisitor.stringBuilder.toString());
		assertEquals("units-name-price-item-units-name-price-item-items-", itemsVisitor.stringBuilder.toString());
	}

	private void assertOK(String resName, StringSink sink) throws IOException {
		assertFalse(DiffBuilder.compare(sink.getResult()).withTest(getRes(resName)).
				ignoreComments().
				ignoreWhitespace().
				build().
				hasDifferences());
	}
	
	private String getRes(String name) throws IOException {
		return StreamUtils.readStreamAsString(getClass().getResourceAsStream(name), "UTF-8");
	}
	
	private static class DOMVBefore implements DOMVisitBefore {
		
		private final StringBuilder stringBuilder = new StringBuilder();

		@Override
		public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
			stringBuilder.append(element.getLocalName()).append("-");		
		}		
	}
	
	private static class DOMVAfter implements DOMVisitAfter {
		
		private final StringBuilder stringBuilder = new StringBuilder();

		@Override
		public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
			stringBuilder.append(element.getLocalName()).append("-");		
		}		
	}
}
