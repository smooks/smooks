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
package org.smooks.cdr.xpath.evaluators.value;

import org.smooks.datatype.factory.DataTypeProviderFactory;
import org.smooks.delivery.sax.SAXElement;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 * Element text value getter.
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class AttributeValue extends Value {

    private static final String EMPTY_STRING = "";

    private final String namespaceURI;
    private final String localPart;
    private final DataTypeProviderFactory<String, ?> dataTypeProviderFactory;

    public AttributeValue(String namespaceURI, String localPart, DataTypeProviderFactory<String, ?> dataTypeProviderFactory) {
        this.namespaceURI = namespaceURI;
        this.localPart = localPart;
        this.dataTypeProviderFactory = dataTypeProviderFactory;
    }

    public Object getValue(SAXElement element) {
        String attribValue;
        if(namespaceURI != null) {
            attribValue = element.getAttributeNS(namespaceURI, localPart);
        } else {
            attribValue = element.getAttribute(localPart);
        }
        return dataTypeProviderFactory.createProvider(attribValue).get();
    }

    public Object getValue(Element element) {
        String attribValue = EMPTY_STRING;

        if(namespaceURI != null) {
            attribValue = element.getAttributeNS(namespaceURI, localPart);
        } else {
            NamedNodeMap attributes = element.getAttributes();
            int numAttributes = attributes.getLength();

            for(int i = 0; i < numAttributes; i++) {
                Attr attr = (Attr) attributes.item(i);
                String attrName = attr.getLocalName();

                if(attrName == null) {
                    attrName = attr.getName();
                }

                if(attrName.equals(localPart)) {
                    attribValue = attr.getValue();
                    break;
                }
            }
        }

        return dataTypeProviderFactory.createProvider(attribValue).get();
    }

    public String toString() {
        if(namespaceURI != null) {
            return "@{" + namespaceURI + "}" + localPart;
        } else {
            return "@" + localPart;
        }
    }
}
