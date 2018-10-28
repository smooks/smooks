/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package org.milyn.delivery.sax;

import java.io.IOException;

import org.milyn.FilterSettings;
import org.milyn.Smooks;
import org.milyn.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.sax.annotation.StreamResultWriter;
import org.milyn.lang.LangUtil;
import org.milyn.payload.StringResult;
import org.milyn.payload.StringSource;
import org.xml.sax.SAXException;

import org.junit.Test;
import static org.junit.Assert.*;


/**
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SAXToXMLWriterTest {

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
	public void test_vafter_write_method() throws IOException, SAXException {
        if (LangUtil.getJavaVersion() != 1.5) {
            return;
        }

        Smooks smooks = new Smooks(getClass().getResourceAsStream("SAXToXMLWriterTest_config.xml"));
		StringResult stringResult = new StringResult();
		
		smooks.filterSource(new StringSource("<a><b>some&amp;text</b></a>"), stringResult);
		
		assertEquals("<a><b>{{some&#38;text}}</b></a>", stringResult.getResult());
		assertEquals("some&#38;text", VisitAfterWrittingVisitor.elementText);
	}	
	
	private class AllWrittingVisitor implements SAXElementVisitor {

		@StreamResultWriter	
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
