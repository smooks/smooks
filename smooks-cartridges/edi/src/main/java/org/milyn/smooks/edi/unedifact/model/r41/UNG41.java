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
package org.milyn.smooks.edi.unedifact.model.r41;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.milyn.edisax.model.internal.DelimiterType;
import org.milyn.edisax.model.internal.Delimiters;
import org.milyn.edisax.util.EDIUtils;
import org.milyn.smooks.edi.EDIWritable;
import org.milyn.smooks.edi.unedifact.model.r41.types.Application;
import org.milyn.smooks.edi.unedifact.model.r41.types.DateTime;
import org.milyn.smooks.edi.unedifact.model.r41.types.MessageVersion;

/**
 * Group Header (Version 4, Release 1).
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UNG41 implements Serializable, EDIWritable {

	private static final long serialVersionUID = 1L;

	private String groupId;
	private Application senderApp;
	private Application recipientApp;
	private DateTime date;
	private String groupRef;
	private String controllingAgencyCode;
	private MessageVersion messageVersion;
	private String applicationPassword;

    public void write(Writer writer, Delimiters delimiters) throws IOException {
        Writer nodeWriter = new StringWriter();
        List<String> nodeTokens = new ArrayList<String>();

        nodeWriter.write("UNG");
        nodeWriter.write(delimiters.getField());
        if(groupId != null) {
            nodeWriter.write(delimiters.escape(groupId));
            nodeTokens.add(nodeWriter.toString());
            ((StringWriter)nodeWriter).getBuffer().setLength(0);
        }
        nodeWriter.write(delimiters.getField());
        if(senderApp != null) {
            senderApp.write(nodeWriter, delimiters);
            nodeTokens.add(nodeWriter.toString());
            ((StringWriter)nodeWriter).getBuffer().setLength(0);
        }
        nodeWriter.write(delimiters.getField());
        if(recipientApp != null) {
            recipientApp.write(nodeWriter, delimiters);
            nodeTokens.add(nodeWriter.toString());
            ((StringWriter)nodeWriter).getBuffer().setLength(0);
        }
        nodeWriter.write(delimiters.getField());
        if(date != null) {
            date.write(nodeWriter, delimiters);
            nodeTokens.add(nodeWriter.toString());
            ((StringWriter)nodeWriter).getBuffer().setLength(0);
        }
        nodeWriter.write(delimiters.getField());
        if(groupRef != null) {
            nodeWriter.write(delimiters.escape(groupRef));
            nodeTokens.add(nodeWriter.toString());
            ((StringWriter)nodeWriter).getBuffer().setLength(0);
        }
        nodeWriter.write(delimiters.getField());
        if(controllingAgencyCode != null) {
            nodeWriter.write(delimiters.escape(controllingAgencyCode));
            nodeTokens.add(nodeWriter.toString());
            ((StringWriter)nodeWriter).getBuffer().setLength(0);
        }
        nodeWriter.write(delimiters.getField());
        if(messageVersion != null) {
            messageVersion.write(nodeWriter, delimiters);
            nodeTokens.add(nodeWriter.toString());
            ((StringWriter)nodeWriter).getBuffer().setLength(0);
        }
        nodeWriter.write(delimiters.getField());
        if(applicationPassword != null) {
            nodeWriter.write(delimiters.escape(applicationPassword));
            nodeTokens.add(nodeWriter.toString());
            ((StringWriter)nodeWriter).getBuffer().setLength(0);
        }

        nodeTokens.add(nodeWriter.toString());

        writer.write(EDIUtils.concatAndTruncate(nodeTokens, DelimiterType.FIELD, delimiters));
        writer.write(delimiters.getSegment());
    }

	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public Application getSenderApp() {
		return senderApp;
	}
	public void setSenderApp(Application senderApp) {
		this.senderApp = senderApp;
	}
	public Application getRecipientApp() {
		return recipientApp;
	}
	public void setRecipientApp(Application recipientApp) {
		this.recipientApp = recipientApp;
	}
	public DateTime getDate() {
		return date;
	}
	public void setDate(DateTime date) {
		this.date = date;
	}
	public String getGroupRef() {
		return groupRef;
	}
	public void setGroupRef(String groupRef) {
		this.groupRef = groupRef;
	}
	public String getControllingAgencyCode() {
		return controllingAgencyCode;
	}
	public void setControllingAgencyCode(String controllingAgencyCode) {
		this.controllingAgencyCode = controllingAgencyCode;
	}
	public MessageVersion getMessageVersion() {
		return messageVersion;
	}
	public void setMessageVersion(MessageVersion messageVersion) {
		this.messageVersion = messageVersion;
	}
	public String getApplicationPassword() {
		return applicationPassword;
	}
	public void setApplicationPassword(String applicationPassword) {
		this.applicationPassword = applicationPassword;
	}
}
