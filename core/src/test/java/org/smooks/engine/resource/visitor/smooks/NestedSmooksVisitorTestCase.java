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
package org.smooks.engine.resource.visitor.smooks;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.DOMWriter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.smooks.Smooks;
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.api.io.Sink;
import org.smooks.api.io.Source;
import org.smooks.api.resource.visitor.sax.ng.AfterVisitor;
import org.smooks.api.resource.visitor.sax.ng.BeforeVisitor;
import org.smooks.api.resource.visitor.sax.ng.ElementVisitor;
import org.smooks.engine.DefaultApplicationContextBuilder;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.smooks.engine.delivery.interceptor.InterceptorVisitorChainFactory;
import org.smooks.engine.delivery.interceptor.InterceptorVisitorDefinition;
import org.smooks.engine.delivery.interceptor.StaticProxyInterceptor;
import org.smooks.engine.delivery.sax.ng.bridge.BridgeInterceptor;
import org.smooks.engine.lookup.InterceptorVisitorChainFactoryLookup;
import org.smooks.engine.memento.SimpleVisitorMemento;
import org.smooks.engine.memento.VisitorMemento;
import org.smooks.engine.resource.visitor.dom.DOMModel;
import org.smooks.io.FragmentWriter;
import org.smooks.io.Stream;
import org.smooks.io.sink.StringSink;
import org.smooks.io.source.DOMSource;
import org.smooks.testkit.MockExecutionContext;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class NestedSmooksVisitorTestCase {

    private NestedSmooksVisitor.Action getRandomActions() {
        return NestedSmooksVisitor.Action.values()[ThreadLocalRandom.current().nextInt(NestedSmooksVisitor.Action.values().length)];
    }

    @Test
    public void testPostConstructRegistersInterceptorVisitorDefinitions() throws IOException, URISyntaxException, ClassNotFoundException, SAXException, ParserConfigurationException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());

        nestedSmooksVisitor.setAction(Optional.of(getRandomActions()));
        nestedSmooksVisitor.setOutputStreamResourceOptional(Optional.of("foo"));
        nestedSmooksVisitor.setBindIdOptional(Optional.of("foo"));
        nestedSmooksVisitor.setNestedSmooks(nestedSmooks);
        nestedSmooksVisitor.setApplicationContext(new DefaultApplicationContextBuilder().build());

        nestedSmooksVisitor.postConstruct();
        nestedSmooksVisitor.getNestedSmooks().createExecutionContext();

        InterceptorVisitorChainFactory interceptorVisitorChainFactory = nestedSmooksVisitor.getNestedSmooks().getApplicationContext().getRegistry().lookup(new InterceptorVisitorChainFactoryLookup());
        List<InterceptorVisitorDefinition> interceptorVisitorDefinitions = interceptorVisitorChainFactory.getInterceptorVisitorDefinitions();
        assertEquals(interceptorVisitorDefinitions.get(0).getInterceptorVisitorClass(), BridgeInterceptor.class);
        assertEquals(interceptorVisitorDefinitions.get(1).getInterceptorVisitorClass(), StaticProxyInterceptor.class);
    }

    @Test
    public void testFilterSourceGivenExecutionContextContentEncoding() throws URISyntaxException, ClassNotFoundException, SAXException, DocumentException, ParserConfigurationException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        nestedSmooksVisitor.setNestedSmooks(new Smooks() {
            @Override
            public void filterSource(ExecutionContext executionContext, Source source, Sink... sinks) throws SmooksException {
                assertEquals("ISO-8859-1", executionContext.getContentEncoding());
            }
        });

        ExecutionContext executionContext = new MockExecutionContext();
        executionContext.setContentEncoding("ISO-8859-1");

        nestedSmooksVisitor.onPreExecution(executionContext);
        nestedSmooksVisitor.filterSource(new NodeFragment(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()), new NodeFragment(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()), null, executionContext, null);
    }

    @Test
    public void testFilterSourceGivenExecutionContextDomModel() throws ParserConfigurationException {
        DOMModel domModel = new DOMModel();
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        nestedSmooksVisitor.setNestedSmooks(new Smooks() {
            @Override
            public void filterSource(ExecutionContext executionContext, Source source, Sink... sinks) throws SmooksException {
                assertEquals(domModel, executionContext.get(DOMModel.DOM_MODEL_TYPED_KEY));
            }
        });

        ExecutionContext executionContext = new MockExecutionContext();
        executionContext.put(DOMModel.DOM_MODEL_TYPED_KEY, domModel);

        nestedSmooksVisitor.onPreExecution(executionContext);
        nestedSmooksVisitor.filterSource(new NodeFragment(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()), new NodeFragment(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()), null, executionContext, null);
    }

    @Test
    public void testVisitGivenMemento() throws DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        nestedSmooks.addVisitor(new ElementVisitor() {
            @Override
            public void visitBefore(Element element, ExecutionContext executionContext) {
                executionContext.getMementoCaretaker().capture(new SimpleVisitorMemento<>(new NodeFragment(element), this, "Hello World!"));
            }
            
            @Override
            public void visitAfter(Element element, ExecutionContext executionContext) {
                VisitorMemento<String> memento = new SimpleVisitorMemento<>(new NodeFragment(element), this, "");
                executionContext.getMementoCaretaker().restore(memento);
                assertEquals("Hello World!", memento.getState());
            }

            @Override
            public void visitChildText(CharacterData characterData, ExecutionContext executionContext) {
                VisitorMemento<String> memento = new SimpleVisitorMemento<>(new NodeFragment(characterData.getParentNode()), this, "");
                executionContext.getMementoCaretaker().restore(memento);
                assertEquals("Hello World!", memento.getState());
            }

            @Override
            public void visitChildElement(Element childElement, ExecutionContext executionContext) {
                // TODO
            }
        }, "a");

        nestedSmooksVisitor.setAction(Optional.of(getRandomActions()));
        nestedSmooksVisitor.setOutputStreamResourceOptional(Optional.of("foo"));
        nestedSmooksVisitor.setBindIdOptional(Optional.of("foo"));
        nestedSmooksVisitor.setNestedSmooks(nestedSmooks);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink stringResult = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").addText("bar").
                getDocument())), stringResult);
    }

    @Test
    public void testVisitBeforeGivenSelectorHasAncestors() throws DocumentException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        nestedSmooks.addVisitor((BeforeVisitor) (element, executionContext) -> {
            assertEquals("b", element.getParentNode().getNodeName());
            assertEquals(element.getOwnerDocument(), element.getParentNode().getParentNode());
            assertNull(element.getParentNode().getParentNode().getParentNode());
            countDownLatch.countDown();
        }, "c");
        
        nestedSmooksVisitor.setAction(Optional.of(getRandomActions()));
        nestedSmooksVisitor.setOutputStreamResourceOptional(Optional.of("foo"));
        nestedSmooksVisitor.setBindIdOptional(Optional.of("foo"));
        nestedSmooksVisitor.setNestedSmooks(nestedSmooks);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "b");

        StringSink stringResult = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").addElement("b").addElement("c").
                getDocument())), stringResult);

        assertEquals(0, countDownLatch.getCount());
    }

    @Test
    public void testVisitAfterGivenSelectorHasAncestors() throws DocumentException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        nestedSmooks.addVisitor((AfterVisitor) (element, executionContext) -> {
            assertEquals("b", element.getParentNode().getNodeName());
            assertEquals(element.getOwnerDocument(), element.getParentNode().getParentNode());
            assertNull(element.getParentNode().getParentNode().getParentNode());
            countDownLatch.countDown();
        }, "c");

        nestedSmooksVisitor.setAction(Optional.of(getRandomActions()));
        nestedSmooksVisitor.setOutputStreamResourceOptional(Optional.of("foo"));
        nestedSmooksVisitor.setBindIdOptional(Optional.of("foo"));
        nestedSmooksVisitor.setNestedSmooks(nestedSmooks);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "b");

        StringSink stringResult = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").addElement("b").addElement("c").
                getDocument())), stringResult);

        assertEquals(0, countDownLatch.getCount());
    }

    @Test
    public void testVisitChildTextGivenSelectorAncestors() throws DocumentException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        nestedSmooks.addVisitor(new ElementVisitor() {
            @Override
            public void visitBefore(Element element, ExecutionContext executionContext) {

            }

            @Override
            public void visitAfter(Element element, ExecutionContext executionContext) {
                
            }

            @Override
            public void visitChildText(CharacterData characterData, ExecutionContext executionContext) {
                assertEquals("c", characterData.getParentNode().getNodeName());
                assertEquals("b", characterData.getParentNode().getParentNode().getNodeName());
                assertEquals(characterData.getOwnerDocument(), characterData.getParentNode().getParentNode().getParentNode());
                assertNull(characterData.getParentNode().getParentNode().getParentNode().getParentNode());
                countDownLatch.countDown();
            }

            @Override
            public void visitChildElement(Element childElement, ExecutionContext executionContext) {

            }
        }, "c");

        nestedSmooksVisitor.setAction(Optional.of(getRandomActions()));
        nestedSmooksVisitor.setOutputStreamResourceOptional(Optional.of("foo"));
        nestedSmooksVisitor.setBindIdOptional(Optional.of("foo"));
        nestedSmooksVisitor.setNestedSmooks(nestedSmooks);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "b");

        StringSink stringResult = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").addElement("b").addElement("c").addText("Hello World!").
                getDocument())), stringResult);

        assertEquals(0, countDownLatch.getCount());
    }
    
    @Test
    public void testVisitChildTextGivenPrependBefore() throws DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        nestedSmooks.addVisitor(new ElementVisitor() {
            @Override
            public void visitBefore(Element element, ExecutionContext executionContext) {
            }

            @Override
            public void visitAfter(Element element, ExecutionContext executionContext) {
            }

            @Override
            public void visitChildText(CharacterData characterData, ExecutionContext executionContext) {
                try {
                    Stream.out(executionContext).write("bar");
                } catch (IOException e) {
                    throw new SmooksException(e);
                }
            }

            @Override
            public void visitChildElement(Element childElement, ExecutionContext executionContext) {

            }
        }, "a");
        
        nestedSmooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.PREPEND_BEFORE));
        nestedSmooksVisitor.setNestedSmooks(nestedSmooks);
        
        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink stringResult = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument())), stringResult);
        
        assertEquals("bar<a>foo</a>", stringResult.toString());
    }

    @Test
    public void testVisitChildTextGivenPrependAfter() throws DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        nestedSmooks.addVisitor(new ElementVisitor() {
            @Override
            public void visitBefore(Element element, ExecutionContext executionContext) {
            }

            @Override
            public void visitAfter(Element element, ExecutionContext executionContext) {
            }

            @Override
            public void visitChildText(CharacterData characterData, ExecutionContext executionContext) {
                try {
                    Stream.out(executionContext).write("bar");
                } catch (IOException e) {
                    throw new SmooksException(e);
                }
            }

            @Override
            public void visitChildElement(Element childElement, ExecutionContext executionContext) {

            }
        }, "a");

        nestedSmooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.PREPEND_AFTER));
        nestedSmooksVisitor.setNestedSmooks(nestedSmooks);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink stringResult = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument())), stringResult);

        assertEquals("<a>barfoo</a>", stringResult.toString());
    }

    @Test
    @Disabled("TODO: undefined behaviour")
    public void testVisitChildElementGivenPrependBefore() throws SAXException, IOException, URISyntaxException, DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        nestedSmooks.addVisitor(new ElementVisitor() {
            @Override
            public void visitBefore(Element element, ExecutionContext executionContext) {
            }

            @Override
            public void visitAfter(Element element, ExecutionContext executionContext) {
            }

            @Override
            public void visitChildText(CharacterData characterData, ExecutionContext executionContext) {
            }

            @Override
            public void visitChildElement(Element childElement, ExecutionContext executionContext) {
                try {
                    new FragmentWriter(executionContext, new NodeFragment(childElement)).write("bar");
                } catch (IOException e) {
                    throw new SmooksException(e);
                }
            }
        }, "a");

        nestedSmooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.PREPEND_BEFORE));
        nestedSmooksVisitor.setNestedSmooks(nestedSmooks);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink stringResult = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                addElement("b").
                getDocument())), stringResult);

        assertEquals("<a>foobar</a>", stringResult.toString());
    }

    @Test
    @Disabled("TODO: undefined behaviour")
    public void testVisitChildElementGivenPrependAfter() throws SAXException, IOException, URISyntaxException, DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        nestedSmooks.addVisitor(new ElementVisitor() {
            @Override
            public void visitBefore(Element element, ExecutionContext executionContext) {
            }

            @Override
            public void visitAfter(Element element, ExecutionContext executionContext) {
            }

            @Override
            public void visitChildText(CharacterData characterData, ExecutionContext executionContext) {
            }

            @Override
            public void visitChildElement(Element childElement, ExecutionContext executionContext) {
                try {
                    new FragmentWriter(executionContext, new NodeFragment(childElement)).write("bar");
                } catch (IOException e) {
                    throw new SmooksException(e);
                }
            }
        }, "a");

        nestedSmooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.PREPEND_AFTER));
        nestedSmooksVisitor.setNestedSmooks(nestedSmooks);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink stringResult = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                addElement("b").
                getDocument())), stringResult);

        assertEquals("<a>foobar</a>", stringResult.toString());
    }
    
    @Test
    public void testVisitBeforeGivenPrependBefore() throws DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        nestedSmooks.addVisitor((BeforeVisitor) (element, executionContext) -> {
            try {
                Stream.out(executionContext).write("bar");
            } catch (IOException e) {
                throw new SmooksException(e);
            }
        }, "a");

        nestedSmooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.PREPEND_BEFORE));
        nestedSmooksVisitor.setNestedSmooks(nestedSmooks);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink stringResult = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument())), stringResult);

        assertEquals("bar<a>foo</a>", stringResult.toString());
    }
    
    @Test
    public void testVisitBeforeGivenPrependAfter() throws DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        nestedSmooks.addVisitor((BeforeVisitor) (element, executionContext) -> {
            try {
                Stream.out(executionContext).write("bar");
            } catch (IOException e) {
                throw new SmooksException(e);
            }
        }, "a");

        nestedSmooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.PREPEND_AFTER));
        nestedSmooksVisitor.setNestedSmooks(nestedSmooks);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink stringResult = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument())), stringResult);

        assertEquals("<a>barfoo</a>", stringResult.toString());
    }

    @Test
    public void testVisitAfterGivenPrependBefore() throws DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        nestedSmooks.addVisitor((AfterVisitor) (element, executionContext) -> {
            try {
                Stream.out(executionContext).write("bar");
            } catch (IOException e) {
                throw new SmooksException(e);
            }
        }, "a");

        nestedSmooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.PREPEND_BEFORE));
        nestedSmooksVisitor.setNestedSmooks(nestedSmooks);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink stringResult = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument())), stringResult);

        assertEquals("bar<a>foo</a>", stringResult.toString());
    }

    @Test
    public void testVisitAfterGivenPrependAfter() throws DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        nestedSmooks.addVisitor((AfterVisitor) (element, executionContext) -> {
            try {
                Stream.out(executionContext).write("bar");
            } catch (IOException e) {
                throw new SmooksException(e);
            }
        }, "a");

        nestedSmooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.PREPEND_AFTER));
        nestedSmooksVisitor.setNestedSmooks(nestedSmooks);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink stringResult = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument())), stringResult);

        assertEquals("<a>barfoo</a>", stringResult.toString());
    }
    
    @Test
    public void testVisitBeforeGivenAppendBefore() throws DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        nestedSmooks.addVisitor((BeforeVisitor) (element, executionContext) -> {
            try {
                Stream.out(executionContext).write("bar");
            } catch (IOException e) {
                throw new SmooksException(e);
            }
        }, "a");

        nestedSmooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.APPEND_BEFORE));
        nestedSmooksVisitor.setNestedSmooks(nestedSmooks);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink stringResult = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument())), stringResult);

        assertEquals("<a>barfoo</a>", stringResult.toString());
    }

    @Test
    public void testVisitBeforeGivenAppendAfter() throws DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        nestedSmooks.addVisitor((BeforeVisitor) (element, executionContext) -> {
            try {
                Stream.out(executionContext).write("bar");
            } catch (IOException e) {
                throw new SmooksException(e);
            }
        }, "a");

        nestedSmooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.APPEND_AFTER));
        nestedSmooksVisitor.setNestedSmooks(nestedSmooks);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink stringResult = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument())), stringResult);

        assertEquals("<a>barfoo</a>", stringResult.toString());
    }

    @Test
    public void testVisitAfterGivenAppendBefore() throws DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        nestedSmooks.addVisitor((AfterVisitor) (element, executionContext) -> {
            try {
                Stream.out(executionContext).write("bar");
            } catch (IOException e) {
                throw new SmooksException(e);
            }
        }, "a");

        nestedSmooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.APPEND_BEFORE));
        nestedSmooksVisitor.setNestedSmooks(nestedSmooks);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink stringResult = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument())), stringResult);

        assertEquals("<a>foobar</a>", stringResult.toString());
    }

    @Test
    public void testVisitAfterGivenAppendAfter() throws DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        nestedSmooks.addVisitor((AfterVisitor) (element, executionContext) -> {
            try {
                Stream.out(executionContext).write("bar");
            } catch (IOException e) {
                throw new SmooksException(e);
            }
        }, "a");

        nestedSmooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.APPEND_AFTER));
        nestedSmooksVisitor.setNestedSmooks(nestedSmooks);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink stringResult = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument())), stringResult);

        assertEquals("<a>foo</a>bar", stringResult.toString());
    }
}
