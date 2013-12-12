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
package org.milyn.cartridge.javabean.jaxb;

import junit.framework.TestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.milyn.cartridge.javabean.jaxb.model.POType;
import org.milyn.commons.io.StreamUtils;
import org.milyn.cartridge.javabean.binding.xml.XMLBinding;
import org.milyn.payload.StringSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringWriter;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class POTypeTest extends TestCase {

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
