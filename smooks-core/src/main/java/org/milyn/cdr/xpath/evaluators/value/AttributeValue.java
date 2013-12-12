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
package org.milyn.cdr.xpath.evaluators.value;

import org.milyn.commons.javabean.DataDecoder;
import org.milyn.delivery.sax.SAXElement;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 * Element text value getter.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class AttributeValue extends Value {

    private String namespaceURI;
    private String localPart;
    private DataDecoder decoder;
    private static final String EMPTY_STRING = "";

    public AttributeValue(String namespaceURI, String localPart, DataDecoder decoder) {
        this.namespaceURI = namespaceURI;
        this.localPart = localPart;
        this.decoder = decoder;
    }

    public Object getValue(SAXElement element) {
        String attribValue;
        if (namespaceURI != null) {
            attribValue = element.getAttributeNS(namespaceURI, localPart);
        } else {
            attribValue = element.getAttribute(localPart);
        }
        return decoder.decode(attribValue);
    }

    public Object getValue(Element element) {
        String attribValue = EMPTY_STRING;

        if (namespaceURI != null) {
            attribValue = element.getAttributeNS(namespaceURI, localPart);
        } else {
            NamedNodeMap attributes = element.getAttributes();
            int numAttributes = attributes.getLength();

            for (int i = 0; i < numAttributes; i++) {
                Attr attr = (Attr) attributes.item(i);
                String attrName = attr.getLocalName();

                if (attrName == null) {
                    attrName = attr.getName();
                }

                if (attrName.equals(localPart)) {
                    attribValue = attr.getValue();
                    break;
                }
            }
        }

        return decoder.decode(attribValue);
    }

    public String toString() {
        if (namespaceURI != null) {
            return "@{" + namespaceURI + "}" + localPart;
        } else {
            return "@" + localPart;
        }
    }
}