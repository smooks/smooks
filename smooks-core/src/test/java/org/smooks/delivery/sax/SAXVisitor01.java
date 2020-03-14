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

import static org.junit.Assert.*;
import org.smooks.SmooksException;
import org.smooks.container.ExecutionContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SAXVisitor01 implements SAXElementVisitor {

    public static SAXElement element;
    public static List<SAXElement> children = new ArrayList<SAXElement>();
    public static List<String> childText = new ArrayList<String>();

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        SAXVisitor01.element = element;
    }

    public void onChildText(SAXElement element, SAXText text, ExecutionContext executionContext) throws SmooksException, IOException {
        assertEquals(SAXVisitor01.element, element);
        element.getCache(this);
        childText.add(text.getText());
    }

    public void onChildElement(SAXElement element, SAXElement childElement, ExecutionContext executionContext) throws SmooksException, IOException {
        assertEquals(SAXVisitor01.element, element);
        children.add(childElement);
    }

    public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        assertEquals(SAXVisitor01.element, element);
    }
}
