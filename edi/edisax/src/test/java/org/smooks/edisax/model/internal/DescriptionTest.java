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
package org.smooks.edisax.model.internal;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DescriptionTest {

        @Test
	public void test() {
		assertEquals(new Description().setName("A").setVersion("1"), new Description().setName("A").setVersion("1"));
		assertNotSame(new Description().setName("A").setVersion("2"), new Description().setName("A").setVersion("1"));
		assertNotSame(new Description().setName("B").setVersion("1"), new Description().setName("A").setVersion("1"));

		assertEquals(new Description().setName("A").setVersion("1"), "A");
		assertNotSame(new Description().setName("A").setVersion("1"), "B");
		
		Map<Description, String> map = new HashMap<Description, String>();
		map.put(new Description().setName("A").setVersion("1"), "A1");		
		map.put(new Description().setName("B").setVersion("2"), "B2");
		
		assertEquals("A1", map.get(new Description().setName("A").setVersion("1")));
		assertEquals("B2", map.get(new Description().setName("B").setVersion("2")));
	}
}
