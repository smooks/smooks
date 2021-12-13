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
package org.smooks.api;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a type-safe key for mapping to an object.
 *
 * @param <T> type of key
 * @see       org.smooks.api.TypedMap
 */
public final class TypedKey<T> {
    private final String name;
    private int hash;

    /**
     * Constructs a <code>TypedKey</code> with a random UUID for its name.
     */
    public TypedKey() {
        this(UUID.randomUUID().toString());
    }


    /**
     * Constructs a <code>TypedKey</code> with a custom name.
     *
     * @param name identifier to give to this <code>TypedKey</code>
     */
    public TypedKey(String name) {
        Objects.requireNonNull(name);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TypedKey)) {
            return false;
        }
        TypedKey<?> typedKey = (TypedKey<?>) o;
        return name.equals(typedKey.name);
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = Objects.hash(name);
        }
        return hash;
    }

    @Override
    public String toString() {
        return name;
    }
}
