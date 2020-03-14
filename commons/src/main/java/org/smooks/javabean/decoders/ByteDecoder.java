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
package org.smooks.javabean.decoders;

import org.smooks.javabean.DataDecodeException;
import org.smooks.javabean.DataDecoder;
import org.smooks.javabean.DecodeType;

/**
 * Byte Decoder
 * 
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
@DecodeType({Byte.class, byte.class})
public class ByteDecoder implements DataDecoder {

    public Object decode(String data) throws DataDecodeException {
        try {
            return Byte.parseByte(data.trim());
        } catch(NumberFormatException e) {
            throw new DataDecodeException("Failed to decode Byte value '" + data + "'.", e);
        }
    }
}
