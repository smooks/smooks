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
package org.milyn.edi.test.unedifact;

import junit.framework.TestCase;
import org.milyn.edi.test.EdifactDirTestHarness;
import org.milyn.payload.StringResult;
import org.milyn.payload.SystemOutResult;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class D03B_Test extends TestCase {

    private EdifactDirTestHarness d03bHarness = new EdifactDirTestHarness(new File("src/test/resources/d03b.zip"), "PAXLST");

    public void test_PAXLST() throws IOException {
        d03bHarness.assertJavaReadWriteOK(getClass().getResourceAsStream("PAXLST.edi"));
    }

    public void test_PAXLST_test_XML() throws IOException, SAXException {
        d03bHarness.assertXMLOK(getClass().getResourceAsStream("PAXLST.edi"), getClass().getResourceAsStream("PAXLST.xml"));
    }
}
