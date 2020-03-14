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
package org.smooks.delivery.sax.terminate;

import java.io.IOException;

import javax.xml.transform.stream.StreamSource;

import org.smooks.Smooks;
import org.smooks.delivery.sax.SAXVisitBeforeVisitor;
import org.xml.sax.SAXException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class TerminateVisitorTest {
	
	@Before
	public void setUp() throws Exception {
		SAXVisitBeforeVisitor.visited = false;
	}

	@Test
	public void test_terminate_prog_after() {
		Smooks smooks = new Smooks();
		
		smooks.addVisitor(new TerminateVisitor(), "customer");
		smooks.addVisitor(new SAXVisitBeforeVisitor().setInjectedParam("blah"), "user");
		
		smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
		assertTrue(SAXVisitBeforeVisitor.visited);
	}

	@Test
	public void test_terminate_prog_before() {
		Smooks smooks = new Smooks();
		
		smooks.addVisitor(new TerminateVisitor().setTerminateBefore(true), "customer");
		smooks.addVisitor(new SAXVisitBeforeVisitor().setInjectedParam("blah"), "user");
		
		smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
		assertFalse(SAXVisitBeforeVisitor.visited);
	}

	@Test
	public void test_terminate_xml_after() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("config-01.xml"));
		
		smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
		assertTrue(SAXVisitBeforeVisitor.visited);
	}

	@Test
	public void test_terminate_xml_before() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("config-02.xml"));
		
		smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order.xml")));
		assertFalse(SAXVisitBeforeVisitor.visited);
	}
}
