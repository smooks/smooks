/*-
 * ========================LICENSE_START=================================
 * Core
 * %%
 * Copyright (C) 2020 - 2021 Smooks
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
package org.smooks.engine.memento;

import org.smooks.api.memento.Memento;
import org.smooks.api.TypedKey;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.api.delivery.fragment.Fragment;

public class VisitorMemento<T> extends AbstractVisitorMemento {

    protected TypedKey<?> typedKey;
    protected T state;
    
    public VisitorMemento(final Fragment<?> fragment, final Visitor visitor, final TypedKey<?> typedKey, final T state) {
        super(fragment, visitor);
        this.state = state;
        this.typedKey = typedKey;
    }
    
    @Override
    public Memento copy() {
        return new VisitorMemento<>(fragment, visitor, typedKey, state);
    }

    @Override
    public void restore(Memento memento) {
        state = ((VisitorMemento<T>) memento).getState();
        typedKey = ((VisitorMemento<T>) memento).getTypedKey();
    }

    public T getState() {
        return state;
    }

    public TypedKey<?> getTypedKey() {
        return typedKey;
    }

    @Override
    public String getAnchor() {
        if (anchor == null) {
            anchor = typedKey.getName() + "@" + fragment.getId() + "@" + visitor.getClass().getName() + "@" + getClass().getName() + "@" + System.identityHashCode(visitor);
        }
        return anchor;
    }
}