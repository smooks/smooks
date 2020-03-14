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

package org.smooks.dtd;

/**
 * DTD Element not defined.
 * @author tfennelly
 */
public class ElementNotDefined extends RuntimeException {

	/**
     * Serial UID.
     */
    private static final long serialVersionUID = 1L;

    /**
	 * Public constructor.
	 * @param message Exception message.
	 */
	public ElementNotDefined(String message) {
		super(message);
	}
}
