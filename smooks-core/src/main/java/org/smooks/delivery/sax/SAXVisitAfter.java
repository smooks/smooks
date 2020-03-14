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

import org.smooks.container.ExecutionContext;
import org.smooks.SmooksException;

import java.io.IOException;

/**
 * SAX Visit after events.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface SAXVisitAfter extends SAXVisitor {
    /**
     * Visit the supplied element <b>after</b> visiting its child elements.
     *
     * @param element          The SAX element being visited.
     * @param executionContext Execution context.
     * @throws org.smooks.SmooksException Event processing failure.
     * @throws java.io.IOException     Error writing event to output writer.
     */
    void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException;
}
