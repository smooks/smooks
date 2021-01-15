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
package org.smooks.delivery;

import org.junit.Test;
import org.smooks.Smooks;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.dom.DOMContentDeliveryConfig;
import org.smooks.delivery.dom.DOMVisitAfter;
import org.smooks.delivery.dom.DOMVisitBefore;
import org.smooks.delivery.dom.serialize.ContextObjectSerializerVisitor;
import org.smooks.delivery.dom.serialize.DefaultDOMSerializerVisitor;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * ConfigurationExpander tests.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ExpandableContentDeliveryUnitTest {

	@Test
    public void test() throws IOException, SAXException {
        Smooks smooks = new Smooks();

        smooks.addConfigurations("expansion-config.xml", getClass().getResourceAsStream("expansion-config.xml"));
        ExecutionContext executionContext = smooks.createExecutionContext();

        DOMContentDeliveryConfig config = (DOMContentDeliveryConfig) executionContext.getContentDeliveryRuntime().getContentDeliveryConfig();
        SelectorTable<DOMVisitBefore> assemblyVisitBeforeSelectorTable = config.getAssemblyVisitBeforeSelectorTable();
        SelectorTable<DOMVisitAfter> assemblyVisitAfterSelectorTable = config.getAssemblyVisitAfterSelectorTable();
        SelectorTable<DOMVisitBefore> processingVisitBeforeSelectorTable = config.getProcessingVisitBeforeSelectorTable();
        SelectorTable<DOMVisitAfter> processingVisitAfterSelectorTable = config.getProcessingVisitAfterSelectorTable();
        SelectorTable<SerializerVisitor> serializerVisitorSelectorTable = config.getSerializerVisitorSelectorTable();

        assertEquals(1, assemblyVisitBeforeSelectorTable.values().stream().mapToLong(Collection::size).sum());
        assertTrue(assemblyVisitBeforeSelectorTable.get("a").get(0).getContentHandler() instanceof Assembly1);
        assertEquals(1, assemblyVisitAfterSelectorTable.values().stream().mapToLong(Collection::size).sum());
        assertTrue(assemblyVisitAfterSelectorTable.get("a").get(0).getContentHandler() instanceof Assembly1);

        assertEquals(3, processingVisitBeforeSelectorTable.values().stream().mapToLong(Collection::size).sum());
        assertTrue(processingVisitBeforeSelectorTable.get("b").get(0).getContentHandler() instanceof Processing1);
        assertEquals(3, processingVisitAfterSelectorTable.values().stream().mapToLong(Collection::size).sum());
        assertTrue(processingVisitAfterSelectorTable.get("b").get(0).getContentHandler() instanceof Processing1);

        assertEquals(4, serializerVisitorSelectorTable.values().stream().mapToLong(Collection::size).sum());
        assertTrue(serializerVisitorSelectorTable.get("c").get(0).getContentHandler() instanceof DefaultDOMSerializerVisitor);
        assertTrue(serializerVisitorSelectorTable.get("context-object").get(0).getContentHandler() instanceof ContextObjectSerializerVisitor);
    }
}
