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
package org.smooks.cdr;

import org.junit.Test;
import org.smooks.cdr.xpath.SelectorStepBuilder;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.util.DomUtil;
import org.smooks.xml.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.helpers.AttributesImpl;

import java.util.List;

import static org.junit.Assert.*;

public class SmooksResourceConfigurationTest {

	@Test
	public void test_getParameter() {
		SmooksResourceConfiguration resourceConfig = new SmooksResourceConfiguration("body", "device", "xxx");

		resourceConfig.setParameter("x", "val x");
		assertEquals("Expected x to be 'val x'", "val x", resourceConfig.getParameter("x", String.class).getValue());
		resourceConfig.setParameter("y", "val y 1");
		resourceConfig.setParameter("y", "val y 2");
		assertEquals("Expected y to be 'val y 1'", "val y 1", resourceConfig.getParameter("y", String.class).getValue());

		List yParams = resourceConfig.getParameters("y");
		assertEquals("val y 1", ((Parameter)yParams.get(0)).getValue());
		assertEquals("val y 2", ((Parameter)yParams.get(1)).getValue());

		List allParams = resourceConfig.getParameterList();
		assertEquals(2, allParams.size());
		assertEquals("x", ((Parameter)allParams.get(0)).getName());
		assertEquals(yParams, allParams.get(1));
	}

	@Test
	public void test_getBoolParameter() {
		SmooksResourceConfiguration resourceConfig = new SmooksResourceConfiguration("body", "device", "xxx");
		resourceConfig.setParameter("x", true);

		assertTrue("Expected x to be true", resourceConfig.getParameterValue("x", Boolean.class, false));
		assertFalse("Expected y to be false", resourceConfig.getParameterValue("y", Boolean.class, false));
	}

	@Test
	public void test_getStringParameter() {
		SmooksResourceConfiguration resourceConfig = new SmooksResourceConfiguration("body", "device", "xxx");
		resourceConfig.setParameter("x", "xxxx");

		assertEquals("Expected x to be xxxx", "xxxx", resourceConfig.getParameterValue("x", String.class, "yyyy"));
		assertEquals("Expected y to be yyyy", "yyyy", resourceConfig.getParameterValue("y", String.class, "yyyy"));
	}

	@Test
    public void test_isTargetedAtElement_DOM() {
        Document doc = DomUtil.parse("<a><b><c><d><e/></d></c></b></a>");
        Element e = (Element) XmlUtil.getNode(doc, "a/b/c/d/e");

        SmooksResourceConfiguration rc1 = new SmooksResourceConfiguration("e", "blah");
        SmooksResourceConfiguration rc2 = new SmooksResourceConfiguration("d/e", "blah");
        SmooksResourceConfiguration rc3 = new SmooksResourceConfiguration("a/b/c/d/e", "blah");
        SmooksResourceConfiguration rc4 = new SmooksResourceConfiguration("xx/a/b/c/d/e", "blah");
        SmooksResourceConfiguration rc5 = new SmooksResourceConfiguration("xx/b/c/d/e", "blah");
        SmooksResourceConfiguration rc7 = new SmooksResourceConfiguration("/c/d/e", "blah");

        assertTrue(rc1.isTargetedAtElement(e, null));
        assertTrue(rc2.isTargetedAtElement(e, null));
        assertTrue(rc3.isTargetedAtElement(e, null));
        assertFalse(rc4.isTargetedAtElement(e, null));
        assertFalse(rc5.isTargetedAtElement(e, null));
        assertFalse(rc7.isTargetedAtElement(e, null));
    }

