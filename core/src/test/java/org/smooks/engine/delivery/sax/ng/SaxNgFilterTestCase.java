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
package org.smooks.engine.delivery.sax.ng;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.smooks.Smooks;
import org.smooks.api.ExecutionContext;
import org.smooks.engine.report.FlatReportGenerator;
import org.smooks.io.StreamUtils;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

import static org.junit.Assert.*;

public class SaxNgFilterTestCase {

    @Before
    public void before() throws Exception {
        Visitor01.element = null;
        Visitor01.children.clear();
        Visitor01.childText.clear();
        Visitor02.element = null;
        Visitor02.children.clear();
        Visitor02.childText.clear();
        Visitor03.element = null;
        Visitor03.children.clear();
        Visitor03.childText.clear();

        VisitBeforeVisitor.visited = false;
        BeforeVisitorAndChildrenVisitor.reset();
        VisitAfterVisitor.visited = false;
        AfterVisitorAndChildrenVisitor.reset();
    }
    
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

	@Test
    public void test_selection() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-02.xml"));
        ExecutionContext execContext = smooks.createExecutionContext();
        String input = new String(StreamUtils.readStream(getClass().getResourceAsStream("test-01.xml")));

        smooks.filterSource(execContext, new StreamSource(new ByteArrayInputStream(input.getBytes())), null);
        assertEquals("h", Visitor01.element.getLocalName());
        assertEquals("y", Visitor01.element.getPrefix());
        assertEquals("http://y", Visitor01.element.getNamespaceURI());
        assertEquals(0, Visitor01.children.size());
        assertEquals(0, Visitor01.childText.size());

        assertEquals("i", Visitor02.element.getLocalName());
        assertNull(Visitor02.element.getPrefix());
        assertEquals("http://x", Visitor02.element.getNamespaceURI());
        assertEquals(0, Visitor02.children.size());
        assertEquals(1, Visitor02.childText.size());

        assertEquals("h", Visitor03.element.getLocalName());
        assertNull(Visitor03.element.getPrefix());
        assertEquals("http://x", Visitor03.element.getNamespaceURI());
        assertEquals(1, Visitor03.children.size());
        assertEquals(7, Visitor03.childText.size());
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
        assertEquals("h", Visitor01.element.getLocalName());
        assertEquals("y", Visitor01.element.getPrefix());
        assertEquals("http://y", Visitor01.element.getNamespaceURI());
        assertEquals(0, Visitor01.children.size());
        assertEquals(0, Visitor01.childText.size());

        assertNull(Visitor02.element);

