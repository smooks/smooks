package org.smooks.javabean.decoders;

import java.util.Date;

import org.smooks.javabean.DataDecodeException;
import org.smooks.javabean.DecodeType;

/**
* {@link java.sql.Timestamp} data decoder.
* <p/>
* Extends {@link org.smooks.javabean.decoders.DateDecoder} and returns
* a java.sql.Timestamp instance.
* <p/>
*
* @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
*/
@DecodeType(java.sql.Timestamp.class)
public class SqlTimestampDecoder extends DateDecoder
{
	@Override
	public Object decode(String data) throws DataDecodeException {
		Date date = (Date)super.decode(data);
	    return new java.sql.Timestamp(date.getTime());
	}
}