	@Test
    public void test_isTargetedAtElement_DOM_with_Attribute() {
        Document doc = DomUtil.parse("<a><b><c><d><e/></d></c></b></a>");
        Element e = (Element) XmlUtil.getNode(doc, "a/b/c/d/e");

        // Check with an attribute on the selector....
        SmooksResourceConfiguration rc8 = new SmooksResourceConfiguration("e/@attrib1", "blah");
        SmooksResourceConfiguration rc9 = new SmooksResourceConfiguration("a/b/c/d/e/@attrib1", "blah");
        SmooksResourceConfiguration rc10 = new SmooksResourceConfiguration("/c/d/e/@attrib1", "blah");

        assertEquals("e", rc8.getTargetElement());
        assertEquals("attrib1", rc8.getTargetAttribute());
        assertTrue(rc8.isTargetedAtElement(e, null));

        assertEquals("e", rc9.getTargetElement());
        assertEquals("attrib1", rc9.getTargetAttribute());
        assertTrue(rc9.isTargetedAtElement(e, null));

        assertEquals("e", rc10.getTargetElement());
        assertEquals("attrib1", rc10.getTargetAttribute());
        assertFalse(rc10.isTargetedAtElement(e, null));
    }

	@Test
    public void test_isTargetedAtElement_DOM_wildcards() {
        Document doc = DomUtil.parse("<a><b><c><d><e/></d></c></b></a>");
        Element e = (Element) XmlUtil.getNode(doc, "a/b/c/d/e");

        SmooksResourceConfiguration rc1 = new SmooksResourceConfiguration("e", "blah");
        SmooksResourceConfiguration rc2 = new SmooksResourceConfiguration("d/e", "blah");
        SmooksResourceConfiguration rc3 = new SmooksResourceConfiguration("a/b/*/d/e", "blah");
        SmooksResourceConfiguration rc4 = new SmooksResourceConfiguration("xx/a/b/*/d/e", "blah");
        SmooksResourceConfiguration rc5 = new SmooksResourceConfiguration("xx/b/*/d/e", "blah");
        SmooksResourceConfiguration rc6 = new SmooksResourceConfiguration("a/*/c/*/e", "blah");
        SmooksResourceConfiguration rc7 = new SmooksResourceConfiguration("a/b/**/e", "blah");
        SmooksResourceConfiguration rc8 = new SmooksResourceConfiguration("a/**/**/e", "blah");
        SmooksResourceConfiguration rc9 = new SmooksResourceConfiguration("a/b/*/**/e", "blah");
        SmooksResourceConfiguration rc10 = new SmooksResourceConfiguration("a/**/c/**/e", "blah");
        SmooksResourceConfiguration rc11 = new SmooksResourceConfiguration("**/c/**/e", "blah");
        SmooksResourceConfiguration rc12 = new SmooksResourceConfiguration("**/**/e", "blah");
        SmooksResourceConfiguration rc13 = new SmooksResourceConfiguration("**/e", "blah");
        SmooksResourceConfiguration rc14 = new SmooksResourceConfiguration("a/**/e", "blah");
        SmooksResourceConfiguration rc15 = new SmooksResourceConfiguration("a/b/**", "blah");
        SmooksResourceConfiguration rc16 = new SmooksResourceConfiguration("a/b/**/*", "blah");
        SmooksResourceConfiguration rc17 = new SmooksResourceConfiguration("h/**", "blah");
        SmooksResourceConfiguration rc18 = new SmooksResourceConfiguration("h/**/e", "blah");
        SmooksResourceConfiguration rc19 = new SmooksResourceConfiguration("a/h/**/e", "blah");
        SmooksResourceConfiguration rc20 = new SmooksResourceConfiguration("/a/**/e", "blah");
        SmooksResourceConfiguration rc21 = new SmooksResourceConfiguration("/**/e", "blah");
        SmooksResourceConfiguration rc22 = new SmooksResourceConfiguration("*/e", "blah");
        SmooksResourceConfiguration rc23 = new SmooksResourceConfiguration("/*/e", "blah");

        assertTrue(rc1.isTargetedAtElement(e, null));
        assertTrue(rc2.isTargetedAtElement(e, null));
        assertTrue(rc3.isTargetedAtElement(e, null));

        assertFalse(rc4.isTargetedAtElement(e, null));
        assertFalse(rc5.isTargetedAtElement(e, null));

        assertTrue(rc6.isTargetedAtElement(e, null));
        assertTrue(rc7.isTargetedAtElement(e, null));
        assertTrue(rc8.isTargetedAtElement(e, null));
        assertTrue(rc9.isTargetedAtElement(e, null));
        assertTrue(rc10.isTargetedAtElement(e, null));
        assertTrue(rc11.isTargetedAtElement(e, null));
        assertTrue(rc12.isTargetedAtElement(e, null));
        assertTrue(rc13.isTargetedAtElement(e, null));
        assertTrue(rc14.isTargetedAtElement(e, null));
        assertTrue(rc15.isTargetedAtElement(e, null));
        assertTrue(rc16.isTargetedAtElement(e, null));

        assertFalse(rc17.isTargetedAtElement(e, null));
        assertFalse(rc18.isTargetedAtElement(e, null));
        assertFalse(rc19.isTargetedAtElement(e, null));

        assertTrue(rc20.isTargetedAtElement(e, null));
        assertTrue(rc21.isTargetedAtElement(e, null));
        assertTrue(rc22.isTargetedAtElement(e, null));

        assertFalse(rc23.isTargetedAtElement(e, null));
    }

