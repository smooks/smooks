/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 - 2021 Smooks
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
package org.smooks.engine.delivery.sax;

import org.smooks.api.SmooksException;
import org.smooks.api.delivery.sax.SAXText;
import org.smooks.api.delivery.sax.TextType;
import org.smooks.xml.HTMLEntityLookup;
import org.smooks.xml.XmlUtil;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class DefaultSAXText implements SAXText {
    private char[] characters;
    private int offset;
    private int length;
    private TextType type;

    public DefaultSAXText() {
    }

    public DefaultSAXText(String text, TextType type) {
        setText(text.toCharArray(), 0, text.length(), type);
    }

    public DefaultSAXText(char[] characters, int offset, int length, TextType type) {
        setText(characters, offset, length, type);
    }

    @Override
    public void setText(char[] characters, int offset, int length, TextType type) {
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
    @Override
    public String getText() {
        return new String(characters, offset, length);
    }

    /**
     * Get the text type (comment, cdata etc).
     *
     * @return The text type.
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public void toWriter(Writer writer, boolean encodeSpecialChars) throws IOException {
        if(writer != null) {
            switch(type) {
                case TEXT: {
                    if(encodeSpecialChars) {
                        XmlUtil.encodeTextValue(characters, offset, length, writer);
                    } else {
                        writer.write(characters, offset, length);
                    }
                    break;
                }
                case COMMENT: {
                    writer.write("<!--");
                    writer.write(characters, offset, length);
                    writer.write("-->");
                    break;
                }
                case CDATA: {
                    writer.write("<![CDATA[");
                    writer.write(characters, offset, length);
                    writer.write("]]>");
                    break;
                }
                case ENTITY: {
                    if(encodeSpecialChars) {
                        writer.write("&");
                        writer.write(HTMLEntityLookup.getEntityRef(characters[offset]));
                        writer.write(';');
                    } else {
                        writer.write(characters, offset, 1);
                    }
                    break;
                }
            }
        }
    }

    /**
     * Clone this SAXText object.
     * @return A cloned copy of this SAXText object.
     */
    @Override
    public SAXText copy() {
        DefaultSAXText clone = new DefaultSAXText();

        clone.characters = new char[length];
        System.arraycopy(characters, offset, clone.characters, 0, length);
        clone.offset = 0;
        clone.length = length;
        clone.type = type;

        return clone;
    }
}
