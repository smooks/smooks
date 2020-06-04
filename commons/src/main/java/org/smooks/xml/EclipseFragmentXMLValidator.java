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

import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.io.StreamUtils;
import org.smooks.util.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Eclipse XML Validator.
 * <p/>
 * Uses the Eclipse based /fragment.xml schema resource files available on the classpath
 * to lookup the schema resources for given namespaces.
 *
 *
 * @author zubairov
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class EclipseFragmentXMLValidator extends XsdValidator {

    private static final Logger logger = LoggerFactory.getLogger(EclipseFragmentXMLValidator.class);

    public EclipseFragmentXMLValidator() throws IOException, SAXException {
        super.setSchemaSourceResolver(new SchemaResolver());
    }

    public EclipseFragmentXMLValidator(SchemaFactory schemaFactory) throws IOException, SAXException {
        super.setSchemaFactory(schemaFactory);
        super.setSchemaSourceResolver(new SchemaResolver());
    }

    @Override
    public void setSchemaSourceResolver(LSResourceResolver schemaSourceResolver) throws SAXException {
        throw new UnsupportedOperationException("Illegal call to set SchemaSourceResolver.");
    }

    @Override
    public void setXSDSources(Collection<Source> xsdSources) throws SAXException {
        throw new UnsupportedOperationException("Illegal call to set XSDSources.");
    }

    private class SchemaResolver implements LSResourceResolver {

        private static final String PLATFORM_FRAGMENT = "platform:/fragment/";

        private Map<String, String> catalog = new ConcurrentHashMap<String, String>();
        private Map<String, ByteArrayInputStream> schemaCache = new ConcurrentHashMap<String, ByteArrayInputStream>();

        private SchemaResolver() throws IOException {
            List<URL> urnFiles = ClassUtil.getResources("/fragment.xml", EclipseFragmentXMLValidator.class);

            logger.debug("Loading XML schemas information from " + urnFiles);

            for (URL url : urnFiles) {
                InputStream in = url.openStream();
                try {
                    if (in != null) {
                        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

                        docBuilderFactory.setNamespaceAware(true);

                        Document fragmentDoc = docBuilderFactory.newDocumentBuilder().parse(in);
                        NodeList uriElements = fragmentDoc.getElementsByTagName("uri");

                        for (int i = 0; i < uriElements.getLength(); i++) {
                            Element uriElement = (Element) uriElements.item(i);

                            String name = uriElement.getAttribute("name");
                            String uri = uriElement.getAttribute("uri");

                            if (uri.startsWith(PLATFORM_FRAGMENT)) {
                                // URI is now something like platform:/fragment/org.smooks.edi.unedifact.d99a-mapping/path/path/file.xsd
                                // we need only /path/path/file.xsd
                                // cut platform:/fragment/
                                uri = uri.substring(PLATFORM_FRAGMENT.length());
                                // cut after first '/'
                                uri = uri.substring(uri.indexOf('/'));
                            }

                            catalog.put(name, uri);
                        }
                    }
                } catch (Exception e) {
                    IOException ioE = new IOException("Error reading Schema resource '" + url + "'.");
                    ioE.initCause(e);
                    throw ioE;
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
            }

            // One resource we have to add manually
            catalog.put("urn:org.milyn.edi.unedifact.v41", "/META-INF/schema/v41-segments.xsd");
        }

        public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
            ByteArrayInputStream schemaBytes = schemaCache.get(namespaceURI);

            if (schemaBytes == null) {
                synchronized (schemaCache) {
                    String schemaPath = catalog.get(namespaceURI);

                    if (schemaPath == null) {
                        throw new SmooksConfigurationException("Unknown EDI namespace '" + namespaceURI + "'.");
                    }
                    InputStream schemaStream = ClassUtil.getResourceAsStream(schemaPath, EclipseFragmentXMLValidator.class);
                    if (schemaStream == null) {
                        throw new SmooksConfigurationException("Unable to locate XSD classpath resource '" + schemaPath + "' for EDI namespace '" + namespaceURI + "'.");
                    }

                    try {
                        schemaBytes = new ByteArrayInputStream(StreamUtils.readStream(schemaStream));
                        schemaCache.put(namespaceURI, schemaBytes);
                    } catch (IOException e) {
                        throw new SmooksConfigurationException("Unable to read XSD classpath resource '" + schemaPath + "' for EDI namespace '" + namespaceURI + "'.");
                    } finally {
                        try {
                            schemaStream.close();
                        } catch (IOException e) {
                            logger.debug("Unexpected exception classing classpath resource stream for '" + schemaPath + "'.", e);
                        }
                    }
                }
            }

            return new StreamSourceLSInput(new StreamSource(schemaBytes));
        }
    }
}
