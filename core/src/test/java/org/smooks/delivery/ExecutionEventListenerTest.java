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

        testListener(eventListener, "smooks-config-dom.xml");
        assertEquals(62, eventListener.getEvents().size());
    }

	@Test
    public void test_01_sax() throws IOException, SAXException {
        BasicExecutionEventListener eventListener = new BasicExecutionEventListener();

        testListener(eventListener, "smooks-config-sax.xml");
        assertEquals(41, eventListener.getEvents().size());
    }

	@Test
    public void test_02_dom() throws IOException, SAXException {
        BasicExecutionEventListener eventListener = new BasicExecutionEventListener();

        eventListener.setFilterEvents(FilterLifecycleEvent.class);
        testListener(eventListener, "smooks-config-dom.xml");
        assertEquals(23, eventListener.getEvents().size());
    }

	@Test
    public void test_03_dom() throws IOException, SAXException {
        BasicExecutionEventListener eventListener = new BasicExecutionEventListener();

        eventListener.setFilterEvents(ResourceTargetingEvent.class);
        testListener(eventListener, "smooks-config-dom.xml");
        assertEquals(42, eventListener.getEvents().size());
    }

	@Test
    public void test_04_dom() throws IOException, SAXException {
        BasicExecutionEventListener eventListener = new BasicExecutionEventListener();

        eventListener.setFilterEvents(FilterLifecycleEvent.class, ResourceTargetingEvent.class);
        testListener(eventListener, "smooks-config-dom.xml");
        assertEquals(42, eventListener.getEvents().size());
    }

    private void testListener(BasicExecutionEventListener eventListener, String config) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream(config));
        ExecutionContext execContext = smooks.createExecutionContext();
        StreamSource source = new StreamSource(getClass().getResourceAsStream("test-data-01.xml"));

        execContext.getContentDeliveryRuntime().addExecutionEventListener(eventListener);
        smooks.filterSource(execContext, source, new StreamResult(new NullWriter()));
    }


}
