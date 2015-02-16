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
package org.milyn.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Set;
import java.util.List;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class CollectionsUtilTest {

	@Test
    public void test_Set_01() {
        Set<String> strings = CollectionsUtil.toSet("1", "2", "3");
        assertTrue(strings.contains("1"));
        assertTrue(strings.contains("2"));
        assertTrue(strings.contains("3"));
    }

	@Test
    public void test_Set_02() {
        Set<Integer> integers = CollectionsUtil.toSet(1, 2, 3);
        assertTrue(integers.contains(1));
        assertTrue(integers.contains(2));
        assertTrue(integers.contains(3));
    }

	@Test
    public void test_Set_03() {
        Set<Integer> integers = CollectionsUtil.toSet();
        assertEquals(0, integers.size());
        Set<String> strings = CollectionsUtil.toSet();
        assertEquals(0, strings.size());
    }

	@Test
    public void test_List_01() {
        List<String> strings = CollectionsUtil.toList("1", "2", "3");
        assertTrue(strings.contains("1"));
        assertTrue(strings.contains("2"));
        assertTrue(strings.contains("3"));
    }

	@Test
    public void test_List_02() {
        List<Integer> integers = CollectionsUtil.toList(1, 2, 3);
        assertTrue(integers.contains(1));
        assertTrue(integers.contains(2));
        assertTrue(integers.contains(3));
    }
}
