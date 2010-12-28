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
package org.milyn;

import java.io.InputStreamReader;

public class TestConstants {

    public static final int NUM_WARMUPS = 100;
    public static final int NUM_ITERATIONS = 10000;
	

    public static InputStreamReader getMessageReader() {
        return new InputStreamReader(TestConstants.class.getResourceAsStream("39910.xml"));
    }
}
