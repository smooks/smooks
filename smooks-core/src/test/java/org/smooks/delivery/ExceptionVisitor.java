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
package org.smooks.delivery;

import org.smooks.delivery.sax.SAXElementVisitor;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.delivery.sax.SAXText;
import org.smooks.delivery.dom.DOMElementVisitor;
import org.smooks.container.ExecutionContext;
import org.smooks.SmooksException;
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
