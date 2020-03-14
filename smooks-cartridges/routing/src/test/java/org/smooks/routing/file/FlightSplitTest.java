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
package org.smooks.routing.file;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.smooks.Smooks;
import org.xml.sax.SAXException;
import org.custommonkey.xmlunit.XMLUnit;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class FlightSplitTest {

    @Before
    protected void setUp() throws Exception {
        new File("target/flights/BA-1234.xml").delete();
        new File("target/flights/BA-5678.xml").delete();
    }

    @Test
    public void test() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("flight-split.xml"));

        try {
            smooks.filterSource(new StreamSource(getClass().getResourceAsStream("flight.xml")));
        } finally {
            smooks.close();
        }

        XMLUnit.setIgnoreWhitespace(true);

        FileReader fileReader = new FileReader(new File("target/flights/BA-1234.xml"));
        try {
            assertTrue(XMLUnit.compareXML(fileReader, new InputStreamReader(getClass().getResourceAsStream("BA-1234.xml"))).identical());
        } finally {
            fileReader.close();
        }

        fileReader = new FileReader(new File("target/flights/BA-5678.xml"));
        try {
            assertTrue(XMLUnit.compareXML(fileReader, new InputStreamReader(getClass().getResourceAsStream("BA-5678.xml"))).identical());
        } finally {
            fileReader.close();
        }
    }
}
