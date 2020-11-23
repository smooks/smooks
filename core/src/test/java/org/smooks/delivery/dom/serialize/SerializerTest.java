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
package org.smooks.delivery.dom.serialize;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.cdr.ResourceConfig;
import org.smooks.container.MockExecutionContext;
import org.smooks.delivery.dom.MockContentDeliveryConfig;
import org.smooks.injector.Scope;
import org.smooks.lifecycle.LifecycleManager;
import org.smooks.lifecycle.phase.PostConstructLifecyclePhase;
import org.smooks.registry.lookup.LifecycleManagerLookup;
import org.smooks.util.CharUtils;
import org.smooks.xml.XmlUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author tfennelly
 */
@SuppressWarnings("unchecked")
public class SerializerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(SerializerTest.class);

	@Test
	public void testSerialize() throws IOException, SAXException {
		MockExecutionContext executionContext = new MockExecutionContext();
		LifecycleManager lifecycleManager = executionContext.getApplicationContext().getRegistry().lookup(new LifecycleManagerLookup());
		// Target a resource at the "document fragment" i.e. the root..

        // Don't write xxx but write its child elements
		ResourceConfig configuration = new ResourceConfig(ResourceConfig.DOCUMENT_FRAGMENT_SELECTOR, "deviceX", "....");
		AddAttributeSerializer addAttributeSerializer = new AddAttributeSerializer();
		lifecycleManager.applyPhase(addAttributeSerializer, new PostConstructLifecyclePhase(new Scope(executionContext.getApplicationContext().getRegistry(), configuration, addAttributeSerializer)));
		((MockContentDeliveryConfig)executionContext.deliveryConfig).getSerializationVisitors().addBinding(ResourceConfig.DOCUMENT_FRAGMENT_SELECTOR, configuration, addAttributeSerializer);

        // Don't write xxx but write its child elements
		configuration = new ResourceConfig("xxx", "deviceX", "....");
		RemoveTestSerializaterVisitor removeTestSerializationUnit = new RemoveTestSerializaterVisitor();
		lifecycleManager.applyPhase(removeTestSerializationUnit, new PostConstructLifecyclePhase(new Scope(executionContext.getApplicationContext().getRegistry(), configuration, removeTestSerializationUnit)));

		((MockContentDeliveryConfig)executionContext.deliveryConfig).getSerializationVisitors().addBinding("xxx", configuration, removeTestSerializationUnit);

		// write yyyy as a badly-formed empty element
		configuration = new ResourceConfig("yyyy", "deviceX", "....");
		configuration.setParameter("wellformed", "false");
		EmptyElTestSerializerVisitor emptyElTestSerializationUnit = new EmptyElTestSerializerVisitor();
		lifecycleManager.applyPhase(emptyElTestSerializationUnit, new PostConstructLifecyclePhase(new Scope(executionContext.getApplicationContext().getRegistry(), configuration, emptyElTestSerializationUnit)));
		
		((MockContentDeliveryConfig)executionContext.deliveryConfig).getSerializationVisitors().addBinding("yyyy", configuration, emptyElTestSerializationUnit);

		/// write zzz as a well-formed empty element
		configuration = new ResourceConfig("zzz", "deviceX", "....");
		EmptyElTestSerializerVisitor otherEmptyElTestSerializationUnit = new EmptyElTestSerializerVisitor();
		lifecycleManager.applyPhase(otherEmptyElTestSerializationUnit, new PostConstructLifecyclePhase(new Scope(executionContext.getApplicationContext().getRegistry(), configuration, otherEmptyElTestSerializationUnit)));

		((MockContentDeliveryConfig) executionContext.deliveryConfig).getSerializationVisitors().addBinding("zzz", configuration, otherEmptyElTestSerializationUnit);

		Document doc = XmlUtil.parseStream(getClass().getResourceAsStream("testmarkup.xxml"), XmlUtil.VALIDATION_TYPE.NONE, true);
		Serializer serializer = new Serializer(doc, executionContext);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(output);

		serializer.serialize(writer);
		writer.flush();
		byte[] actualBytes = output.toByteArray();
		LOGGER.debug(new String(actualBytes));
		boolean areEqual = CharUtils.compareCharStreams(getClass().getResourceAsStream("testmarkup.xxml.ser_1"), new ByteArrayInputStream(actualBytes));
		assertTrue("Unexpected Serialization result failure.", areEqual);
	}
}
