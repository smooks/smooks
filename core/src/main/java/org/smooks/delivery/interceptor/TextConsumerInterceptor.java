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
package org.smooks.delivery.interceptor;

import org.smooks.SmooksException;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.ContentHandlerBinding;
import org.smooks.delivery.Visitor;
import org.smooks.delivery.fragment.NodeFragment;
import org.smooks.delivery.memento.TextAccumulatorMemento;
import org.smooks.delivery.sax.annotation.TextConsumer;
import org.smooks.delivery.sax.ng.*;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;

public class TextConsumerInterceptor extends AbstractInterceptorVisitor implements ElementVisitor, InterceptorVisitor {
    
    @Override
    public void visitBefore(final Element element, final ExecutionContext executionContext) throws SmooksException {
        intercept(new Invocation<BeforeVisitor>() {
            @Override
            public Object invoke(BeforeVisitor visitor) {
                visitor.visitBefore(element, executionContext);
                return null;
            }

            @Override
            public Class<BeforeVisitor> getTarget() {
                return BeforeVisitor.class;
            }
        });
    }

    @Override
    public void visitAfter(final Element element, final ExecutionContext executionContext) throws SmooksException {
        if (isTextConsumer(getTarget())) {
            TextAccumulatorMemento textAccumulatorMemento = new TextAccumulatorMemento(new NodeFragment(element), this);
            executionContext.getMementoCaretaker().restore(textAccumulatorMemento);
            element.setTextContent(textAccumulatorMemento.getText());
        }
        intercept(new Invocation<AfterVisitor>() {
            @Override
            public Object invoke(AfterVisitor visitor) {
                visitor.visitAfter(element, executionContext);
                return null;
            }

            @Override
            public Class<AfterVisitor> getTarget() {
                return AfterVisitor.class;
            }
        });
        if (isTextConsumer(getTarget())) {
            element.setTextContent("");
        }
    }

    @Override
    public void visitChildText(final CharacterData characterData, final ExecutionContext executionContext) throws SmooksException {
        if (isTextConsumer(getTarget())) {
            executionContext.getMementoCaretaker().stash(new TextAccumulatorMemento(new NodeFragment(characterData.getParentNode()), this), textAccumulatorMemento -> textAccumulatorMemento.accumulateText(characterData.getTextContent()));
        }
        intercept(new Invocation<ChildrenVisitor>() {
            @Override
            public Object invoke(ChildrenVisitor visitor) {
                visitor.visitChildText(characterData, executionContext);
                return null;
            }

            @Override
            public Class<ChildrenVisitor> getTarget() {
                return ChildrenVisitor.class;
            }
        });
    }

    @Override
    public void visitChildElement(Element childElement, ExecutionContext executionContext) {
        intercept(new Invocation<ChildrenVisitor>() {
            @Override
            public Object invoke(ChildrenVisitor visitor) {
                visitor.visitChildElement(childElement, executionContext);
                return null;
            }

            @Override
            public Class<ChildrenVisitor> getTarget() {
                return ChildrenVisitor.class;
            }
        });
    }

    protected boolean isTextConsumer(final ContentHandlerBinding<Visitor> visitorBinding) {
        if (!(visitorBinding.getContentHandler() instanceof ParameterizedVisitor) || (((ParameterizedVisitor) visitorBinding.getContentHandler()).getMaxNodeDepth() == 1)) {
            if (visitorBinding.getContentHandler().getClass().isAnnotationPresent(TextConsumer.class)) {
                return true;
            } else if (visitorBinding.getContentHandler() instanceof AfterVisitor) {
                return visitorBinding.getResourceConfig().getSelectorPath().getTargetSelectorStep().accessesText();
            }
        }
        
        return false;
    }
}