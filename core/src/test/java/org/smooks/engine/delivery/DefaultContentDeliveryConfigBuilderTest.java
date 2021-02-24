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
package org.smooks.engine.delivery;

import org.junit.Test;
import org.smooks.Smooks;
import org.smooks.api.SmooksException;
import org.smooks.StreamFilterType;
import org.smooks.api.ExecutionContext;
import org.smooks.api.delivery.Filter;
import org.smooks.engine.delivery.dom.DOMContentDeliveryConfig;
import org.smooks.engine.delivery.sax.SAXContentDeliveryConfig;
import org.smooks.engine.delivery.sax.SAXVisitor01;
import org.smooks.io.StreamUtils;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DefaultContentDeliveryConfigBuilderTest {

	@Test
    public void test_sax() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-sax.xml"));
        ExecutionContext execContext = smooks.createExecutionContext();

        assertTrue(execContext.getContentDeliveryRuntime().getContentDeliveryConfig() instanceof SAXContentDeliveryConfig);
        SAXContentDeliveryConfig config = (SAXContentDeliveryConfig) execContext.getContentDeliveryRuntime().getContentDeliveryConfig();

        // Should be 5: 4 configured + 2 auto-installed
        assertEquals(8, config.getVisitBeforeSelectorTable().size());
        assertTrue(config.getVisitBeforeSelectorTable().get("b").get(0).getContentHandler() instanceof SAXVisitor01);
        assertTrue(config.getVisitBeforeSelectorTable().get("b").get(0).getContentHandler() instanceof SAXVisitor01);
        assertEquals(7, config.getVisitAfterSelectorTable().size());
        assertTrue(config.getVisitAfterSelectorTable().get("b").get(1).getContentHandler() instanceof SAXVisitor01);
        assertTrue(config.getVisitAfterSelectorTable().get("b").get(1).getContentHandler() instanceof SAXVisitor01);
    }

	@Test
    public void test_dom() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-dom.xml"));
        ExecutionContext execContext = smooks.createExecutionContext();

        assertTrue(execContext.getContentDeliveryRuntime().getContentDeliveryConfig() instanceof DOMContentDeliveryConfig);
        DOMContentDeliveryConfig config = (DOMContentDeliveryConfig) execContext.getContentDeliveryRuntime().getContentDeliveryConfig();

        assertEquals(1, config.getAssemblyVisitBeforeSelectorTable().values().stream().mapToLong(Collection::size).sum());
        assertEquals(1, config.getAssemblyVisitAfterSelectorTable().values().stream().mapToLong(Collection::size).sum());
        assertEquals(3, config.getProcessingVisitBeforeSelectorTable().values().stream().mapToLong(Collection::size).sum());
        assertEquals(3, config.getProcessingVisitAfterSelectorTable().values().stream().mapToLong(Collection::size).sum());
        assertEquals(4, config.getSerializerVisitorSelectorTable().values().stream().mapToLong(Collection::size).sum());
    }

	@Test
    public void test_dom_sax_1() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-dom-sax-1.xml"));
        ExecutionContext execContext = smooks.createExecutionContext();

        // Should default to SAX
        assertTrue(execContext.getContentDeliveryRuntime().getContentDeliveryConfig() instanceof SAXContentDeliveryConfig);
    }

	@Test
    public void test_dom_sax_2() throws IOException, SAXException {
        Smooks smooks;
        ExecutionContext execContext;

        smooks = new Smooks(getClass().getResourceAsStream("smooks-config-dom-sax-2.1.xml"));
        execContext = smooks.createExecutionContext();
        assertTrue(execContext.getContentDeliveryRuntime().getContentDeliveryConfig() instanceof SAXContentDeliveryConfig);

        smooks = new Smooks(getClass().getResourceAsStream("smooks-config-dom-sax-2.2.xml"));
        execContext = smooks.createExecutionContext();
        assertTrue(execContext.getContentDeliveryRuntime().getContentDeliveryConfig() instanceof DOMContentDeliveryConfig);

        smooks = new Smooks(getClass().getResourceAsStream("smooks-config-dom-sax-2.3.xml"));
        try {
            smooks.createExecutionContext();
            fail("Expected SmooksException");
        } catch(SmooksException e) {
            assertEquals("The configured Filter ('xxxx') cannot be used: [SAX, DOM] filters can be used for the given set of visitors. Turn on debug logging for more information.", e.getMessage());
        }
    }

	@Test
    public void test_dom_sax_3() throws IOException, SAXException {
        String origDefault = System.setProperty(Filter.STREAM_FILTER_TYPE, StreamFilterType.DOM.toString());

        try {
            Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-dom-sax-1.xml"));
            ExecutionContext execContext = smooks.createExecutionContext();

            // Should default to DOM
            assertTrue(execContext.getContentDeliveryRuntime().getContentDeliveryConfig() instanceof DOMContentDeliveryConfig);
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
