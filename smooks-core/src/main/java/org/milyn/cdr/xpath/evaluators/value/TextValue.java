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

import org.milyn.delivery.sax.SAXElement;
import org.milyn.javabean.DataDecoder;
import org.milyn.cdr.xpath.evaluators.value.Value;
import org.w3c.dom.Element;

/**
 * Element text value getter.
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class TextValue extends Value {

    private DataDecoder decoder;

    public TextValue(DataDecoder decoder) {
        this.decoder = decoder;
    }

    public Object getValue(SAXElement element) {
        return decoder.decode(element.getTextContent());
    }

    public Object getValue(Element element) {
        return decoder.decode(element.getTextContent());
    }

    public String toString() {
        return "text()";
    }
}
