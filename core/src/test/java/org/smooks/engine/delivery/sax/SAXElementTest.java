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
package org.smooks.engine.delivery.sax;

import org.junit.Test;
import org.smooks.api.SmooksException;
import org.smooks.api.delivery.sax.SAXElement;
import org.smooks.api.delivery.sax.TextType;
import org.xml.sax.helpers.AttributesImpl;

import static org.junit.Assert.*;


/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SAXElementTest {

	@Test
    public void test_create() {
        SAXElement saxElement;

        saxElement = new DefaultSAXElement("http://x", "a", "x", new AttributesImpl(), null);
        assertEquals("http://x", saxElement.getName().getNamespaceURI());
        assertEquals("", saxElement.getName().getPrefix());
        assertEquals("a", saxElement.getName().getLocalPart());

        saxElement = new DefaultSAXElement("http://x", "", "x", new AttributesImpl(), null);
        assertEquals("http://x", saxElement.getName().getNamespaceURI());
        assertEquals("", saxElement.getName().getPrefix());
        assertEquals("x", saxElement.getName().getLocalPart());

        saxElement = new DefaultSAXElement("http://x", "a", "x:a", new AttributesImpl(), null);
        assertEquals("http://x", saxElement.getName().getNamespaceURI());
        assertEquals("x", saxElement.getName().getPrefix());
        assertEquals("a", saxElement.getName().getLocalPart());

        saxElement = new DefaultSAXElement("http://x", null, "x", new AttributesImpl(), null);
        assertEquals("http://x", saxElement.getName().getNamespaceURI());
        assertEquals("", saxElement.getName().getPrefix());
        assertEquals("x", saxElement.getName().getLocalPart());

        saxElement = new DefaultSAXElement("http://x", "x", null, new AttributesImpl(), null);
        assertEquals("http://x", saxElement.getName().getNamespaceURI());
        assertEquals("", saxElement.getName().getPrefix());
        assertEquals("x", saxElement.getName().getLocalPart());

        try {
            new DefaultSAXElement("http://x", null, null, new AttributesImpl(), null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid QName: namespaceURI='http://x', localName='null', qName='null'.", e.getMessage());
        }

        try {
            new DefaultSAXElement(null, null, null, new AttributesImpl(), null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid QName: namespaceURI='null', localName='null', qName='null'.", e.getMessage());
        }
    }

	@Test
    public void test_attributes() {
        AttributesImpl attributes = new AttributesImpl();

        attributes.addAttribute("", "a", "", "", "1");
        attributes.addAttribute("http://a", "a", "", "", "a");
        attributes.addAttribute("http://b", "a", "", "", "b");

        SAXElement saxElement = new DefaultSAXElement("http://x", "a", "x", attributes, null);
        assertEquals("1", saxElement.getAttribute("a"));
        assertEquals("a", saxElement.getAttributeNS("http://a", "a"));
        assertEquals("b", saxElement.getAttributeNS("http://b", "a"));
    }

	@Test
    public void test_accumulateText() {
        SAXElement saxElement1 = new DefaultSAXElement("http://x", "a", "x", new AttributesImpl(), null);
        SAXElement saxElement2 = new DefaultSAXElement("http://x", "a", "x", new AttributesImpl(), null);

        try {
            saxElement1.getTextContent();
            fail("Expected SmooksException.");
        } catch(SmooksException e) {
            assertEquals("Illegal call to getTextContent().  SAXElement instance not accumulating SAXText Objects.  You must call SAXElement.accumulateText(), or annotate the Visitor implementation class with the @TextConsumer annotation.", e.getMessage());
        }

        saxElement1.accumulateText();
        saxElement2.accumulateText();

        saxElement1.getText().add(new DefaultSAXText("Text1", TextType.TEXT));
        assertEquals("Text1", saxElement1.getTextContent());
        assertSame(saxElement1.getTextContent(), saxElement1.getTextContent());
        saxElement1.getText().add(new DefaultSAXText("Text2", TextType.CDATA));
        assertEquals("Text1<![CDATA[Text2]]>", saxElement1.getTextContent());

            // Interleave with adding test to saxElement2...
            saxElement2.getText().add(new DefaultSAXText("XXXXXX", TextType.TEXT));
            saxElement2.getText().add(new DefaultSAXText("yyyyyyyy", TextType.CDATA));

        saxElement1.getText().add(new DefaultSAXText("Text3", TextType.COMMENT));
        assertEquals("Text1<![CDATA[Text2]]><!--Text3-->", saxElement1.getTextContent());
        saxElement1.getText().add(new DefaultSAXText("Text4", TextType.TEXT));
        assertEquals("Text1<![CDATA[Text2]]><!--Text3-->Text4", saxElement1.getTextContent());

        // Check saxElement2 OK...
        assertEquals("XXXXXX<![CDATA[yyyyyyyy]]>", saxElement2.getTextContent());
    }
}
