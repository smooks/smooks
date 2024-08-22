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
import org.smooks.FilterSettings;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.parsers.SAXParser;
import org.smooks.engine.resource.config.GenericReaderConfigurator;
import org.smooks.Smooks;
import org.smooks.api.ExecutionContext;
import org.smooks.io.source.StringSource;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class AbstractParserTestCase {

    @Test
    public void test() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-AbstractParserTest.xml"));
        ExecutionContext execContext = smooks.createExecutionContext();

        TestParser parser = new TestParser(execContext);
        TestXMLReader reader = (TestXMLReader) parser.createXMLReader();

        parser.configureReader(reader, new DefaultHandler2(), execContext, null);
        assertEquals(8, reader.features.size());
        assertTrue(reader.features.get("feature-on-1"));
        assertTrue(reader.features.get("feature-on-2"));
		assertFalse(reader.features.get("feature-off-1"));
		assertFalse(reader.features.get("feature-off-2"));

        assertNotNull(reader.dtdHandler);
        assertNotNull(reader.entityResolver);
        assertNotNull(reader.errorHandler);
    }
   
    @Test 
    public void test_readerPool_Pooled() {
    	Smooks smooks = new Smooks();
    	
    	smooks.setReaderConfig(new GenericReaderConfigurator(PooledSAXParser.class));
    	smooks.setFilterSettings(FilterSettings.newSaxNgSettings().setReaderPoolSize(1));
    	
    	PooledSAXParser.numSetHandlerCalls = 0;
    	smooks.filterSource(new StringSource("<x/>"));
    	smooks.filterSource(new StringSource("<x/>"));
    	smooks.filterSource(new StringSource("<x/>"));    	
    	assertEquals(3, PooledSAXParser.numSetHandlerCalls);
    }
   
    @Test 
    public void test_readerPool_Unpooled() {
    	Smooks smooks = new Smooks();
    	
    	smooks.setReaderConfig(new GenericReaderConfigurator(UnpooledSAXParser.class));
    	smooks.setFilterSettings(FilterSettings.newSaxNgSettings().setReaderPoolSize(0));
    	
    	UnpooledSAXParser.numSetHandlerCalls = 0;
    	smooks.filterSource(new StringSource("<x/>"));
    	smooks.filterSource(new StringSource("<x/>"));
    	smooks.filterSource(new StringSource("<x/>"));    	
    	assertEquals(3, UnpooledSAXParser.numSetHandlerCalls);
    }

    private static class TestParser extends AbstractParser {
        public TestParser(ExecutionContext execContext) {
            super(execContext);
        }
    }
    
    public static class PooledSAXParser extends SAXParser {
    	
    	public static int numSetHandlerCalls;
    	public static PooledSAXParser lastParserInstance;
    	public static ContentHandler lastHandlerInstance;

		@Override
		public void setContentHandler(ContentHandler handler) {
			if(lastParserInstance == null) {
				lastParserInstance = this;
			}
			if(this != lastParserInstance) {
				fail("Should only be 1 parser instance (pooled).");
			}
			if(handler == lastHandlerInstance) {
				fail("Shouldn't be just 1 handler instance (pooled or unpooled).");
			}
			numSetHandlerCalls++;
			lastParserInstance = this;
			lastHandlerInstance = handler;
			super.setContentHandler(handler);
		}    	
    }
    
    public static class UnpooledSAXParser extends SAXParser {
    	
    	public static int numSetHandlerCalls;
    	public static UnpooledSAXParser lastParserInstance;
    	public static ContentHandler lastHandlerInstance;

		@Override
		public void setContentHandler(ContentHandler handler) {
			if(this == lastParserInstance) {
				fail("Shouldn't be just 1 parser instance (unpooled).");
			}
			if(handler == lastHandlerInstance) {
				fail("Shouldn't be just 1 handler instance (pooled or unpooled).");
			}
			numSetHandlerCalls++;
			lastParserInstance = this;
			lastHandlerInstance = handler;
			super.setContentHandler(handler);
		}    	
    }
}
