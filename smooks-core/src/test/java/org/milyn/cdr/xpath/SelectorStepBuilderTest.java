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
package org.milyn.cdr.xpath;

import java.util.Properties;

import org.jaxen.saxpath.SAXPathException;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.container.ExecutionContext;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.xml.XmlUtil;
import org.xml.sax.helpers.AttributesImpl;
import org.w3c.dom.Element;

import junit.framework.TestCase;

import javax.xml.namespace.QName;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class SelectorStepBuilderTest extends TestCase {

    private static Properties namespaces = new Properties();

    static {
        namespaces.put("a", "http://a");
        namespaces.put("b", "http://b");
        namespaces.put("c", "http://c");
        namespaces.put("d", "http://d");
    }

    public void test_1() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("x/y[text() = '23']", namespaces);
        assertEquals("x/y(text() = '23')", SelectorStepBuilder.toString(steps));
        assertTrue(!steps[0].isRooted());

        assertFalse(steps[0].accessesText());
        assertTrue(steps[1].accessesText());
    }

    public void test_2() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("x/y[@d = '23']/*", namespaces);
        assertEquals("x/y(@d = '23')/*", SelectorStepBuilder.toString(steps));

        assertFalse(steps[0].accessesText());
        assertFalse(steps[1].accessesText());
        assertFalse(steps[2].accessesText());

        SAXElement y = new SAXElement(null, "y");

        assertFalse(steps[1].getPredicatesEvaluator().evaluate(y, null));

        y.setAttribute("d", "22");
        assertFalse(steps[1].getPredicatesEvaluator().evaluate(y, null));

        y.setAttribute("d", "23");
        assertTrue(steps[1].getPredicatesEvaluator().evaluate(y, null));
    }

    public void test_2_1() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("x/y[@d = 23]/*", namespaces);
        assertEquals("x/y(@d = 23.0)/*", SelectorStepBuilder.toString(steps));

        SAXElement y = new SAXElement(null, "y");

        assertFalse(steps[1].getPredicatesEvaluator().evaluate(y, null));

        y.setAttribute("d", "22");
        assertFalse(steps[1].getPredicatesEvaluator().evaluate(y, null));

        y.setAttribute("d", "23");
        assertTrue(steps[1].getPredicatesEvaluator().evaluate(y, null));
    }

    public void test_3_1() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("x/y[(@d = 23 or text() = 'ddd') and @h = 'rrr']", namespaces);
        assertEquals("x/y(((@d = 23.0) or (text() = 'ddd')) and (@h = 'rrr'))", SelectorStepBuilder.toString(steps));

        assertFalse(steps[0].accessesText());
        assertTrue(steps[1].accessesText());

        SAXElement y = new SAXElement(null, "y");
        y.setAttribute("d", "2");
        y.addText("dd");
        y.setAttribute("h", "rr");
        assertFalse(steps[1].getPredicatesEvaluator().evaluate(y, null));

        y = new SAXElement(null, "y");
        y.setAttribute("d", "23");
        y.addText("dd");
        y.setAttribute("h", "rr");
        assertFalse(steps[1].getPredicatesEvaluator().evaluate(y, null));

        y = new SAXElement(null, "y");
        y.setAttribute("d", "23");
        y.addText("dd");
        y.setAttribute("h", "rrr");
        assertTrue(steps[1].getPredicatesEvaluator().evaluate(y, null));

        y = new SAXElement(null, "y");
        y.setAttribute("d", "2");
        y.addText("dd");
        y.setAttribute("h", "rrr");
        assertFalse(steps[1].getPredicatesEvaluator().evaluate(y, null));

        y = new SAXElement(null, "y");
        y.setAttribute("d", "2");
        y.addText("ddd");
        y.setAttribute("h", "rrr");
        assertTrue(steps[1].getPredicatesEvaluator().evaluate(y, null));
    }

    public void test_3_1_1() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("x/y[(@d = 23 or text() = 'ddd') and @h = 'rrr']", namespaces);
        assertEquals("x/y(((@d = 23.0) or (text() = 'ddd')) and (@h = 'rrr'))", SelectorStepBuilder.toString(steps));

        Element y = XmlUtil.createElement("y");
        y.setAttribute("d", "2");
        y.appendChild(y.getOwnerDocument().createTextNode("dd"));
        y.setAttribute("h", "rr");
        assertFalse(steps[1].getPredicatesEvaluator().evaluate(y, null));

        y = XmlUtil.createElement("y");
        y.setAttribute("d", "23");
        y.appendChild(y.getOwnerDocument().createTextNode("dd"));
        y.setAttribute("h", "rr");
        assertFalse(steps[1].getPredicatesEvaluator().evaluate(y, null));

        y = XmlUtil.createElement("y");
        y.setAttribute("d", "23");
        y.appendChild(y.getOwnerDocument().createTextNode("dd"));
        y.setAttribute("h", "rrr");
        assertTrue(steps[1].getPredicatesEvaluator().evaluate(y, null));

        y = XmlUtil.createElement("y");
        y.setAttribute("d", "2");
        y.appendChild(y.getOwnerDocument().createTextNode("dd"));
        y.setAttribute("h", "rrr");
        assertFalse(steps[1].getPredicatesEvaluator().evaluate(y, null));

        y = XmlUtil.createElement("y");
        y.setAttribute("d", "2");
        y.appendChild(y.getOwnerDocument().createTextNode("ddd"));
        y.setAttribute("h", "rrr");
        assertTrue(steps[1].getPredicatesEvaluator().evaluate(y, null));
    }

    public void test_3_2() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("y[@d = 23 and text() = 35]", namespaces);
        assertEquals("y((@d = 23.0) and (text() = 35.0))", SelectorStepBuilder.toString(steps));

        SAXElement y = new SAXElement(null, "y");
        y.setAttribute("d", "2");
        assertFalse(steps[0].getPredicatesEvaluator().evaluate(y, null));

        y = new SAXElement(null, "y");
        y.setAttribute("d", "23");
        y.addText("dd");
        assertFalse(steps[0].getPredicatesEvaluator().evaluate(y, null));

        y = new SAXElement(null, "y");
        y.setAttribute("d", "23");
        y.addText("35");
        assertTrue(steps[0].getPredicatesEvaluator().evaluate(y, null));
    }

    public void test_3_3() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("y[@a:d = 23]", namespaces);
        assertEquals("y(@{http://a}d = 23.0)", SelectorStepBuilder.toString(steps));

        SAXElement y = new SAXElement(null, "y");
        y.setAttribute("d", "23");
        assertFalse(steps[0].getPredicatesEvaluator().evaluate(y, null));

        y = new SAXElement(null, "y");
        y.setAttributeNS("http://a", "d", "23");
        assertTrue(steps[0].getPredicatesEvaluator().evaluate(y, null));
    }

    public void test_3_4() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("y[@a:d < 23]", namespaces);
        assertEquals("y(@{http://a}d < 23.0)", SelectorStepBuilder.toString(steps));

        SAXElement y = new SAXElement(null, "y");
        y.setAttribute("d", "23");
        assertFalse(steps[0].getPredicatesEvaluator().evaluate(y, null));

        y = new SAXElement(null, "y");
        y.setAttributeNS("http://a", "d", "22");
        assertTrue(steps[0].getPredicatesEvaluator().evaluate(y, null));

        y = new SAXElement(null, "y");
        y.setAttributeNS("http://a", "d", "23");
        assertFalse(steps[0].getPredicatesEvaluator().evaluate(y, null));
    }

    public void test_3_5() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("y[@a:d > 23]", namespaces);
        assertEquals("y(@{http://a}d > 23.0)", SelectorStepBuilder.toString(steps));

        SAXElement y = new SAXElement(null, "y");
        y.setAttribute("d", "23");
        assertFalse(steps[0].getPredicatesEvaluator().evaluate(y, null));

        y = new SAXElement(null, "y");
        y.setAttributeNS("http://a", "d", "24");
        assertTrue(steps[0].getPredicatesEvaluator().evaluate(y, null));

        y = new SAXElement(null, "y");
        y.setAttributeNS("http://a", "d", "23");
        assertFalse(steps[0].getPredicatesEvaluator().evaluate(y, null));
    }

    public void test_4() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("x/y[@d = text()]", namespaces);
        assertEquals("x/y(@d = text())", SelectorStepBuilder.toString(steps));
    }

    public void test_5() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("a:x/b:y", namespaces);
        assertEquals("{http://a}x/{http://b}y", SelectorStepBuilder.toString(steps));
    }

    public void test_6() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("x/y[@c:z = 78]", namespaces);
        assertEquals("x/y(@{http://c}z = 78.0)", SelectorStepBuilder.toString(steps));
    }

    public void test_7() throws SAXPathException {
        try {
            SelectorStepBuilder.buildSteps("x/@v/@c", namespaces);
            fail("Expected SAXPathException");
        } catch(SAXPathException e) {
            assertEquals("Error processing XPath selector expression 'x/@v/@c'.", e.getMessage());
            assertEquals("Attribute axis steps are only supported at the end of the expression.  'attribute::v' is not at the end.", e.getCause().getMessage());
        }
    }

    public void test_8_1() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("x/y/@c", namespaces);
        assertEquals("x/y{@c}", SelectorStepBuilder.toString(steps));
    }

    public void test_8_2() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("x/y/@a:c", namespaces);
        assertEquals("x/y{@{http://a}c}", SelectorStepBuilder.toString(steps));
    }

    public void test_8_3() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("x/y/@a:c[@xxx = 123]", namespaces);
        assertEquals("x/y{@{http://a}c}(@xxx = 123.0)", SelectorStepBuilder.toString(steps));
    }

    public void test_8_4() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("x/y[@xxx = 123]/@a:c[@yyy = 'abc']", namespaces);
        assertEquals("x/y{@{http://a}c}(@xxx = 123.0) and (@yyy = 'abc')", SelectorStepBuilder.toString(steps));
    }

    public void test_9() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("x/y/@c[@g = '987']", namespaces);
        assertEquals("x/y{@c}(@g = '987')", SelectorStepBuilder.toString(steps));
    }

    public void test_10() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("x/y[@n = 1]/@c[@g = '987']", namespaces);
        assertEquals("x/y{@c}(@n = 1.0) and (@g = '987')", SelectorStepBuilder.toString(steps));
    }

    public void test_11() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("x/y[@c:n = 1]/@c[@c:g = '987']", namespaces);
        assertEquals("x/y{@c}(@{http://c}n = 1.0) and (@{http://c}g = '987')", SelectorStepBuilder.toString(steps));
    }

    public void test_12() throws SAXPathException {
        try {
            SelectorStepBuilder.buildSteps("chapter[title=\"Introduction\"]", namespaces);
            fail("Expected SAXPathException");
        } catch(SAXPathException e) {
            assertEquals("Error processing XPath selector expression 'chapter[title=\"Introduction\"]'.", e.getMessage());
            assertEquals("Unsupported XPath value token 'child::title'.", e.getCause().getMessage());
        }
    }

    public void test_13() throws SAXPathException {
        try {
            SelectorStepBuilder.buildSteps("para[last()]", namespaces);
            fail("Expected SAXPathException");
        } catch(SAXPathException e) {
            assertEquals("Error processing XPath selector expression 'para[last()]'.", e.getMessage());
            assertEquals("Unsupported XPath expr token 'last()'.", e.getCause().getMessage());
        }
    }

    public void test_14() throws SAXPathException {
        try {
            SelectorStepBuilder.buildSteps("a/../para[last()]", namespaces);
            fail("Expected SAXPathException");
        } catch(SAXPathException e) {
            assertEquals("Error processing XPath selector expression 'a/../para[last()]'.", e.getMessage());
            assertEquals("XPath step 'parent::node()' not supported.", e.getCause().getMessage());
        }
    }

    public void test_15() throws SAXPathException {
        try {
            SelectorStepBuilder.buildSteps("employee[@secretary and @assistant]", namespaces);
            fail("Expected SAXPathException");
        } catch(SAXPathException e) {
            assertEquals("Error processing XPath selector expression 'employee[@secretary and @assistant]'.", e.getMessage());
            assertEquals("Unsupported XPath expr token 'attribute::secretary'.", e.getCause().getMessage());
        }
    }

    public void test_16() throws SAXPathException {
        try {
            SelectorStepBuilder.buildSteps("employee[/a/b/@c='xxx']", namespaces);
            fail("Expected SAXPathException");
        } catch(SAXPathException e) {
            assertEquals("Error processing XPath selector expression 'employee[/a/b/@c='xxx']'.", e.getMessage());
            assertEquals("Unsupported XPath value token '/child::a/child::b/attribute::c'.", e.getCause().getMessage());
        }
    }

    public void test_17() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("a/b[2]/c", namespaces);
        assertEquals("a/b[2]/c", SelectorStepBuilder.toString(steps));
    }

    public void test_18() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("a/b[2 and @a = 's']/c", namespaces);
        assertEquals("a/b([2] and (@a = 's'))/c", SelectorStepBuilder.toString(steps));
    }

    public void test_19() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("a/b[@a != 's']/c", namespaces);
        assertEquals("a/b(@a != 's')/c", SelectorStepBuilder.toString(steps));
    }

    public void test_20() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("a/b[@a != 123]/c", namespaces);
        assertEquals("a/b(@a != 123.0)/c", SelectorStepBuilder.toString(steps));
    }

    public void test_21() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("a/b[text() != 's']", namespaces);
        assertEquals("a/b(text() != 's')", SelectorStepBuilder.toString(steps));
    }

    public void test_22() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("b[text() != 's']", namespaces);
        assertEquals("b(text() != 's')", SelectorStepBuilder.toString(steps));
    }

    public void test_23() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("a  b[@h != 's'] c", namespaces);
        assertEquals("a/b(@h != 's')/c", SelectorStepBuilder.toString(steps));
    }

    public void test_24() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("#document", namespaces);
        assertEquals("/#document", SelectorStepBuilder.toString(steps));
        assertTrue(steps[0].isRooted());
    }

    public void test_25() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("#document/b[text() != 's']", namespaces);
        assertEquals("/b(text() != 's')", SelectorStepBuilder.toString(steps));
        assertTrue(steps[0].isRooted());
    }

    public void test_26() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("$document", namespaces);
        assertEquals("/#document", SelectorStepBuilder.toString(steps));
        assertTrue(steps[0].isRooted());
    }

    public void test_27() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("$document/b[text() != 's']", namespaces);
        assertEquals("/b(text() != 's')", SelectorStepBuilder.toString(steps));
        assertTrue(steps[0].isRooted());
    }

    public void test_28() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("/b[text() != 's']", namespaces);
        assertEquals("/b(text() != 's')", SelectorStepBuilder.toString(steps));
    }

    public void test_29() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("a//b", namespaces);
        assertEquals("a/**/b", SelectorStepBuilder.toString(steps));
        assertTrue(steps[1].isStarStar());
    }

    public void test_30() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("a/**/b", namespaces);
        assertEquals("a/**/b", SelectorStepBuilder.toString(steps));
        assertTrue(steps[1].isStarStar());
    }

    public void test_31() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("a/**/b/c", namespaces);
        assertEquals("a/**/b/c", SelectorStepBuilder.toString(steps));
        assertTrue(steps[1].isStarStar());
    }

    public void test_32() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("**/b/c", namespaces);
        assertEquals("**/b/c", SelectorStepBuilder.toString(steps));
        assertTrue(steps[0].isStarStar());
    }
                    
    public void test_33() throws SAXPathException {
        SelectorStep[] steps = SelectorStepBuilder.buildSteps("a/**", namespaces);
        assertEquals("a/**", SelectorStepBuilder.toString(steps));
        assertTrue(steps[1].isStarStar());
    }

    public void test_34() throws SAXPathException {
        try {
            // text() XPath nodes only supported in the last step
            SelectorStepBuilder.buildSteps("a[text() = 123]/b", namespaces);
            fail("Expected SAXPathException");
        } catch(SAXPathException e) {
            assertEquals("Unsupported XPath selector expression 'a[text() = 123]/b'.  XPath 'text()' tokens are only supported in the last step.", e.getMessage());
        }
    }
}
