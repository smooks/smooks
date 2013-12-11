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
package org.milyn.payload;

import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * Utility class for creating a Byte based {@link javax.xml.transform.stream.StreamResult}.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ByteResult extends StreamResult {

    private ByteArrayOutputStream result = new ByteArrayOutputStream();

    public ByteResult() {
        super();
        super.setOutputStream(result);
    }

    public final void setOutputStream(OutputStream outputStream) {
        throw new UnsupportedOperationException("Cannot reset the OutputStream for this Result type.");
    }

    public byte[] getResult() {
        return result.toByteArray();
    }

    public String toString() {
        return result.toString();
    }
}