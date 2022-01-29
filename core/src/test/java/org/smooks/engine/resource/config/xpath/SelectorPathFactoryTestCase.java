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
package org.smooks.engine.resource.config.xpath;

import org.junit.jupiter.api.Test;
import org.smooks.api.resource.config.xpath.SelectorPath;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.smooks.engine.resource.config.xpath.step.ElementSelectorStep;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class SelectorPathFactoryTestCase {

    private static final Properties namespaces = new Properties();

    static {
        namespaces.put("a", "http://a");
        namespaces.put("b", "http://b");
        namespaces.put("c", "http://c");
        namespaces.put("d", "http://d");
    }

    @Test
    public void testNewSelectorPathGivenTextPredicate() {
        SelectorPath selectorPath = SelectorPathFactory.newSelectorPath("x/y[text() = '23']");

        assertTrue(selectorPath instanceof IndexedSelectorPath);
        assertEquals(2, selectorPath.size());

        assertTrue(selectorPath.get(0) instanceof ElementSelectorStep);
        assertFalse(((ElementSelectorStep) selectorPath.get(0)).accessesText());
        assertTrue(selectorPath.get(1) instanceof ElementSelectorStep);
        assertTrue(((ElementSelectorStep) selectorPath.get(1)).accessesText());
    }

    @Test
    public void testNewSelectorPathGivenTextAndAttributePredicate() throws ParserConfigurationException {
        SelectorPath selectorPath = SelectorPathFactory.newSelectorPath("x/y[(@d = 23 or text() = 'ddd') and @h = 'rrr']", namespaces);

        Element y = createElement("y");
        y.getOwnerDocument().createElement("x").appendChild(y);
        y.setAttribute("d", "2");
        y.appendChild(y.getOwnerDocument().createTextNode("dd"));
        y.setAttribute("h", "rr");
        assertFalse(new NodeFragment(y).isMatch(selectorPath, null));

        y = createElement("y");
        y.getOwnerDocument().createElement("x").appendChild(y);
        y.setAttribute("d", "23");
        y.appendChild(y.getOwnerDocument().createTextNode("dd"));
        y.setAttribute("h", "rr");
        assertFalse(new NodeFragment(y).isMatch(selectorPath, null));

        y = createElement("y");
        y.getOwnerDocument().createElement("x").appendChild(y);
        y.setAttribute("d", "23");
        y.appendChild(y.getOwnerDocument().createTextNode("dd"));
        y.setAttribute("h", "rrr");
        assertTrue(new NodeFragment(y).isMatch(selectorPath, null));

        y = createElement("y");
        y.getOwnerDocument().createElement("x").appendChild(y);
        y.setAttribute("d", "2");
        y.appendChild(y.getOwnerDocument().createTextNode("dd"));
        y.setAttribute("h", "rrr");
        assertFalse(new NodeFragment(y).isMatch(selectorPath, null));

        y = createElement("y");
        y.getOwnerDocument().createElement("x").appendChild(y);
        y.setAttribute("d", "2");
        y.appendChild(y.getOwnerDocument().createTextNode("ddd"));
        y.setAttribute("h", "rrr");
        assertTrue(new NodeFragment(y).isMatch(selectorPath, null));
    }

    @Test
    public void testNewSelectorPathGivenTextAttributeEqualityPredicate() throws ParserConfigurationException {
        SelectorPath selectorPath = SelectorPathFactory.newSelectorPath("x/y[@d = text()]", namespaces);

        Element y = createElement("y");
        y.getOwnerDocument().createElement("x").appendChild(y);
        y.setAttribute("d", "ddd");
        y.appendChild(y.getOwnerDocument().createTextNode("ddd"));
        y.setAttribute("h", "rrr");

        assertTrue(new NodeFragment(y).isMatch(selectorPath, null));

        y = createElement("y");
        y.getOwnerDocument().createElement("x").appendChild(y);
        y.setAttribute("d", "rrr");
        y.appendChild(y.getOwnerDocument().createTextNode("ddd"));
        y.setAttribute("h", "ddd");

        assertFalse(new NodeFragment(y).isMatch(selectorPath, null));
    }

    @Test
    public void testNewSelectorPathGivenElementNamespacePrefixesInPath() throws ParserConfigurationException {
        SelectorPath selectorPath = SelectorPathFactory.newSelectorPath("a:x/b:y", namespaces);

        Element y = createElement("http://b", "y");
        y.getOwnerDocument().createElementNS("http://a", "x").appendChild(y);
        y.setAttribute("d", "ddd");
        y.appendChild(y.getOwnerDocument().createTextNode("ddd"));
        y.setAttribute("h", "rrr");

        assertTrue(new NodeFragment(y).isMatch(selectorPath, null));

        y = createElement("http://d", "y");
        y.getOwnerDocument().createElementNS("http://a", "x").appendChild(y);
        y.setAttribute("d", "ddd");
        y.appendChild(y.getOwnerDocument().createTextNode("ddd"));
        y.setAttribute("h", "rrr");

        assertFalse(new NodeFragment(y).isMatch(selectorPath, null));
    }

    @Test
    public void testNewSelectorPathGivenAttributeNamespacePrefixPredicate() throws ParserConfigurationException {
        SelectorPath selectorPath = SelectorPathFactory.newSelectorPath("x/y[@c:z = 78]", namespaces);

        Element y = createElement("y");
        y.getOwnerDocument().createElement("x").appendChild(y);
        y.setAttributeNS("http://c", "z", "78");
        y.appendChild(y.getOwnerDocument().createTextNode("ddd"));
        y.setAttribute("h", "rrr");

        assertTrue(new NodeFragment(y).isMatch(selectorPath, null));

        y = createElement("y");
        y.getOwnerDocument().createElement("x").appendChild(y);
        y.setAttributeNS("http://d", "z", "78");
        y.appendChild(y.getOwnerDocument().createTextNode("ddd"));
        y.setAttribute("h", "rrr");

        assertFalse(new NodeFragment(y).isMatch(selectorPath, null));
    }

    @Test
    public void testNewSelectorPathGivenAttributeInPath() throws ParserConfigurationException {
        SelectorPath selectorPath = SelectorPathFactory.newSelectorPath("x/y/@c", namespaces);

        Element y = createElement("y");
        y.getOwnerDocument().createElement("x").appendChild(y);
        y.setAttribute("c", "dd");

        assertTrue(new NodeFragment(y).isMatch(selectorPath, null));
    }

    @Test
    public void testNewSelectorPathGivenNotPredicateIsTrue() throws ParserConfigurationException {
        SelectorPath selectorPath = SelectorPathFactory.newSelectorPath("not(self::v)", namespaces);

        Element y = createElement("y");
        Element x = y.getOwnerDocument().createElement("x");
        x.appendChild(y);
        y.setAttribute("c", "dd");

        assertTrue(new NodeFragment(x).isMatch(selectorPath, null));
    }

    @Test
    public void testNewSelectorPathGivenNotPredicateIsFalse() throws ParserConfigurationException {
        SelectorPath selectorPath = SelectorPathFactory.newSelectorPath("not(self::x)", namespaces);

        Element y = createElement("y");
        Element x = y.getOwnerDocument().createElement("x");
        x.appendChild(y);
        y.setAttribute("c", "dd");

        assertFalse(new NodeFragment(x).isMatch(selectorPath, null));
    }

    @Test
    public void testNewSelectorPathGivenAttributeWithNamespacePrefixInPath() throws ParserConfigurationException {
        SelectorPath selectorPath = SelectorPathFactory.newSelectorPath("x/y/@a:c", namespaces);

        Element y = createElement("y");
        y.getOwnerDocument().createElement("x").appendChild(y);
        y.setAttributeNS("http://a", "c", "dd");

        assertTrue(new NodeFragment(y).isMatch(selectorPath, null));
    }

    @Test
    public void testNewSelectorPathGivenAttributeWithNamespacePrefixInPathAndAttributePredicate() throws ParserConfigurationException {
        SelectorPath selectorPath = SelectorPathFactory.newSelectorPath("x/y[@xxx = 123]/@a:c", namespaces);

        Element y = createElement("y");
        y.getOwnerDocument().createElement("x").appendChild(y);
        y.setAttributeNS("http://a", "c", "dd");
        y.setAttribute("xxx", "123");

        assertTrue(new NodeFragment(y).isMatch(selectorPath, null));
    }

    @Test
    public void testNewSelectorPathGivenAttributeInPathAndAttributePredicates() throws ParserConfigurationException {
        SelectorPath selectorPath = SelectorPathFactory.newSelectorPath("x/y[@n = 1 and @g = '987']/@c", namespaces);

        Element y = createElement("y");
        y.getOwnerDocument().createElement("x").appendChild(y);
        y.setAttribute("c", "dd");
        y.setAttribute("n", "1");
        y.setAttribute("g", "987");

        assertTrue(new NodeFragment(y).isMatch(selectorPath, null));
    }

    @Test
    public void testNewSelectorPathGivenAttributeInPathAndAttributeWithNamespacesPredicates() throws ParserConfigurationException {
        SelectorPath selectorPath = SelectorPathFactory.newSelectorPath("x/y[@c:n = 1 and @c:g = '987']/@c", namespaces);

        Element y = createElement("y");
        y.getOwnerDocument().createElement("x").appendChild(y);
        y.setAttribute("c", "dd");
        y.setAttributeNS("http://c", "n", "1");
        y.setAttributeNS("http://c", "g", "987");

        assertTrue(new NodeFragment(y).isMatch(selectorPath, null));
    }

    @Test
    public void testNewSelectorPathGivenAnyDescendantOrSelfSelector() throws ParserConfigurationException {
        SelectorPath selectorPath = SelectorPathFactory.newSelectorPath("//*", namespaces);

        Element a = createElement("a");
        Element b = a.getOwnerDocument().createElement("b");
        Element c = a.getOwnerDocument().createElement("c");
        a.appendChild(b).appendChild(c);

        assertTrue(new NodeFragment(c).isMatch(selectorPath, null));
        assertTrue(new NodeFragment(b).isMatch(selectorPath, null));
        assertFalse(new NodeFragment(a).isMatch(selectorPath, null));
    }

    @Test
    public void testNewSelectorPathGivenAnyDescendantOrSelfSelectorWithNotPredicate() throws ParserConfigurationException {
        SelectorPath selectorPath = SelectorPathFactory.newSelectorPath("//*[not(self::b)]", namespaces);

        Element a = createElement("a");
        Element b = a.getOwnerDocument().createElement("b");
        Element c = a.getOwnerDocument().createElement("c");
        a.appendChild(b).appendChild(c);

        assertTrue(new NodeFragment(c).isMatch(selectorPath, null));
        assertFalse(new NodeFragment(b).isMatch(selectorPath, null));
        assertFalse(new NodeFragment(a).isMatch(selectorPath, null));
    }

    protected Element createElement(String elementNs, String localPart) throws ParserConfigurationException {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        return (Element) document.appendChild(document.createElementNS(elementNs, localPart));
    }

    protected Element createElement(String localPart) throws ParserConfigurationException {
        return createElement(XMLConstants.NULL_NS_URI, localPart);
    }
}