	@Test
    public void test_isTargetedAtElement_DOM_rooted() {
        Document doc = DomUtil.parse("<a><b><c><a><d><e/></d></a></c></b></a>");
        Element e = (Element) XmlUtil.getNode(doc, "a/b/c/a/d/e");

        SmooksResourceConfiguration rc1 = new SmooksResourceConfiguration("/a/b/c/a/d/e", "blah");
        SmooksResourceConfiguration rc2 = new SmooksResourceConfiguration("/a/d/e", "blah");
        SmooksResourceConfiguration rc3 = new SmooksResourceConfiguration("/**/d/e", "blah");
        SmooksResourceConfiguration rc4 = new SmooksResourceConfiguration("/a/b/**/d/e", "blah");
        SmooksResourceConfiguration rc5 = new SmooksResourceConfiguration("/a/b/*/d/e", "blah");

        try {
            new SmooksResourceConfiguration("xx/#document/a/b/c/a/d/e", "blah");
            fail("Expected SmooksConfigurationException.");
        } catch (SmooksConfigurationException ex) {
            assertEquals("Invalid selector 'xx/#document/a/b/c/a/d/e'.  '#document' token can only exist at the start of the selector.", ex.getMessage());
        }

        assertTrue(rc1.isTargetedAtElement(e, null));
        assertFalse(rc2.isTargetedAtElement(e, null));
        assertTrue(rc3.isTargetedAtElement(e, null));
        assertTrue(rc4.isTargetedAtElement(e, null));
        assertFalse(rc5.isTargetedAtElement(e, null));
    }

	@Test
    public void test_isTargetedAtElement_SAX() {
        SAXElement e = buildE();

        SmooksResourceConfiguration rc1 = new SmooksResourceConfiguration("e", "blah");
        SmooksResourceConfiguration rc2 = new SmooksResourceConfiguration("d/e", "blah");
        SmooksResourceConfiguration rc3 = new SmooksResourceConfiguration("a/b/c/d/e", "blah");
        SmooksResourceConfiguration rc4 = new SmooksResourceConfiguration("xx/a/b/c/d/e", "blah");
        SmooksResourceConfiguration rc5 = new SmooksResourceConfiguration("xx/b/c/d/e", "blah");

        assertTrue(rc1.isTargetedAtElement(e, null));
        assertTrue(rc2.isTargetedAtElement(e, null));
        assertTrue(rc3.isTargetedAtElement(e, null));
        assertFalse(rc4.isTargetedAtElement(e, null));
        assertFalse(rc5.isTargetedAtElement(e, null));
    }

