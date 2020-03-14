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
package org.smooks.resource;

import java.net.URI;

import org.junit.Test;
import static org.junit.Assert.*;


/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class MILYN_374_Test {

	@Test
	public void test_nobase() {
		URIResourceLocator locator = new URIResourceLocator();
		
		URI uri = locator.resolveURI("/lesson_05/_json_configs/smooks-config-reader-only.xml");
		
		assertEquals("lesson_05/_json_configs/smooks-config-reader-only.xml", uri.toString());
	}

	@Test
	public void test_base_01() {
		URIResourceLocator locator = new URIResourceLocator();
		
		locator.setBaseURI(URI.create("lesson_05/_json_configs/"));
		URI uri = locator.resolveURI("/lesson_05/_json_configs/smooks-config-reader-only.xml");
		
		assertEquals("lesson_05/_json_configs/smooks-config-reader-only.xml", uri.toString());
	}

	@Test
	public void test_base_02() {
		URIResourceLocator locator = new URIResourceLocator();
		
		locator.setBaseURI(URI.create("lesson_05/_json_configs/"));
		URI uri = locator.resolveURI("lesson_05/_json_configs/smooks-config-reader-only.xml");
		
		assertEquals("lesson_05/_json_configs/lesson_05/_json_configs/smooks-config-reader-only.xml", uri.toString());
	}
}
