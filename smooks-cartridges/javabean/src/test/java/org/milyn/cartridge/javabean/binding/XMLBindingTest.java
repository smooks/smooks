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

package org.milyn.cartridge.javabean.binding;

import junit.framework.TestCase;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.milyn.Smooks;
import org.milyn.commons.io.StreamUtils;
import org.milyn.cartridge.javabean.binding.config5.Person;
import org.milyn.cartridge.javabean.binding.model.ModelSet;
import org.milyn.cartridge.javabean.binding.ordermodel.Order;
import org.milyn.cartridge.javabean.binding.xml.XMLBinding;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class XMLBindingTest extends TestCase {

    public void test_no_namespaces() throws IOException, SAXException {
        test_pre_created_Smooks("config1");
        test_post_created_Smooks("config1");
    }

    public void test_with_namespaces_01() throws IOException, SAXException {
        test_pre_created_Smooks("config2");
        test_post_created_Smooks("config2");
    }

    public void test_with_namespaces_02() throws IOException, SAXException {
        test_pre_created_Smooks("config3");
        test_post_created_Smooks("config3");
    }

    public void test_with_namespaces_03() throws IOException, SAXException {
        test_pre_created_Smooks("config4");
        test_post_created_Smooks("config4");
    }

    public void test_Person_binding() throws IOException, SAXException {
        XMLBinding xmlBinding = new XMLBinding().add(getClass().getResourceAsStream("config5/person-binding-config.xml"));
        xmlBinding.intiailize();

        Person person = xmlBinding.fromXML("<person name='Max' age='50' />", Person.class);
        String xml = xmlBinding.toXML(person);
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual("<person name='Max' age='50' />", xml);

    }

    public void test_MILYN629() throws IOException, SAXException {
        test_pre_created_Smooks("config6");
        test_post_created_Smooks("config6");
    }

    public void test_add_fails_after_smooks_constructed() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config1/order-binding-config.xml"));
        XMLBinding xmlBinding = new XMLBinding(smooks);

        try {
            xmlBinding.add("blah");
        } catch (IllegalStateException e) {
            assertEquals("Illegal call to method after all configurations have been added.", e.getMessage());
        }
    }

    private void test_pre_created_Smooks(String config) throws IOException, SAXException {
        String inputXML = StreamUtils.readStreamAsString(getClass().getResourceAsStream(config + "/order.xml"));
        Smooks smooks = new Smooks(getClass().getResourceAsStream(config + "/order-binding-config.xml"));
        XMLBinding xmlBinding = new XMLBinding(smooks);
        xmlBinding.intiailize();

        assertTrue("Should be a binding only config.", ModelSet.get(smooks.getApplicationContext()).isBindingOnlyConfig());

        test(inputXML, xmlBinding);
    }

    private void test_post_created_Smooks(String config) throws IOException, SAXException {
        String inputXML = StreamUtils.readStreamAsString(getClass().getResourceAsStream(config + "/order.xml"));
        XMLBinding xmlBinding = new XMLBinding().add(getClass().getResourceAsStream(config + "/order-binding-config.xml"));
        xmlBinding.intiailize();

        test(inputXML, xmlBinding);
    }

    private void test(String inputXML, XMLBinding xmlBinding) throws SAXException, IOException {
        // Read...
        Order order = xmlBinding.fromXML(inputXML, Order.class);

        assertEquals("Joe & Ray", order.getHeader().getCustomerName());

        // write...
        String outputXML = xmlBinding.toXML(order);

//        System.out.println(outputXML);

        // Compare...
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(inputXML, outputXML);
    }
}