	@Test
    public void test_isTargetedAtElement_SAX_wildcards() {
        SAXElement e = buildE();

        SmooksResourceConfiguration rc1 = new SmooksResourceConfiguration("e", "blah");
        SmooksResourceConfiguration rc2 = new SmooksResourceConfiguration("d/e", "blah");
        SmooksResourceConfiguration rc3 = new SmooksResourceConfiguration("a/b/*/d/e", "blah");
        SmooksResourceConfiguration rc4 = new SmooksResourceConfiguration("xx/a/b/*/d/e", "blah");
        SmooksResourceConfiguration rc5 = new SmooksResourceConfiguration("xx/b/*/d/e", "blah");
        SmooksResourceConfiguration rc6 = new SmooksResourceConfiguration("a/*/c/*/e", "blah");
        SmooksResourceConfiguration rc7 = new SmooksResourceConfiguration("a/b/**/e", "blah");
        SmooksResourceConfiguration rc8 = new SmooksResourceConfiguration("a/**/**/e", "blah");
        SmooksResourceConfiguration rc9 = new SmooksResourceConfiguration("a/b/*/**/e", "blah");
        SmooksResourceConfiguration rc10 = new SmooksResourceConfiguration("a/**/c/**/e", "blah");
        SmooksResourceConfiguration rc11 = new SmooksResourceConfiguration("**/c/**/e", "blah");
        SmooksResourceConfiguration rc12 = new SmooksResourceConfiguration("**/**/e", "blah");
        SmooksResourceConfiguration rc13 = new SmooksResourceConfiguration("**/e", "blah");
        SmooksResourceConfiguration rc14 = new SmooksResourceConfiguration("a/**/e", "blah");
        SmooksResourceConfiguration rc15 = new SmooksResourceConfiguration("a/b/**", "blah");
        SmooksResourceConfiguration rc15_1 = new SmooksResourceConfiguration("b/**", "blah");
        SmooksResourceConfiguration rc16 = new SmooksResourceConfiguration("a/b/**/*", "blah");
        SmooksResourceConfiguration rc16_1 = new SmooksResourceConfiguration("b/**/*", "blah");
        SmooksResourceConfiguration rc17 = new SmooksResourceConfiguration("h/**", "blah");
        SmooksResourceConfiguration rc18 = new SmooksResourceConfiguration("h/**/e", "blah");
        SmooksResourceConfiguration rc19 = new SmooksResourceConfiguration("a/h/**/e", "blah");
        SmooksResourceConfiguration rc20 = new SmooksResourceConfiguration("/a/**/e", "blah");
        SmooksResourceConfiguration rc21 = new SmooksResourceConfiguration("/**/e", "blah");
        SmooksResourceConfiguration rc22 = new SmooksResourceConfiguration("*/e", "blah");
        SmooksResourceConfiguration rc23 = new SmooksResourceConfiguration("/*/e", "blah");

        assertTrue(rc1.isTargetedAtElement(e, null));
        assertTrue(rc2.isTargetedAtElement(e, null));
        assertTrue(rc3.isTargetedAtElement(e, null));

        assertFalse(rc4.isTargetedAtElement(e, null));
        assertFalse(rc5.isTargetedAtElement(e, null));

        assertTrue(rc6.isTargetedAtElement(e, null));
        assertTrue(rc7.isTargetedAtElement(e, null));
        assertTrue(rc8.isTargetedAtElement(e, null));
        assertTrue(rc9.isTargetedAtElement(e, null));
        assertTrue(rc10.isTargetedAtElement(e, null));
        assertTrue(rc11.isTargetedAtElement(e, null));
        assertTrue(rc12.isTargetedAtElement(e, null));
        assertTrue(rc13.isTargetedAtElement(e, null));
        assertTrue(rc14.isTargetedAtElement(e, null));
        assertTrue(rc15.isTargetedAtElement(e, null));
        assertTrue(rc16.isTargetedAtElement(e, null));
        assertTrue(rc15_1.isTargetedAtElement(e, null));
        assertTrue(rc16_1.isTargetedAtElement(e, null));

        assertFalse(rc17.isTargetedAtElement(e, null));
        assertFalse(rc18.isTargetedAtElement(e, null));
        assertFalse(rc19.isTargetedAtElement(e, null));

        assertTrue(rc20.isTargetedAtElement(e, null));
        assertTrue(rc21.isTargetedAtElement(e, null));
        assertTrue(rc22.isTargetedAtElement(e, null));

        assertFalse(rc23.isTargetedAtElement(e, null));
    }

