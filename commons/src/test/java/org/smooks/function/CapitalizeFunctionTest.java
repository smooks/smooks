/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (version 2.1) as published by the Free Software
 * Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */

package org.smooks.function;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
public class CapitalizeFunctionTest {

    public void test_execute() {
        CapitalizeFunction function = new CapitalizeFunction();

        assertEquals("Maurice", function.execute("maurice"));
        assertEquals("Maurice", function.execute("Maurice"));
        assertEquals(" Maurice", function.execute(" maurice"));
        assertEquals("Maurice Zeijen", function.execute("maurice zeijen"));
        assertEquals(" Maurice\nZeijen", function.execute(" MAURICE\nZEIJEN"));
    }

}