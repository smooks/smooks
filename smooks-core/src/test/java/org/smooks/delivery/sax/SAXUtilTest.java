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
package org.smooks.delivery.sax;

import org.junit.Test;
import static org.junit.Assert.*;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SAXUtilTest {

	@Test
    public void test_getXPath() {
        SAXElement a = new SAXElement("http://x", "a", "a", new AttributesImpl(), null);
        SAXElement b = new SAXElement("http://x", "b", "b", new AttributesImpl(), a);
        SAXElement c = new SAXElement("http://x", "c", "c", new AttributesImpl(), b);
        assertEquals("a/b/c", SAXUtil.getXPath(c));
    }

	@Test
    public void test_getAttribute() {
        AttributesImpl attributes = new AttributesImpl();

        attributes.addAttribute("", "a", "", "", "1");
        attributes.addAttribute("http://a", "a", "", "", "a");
        attributes.addAttribute("http://b", "a", "", "", "b");

        assertEquals("1", SAXUtil.getAttribute("a", attributes));
        assertEquals("a", SAXUtil.getAttribute("http://a", "a", attributes, ""));
        assertEquals("b", SAXUtil.getAttribute("http://b", "a", attributes, ""));
    }
}
