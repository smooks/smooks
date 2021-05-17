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
package org.smooks.engine.resource.config;

import org.junit.Test;
import org.smooks.api.SmooksConfigException;
import org.smooks.api.resource.config.Parameter;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.smooks.support.DomUtil;
import org.smooks.support.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

import static org.junit.Assert.*;

public class DefaultResourceConfigTestCase {

	@Test
	public void test_getParameter() {
		ResourceConfig resourceConfig = new DefaultResourceConfig("body", "device", "xxx");

		resourceConfig.setParameter("x", "val x");
		assertEquals("Expected x to be 'val x'", "val x", resourceConfig.getParameter("x", String.class).getValue());
		resourceConfig.setParameter("y", "val y 1");
		resourceConfig.setParameter("y", "val y 2");
		assertEquals("Expected y to be 'val y 1'", "val y 1", resourceConfig.getParameter("y", String.class).getValue());

        List<Parameter<?>> yParams = resourceConfig.getParameters("y");
		assertEquals("val y 1", yParams.get(0).getValue());
		assertEquals("val y 2", yParams.get(1).getValue());

        List<?> allParams = resourceConfig.getParameterValues();
		assertEquals(2, allParams.size());
		assertEquals("x", ((Parameter)allParams.get(0)).getName());
		assertEquals(yParams, allParams.get(1));
	}

	@Test
	public void test_getBoolParameter() {
        ResourceConfig resourceConfig = new DefaultResourceConfig("body", "device", "xxx");
		resourceConfig.setParameter("x", true);

		assertTrue("Expected x to be true", resourceConfig.getParameterValue("x", Boolean.class, false));
		assertFalse("Expected y to be false", resourceConfig.getParameterValue("y", Boolean.class, false));
	}

	@Test
	public void test_getStringParameter() {
        ResourceConfig resourceConfig = new DefaultResourceConfig("body", "device", "xxx");
		resourceConfig.setParameter("x", "xxxx");

		assertEquals("Expected x to be xxxx", "xxxx", resourceConfig.getParameterValue("x", String.class, "yyyy"));
		assertEquals("Expected y to be yyyy", "yyyy", resourceConfig.getParameterValue("y", String.class, "yyyy"));
	}

	@Test
    public void test_isTargetedAtElement_DOM() {
        Document doc = DomUtil.parse("<a><b><c><d><e/></d></c></b></a>");
        Element e = (Element) XmlUtil.getNode(doc, "a/b/c/d/e");

        ResourceConfig rc1 = new DefaultResourceConfig("e", "blah");
        ResourceConfig rc2 = new DefaultResourceConfig("d/e", "blah");
        ResourceConfig rc3 = new DefaultResourceConfig("a/b/c/d/e", "blah");
        ResourceConfig rc4 = new DefaultResourceConfig("xx/a/b/c/d/e", "blah");
        ResourceConfig rc5 = new DefaultResourceConfig("xx/b/c/d/e", "blah");
        ResourceConfig rc7 = new DefaultResourceConfig("/c/d/e", "blah");

        NodeFragment nodeFragment = new NodeFragment(e);
        
        assertTrue(nodeFragment.isMatch(rc1.getSelectorPath(), null));
        assertTrue(nodeFragment.isMatch(rc2.getSelectorPath(), null));
        assertTrue(nodeFragment.isMatch(rc3.getSelectorPath(), null));
        assertFalse(nodeFragment.isMatch(rc4.getSelectorPath(), null));
        assertFalse(nodeFragment.isMatch(rc5.getSelectorPath(), null));
        assertFalse(nodeFragment.isMatch(rc7.getSelectorPath(), null));
    }

	@Test
    public void test_isTargetedAtElement_DOM_with_Attribute() {
        Document doc = DomUtil.parse("<a><b><c><d><e/></d></c></b></a>");
        Element e = (Element) XmlUtil.getNode(doc, "a/b/c/d/e");

        // Check with an attribute on the selector....
        ResourceConfig rc8 = new DefaultResourceConfig("e/@attrib1", "blah");
        ResourceConfig rc9 = new DefaultResourceConfig("a/b/c/d/e/@attrib1", "blah");
        ResourceConfig rc10 = new DefaultResourceConfig("/c/d/e/@attrib1", "blah");

        assertEquals("e", rc8.getSelectorPath().getTargetElement());
        assertEquals("attrib1", rc8.getSelectorPath().getTargetAttribute());

        NodeFragment nodeFragment = new NodeFragment(e);

        assertTrue(nodeFragment.isMatch(rc8.getSelectorPath(), null));

        assertEquals("e", rc9.getSelectorPath().getTargetElement());
        assertEquals("attrib1", rc9.getSelectorPath().getTargetAttribute());
        assertTrue(nodeFragment.isMatch(rc9.getSelectorPath(), null));
        
        assertEquals("e", rc10.getSelectorPath().getTargetElement());
        assertEquals("attrib1", rc10.getSelectorPath().getTargetAttribute());
        assertFalse(nodeFragment.isMatch(rc10.getSelectorPath(), null));
    }

