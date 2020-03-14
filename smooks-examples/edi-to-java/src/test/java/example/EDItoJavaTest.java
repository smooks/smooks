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

import com.thoughtworks.xstream.XStream;
import org.junit.Assert;
import org.junit.Test;
import static org.smooks.io.StreamUtils.compareCharStreams;
import static org.smooks.io.StreamUtils.readStreamAsString;
import org.smooks.payload.JavaResult;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class EDItoJavaTest {

    @Test
    public void test() throws IOException, SAXException {
        String expected = readStreamAsString(getClass().getResourceAsStream("expected.xml"));
        Main smooksMain = new Main();

        JavaResult result = smooksMain.runSmooksTransform();

        XStream xstream = new XStream();
        String actual = xstream.toXML(result.getBean("order"));

        actual = actual.replaceFirst("<date>.*</date>", "<date/>");

        boolean matchesExpected = compareCharStreams(new StringReader(expected), new StringReader(actual));
        if (!matchesExpected) {
            Assert.assertEquals("Actual does not match expected.", expected, actual);
        }
    }
}
