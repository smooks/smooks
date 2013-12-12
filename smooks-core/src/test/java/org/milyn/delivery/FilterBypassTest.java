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
package org.milyn.delivery;

import java.io.IOException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.milyn.FilterSettings;
import org.milyn.Smooks;
import org.milyn.commons.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMVisitAfter;
import org.milyn.delivery.dom.DOMVisitBefore;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXVisitAfter;
import org.milyn.delivery.sax.SAXVisitBefore;
import org.milyn.payload.StringResult;
import org.milyn.payload.StringSource;
import org.w3c.dom.Element;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class FilterBypassTest extends TestCase {

	private SimpleVisitor simpleVisitor;
	private FilterSettings filterSettings;
	
	@Override
	protected void setUp() throws Exception {
		simpleVisitor = null;
		filterSettings = null;
	}

	public void test_dom_bypass_only() {
		filterSettings = FilterSettings.DEFAULT_DOM;
		test("#document", true);
		test("$document", true);
		test("/", true);
		test("x", false);
	}
	
	public void test_dom_bypass_with_visitor() {
		simpleVisitor = new SimpleVisitor();

		filterSettings = FilterSettings.DEFAULT_DOM;
		test("#document", false);
		test("$document", false);
		test("/", false);
		test("x", false);		
	}

	public void test_sax_bypass_only() {
		filterSettings = FilterSettings.DEFAULT_SAX;
		test("#document", true);
		test("$document", true);
		test("/", true);
		test("x", false);
	}
	
	public void test_sax_bypass_with_visitor() {
		simpleVisitor = new SimpleVisitor();

		filterSettings = FilterSettings.DEFAULT_SAX;
		test("#document", false);
		test("$document", false);
		test("/", false);
		test("x", false);		
	}

	public void test(String selector, boolean expectBypass) {
		Smooks smooks = new Smooks();
		MyVisitBypass bypassVisitor = new MyVisitBypass(!expectBypass);
		
		smooks.setFilterSettings(filterSettings);
		
		smooks.addVisitor(bypassVisitor, selector);
		if(simpleVisitor != null) {
			smooks.addVisitor(simpleVisitor, "zz");
		}
		
		smooks.filterSource(new StringSource("<x/>"), new StringResult());
		
		assertEquals(expectBypass, bypassVisitor.bypassCalled);
	}
	
	private class MyVisitBypass implements DOMVisitBefore, SAXVisitAfter, FilterBypass {
		
		private boolean expectsVisitCall;
		private boolean bypassCalled;

		public MyVisitBypass(boolean expectsVisitCall) {
			this.expectsVisitCall = expectsVisitCall;
		}
		
		public boolean bypass(ExecutionContext executionContext, Source source, Result result) {
			bypassCalled = true;
			return true;
		}

		public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
			if(!expectsVisitCall) {
				fail("Unexpected call to filter visit method.");
			}
		}
		
		public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
			if(!expectsVisitCall) {
				fail("Unexpected call to filter visit method.");
			}
		}		
	}
	
	private class SimpleVisitor implements DOMVisitAfter, SAXVisitBefore {
		public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
		}
		public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
		}
	}
}
