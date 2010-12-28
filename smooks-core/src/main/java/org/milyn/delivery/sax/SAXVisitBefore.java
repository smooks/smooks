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

import org.milyn.container.ExecutionContext;
import org.milyn.SmooksException;

import java.io.IOException;

/**
 * SAX Visit before events.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface SAXVisitBefore extends SAXVisitor {
    /**
     * Visit the supplied element <b>before</b> visiting its child elements.
     *
     * @param element          The SAX element being visited.
     * @param executionContext Execution context.
     * @throws org.milyn.SmooksException Event processing failure.
     * @throws java.io.IOException     Error writing event to output writer.
     */
    void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException;
}
