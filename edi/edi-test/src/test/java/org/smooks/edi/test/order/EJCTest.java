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
package org.smooks.edi.test.order;

import org.junit.Test;

import java.io.IOException;

import org.smooks.edisax.EDIConfigurationException;
import org.smooks.edi.test.EJCTestUtil;
import org.smooks.edisax.util.IllegalNameException;
import org.xml.sax.SAXException;

/**
 * EJCTest tests compiling edi-mapping to classModel.
 *
 * @author bardl 
 */
public class EJCTest {

    @Test
    public void testOrderModel() throws EDIConfigurationException, IOException, SAXException, IllegalNameException, ClassNotFoundException {
        EJCTestUtil.testModel("order-mapping.xml", "order.edi", "OrderFactory");
    }
}
