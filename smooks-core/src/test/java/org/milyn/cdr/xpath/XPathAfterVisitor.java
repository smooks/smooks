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

import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXVisitAfter;
import org.milyn.delivery.dom.DOMVisitAfter;
import org.milyn.container.ExecutionContext;
import org.milyn.SmooksException;
import org.w3c.dom.Element;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class XPathAfterVisitor implements SAXVisitAfter, DOMVisitAfter {

    public static SAXElement saxVisitedAfterElement;
    public static Element domVisitedAfterElement;

    public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        saxVisitedAfterElement = element;
    }

    public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
        domVisitedAfterElement = element;
    }
}