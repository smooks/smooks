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

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for locating stream resources external to the container..
 * 
 * @author tfennelly
 */
public interface ExternalResourceLocator {
	/**
	 * Get the stream specified by the 'uri' parameter.
	 * 
	 * @param uri
	 *            The location of the resource to be located.
	 * @return The InputStream associated with the org.smooks.resource.
	 * @throws IllegalArgumentException
	 *             Illegal argument. Check the cause exception for more
	 *             information.
	 * @throws IOException
	 *             Unable to get the org.smooks.resource stream.
	 */
	public InputStream getResource(String uri) throws IllegalArgumentException,
			IOException;
}