        assertEquals("i", Visitor03.element.getLocalName());
        assertNull(Visitor03.element.getPrefix());
        assertEquals("http://x", Visitor03.element.getNamespaceURI());
        assertEquals(0, Visitor03.children.size());
        assertEquals(1, Visitor03.childText.size());
    }

    @Test
    public void test_document() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-04.xml"));
        ExecutionContext execContext = smooks.createExecutionContext();
        String input = new String(StreamUtils.readStream(getClass().getResourceAsStream("test-01.xml")));

        smooks.filterSource(execContext, new StreamSource(new ByteArrayInputStream(input.getBytes())), null);
        assertEquals("xx", Visitor01.element.getLocalName());
        assertEquals("xx", Visitor02.element.getLocalName());
        assertNull(Visitor03.element);
    }

    @Test
    public void test_visitBeforeOnly() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-05.xml"));

        smooks.filterSource(smooks.createExecutionContext(), new StreamSource(new StringReader("<a/>")), null);
        assertTrue(VisitBeforeVisitor.visited);
        assertEquals("Hi There!", VisitBeforeVisitor.staticInjectedParam);
    }

    @Test
    public void test_visitBeforeAndChildren() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-05.xml"));

        smooks.filterSource(smooks.createExecutionContext(), new StreamSource(new StringReader("<b/>")), null);
        assertTrue(BeforeVisitorAndChildrenVisitor.visited);
        assertFalse(BeforeVisitorAndChildrenVisitor.onChildText);
        assertFalse(BeforeVisitorAndChildrenVisitor.onChildElement);
        BeforeVisitorAndChildrenVisitor.reset();

        smooks.filterSource(smooks.createExecutionContext(), new StreamSource(new StringReader("<b>text</b>")), null);
        assertTrue(BeforeVisitorAndChildrenVisitor.visited);
        assertTrue(BeforeVisitorAndChildrenVisitor.onChildText);
        assertFalse(BeforeVisitorAndChildrenVisitor.onChildElement);
        BeforeVisitorAndChildrenVisitor.reset();

        smooks.filterSource(smooks.createExecutionContext(), new StreamSource(new StringReader("<b><x/></b>")), null);
        assertTrue(BeforeVisitorAndChildrenVisitor.visited);
        assertFalse(BeforeVisitorAndChildrenVisitor.onChildText);
        assertTrue(BeforeVisitorAndChildrenVisitor.onChildElement);
        BeforeVisitorAndChildrenVisitor.reset();

        smooks.filterSource(smooks.createExecutionContext(), new StreamSource(new StringReader("<b>text<x/></b>")), null);
        assertTrue(BeforeVisitorAndChildrenVisitor.visited);
        assertTrue(BeforeVisitorAndChildrenVisitor.onChildText);
        assertTrue(BeforeVisitorAndChildrenVisitor.onChildElement);
        BeforeVisitorAndChildrenVisitor.reset();
    }

    @Test
    public void test_visitAfterOnly() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-05.xml"));

        smooks.filterSource(smooks.createExecutionContext(), new StreamSource(new StringReader("<c/>")), null);
        assertTrue(VisitAfterVisitor.visited);
    }

    @Test
    public void test_max_node_depth() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-06.xml"));

        smooks.filterSource(smooks.createExecutionContext(), new StreamSource(new StringReader("<a><b><c><d>foo</d></c><e><f><g>bar</g></f></e></b></a>")), null);
        assertNotNull(MaxNodeDepthVisitor.element);
    }

    @Test
    public void test_visitAfterAndChildren() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-05.xml"));

        smooks.filterSource(smooks.createExecutionContext(), new StreamSource(new StringReader("<d/>")), null);
        assertTrue(AfterVisitorAndChildrenVisitor.visited);
        assertFalse(AfterVisitorAndChildrenVisitor.onChildText);
        assertFalse(AfterVisitorAndChildrenVisitor.onChildElement);
        AfterVisitorAndChildrenVisitor.reset();

        smooks.filterSource(smooks.createExecutionContext(), new StreamSource(new StringReader("<d>text</d>")), null);
        assertTrue(AfterVisitorAndChildrenVisitor.visited);
        assertTrue(AfterVisitorAndChildrenVisitor.onChildText);
        assertFalse(AfterVisitorAndChildrenVisitor.onChildElement);
        AfterVisitorAndChildrenVisitor.reset();

        smooks.filterSource(smooks.createExecutionContext(), new StreamSource(new StringReader("<d><x/></d>")), null);
        assertTrue(AfterVisitorAndChildrenVisitor.visited);
        assertFalse(AfterVisitorAndChildrenVisitor.onChildText);
        assertTrue(AfterVisitorAndChildrenVisitor.onChildElement);
        AfterVisitorAndChildrenVisitor.reset();

        smooks.filterSource(smooks.createExecutionContext(), new StreamSource(new StringReader("<d>text<x/></d>")), null);
        assertTrue(AfterVisitorAndChildrenVisitor.visited);
        assertTrue(AfterVisitorAndChildrenVisitor.onChildText);
        assertTrue(AfterVisitorAndChildrenVisitor.onChildElement);
        AfterVisitorAndChildrenVisitor.reset();
    }
    
    @Test
    @Ignore("FIXME")
    public void test_report() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-04.xml"));
        ExecutionContext executionContext = smooks.createExecutionContext();
        StringWriter reportWriter = new StringWriter();

        executionContext.getContentDeliveryRuntime().addExecutionEventListener(new FlatReportGenerator(reportWriter));
        smooks.filterSource(executionContext, new StreamSource(new StringReader("<c/>")), null);

        assertTrue(StreamUtils.compareCharStreams(getClass().getResourceAsStream("report-expected.txt"), new ByteArrayInputStream(reportWriter.toString().getBytes())));
    }
}
