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
package org.smooks.engine.memento;

import org.smooks.api.memento.MementoCaretaker;
import org.smooks.api.TypedKey;
import org.smooks.api.TypedMap;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.memento.Memento;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class DefaultMementoCaretaker implements MementoCaretaker {

    private final Map<Fragment<?>, Set<String>> mementoIds = new HashMap<>();
    private final TypedMap typedMap;

    public DefaultMementoCaretaker(final TypedMap typedMap) {
        this.typedMap = typedMap;
    }

    @Override
    public void capture(Memento visitorMemento) {
        mementoIds.computeIfAbsent(visitorMemento.getFragment(), o -> new HashSet<>()).add(visitorMemento.getAnchor());
        typedMap.put(new TypedKey<>(visitorMemento.getAnchor()), visitorMemento.copy());
    }

    @Override
    public void restore(Memento visitorMemento) {
        final String visitorMementoId = visitorMemento.getAnchor();
        final TypedKey<Memento> visitorMementoTypedKey = new TypedKey<>(visitorMementoId);
        final Memento restoredVisitorMemento = typedMap.get(visitorMementoTypedKey);
        if (restoredVisitorMemento != null) {
            visitorMemento.restore(restoredVisitorMemento);
        } else {
            typedMap.put(visitorMementoTypedKey, visitorMemento);
        }
    }

    @Override
    public boolean exists(Memento visitorMemento) {
        return typedMap.get(new TypedKey<>(visitorMemento.getAnchor())) != null;
    }

    @Override
    public void forget(Memento visitorMemento) {
        typedMap.remove(new TypedKey<>(visitorMemento.getAnchor()) );
        mementoIds.getOrDefault(visitorMemento.getFragment(), new HashSet<>()).remove(visitorMemento.getAnchor());
    }

    @Override
    public void forget(Fragment<?> fragment) {
        for (final String id : mementoIds.getOrDefault(fragment, new HashSet<>())) {
            typedMap.remove(new TypedKey<>(id));
        }
        mementoIds.remove(fragment);
    }

    @Override
    public <T extends Memento> T stash(T defaultVisitorMemento, Function<T, T> function) {
        restore(defaultVisitorMemento);
        T newVisitorMemento = function.apply(defaultVisitorMemento);
        capture(newVisitorMemento);

        return newVisitorMemento;
    }
}