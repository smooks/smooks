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

package org.milyn.commons.cdr;

/**
 * Smooks resource configuration exception.
 * @author tfennelly
 */
public class SmooksConfigurationException extends RuntimeException {

    /**
     * Serial UID.
     */
    private static final long serialVersionUID = 1L;

    public SmooksConfigurationException() {
        super();
    }

    public SmooksConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SmooksConfigurationException(String message) {
        super(message);
    }

    public SmooksConfigurationException(Throwable cause) {
        super(cause);
    }

}
