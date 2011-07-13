/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (version 2.1) as published by the Free Software
 * Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */

package org.milyn.xml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.io.StreamUtils;
import org.milyn.util.ClassUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
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

    private static final Log logger = LogFactory.getLog(EclipseFragmentXMLValidator.class);

    public EclipseFragmentXMLValidator() throws IOException, SAXException {
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
                                // URI is now something like platform:/fragment/org.milyn.edi.unedifact.d99a-mapping/path/path/file.xsd
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
