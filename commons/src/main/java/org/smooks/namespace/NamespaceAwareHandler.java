/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
 * %%
 * Copyright (C) 2020 Smooks
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
package org.smooks.namespace;

import org.xml.sax.*;
import org.xml.sax.ext.DefaultHandler2;

import java.io.IOException;

/**
 * Namespace aware Content Handler wrapper.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class NamespaceAwareHandler extends DefaultHandler2 {

    private final DefaultHandler2 baseHandler;
    private final NamespaceDeclarationStack namespaceDeclarationStack;

    public NamespaceAwareHandler(DefaultHandler2 baseHandler, NamespaceDeclarationStack namespaceDeclarationStack) {
        this.baseHandler = baseHandler;
        this.namespaceDeclarationStack = namespaceDeclarationStack;
    }

    public void setDocumentLocator(Locator locator) {
        baseHandler.setDocumentLocator(locator);
    }

    public void startDocument() throws SAXException {
        baseHandler.startDocument();
    }

    public void endDocument() throws SAXException {
        baseHandler.endDocument();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        baseHandler.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        baseHandler.endPrefixMapping(prefix);
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        namespaceDeclarationStack.pushNamespaces(qName, uri, atts);
        baseHandler.startElement(uri, localName, qName, atts);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        baseHandler.endElement(uri, localName, qName);
        namespaceDeclarationStack.popNamespaces();
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        baseHandler.characters(ch, start, length);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        baseHandler.ignorableWhitespace(ch, start, length);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        baseHandler.processingInstruction(target, data);
    }

    public void skippedEntity(String name) throws SAXException {
        baseHandler.skippedEntity(name);
    }

    public void notationDecl(String name, String publicId, String systemId) throws SAXException {
        baseHandler.notationDecl(name, publicId, systemId);
    }

    public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {
        baseHandler.unparsedEntityDecl(name, publicId, systemId, notationName);
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        return baseHandler.resolveEntity(publicId, systemId);
    }

    public void warning(SAXParseException exception) throws SAXException {
        baseHandler.warning(exception);
    }

    public void error(SAXParseException exception) throws SAXException {
        baseHandler.error(exception);
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        baseHandler.fatalError(exception);
    }

    public void elementDecl(String name, String model) throws SAXException {
        baseHandler.elementDecl(name, model);
    }

    public void attributeDecl(String eName, String aName, String type, String mode, String value) throws SAXException {
        baseHandler.attributeDecl(eName, aName, type, mode, value);
    }

    public void internalEntityDecl(String name, String value) throws SAXException {
        baseHandler.internalEntityDecl(name, value);
    }

    public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException {
        baseHandler.externalEntityDecl(name, publicId, systemId);
    }

    public InputSource getExternalSubset(String name, String baseURI) throws SAXException, IOException {
        return baseHandler.getExternalSubset(name, baseURI);
    }

    public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) throws SAXException, IOException {
        return baseHandler.resolveEntity(name, publicId, baseURI, systemId);
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        baseHandler.startDTD(name, publicId, systemId);
    }

    public void endDTD() throws SAXException {
        baseHandler.endDTD();
    }

    public void startEntity(String name) throws SAXException {
        baseHandler.startEntity(name);
    }

    public void endEntity(String name) throws SAXException {
        baseHandler.endEntity(name);
    }

    public void startCDATA() throws SAXException {
        baseHandler.startCDATA();
    }

    public void endCDATA() throws SAXException {
        baseHandler.endCDATA();
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        baseHandler.comment(ch, start, length);
    }
}
