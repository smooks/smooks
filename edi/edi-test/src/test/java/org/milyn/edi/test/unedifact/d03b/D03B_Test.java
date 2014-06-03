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
package org.milyn.edi.test.unedifact.d03b;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.milyn.edi.test.EdifactDirTestHarness;
import org.milyn.io.StreamUtils;
import org.milyn.payload.JavaResult;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class D03B_Test {

    private static EdifactDirTestHarness d03bHarness;

    @Before
    public void setUp() throws Exception {
        ClassLoader loader = Thread.currentThread().getContextClassLoader() == null ? Thread.currentThread().getContextClassLoader() : D03B_Test.class.getClassLoader();
        URL unedifactZip = loader.getResource("d03b.zip");
        d03bHarness = new EdifactDirTestHarness(new File(unedifactZip.toURI()), "PAXLST");
    }

    @Test
    public void test_PAXLST() throws IOException {
        d03bHarness.assertJavaReadWriteOK(getClass().getResourceAsStream("PAXLST.edi"));
    }

    @Test
    public void test_PAXLST_test_XML() throws IOException, SAXException {
        d03bHarness.assertXMLOK(getClass().getResourceAsStream("PAXLST.edi"), getClass().getResourceAsStream("PAXLST.xml"));
    }

    @Test
    public void test_PAXLST_test_fragment_split() throws IOException, SAXException {
        JavaResult result = new JavaResult();

        d03bHarness.smooksFilterSource("/org/milyn/edi/test/unedifact/d03b/smooks-unedifact-split.xml", new StreamSource(getClass().getResourceAsStream("PAXLST.edi")), result);

        String expectedXML = StreamUtils.readStreamAsString(getClass().getResourceAsStream("PAXLST_frag.xml"));
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.compareXML(expectedXML, (String) result.getBean("PAXLST_frag"));
    }
}
