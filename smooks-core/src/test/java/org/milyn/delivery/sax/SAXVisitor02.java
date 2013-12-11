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
package org.milyn.delivery.sax;

import junit.framework.TestCase;
import org.milyn.commons.SmooksException;
import org.milyn.container.ExecutionContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SAXVisitor02 implements SAXElementVisitor {

    public static SAXElement element;
    public static List<SAXElement> children = new ArrayList<SAXElement>();
    public static List<String> childText = new ArrayList<String>();

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        SAXVisitor02.element = element;
    }

    public void onChildText(SAXElement element, SAXText text, ExecutionContext executionContext) throws SmooksException, IOException {
        TestCase.assertEquals(SAXVisitor02.element, element);
        childText.add(text.getText());
    }

    public void onChildElement(SAXElement element, SAXElement childElement, ExecutionContext executionContext) throws SmooksException, IOException {
        TestCase.assertEquals(SAXVisitor02.element, element);
        children.add(childElement);
    }

    public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        TestCase.assertEquals(SAXVisitor02.element, element);
    }
}