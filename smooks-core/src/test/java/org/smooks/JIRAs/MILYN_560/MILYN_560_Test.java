/*
 * Milyn - Copyright (C) 2006 - 2011
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (version 2.1) as published by the Free Software
 * Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */

package org.smooks.JIRAs.MILYN_560;

import org.junit.Test;
import static org.junit.Assert.*;
import org.smooks.Smooks;
import org.smooks.SmooksException;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.dom.DOMVisitAfter;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.delivery.sax.SAXVisitAfter;
import org.smooks.delivery.sax.annotation.TextConsumer;
import org.smooks.payload.StringResult;
import org.smooks.payload.StringSource;
import org.w3c.dom.Element;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MILYN_560_Test {

	@Test
    public void test_DOM() {
        Smooks smooks = new Smooks();

        smooks.addVisitor(new DOMVisitAfter() {
            public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
                assertEquals("&tomfennelly", element.getAttribute("attrib"));
                assertEquals("&tomfennelly", element.getTextContent());
            }
        }, "element");

        StringResult serializedRes = new StringResult();
        smooks.filterSource(new StringSource("<element attrib=\"&amp;tomfennelly\">&amp;tomfennelly</element>"), serializedRes);

        assertEquals("<element attrib=\"&amp;tomfennelly\">&amp;tomfennelly</element>", serializedRes.getResult());
    }

	@Test
    public void test_SAX() {
        Smooks smooks = new Smooks();

        smooks.addVisitor(new MockSAX(), "element");

        StringResult serializedRes = new StringResult();
        smooks.filterSource(new StringSource("<element attrib=\"&amp;tomfennelly\">&amp;tomfennelly</element>"), serializedRes);

        assertEquals("<element attrib=\"&amp;tomfennelly\">&amp;tomfennelly</element>", serializedRes.getResult());
    }

    @TextConsumer
    private class MockSAX implements SAXVisitAfter {
        public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
            assertEquals("&tomfennelly", element.getAttribute("attrib"));
            assertEquals("&tomfennelly", element.getTextContent());
        }
    }
}
