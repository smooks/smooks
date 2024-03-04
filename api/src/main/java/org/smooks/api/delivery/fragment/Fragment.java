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
package org.smooks.api.delivery.fragment;

import org.smooks.api.resource.config.xpath.SelectorPath;
import org.smooks.api.ExecutionContext;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Wrapper for a fragment object.
 */
@NotThreadSafe
public interface Fragment<T> {

    /**
     * @return an identifier unique across fragments
     */
    String getId();

    /**
     * @return the wrapped fragment
     */
    T unwrap();

    boolean reserve(long id, Object token);

    boolean release(long id, Object token);

    /**
     * Is the supplied <code>SelectorPath</code> targeting this <code>Fragment</code>.
     * <p/>
     * Checks that this fragment is in the correct namespace and is a contextual
     * match for the configuration.
     *
     * @param selectorPath     The selector path to be checked.
     * @param executionContext The current execution context.
     * @return True if this configuration is targeted at the supplied element, otherwise false.
     */
    boolean isMatch(SelectorPath selectorPath, ExecutionContext executionContext);
}