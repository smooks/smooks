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
package org.smooks.cdr.xpath;

import org.jaxen.saxpath.SAXPathException;
import org.junit.Test;
import org.smooks.SmooksException;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.xml.XmlUtil;
import org.w3c.dom.Element;

import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class SelectorStepBuilderTest  {

    private static final Properties namespaces = new Properties();

    static {
        namespaces.put("a", "http://a");
        namespaces.put("b", "http://b");
        namespaces.put("c", "http://c");
        namespaces.put("d", "http://d");
    }

    @Test
    public void test_1() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("x/y[text() = '23']", namespaces);
        assertEquals("x/y(text() = '23')", selectorPath.toString());
        assertFalse(selectorPath.get(0).isRooted());

        assertFalse(selectorPath.get(0).accessesText());
        assertTrue(selectorPath.get(1).accessesText());
    }

    @Test
    public void test_2() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("x/y[@d = '23']/*", namespaces);
        assertEquals("x/y(@d = '23')/*", selectorPath.toString());

        assertFalse(selectorPath.get(0).accessesText());
        assertFalse(selectorPath.get(1).accessesText());
        assertFalse(selectorPath.get(2).accessesText());

        SAXElement y = new SAXElement(null, "y");

        assertFalse(selectorPath.get(1).getPredicatesEvaluator().evaluate(y, null));

        y.setAttribute("d", "22");
        assertFalse(selectorPath.get(1).getPredicatesEvaluator().evaluate(y, null));

        y.setAttribute("d", "23");
        assertTrue(selectorPath.get(1).getPredicatesEvaluator().evaluate(y, null));
    }

    @Test
    public void test_2_1() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("x/y[@d = 23]/*", namespaces);
        assertEquals("x/y(@d = 23.0)/*", selectorPath.toString());

        SAXElement y = new SAXElement(null, "y");

        assertFalse(selectorPath.get(1).getPredicatesEvaluator().evaluate(y, null));

        y.setAttribute("d", "22");
        assertFalse(selectorPath.get(1).getPredicatesEvaluator().evaluate(y, null));

        y.setAttribute("d", "23");
        assertTrue(selectorPath.get(1).getPredicatesEvaluator().evaluate(y, null));
    }

    @Test
    public void test_3_1() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("x/y[(@d = 23 or text() = 'ddd') and @h = 'rrr']", namespaces);
        assertEquals("x/y(((@d = 23.0) or (text() = 'ddd')) and (@h = 'rrr'))", selectorPath.toString());

        assertFalse(selectorPath.get(0).accessesText());
        assertTrue(selectorPath.get(1).accessesText());

        SAXElement y = new SAXElement(null, "y");
        y.setAttribute("d", "2");
        y.addText("dd");
        y.setAttribute("h", "rr");
        assertFalse(selectorPath.get(1).getPredicatesEvaluator().evaluate(y, null));

        y = new SAXElement(null, "y");
        y.setAttribute("d", "23");
        y.addText("dd");
        y.setAttribute("h", "rr");
        assertFalse(selectorPath.get(1).getPredicatesEvaluator().evaluate(y, null));

        y = new SAXElement(null, "y");
        y.setAttribute("d", "23");
        y.addText("dd");
        y.setAttribute("h", "rrr");
        assertTrue(selectorPath.get(1).getPredicatesEvaluator().evaluate(y, null));

        y = new SAXElement(null, "y");
        y.setAttribute("d", "2");
        y.addText("dd");
        y.setAttribute("h", "rrr");
        assertFalse(selectorPath.get(1).getPredicatesEvaluator().evaluate(y, null));

        y = new SAXElement(null, "y");
        y.setAttribute("d", "2");
        y.addText("ddd");
        y.setAttribute("h", "rrr");
        assertTrue(selectorPath.get(1).getPredicatesEvaluator().evaluate(y, null));
    }

    @Test
    public void test_3_1_1() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("x/y[(@d = 23 or text() = 'ddd') and @h = 'rrr']", namespaces);
        assertEquals("x/y(((@d = 23.0) or (text() = 'ddd')) and (@h = 'rrr'))", selectorPath.toString());

        Element y = XmlUtil.createElement("y");
        y.setAttribute("d", "2");
        y.appendChild(y.getOwnerDocument().createTextNode("dd"));
        y.setAttribute("h", "rr");
        assertFalse(selectorPath.get(1).getPredicatesEvaluator().evaluate(y, null));

        y = XmlUtil.createElement("y");
        y.setAttribute("d", "23");
        y.appendChild(y.getOwnerDocument().createTextNode("dd"));
        y.setAttribute("h", "rr");
        assertFalse(selectorPath.get(1).getPredicatesEvaluator().evaluate(y, null));

        y = XmlUtil.createElement("y");
        y.setAttribute("d", "23");
        y.appendChild(y.getOwnerDocument().createTextNode("dd"));
        y.setAttribute("h", "rrr");
        assertTrue(selectorPath.get(1).getPredicatesEvaluator().evaluate(y, null));

        y = XmlUtil.createElement("y");
        y.setAttribute("d", "2");
        y.appendChild(y.getOwnerDocument().createTextNode("dd"));
        y.setAttribute("h", "rrr");
        assertFalse(selectorPath.get(1).getPredicatesEvaluator().evaluate(y, null));

        y = XmlUtil.createElement("y");
        y.setAttribute("d", "2");
        y.appendChild(y.getOwnerDocument().createTextNode("ddd"));
        y.setAttribute("h", "rrr");
        assertTrue(selectorPath.get(1).getPredicatesEvaluator().evaluate(y, null));
    }

    @Test
    public void test_3_2() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("y[@d = 23 and text() = 35]", namespaces);
        assertEquals("y((@d = 23.0) and (text() = 35.0))", selectorPath.toString());

        SAXElement y = new SAXElement(null, "y");
        y.setAttribute("d", "2");
        assertFalse(selectorPath.get(0).getPredicatesEvaluator().evaluate(y, null));

        y = new SAXElement(null, "y");
        y.setAttribute("d", "23");
        y.addText("dd");
        assertFalse(selectorPath.get(0).getPredicatesEvaluator().evaluate(y, null));

        y = new SAXElement(null, "y");
        y.setAttribute("d", "23");
        y.addText("35");
        assertTrue(selectorPath.get(0).getPredicatesEvaluator().evaluate(y, null));
    }

    @Test
    public void test_3_3() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("y[@a:d = 23]", namespaces);
        assertEquals("y(@{http://a}d = 23.0)", selectorPath.toString());

        SAXElement y = new SAXElement(null, "y");
        y.setAttribute("d", "23");
        assertFalse(selectorPath.get(0).getPredicatesEvaluator().evaluate(y, null));

        y = new SAXElement(null, "y");
        y.setAttributeNS("http://a", "d", "23");
        assertTrue(selectorPath.get(0).getPredicatesEvaluator().evaluate(y, null));
    }
    
    @Test
    public void test_3_4() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("y[@a:d < 23]", namespaces);
        assertEquals("y(@{http://a}d < 23.0)", selectorPath.toString());

        SAXElement y = new SAXElement(null, "y");
        y.setAttribute("d", "23");
        assertFalse(selectorPath.get(0).getPredicatesEvaluator().evaluate(y, null));

        y = new SAXElement(null, "y");
        y.setAttributeNS("http://a", "d", "22");
        assertTrue(selectorPath.get(0).getPredicatesEvaluator().evaluate(y, null));

        y = new SAXElement(null, "y");
        y.setAttributeNS("http://a", "d", "23");
        assertFalse(selectorPath.get(0).getPredicatesEvaluator().evaluate(y, null));
    }

    @Test
    public void test_3_5() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("y[@a:d > 23]", namespaces);
        assertEquals("y(@{http://a}d > 23.0)", selectorPath.toString());

        SAXElement y = new SAXElement(null, "y");
        y.setAttribute("d", "23");
        assertFalse(selectorPath.get(0).getPredicatesEvaluator().evaluate(y, null));

        y = new SAXElement(null, "y");
        y.setAttributeNS("http://a", "d", "24");
        assertTrue(selectorPath.get(0).getPredicatesEvaluator().evaluate(y, null));

        y = new SAXElement(null, "y");
        y.setAttributeNS("http://a", "d", "23");
        assertFalse(selectorPath.get(0).getPredicatesEvaluator().evaluate(y, null));
    }

    @Test
    public void test_4() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("x/y[@d = text()]", namespaces);
        assertEquals("x/y(@d = text())", selectorPath.toString());
    }

    @Test
    public void test_5() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("a:x/b:y", namespaces);
        assertEquals("{http://a}x/{http://b}y", selectorPath.toString());
    }

    @Test
    public void test_6() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("x/y[@c:z = 78]", namespaces);
        assertEquals("x/y(@{http://c}z = 78.0)", selectorPath.toString());
    }

    @Test
    public void test_7() throws SAXPathException {
        try {
            SelectorStepBuilder.buildSteps("x/@v/@c", namespaces);
            fail("Expected SAXPathException");
        } catch(SAXPathException e) {
            assertEquals("Error processing XPath selector expression 'x/@v/@c'.", e.getMessage());
            assertEquals("Attribute axis steps are only supported at the end of the expression.  'attribute::v' is not at the end.", e.getCause().getMessage());
        }
    }

    @Test
    public void test_8_1() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("x/y/@c", namespaces);
        assertEquals("x/y{@c}", selectorPath.toString());
    }

    @Test
    public void test_8_2() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("x/y/@a:c", namespaces);
        assertEquals("x/y{@{http://a}c}", selectorPath.toString());
    }

    @Test
    public void test_8_3() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("x/y/@a:c[@xxx = 123]", namespaces);
        assertEquals("x/y{@{http://a}c}(@xxx = 123.0)", selectorPath.toString());
    }

    @Test
    public void test_8_4() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("x/y[@xxx = 123]/@a:c[@yyy = 'abc']", namespaces);
        assertEquals("x/y{@{http://a}c}(@xxx = 123.0) and (@yyy = 'abc')", selectorPath.toString());
    }

    @Test
    public void test_9() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("x/y/@c[@g = '987']", namespaces);
        assertEquals("x/y{@c}(@g = '987')", selectorPath.toString());
    }

    @Test
    public void test_10() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("x/y[@n = 1]/@c[@g = '987']", namespaces);
        assertEquals("x/y{@c}(@n = 1.0) and (@g = '987')", selectorPath.toString());
    }

    @Test
    public void test_11() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("x/y[@c:n = 1]/@c[@c:g = '987']", namespaces);
        assertEquals("x/y{@c}(@{http://c}n = 1.0) and (@{http://c}g = '987')", selectorPath.toString());
    }

    @Test
    public void test_12() throws SAXPathException {
        try {
            SelectorStepBuilder.buildSteps("chapter[title=\"Introduction\"]", namespaces);
            fail("Expected SAXPathException");
        } catch(SAXPathException e) {
            assertEquals("Error processing XPath selector expression 'chapter[title=\"Introduction\"]'.", e.getMessage());
            assertEquals("Unsupported XPath value token 'child::title'.", e.getCause().getMessage());
        }
    }

    @Test
    public void test_13() throws SAXPathException {
        try {
            SelectorStepBuilder.buildSteps("para[last()]", namespaces);
            fail("Expected SAXPathException");
        } catch(SAXPathException e) {
            assertEquals("Error processing XPath selector expression 'para[last()]'.", e.getMessage());
            assertEquals("Unsupported XPath expr token 'last()'.", e.getCause().getMessage());
        }
    }

    @Test
    public void test_14() throws SAXPathException {
        try {
            SelectorStepBuilder.buildSteps("a/../para[last()]", namespaces);
            fail("Expected SAXPathException");
        } catch(SAXPathException e) {
            assertEquals("Error processing XPath selector expression 'a/../para[last()]'.", e.getMessage());
            assertEquals("XPath step 'parent::node()' not supported.", e.getCause().getMessage());
        }
    }

    @Test
    public void test_15() throws SAXPathException {
        try {
            SelectorStepBuilder.buildSteps("employee[@secretary and @assistant]", namespaces);
            fail("Expected SAXPathException");
        } catch(SAXPathException e) {
            assertEquals("Error processing XPath selector expression 'employee[@secretary and @assistant]'.", e.getMessage());
            assertEquals("Unsupported XPath expr token 'attribute::secretary'.", e.getCause().getMessage());
        }
    }

    @Test
    public void test_16() throws SAXPathException {
        try {
            SelectorStepBuilder.buildSteps("employee[/a/b/@c='xxx']", namespaces);
            fail("Expected SAXPathException");
        } catch(SAXPathException e) {
            assertEquals("Error processing XPath selector expression 'employee[/a/b/@c='xxx']'.", e.getMessage());
            assertEquals("Unsupported XPath value token '/child::a/child::b/attribute::c'.", e.getCause().getMessage());
        }
    }

    @Test
    public void test_17() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("a/b[2]/c", namespaces);
        assertEquals("a/b[2]/c", selectorPath.toString());
    }

    @Test
    public void test_18() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("a/b[2 and @a = 's']/c", namespaces);
        assertEquals("a/b([2] and (@a = 's'))/c", selectorPath.toString());
    }

    @Test
    public void test_19() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("a/b[@a != 's']/c", namespaces);
        assertEquals("a/b(@a != 's')/c", selectorPath.toString());
    }

    @Test
    public void test_20() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("a/b[@a != 123]/c", namespaces);
        assertEquals("a/b(@a != 123.0)/c", selectorPath.toString());
    }

    @Test
    public void test_21() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("a/b[text() != 's']", namespaces);
        assertEquals("a/b(text() != 's')", selectorPath.toString());
    }

    @Test
    public void test_22() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("b[text() != 's']", namespaces);
        assertEquals("b(text() != 's')", selectorPath.toString());
    }

    @Test
    public void test_23() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("a  b[@h != 's'] c", namespaces);
        assertEquals("a/b(@h != 's')/c", selectorPath.toString());
    }

    @Test
    public void test_24() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("#document", namespaces);
        assertEquals("/#document", selectorPath.toString());
        assertTrue(selectorPath.get(0).isRooted());
    }

    @Test
    public void test_25() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("#document/b[text() != 's']", namespaces);
        assertEquals("/b(text() != 's')", selectorPath.toString());
        assertTrue(selectorPath.get(0).isRooted());
    }

    @Test
    public void test_28() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("/b[text() != 's']", namespaces);
        assertEquals("/b(text() != 's')", selectorPath.toString());
    }

    @Test
    public void test_29() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("a//b", namespaces);
        assertEquals("a/**/b", selectorPath.toString());
        assertTrue(selectorPath.get(1).isStarStar());
    }

    @Test
    public void test_30() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("a/**/b", namespaces);
        assertEquals("a/**/b", selectorPath.toString());
        assertTrue(selectorPath.get(1).isStarStar());
    }

    @Test
    public void test_31() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("a/**/b/c", namespaces);
        assertEquals("a/**/b/c", selectorPath.toString());
        assertTrue(selectorPath.get(1).isStarStar());
    }

    @Test
    public void test_32() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("**/b/c", namespaces);
        assertEquals("**/b/c", selectorPath.toString());
        assertTrue(selectorPath.get(0).isStarStar());
    }
         
    @Test
    public void test_33() throws SAXPathException {
        SelectorPath selectorPath = SelectorStepBuilder.buildSteps("a/**", namespaces);
        assertEquals("a/**", selectorPath.toString());
        assertTrue(selectorPath.get(1).isStarStar());
    }

    @Test
    public void test_34() throws SAXPathException {
        try {
            // text() XPath nodes only supported in the last step
            SelectorStepBuilder.buildSteps("a[text() = 123]/b", namespaces);
            fail("Expected SAXPathException");
        } catch(SmooksException e) {
            assertEquals("Unsupported XPath selector expression 'a[text() = 123]/b'.  XPath 'text()' tokens are only supported in the last step.", e.getMessage());
        }
    }
}
