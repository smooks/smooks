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
package org.smooks;

import org.junit.jupiter.api.Test;
import org.smooks.api.ExecutionContext;
import org.smooks.api.delivery.Filter;
import org.smooks.engine.resource.config.ParameterAccessor;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class FilterSettingsTestCase {
	@Test
	public void test_01() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("filterSettings-01.xml"));		
		ExecutionContext executionContext = smooks.createExecutionContext();
		
		assertEquals("DOM", ParameterAccessor.getParameterValue(Filter.STREAM_FILTER_TYPE, String.class, executionContext.getContentDeliveryRuntime().getContentDeliveryConfig()));
        assertNull(ParameterAccessor.getParameterValue(Filter.CLOSE_SINK, String.class, executionContext.getContentDeliveryRuntime().getContentDeliveryConfig()));
        assertNull(ParameterAccessor.getParameterValue(Filter.CLOSE_SOURCE, String.class, executionContext.getContentDeliveryRuntime().getContentDeliveryConfig()));
        assertNull(ParameterAccessor.getParameterValue(Filter.DEFAULT_SERIALIZATION_ON, Boolean.class, executionContext.getContentDeliveryRuntime().getContentDeliveryConfig()));
        assertNull(ParameterAccessor.getParameterValue(Filter.READER_POOL_SIZE, Integer.class, executionContext.getContentDeliveryRuntime().getContentDeliveryConfig()));
        assertNull(ParameterAccessor.getParameterValue(Filter.ENTITIES_REWRITE, String.class, executionContext.getContentDeliveryRuntime().getContentDeliveryConfig()));
        assertNull(ParameterAccessor.getParameterValue(Filter.TERMINATE_ON_VISITOR_EXCEPTION, Boolean.class, executionContext.getContentDeliveryRuntime().getContentDeliveryConfig()));
	}

	@Test
	public void test_02() throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("filterSettings-02.xml"));		
		ExecutionContext executionContext = smooks.createExecutionContext();
		
		assertEquals("SAX NG", ParameterAccessor.getParameterValue(Filter.STREAM_FILTER_TYPE, String.class, executionContext.getContentDeliveryRuntime().getContentDeliveryConfig()));
		assertEquals("true", ParameterAccessor.getParameterValue(Filter.CLOSE_SINK, String.class, executionContext.getContentDeliveryRuntime().getContentDeliveryConfig()));
		assertEquals("true", ParameterAccessor.getParameterValue(Filter.CLOSE_SOURCE, String.class, executionContext.getContentDeliveryRuntime().getContentDeliveryConfig()));
		assertEquals("true", ParameterAccessor.getParameterValue(Filter.DEFAULT_SERIALIZATION_ON, String.class, executionContext.getContentDeliveryRuntime().getContentDeliveryConfig()));
		assertEquals("3", ParameterAccessor.getParameterValue(Filter.READER_POOL_SIZE, String.class, executionContext.getContentDeliveryRuntime().getContentDeliveryConfig()));
		assertEquals("true", ParameterAccessor.getParameterValue(Filter.ENTITIES_REWRITE, String.class, executionContext.getContentDeliveryRuntime().getContentDeliveryConfig()));
		assertEquals("true", ParameterAccessor.getParameterValue(Filter.TERMINATE_ON_VISITOR_EXCEPTION, String.class, executionContext.getContentDeliveryRuntime().getContentDeliveryConfig()));
	}
}
