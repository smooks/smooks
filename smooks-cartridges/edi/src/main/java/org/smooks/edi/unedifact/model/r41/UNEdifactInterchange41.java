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

import org.smooks.assertion.AssertArgument;
import org.smooks.edisax.model.internal.Delimiters;
import org.smooks.edisax.unedifact.UNEdifactInterchangeParser;
import org.smooks.edi.EDIWritable;
import org.smooks.edi.unedifact.model.UNEdifactInterchange;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * UN/EDIFACT message interchange (Version 4, Release 1).
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UNEdifactInterchange41 implements UNEdifactInterchange {

	private static final long serialVersionUID = 1L;
	
    private Delimiters interchangeDelimiters;
	private UNB41 interchangeHeader;
	private UNZ41 interchangeTrailer;
	private List<UNEdifactMessage41> messages;

    /**
     * Get the interchange delimiters.
     * @return Interchange delimiters.
     */
    public Delimiters getInterchangeDelimiters() {
        return interchangeDelimiters;
    }

    /**
     * Set the interchange delimiters.
     * @param interchangeDelimiters Interchange delimiters.
     */
    public void setInterchangeDelimiters(Delimiters interchangeDelimiters) {
        this.interchangeDelimiters = interchangeDelimiters;
    }

    /**
	 * Get the interchange header object.
	 * @return The interchange header instance.
	 */
	public UNB41 getInterchangeHeader() {
		return interchangeHeader;
	}
	
	/**
	 * Set the interchange header object.
	 * @param interchangeHeader The interchange header instance.
	 */
	public void setInterchangeHeader(UNB41 interchangeHeader) {
		this.interchangeHeader = interchangeHeader;
	}
	
	/**
	 * Get the interchange trailer object.
	 * @return The interchange trailer instance.
	 */
	public UNZ41 getInterchangeTrailer() {
		return interchangeTrailer;
	}

	/**
	 * Set the interchange trailer object.
	 * @param interchangeTrailer The interchange trailer instance.
	 */
	public void setInterchangeTrailer(UNZ41 interchangeTrailer) {
		this.interchangeTrailer = interchangeTrailer;
	}
	
	/**
	 * Get the List of interchange messages.
	 * <p/>
	 * The list is ungrouped.  {@link UNG41 Interchange group} information is on each
	 * {@link UNEdifactMessage41} message instance, if the message is part
	 * of a group of messages.
	 * 
	 * @return The List of interchange messages.
	 */
	public List<UNEdifactMessage41> getMessages() {
		return messages;
	}
	
	/**
	 * Set the List of interchange messages.
	 * 
	 * @param messages The List of interchange messages.
	 */
	public void setMessages(List<UNEdifactMessage41> messages) {
		this.messages = messages;
	}

    /**
     * Write the interchange to the specified writer.
     * <p/>
     * Uses the default UN/EDIFACT delimiter set.
     * 
     * @param writer The target writer.
     * @throws IOException Error writing interchange.
     */
    public void write(Writer writer) throws IOException {
        write(writer, interchangeDelimiters);
    }

    /**
     * Write the interchange to the specified writer.
     * @param writer The target writer.
     * @param delimiters The delimiters.
     * @throws IOException Error writing interchange.
     */
    public void write(Writer writer, Delimiters delimiters) throws IOException {
        AssertArgument.isNotNull(writer, "writer");

        if(delimiters != null && delimiters != UNEdifactInterchangeParser.defaultUNEdifactDelimiters) {
            // Write a UNA segment definition...
            writer.append("UNA");
            writer.append(delimiters.getComponent());
            writer.append(delimiters.getField());
            writer.append(delimiters.getDecimalSeparator());
            writer.append(delimiters.getEscape());
            writer.append(" ");
            writer.append(delimiters.getSegment());
        } else {
            delimiters = UNEdifactInterchangeParser.defaultUNEdifactDelimiters;
        }

        if(interchangeHeader != null) {
            interchangeHeader.write(writer, delimiters);
        }

        UNEdifactMessage41 previousMessage = null;
        if(messages != null) {
            for(UNEdifactMessage41 message : messages) {

                Object messageObject = message.getMessage();
                if(messageObject == null) {
                    throw new IOException("Cannot write null EDI message object.");
                } else if(!(messageObject instanceof EDIWritable)) {
                    throw new IOException("Cannot write EDI message object type '" + messageObject.getClass().getName() + "'.  Type must implement '" + EDIWritable.class.getName() + "'.");
                }

                // Write group info...
                if(message.getGroupHeader() != null) {
                    if(previousMessage == null) {
                        // Start new group..
                        message.getGroupHeader().write(writer, delimiters);
                    } else if(message.getGroupHeader() != previousMessage.getGroupHeader()) {
                        if(previousMessage.getGroupHeader() != null) {
                            // Close out previous group...
                            previousMessage.getGroupTrailer().write(writer, delimiters);
                        }
                        // Start new group..
                        message.getGroupHeader().write(writer, delimiters);
                    } else {
                        // The message is part of the same group as the previous message...
                    }
                } else if(previousMessage != null && previousMessage.getGroupHeader() != null) {
                    // Close out previous group...
                    previousMessage.getGroupTrailer().write(writer, delimiters);
                }

                // Write the message...
                if(message.getMessageHeader() != null) {
                    message.getMessageHeader().write(writer, delimiters);
                }
                ((EDIWritable)messageObject).write(writer, delimiters);
                if(message.getMessageTrailer() != null) {
                    message.getMessageTrailer().write(writer, delimiters);
                }

                // Capture a ref to the message so its group info can be checked
                // against the next message, or closed if it's the last message...
                previousMessage = message;
            }
        }
        
        // Close out the group of the last message in the interchange (if it's in a group)...
        if(previousMessage != null && previousMessage.getGroupTrailer() != null) {
            // Close out previous group...
            previousMessage.getGroupTrailer().write(writer, delimiters);
        }

        if(interchangeTrailer != null) {
            interchangeTrailer.write(writer, delimiters);
        }

        writer.flush();
    }
}
