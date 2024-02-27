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
package org.smooks.engine.delivery.interceptor;

import org.smooks.api.ExecutionContext;
import org.smooks.api.delivery.event.ExecutionEventListener;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.resource.visitor.sax.ng.AfterVisitor;
import org.smooks.api.resource.visitor.sax.ng.BeforeVisitor;
import org.smooks.api.resource.visitor.sax.ng.ChildrenVisitor;
import org.smooks.api.resource.visitor.sax.ng.ElementVisitor;
import org.smooks.engine.delivery.event.ResourceTargetingExecutionEvent;
import org.smooks.engine.delivery.event.VisitExecutionEvent;
import org.smooks.engine.delivery.event.VisitSequence;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EventInterceptor extends AbstractInterceptorVisitor implements ElementVisitor {

    @Override
    public void visitBefore(Element element, ExecutionContext executionContext) {
        if (getTarget().getContentHandler() instanceof BeforeVisitor) {
            for (ExecutionEventListener executionEventListener : executionContext.getContentDeliveryRuntime().getExecutionEventListeners()) {
                executionEventListener.onEvent(new ResourceTargetingExecutionEvent(new NodeFragment(element), getTarget().getResourceConfig(), VisitSequence.BEFORE));
            }
            intercept(visitBeforeInvocation, element, executionContext);
            onEvent(executionContext, new NodeFragment(element), VisitSequence.BEFORE);
        } else {
            intercept(visitBeforeInvocation, element, executionContext);
        }
    }

    @Override
    public void visitAfter(Element element, ExecutionContext executionContext) {
        intercept(visitAfterInvocation, element, executionContext);
        
        if (getTarget().getContentHandler() instanceof AfterVisitor) {
            onEvent(executionContext, new NodeFragment(element), VisitSequence.AFTER);
        }
    }
    
    @Override
    public void visitChildText(CharacterData characterData, ExecutionContext executionContext) {
        intercept(visitChildTextInvocation, characterData, executionContext);
        
        if (getTarget().getContentHandler() instanceof ChildrenVisitor) {
            onEvent(executionContext, new NodeFragment(characterData), VisitSequence.AFTER);
        }
    }

    @Override
    public void visitChildElement(Element childElement, ExecutionContext executionContext) {
        intercept(visitChildElementInvocation, childElement, executionContext);
        if (getTarget().getContentHandler() instanceof ChildrenVisitor) {
            onEvent(executionContext, new NodeFragment(childElement.getParentNode()), VisitSequence.AFTER);
        }
    }

    protected void onEvent(final ExecutionContext executionContext, final Fragment<Node> fragment, final VisitSequence visitSequence) {
        for (ExecutionEventListener executionEventListener : executionContext.getContentDeliveryRuntime().getExecutionEventListeners()) {
            executionEventListener.onEvent(new VisitExecutionEvent<>(fragment, getTarget(), visitSequence, executionContext));
        }
    }
}