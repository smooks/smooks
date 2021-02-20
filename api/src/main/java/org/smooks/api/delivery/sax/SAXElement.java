/*-
 * ========================LICENSE_START=================================
 * Smooks API
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
package org.smooks.api.delivery.sax;

import org.smooks.api.SmooksException;
import org.smooks.api.resource.visitor.sax.SAXVisitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;

import javax.xml.namespace.QName;
import java.io.Writer;
import java.util.List;

/**
 * Element details as described by the SAX even model API.
 * <p/>
 * {@link SAXVisitor} implementations will be passed
 * an instance of this class for each of the event methods of
 * {@link SAXVisitor} implementations.
 * 
 * <h3 id="element-writing">Element Writing/Serialization</h3>
 * Each SAXElement instance has a {@link Writer writer} set on it.
 * {@link SAXVisitor} implementations can take care of
 * serializing the elements at which they are targeted themselves.  Alternatively, they
 * can use the {@link org.smooks.engine.delivery.sax.SAXElementWriterUtil} class.
 * <p/>
 * {@link SAXVisitor} implementations can also control the serialization
 * of their "child elements" by {@link #setWriter(java.io.Writer, SAXVisitor) setting the writer}
 * on the SAXElement instance they receive.  This works because Smooks passes the
 * writer instance that's set on a SAXElement instance to all of the SAXElement
 * instances created for child elements.
 * <p/>
 * Only one {@link SAXVisitor} can have access to the {@link java.io.Writer writer}
 * for any individual {@link SAXElement}.  The first visitor to request access to
 * the writer via the {@link SAXElement#getWriter(SAXVisitor)} method "owns" the writer
 * for that element.  Any other visitors requesting access to get or change the writer
 * will result in a {@link SAXWriterAccessException} being thrown.  In this situation,
 * you need to restructure the offending Smooks configuration and eliminate one of the
 * visitors attempting to gain access to the writer.  If developing a new Visitor,
 * you should annotate the new Visitor class with the {@link javax.inject.Inject @Inject}
 * annotation.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface SAXElement {


    void accumulateText();

    List<SAXText> getText();

    void addText(String text);

    void addText(String text, TextType type);

    String getTextContent() throws SmooksException;

    Writer getWriter(SAXVisitor visitor) throws SAXWriterAccessException;

    void setWriter(Writer writer, SAXVisitor visitor) throws SAXWriterAccessException;

    boolean isWriterOwner(SAXVisitor visitor);

    QName getName();

    void setName(QName name);

    Attributes getAttributes();

    void setAttributes(Attributes attributes);

    String getAttribute(String attribute);

    String getAttributeNS(String namespaceURI, String attribute);

    void setAttribute(String attribute, String value);

    void setAttributeNS(String namespaceURI, String name, String value);

    void removeAttribute(String name);

    void removeAttributeNS(String namespaceURI, String name);

    Object getCache(SAXVisitor visitor);

    void setCache(SAXVisitor visitor, Object cache);

    SAXElement getParent();

    void setParent(SAXElement parent);

    Element toDOMElement(Document document);
}
