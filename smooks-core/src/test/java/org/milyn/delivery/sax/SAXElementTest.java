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
package org.milyn.delivery.sax;

import junit.framework.TestCase;
import org.milyn.SmooksException;
import org.xml.sax.helpers.AttributesImpl;


/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SAXElementTest extends TestCase {

    public void test_create() {
        SAXElement saxElement;

        saxElement = new SAXElement("http://x", "a", "x", new AttributesImpl(), null);
        assertEquals("http://x", saxElement.getName().getNamespaceURI());
        assertEquals("", saxElement.getName().getPrefix());
        assertEquals("a", saxElement.getName().getLocalPart());

        saxElement = new SAXElement("http://x", "", "x", new AttributesImpl(), null);
        assertEquals("http://x", saxElement.getName().getNamespaceURI());
        assertEquals("", saxElement.getName().getPrefix());
        assertEquals("x", saxElement.getName().getLocalPart());

        saxElement = new SAXElement("http://x", "a", "x:a", new AttributesImpl(), null);
        assertEquals("http://x", saxElement.getName().getNamespaceURI());
        assertEquals("x", saxElement.getName().getPrefix());
        assertEquals("a", saxElement.getName().getLocalPart());

        saxElement = new SAXElement("http://x", null, "x", new AttributesImpl(), null);
        assertEquals("http://x", saxElement.getName().getNamespaceURI());
        assertEquals("", saxElement.getName().getPrefix());
        assertEquals("x", saxElement.getName().getLocalPart());

        saxElement = new SAXElement("http://x", "x", null, new AttributesImpl(), null);
        assertEquals("http://x", saxElement.getName().getNamespaceURI());
        assertEquals("", saxElement.getName().getPrefix());
        assertEquals("x", saxElement.getName().getLocalPart());

        try {
            new SAXElement("http://x", null, null, new AttributesImpl(), null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid QName: namespaceURI='http://x', localName='null', qName='null'.", e.getMessage());
        }

        try {
            new SAXElement(null, null, null, new AttributesImpl(), null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid QName: namespaceURI='null', localName='null', qName='null'.", e.getMessage());
        }
    }

    public void test_attributes() {
        AttributesImpl attributes = new AttributesImpl();

        attributes.addAttribute("", "a", "", "", "1");
        attributes.addAttribute("http://a", "a", "", "", "a");
        attributes.addAttribute("http://b", "a", "", "", "b");

        SAXElement saxElement = new SAXElement("http://x", "a", "x", attributes, null);
        assertEquals("1", saxElement.getAttribute("a"));
        assertEquals("a", saxElement.getAttributeNS("http://a", "a"));
        assertEquals("b", saxElement.getAttributeNS("http://b", "a"));
    }

    public void test_accumulateText() {
        SAXElement saxElement1 = new SAXElement("http://x", "a", "x", new AttributesImpl(), null);
        SAXElement saxElement2 = new SAXElement("http://x", "a", "x", new AttributesImpl(), null);

        try {
            saxElement1.getTextContent();
            fail("Expected SmooksException.");
        } catch(SmooksException e) {
            assertEquals("Illegal call to getTextContent().  SAXElement instance not accumulating SAXText Objects.  You must call SAXElement.accumulateText(), or annotate the Visitor implementation class with the @TextConsumer annotation.", e.getMessage());
        }

        saxElement1.accumulateText();
        saxElement2.accumulateText();

        saxElement1.getText().add(new SAXText("Text1", TextType.TEXT));
        assertEquals("Text1", saxElement1.getTextContent());
        assertTrue(saxElement1.getTextContent() == saxElement1.getTextContent());
        saxElement1.getText().add(new SAXText("Text2", TextType.CDATA));
        assertEquals("Text1<![CDATA[Text2]]>", saxElement1.getTextContent());

            // Interleave with adding test to saxElement2...
            saxElement2.getText().add(new SAXText("XXXXXX", TextType.TEXT));
            saxElement2.getText().add(new SAXText("yyyyyyyy", TextType.CDATA));

        saxElement1.getText().add(new SAXText("Text3", TextType.COMMENT));
        assertEquals("Text1<![CDATA[Text2]]><!--Text3-->", saxElement1.getTextContent());
        saxElement1.getText().add(new SAXText("Text4", TextType.TEXT));
        assertEquals("Text1<![CDATA[Text2]]><!--Text3-->Text4", saxElement1.getTextContent());

        // Check saxElement2 OK...
        assertEquals("XXXXXX<![CDATA[yyyyyyyy]]>", saxElement2.getTextContent());
    }
}
