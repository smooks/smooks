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

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;

/**
 * Utility class for creating a Byte based {@link javax.xml.transform.stream.StreamSource}.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ByteSource extends StreamSource {

    private byte[] source;

    public ByteSource(byte[] source) {
        super(new ByteArrayInputStream(source));
        this.source = source;
    }

    public byte[] getSource() {
        return source;
    }
}