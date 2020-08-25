/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.delivery.dom.serialize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.SmooksException;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.delivery.sax.SAXElementVisitor;
import org.smooks.delivery.sax.SAXText;
import org.smooks.xml.DomUtils;
import org.smooks.xml.Namespace;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextObjectSerializationUnit.class);

    public void writeElementStart(Element element, Writer writer, ExecutionContext executionContext) throws IOException {
        String key = getContextKey(element);

        if(key != null) {
            Object object = executionContext.getAttribute(key);

            if(object != null) {
                writer.write(object.toString());
            } else {
                LOGGER.debug("Invalid <context-object> specification at '" + DomUtils.getXPath(element) + "'. No Object instance found on context at '" + key + "'.");
            }
        } else {
            LOGGER.warn("Invalid <context-object> specification at '" + DomUtils.getXPath(element) + "'. 'key' attribute not specified.");
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
        return DomUtils.getName(element).equals("context-object") && Namespace.SMOOKS_URI.equals(element.getNamespaceURI());
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
