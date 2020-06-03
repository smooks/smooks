/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0 or,
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
package org.smooks.delivery.dom;

import org.junit.Test;
import org.smooks.Smooks;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.ContentHandlerConfigMap;
import org.smooks.event.BasicExecutionEventListener;
import org.smooks.io.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests to make sure the phase annotations work properly.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SmooksVisitorPhaseTest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger( SmooksVisitorPhaseTest.class );

	@Test
    public void test_phase_selection() throws IOException, SAXException {
        Smooks smooks = new Smooks();
        ExecutionContext execContext;
        DOMContentDeliveryConfig config;

        smooks.addConfigurations("config1.xml", getClass().getResourceAsStream("config1.xml"));
        execContext = smooks.createExecutionContext();
        config = (DOMContentDeliveryConfig) execContext.getDeliveryConfig();

        // Check the assembly units...
        List<ContentHandlerConfigMap<DOMVisitBefore>> assemblyVBs = config.getAssemblyVisitBefores().getMappings("a");
        List<ContentHandlerConfigMap<DOMVisitAfter>> assemblyVAs = config.getAssemblyVisitAfters().getMappings("a");
        assertEquals(2, assemblyVBs.size());
        assertTrue(assemblyVBs.get(0).getContentHandler() instanceof AssemblyVisitor1);
        assertTrue(assemblyVBs.get(1).getContentHandler() instanceof ConfigurableVisitor);
        assertEquals(2, assemblyVAs.size());
        assertTrue(assemblyVAs.get(0).getContentHandler() instanceof ConfigurableVisitor);
        assertTrue(assemblyVAs.get(1).getContentHandler() instanceof AssemblyVisitor1);

        List<ContentHandlerConfigMap<DOMVisitBefore>> processingVBs = config.getProcessingVisitBefores().getMappings("a");
        List<ContentHandlerConfigMap<DOMVisitAfter>> processingVAs = config.getProcessingVisitAfters().getMappings("a");
        assertEquals(2, processingVBs.size());
        assertTrue(processingVBs.get(0).getContentHandler() instanceof ProcessorVisitor1);
        assertTrue(processingVBs.get(1).getContentHandler() instanceof ConfigurableVisitor);
        assertEquals(2, processingVAs.size());
        assertTrue(processingVAs.get(0).getContentHandler() instanceof ConfigurableVisitor);
        assertTrue(processingVAs.get(1).getContentHandler() instanceof ProcessorVisitor1);
    }

	@Test
    public void test_filtering() throws IOException, SAXException {
        Smooks smooks = new Smooks();
        BasicExecutionEventListener eventListener = new BasicExecutionEventListener();

        smooks.addConfigurations("config2.xml", getClass().getResourceAsStream("config2.xml"));
        // Create an exec context - no profiles....
        ExecutionContext executionContext = smooks.createExecutionContext();
        CharArrayWriter outputWriter = new CharArrayWriter();

        // Filter the input message to the outputWriter, using the execution context...
        executionContext.setEventListener(eventListener);
        smooks.filterSource(executionContext, new StreamSource(getClass().getResourceAsStream("testxml1.xml")), new StreamResult(outputWriter));

        LOGGER.debug(outputWriter.toString());
        byte[] expected = StreamUtils.readStream(getClass().getResourceAsStream("testxml1-expected.xml"));
        assertTrue(StreamUtils.compareCharStreams(new ByteArrayInputStream(expected), new ByteArrayInputStream(outputWriter.toString().getBytes())));
        assertEquals(32, eventListener.getEvents().size());
    }
}
