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

import java.util.Date;

/**
* {@link java.sql.Date} data decoder.
* <p/>
* Extends {@link org.smooks.javabean.decoders.DateDecoder} and returns
* a java.sql.Date instance.
* <p/>
*
* @author <a href="mailto:daniel.bevenius@gmail.com">daniel.bevenius@gmail.com</a>
*/
@DecodeType(java.sql.Date.class)
public class SqlDateDecoder extends DateDecoder
{
	@Override
	public Object decode(String data) throws DataDecodeException {
		Date date = (Date)super.decode(data);
	    return new java.sql.Date(date.getTime());
	}
}


