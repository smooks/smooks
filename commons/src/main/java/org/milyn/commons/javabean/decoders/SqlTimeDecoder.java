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

import org.milyn.commons.javabean.DataDecodeException;
import org.milyn.commons.javabean.DecodeType;

import java.sql.Time;
import java.util.Date;

/**
 * {@link java.sql.Time} data decoder.
 * <p/>
 * Extends {@link org.milyn.commons.javabean.decoders.DateDecoder} and returns
 * a java.sql.Time instance.
 * <p/>
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">daniel.bevenius@gmail.com</a>
 */
@DecodeType(Time.class)
public class SqlTimeDecoder extends DateDecoder {

    public Object decode(String data) throws DataDecodeException {
        Date date = (Date) super.decode(data);
        return new Time(date.getTime());
    }
}
