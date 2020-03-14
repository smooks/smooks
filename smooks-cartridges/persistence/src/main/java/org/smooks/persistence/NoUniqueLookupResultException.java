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
package org.smooks.persistence;

import org.smooks.SmooksException;

/**
 * @author maurice
 *
 */
public class NoUniqueLookupResultException extends SmooksException {

	/**
	 *
	 */
	private static final long serialVersionUID = -8531504732429373509L;

	/**
	 * @param message
	 */
	public NoUniqueLookupResultException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NoUniqueLookupResultException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
