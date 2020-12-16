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
package org.smooks.container.standalone;

import org.smooks.container.MementoCaretaker;
import org.smooks.container.TypedKey;
import org.smooks.container.TypedMap;
import org.smooks.delivery.memento.Visitable;
import org.smooks.delivery.memento.VisitorMemento;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class DefaultMementoCaretaker implements MementoCaretaker {

    private final Map<Visitable, Set<String>> mementoIds = new HashMap<>();
    private final TypedMap typedMap;

    public DefaultMementoCaretaker(final TypedMap typedMap) {
        this.typedMap = typedMap;
    }
    
    @Override
    public void save(final VisitorMemento visitorMemento) {
        mementoIds.computeIfAbsent(visitorMemento.getVisitable(), o -> new HashSet<>()).add(visitorMemento.getId());
        typedMap.put(new TypedKey<>(visitorMemento.getId()), visitorMemento.copy());
    }

    @Override
    public <T extends VisitorMemento> void stash(final T visitorMemento, final Consumer<T> visitorMementoConsumer) {
        restore(visitorMemento);
        visitorMementoConsumer.accept(visitorMemento);
        save(visitorMemento);
    }
    
    @Override
    public void restore(final VisitorMemento visitorMemento) {
        final String visitorMementoId = visitorMemento.getId();
        final TypedKey<VisitorMemento> visitorMementoTypedKey = new TypedKey<>(visitorMementoId);
        final VisitorMemento restoredVisitorMemento = typedMap.get(visitorMementoTypedKey);
        if (restoredVisitorMemento != null) {
            visitorMemento.restore(restoredVisitorMemento);
        } else {
            typedMap.put(visitorMementoTypedKey, visitorMemento);
        }
    }
    
    @Override
    public void remove(final VisitorMemento visitorMemento) {
        typedMap.remove(new TypedKey<>(visitorMemento.getId()) );
        mementoIds.getOrDefault(visitorMemento.getVisitable(), new HashSet<>()).remove(visitorMemento.getId());
    }

    @Override
    public void forget(final Visitable visitable) {
        for (final String id : mementoIds.getOrDefault(visitable, new HashSet<>())) {
            typedMap.remove(new TypedKey<>(id));
        }
        mementoIds.remove(visitable);
    }
}
