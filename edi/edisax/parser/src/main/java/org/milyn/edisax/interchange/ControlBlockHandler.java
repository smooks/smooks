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
package org.milyn.edisax.interchange;

import java.io.IOException;

import org.xml.sax.SAXException;

/**
 * Interchange control block handler.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface ControlBlockHandler {

	public static final String NAMESPACE = "http://www.milyn.org/schema/edi/un/header-4.1.xsd";

	/**
	 * Process an interchange control block.
	 * @param interchangeContext The interchange context.
     * @throws IOException Error reading from stream.
     * @throws SAXException SAX Error handling segment data.
	 */
	void process(InterchangeContext interchangeContext)  throws IOException, SAXException;
}
