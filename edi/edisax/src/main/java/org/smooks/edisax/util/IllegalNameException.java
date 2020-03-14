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
package org.smooks.edisax.util;

/**
 * IllegalNameException is used when a JClass or JField is given a name matching a reserved keyword in java.
 *
 * @author bardl
 */
public class IllegalNameException extends Exception {

    public IllegalNameException() {
        super();
    }

    public IllegalNameException(String message) {
        super(message);
    }

    public IllegalNameException(Throwable cause) {
        super(cause);
    }

    public IllegalNameException(String message, Throwable cause) {
        super(message, cause);
    }
}
