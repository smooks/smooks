/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License (version 2.1) as published by the Free Software
 *  Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  See the GNU Lesser General Public License for more details:
 *  http://www.gnu.org/licenses/lgpl.txt
 */

package org.smooks.model.javabean;

import org.junit.Before;
import org.junit.Test;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.milyn.javabean.dynamic.Model;
import org.milyn.javabean.dynamic.ModelBuilder;
import org.smooks.model.core.SmooksModel;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class BeanTest {

    private ModelBuilder modelBuilder;

    @Before
    public void setUp() throws IOException, SAXException {
        modelBuilder = new ModelBuilder(SmooksModel.MODEL_DESCRIPTOR, false);
    }

    @Test
    public void test_v11() throws IOException, SAXException {
        test("v11/config-01.xml");
    }

    @Test
    public void test_v12_01() throws IOException, SAXException {
        test("v12/config-01.xml");
    }

    @Test
    public void test_v12_02() throws IOException, SAXException {
        // mixed namespaces...
        test("v12/config-02.xml");
    }

    @Test
    public void test_v13_01() throws IOException, SAXException {
        test("v13/config-01.xml");
    }

    @Test
    public void test_v13_02() throws IOException, SAXException {
        // mixed namespaces...
        test("v13/config-02.xml");
    }

    public void test(String messageFile) throws IOException, SAXException {
        Model<SmooksModel> model = modelBuilder.readModel(getClass().getResourceAsStream(messageFile), SmooksModel.class);

        StringWriter modelWriter = new StringWriter();
        model.writeModel(modelWriter);
        System.out.println(modelWriter);
        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream(messageFile)), new StringReader(modelWriter.toString()));
    }
}