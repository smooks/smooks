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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * {@link BigInteger} Decoder.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@DecodeType(BigInteger.class)
public class BigIntegerDecoder extends NumberDecoder {

    public Object decode(String data) throws DataDecodeException {
        NumberFormat format = getNumberFormat();

        if(format != null) {
            try {
                Number number = format.parse(data.trim());

                if(number instanceof BigInteger) {
                    return number;
                } else if(number instanceof BigDecimal) {
                    return ((BigDecimal) number).toBigInteger();
                }

                return new BigInteger(String.valueOf(number.intValue()));
            } catch (ParseException e) {
                throw new DataDecodeException("Failed to decode BigInteger value '" + data + "' using NumberFormat instance " + format + ".", e);
            }
        } else {
            try {
                return new BigInteger(data.trim());
            } catch(NumberFormatException e) {
                throw new DataDecodeException("Failed to decode BigInteger value '" + data + "'.", e);
            }
        }
    }
}
