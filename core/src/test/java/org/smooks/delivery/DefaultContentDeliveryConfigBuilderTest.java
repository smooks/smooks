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
import org.smooks.SmooksException;
import org.smooks.StreamFilterType;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.dom.DOMContentDeliveryConfig;
import org.smooks.delivery.sax.SAXContentDeliveryConfig;
import org.smooks.delivery.sax.SAXVisitor01;
import org.smooks.io.StreamUtils;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DefaultContentDeliveryConfigBuilderTest {

	@Test
    public void test_sax() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-sax.xml"));
        ExecutionContext execContext = smooks.createExecutionContext();

        assertTrue(execContext.getDeliveryConfig() instanceof SAXContentDeliveryConfig);
        SAXContentDeliveryConfig config = (SAXContentDeliveryConfig) execContext.getDeliveryConfig();

        // Should be 5: 4 configured + 2 auto-installed
        assertEquals(7, config.getVisitBefores().getCount());
        assertTrue(config.getVisitBefores().getMappings("b").get(0).getContentHandler() instanceof SAXVisitor01);
        assertTrue(config.getVisitBefores().getMappings("b").get(0).getContentHandler() instanceof SAXVisitor01);
        assertEquals(6, config.getVisitAfters().getCount());
        assertTrue(config.getVisitAfters().getMappings("b").get(0).getContentHandler() instanceof SAXVisitor01);
        assertTrue(config.getVisitAfters().getMappings("b").get(0).getContentHandler() instanceof SAXVisitor01);
    }

	@Test
    public void test_dom() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-dom.xml"));
        ExecutionContext execContext = smooks.createExecutionContext();

        assertTrue(execContext.getDeliveryConfig() instanceof DOMContentDeliveryConfig);
        DOMContentDeliveryConfig config = (DOMContentDeliveryConfig) execContext.getDeliveryConfig();

        assertEquals(1, config.getAssemblyVisitBefores().getCount());
        assertEquals(1, config.getAssemblyVisitAfters().getCount());
        assertEquals(2, config.getProcessingVisitBefores().getCount());
        assertEquals(2, config.getProcessingVisitAfters().getCount());
        assertEquals(4, config.getSerializationVisitors().getCount());
    }

	@Test
    public void test_dom_sax_1() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-dom-sax-1.xml"));
        ExecutionContext execContext = smooks.createExecutionContext();

        // Should default to SAX
        assertTrue(execContext.getDeliveryConfig() instanceof SAXContentDeliveryConfig);
    }

	@Test
    public void test_dom_sax_2() throws IOException, SAXException {
        Smooks smooks;
        ExecutionContext execContext;

        smooks = new Smooks(getClass().getResourceAsStream("smooks-config-dom-sax-2.1.xml"));
        execContext = smooks.createExecutionContext();
        assertTrue(execContext.getDeliveryConfig() instanceof SAXContentDeliveryConfig);

        smooks = new Smooks(getClass().getResourceAsStream("smooks-config-dom-sax-2.2.xml"));
        execContext = smooks.createExecutionContext();
        assertTrue(execContext.getDeliveryConfig() instanceof DOMContentDeliveryConfig);

        smooks = new Smooks(getClass().getResourceAsStream("smooks-config-dom-sax-2.3.xml"));
        try {
            smooks.createExecutionContext();
            fail("Expected SmooksException");
        } catch(SmooksException e) {
            assertEquals("Invalid 'stream.filter.type' configuration parameter value of 'xxxx'.  Must be 'SAX' or 'DOM'.", e.getMessage());
        }
    }

	@Test
    public void test_dom_sax_3() throws IOException, SAXException {
        String origDefault = System.setProperty(Filter.STREAM_FILTER_TYPE, StreamFilterType.DOM.toString());

        try {
            Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-dom-sax-1.xml"));
            ExecutionContext execContext = smooks.createExecutionContext();

            // Should default to DOM
            assertTrue(execContext.getDeliveryConfig() instanceof DOMContentDeliveryConfig);
        } finally {
            if(origDefault != null) {
                System.setProperty(Filter.STREAM_FILTER_TYPE, origDefault);
            } else {
                System.getProperties().remove(Filter.STREAM_FILTER_TYPE);
            }
        }
    }

	@Test
    public void test_invalid() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-invalid.xml"));

        try {
            smooks.createExecutionContext();
            fail("Expected SmooksException");
        } catch(SmooksException e) {
            String expected = StreamUtils.trimLines(getClass().getResourceAsStream("smooks-config-invalid-error.txt")).toString();
            String actual = StreamUtils.trimLines(new StringReader(e.getMessage())).toString();

            assertEquals(expected.toLowerCase(), actual.toLowerCase());
        }
    }
}
