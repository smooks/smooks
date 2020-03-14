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
package org.smooks.javabean;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class BeanUtilsTest {

    @Test
    public void test_convertListToArray() {
        ArrayList<Object> list = new ArrayList<Object>();

        // An Object array...
        list.add(new Integer(3));
        list.add(new Float(3f));
        list.add(3);
        Number[] array1 = (Number[]) BeanUtils.convertListToArray(list, Number.class);
        assertEquals(new Integer(3), array1[0]);
        assertEquals(new Float(3f), array1[1]);
        assertEquals(3, array1[2]);

        list.clear();

        // A Primitive array...
        list.add(1);
        list.add(2);
        list.add(3);
        int[] array2 = (int[]) BeanUtils.convertListToArray(list, Integer.TYPE);
        assertEquals(1, array2[0]);
        assertEquals(2, array2[1]);
        assertEquals(3, array2[2]);
    }
}
