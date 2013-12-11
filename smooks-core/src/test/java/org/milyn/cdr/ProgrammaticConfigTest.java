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
package org.milyn.cdr;

import junit.framework.TestCase;
import org.milyn.Smooks;
import org.milyn.commons.cdr.SmooksConfigurationException;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class ProgrammaticConfigTest extends TestCase {

    public void test_properly_configed() {
        Smooks smooks = new Smooks();
        ConfigurableVisitor visitor = new ConfigurableVisitor().setStringParam("hi");

        smooks.addVisitor(visitor);

        assertEquals("hi", visitor.getStringParam());
        assertEquals(5, visitor.getIntParam());
        assertEquals(null, visitor.getOptionalStringParam());
    }

    public void test_not_configed() {
        Smooks smooks = new Smooks();

        try {
            smooks.addVisitor(new ConfigurableVisitor());
            fail("Expected SmooksConfigurationException");
        } catch (SmooksConfigurationException e) {
            assertEquals("Property 'stringParam' not configured on class org.milyn.cdr.ConfigurableVisitor'.", e.getMessage());
        }
    }
}
