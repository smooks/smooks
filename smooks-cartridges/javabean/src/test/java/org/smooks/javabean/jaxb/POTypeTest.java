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
package org.smooks.javabean.jaxb;

import org.junit.Test;
import org.custommonkey.xmlunit.XMLUnit;
import org.smooks.io.StreamUtils;
import org.smooks.javabean.binding.xml.XMLBinding;
import org.smooks.javabean.jaxb.model.POType;
import org.smooks.payload.StringSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringWriter;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class POTypeTest {

    @Test
    public void test() throws IOException, SAXException {
        XMLBinding xmlBinding =
                new XMLBinding().add(getClass().getResourceAsStream("POType-binding.xml")).intiailize();

        String poXML = StreamUtils.readStreamAsString(getClass().getResourceAsStream("po.xml"));
        POType po = xmlBinding.fromXML(new StringSource(poXML), POType.class);

        StringWriter writer = new StringWriter();
        xmlBinding.toXML(po, writer);

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.compareXML(poXML, writer.toString());
    }
}
