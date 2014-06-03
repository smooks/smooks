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
import org.junit.Ignore;
import org.milyn.io.StreamUtils;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author <a href="daniel.bevenius@redpill.se">Daniel Bevenius</a>
 */
@Ignore
public class BasicXslTransformTest extends TestCase {

    public void test() throws IOException, SAXException, URISyntaxException {
        byte[] expected = StreamUtils.readStream(getClass().getResourceAsStream("expected.xml"));
        String result = Main.runSmooksTransform();

        assertTrue(StreamUtils.compareCharStreams(new ByteArrayInputStream(expected), new ByteArrayInputStream(result.getBytes())));
    }
}
