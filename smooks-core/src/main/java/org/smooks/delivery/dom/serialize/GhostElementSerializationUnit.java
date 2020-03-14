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
package org.smooks.delivery.dom.serialize;

import org.smooks.SmooksException;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.delivery.sax.SAXElementVisitor;
import org.smooks.delivery.sax.SAXText;
import org.smooks.xml.Namespace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.Writer;

/**
 * Ghost element serialization unit.
 * <p/>
 * A ghost element can be used to "wrap" other DOM content.  The Ghost element itself
 * is not serialized, but it's child content is.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class GhostElementSerializationUnit extends DefaultSerializationUnit implements SAXElementVisitor {

    public void writeElementStart(Element element, Writer writer, ExecutionContext executionContext) throws IOException {
        // Write nothing...
    }

    public void writeElementEnd(Element element, Writer writer, ExecutionContext executionContext) throws IOException {
        // Write nothing...
    }

    /**
     * Utility method for creating a &lt;ghost-element/&gt; element.
     * @param ownerDocument The owner document.
     * @return The &lt;ghost-element/&gt; element.
     */
    public static Element createElement(Document ownerDocument) {
        return ownerDocument.createElementNS(Namespace.SMOOKS_URI, "ghost-element");
    }

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
    }

    public void onChildText(SAXElement element, SAXText childText, ExecutionContext executionContext) throws SmooksException, IOException {
    }

    public void onChildElement(SAXElement element, SAXElement childElement, ExecutionContext executionContext) throws SmooksException, IOException {
    }

    public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
    }
}