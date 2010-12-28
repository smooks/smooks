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

/**
 * Thrown when an request is made to load an unknown CDRArchiveEntry.
 * <p/>
 * An unknown CDRArchiveEntry is defined as an entry which was not loaded from 
 * any of the loaded cdrar files.
 * @author tfennelly
 */
public class ResourceConfigurationNotFoundException extends RuntimeException {

	/**
     * Serail UID.
     */
    private static final long serialVersionUID = 1L;

    /**
	 * Public constructor.
	 * @param message The exception message.
	 */
	public ResourceConfigurationNotFoundException(String message) {
		super(message);
	}
}
