/*-
 * ========================LICENSE_START=================================
 * Smooks API
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
import org.w3c.dom.Element;

/**
 * Adds knobs to the {@link SaxNgVisitor}.
 */
public interface ParameterizedVisitor extends BeforeVisitor, AfterVisitor {

    /**
     * Gets the maximum node depth this <code>ParameterizedVisitor<code/> can accept when visiting an <code>Element</code> 
     * in {@link AfterVisitor#visitAfter(Element, ExecutionContext)}. 
     * 
     * This method allows the targeted <code>Element</code> to emulate a DOM tree at the cost of performance. A 
     * <code>ParameterizedVisitor</code> with a maximum node depth of {@link Integer#MAX_VALUE} essentially means that 
     * the visited <code>Element</code> is a complete DOM tree.
     * 
     * @return the maximum node depth this <code>ParameterizedVisitor<code/> can traverse. Any value greater than 0 is valid.
     */
    int getMaxNodeDepth();
}
