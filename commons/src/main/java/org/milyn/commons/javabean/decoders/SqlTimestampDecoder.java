package org.milyn.commons.javabean.decoders;

import org.milyn.commons.javabean.DataDecodeException;
import org.milyn.commons.javabean.DecodeType;

import java.util.Date;

/**
 * {@link java.sql.Timestamp} data decoder.
 * <p/>
 * Extends {@link org.milyn.commons.javabean.decoders.DateDecoder} and returns
 * a java.sql.Timestamp instance.
 * <p/>
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
@DecodeType(java.sql.Timestamp.class)
public class SqlTimestampDecoder extends DateDecoder {
    @Override
    public Object decode(String data) throws DataDecodeException {
        Date date = (Date) super.decode(data);
        return new java.sql.Timestamp(date.getTime());
    }
}

