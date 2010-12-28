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
package org.milyn.cdr.xsd11.conditiontests;

import junit.framework.TestCase;
import org.milyn.Smooks;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.payload.StringSource;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Config11ConditionTest extends TestCase {

    /**
     * Normal conditions defined on the resource
     */
    public void test_01() throws IOException, SAXException {
        testConfig("config01.xml", "[config1, config3]");
    }

    /**
     * One of the conditions defined globally and referenced from one of the resources
     */
    public void test_02() throws IOException, SAXException {
        testConfig("config02.xml", "[config1, config3]");
    }

    /**
     * Application of a default condition ref
     */
    public void test_03() throws IOException, SAXException {
        testConfig("config03.1.xml", "[config1, config3]"); // No default defined
        testConfig("config03.2.xml", "[config1]"); // Has a default defined
    }

    /**
     * config04.xml with an imported config04.1.xml config which inherits
     * conditions from config04.xml
     */
    public void test_04() throws IOException, SAXException {
        testConfig("config04.xml", "[config1, config3]");
    }

    /**
     * config06.xml importing config06.1.xml and config06.2.xml. config06.1.xml inheriting
     * conditions from config06.xml and config06.2.xml redefining the same conditions locally.
     * So... this is basically a scoping test on the conditions...
     */
    public void test_06() throws IOException, SAXException {
        testConfig("config06.xml", "[config1, config3, 2nd_config1, 2nd_config2]");
    }

    /**
     * Undefined condition ref
     */
    public void test_05() throws IOException, SAXException {
        try {
            testConfig("config05.xml", "n/a");
            fail("Expected SmooksConfigurationException");
        } catch(SmooksConfigurationException e) {
            assertEquals("Unknown condition idRef 'config3'.", e.getMessage());
        }
    }

    /**
     * Duplicate condition ids
     */
    public void test_07() throws IOException, SAXException {
        try {
            testConfig("config07.xml", "n/a");
            fail("Expected SmooksConfigurationException");
        } catch(SmooksConfigurationException e) {
            assertEquals("Duplicate condition ID 'config3'.", e.getMessage());
        }
    }

    private void testConfig(String config, String expected) throws IOException, SAXException {
        Smooks smooks = new Smooks("/org/milyn/cdr/xsd11/conditiontests/" + config);

        smooks.filterSource(new StringSource("<a/>"), null);
        assertEquals(expected, ConditionTestVisitor.messagesUsed.toString());
        ConditionTestVisitor.messagesUsed.clear();
    }
}
