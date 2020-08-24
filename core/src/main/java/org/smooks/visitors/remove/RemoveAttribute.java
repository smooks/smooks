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
package org.smooks.visitors.remove;

import org.smooks.SmooksException;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.dom.DOMVisitAfter;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.delivery.sax.SAXVisitBefore;
import org.w3c.dom.Element;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.XMLConstants;
import java.io.IOException;
import java.util.Optional;

/**
 * Remove attribute.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class RemoveAttribute implements SAXVisitBefore, DOMVisitAfter {

    private String qName;
    private String localPart;
    private Optional<String> namespace;

    @PostConstruct
    public void init() {
        int prefixQualifier = qName.indexOf(':');
        if(prefixQualifier != -1) {
            localPart = qName.substring(prefixQualifier + 1);

            // Default the namespace to xmlns if undefined and the prefix is "xmlns"...
            if(!namespace.isPresent() && qName.substring(0, prefixQualifier).equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                namespace = Optional.of(XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
            }
        } else {
            localPart = qName;

            // Default the namespace to xmlns if undefined and the localPart is "xmlns"...
            if(!namespace.isPresent() && localPart.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                namespace = Optional.of(XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
            }
        }
    }

    @Inject
    public RemoveAttribute setName(String attributeName) {
        this.qName = attributeName;
        return this;
    }

    @Inject
    public RemoveAttribute setNamespace(Optional<String> attributeNamespace) {
        this.namespace = attributeNamespace;
        return this;
    }

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        if(namespace .isPresent()) {
            element.removeAttributeNS(namespace.orElse(null), qName);
        } else {
            element.removeAttribute(qName);
        }
    }

    public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
        if(namespace .isPresent()) {
            element.removeAttributeNS(namespace.orElse(null), localPart);
        } else {
            element.removeAttribute(localPart);
        }
    }
}
 
