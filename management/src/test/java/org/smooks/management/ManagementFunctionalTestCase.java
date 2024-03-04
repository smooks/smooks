/*-
 * ========================LICENSE_START=================================
 * Management
 * %%
 * Copyright (C) 2020 - 2024 Smooks
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
package org.smooks.management;

import org.junit.jupiter.api.Test;
import org.smooks.Smooks;
import org.smooks.api.Registry;
import org.smooks.api.management.InstrumentationAgent;
import org.smooks.api.management.InstrumentationResource;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.engine.lookup.InstanceLookup;
import org.smooks.io.payload.StringSource;
import org.xml.sax.SAXException;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ManagementFunctionalTestCase {
    @Test
    public void test() throws IOException, SAXException, MalformedObjectNameException, ReflectionException, AttributeNotFoundException, InstanceNotFoundException, IntrospectionException, MBeanException, InterruptedException {
        Smooks smooks = new Smooks("smooks-config.xml");
        smooks.filterSource(new StringSource("<a><b>bar</b></a>"));

        Visitor fooVisitor = smooks.getApplicationContext().getRegistry().lookup("Foo");
        Map<String, Object> fooVisitorAttributes = getAttributes(new ObjectName("org.smooks:context=\"/a\",type=visitor,name=Foo@" + Integer.toHexString(fooVisitor.hashCode())), smooks.getApplicationContext().getRegistry());
        assertEquals(7, fooVisitorAttributes.size());
        assertEquals("/a", fooVisitorAttributes.get("Selector"));
        assertEquals(1L, fooVisitorAttributes.get("VisitBeforeCount"));
        assertEquals(0L, fooVisitorAttributes.get("VisitAfterCount"));
        assertEquals(0L, fooVisitorAttributes.get("VisitChildElementCount"));
        assertEquals(0L, fooVisitorAttributes.get("FailedVisitCount"));
        assertEquals(0L, fooVisitorAttributes.get("VisitChildTextCount"));

        Visitor quuzVisitor = smooks.getApplicationContext().getRegistry().lookup("Quuz");
        Map<String, Object> quuzVisitorAttributes = getAttributes(new ObjectName("org.smooks:context=\"/a/b\",type=visitor,name=Quuz@" + Integer.toHexString(quuzVisitor.hashCode())), smooks.getApplicationContext().getRegistry());
        assertEquals(7, quuzVisitorAttributes.size());
        assertEquals("/a/b", quuzVisitorAttributes.get("Selector"));
        assertEquals(0L, quuzVisitorAttributes.get("VisitBeforeCount"));
        assertEquals(1L, quuzVisitorAttributes.get("VisitAfterCount"));
        assertEquals(0L, quuzVisitorAttributes.get("VisitChildElementCount"));
        assertEquals(0L, quuzVisitorAttributes.get("FailedVisitCount"));
        assertEquals(0L, quuzVisitorAttributes.get("VisitChildTextCount"));

        Visitor barVisitor = smooks.getApplicationContext().getRegistry().lookup("Bar");
        Map<String, Object> barVisitorAttributes = getAttributes(new ObjectName("org.smooks:context=\"/a/b\",type=visitor,name=Bar@" + Integer.toHexString(barVisitor.hashCode())), smooks.getApplicationContext().getRegistry());
        assertEquals(7, barVisitorAttributes.size());
        assertEquals("/a/b", barVisitorAttributes.get("Selector"));
        assertEquals(1L, barVisitorAttributes.get("VisitBeforeCount"));
        assertEquals(1L, barVisitorAttributes.get("VisitAfterCount"));
        assertEquals(0L, barVisitorAttributes.get("VisitChildElementCount"));
        assertEquals(0L, barVisitorAttributes.get("FailedVisitCount"));
        assertEquals(1L, barVisitorAttributes.get("VisitChildTextCount"));
    }

    protected Map<String, Object> getAttributes(ObjectName objectName, Registry registry) throws ReflectionException, InstanceNotFoundException, IntrospectionException, AttributeNotFoundException, MBeanException {
        InstrumentationAgent instrumentationAgent = registry.lookup(InstrumentationResource.INSTRUMENTATION_RESOURCE_TYPED_KEY).getInstrumentationAgent();
        MBeanServer mBeanServer = instrumentationAgent.getMBeanServer();
        MBeanInfo mBeanInfo = mBeanServer.getMBeanInfo(objectName);

        MBeanAttributeInfo[] mBeanAttributeInfos = mBeanInfo.getAttributes();
        Map<String, Object> attributes = new HashMap<>();
        for (MBeanAttributeInfo mBeanAttributeInfo : mBeanAttributeInfos) {
            if (mBeanAttributeInfo.isReadable()) {
                attributes.put(mBeanAttributeInfo.getName(), mBeanServer.getAttribute(objectName, mBeanAttributeInfo.getName()));
            }
        }

        return attributes;
    }
}
