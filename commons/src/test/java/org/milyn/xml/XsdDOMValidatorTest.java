/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package org.milyn.xml;

import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class XsdDOMValidatorTest {

	@Test
    public void test_namespace_gathering() throws IOException, SAXException, ParserConfigurationException {
        Document document = XmlUtil.parseStream(getClass().getResourceAsStream("xsdDomValidator-test-01.xml"));
        XsdDOMValidator validator = new XsdDOMValidator(document);

        assertEquals("http://www.milyn.org/xsd/test-xsd-01.xsd", validator.getDefaultNamespace().toString());
        assertEquals("[http://www.milyn.org/xsd/test-xsd-01.xsd, http://www.milyn.org/xsd/test-xsd-02.xsd]", validator.getNamespaces().toString());
    }

	@Test
    public void test_validation_validdoc() throws IOException, SAXException, ParserConfigurationException {
        Document document = XmlUtil.parseStream(getClass().getResourceAsStream("xsdDomValidator-test-01.xml"));
        XsdDOMValidator validator = new XsdDOMValidator(document);

        validator.validate();
    }

	@Test
    public void test_validation_invaliddoc() throws IOException, SAXException, ParserConfigurationException {
        Document document = XmlUtil.parseStream(getClass().getResourceAsStream("xsdDomValidator-test-02.xml"));
        XsdDOMValidator validator = new XsdDOMValidator(document);

        try {
            validator.validate();
            fail("Expected SAXParseException");
        } catch(SAXParseException e) {
            assertEquals("cvc-complex-type.4: Attribute 'myName' must appear on element 'a:myNVP'.", e.getMessage());
        }
    }
}
