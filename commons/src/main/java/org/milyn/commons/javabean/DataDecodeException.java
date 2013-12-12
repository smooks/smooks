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
package org.milyn.commons.javabean;

import org.milyn.commons.SmooksException;

/**
 * Data Decode Exception.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DataDecodeException extends SmooksException {

    /**
     * Public constructor.
     *
     * @param message Exception message.
     */
    public DataDecodeException(String message) {
        super(message);
    }

    /**
     * Public constructor.
     *
     * @param message Exception message.
     * @param cause   Exception cause chain.
     */
    public DataDecodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
