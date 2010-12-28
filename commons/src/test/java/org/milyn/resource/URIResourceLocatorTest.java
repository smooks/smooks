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

package org.milyn.resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.TestCase;

/**
 * @author tfennelly
 */
public class URIResourceLocatorTest extends TestCase {

	private File file = new File("testfilex.zap");

	protected void setUp() throws Exception {
		file.createNewFile();
	}

	protected void tearDown() throws Exception {
		file.delete();
	}

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

    public void test_fileBasedResource() throws IllegalArgumentException, IOException, URISyntaxException {
        URIResourceLocator locator = new URIResourceLocator();
        InputStream stream;

        // Resource exists - no scheme - should get it from the filesystem ...
        stream = locator.getResource("src/test/java/org/milyn/resource/somefile.txt");
        assertNotNull(stream);

        // Try it again now with a non-default base URI...
        locator.setBaseURI(new URI("src/test"));
        stream = locator.getResource("java/org/milyn/resource/somefile.txt");
        assertNotNull(stream);
    }

	public void test_classpathBasedResource() throws IllegalArgumentException, IOException {
		URIResourceLocator locator = new URIResourceLocator();
		InputStream stream;

		// Resource doesn't exists...
		try {
			stream = locator.getResource("classpath:/org/milyn/resource/someunknownfile.txt");
			fail("Expected IOException for bad resource path.");
		} catch (IOException e) {
			assertTrue(e.getMessage().startsWith(
					"Failed to access data stream for"));
		}

		// Resource exists...
		stream = locator.getResource("classpath:/org/milyn/resource/somefile.txt");
		assertNotNull(stream);

        // Resource exists - no scheme - should get it from the classpath ...
		stream = locator.getResource("/org/milyn/resource/somefile.txt");
		assertNotNull(stream);
	}

	public void test_setBaseURI() throws IllegalArgumentException, IOException {
		URIResourceLocator locator = new URIResourceLocator();
		InputStream stream;

		locator.setBaseURI(URI.create("classpath:/"));

		// Resource exists...
		stream = locator.getResource("/org/milyn/resource/somefile.txt");
		assertNotNull(stream);
	}
}
