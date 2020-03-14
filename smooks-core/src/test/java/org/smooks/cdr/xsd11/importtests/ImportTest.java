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
package org.smooks.cdr.xsd11.importtests;

import org.junit.Test;
import static org.junit.Assert.*;
import org.smooks.Smooks;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.delivery.dom.serialize.SimpleDOMVisitor;
import org.smooks.payload.StringSource;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ImportTest {

	@Test
    public void test_11_imports_10() throws IOException, SAXException {
        testConfig("11_import_10.xml");
    }

	@Test
    public void test_10_imports_11() throws IOException, SAXException {
        try {
            testConfig("10_import_11.xml");
            fail("Expected SmooksConfigurationException.");
        } catch(SmooksConfigurationException e) {
            assertEquals("Unsupported import of a v1.1 configuration from inside a v1.0 configuration.  Path to configuration: '/[root-config]/[11Config.xml]'.", e.getMessage());
        }
    }

	@Test
    public void test_paramaterized_import() throws IOException, SAXException {
        SimpleDOMVisitor.visited = false;
        testConfig("paramaterized_import_main.xml");
        assertTrue("Parameters not properly injected into import.", SimpleDOMVisitor.visited);
    }

    private void testConfig(String config) throws IOException, SAXException {
        Smooks smooks = new Smooks("/org/smooks/cdr/xsd11/importtests/" + config);
        smooks.filterSource(new StringSource("<a/>"), null);
    }
}
