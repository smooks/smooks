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

import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.resource.visitor.Visitor;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Manages {@link Memento}s on behalf of {@link Visitor}s. 
 */
public interface MementoCaretaker {

    /**
     * Stores a copy of a <code>Memento</code>. It is the client's responsibility to remove the saved
     * <code>Memento</code> once it is no longer needed either by calling {@link #forget(Fragment)} or
     * {@link #forget(Memento)}.
     * 
     * @param memento  the <code>Memento</code> to copy and store. After saving, mutations to this
     *                 <code>Memento</code> should not alter the saved copy.
     */
    void capture(Memento memento);

    /**
     * Mutates a <code>Memento</code> to match the state of a saved <code>Memento</code>. The
     * <code>Memento</code> parameter is restored from a saved <code>Memento</code> having an ID equal to
     * its ID as returned by {@link Memento#getAnchor()}. The <code>Memento</code> parameter remains unchanged
     * if no such saved <code>Memento</code> exists.
     * 
     * @param memento  the <code>Memento</code> to restore
     */
     void restore(Memento memento);

     boolean exists(Memento memento);

    /**
     * Removes the <code>Memento</code> having an ID equal to the <code>Memento</code> parameter's ID as
     * returned by {@link Memento#getAnchor()}.
     * 
     * @param memento  the <code>Memento</code> to be removed
     */
     void forget(Memento memento);

    /**
     * Removes all <code>Memento</code>s bound to the <code>Fragment</code> parameter.
     * 
     * @param fragment  the fragment 
     */
     void forget(Fragment<?> fragment);

    /**
     * Invokes a {@link Consumer} with a restored <code>Memento</code> and then saves the <code>Memento</code>.
     * This method offers a convenient way to aggregate and save data instead of writing:
     * <pre>
     * mementoCaretaker.restore(Memento);
     * // add data to memento
     * // ...
     * mementoCaretaker.save(memento);
     * </pre>
     * 
     * @param defaultMemento  the <code>Memento</code> to be restored
     * @param function        the function acting on the restored <code>Memento</code> and returning a new
     *                        <code>Memento replacing the earlier memento</code>
     * 
     * @see #restore(Memento)
     * @see #capture(Memento)
     */
    <T extends Memento> T stash(T defaultMemento, Function<T, T> function);
}