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
package org.milyn.persistence;

import org.milyn.commons.SmooksException;

/**
 * @author maurice
 *
 */
public class NoLookupResultException extends SmooksException {

	/**
	 *
	 */
	private static final long serialVersionUID = -3098674998479850826L;

	/**
	 * @param message
	 */
	public NoLookupResultException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NoLookupResultException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
