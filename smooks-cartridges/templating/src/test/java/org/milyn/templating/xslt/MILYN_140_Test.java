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
package org.milyn.templating.xslt;

import junit.framework.TestCase;
import org.milyn.Smooks;
import org.milyn.payload.StringResult;
import org.milyn.payload.StringSource;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MILYN_140_Test extends TestCase {

    public void test_external() throws IOException, SAXException {
        test("MILYN-140-01.xml");
    }

    public void test_templatelet() throws IOException, SAXException {
        test("MILYN-140-02.xml");
    }

    public void test(String config) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream(config));
        StringResult result = new StringResult();

        smooks.filterSource(new StringSource("<x/>"), result);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>Hi there!", result.getResult());
    }
}
