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
package org.smooks.engine.resource.config.xpath;

import org.smooks.api.delivery.sax.SAXElement;
import org.smooks.api.delivery.sax.SAXText;
import org.smooks.api.resource.visitor.sax.SAXElementVisitor;
import org.smooks.api.resource.visitor.dom.DOMElementVisitor;
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.w3c.dom.Element;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class XPathVisitor implements SAXElementVisitor, DOMElementVisitor {

    public static SAXElement saxVisitedBeforeElementStatic;
    public static SAXElement saxVisitedAfterElementStatic;
    public static Element domVisitedBeforeElementStatic;
    public static Element domVisitedAfterElementStatic;
    public SAXElement saxVisitedBeforeElement;
    public SAXElement saxVisitedAfterElement;
    public Element domVisitedBeforeElement;
    public Element domVisitedAfterElement;

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        saxVisitedBeforeElementStatic = element;
        saxVisitedBeforeElement = element;
    }

    public void onChildText(SAXElement element, SAXText childText, ExecutionContext executionContext) throws SmooksException, IOException {
    }

    public void onChildElement(SAXElement element, SAXElement childElement, ExecutionContext executionContext) throws SmooksException, IOException {
    }

    public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        saxVisitedAfterElementStatic = element;
        saxVisitedAfterElement = element;
    }

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        domVisitedBeforeElementStatic = element;
        domVisitedBeforeElement = element;
    }

    public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
        domVisitedAfterElementStatic = element;
        domVisitedAfterElement = element;
    }

    public SAXElement getSaxVisitedBeforeElement() {
        return saxVisitedBeforeElement;
    }

    public SAXElement getSaxVisitedAfterElement() {
        return saxVisitedAfterElement;
    }

    public Element getDomVisitedBeforeElement() {
        return domVisitedBeforeElement;
    }

    public Element getDomVisitedAfterElement() {
        return domVisitedAfterElement;
    }
}
