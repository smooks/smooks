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

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.smooks.edisax.model.internal.DelimiterType;
import org.smooks.edisax.model.internal.Delimiters;
import org.smooks.edisax.util.EDIUtils;
import org.smooks.edi.EDIWritable;
import org.smooks.edi.unedifact.model.r41.types.DateTime;
import org.smooks.edi.unedifact.model.r41.types.Party;
import org.smooks.edi.unedifact.model.r41.types.Ref;
import org.smooks.edi.unedifact.model.r41.types.SyntaxIdentifier;

/**
 * Interchange Header (UNB) Control Segment Data (Version 4, Release 1).
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UNB41 implements Serializable, EDIWritable {

	private static final long serialVersionUID = 1L;
	
	private SyntaxIdentifier syntaxIdentifier;
	private Party sender;
	private Party recipient;
	private DateTime date;
	private String controlRef;
	private Ref recipientRef;
	private String applicationRef;
	private String processingPriorityCode;
	private String ackRequest;
	private String agreementId;
    private String testIndicator;

    public void write(Writer writer, Delimiters delimiters) throws IOException {
        Writer nodeWriter = new StringWriter();
        List<String> nodeTokens = new ArrayList<String>();

        nodeWriter.write("UNB");
        nodeWriter.write(delimiters.getField());
        if(syntaxIdentifier != null) {
            syntaxIdentifier.write(nodeWriter, delimiters);
            nodeTokens.add(nodeWriter.toString());
            ((StringWriter)nodeWriter).getBuffer().setLength(0);
        }
        nodeWriter.write(delimiters.getField());
        if(sender != null) {
            sender.write(nodeWriter, delimiters);
            nodeTokens.add(nodeWriter.toString());
            ((StringWriter)nodeWriter).getBuffer().setLength(0);
        }
        nodeWriter.write(delimiters.getField());
        if(recipient != null) {
            recipient.write(nodeWriter, delimiters);
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
        if(controlRef != null) {
            nodeWriter.write(delimiters.escape(controlRef));
            nodeTokens.add(nodeWriter.toString());
            ((StringWriter)nodeWriter).getBuffer().setLength(0);
        }
        nodeWriter.write(delimiters.getField());
        if(recipientRef != null) {
            recipientRef.write(nodeWriter, delimiters);
            nodeTokens.add(nodeWriter.toString());
            ((StringWriter)nodeWriter).getBuffer().setLength(0);
        }
        nodeWriter.write(delimiters.getField());
        if(applicationRef != null) {
            nodeWriter.write(delimiters.escape(applicationRef));
            nodeTokens.add(nodeWriter.toString());
            ((StringWriter)nodeWriter).getBuffer().setLength(0);
        }
        nodeWriter.write(delimiters.getField());
        if(processingPriorityCode != null) {
            nodeWriter.write(delimiters.escape(processingPriorityCode));
            nodeTokens.add(nodeWriter.toString());
            ((StringWriter)nodeWriter).getBuffer().setLength(0);
        }
        nodeWriter.write(delimiters.getField());
        if(ackRequest != null) {
            nodeWriter.write(delimiters.escape(ackRequest));
            nodeTokens.add(nodeWriter.toString());
            ((StringWriter)nodeWriter).getBuffer().setLength(0);
        }
        nodeWriter.write(delimiters.getField());
        if(agreementId != null) {
            nodeWriter.write(delimiters.escape(agreementId));
            nodeTokens.add(nodeWriter.toString());
            ((StringWriter)nodeWriter).getBuffer().setLength(0);
        }
        nodeWriter.write(delimiters.getField());
        if(testIndicator != null) {
            nodeWriter.write(delimiters.escape(testIndicator));
            nodeTokens.add(nodeWriter.toString());
            ((StringWriter)nodeWriter).getBuffer().setLength(0);
        }

        nodeTokens.add(nodeWriter.toString());

        writer.write(EDIUtils.concatAndTruncate(nodeTokens, DelimiterType.FIELD, delimiters));
        writer.write(delimiters.getSegment());
        writer.flush();
    }

    public SyntaxIdentifier getSyntaxIdentifier() {
		return syntaxIdentifier;
	}

    public void setSyntaxIdentifier(SyntaxIdentifier syntaxIdentifier) {
		this.syntaxIdentifier = syntaxIdentifier;
	}

    public Party getSender() {
		return sender;
	}

    public void setSender(Party sender) {
		this.sender = sender;
	}

    public Party getRecipient() {
		return recipient;
	}

    public void setRecipient(Party recipient) {
		this.recipient = recipient;
	}

    public DateTime getDate() {
		return date;
	}

    public void setDate(DateTime date) {
		this.date = date;
	}

    public String getControlRef() {
		return controlRef;
	}

    public void setControlRef(String controlRef) {
		this.controlRef = controlRef;
	}

    public Ref getRecipientRef() {
		return recipientRef;
	}

    public void setRecipientRef(Ref recipientRef) {
		this.recipientRef = recipientRef;
	}

    public String getApplicationRef() {
		return applicationRef;
	}

    public void setApplicationRef(String applicationRef) {
		this.applicationRef = applicationRef;
	}

    public String getProcessingPriorityCode() {
		return processingPriorityCode;
	}

    public void setProcessingPriorityCode(String processingPriorityCode) {
		this.processingPriorityCode = processingPriorityCode;
	}

    public String getAckRequest() {
		return ackRequest;
	}

    public void setAckRequest(String ackRequest) {
		this.ackRequest = ackRequest;
	}

    public String getAgreementId() {
		return agreementId;
	}

    public void setAgreementId(String agreementId) {
		this.agreementId = agreementId;
	}

    public String getTestIndicator() {
		return testIndicator;
	}

    public void setTestIndicator(String testIndicator) {
		this.testIndicator = testIndicator;
	}
}
