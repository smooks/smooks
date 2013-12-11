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
package org.milyn.commons.javabean.decoders;

import junit.framework.TestCase;

import java.util.Arrays;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class CSVDecoderTest extends TestCase {

    public void test() {
        CSVDecoder decoder = new CSVDecoder();
        String[] values;

        values = (String[]) decoder.decode("a,b,c");
        assertEquals("[a, b, c]", Arrays.asList(values).toString());

        values = (String[]) decoder.decode("\na\n,\nb\n,\nc\n");
        assertEquals("[a, b, c]", Arrays.asList(values).toString());
    }
}