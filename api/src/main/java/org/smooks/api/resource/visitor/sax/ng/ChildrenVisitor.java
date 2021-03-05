/*-
 * ========================LICENSE_START=================================
 * API
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
package org.smooks.api.resource.visitor.sax.ng;

import org.smooks.api.ExecutionContext;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;


/**
 * Applies an operation on each {@link org.w3c.dom.Element} child.
 */
public interface ChildrenVisitor extends SaxNgVisitor {
    
    /**
     * Visits the character data of an <code>Element</code>. This method is invoked once for each chunk of character 
     * data. A shortcut for collecting character data is to annotate the <code>SaxNgVisitor</code> implementation with 
     * {@link org.smooks.engine.delivery.sax.annotation.StreamResultWriter}, or stash the character data in a 
     * {@link org.smooks.engine.memento.TextAccumulatorMemento} and restore the <code>TextAccumulatorMemento</code> in 
     * {@link AfterVisitor#visitAfter(Element, ExecutionContext)}.
     *
     * @param characterData              the <code>node</code> which includes character data but not any child 
     *                          <code>Element</code>s. The <code>Element</code>'s ancestors are traversable unless the 
     *                          global configuration parameter <code>maintain.element.stack</code> is set to false.
     * @param executionContext  the current <code>ExecutionContext</code>
     */
    void visitChildText(CharacterData characterData, ExecutionContext executionContext);

    /**
     * Visits a child <code>Element</code>. This method is invoked once for each child <code>Element</code>.
     *
     * @param childElement      the child <code>Element</code. The <code>Element</code>'s ancestors are traversable 
     *                          unless the global configuration parameter <code>maintain.element.stack</code> is set to 
     *                          false. The <code>Element</code>'s child nodes are not traversable.
     * @param executionContext  the current <code>ExecutionContext</code>
     */
    void visitChildElement(Element childElement, ExecutionContext executionContext);
}
