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
package org.milyn.delivery;

import static org.junit.Assert.*;
import org.junit.Test;

import org.apache.xerces.parsers.SAXParser;
import org.milyn.container.ExecutionContext;
import org.milyn.payload.StringSource;
import org.milyn.FilterSettings;
import org.milyn.GenericReaderConfigurator;
import org.milyn.Smooks;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class AbstractParserTest {

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
        assertTrue(!reader.features.get("feature-off-1"));
        assertTrue(!reader.features.get("feature-off-2"));

        assertNotNull(reader.dtdHandler);
        assertNotNull(reader.entityResolver);
        assertNotNull(reader.errorHandler);
    }
   
    @Test 
    public void test_readerPool_Pooled() {
    	Smooks smooks = new Smooks();
    	
    	smooks.setReaderConfig(new GenericReaderConfigurator(PooledSAXParser.class));
    	smooks.setFilterSettings(FilterSettings.newSAXSettings().setReaderPoolSize(1));
    	
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
    	smooks.setFilterSettings(FilterSettings.newSAXSettings().setReaderPoolSize(0));
    	
    	UnpooledSAXParser.numSetHandlerCalls = 0;
    	smooks.filterSource(new StringSource("<x/>"));
    	smooks.filterSource(new StringSource("<x/>"));
    	smooks.filterSource(new StringSource("<x/>"));    	
    	assertEquals(3, UnpooledSAXParser.numSetHandlerCalls);
    }

    private class TestParser extends AbstractParser {
        public TestParser(ExecutionContext execContext) {
            super(execContext);
        }
    }
    
    public static class PooledSAXParser extends SAXParser {
    	
    	public static int numSetHandlerCalls = 0;
    	public static PooledSAXParser lastParserInstance;
    	public static ContentHandler lastHandlerInstance;
    	
		public void setContentHandler(ContentHandler handler) {
			if(lastParserInstance == null) {
				lastParserInstance = this;
			}
			if(this != lastParserInstance) {
				fail("Should only be 1 parser instanse (pooled).");
			}
			if(handler == lastHandlerInstance) {
				fail("Shouldn't be just 1 handler instanse (pooled or unpooled).");
			}
			numSetHandlerCalls++;
			lastParserInstance = this;
			lastHandlerInstance = handler;
			super.setContentHandler(handler);
		}    	
    }
    
    public static class UnpooledSAXParser extends SAXParser {
    	
    	public static int numSetHandlerCalls = 0;
    	public static UnpooledSAXParser lastParserInstance;
    	public static ContentHandler lastHandlerInstance;
    	
		public void setContentHandler(ContentHandler handler) {
			if(this == lastParserInstance) {
				fail("Shouldn't be just 1 parser instanse (unpooled).");
			}
			if(handler == lastHandlerInstance) {
				fail("Shouldn't be just 1 handler instanse (pooled or unpooled).");
			}
			numSetHandlerCalls++;
			lastParserInstance = this;
			lastHandlerInstance = handler;
			super.setContentHandler(handler);
		}    	
    }
}
