/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0 or,
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

import org.junit.Test;
import org.apache.xerces.jaxp.validation.XMLSchemaFactory;
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
public class XsdValidatorTest {

	@Test
    public void test_validation_via_sources() throws IOException, SAXException, ParserConfigurationException, URISyntaxException {
        Document document = XmlUtil.parseStream(getClass().getResourceAsStream("xsdDomValidator-test-01.xml"));
        XsdValidator validator = new XsdValidator();
        List<Source> sources = new ArrayList<Source>();

        sources.add(new StreamSource(getClass().getResourceAsStream("/META-INF/xsd/test-xsd-01.xsd")));
        sources.add(new StreamSource(getClass().getResourceAsStream("/META-INF/xsd/test-xsd-02.xsd")));

        validator.setXSDSources(sources);
        validator.validate(new DOMSource(document));
    }

	@Test
    public void test_dom_validation_via_resolver() throws IOException, SAXException, ParserConfigurationException, URISyntaxException {
        Document document = XmlUtil.parseStream(getClass().getResourceAsStream("xsdDomValidator-test-01.xml"));
        XsdValidator validator = new XsdValidator();

        validator.setSchemaFactory(new XMLSchemaFactory());

        MyLSResourceResolver schemaSourceResolver = new MyLSResourceResolver();
        schemaSourceResolver.resources.put("http://www.milyn.org/xsd/test-xsd-01.xsd", new StreamSourceLSInput(new StreamSource(getClass().getResourceAsStream("/META-INF/xsd/test-xsd-01.xsd"))));
        schemaSourceResolver.resources.put("http://www.milyn.org/xsd/test-xsd-02.xsd", new StreamSourceLSInput(new StreamSource(getClass().getResourceAsStream("/META-INF/xsd/test-xsd-02.xsd"))));

        validator.setSchemaSourceResolver(schemaSourceResolver);
        validator.validate(new DOMSource(document));
    }

	@Test
    public void test_stream_validation_via_resolver() throws IOException, SAXException, ParserConfigurationException, URISyntaxException {
        XsdValidator validator = new XsdValidator();

        validator.setSchemaFactory(new XMLSchemaFactory());

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
