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
import org.smooks.FilterSettings;
import org.smooks.Smooks;
import org.smooks.StreamFilterType;
import org.smooks.api.ApplicationContext;
import org.smooks.api.ExecutionContext;
import org.smooks.api.NotAppContextScoped;
import org.smooks.api.SmooksException;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.io.Sink;
import org.smooks.api.io.Source;
import org.smooks.api.lifecycle.PostFragmentLifecycle;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.visitor.sax.ng.AfterVisitor;
import org.smooks.api.resource.visitor.sax.ng.BeforeVisitor;
import org.smooks.api.resource.visitor.sax.ng.ElementVisitor;
import org.smooks.engine.DefaultApplicationContextBuilder;
import org.smooks.engine.DefaultFilterSettings;
import org.smooks.engine.delivery.event.VisitSequence;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.smooks.engine.delivery.interceptor.ExceptionInterceptor;
import org.smooks.engine.delivery.interceptor.InterceptorVisitorChainFactory;
import org.smooks.engine.delivery.interceptor.InterceptorVisitorDefinition;
import org.smooks.engine.delivery.sax.ng.SimpleSerializerVisitor;
import org.smooks.engine.delivery.sax.ng.pointer.EventPointerStaticProxyInterceptor;
import org.smooks.engine.lookup.InstanceLookup;
import org.smooks.engine.lookup.InterceptorVisitorChainFactoryLookup;
import org.smooks.engine.memento.SimpleVisitorMemento;
import org.smooks.engine.memento.VisitorMemento;
import org.smooks.engine.resource.config.DefaultResourceConfig;
import org.smooks.engine.resource.visitor.dom.DOMModel;
import org.smooks.io.AbstractOutputStreamResource;
import org.smooks.io.FragmentWriter;
import org.smooks.io.Stream;
import org.smooks.io.sink.StringSink;
import org.smooks.io.source.DOMSource;
import org.smooks.testkit.MockExecutionContext;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class NestedSmooksVisitorTestCase {

    private NestedSmooksVisitor.Action getRandomActions() {
        return NestedSmooksVisitor.Action.values()[ThreadLocalRandom.current().nextInt(NestedSmooksVisitor.Action.values().length)];
    }

    @Test
    public void testPostConstructCopiesNonAppContextScopedEntriesFromParentAppContextRegistry() throws IOException, URISyntaxException, ClassNotFoundException, SAXException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks pipeline = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());

        nestedSmooksVisitor.setAction(Optional.of(getRandomActions()));
        nestedSmooksVisitor.setOutputStreamResourceOptional(Optional.of("foo"));
        nestedSmooksVisitor.setBindIdOptional(Optional.of("foo"));
        nestedSmooksVisitor.setPipeline(pipeline);

        ApplicationContext applicationContext = new DefaultApplicationContextBuilder().build();

        FooClass foo = new FooClass();
        BarClass bar = new BarClass();

        applicationContext.getRegistry().registerObject("foo", (NotAppContextScoped.Ref<FooClass>) () -> foo);
        applicationContext.getRegistry().registerObject("bar", bar);
        nestedSmooksVisitor.setApplicationContext(applicationContext);

        nestedSmooksVisitor.postConstruct();
        assertEquals(foo, nestedSmooksVisitor.getPipeline().getApplicationContext().getRegistry().lookup("foo"));
        assertEquals(bar, nestedSmooksVisitor.getPipeline().getApplicationContext().getRegistry().lookup("bar"));
    }

    @Test
    public void testPostConstructRegistersInterceptorVisitorDefinitions() throws IOException, URISyntaxException, ClassNotFoundException, SAXException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks pipeline = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());

        nestedSmooksVisitor.setAction(Optional.of(getRandomActions()));
        nestedSmooksVisitor.setOutputStreamResourceOptional(Optional.of("foo"));
        nestedSmooksVisitor.setBindIdOptional(Optional.of("foo"));
        nestedSmooksVisitor.setPipeline(pipeline);
        nestedSmooksVisitor.setApplicationContext(new DefaultApplicationContextBuilder().build());

        nestedSmooksVisitor.postConstruct();
        nestedSmooksVisitor.getPipeline().createExecutionContext();

        InterceptorVisitorChainFactory interceptorVisitorChainFactory = nestedSmooksVisitor.getPipeline().getApplicationContext().getRegistry().lookup(new InterceptorVisitorChainFactoryLookup());
        List<InterceptorVisitorDefinition> interceptorVisitorDefinitions = interceptorVisitorChainFactory.getInterceptorVisitorDefinitions();
        assertEquals(interceptorVisitorDefinitions.get(0).getInterceptorVisitorClass(), ExceptionInterceptor.class);
        assertEquals(interceptorVisitorDefinitions.get(1).getInterceptorVisitorClass(), EventPointerStaticProxyInterceptor.class);
    }

    @Test
    public void testFilterSourceGivenExecutionContextContentEncoding() throws ParserConfigurationException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        nestedSmooksVisitor.setPipeline(new Smooks() {
            @Override
            public void filterSource(ExecutionContext executionContext, Source source, Sink... sinks) throws SmooksException {
                assertEquals("ISO-8859-1", executionContext.getContentEncoding());
            }
        });

        ExecutionContext executionContext = new MockExecutionContext();
        executionContext.setContentEncoding("ISO-8859-1");

        nestedSmooksVisitor.onPreExecution(executionContext);
        nestedSmooksVisitor.filterSource(new NodeFragment(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()), new NodeFragment(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()), null, executionContext, VisitSequence.values()[new Random().nextInt(VisitSequence.values().length)]);
    }

    @Test
    public void testFilterSourceGivenExecutionContextDomModel() throws ParserConfigurationException {
        DOMModel domModel = new DOMModel();
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        nestedSmooksVisitor.setPipeline(new Smooks() {
            @Override
            public void filterSource(ExecutionContext executionContext, Source source, Sink... sinks) throws SmooksException {
                assertEquals(domModel, executionContext.get(DOMModel.DOM_MODEL_TYPED_KEY));
            }
        });

        ExecutionContext executionContext = new MockExecutionContext();
        executionContext.put(DOMModel.DOM_MODEL_TYPED_KEY, domModel);

        nestedSmooksVisitor.onPreExecution(executionContext);
        nestedSmooksVisitor.filterSource(new NodeFragment(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()), new NodeFragment(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()), null, executionContext, VisitSequence.values()[new Random().nextInt(VisitSequence.values().length)]);
    }

    @Test
    public void testPipelineVisitorGivenMemento() throws DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks pipeline = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        pipeline.addVisitor(new ElementVisitor() {
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
        nestedSmooksVisitor.setPipeline(pipeline);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink strinkSink = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").addText("bar").
                getDocument())), strinkSink);
    }

    @Test
    public void testVisitBeforeGivenSelectorHasAncestors() throws DocumentException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks pipeline = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        pipeline.addVisitor((BeforeVisitor) (element, executionContext) -> {
            assertEquals("b", element.getParentNode().getNodeName());
            assertEquals(element.getOwnerDocument(), element.getParentNode().getParentNode());
            assertNull(element.getParentNode().getParentNode().getParentNode());
            countDownLatch.countDown();
        }, "c");
        
        nestedSmooksVisitor.setAction(Optional.of(getRandomActions()));
        nestedSmooksVisitor.setOutputStreamResourceOptional(Optional.of("foo"));
        nestedSmooksVisitor.setBindIdOptional(Optional.of("foo"));
        nestedSmooksVisitor.setPipeline(pipeline);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "b");

        StringSink strinkSink = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").addElement("b").addElement("c").
                getDocument())), strinkSink);

        assertEquals(0, countDownLatch.getCount());
    }

    @Test
    public void testVisitAfterGivenSelectorHasAncestors() throws DocumentException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks pipeline = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        pipeline.addVisitor((AfterVisitor) (element, executionContext) -> {
            assertEquals("b", element.getParentNode().getNodeName());
            assertEquals(element.getOwnerDocument(), element.getParentNode().getParentNode());
            assertNull(element.getParentNode().getParentNode().getParentNode());
            countDownLatch.countDown();
        }, "c");

        nestedSmooksVisitor.setAction(Optional.of(getRandomActions()));
        nestedSmooksVisitor.setOutputStreamResourceOptional(Optional.of("foo"));
        nestedSmooksVisitor.setBindIdOptional(Optional.of("foo"));
        nestedSmooksVisitor.setPipeline(pipeline);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "b");

        StringSink strinkSink = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").addElement("b").addElement("c").
                getDocument())), strinkSink);

        assertEquals(0, countDownLatch.getCount());
    }

    @Test
    public void testVisitChildTextGivenSelectorAncestors() throws DocumentException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks pipeline = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        pipeline.addVisitor(new ElementVisitor() {
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
        nestedSmooksVisitor.setPipeline(pipeline);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "b");

        StringSink strinkSink = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").addElement("b").addElement("c").addText("Hello World!").
                getDocument())), strinkSink);

        assertEquals(0, countDownLatch.getCount());
    }
    
    @Test
    public void testVisitChildTextGivenPrependBefore() throws DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks pipeline = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        pipeline.addVisitor(new ElementVisitor() {
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
        nestedSmooksVisitor.setPipeline(pipeline);
        
        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink strinkSink = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument())), strinkSink);
        
        assertEquals("bar<a>foo</a>", strinkSink.toString());
    }

    @Test
    public void testVisitChildTextGivenPrependAfter() throws DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks pipeline = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        pipeline.addVisitor(new ElementVisitor() {
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
        nestedSmooksVisitor.setPipeline(pipeline);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink strinkSink = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument())), strinkSink);

        assertEquals("<a>barfoo</a>", strinkSink.toString());
    }

    @Test
    @Disabled("TODO: undefined behaviour")
    public void testVisitChildElementGivenPrependBefore() throws SAXException, IOException, URISyntaxException, DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks pipeline = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        pipeline.addVisitor(new ElementVisitor() {
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
        nestedSmooksVisitor.setPipeline(pipeline);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink strinkSink = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                addElement("b").
                getDocument())), strinkSink);

        assertEquals("<a>foobar</a>", strinkSink.toString());
    }

    @Test
    @Disabled("TODO: undefined behaviour")
    public void testVisitChildElementGivenPrependAfter() throws SAXException, IOException, URISyntaxException, DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks pipeline = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        pipeline.addVisitor(new ElementVisitor() {
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
        nestedSmooksVisitor.setPipeline(pipeline);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink strinkSink = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                addElement("b").
                getDocument())), strinkSink);

        assertEquals("<a>foobar</a>", strinkSink.toString());
    }
    
    @Test
    public void testVisitBeforeGivenPrependBefore() throws DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks pipeline = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        pipeline.addVisitor((BeforeVisitor) (element, executionContext) -> {
            try {
                Stream.out(executionContext).write("bar");
            } catch (IOException e) {
                throw new SmooksException(e);
            }
        }, "a");

        nestedSmooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.PREPEND_BEFORE));
        nestedSmooksVisitor.setPipeline(pipeline);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink strinkSink = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument())), strinkSink);

        assertEquals("bar<a>foo</a>", strinkSink.toString());
    }
    
    @Test
    public void testVisitBeforeGivenPrependAfter() throws DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks pipeline = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        pipeline.addVisitor((BeforeVisitor) (element, executionContext) -> {
            try {
                Stream.out(executionContext).write("bar");
            } catch (IOException e) {
                throw new SmooksException(e);
            }
        }, "a");

        nestedSmooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.PREPEND_AFTER));
        nestedSmooksVisitor.setPipeline(pipeline);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink strinkSink = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument())), strinkSink);

        assertEquals("<a>barfoo</a>", strinkSink.toString());
    }

    @Test
    public void testVisitAfterGivenPrependBefore() throws DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks pipeline = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        pipeline.addVisitor((AfterVisitor) (element, executionContext) -> {
            try {
                Stream.out(executionContext).write("bar");
            } catch (IOException e) {
                throw new SmooksException(e);
            }
        }, "a");

        nestedSmooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.PREPEND_BEFORE));
        nestedSmooksVisitor.setPipeline(pipeline);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink strinkSink = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument())), strinkSink);

        assertEquals("bar<a>foo</a>", strinkSink.toString());
    }

    @Test
    public void testVisitAfterGivenPrependAfter() throws DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks pipeline = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        pipeline.addVisitor((AfterVisitor) (element, executionContext) -> {
            try {
                Stream.out(executionContext).write("bar");
            } catch (IOException e) {
                throw new SmooksException(e);
            }
        }, "a");

        nestedSmooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.PREPEND_AFTER));
        nestedSmooksVisitor.setPipeline(pipeline);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink strinkSink = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument())), strinkSink);

        assertEquals("<a>barfoo</a>", strinkSink.toString());
    }
    
    @Test
    public void testVisitBeforeGivenAppendBefore() throws DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks pipeline = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        pipeline.addVisitor((BeforeVisitor) (element, executionContext) -> {
            try {
                Stream.out(executionContext).write("bar");
            } catch (IOException e) {
                throw new SmooksException(e);
            }
        }, "a");

        nestedSmooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.APPEND_BEFORE));
        nestedSmooksVisitor.setPipeline(pipeline);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink strinkSink = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument())), strinkSink);

        assertEquals("<a>barfoo</a>", strinkSink.toString());
    }

    @Test
    public void testVisitBeforeGivenAppendAfter() throws DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks pipeline = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        pipeline.addVisitor((BeforeVisitor) (element, executionContext) -> {
            try {
                Stream.out(executionContext).write("bar");
            } catch (IOException e) {
                throw new SmooksException(e);
            }
        }, "a");

        nestedSmooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.APPEND_AFTER));
        nestedSmooksVisitor.setPipeline(pipeline);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink strinkSink = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument())), strinkSink);

        assertEquals("<a>barfoo</a>", strinkSink.toString());
    }

    @Test
    public void testVisitAfterGivenAppendBefore() throws DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks pipeline = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        pipeline.addVisitor((AfterVisitor) (element, executionContext) -> {
            try {
                Stream.out(executionContext).write("bar");
            } catch (IOException e) {
                throw new SmooksException(e);
            }
        }, "a");

        nestedSmooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.APPEND_BEFORE));
        nestedSmooksVisitor.setPipeline(pipeline);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink strinkSink = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument())), strinkSink);

        assertEquals("<a>foobar</a>", strinkSink.toString());
    }

    @Test
    public void testVisitAfterGivenAppendAfter() throws DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks pipeline = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        pipeline.addVisitor((AfterVisitor) (element, executionContext) -> {
            try {
                Stream.out(executionContext).write("bar");
            } catch (IOException e) {
                throw new SmooksException(e);
            }
        }, "a");

        nestedSmooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.APPEND_AFTER));
        nestedSmooksVisitor.setPipeline(pipeline);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        StringSink strinkSink = new StringSink();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument())), strinkSink);

        assertEquals("<a>foo</a>bar", strinkSink.toString());
    }

    public static class OutputStreamResourceUnderTest extends AbstractOutputStreamResource {

        private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        @Override
        public OutputStream getOutputStream(ExecutionContext executionContext) {
            return byteArrayOutputStream;
        }

        public ByteArrayOutputStream getByteArrayOutputStream() {
            return byteArrayOutputStream;
        }

    }

    @Test
    public void testOutputToGivenDocumentSelector() throws DocumentException {
        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks pipeline = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).
                withFilterSettings(new DefaultFilterSettings()
                        .setCloseSink(false))
                .build());

        pipeline.addVisitor(new SimpleSerializerVisitor(), "*");

        nestedSmooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.OUTPUT_TO));
        nestedSmooksVisitor.setOutputStreamResourceOptional(Optional.of("outputStreamResourceUnderTest"));
        nestedSmooksVisitor.setPipeline(pipeline);

        ResourceConfig resourceConfig = new DefaultResourceConfig("#document", new Properties());
        resourceConfig.setResource("org.smooks.engine.resource.visitor.smooks.NestedSmooksVisitorTestCase$OutputStreamResourceUnderTest");
        resourceConfig.setParameter("resourceName", "outputStreamResourceUnderTest");

        Smooks smooks = new Smooks();
        smooks.addResourceConfig(resourceConfig);
        smooks.addVisitor(nestedSmooksVisitor, "#document");

        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument())));

        OutputStreamResourceUnderTest outputStreamResourceUnderTest = smooks.getApplicationContext().getRegistry().lookup(new InstanceLookup<>(OutputStreamResourceUnderTest.class)).values().iterator().next();
        assertEquals("<a>foo</a>", outputStreamResourceUnderTest.getByteArrayOutputStream().toString());
    }

    @Test
    public void testPostFragmentLifecyclePipelineVisitor() throws DocumentException {
        class PostFragmentVisitorUnderTest implements ElementVisitor, PostFragmentLifecycle {
            private final CountDownLatch countDownLatch = new CountDownLatch(2);

            @Override
            public void onPostFragment(Fragment<?> fragment, ExecutionContext executionContext) {
                countDownLatch.countDown();
                assertEquals("a", ((Node) fragment.unwrap()).getNodeName());
            }

            @Override
            public void visitAfter(Element element, ExecutionContext executionContext) {

            }

            @Override
            public void visitBefore(Element element, ExecutionContext executionContext) {

            }

            @Override
            public void visitChildText(CharacterData characterData, ExecutionContext executionContext) {

            }

            @Override
            public void visitChildElement(Element childElement, ExecutionContext executionContext) {

            }

            public CountDownLatch getCountDownLatch() {
                return countDownLatch;
            }
        }

        Smooks pipeline = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        PostFragmentVisitorUnderTest postFragmentVisitorUnderTest = new PostFragmentVisitorUnderTest();
        pipeline.addVisitor(postFragmentVisitorUnderTest, "a");

        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        nestedSmooksVisitor.setAction(Optional.of(getRandomActions()));
        nestedSmooksVisitor.setOutputStreamResourceOptional(Optional.of("foo"));
        nestedSmooksVisitor.setBindIdOptional(Optional.of("foo"));
        nestedSmooksVisitor.setPipeline(pipeline);

        Smooks smooks = new Smooks();
        smooks.addVisitor(nestedSmooksVisitor, "a");

        org.dom4j.Element elementUnderTest = DocumentHelper.createDocument().
                addElement("a");
        elementUnderTest.addText("bar");
        elementUnderTest.addElement("b");

        smooks.filterSource(new DOMSource(new DOMWriter().write(elementUnderTest.
                getDocument())));

        assertEquals(1, postFragmentVisitorUnderTest.getCountDownLatch().getCount());
    }

    @Test
    public void testPipelineTerminationErrorBubblesUpWhenExceptionIsThrownFromPipelinesVisitorAndTerminateOnExceptionFilterSettingIsTrue() throws DocumentException {
        class VisitorUnderTest implements AfterVisitor {
            private final CountDownLatch countDownLatch = new CountDownLatch(1);

            @Override
            public void visitAfter(Element element, ExecutionContext executionContext) {
                countDownLatch.countDown();
                throw new RuntimeException("Unhappy path");
            }

            public CountDownLatch getCountDownLatch() {
                return countDownLatch;
            }
        }

        NestedSmooksVisitor nestedSmooksVisitor = new NestedSmooksVisitor();
        Smooks pipeline = new Smooks(new DefaultApplicationContextBuilder().withSystemResources(false).build());
        VisitorUnderTest visitorUnderTest = new VisitorUnderTest();
        pipeline.addVisitor(visitorUnderTest, "a");

        nestedSmooksVisitor.setAction(Optional.of(getRandomActions()));
        nestedSmooksVisitor.setOutputStreamResourceOptional(Optional.of("foo"));
        nestedSmooksVisitor.setBindIdOptional(Optional.of("foo"));
        nestedSmooksVisitor.setPipeline(pipeline);

        org.dom4j.Element elementUnderTest = DocumentHelper.createDocument().
                addElement("a");
        elementUnderTest.addText("bar");
        elementUnderTest.addElement("b");

        Smooks smooks = new Smooks();
        smooks.setFilterSettings(new FilterSettings().setTerminateOnException(false).setFilterType(StreamFilterType.SAX_NG));
        smooks.addVisitor(nestedSmooksVisitor, "a");
        ExecutionContext executionContext = smooks.createExecutionContext();
        smooks.filterSource(executionContext, new DOMSource(new DOMWriter().write(elementUnderTest.
                getDocument())));

        assertEquals(0, visitorUnderTest.getCountDownLatch().getCount());
        Throwable terminationError = executionContext.getTerminationError();
        assertNotNull(terminationError);
        assertEquals("Unhappy path", terminationError.getCause().getCause().getMessage());
    }
}
