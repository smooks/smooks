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
package org.smooks.javabean.gen;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ConfigGeneratortTest {

    @Test
    public void test() throws ClassNotFoundException, java.io.IOException {
        java.util.Properties properties = new java.util.Properties();
        java.io.StringWriter writer = new java.io.StringWriter();

        properties.setProperty(ConfigGenerator.ROOT_BEAN_CLASS, org.smooks.javabean.Order.class.getName());

        ConfigGenerator generator = new ConfigGenerator(properties, writer);

        generator.generate();

        String expected = org.smooks.io.StreamUtils.readStreamAsString(getClass().getResourceAsStream("expected-01.xml"));
        assertTrue("Generated config not as expected.", org.smooks.io.StreamUtils.compareCharStreams(new java.io.StringReader(expected), new java.io.StringReader(writer.toString())));
    }

    @Test
    public void test_commandLine() throws ClassNotFoundException, java.io.IOException {
        ConfigGenerator.main(new String[] {"-c", org.smooks.javabean.Order.class.getName(), "-o", "./target/binding-config-test.xml"});
    }
}
