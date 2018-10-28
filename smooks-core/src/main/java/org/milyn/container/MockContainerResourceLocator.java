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

package org.milyn.container;

import org.milyn.io.StreamUtils;
import org.milyn.resource.ContainerResourceLocator;

import java.io.*;
import java.net.URI;
import java.util.Hashtable;


@SuppressWarnings("WeakerAccess")
public class MockContainerResourceLocator implements ContainerResourceLocator {

	public static final File TEST_STANDALONE_CTX_BASE = new File("src/test/standalone-ctx");
	private Hashtable streams = new Hashtable();

	@SuppressWarnings("unchecked")
	public void setResource(String nameOrUri, InputStream stream) {
		try {
			byte[] streamData = StreamUtils.readStream(stream);
			streams.put(nameOrUri, streamData);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public InputStream getResource(String configName, String defaultUri) throws IllegalArgumentException, IOException {
		return getResource(defaultUri);
	}

	public InputStream getResource(String uri) throws IllegalArgumentException, IOException {
		String relUri = uri;

        if(uri.charAt(0) == '\\' || uri.charAt(0) == '/') {
        	relUri = uri.substring(1);
        }
		// Try loading the resource from the standalone test context
    	File resFile = new File(TEST_STANDALONE_CTX_BASE, relUri);
    	if(resFile.exists() && !resFile.isDirectory()) {
    		return new FileInputStream(resFile);
    	}

		// Check has it been set in this mock instance.
		byte[] resBytes = (byte[])streams.get(uri);
		if(resBytes == null) {
			throw new IllegalStateException("Resource [" + uri + "] not set in MockContainerResourceLocator OR loadable from the test standalone context.  Use MockContainerResourceLocator.setResource()");
		}

		return new ByteArrayInputStream(resBytes);
	}

	public URI getBaseURI() {
		return URI.create("./");
	}
}
