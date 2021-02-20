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
import static org.junit.Assert.*;

import org.smooks.api.delivery.sax.SAXElement;
import org.smooks.support.SAXUtil;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SAXUtilTest {

	@Test
    public void test_getXPath() {
        SAXElement a = new DefaultSAXElement("http://x", "a", "a", new AttributesImpl(), null);
        SAXElement b = new DefaultSAXElement("http://x", "b", "b", new AttributesImpl(), a);
        SAXElement c = new DefaultSAXElement("http://x", "c", "c", new AttributesImpl(), b);
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
