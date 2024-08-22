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
import org.smooks.engine.delivery.event.BasicExecutionEventListener;
import org.smooks.engine.delivery.event.ResourceTargetingExecutionEvent;
import org.smooks.io.NullWriter;
import org.smooks.io.sink.WriterSink;
import org.smooks.io.source.StreamSource;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@SuppressWarnings("unchecked")
public class ExecutionEventListenerTestCase {

	@Test
    public void test_01_dom() throws IOException, SAXException {
        BasicExecutionEventListener eventListener = new BasicExecutionEventListener();

        testListener(eventListener, "smooks-config-dom.xml");
        assertEquals(57, eventListener.getEvents().size());
    }

	@Test
    public void test_01_sax() throws IOException, SAXException {
        BasicExecutionEventListener eventListener = new BasicExecutionEventListener();

        testListener(eventListener, "smooks-config-sax.xml");
        assertEquals(54, eventListener.getEvents().size());
    }

	@Test
    public void test_03_dom() throws IOException, SAXException {
        BasicExecutionEventListener eventListener = new BasicExecutionEventListener();

        eventListener.setFilterEvents(ResourceTargetingExecutionEvent.class);
        testListener(eventListener, "smooks-config-dom.xml");
        assertEquals(37, eventListener.getEvents().size());
    }

    private void testListener(BasicExecutionEventListener eventListener, String config) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream(config));
        ExecutionContext execContext = smooks.createExecutionContext();
        StreamSource source = new StreamSource(getClass().getResourceAsStream("test-data-01.xml"));

        execContext.getContentDeliveryRuntime().addExecutionEventListener(eventListener);
        smooks.filterSource(execContext, source, new WriterSink(new NullWriter()));
    }


}
