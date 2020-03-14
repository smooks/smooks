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

package org.smooks;

import org.junit.Before;
import org.junit.Test;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.JavaContentHandlerFactory;
import org.smooks.delivery.dom.DOMVisitAfter;
import org.smooks.delivery.dom.DOMVisitBefore;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.delivery.sax.SAXVisitAfter;
import org.smooks.delivery.sax.SAXVisitBefore;
import org.smooks.payload.StringResult;
import org.smooks.payload.StringSource;
import org.smooks.profile.DefaultProfileSet;
import org.smooks.resource.URIResourceLocator;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author tfennelly
 */
public class SmooksTest {

	@Before
    public void setUp() throws Exception {
        Smooks smooks = new Smooks();
        SmooksUtil.registerProfileSet(DefaultProfileSet.create("device1", new String[] {"profile1"}), smooks);
    }

	@Test
    public void test_setClassPath() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("test_setClassLoader_01.xml"));
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        TestClassLoader classLoader = new TestClassLoader(contextClassLoader);
        StringResult result = new StringResult();

        smooks.setClassLoader(classLoader);

        ExecutionContext execCtx = smooks.createExecutionContext();
        assertTrue(classLoader.requests.contains(JavaContentHandlerFactory.class.getName()));
        assertTrue(contextClassLoader == Thread.currentThread().getContextClassLoader());

        classLoader.requests.clear();
        smooks.filterSource(execCtx, new StringSource("<a/>"), result);
        assertEquals("<b></b>", result.getResult());
        //assertTrue(classLoader.requests.contains(XIncludeParserConfiguration.class.getName()));
        assertTrue(contextClassLoader == Thread.currentThread().getContextClassLoader());
    }

	@Test
    public void test_addVisitor_DOM_01() {
        Smooks smooks = new Smooks();
        TestDOMVisitorBefore visitor1 = new TestDOMVisitorBefore();
        TestDOMVisitorAfter visitor2 = new TestDOMVisitorAfter();

        smooks.addVisitor(visitor1, "c/xxx");
        smooks.addVisitor(visitor2, "c");

        smooks.filterSource(new StringSource("<a><xxx/><xxx/><c><xxx/><xxx/></c></a>"));

        assertEquals(2, visitor1.callCount);
        assertEquals(1, visitor2.callCount);
    }

	@Test
    public void test_addVisitor_SAX_01() {
        Smooks smooks = new Smooks();
        TestSAXVisitorBefore visitor1 = new TestSAXVisitorBefore();
        TestSAXVisitorAfter visitor2 = new TestSAXVisitorAfter();

        smooks.addVisitor(visitor1, "c/xxx");
        smooks.addVisitor(visitor2, "c");

        smooks.filterSource(new StringSource("<a><xxx/><xxx/><c><xxx/><xxx/></c></a>"));

        assertEquals(2, visitor1.callCount);
        assertEquals(1, visitor2.callCount);
    }


	@Test
    public void test_setResourceLocator() throws IOException, SAXException {
        Smooks smooks = new Smooks("classpath:/org/smooks/test_setClassLoader_01.xml");

        // Check that the base URI was properly resolved
        URIResourceLocator resourceLocator = (URIResourceLocator)smooks.getApplicationContext().getResourceLocator();
		assertEquals("classpath:/org/smooks/", resourceLocator.getBaseURI().toString());
		assertEquals("classpath:/org/smooks/somethingelse.xml", resourceLocator.getBaseURI().resolve("somethingelse.xml").toString());
    }

    private class TestDOMVisitorBefore implements DOMVisitBefore {
        private int callCount = 0;
        public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
            callCount++;
        }
    }

    private class TestDOMVisitorAfter implements DOMVisitAfter {
        private int callCount = 0;
        public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
            callCount++;
        }
    }

    private class TestSAXVisitorBefore implements SAXVisitBefore {
        private int callCount = 0;
        public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
            callCount++;
        }
    }

    private class TestSAXVisitorAfter implements SAXVisitAfter {
        private int callCount = 0;
        public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
            callCount++;
        }
    }

    private class TestClassLoader extends ClassLoader {

        private Set<String> requests = new HashSet<String>();

        public TestClassLoader(ClassLoader contextClassLoader) {
            super(contextClassLoader);
        }

        public Class<?> loadClass(String name) throws ClassNotFoundException {
            requests.add(name);
            return super.loadClass(name);
        }
    }
}
