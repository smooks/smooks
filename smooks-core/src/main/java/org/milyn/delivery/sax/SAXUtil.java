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

import org.milyn.commons.xml.DomUtils;
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
     *
     * @param attributeName The attribute name.
     * @param attributes    The attribute list.
     * @return The attribute value, or an empty string if not available (as with DOM).
     */
    public static String getAttribute(String attributeName, Attributes attributes) {
        return getAttribute(attributeName, attributes, "");
    }

    /**
     * Get the value of the named attribute.
     *
     * @param attributeName The attribute name.
     * @param attributes    The attribute list.
     * @param defaultVal    The default value, if the attribute is not set.
     * @return The attribute value, or an empty string if not available (as with DOM).
     */
    public static String getAttribute(String attributeName, Attributes attributes, String defaultVal) {
        int attribCount = attributes.getLength();

        for (int i = 0; i < attribCount; i++) {
            String attribName = attributes.getLocalName(i);
            if (attribName.equalsIgnoreCase(attributeName)) {
                return attributes.getValue(i);
            }
        }

        return defaultVal;
    }

    /**
     * Get the value of the named attribute.
     *
     * @param attributeNamespace The attribute namespace.
     * @param attributeName      The attribute name.
     * @param attributes         The attribute list.
     * @param defaultVal         The default value, if the attribute is not set.
     * @return The attribute value, or an empty string if not available (as with DOM).
     */
    public static String getAttribute(String attributeNamespace, String attributeName, Attributes attributes, String defaultVal) {
        int attribCount = attributes.getLength();

        for (int i = 0; i < attribCount; i++) {
            String attribName = attributes.getLocalName(i);
            if (attribName.equalsIgnoreCase(attributeName)) {
                if (attributes.getURI(i).equals(attributeNamespace)) {
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
        if (builder.length() > 0) {
            builder.insert(0, "/");
            builder.insert(0, element.getName().getLocalPart());
        } else {
            builder.append(element.getName().getLocalPart());
        }

        SAXElement parent = element.getParent();
        if (parent != null) {
            addXPathElement(parent, builder);
        }
    }

    public static int getDepth(SAXElement element) {
        int depth = 0;

        SAXElement parent = element.getParent();
        while (parent != null) {
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
     *
     * @param element The element.
     * @return Element QName.
     */
    public static QName toQName(Element element) {
        if (element == null) {
            return null;
        }

        return toQName(element.getNamespaceURI(), DomUtils.getName(element), element.getNodeName());
    }

    static void thowInvalidNameException(String namespaceURI, String localName, String qName) {
        throw new IllegalArgumentException("Invalid QName: namespaceURI='" + namespaceURI + "', localName='" + localName + "', qName='" + qName + "'.");
    }
}
