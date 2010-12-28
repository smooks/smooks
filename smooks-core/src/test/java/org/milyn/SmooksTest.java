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

package org.milyn;

import junit.framework.TestCase;
import org.milyn.container.ExecutionContext;
import org.milyn.container.standalone.StandaloneExecutionContext;
import org.milyn.delivery.JavaContentHandlerFactory;
import org.milyn.delivery.dom.DOMVisitAfter;
import org.milyn.delivery.dom.DOMVisitBefore;
import org.milyn.delivery.dom.SmooksDOMFilter;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXVisitAfter;
import org.milyn.delivery.sax.SAXVisitBefore;
import org.milyn.payload.StringResult;
import org.milyn.payload.StringSource;
import org.milyn.profile.DefaultProfileSet;
import org.milyn.resource.URIResourceLocator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author tfennelly
 */
public class SmooksTest extends TestCase {

    private ExecutionContext execContext;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        Smooks smooks = new Smooks();
        SmooksUtil.registerProfileSet(DefaultProfileSet.create("device1", new String[] {"profile1"}), smooks);
        execContext = new StandaloneExecutionContext("device1", smooks.getApplicationContext(), null);
    }

		public void test_applyTransform_bad_params() {
			SmooksDOMFilter smooks = new SmooksDOMFilter(execContext);

			try {
				smooks.filter((Source)null);
				fail("Expected exception on null stream");
			} catch (IllegalArgumentException e) {
				//Expected
			} catch (SmooksException e) {
				e.printStackTrace();
				fail("unexpected exception: " + e.getMessage());
			}
		}


		public void test_applyTransform_DocumentCheck() {
			SmooksDOMFilter smooks;
			InputStream stream = null;
			Node deliveryNode = null;

			stream = getClass().getResourceAsStream("html_1.html");
			smooks = new SmooksDOMFilter(execContext);
			try {
				deliveryNode = smooks.filter(new StreamSource( stream));
			} catch (SmooksException e) {
				e.printStackTrace();
				fail("unexpected exception: " + e.getMessage());
			}
			assertNotNull("Null transform 'Document' return.", deliveryNode);
		}


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


    public void test_setResourceLocator() throws IOException, SAXException {
        Smooks smooks = new Smooks("classpath:/org/milyn/test_setClassLoader_01.xml");

        // Check that the base URI was properly resolved
        URIResourceLocator resourceLocator = (URIResourceLocator)smooks.getApplicationContext().getResourceLocator();
		assertEquals("classpath:/org/milyn/", resourceLocator.getBaseURI().toString());
		assertEquals("classpath:/org/milyn/somethingelse.xml", resourceLocator.getBaseURI().resolve("somethingelse.xml").toString());
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

        private Set requests = new HashSet();

        public TestClassLoader(ClassLoader contextClassLoader) {
            super(contextClassLoader);
        }

        public Class<?> loadClass(String name) throws ClassNotFoundException {
            requests.add(name);
            return super.loadClass(name);
        }
    }
}
