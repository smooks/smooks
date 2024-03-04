/*-
 * ========================LICENSE_START=================================
 * API
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
package org.smooks.api.delivery;

import org.smooks.api.SmooksException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import java.io.Closeable;

public interface Filter extends Closeable {
    /**
     * Stream filter type config parameter.
     */
    String STREAM_FILTER_TYPE = "stream.filter.type";

    String CLOSE_SOURCE = "close.source";

    String CLOSE_RESULT = "close.result";

    String ENTITIES_REWRITE = "entities.rewrite";

    String CLOSE_EMPTY_ELEMENTS = "close.empty.elements";

    String DEFAULT_SERIALIZATION_ON = "default.serialization.on";

    String MAINTAIN_ELEMENT_STACK = "maintain.element.stack";

    String MAX_NODE_DEPTH = "max.node.depth";

    String REVERSE_VISIT_ORDER_ON_VISIT_AFTER = "reverse.visit.order.on.visit.after";

    String TERMINATE_ON_VISITOR_EXCEPTION = "terminate.on.visitor.exception";

    String READER_POOL_SIZE = "reader.pool.size";

    /**
     * Filter the content in the supplied {@link javax.xml.transform.Source} instance, outputing the result
     * to the supplied {@link javax.xml.transform.Result} instance.
     * <p/>
     * Implementations use static methods on the {@link FilterSource} and {@link FilterResult} classes
     * to access the {@link Source} and {@link Result Results} objects.
     *
     * @throws SmooksException Failed to filter.
     */
    void doFilter() throws SmooksException;
}
