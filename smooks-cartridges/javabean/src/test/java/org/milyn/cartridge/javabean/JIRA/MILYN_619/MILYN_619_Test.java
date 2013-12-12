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
package org.milyn.cartridge.javabean.JIRA.MILYN_619;

import junit.framework.TestCase;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.milyn.Smooks;
import org.milyn.commons.io.StreamUtils;
import org.milyn.payload.JavaSource;
import org.milyn.payload.StringResult;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MILYN_619_Test extends TestCase {

    public void test() throws IOException, SAXException {
        RootObject rootObj = new RootObject();

        rootObj.getEnums().add(new TheEnumContainer());
        rootObj.getEnums().add(new TheEnumContainer());

        Smooks smooks = new Smooks();
        StringResult res = new StringResult();

        smooks.filterSource(new JavaSource(rootObj), res);

        String expected = StreamUtils.readStreamAsString(getClass().getResourceAsStream("expected.xml"));
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(expected, res.getResult());
    }
}
