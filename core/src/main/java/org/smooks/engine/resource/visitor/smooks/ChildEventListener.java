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
package org.smooks.engine.resource.visitor.smooks;

import org.smooks.api.ExecutionContext;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.smooks.engine.memento.SimpleVisitorMemento;
import org.smooks.engine.memento.VisitorMemento;
import org.smooks.engine.delivery.sax.ng.CharDataFragmentExecutionEvent;
import org.smooks.api.delivery.event.ExecutionEvent;
import org.smooks.engine.delivery.event.FragmentExecutionEvent;
import org.smooks.engine.delivery.sax.ng.bridge.BridgeAwareExecutionEventListener;
import org.smooks.engine.delivery.event.EndFragmentExecutionEvent;
import org.smooks.engine.delivery.event.StartFragmentExecutionEvent;
import org.w3c.dom.Node;

import java.io.Writer;

class ChildEventListener extends BridgeAwareExecutionEventListener {
    private final NestedSmooksVisitor nestedSmooksVisitor;
    private final NodeFragment visitedFragment;
    private final Writer selectorWriter;
    private int currentNodeDepth;

    public ChildEventListener(final NestedSmooksVisitor nestedSmooksVisitor, final Writer selectorWriter, final NodeFragment visitedFragment, final ExecutionContext executionContext) {
        super(executionContext);
        this.nestedSmooksVisitor = nestedSmooksVisitor;
        this.selectorWriter = selectorWriter;
        this.visitedFragment = visitedFragment;
    }

    @Override
    public void doOnEvent(final ExecutionEvent executionEvent) {
        if (executionEvent instanceof FragmentExecutionEvent) {
            final Fragment<Node> childFragment = ((FragmentExecutionEvent<Node>) executionEvent).getFragment();
            final VisitorMemento<Node> sourceTreeMemento = new SimpleVisitorMemento<>(visitedFragment, nestedSmooksVisitor, visitedFragment.unwrap());
            executionContext.getMementoCaretaker().restore(sourceTreeMemento);

            if (executionEvent instanceof StartFragmentExecutionEvent) {
                if (!visitedFragment.equals(childFragment)) {
                    visitBefore(sourceTreeMemento, childFragment);
                }
            } else if (executionEvent instanceof CharDataFragmentExecutionEvent) {
                visitChildText(sourceTreeMemento, childFragment);
            } else if (executionEvent instanceof EndFragmentExecutionEvent) {
                if (!visitedFragment.equals(childFragment)) {
                    visitAfter(sourceTreeMemento);
                }
            }
        }
    }

    protected void visitBefore(final VisitorMemento<Node> sourceTreeMemento, final Fragment<Node> childFragment) {
        final Node childNode = sourceTreeMemento.getState().getOwnerDocument().importNode(childFragment.unwrap(), true);
        nestedSmooksVisitor.filterSource(visitedFragment, new NodeFragment(sourceTreeMemento.getState().appendChild(childNode)), selectorWriter, executionContext, "visitBefore");
        currentNodeDepth++;
        executionContext.getMementoCaretaker().capture(new SimpleVisitorMemento<>(visitedFragment, nestedSmooksVisitor, childNode));
    }

    protected void visitChildText(final VisitorMemento<Node> sourceTreeMemento, final Fragment<Node> childFragment) {
        final Node childNode = sourceTreeMemento.getState().getOwnerDocument().importNode(childFragment.unwrap(), true);
        nestedSmooksVisitor.filterSource(visitedFragment, new NodeFragment(sourceTreeMemento.getState().appendChild(childNode)), selectorWriter, executionContext, "visitChildText");
        if ((currentNodeDepth + 1) >= nestedSmooksVisitor.getMaxNodeDepth()) {
            sourceTreeMemento.getState().removeChild(childNode);
        }
    }

    protected void visitAfter(final VisitorMemento<Node> sourceTreeMemento) {
        nestedSmooksVisitor.filterSource(visitedFragment, new NodeFragment(sourceTreeMemento.getState()), selectorWriter, executionContext, "visitAfter");
        final Node parentNode = sourceTreeMemento.getState().getParentNode();
        if (currentNodeDepth >= nestedSmooksVisitor.getMaxNodeDepth()) {
            parentNode.removeChild(sourceTreeMemento.getState());
        }
        executionContext.getMementoCaretaker().capture(new SimpleVisitorMemento<>(visitedFragment, nestedSmooksVisitor, parentNode));
        currentNodeDepth--;
    }

}