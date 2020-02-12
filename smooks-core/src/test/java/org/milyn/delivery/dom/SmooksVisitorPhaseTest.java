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
package org.milyn.delivery.dom;

import org.junit.Test;
import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.ContentHandlerConfigMap;
import org.milyn.event.BasicExecutionEventListener;
import org.milyn.io.StreamUtils;
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