	@Test
    public void test_attributeSelector() {
        // Test that the attribute part of the selector doesn't get lowercased...
        SmooksResourceConfiguration resource = new SmooksResourceConfiguration("a/b/@myAttribute");
        assertEquals("a/b{@myAttribute}", SelectorStepBuilder.toString(resource.getSelectorSteps()));
    }

	@Test
    public void test_isTargetedAtElement_SAX_rooted() {
        SAXElement e = buildE_rooted();

        SmooksResourceConfiguration rc1 = new SmooksResourceConfiguration("/a/b/c/a/d/e", "blah");
        SmooksResourceConfiguration rc2 = new SmooksResourceConfiguration("/a/d/e", "blah");
        SmooksResourceConfiguration rc3 = new SmooksResourceConfiguration("/**/d/e", "blah");
        SmooksResourceConfiguration rc4 = new SmooksResourceConfiguration("/a/b/**/d/e", "blah");
        SmooksResourceConfiguration rc5 = new SmooksResourceConfiguration("/a/b/*/d/e", "blah");

        assertTrue(rc1.isTargetedAtElement(e, null));
        assertFalse(rc2.isTargetedAtElement(e, null));
        assertTrue(rc3.isTargetedAtElement(e, null));
        assertTrue(rc4.isTargetedAtElement(e, null));
        assertFalse(rc5.isTargetedAtElement(e, null));
    }

	@Test
    public void test_isTargetedAtElement_SAX_with_Attribute() {
        SAXElement e = buildE_rooted();

        SmooksResourceConfiguration noAtt = new SmooksResourceConfiguration("e", "blah");
        assertNull(noAtt.getTargetAttribute());

        // Check with an attribute on the selector....
        SmooksResourceConfiguration rc8 = new SmooksResourceConfiguration("e/@attrib1", "blah");
        SmooksResourceConfiguration rc9 = new SmooksResourceConfiguration("a/b/c/a/d/e/@attrib1", "blah");
        SmooksResourceConfiguration rc10 = new SmooksResourceConfiguration("/a/d/e/@attrib1", "blah");

        assertEquals("e", rc8.getTargetElement());
        assertEquals("attrib1", rc8.getTargetAttribute());
        assertTrue(rc8.isTargetedAtElement(e, null));

        assertEquals("e", rc9.getTargetElement());
        assertEquals("attrib1", rc9.getTargetAttribute());
        assertTrue(rc9.isTargetedAtElement(e, null));

        assertEquals("e", rc10.getTargetElement());
        assertEquals("attrib1", rc10.getTargetAttribute());
        assertFalse(rc10.isTargetedAtElement(e, null));
    }

    private SAXElement buildE() {
        SAXElement element;

        element = new SAXElement(null, "a", null, new AttributesImpl(), null);
        element = new SAXElement(null, "b", null, new AttributesImpl(), element);
        element = new SAXElement(null, "c", null, new AttributesImpl(), element);
        element = new SAXElement(null, "d", null, new AttributesImpl(), element);
        element = new SAXElement(null, "e", null, new AttributesImpl(), element);

        return element;
    }

    private SAXElement buildE_rooted() {
        SAXElement element;

        element = new SAXElement(null, "a", null, new AttributesImpl(), null);
        element = new SAXElement(null, "b", null, new AttributesImpl(), element);
        element = new SAXElement(null, "c", null, new AttributesImpl(), element);
        element = new SAXElement(null, "a", null, new AttributesImpl(), element);
        element = new SAXElement(null, "d", null, new AttributesImpl(), element);
        element = new SAXElement(null, "e", null, new AttributesImpl(), element);

        return element;
    }
}
