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
package org.smooks.javabean.JIRA.MILYN_238;

import org.junit.Test;
import static org.junit.Assert.*;
import org.smooks.delivery.sax.TrackedStringWriter;

import java.io.IOException;

/**
 * http://jira.codehaus.org/browse/MILYN-238
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class MILYN238Test {

    @Test
    public void test() throws IOException {
        char[] text = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        TrackedStringWriter writer = new TrackedStringWriter();

        writer.write(text, 0, 5);
        writer.write(text, 0, 5);
        writer.write(text, 0, 5);
        writer.write(text, 5, 5);
        writer.write(text, 10, 5);
        writer.write(text, 0, 5);
        writer.write(text, 10, 5);
        writer.write(text, 0, 5);
        writer.write(text, 15, 5);

        assertEquals("abcdefghijklmnopqrst", writer.toString());
    }
}
