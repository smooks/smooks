/*
 * Milyn - Copyright (C) 2006 - 2011
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

package org.milyn.javabean.binding;

import junit.framework.TestCase;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.milyn.io.StreamUtils;
import org.milyn.javabean.binding.ordermodel.Order;
import org.milyn.javabean.binding.xml.XMLBinding;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class XMLBindingTest extends TestCase {

    public void test_no_namespaces() throws IOException, SAXException {
        test("config1");
    }

    public void test_with_namespaces_01() throws IOException, SAXException {
        test("config2");
    }

    public void test_with_namespaces_02() throws IOException, SAXException {
        test("config3");
    }

    private void test(String config) throws IOException, SAXException {
        String inputXML = StreamUtils.readStreamAsString(getClass().getResourceAsStream(config + "/order.xml"));
        XMLBinding xmlBinding = (XMLBinding) new XMLBinding().add(getClass().getResourceAsStream(config + "/order-binding-config.xml"));
        xmlBinding.intiailize();

        // Read...
        Order order = xmlBinding.fromXML(inputXML, Order.class);

        // write...
        String outputXML = xmlBinding.toXML(order);

//        System.out.println(outputXML);

        // Compare...
        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(inputXML, outputXML);
    }
}
