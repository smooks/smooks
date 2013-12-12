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

import org.milyn.commons.xml.HTMLEntityLookup;
import org.milyn.commons.xml.XmlUtil;
import org.xml.sax.Attributes;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.Writer;

/**
 * {@link SAXElement} XML writing/serialization utility class.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SAXElementWriterUtil {

    public static void writeEmptyElement(SAXElement element, Writer writer, boolean encodeSpecialChars) throws IOException {
        if (writer != null) {
            writeUnclosedElement(element, writer, encodeSpecialChars);
            writer.write(" />");
        }
    }

    public static void writeStartElement(SAXElement element, Writer writer, boolean encodeSpecialChars) throws IOException {
        if (writer != null) {
            writeUnclosedElement(element, writer, encodeSpecialChars);
            writer.write(">");
        }
    }

    public static void writeEndElement(SAXElement element, Writer writer) throws IOException {
        if (writer != null) {
            QName name = element.getName();
            String prefix = name.getPrefix();

            writer.write("</");
            if (prefix != null && !prefix.equals(XMLConstants.NULL_NS_URI)) {
                writer.write(prefix);
                writer.write(':');
            }
            writer.write(name.getLocalPart());
            writer.write('>');
        }
    }

    public static void writeText(String text, TextType textType, Writer writer) throws IOException {
        if (writer != null) {
            switch (textType) {
                case TEXT:
                    writer.write(text);
                    break;
                case CDATA:
                    writer.write("<![CDATA[");
                    writer.write(text);
                    writer.write("]]>");
                    break;
                case COMMENT:
                    writer.write("<!--");
                    writer.write(text);
                    writer.write("-->");
                    break;
                case ENTITY:
                    writer.write("&");
                    writer.write(HTMLEntityLookup.getEntityRef(text.charAt(0)));
                    writer.write(';');
                    break;
            }
        }
    }

    public static void writeText(SAXText text, Writer writer) throws IOException {
        if (writer != null) {
            switch (text.getType()) {
                case TEXT:
                    writer.write(text.getCharacters(), text.getOffset(), text.getLength());
                    break;
                case CDATA:
                    writer.write("<![CDATA[");
                    writer.write(text.getCharacters(), text.getOffset(), text.getLength());
                    writer.write("]]>");
                    break;
                case COMMENT:
                    writer.write("<!--");
                    writer.write(text.getCharacters(), text.getOffset(), text.getLength());
                    writer.write("-->");
                    break;
                case ENTITY:
                    writer.write("&");
                    writer.write(HTMLEntityLookup.getEntityRef(text.getCharacters()[text.getOffset()]));
                    writer.write(';');
                    break;
            }
        }
    }

    private static void writeUnclosedElement(SAXElement element, Writer writer, boolean encodeSpecialChars) throws IOException {
        QName name = element.getName();
        String prefix = name.getPrefix();

        writer.write('<');
        if (prefix != null && !prefix.equals(XMLConstants.NULL_NS_URI)) {
            writer.write(prefix);
            writer.write(':');
        }
        writer.write(name.getLocalPart());
        writeAttributes(element.getAttributes(), writer, encodeSpecialChars);
    }

    private static void writeAttributes(Attributes attributes, Writer writer, boolean encodeSpecialChars) throws IOException {
        if (attributes != null) {
            int attribCount = attributes.getLength();

            for (int i = 0; i < attribCount; i++) {
                String attQName = attributes.getQName(i);
                String attValue = attributes.getValue(i);

                writer.write(' ');
                if (attQName != null && attQName.length() > 0) {
                    writer.write(attQName);
                } else {
                    writer.write(attributes.getLocalName(i));
                }
                if (encodeSpecialChars) {
                    writer.write("=\"");
                    XmlUtil.encodeAttributeValue(attValue.toCharArray(), 0, attValue.length(), writer);
                    writer.write('\"');
                } else {
                    if (attValue.indexOf('"') != -1) {
                        writer.write("=\'");
                        writer.write(attValue);
                        writer.write('\'');
                    } else {
                        writer.write("=\"");
                        writer.write(attValue);
                        writer.write('\"');
                    }
                }
            }
        }
    }
}
