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
import org.smooks.delivery.Visitor;
import org.w3c.dom.*;

import java.io.IOException;
import java.io.Writer;

/**
 * W3C Node Serailization interface definition.
 * <p/>
 * Implementations of this interface are applied to the content during the
 * <a href="../SmooksDOMFilter.html#phases">Serialisation phase</a>.
 * <p/>
 * This interface allows element targeted (by device, profile, DTD info etc)
 * serialization code to be implemented.  It gives control over how an
 * element is "printed" to the target device.
 * <p/>
 * Serialization Units are defined in the .cdrl file (in .cdrar file(s)) in the very
 * same way as any other content delivery resource (Assembly Units, Processing Units, 
 * DTDs etc).
 * <p/>
 * Implementations must be stateless in nature.
 * <p/>
 * Only one Serialisation Unit is applied to each element.  If more than one 
 * Serialization Unit is applicable to a given element, the most specific Serialization 
 * Unit is choosen.  See {@link org.smooks.cdr.SmooksResourceConfigurationSortComparator}.
 * <p/>
 * See <a href="../package-summary.html">Delivery Overview</a>.
 * @author tfennelly
 */
public interface SerializationUnit extends Visitor {

	/**
	 * Write the element start portion; the element name and it's
	 * attributes.
	 * <p/>
	 * EG: &lt;a href="http://www.x.com"&gt;
	 * @param element The element start to write.
	 * @param writer The writer to be written to.
	 * @param executionContext ExecutionContext instance for the delivery context.
	 * @throws IOException Exception writing output.
	 */
	public abstract void writeElementStart(Element element, Writer writer, ExecutionContext executionContext) throws IOException;

	/**
	 * Write the element end portion; close the element.
	 * <p/>
	 * EG: &lt;/a&gt;
	 * @param element The element end to write.
	 * @param writer The writer to be written to.
	 * @param executionContext ExecutionContext instance for the delivery context.
	 * @throws IOException Exception writing output.
	 */
	public abstract void writeElementEnd(Element element, Writer writer, ExecutionContext executionContext) throws IOException;

	/**
	 * Write element text.
	 * @param text The Text object to write.
	 * @param writer The writer to be written to.
	 * @param executionContext ExecutionContext instance for the delivery context.
	 * @throws IOException Exception writing output.
	 */
	public abstract void writeElementText(Text text, Writer writer, ExecutionContext executionContext) throws IOException;
	
	/**
	 * Write element comment.
	 * @param comment The comment o write.
	 * @param writer The writer to be written to.
	 * @param executionContext ExecutionContext instance for the delivery context.
	 * @throws IOException Exception writing output.
	 */
	public abstract void writeElementComment(Comment comment, Writer writer, ExecutionContext executionContext) throws IOException;

	/**
	 * Write element entity reference object.
	 * @param entityRef The entity reference to write.
	 * @param writer The writer to be written to.
	 * @param executionContext ExecutionContext instance for the delivery context.
	 * @throws IOException Exception writing output.
	 */
	public abstract void writeElementEntityRef(EntityReference entityRef, Writer writer, ExecutionContext executionContext) throws IOException;
	
	/**
	 * Write element CDATA section.
	 * @param cdata The CDATA section to write.
	 * @param writer The writer to be written to.
	 * @param executionContext ExecutionContext instance for the delivery context.
	 * @throws IOException Exception writing output.
	 */
	public abstract void writeElementCDATA(CDATASection cdata, Writer writer, ExecutionContext executionContext) throws IOException;

	/**
	 * Write element Node object.
	 * <p/>
	 * Called to write DOM types not covered by the other methods on 
	 * this interface.
	 * @param node The node to write.
	 * @param writer The writer to be written to.
	 * @param executionContext ExecutionContext instance for the delivery context.
	 * @throws IOException Exception writing output.
	 */
	public abstract void writeElementNode(Node node, Writer writer, ExecutionContext executionContext) throws IOException;
	
	/**
	 * Write the child elements of the element this SerializationUnit is being applied to.
	 * @return True if the child elements are to be writen, otherwise false.
	 */
	public abstract boolean writeChildElements();
}
