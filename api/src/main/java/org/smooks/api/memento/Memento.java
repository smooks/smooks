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
package org.smooks.api.memento;

import org.smooks.api.resource.visitor.Visitor;
import org.smooks.api.delivery.fragment.Fragment;

/**
 * Holds the state of a {@link Visitor}.
 * <p>
 * A <code>Memento</code> is saved and restored it at a later stage. A <code>Memento</code> is bound to a
 * <code>Visitor<code></> and its fragment (e.g., {@link Fragment}). Management of <code>Memento</code>s
 * should be delegated to {@link MementoCaretaker}.
 */
public interface Memento {

    /**
     * Performs a deep clone of this <code>Memento</code>.
     *
     * @return a deep clone of this <code>Memento</code>
     */
    Memento copy();

    /**
     * Combines a <code>Memento</code> state with this <code>Memento</code>
     *
     * @param memento the <code>Memento</code> restoring this <code>Memento</code>
     */
    void restore(Memento memento);

    /**
     * @return the fragment which this <code>Memento</code> is bound to
     */
    Fragment<?> getFragment();

    /**
     * Gets the anchor value of this <code>Memento</code>. <code>Memento</code>s with equal anchor values
     * are considered to be capturing the state of the same object but at different points in time.
     *
     * @return the ID of this <code>Memento</code>
     */
    String getAnchor();

}