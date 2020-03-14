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
package org.smooks.edi.unedifact.model.r41;

import org.smooks.edisax.model.internal.DelimiterType;
import org.smooks.edisax.model.internal.Delimiters;
import org.smooks.edisax.util.EDIUtils;
import org.smooks.edi.EDIWritable;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Message Trailer (Version 4, Release 1).
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UNT41 implements Serializable, EDIWritable {

	private static final long serialVersionUID = 1L;

	private int segmentCount;
	private String messageRefNum;

    public void write(Writer writer, Delimiters delimiters) throws IOException {
        Writer nodeWriter = new StringWriter();
        List<String> nodeTokens = new ArrayList<String>();

        nodeWriter.write("UNT");
        nodeWriter.write(delimiters.getField());
        nodeWriter.write(Integer.toString(segmentCount));
        nodeTokens.add(nodeWriter.toString());
        ((StringWriter)nodeWriter).getBuffer().setLength(0);

        nodeWriter.write(delimiters.getField());
        if(messageRefNum != null) {
            nodeWriter.write(delimiters.escape(messageRefNum));
            nodeTokens.add(nodeWriter.toString());
            ((StringWriter)nodeWriter).getBuffer().setLength(0);
        }

        nodeTokens.add(nodeWriter.toString());

        writer.write(EDIUtils.concatAndTruncate(nodeTokens, DelimiterType.FIELD, delimiters));
        writer.write(delimiters.getSegment());
    }

    public int getSegmentCount() {
		return segmentCount;
	}

    public void setSegmentCount(int segmentCount) {
		this.segmentCount = segmentCount;
	}

    public String getMessageRefNum() {
		return messageRefNum;
	}

    public void setMessageRefNum(String messageRefNum) {
		this.messageRefNum = messageRefNum;
	}
}
