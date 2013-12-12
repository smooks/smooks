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

import org.milyn.commons.javabean.DataDecoder;
import org.milyn.commons.javabean.DataDecodeException;
import org.milyn.commons.javabean.DecodeType;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * The BinaryDecoder validates that CharacterSequence only consists of zeros and ones.
 *  
 * @author bardl
 */
@DecodeType(String.class)
public class BinaryDecoder implements DataDecoder {

    private static final Pattern BINARY_PATTERN = Pattern.compile("^[01]+$");

    public Object decode(String data) throws DataDecodeException {
        Matcher binaryMatcher = BINARY_PATTERN.matcher(data);
        if (binaryMatcher.matches()) {
            return data;
        } else {
            throw new DataDecodeException("Failed to decode binary sequence '" + data + "'."); 
        }
    }
}
