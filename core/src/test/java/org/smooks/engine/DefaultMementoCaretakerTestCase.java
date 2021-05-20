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
package org.smooks.engine;

import org.junit.Test;
import org.smooks.api.TypedKey;
import org.smooks.api.TypedMap;
import org.smooks.api.memento.MementoCaretaker;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.smooks.engine.memento.DefaultMementoCaretaker;
import org.smooks.engine.memento.SimpleVisitorMemento;
import org.smooks.engine.memento.VisitorMemento;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DefaultMementoCaretakerTestCase {

    @Test
    public void testRestore() throws ParserConfigurationException {
        Map<Object, Object> typedMap = new HashMap<>();
        MementoCaretaker mementoCaretaker = new DefaultMementoCaretaker(new TypedMap() {
            @Override
            public <T> void put(TypedKey<T> key, T value) {
                typedMap.put(key, value);
            }

            @Override
            public <T> T get(TypedKey<T> key) {
                return (T) typedMap.get(key);
            }

            @Override
            public <T> T getOrDefault(TypedKey<T> key, T value) {
                return (T) typedMap.getOrDefault(key, value);
            }

            @Override
            public Map<TypedKey<Object>, Object> getAll() {
                return null;
            }

            @Override
            public <T> void remove(TypedKey<T> key) {
                typedMap.remove(key);
            }
        });
        VisitorMemento<String> visitorMemento = new SimpleVisitorMemento<>(new NodeFragment(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()), new Visitor() {
        }, "foo");
        mementoCaretaker.restore(visitorMemento);
        assertEquals("foo", visitorMemento.getState());
        assertEquals(0, typedMap.size());
    }
}
