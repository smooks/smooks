/*-
 * ========================LICENSE_START=================================
 * Core
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
package org.smooks.io;

import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.stax.InputFactoryImpl;
import org.smooks.api.SmooksException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.Writer;

public class SAXWriter extends Writer {
    protected static final InputFactoryImpl ASYNC_XML_INPUT_FACTORY;

    protected final ContentHandler contentHandler;
    protected final AsyncXMLStreamReader<AsyncByteArrayFeeder> asyncXMLStreamReader;
    protected final LexicalHandler lexicalHandler;

    static {
        ASYNC_XML_INPUT_FACTORY = new InputFactoryImpl();
        ASYNC_XML_INPUT_FACTORY.configureForLowMemUsage();
    }
    
    public SAXWriter(final ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
        lexicalHandler = contentHandler instanceof LexicalHandler ? (LexicalHandler) contentHandler : null;
        asyncXMLStreamReader = ASYNC_XML_INPUT_FACTORY.createAsyncForByteArray();
    }
    
    @Override
    public void write(final char[] cbuf, final int off, final int len) throws IOException {
        try {
            asyncXMLStreamReader.getInputFeeder().feedInput(new String(cbuf).getBytes(), off, len);
            while (true) {
                int event = asyncXMLStreamReader.next();
                if (AsyncXMLStreamReader.EVENT_INCOMPLETE == event) {
                    break;
                }
                switch (event) {
                    case XMLStreamConstants.START_DOCUMENT:
                        contentHandler.startDocument();
                        break;
                    case XMLStreamConstants.START_ELEMENT:
                        for (int i = 0; i < asyncXMLStreamReader.getNamespaceCount(); i++) {
                            contentHandler.startPrefixMapping(asyncXMLStreamReader.getNamespacePrefix(i), asyncXMLStreamReader.getNamespaceURI(i));
                        }
                        final AttributesImpl saxAttributes = new AttributesImpl();
                        for (int i = 0, n = asyncXMLStreamReader.getAttributeCount(); i < n; ++i) {
                            saxAttributes.addAttribute(asyncXMLStreamReader.getAttributeName(i).getNamespaceURI(), asyncXMLStreamReader.getAttributeName(i).getLocalPart(), asyncXMLStreamReader.getAttributeName(i).getPrefix() + ":" + asyncXMLStreamReader.getAttributeName(i).getLocalPart(), asyncXMLStreamReader.getAttributeType(i), asyncXMLStreamReader.getAttributeValue(i));
                        }
                        for (int i = 0, n = asyncXMLStreamReader.getNamespaceCount(); i < n; ++i) {
                            saxAttributes.addAttribute(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, asyncXMLStreamReader.getNamespacePrefix(i), XMLConstants.XMLNS_ATTRIBUTE + ":" + asyncXMLStreamReader.getNamespacePrefix(i), "CDATA", asyncXMLStreamReader.getNamespaceURI(i));
                        }
                        if (asyncXMLStreamReader.getName().getNamespaceURI().equals(XMLConstants.NULL_NS_URI)) {
                            contentHandler.startElement(asyncXMLStreamReader.getName().getNamespaceURI(), asyncXMLStreamReader.getName().getLocalPart(), asyncXMLStreamReader.getName().getLocalPart(), saxAttributes);
                        } else {
                            contentHandler.startElement(asyncXMLStreamReader.getName().getNamespaceURI(), asyncXMLStreamReader.getName().getLocalPart(), asyncXMLStreamReader.getName().getPrefix() + ":" + asyncXMLStreamReader.getName().getLocalPart(), saxAttributes);
                        }
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        contentHandler.characters(asyncXMLStreamReader.getText().toCharArray(), asyncXMLStreamReader.getTextStart(), asyncXMLStreamReader.getTextLength());
                        break;
                    case XMLStreamConstants.CDATA:
                        if (lexicalHandler != null) {
                            lexicalHandler.startCDATA();
                        }
                        contentHandler.characters(asyncXMLStreamReader.getText().toCharArray(), asyncXMLStreamReader.getTextStart(), asyncXMLStreamReader.getTextLength());
                        if (lexicalHandler != null) {
                            lexicalHandler.endCDATA();
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        if (asyncXMLStreamReader.getName().getNamespaceURI().equals(XMLConstants.NULL_NS_URI)) {
                            contentHandler.endElement(asyncXMLStreamReader.getName().getNamespaceURI(), asyncXMLStreamReader.getName().getLocalPart(), asyncXMLStreamReader.getName().getLocalPart());
                        } else {
                            contentHandler.endElement(asyncXMLStreamReader.getName().getNamespaceURI(), asyncXMLStreamReader.getName().getLocalPart(), asyncXMLStreamReader.getName().getPrefix() + ":" + asyncXMLStreamReader.getName().getLocalPart());
                        }
                        for (int i = 0; i < asyncXMLStreamReader.getNamespaceCount(); i++) {
                            contentHandler.endPrefixMapping(asyncXMLStreamReader.getNamespacePrefix(i));
                        }
                        break;
                    case XMLStreamConstants.END_DOCUMENT:
                        contentHandler.endDocument();
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
            }
        } catch (SAXException | XMLStreamException e) {
            throw new SmooksException(e);
        }
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public void close() throws IOException {
        try {
            asyncXMLStreamReader.close();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }
}