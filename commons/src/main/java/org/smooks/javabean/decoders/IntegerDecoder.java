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
import org.smooks.javabean.DecodeType;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Integer Decoder.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@DecodeType({Integer.class, int.class})
public class IntegerDecoder extends NumberDecoder {

    public Object decode(String data) throws DataDecodeException {
        NumberFormat format = getNumberFormat();

        if(format != null) {
            try {
                Number number = format.parse(data.trim());

                if(isPercentage()) {
                    return (int) (number.doubleValue() * 100);
                } else {
                    return number.intValue();
                }
            } catch (ParseException e) {
                throw new DataDecodeException("Failed to decode Integer value '" + data + "' using NumberFormat instance " + format + ".", e);
            }
        } else {
            try {
                return Integer.parseInt(data.trim());
            } catch(NumberFormatException e) {
                throw new DataDecodeException("Failed to decode Integer value '" + data + "'.", e);
            }
        }
    }
}
