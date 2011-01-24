/*
 * Milyn - Copyright (C) 2006 - 2011
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (version 2.1) as published by the Free Software
 * Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */

package org.milyn.javabean.binding.xml;

import org.milyn.javabean.binding.SerializationContext;
import org.milyn.javabean.binding.model.Bean;
import org.milyn.xml.XmlUtil;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.Writer;

/**
 * XML Attribute Serialization Node.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class XMLAttributeSerializationNode extends XMLSerializationNode {

    private Bean model;

    public XMLAttributeSerializationNode(QName qName) {
        super(qName);
    }

    @Override
    public void serialize(Writer outputStream, SerializationContext context) throws IOException {
        String value = getValue(context);

        if(value != null) {
            outputStream.write(" ");
            writeName(outputStream);
            outputStream.write("=\"");
            char[] characters = value.toCharArray();
            XmlUtil.encodeAttributeValue(characters, 0, characters.length, outputStream);
            outputStream.write('"');
        }
    }

    @Override
    protected Object clone() {
        XMLAttributeSerializationNode clone = new XMLAttributeSerializationNode(qName);
        copyProperties(clone);
        return clone;
    }
}
