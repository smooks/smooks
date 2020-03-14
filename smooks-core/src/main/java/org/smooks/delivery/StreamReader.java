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
package org.smooks.delivery;

/**
 * This is a marker interface that specify ability to work with <b>binary</b> streams.
 *
 * @author <a href="mailto:igorya@gmail.com">igorya@gmail.com</a>
 * @deprecated No longer required.  {@link AbstractParser} always creates an {@link org.xml.sax.InputSource}
 * that contains both an {@link java.io.InputStream} and a {@link java.io.Reader}, XMLReader implementation
 * can pick whichever it needs and no need to mark it as requiring one or the other.
 */

public interface StreamReader {

}
