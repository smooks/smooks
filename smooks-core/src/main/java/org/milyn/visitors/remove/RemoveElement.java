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
package org.milyn.visitors.remove;

import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.commons.SmooksException;
import org.milyn.commons.io.NullWriter;
import org.milyn.commons.xml.DomUtils;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.dom.DOMVisitAfter;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXVisitAfter;
import org.milyn.delivery.sax.SAXVisitBefore;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.Writer;

/**
 * Remove Element.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class RemoveElement implements SAXVisitBefore, SAXVisitAfter, DOMVisitAfter {

    private boolean keepChildren;

    @ConfigParam(defaultVal = "false")
    public RemoveElement setKeepChildren(boolean keepChildren) {
        this.keepChildren = keepChildren;
        return this;
    }

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        // Claim ownership of the writer for this fragment element...
        Writer writer = element.getWriter(this);

        if (!keepChildren) {
            // Swap in a NullWriter instance for the whole fragment...
            element.setWriter(new NullWriter(), this);
            // Stash the real writer instance on the element so we can reset it at the end...
            element.setCache(this, writer);
        } else {
            // Just don't write this element, but write the child elements...
        }
    }

    public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        if (!keepChildren) {
            // Reset the writer...
            element.setWriter((Writer) element.getCache(this), this);
        }
    }

    public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
        DomUtils.removeElement(element, keepChildren);
    }
}
 