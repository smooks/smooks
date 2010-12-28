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
package org.milyn.visitors.remove;

import org.milyn.SmooksException;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.delivery.dom.DOMVisitAfter;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXVisitBefore;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import java.io.IOException;

/**
 * Remove attribute.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class RemoveAttribute implements SAXVisitBefore, DOMVisitAfter {

    private String qName;
    private String localPart;
    private String namespace;

    @Initialize
    public void init() {
        int prefixQualifier = qName.indexOf(':');
        if(prefixQualifier != -1) {
            localPart = qName.substring(prefixQualifier + 1);

            // Default the namespace to xmlns if undefined and the prefix is "xmlns"...
            if(namespace == null && qName.substring(0, prefixQualifier).equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                namespace = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
            }
        } else {
            localPart = qName;

            // Default the namespace to xmlns if undefined and the localPart is "xmlns"...
            if(namespace == null && localPart.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                namespace = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
            }
        }
    }

    @ConfigParam
    public RemoveAttribute setName(String attributeName) {
        this.qName = attributeName;
        return this;
    }

    @ConfigParam (use = ConfigParam.Use.OPTIONAL)
    public RemoveAttribute setNamespace(String attributeNamespace) {
        this.namespace = attributeNamespace;
        return this;
    }

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        if(namespace != null) {
            element.removeAttributeNS(namespace, qName);
        } else {
            element.removeAttribute(qName);
        }
    }

    public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
        if(namespace != null) {
            element.removeAttributeNS(namespace, localPart);
        } else {
            element.removeAttribute(localPart);
        }
    }
}
 