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
package org.smooks.templating.bugfixes.MILYN_285;

import org.junit.Test;
import org.smooks.Smooks;
import org.smooks.payload.StringResult;
import org.xml.sax.SAXException;
import org.custommonkey.xmlunit.XMLUnit;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class MILYN285Test {

    @Test
    public void test() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("message.xml")), result);
        XMLUnit.compareXML("<root>\n" +
                "\t<abc>def</abc>\n" +
                "\t<bla>\n" +
                "\t\tdef\n" +
                "\t</bla>\n" +
                "</root>", result.toString());
    }
}
