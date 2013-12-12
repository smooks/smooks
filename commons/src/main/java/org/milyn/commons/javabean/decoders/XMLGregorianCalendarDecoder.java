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

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * {@link javax.xml.datatype.XMLGregorianCalendar} data decoder.
 * <p/>
 * Decodes the supplied string into a {@link javax.xml.datatype.XMLGregorianCalendar} value based on the supplied "
 * {@link java.text.SimpleDateFormat format}" parameter, or the default (see below).
 * <p/>
 * The default date format used is "<i>yyyy-MM-dd'T'HH:mm:ss</i>" (see {@link java.text.SimpleDateFormat}). This format is based on the <a
 * href="http://www.w3.org/TR/2004/REC-xmlschema-2-20041028/#isoformats">ISO 8601</a> standard as used by the XML Schema type "<a
 * href="http://www.w3.org/TR/xmlschema-2/#dateTime">dateTime</a>".
 * <p/>
 * This decoder is synchronized on its underlying {@link java.text.SimpleDateFormat} instance.
 *
 * @author <a href="mailto:stefano.maestri@javalinux.it">stefano.maestri@javalinux.it</a>
 */
@DecodeType(XMLGregorianCalendar.class)
public class XMLGregorianCalendarDecoder extends DateDecoder {

    @Override
    public Object decode(String data) throws DataDecodeException {
        Date date = (Date) super.decode(data);

        try {
            GregorianCalendar gregCal = new GregorianCalendar();
            gregCal.setTime(date);
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregCal);
        } catch (DatatypeConfigurationException e) {
            throw new DataDecodeException("Error decoding XMLGregorianCalendar data value '" + data + "' with decode format '" + format + "'.", e);
        }
    }

    @Override
    public String encode(Object date) throws DataDecodeException {
        if(!(date instanceof XMLGregorianCalendar)) {
            throw new DataDecodeException("Cannot encode Object type '" + date.getClass().getName() + "'.  Must be type '" + XMLGregorianCalendar.class.getName() + "'.");
        }
        return super.encode(((XMLGregorianCalendar)date).toGregorianCalendar().getTime());
    }
}
