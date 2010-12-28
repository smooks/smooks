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
package org.milyn.delivery;

import org.milyn.delivery.sax.SAXElementVisitor;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXText;
import org.milyn.delivery.dom.DOMElementVisitor;
import org.milyn.container.ExecutionContext;
import org.milyn.SmooksException;
import org.w3c.dom.Element;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ExceptionVisitor implements SAXElementVisitor, DOMElementVisitor {

    public static SmooksException beforeException;
    public static SmooksException afterException;

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        if(beforeException != null) {
            throw beforeException;
        }
    }

    public void onChildText(SAXElement element, SAXText childText, ExecutionContext executionContext) throws SmooksException, IOException {
    }

    public void onChildElement(SAXElement element, SAXElement childElement, ExecutionContext executionContext) throws SmooksException, IOException {
    }

    public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        if(afterException != null) {
            throw afterException;
        }
    }

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        if(beforeException != null) {
            throw beforeException;
        }
    }

    public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
        if(afterException != null) {
            throw afterException;
        }
    }
}
