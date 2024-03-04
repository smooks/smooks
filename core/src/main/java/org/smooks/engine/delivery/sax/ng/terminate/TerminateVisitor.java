/*-
 * ========================LICENSE_START=================================
 * Core
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
package org.smooks.engine.delivery.sax.ng.terminate;

import org.smooks.api.ExecutionContext;
import org.smooks.api.delivery.ordering.Producer;
import org.smooks.api.resource.visitor.sax.ng.AfterVisitor;
import org.smooks.api.resource.visitor.sax.ng.BeforeVisitor;
import org.w3c.dom.Element;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class TerminateVisitor implements BeforeVisitor, AfterVisitor, Producer {

    private boolean terminateBefore;

    /**
     * @param terminateBefore the terminateBefore to set
     */
    @SuppressWarnings("WeakerAccess")
    @Inject
    public TerminateVisitor setTerminateBefore(Optional<Boolean> terminateBefore) {
        this.terminateBefore = terminateBefore.orElse(this.terminateBefore);
        return this;
    }

    /* (non-Javadoc)
     * @see org.smooks.engine.delivery.ordering.Producer#getProducts()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Set<?> getProducts() {
        // Doesn't actually produce anything.  Just using the Producer/Consumer mechanism to
        // force this vistor to the top of the visitor apply list.
        return Collections.EMPTY_SET;
    }

    @Override
    public void visitAfter(Element element, ExecutionContext executionContext) {
        if (!terminateBefore) {
            throw new TerminateException(element);
        }
    }

    @Override
    public void visitBefore(Element element, ExecutionContext executionContext) {
        if (terminateBefore) {
            throw new TerminateException(element);
        }
    }
}