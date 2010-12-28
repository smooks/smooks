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

package org.milyn.delivery.dom.serialize;

import java.io.IOException;
import java.io.Writer;

import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.container.ExecutionContext;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * 
 * @author tfennelly
 */
public class EmptyElTestSerializationUnit extends DefaultSerializationUnit {

    @ConfigParam(name="wellformed", use=ConfigParam.Use.OPTIONAL, defaultVal ="true")
    private boolean wellFormed = true;

	/* (non-Javadoc)
	 * @see org.milyn.serialize.SerializationUnit#writeElementStart(org.w3c.dom.Element, java.io.Writer, org.milyn.device.UAContext)
	 */
	public void writeElementStart(Element element, Writer writer, ExecutionContext executionContext) throws IOException {
		writer.write((int)'<');
		writer.write(element.getTagName());
		
		writeAttributes(element.getAttributes(), writer);
	}

	/* (non-Javadoc)
	 * @see org.milyn.serialize.SerializationUnit#writeElementEnd(org.w3c.dom.Element, java.io.Writer, org.milyn.device.UAContext)
	 */
	public void writeElementEnd(Element element, Writer writer, ExecutionContext executionContext) throws IOException {
		if(wellFormed) {
			writer.write("/>");
		} else {
			writer.write((int)'>');
		}
	}

	/* (non-Javadoc)
	 * @see org.milyn.serialize.SerializationUnit#writeElementText(org.w3c.dom.Text, java.io.Writer, org.milyn.device.UAContext)
	 */
	public void writeElementText(Text text, Writer writer, ExecutionContext executionContext) throws IOException {
	}

	/* (non-Javadoc)
	 * @see org.milyn.serialize.SerializationUnit#writeElementComment(org.w3c.dom.Comment, java.io.Writer, org.milyn.device.UAContext)
	 */
	public void writeElementComment(Comment comment, Writer writer, ExecutionContext executionContext) throws IOException {
	}

	/* (non-Javadoc)
	 * @see org.milyn.serialize.SerializationUnit#writeElementEntityRef(org.w3c.dom.EntityReference, java.io.Writer, org.milyn.device.UAContext)
	 */
	public void writeElementEntityRef(EntityReference entityRef, Writer writer, ExecutionContext executionContext) throws IOException {
	}

	/* (non-Javadoc)
	 * @see org.milyn.serialize.SerializationUnit#writeElementCDATA(org.w3c.dom.CDATASection, java.io.Writer, org.milyn.device.UAContext)
	 */
	public void writeElementCDATA(CDATASection cdata, Writer writer, ExecutionContext executionContext) throws IOException {
	}

	/* (non-Javadoc)
	 * @see org.milyn.serialize.SerializationUnit#writeElementNode(org.w3c.dom.Node, java.io.Writer, org.milyn.device.UAContext)
	 */
	public void writeElementNode(Node node, Writer writer, ExecutionContext executionContext) throws IOException {
	}
	
	/* (non-Javadoc)
	 * @see org.milyn.serialize.SerializationUnit#writeChildElements()
	 */
	public boolean writeChildElements() {
		return false;
	}
}
