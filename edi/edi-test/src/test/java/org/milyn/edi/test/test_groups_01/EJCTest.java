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
package org.milyn.edi.test.test_groups_01;

import org.junit.Test;
import static org.junit.Assert.*;
import org.milyn.edisax.EDIConfigurationException;
import org.milyn.edi.test.EJCTestUtil;
import org.milyn.edisax.util.IllegalNameException;
import org.xml.sax.SAXException;

import java.io.IOException;

public class EJCTest {

    @Test
    public void testModel() throws EDIConfigurationException, IOException, SAXException, IllegalNameException, ClassNotFoundException {
        EJCTestUtil.testModel("edi-to-xml-mapping.xml", "edi-input.txt", "OuterFactory", true);
    }
}
