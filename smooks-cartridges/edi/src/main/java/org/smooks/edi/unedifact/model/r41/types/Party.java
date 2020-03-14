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
package org.smooks.edi.unedifact.model.r41.types;

import org.smooks.edi.EDIWritable;
import org.smooks.edisax.model.internal.DelimiterType;
import org.smooks.edisax.model.internal.Delimiters;
import org.smooks.edisax.util.EDIUtils;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Interchange Party (sender or recipient).
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Party implements Serializable, EDIWritable {

	private static final long serialVersionUID = 1L;
	
	private String id;
	private String codeQualifier;
	private String internalId;
	private String internalSubId;

    public void write(Writer writer, Delimiters delimiters) throws IOException {
        Writer nodeWriter = new StringWriter();
        List<String> nodeTokens = new ArrayList<String>();

        if(id != null) {
            nodeWriter.write(delimiters.escape(id));
            nodeTokens.add(nodeWriter.toString());
            ((StringWriter)nodeWriter).getBuffer().setLength(0);
        }
        nodeWriter.write(delimiters.getComponent());
        if(codeQualifier != null) {
            nodeWriter.write(delimiters.escape(codeQualifier));
            nodeTokens.add(nodeWriter.toString());
            ((StringWriter)nodeWriter).getBuffer().setLength(0);
        }
        nodeWriter.write(delimiters.getComponent());
        if(internalId != null) {
            nodeWriter.write(delimiters.escape(internalId));
            nodeTokens.add(nodeWriter.toString());
            ((StringWriter)nodeWriter).getBuffer().setLength(0);
        }
        nodeWriter.write(delimiters.getComponent());
        if(internalSubId != null) {
            nodeWriter.write(delimiters.escape(internalSubId));
            nodeTokens.add(nodeWriter.toString());
            ((StringWriter)nodeWriter).getBuffer().setLength(0);
        }

        nodeTokens.add(nodeWriter.toString());
        writer.write(EDIUtils.concatAndTruncate(nodeTokens, DelimiterType.COMPONENT, delimiters));
    }

    public String getId() {
		return id;
	}

    public void setId(String id) {
		this.id = id;
	}

    public String getCodeQualifier() {
		return codeQualifier;
	}

    public void setCodeQualifier(String codeQualifier) {
		this.codeQualifier = codeQualifier;
	}

    public String getInternalId() {
		return internalId;
	}

    public void setInternalId(String internalId) {
		this.internalId = internalId;
	}

    public String getInternalSubId() {
		return internalSubId;
	}

    public void setInternalSubId(String internalSubId) {
		this.internalSubId = internalSubId;
	}
}
