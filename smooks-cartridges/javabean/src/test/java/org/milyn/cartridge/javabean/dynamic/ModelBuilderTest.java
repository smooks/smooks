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
package org.milyn.cartridge.javabean.dynamic;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;

import junit.framework.TestCase;
import org.xml.sax.SAXParseException;

/**
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ModelBuilderTest extends TestCase {

	public static final String NS_DESCRIPTOR = "META-INF/services/org/smooks/javabean/dynamic/ns-descriptors.properties";

	public void test_1_schema() throws SAXException, IOException {
		ModelBuilder builder = new ModelBuilder(NS_DESCRIPTOR, true);

		Model<AAA> model = builder.readModel(getClass().getResourceAsStream("aaa-message.xml"), AAA.class);
		AAA aaa = model.getModelRoot();
		assertEquals(1234.98765, aaa.getDoubleProperty());
		assertEquals("http://www.acme.com/xsd/aaa.xsd", model.getBeanMetadata(aaa).getNamespace());

		aaa = builder.readObject(getClass().getResourceAsStream("aaa-message.xml"), AAA.class);
		assertEquals(1234.98765, aaa.getDoubleProperty());
	}

	public void test_2_schema_with_validation_1() throws SAXException, IOException {
		test_2_schema(new ModelBuilder(NS_DESCRIPTOR, true), "bbb-message.xml");
	}

    public void test_2_schema_with_validation_2() throws SAXException, IOException {
        try {
            test_2_schema(new ModelBuilder(NS_DESCRIPTOR, true), "bbb-message-invalid.xml");
            fail("Expected SAXParseException");
        } catch(SAXParseException e) {
            assertTrue(e.getMessage().indexOf("Invalid content was found starting with element 'boo:ddd'") != -1);
        }
    }

	public void test_2_schema_without_validation() throws SAXException, IOException {
		test_2_schema(new ModelBuilder(NS_DESCRIPTOR, false), "bbb-message-invalid.xml");
	}

	private void test_2_schema(ModelBuilder builder, String message) throws SAXException, IOException {
		Model<BBB> model = builder.readModel(getClass().getResourceAsStream(message), BBB.class);
		BBB bbb = model.getModelRoot();
		assertEquals(1234, bbb.getFloatProperty(), 1.0);

		assertEquals("http://www.acme.com/xsd/bbb.xsd", model.getBeanMetadata(bbb).getNamespace());
		List<AAA> aaas = bbb.getAaas();
        assertEquals(3, aaas.size());
		assertEquals("http://www.acme.com/xsd/aaa.xsd", model.getBeanMetadata(aaas.get(0)).getNamespace());

		bbb = builder.readObject(getClass().getResourceAsStream(message), BBB.class);
		assertEquals(1234, bbb.getFloatProperty(), 1.0);

		aaas = bbb.getAaas();
		assertEquals(1234.98765, aaas.get(0).getDoubleProperty());

        StringWriter writer = new StringWriter();
        model.writeModel(writer);
        System.out.println(writer);
        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream(message)), new StringReader(writer.toString()));
	}

    public void test_build_model() throws IOException, SAXException {
        ModelBuilder builder = new ModelBuilder(NS_DESCRIPTOR, false);
        BBB bbb = new BBB();
        List<AAA> aaas = new ArrayList<AAA>();
        Model<BBB> model = new Model<BBB>(bbb, builder);

        bbb.setFloatProperty(1234.87f);
        bbb.setAaas(aaas);

        aaas.add(new AAA());
        aaas.get(0).setDoubleProperty(1234.98765d);
        aaas.get(0).setIntProperty(123);
        model.registerBean(aaas.get(0));
        aaas.add(new AAA());
        aaas.get(1).setDoubleProperty(2234.98765d);
        aaas.get(1).setIntProperty(223);
        model.registerBean(aaas.get(1));
        aaas.add(new AAA());
        aaas.get(2).setDoubleProperty(3234.98765d);
        aaas.get(2).setIntProperty(323);
        model.registerBean(aaas.get(2));

        StringWriter writer = new StringWriter();
        model.writeModel(writer);
//        System.out.println(writer);
        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream("bbb-message.xml")), new StringReader(writer.toString()));
    }

    @Override
    protected void setUp() throws Exception {
    	Locale.setDefault(new Locale("en", "IE"));
    }

}