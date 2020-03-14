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
package org.smooks.delivery.lifecyclecleanup;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.smooks.Smooks;
import org.smooks.payload.StringSource;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ExecutionLifecycleTest {

	@Before
    public void setUp() throws Exception {
        DomAssemblyBefore.cleaned = false;
        DomAssemblyAfter.cleaned = false;
        DomAssemblyAfterWithException.cleaned = false;
        DomProcessingBefore.initialized = false;
        DomProcessingBefore.cleaned = false;
        DomProcessingAfter.cleaned = false;
        SaxVisitBefore.initialized = false;
        SaxVisitBefore.cleaned = false;
        SaxVisitAfter.cleaned = false;
        DomProcessingVisitCleanable.cleaned = false;
        SaxVisitCleanable.cleaned = false;
    }

	@Test
    public void test_dom_01() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("dom-config-01.xml"));

        smooks.filterSource(new StringSource("<a><b/><c/><d/><e/></a>"), null);
        assertTrue(DomAssemblyBefore.cleaned);
        assertTrue(DomAssemblyAfter.cleaned);
        assertTrue(DomAssemblyAfterWithException.cleaned);
        assertTrue(DomProcessingBefore.initialized);
        assertTrue(DomProcessingBefore.cleaned);
        assertTrue(DomProcessingAfter.cleaned);
    }

	@Test
    public void test_dom_02() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("dom-config-02.xml"));

        smooks.filterSource(new StringSource("<a></a>"), null);
        assertTrue(DomProcessingVisitCleanable.cleaned);
    }

	@Test
    public void test_SAX_01() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("sax-config-01.xml"));

        smooks.filterSource(new StringSource("<a><b/><c/><d/><e/></a>"), null);
        assertTrue(SaxVisitBefore.initialized);
        assertTrue(SaxVisitBefore.cleaned);
        assertTrue(SaxVisitAfter.cleaned);
    }

	@Test
    public void test_SAX_02() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("sax-config-02.xml"));

        smooks.filterSource(new StringSource("<a></a>"), null);
        assertTrue(SaxVisitCleanable.cleaned);
    }
}
