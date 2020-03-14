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
package org.smooks.cdr.xpath;

import org.smooks.delivery.sax.SAXElementVisitor;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.delivery.sax.SAXText;
import org.smooks.delivery.dom.DOMElementVisitor;
import org.smooks.container.ExecutionContext;
import org.smooks.SmooksException;
import org.w3c.dom.Element;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class XPathVisitor implements SAXElementVisitor, DOMElementVisitor {

    public static SAXElement saxVisitedBeforeElementStatic;
    public static SAXElement saxVisitedAfterElementStatic;
    public static Element domVisitedBeforeElementStatic;
    public static Element domVisitedAfterElementStatic;
    public SAXElement saxVisitedBeforeElement;
    public SAXElement saxVisitedAfterElement;
    public Element domVisitedBeforeElement;
    public Element domVisitedAfterElement;

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        saxVisitedBeforeElementStatic = element;
        saxVisitedBeforeElement = element;
    }

    public void onChildText(SAXElement element, SAXText childText, ExecutionContext executionContext) throws SmooksException, IOException {
    }

    public void onChildElement(SAXElement element, SAXElement childElement, ExecutionContext executionContext) throws SmooksException, IOException {
    }

    public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        saxVisitedAfterElementStatic = element;
        saxVisitedAfterElement = element;
    }

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        domVisitedBeforeElementStatic = element;
        domVisitedBeforeElement = element;
    }

    public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
        domVisitedAfterElementStatic = element;
        domVisitedAfterElement = element;
    }

    public SAXElement getSaxVisitedBeforeElement() {
        return saxVisitedBeforeElement;
    }

    public SAXElement getSaxVisitedAfterElement() {
        return saxVisitedAfterElement;
    }

    public Element getDomVisitedBeforeElement() {
        return domVisitedBeforeElement;
    }

    public Element getDomVisitedAfterElement() {
        return domVisitedAfterElement;
    }
}
