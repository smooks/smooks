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
package org.smooks.visitors.set;

import org.smooks.SmooksException;
import org.smooks.cdr.Parameter;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.cdr.annotation.ConfigParam;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.Filter;
import org.smooks.delivery.annotation.Initialize;
import org.smooks.delivery.dom.DOMVisitAfter;
import org.smooks.delivery.sax.DefaultSAXElementSerializer;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.util.FreeMarkerTemplate;
import org.smooks.xml.DomUtils;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Set Element Data visitor.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SetElementData extends DefaultSAXElementSerializer implements DOMVisitAfter {

    protected static final String ATTRIBUTE_DATA = "attributeData";

    private String elementQName;
    private String elementName;
    private String elementNamespace;
    private String elementNamespacePrefix;
    private Map<QName, FreeMarkerTemplate> attributes = new LinkedHashMap<QName, FreeMarkerTemplate>();

    @Inject
    private SmooksResourceConfiguration resourceConfig;

    @Initialize
    public void init() {
        if(elementQName != null) {
            int nsPrefixIdx = elementQName.indexOf(":");
            if(nsPrefixIdx != -1) {
                elementNamespacePrefix = elementQName.substring(0, nsPrefixIdx);
                elementName = elementQName.substring(nsPrefixIdx + 1);

                if(elementNamespacePrefix.equals(XMLConstants.XMLNS_ATTRIBUTE) && elementNamespace == null) {
                    elementNamespace = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
                }
            } else {
                elementName = elementQName;

                if(elementName.equals(XMLConstants.XMLNS_ATTRIBUTE) && elementNamespace == null) {
                    elementNamespace = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
                }
            }
        }

        List<Parameter> attributeDataParams = resourceConfig.getParameters(ATTRIBUTE_DATA);
        if(attributeDataParams != null && !attributeDataParams.isEmpty()) {
            extractAttributeData(attributeDataParams);
        }
    }

    private void extractAttributeData(List<Parameter> attributeDataParams) {
        for(Parameter attributeDataParam : attributeDataParams) {
            Element attributeElement = attributeDataParam.getXml();
            String name = attributeElement.getAttribute("name");
            String namespace = attributeElement.getAttribute("namespace");
            String value = attributeElement.getAttribute("value");
            int prefixTokIdx = name.indexOf(':');
            QName qName;

            if(prefixTokIdx != -1) {
                String prefix = name.substring(0, prefixTokIdx);

                if(prefix.equals(XMLConstants.XMLNS_ATTRIBUTE) && (namespace.equals(""))) {
                    namespace = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
                }
                qName = new QName(namespace, name.substring(prefixTokIdx + 1), prefix);
            } else {
                if(name.equals(XMLConstants.XMLNS_ATTRIBUTE) && (namespace.equals(""))) {
                    namespace = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
                }
                qName = new QName(namespace, name);
            }

            attributes.put(qName, new FreeMarkerTemplate(value));
        }
    }

    @ConfigParam (name = "name", use = ConfigParam.Use.OPTIONAL)
    public SetElementData setElementName(String elementQName) {
        this.elementQName = elementQName;
        return this;
    }

    @ConfigParam (name = "namespace", use = ConfigParam.Use.OPTIONAL)
    public SetElementData setElementNamespace(String namespace) {
        this.elementNamespace = namespace;
        return this;
    }

    public SetElementData setAttribute(QName name, String valueTemplate) {
        attributes.put(name, new FreeMarkerTemplate(valueTemplate));
        return this;
    }

    @Override
    protected void writeStart(SAXElement element) throws IOException {
        SAXElement reconstructedElement = reconstructElement(element);
        super.writeStart(reconstructedElement);
        element.setCache(this, reconstructedElement.getCache(this));
    }

    @Override
    protected void writeEnd(SAXElement element) throws IOException {
        SAXElement reconstructedElement = reconstructElement(element);
        reconstructedElement.setCache(this, element.getCache(this));
        super.writeEnd(reconstructedElement);
    }

    private SAXElement reconstructElement(SAXElement element) {
        QName qName = element.getName();

        if(elementQName != null || elementNamespace != null) {
            // Need to create a new QName for the element...
            String newElementName = (elementName != null? elementName : qName.getLocalPart());
            String newElementNamespace = (elementNamespace != null? elementNamespace : qName.getNamespaceURI());
            String newElementNamespacePrefix = (elementNamespacePrefix != null? elementNamespacePrefix : qName.getPrefix());

            qName = new QName(newElementNamespace, newElementName, newElementNamespacePrefix);
        }

        SAXElement newElement = new SAXElement(qName, element.getAttributes(), element.getParent());
        newElement.setWriter(element.getWriter(this), this);

        if(!attributes.isEmpty()) {
            Map<String, Object> beans = Filter.getCurrentExecutionContext().getBeanContext().getBeanMap();
            Set<Map.Entry<QName, FreeMarkerTemplate>> attributeSet = attributes.entrySet();

            for(Map.Entry<QName, FreeMarkerTemplate> attributeConfig : attributeSet) {
                QName attribName = attributeConfig.getKey();
                FreeMarkerTemplate valueTemplate = attributeConfig.getValue();
                String namespaceURI = attribName.getNamespaceURI();

                if(namespaceURI != null) {
                    String prefix = attribName.getPrefix();
                    if(prefix != null && prefix.length() > 0) {
                        newElement.setAttributeNS(namespaceURI, prefix + ":" + attribName.getLocalPart(), valueTemplate.apply(beans));
                    } else {
                        newElement.setAttributeNS(namespaceURI, attribName.getLocalPart(), valueTemplate.apply(beans));
                    }
                } else {
                    newElement.setAttribute(attribName.getLocalPart(), valueTemplate.apply(beans));
                }
            }
        }

        return newElement;
    }

    public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
        if(elementQName != null || elementNamespace != null) {
            String newElementName = (elementQName != null? elementQName : element.getTagName());
            String newElementNamespace = (elementNamespace != null? elementNamespace : element.getNamespaceURI());

            element = DomUtils.renameElementNS(element, newElementName, newElementNamespace, true, true);
        }

        if(!attributes.isEmpty()) {
            Map<String, Object> beans = Filter.getCurrentExecutionContext().getBeanContext().getBeanMap();
            Set<Map.Entry<QName, FreeMarkerTemplate>> attributeSet = attributes.entrySet();

            for(Map.Entry<QName, FreeMarkerTemplate> attributeConfig : attributeSet) {
                QName attribName = attributeConfig.getKey();
                FreeMarkerTemplate valueTemplate = attributeConfig.getValue();
                String namespaceURI = attribName.getNamespaceURI();

                if(namespaceURI != null) {
                    String prefix = attribName.getPrefix();
                    if(prefix != null && prefix.length() > 0) {
                        element.setAttributeNS(namespaceURI, prefix + ":" + attribName.getLocalPart(), valueTemplate.apply(beans));
                    } else {
                        element.setAttributeNS(namespaceURI, attribName.getLocalPart(), valueTemplate.apply(beans));
                    }
                } else {
                    element.setAttribute(attribName.getLocalPart(), valueTemplate.apply(beans));
                }
            }
        }
    }
}
 
