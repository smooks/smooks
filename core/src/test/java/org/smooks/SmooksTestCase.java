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
package org.smooks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.api.resource.visitor.dom.DOMVisitAfter;
import org.smooks.api.resource.visitor.dom.DOMVisitBefore;
import org.smooks.api.resource.visitor.sax.ng.AfterVisitor;
import org.smooks.api.resource.visitor.sax.ng.BeforeVisitor;
import org.smooks.engine.DefaultApplicationContextBuilder;
import org.smooks.engine.profile.DefaultProfileSet;
import org.smooks.io.sink.StringSink;
import org.smooks.io.source.StringSource;
import org.smooks.resource.URIResourceLocator;
import org.smooks.support.SmooksUtil;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 *
 * @author tfennelly
 */
public class SmooksTestCase {

	@BeforeEach
    public void setUp() throws Exception {
        Smooks smooks = new Smooks();
        SmooksUtil.registerProfileSet(new DefaultProfileSet("device1", new String[] {"profile1"}), smooks);
    }

	@Test
    public void test_setClassPath() throws IOException, SAXException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        TestClassLoader classLoader = new TestClassLoader(contextClassLoader);
        Smooks smooks = new Smooks(new DefaultApplicationContextBuilder().withClassLoader(classLoader).build());
        smooks.addResourceConfigs(getClass().getResourceAsStream("test_setClassLoader_01.xml"));
                
        StringSink sink = new StringSink();
        
        ExecutionContext execCtx = smooks.createExecutionContext();
        assertSame(contextClassLoader, Thread.currentThread().getContextClassLoader());

        classLoader.requests.clear();
        smooks.filterSource(execCtx, new StringSource("<a/>"), sink);
        assertEquals("<b></b>", sink.getResult());
        //assertTrue(classLoader.requests.contains(XIncludeParserConfiguration.class.getName()));
        assertSame(contextClassLoader, Thread.currentThread().getContextClassLoader());
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

    private static class TestDOMVisitorBefore implements DOMVisitBefore {
        private int callCount;

        @Override
        public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
            callCount++;
        }
    }

    private static class TestDOMVisitorAfter implements DOMVisitAfter {
        private int callCount;

        @Override
        public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
            callCount++;
        }
    }

    private static class TestSAXVisitorBefore implements BeforeVisitor {
        private int callCount;

        @Override
        public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
            callCount++;
        }
    }

    private static class TestSAXVisitorAfter implements AfterVisitor {
        private int callCount;

        @Override
        public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
            callCount++;
        }
    }

    private static class TestClassLoader extends ClassLoader {

        private final Set<String> requests = new HashSet<String>();

        public TestClassLoader(ClassLoader contextClassLoader) {
            super(contextClassLoader);
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            requests.add(name);
            return super.loadClass(name);
        }
    }
}
