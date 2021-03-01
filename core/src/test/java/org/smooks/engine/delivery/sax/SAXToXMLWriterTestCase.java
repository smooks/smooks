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
package org.smooks.engine.delivery.sax;

import org.junit.Test;
import org.smooks.Smooks;
import org.smooks.api.SmooksException;
import org.smooks.api.ExecutionContext;
import org.smooks.api.delivery.sax.SAXElement;
import org.smooks.api.delivery.sax.SAXText;
import org.smooks.api.resource.visitor.sax.SAXElementVisitor;
import org.smooks.io.SAXToXMLWriter;
import org.smooks.io.payload.StringResult;
import org.smooks.io.payload.StringSource;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import java.io.IOException;

import static org.junit.Assert.assertEquals;


/**
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SAXToXMLWriterTestCase {

	@Test
	public void test_all_write_methods() {
		Smooks smooks = new Smooks();
		StringResult stringResult = new StringResult();
		
		smooks.addVisitor(new AllWrittingVisitor().setLeftWrapping("{{").setRightWrapping("}}"), "a");
		smooks.addVisitor(new AllWrittingVisitor().setLeftWrapping("((").setRightWrapping("))"), "b");
		smooks.filterSource(new StringSource("<a><b>sometext</b></a>"), stringResult);
		
		assertEquals("{{<a>((<b>sometext</b>))</a>}}", stringResult.getResult());
	}	

	@Test
	public void test_after_write_method() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("SAXToXMLWriterTest_config.xml"));
		StringResult stringResult = new StringResult();
		
		smooks.filterSource(new StringSource("<a><b>some&amp;text</b></a>"), stringResult);
		
		assertEquals("<a><b>{{some&#38;text}}</b></a>", stringResult.getResult());
		assertEquals("some&#38;text", VisitAfterWrittingVisitor.elementText);
	}	
	
	private static class AllWrittingVisitor implements SAXElementVisitor {

		@Inject	
		private SAXToXMLWriter writer;
		private String leftWrapping;
		private String rightWrapping;
		
		public AllWrittingVisitor setLeftWrapping(String leftWrapping) {
			this.leftWrapping = leftWrapping;
			return this;
		}
		
		public AllWrittingVisitor setRightWrapping(String rightWrapping) {
			this.rightWrapping = rightWrapping;
			return this;
		}
		
		public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
			writer.writeText(leftWrapping, element);
			writer.writeStartElement(element);
		}

		public void onChildElement(SAXElement element, SAXElement childElement, ExecutionContext executionContext) throws SmooksException, IOException {
		}

		public void onChildText(SAXElement element, SAXText childText, ExecutionContext executionContext) throws SmooksException, IOException {
			writer.writeText(childText, element);
		}		
		
		public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
			writer.writeEndElement(element);
			writer.writeText(rightWrapping, element);
		}
	}
}
