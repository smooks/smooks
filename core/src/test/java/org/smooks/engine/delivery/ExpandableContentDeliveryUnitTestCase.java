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
package org.smooks.engine.delivery;

import org.junit.jupiter.api.Test;
import org.smooks.Smooks;
import org.smooks.api.ExecutionContext;
import org.smooks.api.resource.visitor.SerializerVisitor;
import org.smooks.api.resource.visitor.dom.DOMVisitAfter;
import org.smooks.api.resource.visitor.dom.DOMVisitBefore;
import org.smooks.engine.delivery.dom.DOMContentDeliveryConfig;
import org.smooks.engine.delivery.dom.serialize.ContextObjectSerializerVisitor;
import org.smooks.engine.delivery.dom.serialize.DefaultDOMSerializerVisitor;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ConfigurationExpander tests.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ExpandableContentDeliveryUnitTestCase {

	@Test
    public void test() throws IOException, SAXException {
        Smooks smooks = new Smooks();

        smooks.addConfigurations("expansion-config.xml", getClass().getResourceAsStream("expansion-config.xml"));
        ExecutionContext executionContext = smooks.createExecutionContext();

        DOMContentDeliveryConfig config = (DOMContentDeliveryConfig) executionContext.getContentDeliveryRuntime().getContentDeliveryConfig();
        ContentHandlerBindingIndex<DOMVisitBefore> assemblyVisitBeforeContentHandlerBindingIndex = config.getAssemblyVisitBeforeIndex();
        ContentHandlerBindingIndex<DOMVisitAfter> assemblyVisitAfterContentHandlerBindingIndex = config.getAssemblyVisitAfterIndex();
        ContentHandlerBindingIndex<DOMVisitBefore> processingVisitBeforeContentHandlerBindingIndex = config.getProcessingVisitBeforeIndex();
        ContentHandlerBindingIndex<DOMVisitAfter> processingVisitAfterContentHandlerBindingIndex = config.getProcessingVisitAfterIndex();
        ContentHandlerBindingIndex<SerializerVisitor> serializerVisitorContentHandlerBindingIndex = config.getSerializerVisitorIndex();

        assertEquals(1, assemblyVisitBeforeContentHandlerBindingIndex.values().stream().mapToLong(Collection::size).sum());
        assertTrue(assemblyVisitBeforeContentHandlerBindingIndex.get("a").get(0).getContentHandler() instanceof Assembly1);
        assertEquals(1, assemblyVisitAfterContentHandlerBindingIndex.values().stream().mapToLong(Collection::size).sum());
        assertTrue(assemblyVisitAfterContentHandlerBindingIndex.get("a").get(0).getContentHandler() instanceof Assembly1);

        assertEquals(3, processingVisitBeforeContentHandlerBindingIndex.values().stream().mapToLong(Collection::size).sum());
        assertTrue(processingVisitBeforeContentHandlerBindingIndex.get("b").get(0).getContentHandler() instanceof Processing1);
        assertEquals(3, processingVisitAfterContentHandlerBindingIndex.values().stream().mapToLong(Collection::size).sum());
        assertTrue(processingVisitAfterContentHandlerBindingIndex.get("b").get(0).getContentHandler() instanceof Processing1);

        assertEquals(4, serializerVisitorContentHandlerBindingIndex.values().stream().mapToLong(Collection::size).sum());
        assertTrue(serializerVisitorContentHandlerBindingIndex.get("c").get(0).getContentHandler() instanceof DefaultDOMSerializerVisitor);
        assertTrue(serializerVisitorContentHandlerBindingIndex.get("context-object").get(0).getContentHandler() instanceof ContextObjectSerializerVisitor);
    }
}
