/*-
 * ========================LICENSE_START=================================
 * Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.io;

import org.smooks.support.XmlUtil;
import org.w3c.dom.*;

import java.io.IOException;
import java.io.Writer;

/**
 * Default SerializationUnit implementation.
 * <p/>
 * Default SerialisationUnit where none defined.
 * @author tfennelly
 */
public class DomToXmlWriter {

	private final Boolean closeEmptyElements;
	private final Boolean rewriteEntities;

	public DomToXmlWriter(Boolean closeEmptyElements, Boolean rewriteEntities) {
		this.closeEmptyElements = closeEmptyElements;
		this.rewriteEntities = rewriteEntities;
	}

	public boolean isRewriteEntities() {
		return rewriteEntities;
	}

	/* (non-Javadoc)
	 * @see org.smooks.serialize.SerializationUnit#writeElementStart(org.w3c.dom.Element, java.io.Writer)
	 */
	public void writeStartElement(Element element, Writer writer) throws IOException {
		writer.write('<');
		writer.write(element.getTagName());
		writeAttributes(element.getAttributes(), writer);
		if (closeEmptyElements && !element.hasChildNodes()) {
			// Do nothing.  We'll close it "short-hand" in writeElementEnd below...
		} else {
			writer.write('>');
		}
	}

	/**
	 * Write the element attributes.
	 *
	 * @param attributes The element attibutes.
	 * @param writer     The writer to be written to.
	 * @throws IOException Exception writing output.
	 */
	public void writeAttributes(NamedNodeMap attributes, Writer writer) throws IOException {
		int attribCount = attributes.getLength();

		for (int i = 0; i < attribCount; i++) {
			Attr attribute = (Attr) attributes.item(i);
			String attribValue = attribute.getValue();
			int enclosingChar = '"';

			writer.write(' ');
			writer.write(attribute.getName());
			writer.write('=');

			if (rewriteEntities) {
				writer.write('\"');
				XmlUtil.encodeAttributeValue(attribValue.toCharArray(), 0, attribValue.length(), writer);
				writer.write('\"');
			} else {
				if (attribValue.indexOf('"') != -1) {
					enclosingChar = '\'';
				}
				writer.write(enclosingChar);
				writer.write(attribValue);
				writer.write(enclosingChar);
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.smooks.serialize.SerializationUnit#writeElementEnd(org.w3c.dom.Element, java.io.Writer)
	 */
	public void writeEndElement(Element element, Writer writer) throws IOException {
		if (closeEmptyElements && !element.hasChildNodes()) {
			writer.write("/>");
		} else {
			writer.write("</");
			writer.write(element.getTagName());
			writer.write('>');
		}
	}
	
	public void writeCharacterData(final Node node, final Writer writer) throws IOException {
		switch (node.getNodeType()) {
			case Node.CDATA_SECTION_NODE: {
				writeElementCDATA((CDATASection) node, writer);
				break;
			}
			case Node.COMMENT_NODE: {
				writeElementComment((Comment) node, writer);
				break;
			}
			case Node.ENTITY_REFERENCE_NODE: {
				writeElementEntityRef((EntityReference) node, writer);
				break;
			}
			case Node.TEXT_NODE: {
				if (rewriteEntities) {
					String textString = ((Text) node).getData();
					XmlUtil.encodeTextValue(textString.toCharArray(), 0, textString.length(), writer);
				} else {
					writer.write(((Text) node).getData());
				}
				break;
			}
			default: {
				throw new IOException("writeElementNode not implemented yet. Node: " + node.getNodeValue() + ", node: [" + node + "]");
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.smooks.serialize.SerializationUnit#writeElementComment(org.w3c.dom.Comment, java.io.Writer)
	 */
	public void writeElementComment(Comment comment, Writer writer) throws IOException {
		writer.write("<!--");
		writer.write(comment.getData());
		writer.write("-->");
	}

	/* (non-Javadoc)
	 * @see org.smooks.serialize.SerializationUnit#writeElementEntityRef(org.w3c.dom.EntityReference, java.io.Writer)
	 */
	public void writeElementEntityRef(EntityReference entityRef, Writer writer) throws IOException {
		writer.write('&');
		writer.write(entityRef.getNodeName());
		writer.write(';');
	}

	/* (non-Javadoc)
	 * @see org.smooks.serialize.SerializationUnit#writeElementCDATA(org.w3c.dom.CDATASection, java.io.Writer)
	 */
	public void writeElementCDATA(CDATASection cdata, Writer writer) throws IOException {
		writer.write("<![CDATA[");
		writer.write(cdata.getData());
		writer.write("]]>");
	}

	public Boolean getCloseEmptyElements() {
		return closeEmptyElements;
	}

	public Boolean getRewriteEntities() {
		return rewriteEntities;
	}
}
