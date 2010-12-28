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
package org.milyn.delivery.sax.terminate;

import org.milyn.delivery.sax.SAXElement;

/**
 * Terminate Exception.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class TerminateException extends RuntimeException {
	
	private SAXElement element;
	private boolean terminateBefore;

	/**
	 * Public constructor.
	 * @param element The element on which the terminate was fired.
	 * @param terminateBefore 
	 */
	public TerminateException(SAXElement element, boolean terminateBefore) {
		this.element = element;
		this.terminateBefore = terminateBefore;
	}

	/**
	 * Get the element on which the terminate was fired.
	 * @return The element on which the terminate was fired.
	 */
	public SAXElement getElement() {
		return element;
	}

	/**
	 * Is this exception a visitBefore TerminateException.
	 * @return True if the TerminateException was thrown on the visitBefore of the
	 * target element, otherwise false.
	 */
	public boolean isTerminateBefore() {
		return terminateBefore;
	}
}
