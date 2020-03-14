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

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;

public class StandaloneResourceLocatorTest {

	@Test
	public void testGetResourceLocator() {
		URIResourceLocator standAloneResLocator = new URIResourceLocator();

		standAloneResLocator.setBaseURI((new File("src/test/java")).toURI());

		try {
			standAloneResLocator.getResource(
					null, "xxxxyz.txt");
			fail("Expected exception on non-existant file resource.");
		} catch (IOException e) {
			// OK
		}
		try {
			standAloneResLocator.getResource(
					null, "a.adf");
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

}
