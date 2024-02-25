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
import org.smooks.api.TypedKey;
import org.smooks.api.resource.visitor.sax.ng.ElementVisitor;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.smooks.engine.delivery.interceptor.AbstractInterceptorVisitor;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class BridgeInterceptor extends AbstractInterceptorVisitor implements ElementVisitor {
    protected boolean doVisit(final Node node, final String currentVisit, final ExecutionContext executionContext) {
        if (((Element) node).getAttribute("visit").equals(currentVisit)) {
            Node sourceNode = executionContext.get(TypedKey.of(((Element) node).getAttribute("source")));
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
        if (Bridge.isBridge(element)) {
            if (doVisit(element, "visitBefore", executionContext)) {
                Object source = executionContext.get(TypedKey.of(element.getAttribute("source")));
                intercept(visitBeforeInvocation, source, executionContext);
            }
        } else {
            if (new NodeFragment(element).isMatch(getTarget().getResourceConfig().getSelectorPath(), executionContext)) {
                intercept(visitBeforeInvocation, element, executionContext);
            }
        }
    }

    @Override
    public void visitChildText(final CharacterData characterData, final ExecutionContext executionContext) {
        if (new NodeFragment(characterData.getParentNode()).isMatch(getTarget().getResourceConfig().getSelectorPath(), executionContext)) {
            intercept(visitChildTextInvocation, characterData, executionContext);
        }
    }

    @Override
    public void visitChildElement(Element childElement, ExecutionContext executionContext) {
        intercept(visitChildElementInvocation, childElement, executionContext);
    }

    @Override
    public void visitAfter(final Element element, final ExecutionContext executionContext) {
        if (Bridge.isBridge(element)) {
            if (doVisit(element, "visitChildText", executionContext) || doVisit(element, "visitAfter", executionContext)) {
                Object source = executionContext.get(TypedKey.of(element.getAttribute("source")));
                if (element.getAttribute("visit").equals("visitChildText")) {
                    visitChildText((CharacterData) source, executionContext);
                } else {
                    intercept(visitAfterInvocation, source, executionContext);
                }
            }
        } else {
            if (new NodeFragment(element).isMatch(getTarget().getResourceConfig().getSelectorPath(), executionContext)) {
                intercept(visitAfterInvocation, element, executionContext);
            }
        }
    }
}