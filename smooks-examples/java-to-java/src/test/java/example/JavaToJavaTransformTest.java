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

import example.srcmodel.Order;
import example.trgmodel.LineOrder;
import org.junit.Test;
import static org.junit.Assert.*;
import org.milyn.io.StreamUtils;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class JavaToJavaTransformTest {

	@Test
    public void test() throws IOException, SAXException {
        byte[] expected_res = StreamUtils.readStream(getClass().getResourceAsStream("expected.txt"));
        Main smooksMain = new Main();
        LineOrder lineOrder;

        lineOrder = smooksMain.runSmooksTransform(new Order());
        assertTrue("Expected:\n" + new String(expected_res) + ". \nGot:\n" + lineOrder, StreamUtils.compareCharStreams(new ByteArrayInputStream(expected_res), new ByteArrayInputStream(lineOrder.toString().getBytes())));
    }
}