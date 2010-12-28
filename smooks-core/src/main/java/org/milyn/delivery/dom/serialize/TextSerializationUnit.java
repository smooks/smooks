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
package org.milyn.delivery.dom.serialize;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.SmooksException;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXElementVisitor;
import org.milyn.delivery.sax.SAXText;
import org.milyn.delivery.sax.SAXVisitBefore;
import org.milyn.xml.DomUtils;
import org.milyn.xml.Namespace;
import org.w3c.dom.*;

import java.io.IOException;
import java.io.Writer;

/**
 * Write a &lt;text&gt; element.
 * <p/>
 * Basically just drops the &lt;text&gt; tags. 
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class TextSerializationUnit extends DefaultSerializationUnit implements SAXVisitBefore {

    public void writeElementStart(Element element, Writer writer, ExecutionContext executionContext) throws IOException {
    }

    public void writeElementEnd(Element element, Writer writer, ExecutionContext executionContext) throws IOException {
    }

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
    }

    public static Element createTextElement(Element element, String templatingResult) {
        Document ownerDocument = element.getOwnerDocument();
        Element resultElement = ownerDocument.createElementNS(Namespace.SMOOKS_URI, "text");
        resultElement.appendChild(ownerDocument.createTextNode(templatingResult));
        return resultElement;
    }

    public static boolean isTextElement(Element element) {
        if(DomUtils.getName(element).equals("text") && Namespace.SMOOKS_URI.equals(element.getNamespaceURI())) {
            return true;
        }

        return false;
    }

    public static String getText(Element element) {
        return DomUtils.getAllText(element, false);
    }
}