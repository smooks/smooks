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

import org.milyn.xml.HTMLEntityLookup;
import org.milyn.xml.XmlUtil;
import org.milyn.SmooksException;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * SAX Text.
 * <p/>
 * This class provides a wrapper around the char array supplied by the
 * SAXParser/XMLReader.  It help provide an optimization by allowing the process
 * to avoid String construction where possible.
 * <p/>
 * <i><b><u>NOTE</u></b>: References to instances of this type should not be cached.  If you
 * need to cache the character data housed in this class, you should use either the
 * {@link #getText()} or {@link #toString()} methods.</i>
 *
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SAXText {

    private char[] characters;
    private int offset;
    private int length;
    private TextType type;

    protected SAXText() {
    }

    protected SAXText(String text, TextType type) {
        setText(text.toCharArray(), 0, text.length(), type);
    }

    public SAXText(char[] characters, int offset, int length, TextType type) {
        setText(characters, offset, length, type);
    }

    protected void setText(char[] characters, int offset, int length, TextType type) {
        this.characters = characters;
        this.offset = offset;
        this.length = length;
        this.type = type;
    }

    /**
     * Get the raw text, unwrapped.
     * <p/>
     * This method differs from the {@link #toString()} implementation because
     * it doesn't wrap the test based on it's {@link #getType() type}.
     *
     * @return The raw (unwrapped) text.
     */
    public String getText() {
        return new String(characters, offset, length);
    }

    /**
     * Get the text type (comment, cdata etc).
     *
     * @return The text type.
     */
    public TextType getType() {
        return type;
    }

    /**
     * Get the "wrapped" text as a String.
     * <p/>
     * Wraps the underlying characters based on the text {@link #getType() type}.
     * See {@link #getType()}. 
     *
     * @return The "wrapped" text String.
     */
    public String toString() {
        StringWriter writer = new StringWriter(characters.length + 12);
        try {
            toWriter(writer);
        } catch (IOException e) {
            throw new SmooksException("Unexpected IOException writing to a StringWriter.");
        }
        return writer.toString();
    }

    /**
     * Get the underlying character buffer.
     * @return The underlying character buffer.
     * @see #getOffset()
     * @see #getLength()
     */
    public char[] getCharacters() {
        return characters;
    }

    /**
     * Get the character offset (in the {@link #getCharacters() character buffer}) of the text a
     * associated with this SAXText instance.
     * @return The character offset.
     * @see #getCharacters()
     * @see #getLength()
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Get the character buffer length (in the {@link #getCharacters() character buffer}) of the text a
     * associated with this SAXText instance.
     * @return The character buffer length.
     * @see #getCharacters() 
     * @see #getOffset()
     */
    public int getLength() {
        return length;
    }

    /**
     * Write the text to the supplied writer.
     * <p/>
     * It wraps the text based on its {@link #getType() type}.
     *
     * @param writer The writer.
     * @throws IOException Write exception.
     */
    public void toWriter(Writer writer) throws IOException {
        toWriter(writer, true);
    }

    /**
     * Write the text to the supplied writer.
     * <p/>
     * It wraps the text based on its {@link #getType() type}.
     *
     * @param writer The writer.
     * @param encodeSpecialChars Encode special XML characters.
     * @throws IOException Write exception.
     */
    public void toWriter(Writer writer, boolean encodeSpecialChars) throws IOException {
        if(writer != null) {
            if(type == TextType.TEXT) {
                if(encodeSpecialChars) {
                    XmlUtil.encodeTextValue(characters, offset, length, writer);
                } else {
                    writer.write(characters, offset, length);
                }
            } else if(type == TextType.COMMENT) {
                writer.write("<!--");
                writer.write(characters, offset, length);
                writer.write("-->");
            } else if(type == TextType.CDATA) {
                writer.write("<![CDATA[");
                writer.write(characters, offset, length);
                writer.write("]]>");
            } else if(type == TextType.ENTITY) {
                writer.write("&");
                writer.write(HTMLEntityLookup.getEntityRef(characters[0]));
                writer.write(';');
            }
        }
    }

    /**
     * Clone this SAXText object.
     * @return A cloned copy of this SAXText object.
     */
    protected Object clone() {
        SAXText clone = new SAXText();

        clone.characters = new char[length];
        System.arraycopy(characters, offset, clone.characters, 0, length);
        clone.offset = 0;
        clone.length = length;
        clone.type = type;

        return clone;
    }
}
