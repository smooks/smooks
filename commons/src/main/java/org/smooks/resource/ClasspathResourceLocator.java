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

import java.io.InputStream;
import java.net.URI;

import org.smooks.util.ClassUtil;

/**
 * Classpath Resource Locator. <p/> Loads resources from the classpath. The URI
 * must have no scheme or authority components, and have a leading slash
 * character ('/') on the path i.e. it must be relative to the root of the
 * classpath.
 * 
 * @author tfennelly
 */
public class ClasspathResourceLocator implements ContainerResourceLocator {

	public InputStream getResource(String configName, String defaultUri)
			throws IllegalArgumentException {
		return getResource(defaultUri);
	}

	public InputStream getResource(String uri) throws IllegalArgumentException {
		if (uri == null) {
			throw new IllegalArgumentException("null 'uri' arg in method call.");
		} else if (uri.charAt(0) != '/') {
			throw new IllegalArgumentException(
					"classpath 'uri' must be a valid classpath with a leading '/' char on the path i.e. specified relative to the root of the classpath.");
		}

		return ClassUtil.getResourceAsStream(uri, getClass());
	}

	public URI getBaseURI() {
		return null;
	}
}
