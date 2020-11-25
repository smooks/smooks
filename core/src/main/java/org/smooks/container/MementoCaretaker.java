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
package org.smooks.container;

import org.smooks.delivery.memento.Visitable;
import org.smooks.delivery.memento.VisitorMemento;

import java.util.function.Consumer;

/**
 * Manages {@link VisitorMemento}s on behalf of {@link org.smooks.delivery.Visitor}s. 
 */
public interface MementoCaretaker {

    /**
     * Stores a copy of a <code>VisitorMemento</code>. It is the client's responsibility to remove the saved 
     * <code>VisitorMemento</code> once it is no longer needed either by calling {@link #forget(Visitable)} or 
     * {@link #remove(VisitorMemento)}.
     * 
     * @param visitorMemento  the <code>VisitorMemento</code> to copy and store. After saving, mutations to this 
     *                        <code>VisitorMemento</code> should not alter the saved copy.
     */
    void save(VisitorMemento visitorMemento);

    /**
     * Mutates a <code>VisitorMemento</code> to match the state of a saved <code>VisitorMemento</code>. The 
     * <code>VisitorMemento</code> parameter is restored from a saved <code>VisitorMemento</code> having an ID equal to 
     * its ID as returned by {@link VisitorMemento#getId()}. The <code>VisitorMemento</code> parameter remains unchanged 
     * if no such saved <code>VisitorMemento</code> exists. 
     * 
     * @param visitorMemento  the <code>VisitorMemento</code> to restore
     */
    void restore(VisitorMemento visitorMemento);

    /**
     * Removes the <code>VisitorMemento</code> having an ID equal to the <code>VisitorMemento</code> parameter's ID as 
     * returned by {@link VisitorMemento#getId()}.
     * 
     * @param visitorMemento  the <code>VisitorMemento</code> to be removed
     */
    void remove(VisitorMemento visitorMemento);

    /**
     * Removes all <code>VisitorMemento</code>s bound to the <code>Visitable</code> parameter.
     * 
     * @param visitable  the visitable 
     */
    void forget(Visitable visitable);

    /**
     * Invokes a {@link Consumer} with a restored <code>VisitorMemento</code> and then saves the <code>VisitorMemento</code>. 
     * This method offers a convenient way to aggregate and save data instead of writing:
     * <pre>
     * mementoCaretaker.restore(visitorMemento);
     * // add data to visitorMemento
     * // ...
     * mementoCaretaker.save(visitorMemento);
     * </pre>
     * 
     * @param visitorMemento  the <code>VisitorMemento</code> to be restored
     * @param consumer        the consumer acting on the restored <code>VisitorMemento</code>
     * 
     * @see #restore(VisitorMemento)
     * @see #save(VisitorMemento)
     */
    <T extends VisitorMemento> void stash(final T visitorMemento, Consumer<T> consumer);
}