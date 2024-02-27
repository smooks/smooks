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
package org.smooks.engine.delivery.sax.ng.bridge;

import org.smooks.api.ExecutionContext;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.smooks.engine.delivery.sax.ng.CharDataFragmentExecutionEvent;
import org.smooks.api.delivery.event.ExecutionEvent;
import org.smooks.api.delivery.event.ExecutionEventListener;
import org.smooks.engine.delivery.event.FragmentExecutionEvent;
import org.smooks.engine.delivery.event.EndFragmentExecutionEvent;
import org.smooks.engine.delivery.event.StartFragmentExecutionEvent;
import org.w3c.dom.Node;

public abstract class BridgeAwareExecutionEventListener implements ExecutionEventListener {

    protected final ExecutionContext executionContext;

    public BridgeAwareExecutionEventListener(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }
    
    @Override
    public void onEvent(final ExecutionEvent executionEvent) {
        if (executionEvent instanceof FragmentExecutionEvent<?> && ((FragmentExecutionEvent<?>) executionEvent).getFragment() instanceof NodeFragment) {
            final Node node = (Node) ((FragmentExecutionEvent<?>) executionEvent).getFragment().unwrap();
            if (Bridge.isBridge(node)) {
                if (executionEvent instanceof StartFragmentExecutionEvent<?>) {
                    Bridge bridge = new Bridge(node);
                    if (bridge.getVisit().equals("visitBefore")) {
                        doOnEvent(new StartFragmentExecutionEvent<>(new NodeFragment(executionContext.get(bridge.getSourceKey()))));
                    } else if (bridge.getVisit().equals("visitChildText")) {
                        doOnEvent(new CharDataFragmentExecutionEvent(new NodeFragment(executionContext.get(bridge.getSourceKey()))));
                    } else if (bridge.getVisit().equals("visitAfter")) {
                        doOnEvent(new EndFragmentExecutionEvent(new NodeFragment(executionContext.get(bridge.getSourceKey()))));
                    }
                }
                return;
            }
        }

        doOnEvent(executionEvent);
    }
    
    public abstract void doOnEvent(ExecutionEvent executionEvent);
}