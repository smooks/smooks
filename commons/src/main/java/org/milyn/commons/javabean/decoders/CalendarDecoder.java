package org.milyn.commons.javabean.decoders;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.milyn.commons.javabean.DataDecodeException;
import org.milyn.commons.javabean.DataDecoder;
import org.milyn.commons.javabean.DecodeType;

/**
 * {@link java.util.Calendar} data decoder.
 * <p/>
 * Decodes the supplied string into a {@link java.util.Calendar} value
 * based on the supplied "{@link java.text.SimpleDateFormat format}" parameter.
 * <p/>
 * This decoder is synchronized on its underlying {@link SimpleDateFormat} instance.
 *
 * @see {@link LocaleAwareDateDecoder}
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 * @author Pavel Kadlec
 * @author <a href="mailto:daniel.bevenius@gmail.com">daniel.bevenius@gmail.com</a>
 *
 */
@DecodeType(Calendar.class)
public class CalendarDecoder extends LocaleAwareDateDecoder implements DataDecoder {

    public Object decode(String data) throws DataDecodeException {
        if (decoder == null) {
            throw new IllegalStateException("Calendar decoder not initialised.  A decoder for this type (" + getClass().getName() + ") must be explicitly configured (unlike the primitive type decoders) with a date 'format'. See Javadoc.");
        }
        try {
            // Must be sync'd - DateFormat is not synchronized.
            synchronized(decoder) {
                decoder.parse(data.trim());
                return decoder.getCalendar().clone();
            }
        } catch (ParseException e) {
            throw new DataDecodeException("Error decoding Date data value '" + data + "' with decode format '" + format + "'.", e);
        }
    }
}
