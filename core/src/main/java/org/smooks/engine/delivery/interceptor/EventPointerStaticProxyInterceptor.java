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
package org.smooks.engine.delivery.interceptor;

import org.smooks.api.ExecutionContext;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.engine.delivery.event.VisitSequence;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.smooks.engine.delivery.sax.ng.pointer.EventPointer;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EventPointerStaticProxyInterceptor extends StaticProxyInterceptor {
    protected boolean doVisit(final EventPointer eventPointer, final VisitSequence currentVisit, final ExecutionContext executionContext) {
        if (eventPointer.getVisit().equals(currentVisit)) {
            final Node sourceNode = eventPointer.dereference(executionContext);
            if (sourceNode instanceof CharacterData) {
                return new NodeFragment(sourceNode.getParentNode()).isMatch(getTarget().getResourceConfig().getSelectorPath(), executionContext);
            } else {
                return new NodeFragment(sourceNode).isMatch(getTarget().getResourceConfig().getSelectorPath(), executionContext);
            }
        }
        return false;
    }

    @Override
    public void visitBefore(final Element element, final ExecutionContext executionContext) {
        if (EventPointer.isPointer(element)) {
            EventPointer eventPointer = new EventPointer(element);
            if (doVisit(eventPointer, VisitSequence.BEFORE, executionContext)) {
                super.visitBefore((Element) eventPointer.dereference(executionContext), executionContext);
            }
        } else {
            if (new NodeFragment(element).isMatch(getTarget().getResourceConfig().getSelectorPath(), executionContext)) {
                super.visitBefore(element, executionContext);
            }
        }
    }

    @Override
    public void visitChildText(final CharacterData characterData, final ExecutionContext executionContext) {
        if (new NodeFragment(characterData.getParentNode()).isMatch(getTarget().getResourceConfig().getSelectorPath(), executionContext)) {
            super.visitChildText(characterData, executionContext);
        }
    }

    @Override
    public void visitChildElement(final Element childElement, final ExecutionContext executionContext) {
        super.visitChildElement(childElement, executionContext);
    }


    @Override
    public void visitAfter(final Element element, final ExecutionContext executionContext) {
        if (EventPointer.isPointer(element)) {
            final EventPointer eventPointer = new EventPointer(element);
            if (doVisit(eventPointer, VisitSequence.CHILD_TEXT, executionContext) || doVisit(eventPointer, VisitSequence.AFTER, executionContext)) {
                Object source = eventPointer.dereference(executionContext);
                if (eventPointer.getVisit().equals(VisitSequence.CHILD_TEXT)) {
                    super.visitChildText((CharacterData) source, executionContext);
                } else {
                    super.visitAfter((Element) source, executionContext);
                }
            }
        } else {
            if (new NodeFragment(element).isMatch(getTarget().getResourceConfig().getSelectorPath(), executionContext)) {
                super.visitAfter(element, executionContext);
            }
        }
    }

    @Override
    public void onPostFragment(final Fragment<?> fragment, final ExecutionContext executionContext) {
        final Fragment<?> fragmentUnderTest;
        final Node node = (Node) fragment.unwrap();
        if (EventPointer.isPointer(node)) {
            final EventPointer eventPointer = new EventPointer(node);
            if (eventPointer.getVisit().equals(VisitSequence.AFTER)) {
                fragmentUnderTest = new NodeFragment(eventPointer.dereference(executionContext));
            } else {
                return;
            }
        } else {
            fragmentUnderTest = fragment;
        }
        if (fragmentUnderTest.isMatch(getTarget().getResourceConfig().getSelectorPath(), executionContext)) {
            super.onPostFragment(fragmentUnderTest, executionContext);
        }
    }
}