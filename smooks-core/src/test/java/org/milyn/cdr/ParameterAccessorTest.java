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
import org.milyn.delivery.ContentDeliveryConfig;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class ParameterAccessorTest extends TestCase {

    protected void tearDown() throws Exception {
        System.getProperties().remove("test.parameter");
    }

    public void test_system_property() {
        Smooks smooks = new Smooks();
        ContentDeliveryConfig deliveryConfig = smooks.createExecutionContext().getDeliveryConfig();

        assertEquals(null, ParameterAccessor.getStringParameter("test.parameter", deliveryConfig));

        System.setProperty("test.parameter", "xxxxxxx");
        assertEquals("xxxxxxx", ParameterAccessor.getStringParameter("test.parameter", deliveryConfig));
    }
}
