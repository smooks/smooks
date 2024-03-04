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
import org.smooks.api.SmooksException;
import org.smooks.api.delivery.ContentHandlerBinding;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.delivery.sax.TextConsumer;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.api.resource.visitor.dom.DOMElementVisitor;
import org.smooks.api.resource.visitor.interceptor.InterceptorVisitor;
import org.smooks.api.resource.visitor.sax.ng.AfterVisitor;
import org.smooks.api.resource.visitor.sax.ng.ElementVisitor;
import org.smooks.api.resource.visitor.sax.ng.ParameterizedVisitor;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.smooks.engine.memento.TextAccumulatorMemento;
import org.smooks.engine.resource.config.xpath.IndexedSelectorPath;
import org.smooks.engine.resource.config.xpath.step.ElementSelectorStep;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import jakarta.annotation.PostConstruct;

public class TextConsumerInterceptor extends AbstractInterceptorVisitor implements ElementVisitor, DOMElementVisitor {

    protected boolean isTextConsumer;

    @PostConstruct
    public void postConstruct() {
        final ContentHandlerBinding<Visitor> visitorBinding = getTarget();
        if (!(visitorBinding.getContentHandler() instanceof ParameterizedVisitor) || (((ParameterizedVisitor) visitorBinding.getContentHandler()).getMaxNodeDepth() == 1)) {
            if (visitorBinding.getContentHandler().getClass().isAnnotationPresent(TextConsumer.class)) {
                isTextConsumer = true;
            } else if (visitorBinding.getContentHandler() instanceof AfterVisitor &&
                    visitorBinding.getResourceConfig().getSelectorPath() instanceof IndexedSelectorPath &&
                    ((IndexedSelectorPath) visitorBinding.getResourceConfig().getSelectorPath()).getTargetSelectorStep() instanceof ElementSelectorStep) {
                isTextConsumer = ((ElementSelectorStep) ((IndexedSelectorPath) visitorBinding.getResourceConfig().getSelectorPath()).getTargetSelectorStep()).accessesText();
            } else {
                isTextConsumer = false;
            }
        } else {
            isTextConsumer = false;
        }
    }

    @Override
    public void visitBefore(final Element element, final ExecutionContext executionContext) throws SmooksException {
        intercept(visitBeforeInvocation, element, executionContext);
    }

    @Override
    public void visitAfter(final Element element, final ExecutionContext executionContext) throws SmooksException {
        if (isTextConsumer) {
            final TextAccumulatorMemento textAccumulatorMemento = new TextAccumulatorMemento(new NodeFragment(element));
            executionContext.getMementoCaretaker().restore(textAccumulatorMemento);
            executionContext.getMementoCaretaker().capture(textAccumulatorMemento);
            element.setTextContent(textAccumulatorMemento.getText());
        }
        intercept(visitAfterInvocation, element, executionContext);
        if (isTextConsumer) {
            element.setTextContent("");
        }
    }

    @Override
    public void visitChildText(final CharacterData characterData, final ExecutionContext executionContext) throws SmooksException {
        if (isTextConsumer) {
            final Fragment<Node> parentNodeFragment = new NodeFragment(characterData.getParentNode());
            if (parentNodeFragment.reserve(Long.parseLong(parentNodeFragment.getId()), this)) {
                executionContext.getMementoCaretaker().stash(new TextAccumulatorMemento(parentNodeFragment), textAccumulatorMemento -> textAccumulatorMemento.accumulateText(characterData.getTextContent()));
            }
        }
        intercept(visitChildTextInvocation, characterData, executionContext);
    }

    @Override
    public void visitChildElement(Element childElement, ExecutionContext executionContext) {
        intercept(visitChildElementInvocation, childElement, executionContext);
    }
}
