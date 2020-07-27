/*-
 * ========================LICENSE_START=================================
 * Smooks Core
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
package org.smooks.delivery.dom.serialize;

import org.smooks.container.ExecutionContext;
import org.w3c.dom.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Writer;

/**
 * 
 * @author tfennelly
 */
public class EmptyElTestSerializationUnit extends DefaultSerializationUnit {

    @Inject
	@Named("wellformed")
    private Boolean wellFormed = true;

	/* (non-Javadoc)
	 * @see org.smooks.serialize.SerializationUnit#writeElementStart(org.w3c.dom.Element, java.io.Writer, org.smooks.device.UAContext)
	 */
	public void writeElementStart(Element element, Writer writer, ExecutionContext executionContext) throws IOException {
		writer.write((int)'<');
		writer.write(element.getTagName());
		
		writeAttributes(element.getAttributes(), writer);
	}

	/* (non-Javadoc)
	 * @see org.smooks.serialize.SerializationUnit#writeElementEnd(org.w3c.dom.Element, java.io.Writer, org.smooks.device.UAContext)
	 */
	public void writeElementEnd(Element element, Writer writer, ExecutionContext executionContext) throws IOException {
		if(wellFormed) {
			writer.write("/>");
		} else {
			writer.write((int)'>');
		}
	}

	/* (non-Javadoc)
	 * @see org.smooks.serialize.SerializationUnit#writeElementText(org.w3c.dom.Text, java.io.Writer, org.smooks.device.UAContext)
	 */
	public void writeElementText(Text text, Writer writer, ExecutionContext executionContext) throws IOException {
	}

	/* (non-Javadoc)
	 * @see org.smooks.serialize.SerializationUnit#writeElementComment(org.w3c.dom.Comment, java.io.Writer, org.smooks.device.UAContext)
	 */
	public void writeElementComment(Comment comment, Writer writer, ExecutionContext executionContext) throws IOException {
	}

	/* (non-Javadoc)
	 * @see org.smooks.serialize.SerializationUnit#writeElementEntityRef(org.w3c.dom.EntityReference, java.io.Writer, org.smooks.device.UAContext)
	 */
	public void writeElementEntityRef(EntityReference entityRef, Writer writer, ExecutionContext executionContext) throws IOException {
	}

	/* (non-Javadoc)
	 * @see org.smooks.serialize.SerializationUnit#writeElementCDATA(org.w3c.dom.CDATASection, java.io.Writer, org.smooks.device.UAContext)
	 */
	public void writeElementCDATA(CDATASection cdata, Writer writer, ExecutionContext executionContext) throws IOException {
	}

	/* (non-Javadoc)
	 * @see org.smooks.serialize.SerializationUnit#writeElementNode(org.w3c.dom.Node, java.io.Writer, org.smooks.device.UAContext)
	 */
	public void writeElementNode(Node node, Writer writer, ExecutionContext executionContext) throws IOException {
	}
	
	/* (non-Javadoc)
	 * @see org.smooks.serialize.SerializationUnit#writeChildElements()
	 */
	public boolean writeChildElements() {
		return false;
	}
}
