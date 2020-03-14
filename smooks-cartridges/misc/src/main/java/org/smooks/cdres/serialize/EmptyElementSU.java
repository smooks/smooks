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

package org.smooks.cdres.serialize;

import java.io.IOException;
import java.io.Writer;

import org.smooks.cdr.annotation.ConfigParam;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.dom.serialize.DefaultSerializationUnit;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Empty element serialization unit.
 * <p/>
 * Writes empty elements well-formed (&lt;xxx/&gt;) or badly-formed (&lt;xxx&gt;).  If 
 * applied to an element, it also ensures that any child content is not 
 * writen to the requesting device.
 * <h3>.cdrl Configuration</h3>
 * <pre>
 * &lt;smooks-resource	useragent="<i>device/profile</i>" selector="<i>target-element-name</i>" 
 * 	path="org.smooks.cdres.serialize.EmptyElementSU"&gt;
 * 	&lt;!-- (Optional) Should the empty element be printed "well-formed". Default true. --&gt;
 * 	&lt;param name="<b>wellFormed</b>"&gt;<i>true/false</i>&lt;/param&gt;
 * &lt;/smooks-resource&gt;</pre>
 * See {@link org.smooks.cdr.SmooksResourceConfiguration}.
 * @author tfennelly
 */
public class EmptyElementSU extends DefaultSerializationUnit {

    @ConfigParam(use=ConfigParam.Use.OPTIONAL, defaultVal ="true")
    private boolean wellFormed;

	/* (non-Javadoc)
	 * @see org.smooks.serialize.SerializationUnit#writeElementStart(org.w3c.dom.Element, java.io.Writer, org.smooks.useraegnt.UAContext)
	 */
	public void writeElementStart(Element element, Writer writer, ExecutionContext executionContext) throws IOException {
		writer.write((int)'<');
		writer.write(element.getTagName());
		
		writeAttributes(element.getAttributes(), writer);
	}

	/* (non-Javadoc)
	 * @see org.smooks.serialize.SerializationUnit#writeElementEnd(org.w3c.dom.Element, java.io.Writer, org.smooks.useraegnt.UAContext)
	 */
	public void writeElementEnd(Element element, Writer writer, ExecutionContext executionContext) throws IOException {
		if(wellFormed) {
			writer.write("/>");
		} else {
			writer.write((int)'>');
		}
	}

	/* (non-Javadoc)
	 * @see org.smooks.serialize.SerializationUnit#writeElementText(org.w3c.dom.Text, java.io.Writer, org.smooks.useraegnt.UAContext)
	 */
	public void writeElementText(Text text, Writer writer, ExecutionContext executionContext) throws IOException {
	}

	/* (non-Javadoc)
	 * @see org.smooks.serialize.SerializationUnit#writeElementComment(org.w3c.dom.Comment, java.io.Writer, org.smooks.useraegnt.UAContext)
	 */
	public void writeElementComment(Comment comment, Writer writer, ExecutionContext executionContext) throws IOException {
	}

	/* (non-Javadoc)
	 * @see org.smooks.serialize.SerializationUnit#writeElementEntityRef(org.w3c.dom.EntityReference, java.io.Writer, org.smooks.useraegnt.UAContext)
	 */
	public void writeElementEntityRef(EntityReference entityRef, Writer writer, ExecutionContext executionContext) throws IOException {
	}

	/* (non-Javadoc)
	 * @see org.smooks.serialize.SerializationUnit#writeElementCDATA(org.w3c.dom.CDATASection, java.io.Writer, org.smooks.useraegnt.UAContext)
	 */
	public void writeElementCDATA(CDATASection cdata, Writer writer, ExecutionContext executionContext) throws IOException {
	}

	/* (non-Javadoc)
	 * @see org.smooks.serialize.SerializationUnit#writeElementNode(org.w3c.dom.Node, java.io.Writer, org.smooks.useraegnt.UAContext)
	 */
	public void writeElementNode(Node node, Writer writer, ExecutionContext executionContext) throws IOException {
	}

	/* (non-Javadoc)
	 * @see org.smooks.ContentHandler#getShortDescription()
	 */
	public String getShortDescription() {
		return "Write empty elements";
	}

	/* (non-Javadoc)
	 * @see org.smooks.ContentHandler#getDetailDescription()
	 */
	public String getDetailDescription() {
		return "Writes empty elements well-formed (<xxx/>) or badly-formed (<xxx>).  Ensures that any child content is not writen.";
	}
	
	/* (non-Javadoc)
	 * @see org.smooks.serialize.SerializationUnit#writeChildElements()
	 */
	public boolean writeChildElements() {
		return false;
	}
}