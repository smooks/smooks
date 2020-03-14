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
package org.smooks.delivery;

import org.junit.Test;
import org.smooks.Smooks;
import org.smooks.container.ExecutionContext;
import org.smooks.event.BasicExecutionEventListener;
import org.smooks.event.types.FilterLifecycleEvent;
import org.smooks.event.types.ResourceTargetingEvent;
import org.smooks.io.NullWriter;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@SuppressWarnings("unchecked")
public class ExecutionEventListenerTest {

	@Test
    public void test_01_dom() throws IOException, SAXException {
        BasicExecutionEventListener eventListener = new BasicExecutionEventListener();

        testListener(eventListener, "smooks-config-dom.xml", "test-data-01.xml");
        assertEquals(38, eventListener.getEvents().size());
    }

	@Test
    public void test_01_sax() throws IOException, SAXException {
        BasicExecutionEventListener eventListener = new BasicExecutionEventListener();

        testListener(eventListener, "smooks-config-sax.xml", "test-data-01.xml");
        assertEquals(23, eventListener.getEvents().size());
    }

	@Test
    public void test_02_dom() throws IOException, SAXException {
        BasicExecutionEventListener eventListener = new BasicExecutionEventListener();

        eventListener.setFilterEvents(FilterLifecycleEvent.class);
        testListener(eventListener, "smooks-config-dom.xml", "test-data-01.xml");
        assertEquals(23, eventListener.getEvents().size());
    }

	@Test
    public void test_03_dom() throws IOException, SAXException {
        BasicExecutionEventListener eventListener = new BasicExecutionEventListener();

        eventListener.setFilterEvents(ResourceTargetingEvent.class);
        testListener(eventListener, "smooks-config-dom.xml", "test-data-01.xml");
        assertEquals(30, eventListener.getEvents().size());
    }

	@Test
    public void test_04_dom() throws IOException, SAXException {
        BasicExecutionEventListener eventListener = new BasicExecutionEventListener();

        eventListener.setFilterEvents(FilterLifecycleEvent.class, ResourceTargetingEvent.class);
        testListener(eventListener, "smooks-config-dom.xml", "test-data-01.xml");
        assertEquals(30, eventListener.getEvents().size());
    }

    private void testListener(BasicExecutionEventListener eventListener, String config, String sourceFile) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream(config));
        ExecutionContext execContext = smooks.createExecutionContext();
        StreamSource source = new StreamSource(getClass().getResourceAsStream(sourceFile));

        execContext.setEventListener(eventListener);
        smooks.filterSource(execContext, source, new StreamResult(new NullWriter()));
    }


}
