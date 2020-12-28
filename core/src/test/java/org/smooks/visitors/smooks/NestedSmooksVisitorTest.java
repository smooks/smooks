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
package org.smooks.visitors.smooks;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.DOMWriter;
import org.junit.Ignore;
import org.junit.Test;
import org.smooks.Smooks;
import org.smooks.container.ExecutionContext;
import org.smooks.container.standalone.DefaultApplicationContextBuilder;
import org.smooks.delivery.fragment.NodeFragment;
import org.smooks.delivery.sax.ng.AfterVisitor;
import org.smooks.delivery.sax.ng.BeforeVisitor;
import org.smooks.delivery.sax.ng.ElementVisitor;
import org.smooks.io.DefaultFragmentWriter;
import org.smooks.payload.StringResult;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.transform.dom.DOMSource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class NestedSmooksVisitorTest {
    
    @Test
    public void testVisitChildTextGivenInsertBefore() throws SAXException, IOException, URISyntaxException, DocumentException {
        NestedSmooksVisitor smooksVisitor = new NestedSmooksVisitor();
        Smooks nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().setRegisterSystemResources(false).build());
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
                    new DefaultFragmentWriter(executionContext, new NodeFragment(characterData)).write("bar");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void visitChildElement(Element childElement, ExecutionContext executionContext) {

            }
        }, "a");
        
        smooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.INSERT_BEFORE));
        smooksVisitor.setNestedSmooks(nestedSmooks);
        
        Smooks smooks = new Smooks();
        smooks.addVisitor(smooksVisitor, "a");

        StringResult stringResult = new StringResult();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument()).getDocumentElement()), stringResult);
        
        assertEquals("<a>foobar</a>", stringResult.toString());
    }

    @Test
    public void testVisitChildTextGivenInsertAfter() throws SAXException, IOException, URISyntaxException, DocumentException {
        NestedSmooksVisitor smooksVisitor = new NestedSmooksVisitor();
        Smooks nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().setRegisterSystemResources(false).build());
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
                    new DefaultFragmentWriter(executionContext, new NodeFragment(characterData)).write("bar");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void visitChildElement(Element childElement, ExecutionContext executionContext) {

            }
        }, "a");

        smooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.INSERT_AFTER));
        smooksVisitor.setNestedSmooks(nestedSmooks);

        Smooks smooks = new Smooks();
        smooks.addVisitor(smooksVisitor, "a");

        StringResult stringResult = new StringResult();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument()).getDocumentElement()), stringResult);

        assertEquals("<a>foobar</a>", stringResult.toString());
    }

    @Test
    @Ignore("FIXME: visitChildElement(...) is not executed")
    public void testVisitChildElementGivenInsertBefore() throws SAXException, IOException, URISyntaxException, DocumentException {
        NestedSmooksVisitor smooksVisitor = new NestedSmooksVisitor();
        Smooks nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().setRegisterSystemResources(false).build());
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
                    new DefaultFragmentWriter(executionContext, new NodeFragment(childElement)).write("bar");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, "a");

        smooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.INSERT_BEFORE));
        smooksVisitor.setNestedSmooks(nestedSmooks);

        Smooks smooks = new Smooks();
        smooks.addVisitor(smooksVisitor, "a");

        StringResult stringResult = new StringResult();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                addElement("b").
                getDocument()).getDocumentElement()), stringResult);

        assertEquals("<a>foobar</a>", stringResult.toString());
    }

    @Test
    @Ignore("FIXME: visitChildElement(...) is not executed")
    public void testVisitChildElementGivenInsertAfter() throws SAXException, IOException, URISyntaxException, DocumentException {
        NestedSmooksVisitor smooksVisitor = new NestedSmooksVisitor();
        Smooks nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().setRegisterSystemResources(false).build());
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
                    new DefaultFragmentWriter(executionContext, new NodeFragment(childElement)).write("bar");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, "a");

        smooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.INSERT_AFTER));
        smooksVisitor.setNestedSmooks(nestedSmooks);

        Smooks smooks = new Smooks();
        smooks.addVisitor(smooksVisitor, "a");

        StringResult stringResult = new StringResult();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                addElement("b").
                getDocument()).getDocumentElement()), stringResult);

        assertEquals("<a>foobar</a>", stringResult.toString());
    }
    
    @Test
    public void testVisitBeforeGivenInsertBefore() throws DocumentException {
        NestedSmooksVisitor smooksVisitor = new NestedSmooksVisitor();
        Smooks nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().setRegisterSystemResources(false).build());
        nestedSmooks.addVisitor((BeforeVisitor) (element, executionContext) -> {
            try {
                new DefaultFragmentWriter(executionContext, new NodeFragment(element)).write("bar");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "a");

        smooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.INSERT_BEFORE));
        smooksVisitor.setNestedSmooks(nestedSmooks);

        Smooks smooks = new Smooks();
        smooks.addVisitor(smooksVisitor, "a");

        StringResult stringResult = new StringResult();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument()).getDocumentElement()), stringResult);

        assertEquals("bar<a>foo</a>", stringResult.toString());
    }
    
    @Test
    public void testVisitBeforeGivenInsertAfter() throws DocumentException {
        NestedSmooksVisitor smooksVisitor = new NestedSmooksVisitor();
        Smooks nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().setRegisterSystemResources(false).build());
        nestedSmooks.addVisitor((BeforeVisitor) (element, executionContext) -> {
            try {
                new DefaultFragmentWriter(executionContext, new NodeFragment(element)).write("bar");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "a");

        smooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.INSERT_AFTER));
        smooksVisitor.setNestedSmooks(nestedSmooks);

        Smooks smooks = new Smooks();
        smooks.addVisitor(smooksVisitor, "a");

        StringResult stringResult = new StringResult();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument()).getDocumentElement()), stringResult);

        assertEquals("<a>barfoo</a>", stringResult.toString());
    }

    @Test
    public void testVisitAfterGivenInsertBefore() throws DocumentException {
        NestedSmooksVisitor smooksVisitor = new NestedSmooksVisitor();
        Smooks nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().setRegisterSystemResources(false).build());
        nestedSmooks.addVisitor((AfterVisitor) (element, executionContext) -> {
            try {
                new DefaultFragmentWriter(executionContext, new NodeFragment(element)).write("bar");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "a");

        smooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.INSERT_BEFORE));
        smooksVisitor.setNestedSmooks(nestedSmooks);

        Smooks smooks = new Smooks();
        smooks.addVisitor(smooksVisitor, "a");

        StringResult stringResult = new StringResult();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument()).getDocumentElement()), stringResult);

        assertEquals("<a>foobar</a>", stringResult.toString());
    }

    @Test
    public void testVisitAfterGivenInsertAfter() throws DocumentException {
        NestedSmooksVisitor smooksVisitor = new NestedSmooksVisitor();
        Smooks nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().setRegisterSystemResources(false).build());
        nestedSmooks.addVisitor((AfterVisitor) (element, executionContext) -> {
            try {
                new DefaultFragmentWriter(executionContext, new NodeFragment(element)).write("bar");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "a");

        smooksVisitor.setAction(Optional.of(NestedSmooksVisitor.Action.INSERT_AFTER));
        smooksVisitor.setNestedSmooks(nestedSmooks);

        Smooks smooks = new Smooks();
        smooks.addVisitor(smooksVisitor, "a");

        StringResult stringResult = new StringResult();
        smooks.filterSource(new DOMSource(new DOMWriter().write(DocumentHelper.createDocument().
                addElement("a").
                addText("foo").
                getDocument()).getDocumentElement()), stringResult);

        assertEquals("<a>foo</a>bar", stringResult.toString());
    }
}
