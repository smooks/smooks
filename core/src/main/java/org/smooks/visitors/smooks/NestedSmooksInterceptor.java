/*-
 * ========================LICENSE_START=================================
 * Smooks Core
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
package org.smooks.visitors.smooks;

import org.smooks.container.ExecutionContext;
import org.smooks.container.TypedKey;
import org.smooks.delivery.fragment.NodeFragment;
import org.smooks.delivery.interceptor.AbstractInterceptorVisitor;
import org.smooks.delivery.sax.ng.AfterVisitor;
import org.smooks.delivery.sax.ng.BeforeVisitor;
import org.smooks.delivery.sax.ng.ChildrenVisitor;
import org.smooks.delivery.sax.ng.ElementVisitor;
import org.smooks.xml.DomUtils;
import org.w3c.dom.*;

public class NestedSmooksInterceptor extends AbstractInterceptorVisitor implements ElementVisitor {
    public static final TypedKey<NestedSmooksInterceptorCommand> COMMAND_TYPED_KEY = new TypedKey<>();
    
    protected boolean doVisit(final Class<? extends NestedSmooksInterceptorCommand.VisitPhase> currentVisitPhase, final Node visitedNode, final NestedSmooksInterceptorCommand nestedSmooksInterceptorCommand) {
        return (nestedSmooksInterceptorCommand.getRequiredVisitPhase() == null || currentVisitPhase.isInstance(nestedSmooksInterceptorCommand.getRequiredVisitPhase())) && DomUtils.getDepth(visitedNode) == nestedSmooksInterceptorCommand.getRequiredNodeDepth();
    }
    
    @Override
    public void visitBefore(final Element element, final ExecutionContext executionContext) {
        final NestedSmooksInterceptorCommand nestedSmooksInterceptorCommand = executionContext.getOrDefault(COMMAND_TYPED_KEY, new NestedSmooksInterceptorCommand(1, new NestedSmooksInterceptorCommand.VisitBeforePhase(), new NodeFragment(element)));
        
        if (doVisit(NestedSmooksInterceptorCommand.VisitBeforePhase.class, element, nestedSmooksInterceptorCommand)) {
            copyFragmentIds(nestedSmooksInterceptorCommand.getNodeFragment(), element);
            intercept(new Invocation<BeforeVisitor>() {
                @Override
                public Object invoke(final BeforeVisitor visitor) {
                    visitor.visitBefore(element, executionContext);
                    return null;
                }

                @Override
                public Class<BeforeVisitor> getTarget() {
                    return BeforeVisitor.class;
                }
            });
        }
    }

    @Override
    public void visitChildText(final CharacterData characterData, final ExecutionContext executionContext) {
        final NestedSmooksInterceptorCommand nestedSmooksInterceptorCommand = executionContext.getOrDefault(COMMAND_TYPED_KEY, new NestedSmooksInterceptorCommand(2, new NestedSmooksInterceptorCommand.VisitChildTextPhase(), new NodeFragment(characterData)));
        if (doVisit(NestedSmooksInterceptorCommand.VisitChildTextPhase.class, characterData, nestedSmooksInterceptorCommand)) {
            copyFragmentIds(nestedSmooksInterceptorCommand.getNodeFragment(), characterData);
            intercept(new Invocation<ChildrenVisitor>() {
                @Override
                public Object invoke(final ChildrenVisitor visitor) {
                    visitor.visitChildText(characterData, executionContext);
                    return null;
                }

                @Override
                public Class<ChildrenVisitor> getTarget() {
                    return ChildrenVisitor.class;
                }
            });
        }
    }

    @Override
    public void visitChildElement(Element childElement, ExecutionContext executionContext) {
        intercept(new Invocation<ChildrenVisitor>() {
            @Override
            public Object invoke(final ChildrenVisitor visitor) {
                visitor.visitChildElement(childElement, executionContext);
                return null;
            }

            @Override
            public Class<ChildrenVisitor> getTarget() {
                return ChildrenVisitor.class;
            }
        });
    }

    @Override
    public void visitAfter(final Element element, final ExecutionContext executionContext) {
        final NestedSmooksInterceptorCommand nestedSmooksInterceptorCommand = executionContext.getOrDefault(COMMAND_TYPED_KEY, new NestedSmooksInterceptorCommand(1, new NestedSmooksInterceptorCommand.VisitAfterPhase(), new NodeFragment(element)));
        if (doVisit(NestedSmooksInterceptorCommand.VisitAfterPhase.class, element, nestedSmooksInterceptorCommand)) {
            copyFragmentIds(nestedSmooksInterceptorCommand.getNodeFragment(), element);
            intercept(new Invocation<AfterVisitor>() {
                @Override
                public Object invoke(final AfterVisitor visitor) {
                    visitor.visitAfter(element, executionContext);
                    return null;
                }

                @Override
                public Class<AfterVisitor> getTarget() {
                    return AfterVisitor.class;
                }
            });
        }
    }

    protected void copyFragmentIds(final NodeFragment sourceNodeFragment, final Node destinationNode) {
        Node parentSourceNode = (Node) sourceNodeFragment.unwrap();
        Node parentDestinationNode = destinationNode;
        while (parentDestinationNode != null && !parentDestinationNode.equals(destinationNode.getOwnerDocument())) {
            parentDestinationNode.setUserData(NodeFragment.ID_USER_DATA_KEY, new NodeFragment(parentSourceNode).getId(), null);
            parentSourceNode = parentSourceNode.getParentNode();
            parentDestinationNode = parentDestinationNode.getParentNode();
        }
    }
}
