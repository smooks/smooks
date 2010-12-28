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

import junit.framework.*;
import org.xml.sax.*;

import java.io.*;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class EDItoJavaTest extends TestCase {

    public void test() throws IOException, SAXException {
        String expected = org.milyn.io.StreamUtils.readStreamAsString(getClass().getResourceAsStream("expected.xml"));
        Main smooksMain = new Main();

        org.milyn.payload.JavaResult result = smooksMain.runSmooksTransform();

        com.thoughtworks.xstream.XStream xstream = new com.thoughtworks.xstream.XStream();
        String actual = xstream.toXML(result.getBean("order"));

        actual = actual.replaceFirst("<date>.*</date>", "<date/>");

        boolean matchesExpected = org.milyn.io.StreamUtils.compareCharStreams(new java.io.StringReader(expected), new java.io.StringReader(actual));
        if(!matchesExpected) {
            assertEquals("Actual does not match expected.", expected, actual);
        }
    }
}
