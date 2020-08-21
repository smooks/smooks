/*-
 * ========================LICENSE_START=================================
 * Smooks Core
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
package org.smooks.delivery.dom.serialize;

import org.smooks.SmooksException;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.delivery.sax.SAXVisitBefore;
import org.smooks.xml.DomUtils;
import org.smooks.xml.Namespace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.Writer;

/**
 * Write a &lt;text&gt; element.
 * <p/>
 * Basically just drops the &lt;text&gt; tags. 
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class TextSerializationUnit extends DefaultSerializationUnit implements SAXVisitBefore {

    public void writeElementStart(Element element, Writer writer, ExecutionContext executionContext) throws IOException {
    }

    public void writeElementEnd(Element element, Writer writer, ExecutionContext executionContext) throws IOException {
    }

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
    }

    public static Element createTextElement(Element element, String templatingResult) {
        Document ownerDocument = element.getOwnerDocument();
        Element resultElement = ownerDocument.createElementNS(Namespace.SMOOKS_URI, "text");
        resultElement.appendChild(ownerDocument.createTextNode(templatingResult));
        return resultElement;
    }

    public static boolean isTextElement(Element element) {
        if(DomUtils.getName(element).equals("text") && Namespace.SMOOKS_URI.equals(element.getNamespaceURI())) {
            return true;
        }

        return false;
    }

    public static String getText(Element element) {
        return DomUtils.getAllText(element, false);
    }
}
