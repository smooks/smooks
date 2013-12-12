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

package org.milyn.commons.resource;

import java.io.InputStream;

import junit.framework.TestCase;

public class ClasspathResourceLocatorTest extends TestCase {

	public void test_getResource() {
		ClasspathResourceLocator cpResLocator = new ClasspathResourceLocator();

		testArgs(cpResLocator, null);
		testArgs(cpResLocator, "http://a/b.txt");
		testArgs(cpResLocator, "a/b.txt");

		InputStream stream = cpResLocator.getResource("/a.adf");
		assertNotNull("Expected a resource stream.", stream);

		stream = cpResLocator.getResource("/x.txt");
		assertNull("Expected no resource stream.", stream);
	}

	/**
	 * @param cpResLocator
	 */
	private void testArgs(ClasspathResourceLocator cpResLocator, String uri) {
		try {
			cpResLocator.getResource(uri);
			fail("Expected IllegalArgumentException.");
		} catch (IllegalArgumentException e) {
			// OK
		}
	}
}
