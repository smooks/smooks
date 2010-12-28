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
package org.milyn.scribe;

/**
 * A IllegalAnnotationUsageException is thrown when a Annotation
 * was used in a wrong 'illegal' way.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class IllegalAnnotationUsageException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = -8613863508327591649L;

	/**
	 *
	 */
	public IllegalAnnotationUsageException() {
	}

	/**
	 * @param message
	 */
	public IllegalAnnotationUsageException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public IllegalAnnotationUsageException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public IllegalAnnotationUsageException(String message, Throwable cause) {
		super(message, cause);
	}

}
