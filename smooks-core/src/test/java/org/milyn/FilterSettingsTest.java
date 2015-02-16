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
package org.milyn;

import java.io.IOException;

import org.milyn.cdr.ParameterAccessor;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.Filter;
import org.xml.sax.SAXException;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class FilterSettingsTest {
	@Test
	public void test_01() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("filterSettings-01.xml"));		
		ExecutionContext execContext = smooks.createExecutionContext();
		
		assertEquals("DOM", ParameterAccessor.getStringParameter(Filter.STREAM_FILTER_TYPE, execContext.getDeliveryConfig()));
		assertEquals(null, ParameterAccessor.getStringParameter(Filter.CLOSE_RESULT, execContext.getDeliveryConfig()));
		assertEquals(null, ParameterAccessor.getStringParameter(Filter.CLOSE_SOURCE, execContext.getDeliveryConfig()));
		assertEquals(null, ParameterAccessor.getStringParameter(Filter.DEFAULT_SERIALIZATION_ON, execContext.getDeliveryConfig()));
		assertEquals(null, ParameterAccessor.getStringParameter(Filter.READER_POOL_SIZE, execContext.getDeliveryConfig()));
		assertEquals(null, ParameterAccessor.getStringParameter(Filter.ENTITIES_REWRITE, execContext.getDeliveryConfig()));
		assertEquals(null, ParameterAccessor.getStringParameter(Filter.TERMINATE_ON_VISITOR_EXCEPTION, execContext.getDeliveryConfig()));
	}

	@Test
	public void test_02() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("filterSettings-02.xml"));		
		ExecutionContext execContext = smooks.createExecutionContext();
		
		assertEquals("SAX", ParameterAccessor.getStringParameter(Filter.STREAM_FILTER_TYPE, execContext.getDeliveryConfig()));
		assertEquals("true", ParameterAccessor.getStringParameter(Filter.CLOSE_RESULT, execContext.getDeliveryConfig()));
		assertEquals("true", ParameterAccessor.getStringParameter(Filter.CLOSE_SOURCE, execContext.getDeliveryConfig()));
		assertEquals("true", ParameterAccessor.getStringParameter(Filter.DEFAULT_SERIALIZATION_ON, execContext.getDeliveryConfig()));
		assertEquals("3", ParameterAccessor.getStringParameter(Filter.READER_POOL_SIZE, execContext.getDeliveryConfig()));
		assertEquals("true", ParameterAccessor.getStringParameter(Filter.ENTITIES_REWRITE, execContext.getDeliveryConfig()));
		assertEquals("true", ParameterAccessor.getStringParameter(Filter.TERMINATE_ON_VISITOR_EXCEPTION, execContext.getDeliveryConfig()));
	}
}
