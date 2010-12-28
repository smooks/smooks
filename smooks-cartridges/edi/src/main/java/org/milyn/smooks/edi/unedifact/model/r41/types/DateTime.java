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
package org.milyn.smooks.edi.unedifact.model.r41.types;

import org.milyn.edisax.model.internal.DelimiterType;
import org.milyn.edisax.model.internal.Delimiters;
import org.milyn.edisax.util.EDIUtils;
import org.milyn.smooks.edi.EDIWritable;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * UN/EDIFACT Date and Time. 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DateTime implements Serializable, EDIWritable {

	private static final long serialVersionUID = 1L;

	private static final String DATE_TIME_FORMAT = "yyyyMMddHHmm";

	private String date = "00000000";
	private String time = "0000";
	private Date dateObj;

    public void write(Writer writer, Delimiters delimiters) throws IOException {
        Writer nodeWriter = new StringWriter();
        List<String> nodeTokens = new ArrayList<String>();

        if(date != null) {
            nodeWriter.write(delimiters.escape(date));
            nodeTokens.add(nodeWriter.toString());
            ((StringWriter)nodeWriter).getBuffer().setLength(0);
        }
        nodeWriter.write(delimiters.getComponent());
        if(time != null) {
            nodeWriter.write(delimiters.escape(time));
            nodeTokens.add(nodeWriter.toString());
            ((StringWriter)nodeWriter).getBuffer().setLength(0);
        }

        nodeTokens.add(nodeWriter.toString());
        writer.write(EDIUtils.concatAndTruncate(nodeTokens, DelimiterType.COMPONENT, delimiters));
    }

    public String getDate() {
		return date;
	}

    public void setDate(String date) {
		this.date = date;
	}

    public String getTime() {
		return time;
	}

    public void setTime(String time) {
		this.time = time;
	}

    public Date toDate() throws ParseException {
		if(dateObj == null) {
			dateObj = new SimpleDateFormat(DATE_TIME_FORMAT).parse(date + time);
		}
		return dateObj;
	}
}
