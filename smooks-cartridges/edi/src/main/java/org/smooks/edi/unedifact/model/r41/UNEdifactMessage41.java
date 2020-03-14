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

import org.smooks.edi.unedifact.model.UNEdifactMessage;

/**
 * UN/EDIFACT message (Version 4, Release 1).
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UNEdifactMessage41 implements UNEdifactMessage {

	private static final long serialVersionUID = 1L;

	private UNB41 interchangeHeader;
	private UNG41 groupHeader;
    private UNE41 groupTrailer;
	private UNH41 messageHeader;
	private UNT41 messageTrailer;
	private Object message;

	public UNB41 getInterchangeHeader() {
		return interchangeHeader;
	}
	public void setInterchangeHeader(UNB41 interchangeHeader) {
		this.interchangeHeader = interchangeHeader;
	}
	public UNG41 getGroupHeader() {
		return groupHeader;
	}
	public void setGroupHeader(UNG41 groupHeader) {
		this.groupHeader = groupHeader;
	}
    public UNE41 getGroupTrailer() {
        return groupTrailer;
    }
    public void setGroupTrailer(UNE41 groupTrailer) {
        this.groupTrailer = groupTrailer;
    }
    public UNH41 getMessageHeader() {
		return messageHeader;
	}
	public void setMessageHeader(UNH41 messageHeader) {
		this.messageHeader = messageHeader;
	}
	public UNT41 getMessageTrailer() {
		return messageTrailer;
	}
	public void setMessageTrailer(UNT41 messageTrailer) {
		this.messageTrailer = messageTrailer;
	}
	public Object getMessage() {
		return message;
	}
	public void setMessage(Object message) {
		this.message = message;
	}	
}
