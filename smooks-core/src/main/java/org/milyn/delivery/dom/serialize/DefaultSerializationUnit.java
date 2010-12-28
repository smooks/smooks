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

import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.Filter;
import org.milyn.xml.XmlUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.Writer;

/**
 * Default SerializationUnit implementation.
 * <p/>
 * Default SerialisationUnit where none defined.
 * @author tfennelly
 */
public class DefaultSerializationUnit implements SerializationUnit {

    private boolean closeEmptyElements = false;
    private boolean rewriteEntities;

    @ConfigParam(defaultVal = "false")
    public void setCloseEmptyElements(boolean closeEmptyElements) {
        this.closeEmptyElements = closeEmptyElements;
    }

    @ConfigParam(name = Filter.ENTITIES_REWRITE, defaultVal = "true")
    public void setRewriteEntities(boolean rewriteEntities) {
        this.rewriteEntities = rewriteEntities;
    }

    public boolean isRewriteEntities() {
        return rewriteEntities;
    }

    /* (non-Javadoc)
     * @see org.milyn.serialize.SerializationUnit#writeElementStart(org.w3c.dom.Element, java.io.Writer)
     */
    public void writeElementStart(Element element, Writer writer, ExecutionContext executionContext) throws IOException {
        writeElementStart(element, writer);
    }

    /* (non-Javadoc)
	 * @see org.milyn.serialize.SerializationUnit#writeElementStart(org.w3c.dom.Element, java.io.Writer)
	 */
	public void writeElementStart(Element element, Writer writer) throws IOException {
		writer.write((int)'<');
        writer.write(element.getTagName());
        writeAttributes(element.getAttributes(), writer);
        if(closeEmptyElements && !element.hasChildNodes()) {
            // Do nothing.  We'll close it "short-hand" in writeElementEnd below...
        } else {
            writer.write((int)'>');
        }
    }

	/**
	 * Write the element attributes.
	 * @param attributes The element attibutes.
	 * @param writer The writer to be written to.
	 * @throws IOException Exception writing output.
	 */
	protected void writeAttributes(NamedNodeMap attributes, Writer writer) throws IOException {
		int attribCount = attributes.getLength();
		
		for(int i = 0; i < attribCount; i++) {
			Attr attribute = (Attr)attributes.item(i);
			String attribValue = attribute.getValue();
			int enclosingChar = (int)'"';

            writer.write((int)' ');
            writer.write(attribute.getName());
            writer.write((int)'=');

            if(rewriteEntities) {
                writer.write('\"');
                XmlUtil.encodeAttributeValue(attribValue.toCharArray(), 0, attribValue.length(), writer);
                writer.write('\"');
            } else {
                if(attribValue.indexOf((int)'"') != -1) {
                    enclosingChar = (int)'\'';
                }
                writer.write(enclosingChar);
                writer.write(attribValue);
                writer.write(enclosingChar);
            }
        }
	}

    /* (non-Javadoc)
     * @see org.milyn.serialize.SerializationUnit#writeElementEnd(org.w3c.dom.Element, java.io.Writer)
     */
    public void writeElementEnd(Element element, Writer writer) throws IOException {
        writeElementEnd(element, writer, null);
    }

	/* (non-Javadoc)
	 * @see org.milyn.serialize.SerializationUnit#writeElementEnd(org.w3c.dom.Element, java.io.Writer)
	 */
	public void writeElementEnd(Element element, Writer writer, ExecutionContext executionContext) throws IOException {
        if(closeEmptyElements && !element.hasChildNodes()) {
            writer.write("/>");
        } else {
            writer.write("</");
            writer.write(element.getTagName());
            writer.write((int)'>');
        }
    }

	/* (non-Javadoc)
	 * @see org.milyn.serialize.SerializationUnit#writeElementText(org.w3c.dom.Text, java.io.Writer)
	 */
	public void writeElementText(Text text, Writer writer, ExecutionContext executionContext) throws IOException {
        if(rewriteEntities) {
            String textString = text.getData();
            XmlUtil.encodeTextValue(textString.toCharArray(), 0, textString.length(), writer);
        } else {
            writer.write(text.getData());
        }
    }

	/* (non-Javadoc)
	 * @see org.milyn.serialize.SerializationUnit#writeElementComment(org.w3c.dom.Comment, java.io.Writer)
	 */
	public void writeElementComment(Comment comment, Writer writer, ExecutionContext executionContext) throws IOException {
		writer.write("<!--");
		writer.write(comment.getData());
		writer.write("-->");
	}

	/* (non-Javadoc)
	 * @see org.milyn.serialize.SerializationUnit#writeElementEntityRef(org.w3c.dom.EntityReference, java.io.Writer)
	 */
	public void writeElementEntityRef(EntityReference entityRef, Writer writer, ExecutionContext executionContext) throws IOException {
		writer.write('&');
		writer.write(entityRef.getNodeName());
		writer.write(';');
	}

	/* (non-Javadoc)
	 * @see org.milyn.serialize.SerializationUnit#writeElementCDATA(org.w3c.dom.CDATASection, java.io.Writer)
	 */
	public void writeElementCDATA(CDATASection cdata, Writer writer, ExecutionContext executionContext) throws IOException {
		writer.write("<![CDATA[");
		writer.write(cdata.getData());
		writer.write("]]>");
	}

	/* (non-Javadoc)
	 * @see org.milyn.serialize.SerializationUnit#writeElementNode(org.w3c.dom.Node, java.io.Writer)
	 */
	public void writeElementNode(Node node, Writer writer, ExecutionContext executionContext) throws IOException {
		throw new IOException("writeElementNode not implemented yet. Node: " + node.getNodeValue() + ", node: [" + node + "]");
	}

	/* (non-Javadoc)
	 * @see org.milyn.serialize.SerializationUnit#writeChildElements()
	 */
	public boolean writeChildElements() {
		return true;
	}
}
