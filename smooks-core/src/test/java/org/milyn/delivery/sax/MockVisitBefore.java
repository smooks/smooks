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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.milyn.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMVisitBefore;
import org.w3c.dom.Element;

/**
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MockVisitBefore implements SAXVisitBefore, DOMVisitBefore {

	private List<String> elements = new ArrayList<String>();
	
	/* (non-Javadoc)
	 * @see org.milyn.delivery.sax.SAXVisitBefore#visitBefore(org.milyn.delivery.sax.SAXElement, org.milyn.container.ExecutionContext)
	 */
	public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
		elements.add(element.getName().getLocalPart());
	}
	
	/* (non-Javadoc)
	 * @see org.milyn.delivery.dom.DOMVisitBefore#visitBefore(org.w3c.dom.Element, org.milyn.container.ExecutionContext)
	 */
	public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
		elements.add(element.getLocalName());
	}

	/**
	 * @return the elements
	 */
	public List<String> getElements() {
		return elements;
	}
}
