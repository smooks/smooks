/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0 or,
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
package org.smooks.delivery.sax;

import org.smooks.xml.DomUtils;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;

import javax.xml.namespace.QName;

/**
 * SAX utility methods.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class SAXUtil {

    /**
     * Get the value of the named attribute.
     * @param attributeName The attribute name.
     * @param attributes The attribute list.
     * @return The attribute value, or an empty string if not available (as with DOM).
     */
    public static String getAttribute(String attributeName, Attributes attributes) {
        return getAttribute(attributeName, attributes, "");
    }

    /**
     * Get the value of the named attribute.
     * @param attributeName The attribute name.
     * @param attributes The attribute list.
     * @param defaultVal The default value, if the attribute is not set.
     * @return The attribute value, or an empty string if not available (as with DOM).
     */
    public static String getAttribute(String attributeName, Attributes attributes, String defaultVal) {
        int attribCount = attributes.getLength();

        for(int i = 0; i < attribCount; i++) {
            String attribName = attributes.getLocalName(i);
            if(attribName.equalsIgnoreCase(attributeName)) {
                return attributes.getValue(i);
            }
        }

        return defaultVal;
    }

    /**
     * Get the value of the named attribute.
     * @param attributeNamespace The attribute namespace.
     * @param attributeName The attribute name.
     * @param attributes The attribute list.
     * @param defaultVal The default value, if the attribute is not set.
     * @return The attribute value, or an empty string if not available (as with DOM).
     */
    public static String getAttribute(String attributeNamespace, String attributeName, Attributes attributes, String defaultVal) {
        int attribCount = attributes.getLength();

        for(int i = 0; i < attribCount; i++) {
            String attribName = attributes.getLocalName(i);
            if(attribName.equalsIgnoreCase(attributeName)) {
                if(attributes.getURI(i).equals(attributeNamespace)) {
                    return attributes.getValue(i);
                }
            }
        }

        return defaultVal;
    }

    public static String getXPath(SAXElement element) {
        StringBuilder builder = new StringBuilder();

        addXPathElement(element, builder);

        return builder.toString();
    }

    private static void addXPathElement(SAXElement element, StringBuilder builder) {
        if(builder.length() > 0) {
            builder.insert(0, "/");
            builder.insert(0, element.getName().getLocalPart());
        } else {
            builder.append(element.getName().getLocalPart());
        }

        SAXElement parent = element.getParent();
        if(parent != null) {
            addXPathElement(parent, builder);
        }
    }

    public static int getDepth(SAXElement element) {
        int depth = 0;

        SAXElement parent = element.getParent();
        while(parent != null) {
            depth++;
            parent = parent.getParent();
        }

        return depth;
    }

    /**
     * Create a {@link javax.xml.namespace.QName} instance from the supplied element naming parameters.
     *
     * @param namespaceURI The Namespace URI, or the empty string if the
     *                     element has no Namespace URI or if Namespace
     *                     processing is not being performed.
     * @param localName    The local name (without prefix), or the
     *                     empty string if Namespace processing is not being
     *                     performed.
     * @param qName        The qualified name (with prefix), or the
     *                     empty string if qualified names are not available.
     * @return A {@link javax.xml.namespace.QName} instance representing the element named by the supplied parameters.
     */
    public static QName toQName(String namespaceURI, String localName, String qName) {
        if (namespaceURI != null) {
            int colonIndex;

            if (namespaceURI.length() != 0 && qName != null && (colonIndex = qName.indexOf(':')) != -1) {
                String prefix = qName.substring(0, colonIndex);
                String qNameLocalName = qName.substring(colonIndex + 1);

                return new QName(namespaceURI.intern(), qNameLocalName, prefix);
            } else if (localName != null && localName.length() != 0) {
                return new QName(namespaceURI, localName);
            } else if (qName != null && qName.length() != 0) {
                return new QName(namespaceURI, qName);
            } else {
                thowInvalidNameException(namespaceURI, localName, qName);
            }
        } else if (localName != null && localName.length() != 0) {
            return new QName(localName);
        } else {
            thowInvalidNameException(namespaceURI, localName, qName);
        }

        return null;
    }

    /**
     * Create a {@link QName} instance for the supplied DOM {@link org.w3c.dom.Element}.
     * @param element The element.
     * @return Element QName.
     */
    public static QName toQName(Element element) {
        if(element == null) {
            return null;
        }

        return toQName(element.getNamespaceURI(), DomUtils.getName(element), element.getNodeName());
    }

    static void thowInvalidNameException(String namespaceURI, String localName, String qName) {
        throw new IllegalArgumentException("Invalid QName: namespaceURI='" + namespaceURI + "', localName='" + localName + "', qName='" + qName + "'.");
    }
}
