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

package org.milyn.edisax;

import org.xml.sax.SAXException;
import org.milyn.edisax.model.internal.Edimap;
import org.milyn.edisax.model.internal.MappingNode;

/**
 * EDI message parsing exception.
 * @author tfennelly
 */
public class EDIParseException extends SAXException {

	private static final long serialVersionUID = 1L;
    private MappingNode errorNode;
    private int segmentNumber;
    private String[] segmentline; 

    /**
	 * Public constructor.	 
	 * @param message Exception message.
	 */
	public EDIParseException(String message) {
		super(message);
	}

    /**
	 * Public constructor.	 
	 * @param message Exception message.
     * @param cause Exception cause
	 */
	public EDIParseException(String message, Exception cause) {
		super(message, cause);
	}

    /**
	 * Public constructor.
	 * @param mappingModel The mapping model for the message on which the exception was encoutered.
	 * @param message Exception message.
	 */
	public EDIParseException(Edimap mappingModel, String message) {
		super(getMessagePrefix(mappingModel) + "  " + message);
	}

	/**
	 * Public constructor.
	 * @param mappingModel The mapping model for the message on which the exception was encoutered.
	 * @param message Exception message.
	 * @param cause Exception cause.
	 */
	public EDIParseException(Edimap mappingModel, String message, Exception cause) {
		super(getMessagePrefix(mappingModel) + "  " + message, cause);
	}

    /**
	 * Public constructor.
	 * @param message Exception message.
     * @param cause Exception cause
     * @param mappingNode the Segment, Field or Component where error occured.
     * @param segmentNumber the segment number where the error occured.
     * @param segmentLine the segment line where the error occured.
	 */
	public EDIParseException(String message, Exception cause, MappingNode mappingNode, int segmentNumber, String[] segmentLine) {
		super(message, cause);
        this.errorNode = mappingNode;
        this.segmentNumber = segmentNumber;
        this.segmentline = segmentLine;
	}

    /**
	 * Public constructor.
	 * @param mappingModel The mapping model for the message on which the exception was encoutered.
	 * @param message Exception message.
     * @param mappingNode the Segment, Field or Component where error occured.
     * @param segmentNumber the segment number where the error occured.
     * @param segmentLine the segment line where the error occured.
	 */
	public EDIParseException(Edimap mappingModel, String message, MappingNode mappingNode, int segmentNumber, String[] segmentLine) {
		super(getMessagePrefix(mappingModel) + "  " + message);
        this.errorNode = mappingNode;
        this.segmentNumber = segmentNumber;
        this.segmentline = segmentLine;
	}

    /**
	 * Public constructor.
	 * @param mappingModel The mapping model for the message on which the exception was encoutered.
	 * @param message Exception message.
	 * @param cause Exception cause.
     * @param mappingNode the Segment, Field or Component where error occured.
     * @param segmentNumber the segment number where the error occured.
     * @param segmentLine the segment line where the error occured.
	 */
	public EDIParseException(Edimap mappingModel, String message, Exception cause, MappingNode mappingNode, int segmentNumber, String[] segmentLine) {
		super(getMessagePrefix(mappingModel) + "  " + message, cause);
        this.errorNode = mappingNode;
        this.segmentNumber = segmentNumber;
        this.segmentline = segmentLine;
	}

	private static String getMessagePrefix(Edimap mappingModel) {
		return "EDI message processing failed [" + mappingModel.getDescription().getName() + "][" + mappingModel.getDescription().getVersion() + "].";
	}

    public MappingNode getErrorNode() {
        return errorNode;
    }

    public int getSegmentNumber() {
        return segmentNumber;
    }

    public String[] getSegmentline() {
        return segmentline;
    }
}
