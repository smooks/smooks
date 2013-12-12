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

import org.milyn.commons.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.sax.annotation.StreamResultWriter;
import org.milyn.delivery.sax.annotation.TextConsumer;

import java.io.IOException;

@TextConsumer
public class VisitAfterWrittingVisitor implements SAXVisitAfter {

    @StreamResultWriter
    private SAXToXMLWriter writer;

    public static String elementText = null;

    public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        writer.writeStartElement(element);
        writer.writeText("{{", element);
        writer.writeText(element);
        writer.writeText("}}", element);
        writer.writeEndElement(element);

        elementText = element.getTextContent();
    }
}