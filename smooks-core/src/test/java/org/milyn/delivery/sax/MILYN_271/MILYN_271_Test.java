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
package org.milyn.delivery.sax.MILYN_271;

import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import static org.junit.Assert.*;
import org.milyn.FilterSettings;
import org.milyn.Smooks;
import org.milyn.delivery.sax.MockVisitBefore;

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
