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
package org.milyn.event;

import org.milyn.commons.xml.DomUtils;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXUtil;
import org.w3c.dom.Element;

/**
 * An element processing related event.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class ElementProcessingEvent implements ExecutionEvent {

    private Object element;

    public ElementProcessingEvent(Object element) {
        this.element = element;
    }

    public Object getElement() {
        return element;
    }

    public int getDepth() {
        if (element instanceof Element) {
            return DomUtils.getDepth((Element) element);
        } else if (element instanceof SAXElement) {
            return SAXUtil.getDepth((SAXElement) element);
        }

        return 0;
    }
}
