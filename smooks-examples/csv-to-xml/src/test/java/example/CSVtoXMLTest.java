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
package example;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.ByteArrayInputStream;

import org.xml.sax.SAXException;
import org.milyn.commons.io.StreamUtils;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class CSVtoXMLTest extends TestCase {

    public void test() throws IOException, SAXException {
        byte[] expected = StreamUtils.readStream(getClass().getResourceAsStream("expected.xml"));
        String result = Main.runSmooksTransform();

        StringBuffer s1 = StreamUtils.trimLines(new ByteArrayInputStream(expected));
        StringBuffer s2 = StreamUtils.trimLines(new ByteArrayInputStream(result.getBytes()));

        assertEquals("Expected:\n" + s1 + "\nActual:\n" + s2, s1.toString(), s2.toString());
    }
}
