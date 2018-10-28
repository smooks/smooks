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
package org.milyn.cdr;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ConfigSearchTest {

	@Test
	public void test() {
		SmooksResourceConfiguration config = new SmooksResourceConfiguration();
		ConfigSearch search = new ConfigSearch();
		
		assertTrue(search.matches(config));

		config.setSelector("a");
		assertTrue(search.matches(config));
		assertTrue(search.selector("a").matches(config));
		assertFalse(search.selector("b").matches(config));
		search.selector(null); // clear it
		assertTrue(search.matches(config));

		config.setExtendedConfigNS("http://x/y");
		assertTrue(search.matches(config));
		assertTrue(search.configNS("http://x/y").matches(config));
		assertTrue(search.configNS("http://x").matches(config));
		assertFalse(search.configNS("http://x/z").matches(config));
		search.configNS(null); // clear it
		assertTrue(search.matches(config));

		config.setSelectorNamespaceURI("http://x/y");
		assertTrue(search.selectorNS("http://x/y").matches(config));
		assertFalse(search.selectorNS("http://x/z").matches(config));
		search.selectorNS(null); // clear it
		assertTrue(search.matches(config));

		config.setResource("a");
		assertTrue(search.resource("a").matches(config));
		assertFalse(search.resource("b").matches(config));
		search.resource(null); // clear it
		assertTrue(search.matches(config));
		
		search.param("a", "1");
		assertFalse(search.matches(config));
		config.setParameter("a", "1");
		assertTrue(search.matches(config));		
	}
}
