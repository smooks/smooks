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
package org.smooks.xml;

import org.smooks.assertion.AssertArgument;
import org.smooks.util.ClassUtil;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * XSD DOM Validator.
 * <p/>
 * Iterates through the document (DOM) gathering the namespaces.  It validates
 * based on the convention that the gathered namespace XSDs are provided on the
 * classpath.  It uses the namespace path (URI.getPath()), prepending it with "/META-INF"
 * to perform a classpath resource lookup for the XSD i.e. the XSDs must be provided on
 * the classpath below the "META-INF" package.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class XsdDOMValidator extends XsdValidator {

    private final Document document;
    private URI defaultNamespace;
    private final List<URI> namespaces = new ArrayList<>();

    public XsdDOMValidator(Document document) throws SAXException {
        AssertArgument.isNotNull(document, "document");
        this.document = document;

        // Get the default namespace...
        String defaultNamespaceString = getDefaultNamespace(document.getDocumentElement());
        if(defaultNamespaceString != null) {
            try {
                defaultNamespace = new URI(defaultNamespaceString);
            } catch (URISyntaxException e) {
                throw new SAXException("Cannot validate this document with this class.  Namespaces must be valid URIs.  Default Namespace: '" + defaultNamespaceString + "'.", e);
            }
        }

        // Get the full namespace list...
        namespaces.addAll(collectNamespaces(document.getDocumentElement()));

        // Using the namespace URI list, create the XSD Source array used to
        // create the merged Schema instance...
    	List<Source> sources = new ArrayList<Source>();
        for (int i = 0; i < namespaces.size(); i++) {
            URI namespace = namespaces.get(i);
            if(!XmlUtil.isXMLReservedNamespace(namespace.toString())) {
                final Source namespaceSource = getNamespaceSource(namespace);
                if (namespaceSource != null) {
                    sources.add(namespaceSource);
                }
            }
        }
        setXSDSources(sources);
    }

    public URI getDefaultNamespace() {
        return defaultNamespace;
    }

    public List<URI> getNamespaces() {
        return namespaces;
    }

    /**
     * Validate the document against the namespaces referenced in it.
     * @throws SAXException Validation error.
     * @throws IOException Error reading the XSD Sources.
     */
    public void validate() throws SAXException, IOException {
        validate(new DOMSource(document));
    }

    /**
     * Get the default namespace associated with the supplied element.
     * @param element The element to be checked.
     * @return The default namespace, or null if none was found.
     */
    public static String getDefaultNamespace(Element element) {
        NamedNodeMap attributes = element.getAttributes();
        int attributeCount = attributes.getLength();

        for(int i = 0; i < attributeCount; i++) {
            Attr attribute = (Attr) attributes.item(i);

            if(XMLConstants.XMLNS_ATTRIBUTE.equals(attribute.getName()) && XMLConstants.XMLNS_ATTRIBUTE.equals(attribute.getLocalName())) {
                return attribute.getValue();
            }
        }

        return null;
    }

    private List<URI> collectNamespaces(Element element) throws SAXException {
        List<URI> namespaceSources = new ArrayList<>();
        NamedNodeMap attributes = element.getAttributes();
        int attributeCount = attributes.getLength();

        for(int i = 0; i < attributeCount; i++) {
            Attr attribute = (Attr) attributes.item(i);
            String namespace = attribute.getNamespaceURI();

            if(XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespace)) {
                try {
                    namespaceSources.add(new URI(attribute.getValue()));
                } catch (URISyntaxException e) {
                    throw new SAXException("Cannot validate this document with this class.  Namespaces must be valid URIs.  Found Namespace: '" + attribute.getValue() + "'.", e);
                }
            }
        }

        NodeList childNodes = element.getChildNodes();
        int childCount = childNodes.getLength();
        for(int i = 0; i < childCount; i++) {
            Node child = childNodes.item(i);

            if(child.getNodeType() == Node.ELEMENT_NODE) {
                namespaceSources.addAll(collectNamespaces((Element) child));
            }
        }
        
        return namespaceSources;
    }

    private Source getNamespaceSource(URI namespace) {
        if (namespace.getPath().length() > 0) {
            String resourcePath = "/META-INF" + namespace.getPath();
            InputStream xsdStream = ClassUtil.getResourceAsStream(resourcePath, getClass());

            if (xsdStream == null) {
                return null;
            } else {
                return new StreamSource(xsdStream);
            }
        } else {
            return null;
        }
    }
}
