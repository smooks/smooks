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
 * SAX Visit children events.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface SAXVisitChildren extends SAXVisitor {
    /**
     * Process the onChildText event for the targeted element.
     * <p/>
     * Be careful when caching element data.  This is not a DOM.
     *
     * @param element          The element containing the text (parent).  The targeted element.
     * @param childText        The text.
     * @param executionContext Execution context.
     * @throws org.milyn.SmooksException Event processing failure.
     * @throws java.io.IOException     Error writing event to output writer.
     */
    void onChildText(SAXElement element, SAXText childText, ExecutionContext executionContext) throws SmooksException, IOException;

    /**
     * Process the onChildElement event for the targeted element.
     * <p/>
     * Be careful when caching element data.  This is not a DOM.
     *
     * @param element          The element containing the child element (parent). The targeted element.
     * @param childElement     The child element just added to the targeted element.
     * @param executionContext Execution context.
     * @throws org.milyn.SmooksException Event processing failure.
     * @throws java.io.IOException     Error writing event to output writer.
     */
    void onChildElement(SAXElement element, SAXElement childElement, ExecutionContext executionContext) throws SmooksException, IOException;
}
