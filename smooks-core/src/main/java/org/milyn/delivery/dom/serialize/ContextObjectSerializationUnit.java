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
import org.milyn.xml.DomUtils;
import org.milyn.xml.Namespace;
import org.w3c.dom.*;

import java.io.IOException;
import java.io.Writer;

/**
 * {@link ExecutionContext} object serializer.
 * <p/>
 * Outputs an object bound to the {@link ExecutionContext}.  The location of the object (context key)
 * must be specified on the "key" attribute.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ContextObjectSerializationUnit implements SerializationUnit, SAXElementVisitor {

    private static Log logger = LogFactory.getLog(ContextObjectSerializationUnit.class);

    public void writeElementStart(Element element, Writer writer, ExecutionContext executionContext) throws IOException {
        String key = getContextKey(element);

        if(key != null) {
            Object object = executionContext.getAttribute(key);

            if(object != null) {
                writer.write(object.toString());
            } else {
                logger.debug("Invalid <context-object> specification at '" + DomUtils.getXPath(element) + "'. No Object instance found on context at '" + key + "'.");
            }
        } else {
            logger.warn("Invalid <context-object> specification at '" + DomUtils.getXPath(element) + "'. 'key' attribute not specified.");
        }
    }

    public static String getContextKey(Element element) {
        return DomUtils.getAttributeValue(element, "key");
    }

    public void writeElementEnd(Element element, Writer writer, ExecutionContext executionContext) throws IOException {
    }

    public void writeElementText(Text text, Writer writer, ExecutionContext executionContext) throws IOException {
    }

    public void writeElementComment(Comment comment, Writer writer, ExecutionContext executionContext) throws IOException {
    }

    public void writeElementEntityRef(EntityReference entityRef, Writer writer, ExecutionContext executionContext) throws IOException {
    }

    public void writeElementCDATA(CDATASection cdata, Writer writer, ExecutionContext executionContext) throws IOException {
    }

    public void writeElementNode(Node node, Writer writer, ExecutionContext executionContext) throws IOException {
    }

    public boolean writeChildElements() {
        return false;
    }

    /**
     * Utility method for creating a &lt;context-object/&gt; element.
     * @param ownerDocument The owner document.
     * @param key The context key.
     * @return The &lt;context-object/&gt; element.
     */
    public static Element createElement(Document ownerDocument, String key) {
        Element resultElement = ownerDocument.createElementNS(Namespace.SMOOKS_URI, "context-object");
        Comment comment = ownerDocument.createComment(" The actual message payload is set on the associated Smooks ExecutionContext under the key '" + key + "'.  Alternatively, you can use Smooks to serialize the message. ");

        resultElement.setAttribute("key", key);
        resultElement.appendChild(comment);

        return resultElement;
    }

    public static boolean isContextObjectElement(Element element) {
        if(DomUtils.getName(element).equals("context-object") && Namespace.SMOOKS_URI.equals(element.getNamespaceURI())) {
            return true;
        }

        return false;
    }

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
    }

    public void onChildText(SAXElement element, SAXText text, ExecutionContext executionContext) throws SmooksException, IOException {
    }

    public void onChildElement(SAXElement element, SAXElement childElement, ExecutionContext executionContext) throws SmooksException, IOException {
    }

    public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
    }
}
