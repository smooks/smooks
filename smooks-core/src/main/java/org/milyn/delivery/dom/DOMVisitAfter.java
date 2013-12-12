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

import org.milyn.commons.SmooksException;
import org.milyn.container.ExecutionContext;
import org.w3c.dom.Element;

/**
 * DOM Visit after events.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface DOMVisitAfter extends DOMVisitor {
    /**
     * Visit the supplied element <b>after</b> visiting its child elements.
     *
     * @param element          The DOM element being visited.
     * @param executionContext Request relative instance.
     * @throws org.milyn.commons.SmooksException
     *          Element processing failure.
     */
    void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException;
}
