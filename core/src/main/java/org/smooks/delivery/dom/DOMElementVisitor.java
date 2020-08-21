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
package org.smooks.delivery.dom;

/**
 * Element <b>Visitor</b> (GoF) interface for DOM.
 * <p/>
 * {@link SmooksDOMFilter} filters (analyses/transforms) XML/XHTML/HTML content
 * by "visting" the DOM {@link org.w3c.dom.Element} nodes through a series of iterations over
 * the source XML DOM.
 * <p/>
 * This interface defines the methods for a "visiting" filter.
 * Implementations of this interface provide a means of hooking analysis
 * and transformation logic into the {@link SmooksDOMFilter} filtering process.
 * <p/>
 * Implementations should be annotated with the {@link org.smooks.delivery.dom.Phase}
 * annotation, indicating in which of the {@link SmooksDOMFilter Visit Phases} the visitor should be applied. If not
 * annotated, the visitor is applied during the Processing phase.  The phase may also be specified via the
 * "VisitPhase" property on the {@link org.smooks.cdr.SmooksResourceConfiguration resource configuration}.  Valid values
 * in this case are "ASSEMBLY" and "PROCESSING".
 * <p/>
 * Implementations must be stateless.  If state storage is required, attach the state to the
 * supplied {@link org.smooks.container.ExecutionContext}.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface DOMElementVisitor extends DOMVisitBefore, DOMVisitAfter {
}
