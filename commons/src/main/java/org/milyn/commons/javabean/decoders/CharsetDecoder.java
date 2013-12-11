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
package org.milyn.commons.javabean.decoders;

import org.milyn.commons.javabean.DecodeType;
import org.milyn.commons.javabean.DataDecoder;
import org.milyn.commons.javabean.DataDecodeException;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 * String data decoder.
 * <p/>
 * This decoded does nothing.  Simply returns the String data unmodified.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@DecodeType(Charset.class)
public class CharsetDecoder implements DataDecoder {
    public Object decode(String data) throws DataDecodeException {
        try {
            return Charset.forName(data);
        } catch(UnsupportedCharsetException e) {
            throw new DataDecodeException("Unsupported character set '" + data + "'.");
        }
    }
}
