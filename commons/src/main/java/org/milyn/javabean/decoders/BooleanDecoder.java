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
package org.milyn.javabean.decoders;

import java.util.Arrays;
import java.util.List;

import org.milyn.javabean.DataDecodeException;
import org.milyn.javabean.DataDecoder;
import org.milyn.javabean.DecodeType;

/**
 * Boolean decoder.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@DecodeType({Boolean.class, boolean.class})
public class BooleanDecoder implements DataDecoder {

    private static List<String> trueValues = Arrays.asList("y", "yes", "true", "1");
    private static List<String> falseValues = Arrays.asList("n", "no", "false", "0");
    
    public Object decode(String data) throws DataDecodeException {
        Boolean retval = null;
        if(trueValues.contains(data.trim().toLowerCase())) {
            retval = Boolean.TRUE;
        } else if(falseValues.contains(data.trim().toLowerCase())) {
            retval = Boolean.FALSE;
        }
        if(null==retval) {
            throw new DataDecodeException("Failed to decode Boolean value '" + data + "'.");
        }
        return retval;
    }
}
