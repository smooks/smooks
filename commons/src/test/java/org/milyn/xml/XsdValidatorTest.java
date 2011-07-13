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

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class XsdValidatorTest extends TestCase {

    public void test_validation_via_sources() throws IOException, SAXException, ParserConfigurationException, URISyntaxException {
        Document document = XmlUtil.parseStream(getClass().getResourceAsStream("xsdDomValidator-test-01.xml"));
        XsdValidator validator = new XsdValidator();
        List<Source> sources = new ArrayList<Source>();

        sources.add(new StreamSource(getClass().getResourceAsStream("/META-INF/xsd/test-xsd-01.xsd")));
        sources.add(new StreamSource(getClass().getResourceAsStream("/META-INF/xsd/test-xsd-02.xsd")));

        validator.setXSDSources(sources);
        validator.validate(new DOMSource(document));
    }

    public void test_dom_validation_via_resolver() throws IOException, SAXException, ParserConfigurationException, URISyntaxException {
        Document document = XmlUtil.parseStream(getClass().getResourceAsStream("xsdDomValidator-test-01.xml"));
        XsdValidator validator = new XsdValidator();

        MyLSResourceResolver schemaSourceResolver = new MyLSResourceResolver();
        schemaSourceResolver.resources.put("http://www.milyn.org/xsd/test-xsd-01.xsd", new StreamSourceLSInput(new StreamSource(getClass().getResourceAsStream("/META-INF/xsd/test-xsd-01.xsd"))));
        schemaSourceResolver.resources.put("http://www.milyn.org/xsd/test-xsd-02.xsd", new StreamSourceLSInput(new StreamSource(getClass().getResourceAsStream("/META-INF/xsd/test-xsd-02.xsd"))));

        validator.setSchemaSourceResolver(schemaSourceResolver);
        validator.validate(new DOMSource(document));
    }

    public void test_stream_validation_via_resolver() throws IOException, SAXException, ParserConfigurationException, URISyntaxException {
        XsdValidator validator = new XsdValidator();

        MyLSResourceResolver schemaSourceResolver = new MyLSResourceResolver();
        schemaSourceResolver.resources.put("http://www.milyn.org/xsd/test-xsd-01.xsd", new StreamSourceLSInput(new StreamSource(getClass().getResourceAsStream("/META-INF/xsd/test-xsd-01.xsd"))));
        schemaSourceResolver.resources.put("http://www.milyn.org/xsd/test-xsd-02.xsd", new StreamSourceLSInput(new StreamSource(getClass().getResourceAsStream("/META-INF/xsd/test-xsd-02.xsd"))));

        validator.setSchemaSourceResolver(schemaSourceResolver);
        validator.validate(new StreamSource(getClass().getResourceAsStream("xsdDomValidator-test-01.xml")));
    }

    public class MyLSResourceResolver implements LSResourceResolver {

        private Map<String, StreamSourceLSInput> resources = new HashMap<String, StreamSourceLSInput>();

        public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
            return resources.get(namespaceURI);
        }
    }
}