	@Test
    public void test_isTargetedAtElement_DOM_wildcards() {
        Document doc = DomUtil.parse("<a><b><c><d><e/></d></c></b></a>");
        Element e = (Element) XmlUtil.getNode(doc, "a/b/c/d/e");

        ResourceConfig rc1 = new DefaultResourceConfig("e", "blah");
        ResourceConfig rc2 = new DefaultResourceConfig("d/e", "blah");
        ResourceConfig rc3 = new DefaultResourceConfig("a/b/*/d/e", "blah");
        ResourceConfig rc4 = new DefaultResourceConfig("xx/a/b/*/d/e", "blah");
        ResourceConfig rc5 = new DefaultResourceConfig("xx/b/*/d/e", "blah");
        ResourceConfig rc6 = new DefaultResourceConfig("a/*/c/*/e", "blah");
        ResourceConfig rc7 = new DefaultResourceConfig("a/b/**/e", "blah");
        ResourceConfig rc8 = new DefaultResourceConfig("a/**/**/e", "blah");
        ResourceConfig rc9 = new DefaultResourceConfig("a/b/*/**/e", "blah");
        ResourceConfig rc10 = new DefaultResourceConfig("a/**/c/**/e", "blah");
        ResourceConfig rc11 = new DefaultResourceConfig("**/c/**/e", "blah");
        ResourceConfig rc12 = new DefaultResourceConfig("**/**/e", "blah");
        ResourceConfig rc13 = new DefaultResourceConfig("**/e", "blah");
        ResourceConfig rc14 = new DefaultResourceConfig("a/**/e", "blah");
        ResourceConfig rc15 = new DefaultResourceConfig("a/b/**", "blah");
        ResourceConfig rc16 = new DefaultResourceConfig("a/b/**/*", "blah");
        ResourceConfig rc17 = new DefaultResourceConfig("h/**", "blah");
        ResourceConfig rc18 = new DefaultResourceConfig("h/**/e", "blah");
        ResourceConfig rc19 = new DefaultResourceConfig("a/h/**/e", "blah");
        ResourceConfig rc20 = new DefaultResourceConfig("/a/**/e", "blah");
        ResourceConfig rc21 = new DefaultResourceConfig("/**/e", "blah");
        ResourceConfig rc22 = new DefaultResourceConfig("*/e", "blah");
        ResourceConfig rc23 = new DefaultResourceConfig("/*/e", "blah");

        NodeFragment nodeFragment = new NodeFragment(e);
        
        assertTrue(nodeFragment.isMatch(rc1.getSelectorPath(), null));
        assertTrue(nodeFragment.isMatch(rc2.getSelectorPath(), null));
        assertTrue(nodeFragment.isMatch(rc3.getSelectorPath(), null));

        assertFalse(nodeFragment.isMatch(rc4.getSelectorPath(), null));
        assertFalse(nodeFragment.isMatch(rc5.getSelectorPath(), null));

        assertTrue(nodeFragment.isMatch(rc6.getSelectorPath(), null));
        assertTrue(nodeFragment.isMatch(rc7.getSelectorPath(), null));
        assertTrue(nodeFragment.isMatch(rc8.getSelectorPath(), null));
        assertTrue(nodeFragment.isMatch(rc9.getSelectorPath(), null));
        assertTrue(nodeFragment.isMatch(rc10.getSelectorPath(), null));
        assertTrue(nodeFragment.isMatch(rc11.getSelectorPath(), null));
        assertTrue(nodeFragment.isMatch(rc12.getSelectorPath(), null));
        assertTrue(nodeFragment.isMatch(rc13.getSelectorPath(), null));
        assertTrue(nodeFragment.isMatch(rc14.getSelectorPath(), null));
        assertTrue(nodeFragment.isMatch(rc15.getSelectorPath(), null));
        assertTrue(nodeFragment.isMatch(rc16.getSelectorPath(), null));

        assertFalse(nodeFragment.isMatch(rc17.getSelectorPath(), null));
        assertFalse(nodeFragment.isMatch(rc18.getSelectorPath(), null));
        assertFalse(nodeFragment.isMatch(rc19.getSelectorPath(), null));

        assertTrue(nodeFragment.isMatch(rc20.getSelectorPath(), null));
        assertTrue(nodeFragment.isMatch(rc21.getSelectorPath(), null));
        assertTrue(nodeFragment.isMatch(rc22.getSelectorPath(), null));

        assertFalse(nodeFragment.isMatch(rc23.getSelectorPath(), null));
    }

	@Test
    public void test_isTargetedAtElement_DOM_rooted() {
        Document doc = DomUtil.parse("<a><b><c><a><d><e/></d></a></c></b></a>");
        Element e = (Element) XmlUtil.getNode(doc, "a/b/c/a/d/e");

        ResourceConfig rc1 = new DefaultResourceConfig("/a/b/c/a/d/e", "blah");
        ResourceConfig rc2 = new DefaultResourceConfig("/a/d/e", "blah");
        ResourceConfig rc3 = new DefaultResourceConfig("/**/d/e", "blah");
        ResourceConfig rc4 = new DefaultResourceConfig("/a/b/**/d/e", "blah");
        ResourceConfig rc5 = new DefaultResourceConfig("/a/b/*/d/e", "blah");

        try {
            new DefaultResourceConfig("xx/#document/a/b/c/a/d/e", "blah");
            fail("Expected SmooksConfigurationException.");
        } catch (SmooksConfigException ex) {
            assertEquals("Invalid selector 'xx/#document/a/b/c/a/d/e'.  '#document' token can only exist at the start of the selector.", ex.getMessage());
        }

        NodeFragment nodeFragment = new NodeFragment(e);

        assertTrue(nodeFragment.isMatch(rc1.getSelectorPath(), null));
        assertFalse(nodeFragment.isMatch(rc2.getSelectorPath(), null));
        assertTrue(nodeFragment.isMatch(rc3.getSelectorPath(), null));
        assertTrue(nodeFragment.isMatch(rc4.getSelectorPath(), null));
        assertFalse(nodeFragment.isMatch(rc5.getSelectorPath(), null));
    }

	@Test
    public void test_attributeSelector() {
        // Test that the attribute part of the selector doesn't get lowercased...
        ResourceConfig resource = new DefaultResourceConfig("a/b/@myAttribute");
        assertEquals("a/b{@myAttribute}", resource.getSelectorPath().toString());
    }
}