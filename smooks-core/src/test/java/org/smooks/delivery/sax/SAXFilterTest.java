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
package org.smooks.delivery.sax;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.smooks.Smooks;
import org.smooks.container.ExecutionContext;
import org.smooks.io.StreamUtils;
import org.smooks.lang.LangUtil;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SAXFilterTest {

	@Test
    public void test_reader_writer() throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-01.xml"));
        ExecutionContext execContext = smooks.createExecutionContext();
        String input = new String(StreamUtils.readStream(getClass().getResourceAsStream("test-01.xml")));
        StringWriter writer = new StringWriter();

        smooks.filterSource(execContext, new StreamSource(new StringReader(input)), new StreamResult(writer));
        assertEquals(StreamUtils.trimLines(new StringReader(input)).toString(), StreamUtils.trimLines(new StringReader(writer.toString())).toString());
    }

	@Test
    public void test_reader_stream() throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-01.xml"));
        ExecutionContext execContext = smooks.createExecutionContext();
        String input = new String(StreamUtils.readStream(getClass().getResourceAsStream("test-01.xml")));
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        smooks.filterSource(execContext, new StreamSource(new StringReader(input)), new StreamResult(outStream));
        assertEquals(StreamUtils.trimLines(new StringReader(input)).toString(), StreamUtils.trimLines(new StringReader(outStream.toString())).toString());
    }

	@Test
    public void test_stream_stream() throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-01.xml"));
        ExecutionContext execContext = smooks.createExecutionContext();
        String input = new String(StreamUtils.readStream(getClass().getResourceAsStream("test-01.xml")));
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        smooks.filterSource(execContext, new StreamSource(new ByteArrayInputStream(input.getBytes())), new StreamResult(outStream));
        assertEquals(StreamUtils.trimLines(new StringReader(input)).toString(), StreamUtils.trimLines(new StringReader(outStream.toString())).toString());
    }

	@Test
    public void test_stream_writer() throws SAXException, IOException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-01.xml"));
        ExecutionContext execContext = smooks.createExecutionContext();
        String input = new String(StreamUtils.readStream(getClass().getResourceAsStream("test-01.xml")));
        StringWriter writer = new StringWriter();

        smooks.filterSource(execContext, new StreamSource(new ByteArrayInputStream(input.getBytes())), new StreamResult(writer));
        assertEquals(StreamUtils.trimLines(new StringReader(input)).toString(), StreamUtils.trimLines(new StringReader(writer.toString())).toString());
    }

	@Before
    public void setUp() throws Exception {
        SAXVisitor01.element = null;
        SAXVisitor01.children.clear();
        SAXVisitor01.childText.clear();
        SAXVisitor02.element = null;
        SAXVisitor02.children.clear();
        SAXVisitor02.childText.clear();
        SAXVisitor03.element = null;
        SAXVisitor03.children.clear();
        SAXVisitor03.childText.clear();

        SAXVisitBeforeVisitor.visited = false;
        SAXVisitBeforeAndChildrenVisitor.reset();
        SAXVisitAfterVisitor.visited = false;
        SAXVisitAfterAndChildrenVisitor.reset();
    }

	@Test
    public void test_selection() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-02.xml"));
        ExecutionContext execContext = smooks.createExecutionContext();
        String input = new String(StreamUtils.readStream(getClass().getResourceAsStream("test-01.xml")));

        smooks.filterSource(execContext, new StreamSource(new ByteArrayInputStream(input.getBytes())), null);
        assertEquals("h", SAXVisitor01.element.getName().getLocalPart());
        assertEquals("y", SAXVisitor01.element.getName().getPrefix());
        assertEquals("http://y", SAXVisitor01.element.getName().getNamespaceURI());
        assertEquals(0, SAXVisitor01.children.size());
        assertEquals(0, SAXVisitor01.childText.size());

        assertEquals("i", SAXVisitor02.element.getName().getLocalPart());
        assertEquals("", SAXVisitor02.element.getName().getPrefix());
        assertEquals("http://x", SAXVisitor02.element.getName().getNamespaceURI());
        assertEquals(0, SAXVisitor02.children.size());
        assertEquals(1, SAXVisitor02.childText.size());

        assertEquals("h", SAXVisitor03.element.getName().getLocalPart());
        assertEquals("", SAXVisitor03.element.getName().getPrefix());
        assertEquals("http://x", SAXVisitor03.element.getName().getNamespaceURI());
        assertEquals(1, SAXVisitor03.children.size());
        if (LangUtil.getJavaVersion() == 1.5) {
            assertEquals(7, SAXVisitor03.childText.size());
        }
    }

	@Test
    public void test_contextual_1() throws IOException, SAXException {
        test_contextual("smooks-config-03.xml");
    }

	@Test
    public void test_contextual_2() throws IOException, SAXException {
        test_contextual("smooks-config-03.01.xml");
    }

	@Test
    public void test_contextual_3() throws IOException, SAXException {
        test_contextual("smooks-config-03.02.xml");
    }

    public void test_contextual(String config) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream(config));
        ExecutionContext execContext = smooks.createExecutionContext();
        String input = new String(StreamUtils.readStream(getClass().getResourceAsStream("test-01.xml")));

        smooks.filterSource(execContext, new StreamSource(new ByteArrayInputStream(input.getBytes())), null);
        assertEquals("h", SAXVisitor01.element.getName().getLocalPart());
        assertEquals("y", SAXVisitor01.element.getName().getPrefix());
        assertEquals("http://y", SAXVisitor01.element.getName().getNamespaceURI());
        assertEquals(0, SAXVisitor01.children.size());
        assertEquals(0, SAXVisitor01.childText.size());

        assertNull(SAXVisitor02.element);

        assertEquals("i", SAXVisitor03.element.getName().getLocalPart());
        assertEquals("", SAXVisitor03.element.getName().getPrefix());
        assertEquals("http://x", SAXVisitor03.element.getName().getNamespaceURI());
        assertEquals(0, SAXVisitor03.children.size());
        assertEquals(1, SAXVisitor03.childText.size());
    }

    @Test
    public void test_document() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-04.xml"));
        ExecutionContext execContext = smooks.createExecutionContext();
        String input = new String(StreamUtils.readStream(getClass().getResourceAsStream("test-01.xml")));

        smooks.filterSource(execContext, new StreamSource(new ByteArrayInputStream(input.getBytes())), null);
        assertEquals("xx", SAXVisitor01.element.getName().getLocalPart());
        assertEquals("xx", SAXVisitor02.element.getName().getLocalPart());
        assertNull(SAXVisitor03.element);
    }

    @Test
    public void test_visitBeforeOnly() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-05.xml"));

        smooks.filterSource(smooks.createExecutionContext(), new StreamSource(new StringReader("<a/>")), null);
        assertTrue(SAXVisitBeforeVisitor.visited);
        assertEquals("Hi There!", SAXVisitBeforeVisitor.staticInjectedParam);
    }

    @Test
    public void test_visitBeforeAndChildren() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-05.xml"));

        smooks.filterSource(smooks.createExecutionContext(), new StreamSource(new StringReader("<b/>")), null);
        assertTrue(SAXVisitBeforeAndChildrenVisitor.visited);
        assertFalse(SAXVisitBeforeAndChildrenVisitor.onChildText);
        assertFalse(SAXVisitBeforeAndChildrenVisitor.onChildElement);
        SAXVisitBeforeAndChildrenVisitor.reset();

        smooks.filterSource(smooks.createExecutionContext(), new StreamSource(new StringReader("<b>text</b>")), null);
        assertTrue(SAXVisitBeforeAndChildrenVisitor.visited);
        assertTrue(SAXVisitBeforeAndChildrenVisitor.onChildText);
        assertFalse(SAXVisitBeforeAndChildrenVisitor.onChildElement);
        SAXVisitBeforeAndChildrenVisitor.reset();

        smooks.filterSource(smooks.createExecutionContext(), new StreamSource(new StringReader("<b><x/></b>")), null);
        assertTrue(SAXVisitBeforeAndChildrenVisitor.visited);
        assertFalse(SAXVisitBeforeAndChildrenVisitor.onChildText);
        assertTrue(SAXVisitBeforeAndChildrenVisitor.onChildElement);
        SAXVisitBeforeAndChildrenVisitor.reset();

        smooks.filterSource(smooks.createExecutionContext(), new StreamSource(new StringReader("<b>text<x/></b>")), null);
        assertTrue(SAXVisitBeforeAndChildrenVisitor.visited);
        assertTrue(SAXVisitBeforeAndChildrenVisitor.onChildText);
        assertTrue(SAXVisitBeforeAndChildrenVisitor.onChildElement);
        SAXVisitBeforeAndChildrenVisitor.reset();
    }

    @Test
    public void test_visitAfterOnly() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-05.xml"));

        smooks.filterSource(smooks.createExecutionContext(), new StreamSource(new StringReader("<c/>")), null);
        assertTrue(SAXVisitAfterVisitor.visited);
    }

    @Test
    public void test_visitAfterAndChildren() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-05.xml"));

        smooks.filterSource(smooks.createExecutionContext(), new StreamSource(new StringReader("<d/>")), null);
        assertTrue(SAXVisitAfterAndChildrenVisitor.visited);
        assertFalse(SAXVisitAfterAndChildrenVisitor.onChildText);
        assertFalse(SAXVisitAfterAndChildrenVisitor.onChildElement);
        SAXVisitAfterAndChildrenVisitor.reset();

        smooks.filterSource(smooks.createExecutionContext(), new StreamSource(new StringReader("<d>text</d>")), null);
        assertTrue(SAXVisitAfterAndChildrenVisitor.visited);
        assertTrue(SAXVisitAfterAndChildrenVisitor.onChildText);
        assertFalse(SAXVisitAfterAndChildrenVisitor.onChildElement);
        SAXVisitAfterAndChildrenVisitor.reset();

        smooks.filterSource(smooks.createExecutionContext(), new StreamSource(new StringReader("<d><x/></d>")), null);
        assertTrue(SAXVisitAfterAndChildrenVisitor.visited);
        assertFalse(SAXVisitAfterAndChildrenVisitor.onChildText);
        assertTrue(SAXVisitAfterAndChildrenVisitor.onChildElement);
        SAXVisitAfterAndChildrenVisitor.reset();

        smooks.filterSource(smooks.createExecutionContext(), new StreamSource(new StringReader("<d>text<x/></d>")), null);
        assertTrue(SAXVisitAfterAndChildrenVisitor.visited);
        assertTrue(SAXVisitAfterAndChildrenVisitor.onChildText);
        assertTrue(SAXVisitAfterAndChildrenVisitor.onChildElement);
        SAXVisitAfterAndChildrenVisitor.reset();
    }

    @Test
    public void test_report() throws IOException, SAXException {
        System.out.println("********* FIX TEST!!");
        /*
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-04.xml"));
        ExecutionContext executionContext = smooks.createExecutionContext();
        StringWriter reportWriter = new StringWriter();

        executionContext.setEventListener(new FlatReportGenerator(reportWriter));
        smooks.filter(new StreamSource(new StringReader("<c/>")), null, executionContext);

        assertTrue(StreamUtils.compareCharStreams(
                getClass().getResourceAsStream("report-expected.txt"),
                new ByteArrayInputStream(reportWriter.toString().getBytes())));
        */
    }
}
