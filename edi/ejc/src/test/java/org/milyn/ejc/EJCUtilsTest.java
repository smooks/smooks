/*
 * Milyn - Copyright (C) 2006 - 2010
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
package org.milyn.ejc;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class EJCUtilsTest extends TestCase {

    public void testEncodeClassName() throws IllegalNameException {
        assertEquals("Address", EJCUtils.encodeClassName("ADDRESS"));
        assertEquals("CustomerAddress", EJCUtils.encodeClassName("CUSTOMER_ADDRESS"));
        assertEquals("CustomerADDRESS", EJCUtils.encodeClassName("Customer_ADDRESS"));
        assertEquals("CustomerAddress", EJCUtils.encodeClassName("Customer_address"));
        assertEquals("Default", EJCUtils.encodeClassName("default"));
        assertEquals("_1CustomerAddressPOBox", EJCUtils.encodeClassName("1CustomerAddressP.O.Box"));
    }

    public void testEncodeAttribute() throws IllegalNameException {
        assertEquals("address", EJCUtils.encodeAttributeName("ADDRESS"));
        assertEquals("addRESS", EJCUtils.encodeAttributeName("addRESS"));
        assertEquals("addRESS", EJCUtils.encodeAttributeName("AddRESS"));
        assertEquals("orderId", EJCUtils.encodeAttributeName("orderId"));
        assertEquals("orderId", EJCUtils.encodeAttributeName("order_id"));
        assertEquals("_default", EJCUtils.encodeAttributeName("default"));
        assertEquals("_package", EJCUtils.encodeAttributeName("package"));
        assertEquals("_package", EJCUtils.encodeAttributeName("Package"));
        assertEquals("_1address", EJCUtils.encodeAttributeName("1ADDRESS"));
        assertEquals("_1addressPOBox", EJCUtils.encodeAttributeName("_1addressP.O.Box"));
    }
}
