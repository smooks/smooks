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
package org.smooks.api.delivery.ordering;

import org.smooks.api.ExecutionContext;
import org.smooks.api.resource.visitor.Visitor;

/**
 * Object Consumer interface.
 * <p/>
 * A consumer is a {@link Visitor} that "consumes" a named object that has been added to the
 * {@link ExecutionContext} by a {@link Producer} of some sort.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 * @see Producer
 * @since 1.2
 */
public interface Consumer extends Visitor {

    /**
     * Does this consumer consume the specified named object.
     * <p/>
     * The named object would be a product of a {@link Producer} that is executing
     * on the same element.  The consumer should only return <code>false</code> if it knows for
     * certain that it doesn't consumer the specified named object.  If uncertain, it should
     * error on the side of saying that it does consume the object.
     *
     * @param object The product representation
     * @return True if the consumer consumes the specified product, otherwise false.
     */
    boolean consumes(Object object);
}
