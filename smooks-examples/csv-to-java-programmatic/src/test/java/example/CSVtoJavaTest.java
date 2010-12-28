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
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class CSVtoJavaTest extends TestCase {

    public void test() throws IOException, SAXException {
        List result = Main.runSmooksTransform();

        assertEquals("[[charles, moulliard, Male, 43, belgium], [maxence, dewil, Male, 30, belgium], [eleonor, moulliard, Female, 12, belgium]]", result.toString());
    }
}
