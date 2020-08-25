/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

/**
 * @author tfennelly
 */
public class URIResourceLocatorTest {

	private final File file = new File("testfilex.zap");

	@Before
	public void setUp() throws Exception {
		file.createNewFile();
	}

	@After
	public void tearDown() throws Exception {
		file.delete();
	}

	@Test
	public void test_urlBasedResource() throws IllegalArgumentException,
			IOException {
		URIResourceLocator locator = new URIResourceLocator();
		InputStream stream;

		// Resource doesn't exist...
		try {
			stream = locator.getResource((new File("nofile")).toURI()
					.toString());
			fail("Expected FileNotFoundException.");
		} catch (FileNotFoundException e) {
			// OK
		}

		// Resource exists...
		stream = locator.getResource(file.toURI().toString());
		assertNotNull(stream);
		stream.close();
	}

	@Test
    public void test_fileBasedResource() throws IllegalArgumentException, IOException, URISyntaxException {
        URIResourceLocator locator = new URIResourceLocator();
        InputStream stream;

        // Resource exists - no scheme - should get it from the filesystem ...
        stream = locator.getResource("src/test/java/org/smooks/resource/somefile.txt");
        assertNotNull(stream);

        // Try it again now with a non-default base URI...
        locator.setBaseURI(new URI("src/test"));
        stream = locator.getResource("java/org/smooks/resource/somefile.txt");
        assertNotNull(stream);
    }

	@Test
	public void test_classpathBasedResource() throws IllegalArgumentException, IOException {
		URIResourceLocator locator = new URIResourceLocator();
		InputStream stream;

		// Resource doesn't exists...
		try {
			stream = locator.getResource("classpath:/org/smooks/resource/someunknownfile.txt");
			fail("Expected IOException for bad resource path.");
		} catch (IOException e) {
			assertTrue(e.getMessage().startsWith(
					"Failed to access data stream for"));
		}

		// Resource exists...
		stream = locator.getResource("classpath:/org/smooks/resource/somefile.txt");
		assertNotNull(stream);

        // Resource exists - no scheme - should get it from the classpath ...
		stream = locator.getResource("/org/smooks/resource/somefile.txt");
		assertNotNull(stream);
	}

	@Test
	public void test_setBaseURI() throws IllegalArgumentException, IOException {
		URIResourceLocator locator = new URIResourceLocator();
		InputStream stream;

		locator.setBaseURI(URI.create("classpath:/"));

		// Resource exists...
		stream = locator.getResource("/org/smooks/resource/somefile.txt");
		assertNotNull(stream);
	}
}
