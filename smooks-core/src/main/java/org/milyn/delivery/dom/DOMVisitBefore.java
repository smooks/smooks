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
package org.milyn.delivery.dom;

import org.w3c.dom.Element;
import org.milyn.container.ExecutionContext;
import org.milyn.SmooksException;
import org.milyn.delivery.ContentHandler;

/**
 * DOM Visit before events.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface DOMVisitBefore  extends DOMVisitor {
    /**
     * Visit the supplied element <b>before</b> visiting its child elements.
     *
     * @param element          The DOM element being visited.
     * @param executionContext Request relative instance.
     * @throws org.milyn.SmooksException Element processing failure.
     */
    void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException;
}
