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

import org.smooks.api.TypedKey;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.memento.Memento;

public class TextAccumulatorMemento implements Memento {
    private static final TypedKey<String> ANCHOR_TYPED_KEY = new TypedKey<>();

    protected final Fragment<?> fragment;
    protected final StringBuilder stringBuilder = new StringBuilder();
    protected String anchor;

    public TextAccumulatorMemento(final Fragment<?> fragment) {
        this.fragment = fragment;
    }

    @Override
    public Memento copy() {
        final TextAccumulatorMemento textAccumulatorMemento = new TextAccumulatorMemento(fragment);
        textAccumulatorMemento.accumulateText(getText());

        return textAccumulatorMemento;
    }

    @Override
    public void restore(final Memento memento) {
        stringBuilder.append(((TextAccumulatorMemento) memento).getText());
    }

    @Override
    public Fragment<?> getFragment() {
        return fragment;
    }

    @Override
    public String getAnchor() {
        if (anchor == null) {
            anchor = ANCHOR_TYPED_KEY.getName() + "@" + fragment.getId();
        }
        return anchor;
    }

    public TextAccumulatorMemento accumulateText(final String text) {
        stringBuilder.append(text);
        return this;
    }

    public String getText() {
        return stringBuilder.toString();
    }
}
