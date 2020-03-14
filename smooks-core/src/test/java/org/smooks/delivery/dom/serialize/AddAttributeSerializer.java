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
package org.smooks.delivery.dom.serialize;

import org.w3c.dom.NamedNodeMap;

import java.io.Writer;
import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class AddAttributeSerializer extends DefaultSerializationUnit {

    protected void writeAttributes(NamedNodeMap attributes, Writer writer) throws IOException {
        super.writeAttributes(attributes, writer);
        writer.write(" someattrib=\"hasval\"");
    }
}
