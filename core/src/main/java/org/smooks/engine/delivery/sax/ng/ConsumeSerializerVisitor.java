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
package org.smooks.engine.delivery.sax.ng;

import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.api.delivery.Filter;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.memento.Memento;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.api.resource.visitor.dom.DOMElementVisitor;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.smooks.engine.memento.AbstractVisitorMemento;
import org.smooks.engine.memento.SimpleVisitorMemento;
import org.smooks.engine.memento.VisitorMemento;
import org.smooks.io.FragmentWriter;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Writer;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Consumes a node and serializes it to the execution sink stream.
 * <p>
 * Once this <code>ConsumeSerializerVisitor</code> consumes a node, no other <code>ConsumeSerializerVisitor</code>
 * instance, including this <code>ConsumeSerializerVisitor</code>, can re-serialize the node.
 */
public class ConsumeSerializerVisitor extends SimpleSerializerVisitor implements DOMElementVisitor {

    protected Boolean closeEmptyElements = true;

    protected static class ElementMemento extends AbstractVisitorMemento {
        private Boolean isOpen;

        public ElementMemento(final Fragment<?> fragment, final Visitor visitor, final Boolean isOpen) {
            super(fragment, visitor);
            this.isOpen = isOpen;
        }

        @Override
        public Memento copy() {
            return new ElementMemento(fragment, visitor, isOpen);
        }

        @Override
        public void restore(final Memento memento) {
            isOpen = ((ElementMemento) memento).isOpen();
        }

        public Boolean isOpen() {
            return isOpen;
        }
    }

    @Inject
    public void setCloseEmptyElements(@Named(Filter.CLOSE_EMPTY_ELEMENTS) Optional<Boolean> closeEmptyElements) {
        this.closeEmptyElements = closeEmptyElements.orElse(this.closeEmptyElements);
    }

    public void writeStartElement(final Element element, final ExecutionContext executionContext) {
        final Fragment<Node> nodeFragment = new NodeFragment(element);
        executionContext.getMementoCaretaker().stash(new ElementMemento(nodeFragment, this, false), elementMemento -> {
            if (!elementMemento.isOpen()) {
                try {
                    writeStartElement(element, new FragmentWriter(executionContext, nodeFragment), executionContext);
                } catch (IOException e) {
                    throw new SmooksException(e.getMessage(), e);
                }

                return new ElementMemento(nodeFragment, ConsumeSerializerVisitor.this, true);
            } else {
                return elementMemento;
            }
        });
    }

    protected void closeElement(final Element element, final Writer writer, final ExecutionContext executionContext) {
        final ElementMemento elementMemento = new ElementMemento(new NodeFragment(element), this, false);
        executionContext.getMementoCaretaker().restore(elementMemento);
        try {
            if (closeEmptyElements && !elementMemento.isOpen()) {
                writer.write('<');
                writer.write(element.getTagName());
                domSerializer.writeAttributes(element.getAttributes(), writer);
                writer.write("/>");
            } else {
                if (!elementMemento.isOpen()) {
                    writeStartElement(element, executionContext);
                }
                writer.write("</");
                writer.write(element.getTagName());
                writer.write('>');
            }

            writer.flush();
        } catch (IOException e) {
            throw new SmooksException(e.getMessage(), e);
        }
    }

    @Override
    public void visitBefore(final Element element, final ExecutionContext executionContext) throws SmooksException {

    }

    @Override
    public void visitChildText(final CharacterData characterData, final ExecutionContext executionContext) throws SmooksException {
        onWrite(nodeWriter -> {
            executionContext.getMementoCaretaker().stash(new ElementMemento(new NodeFragment(characterData.getParentNode()), this, false), elementMemento -> {
                if (!elementMemento.isOpen()) {
                    try {
                        writeStartElement((Element) characterData.getParentNode(), nodeWriter, executionContext);
                    } catch (IOException e) {
                        throw new SmooksException(e);
                    }

                    return new ElementMemento(new NodeFragment(characterData.getParentNode()), ConsumeSerializerVisitor.this, true);
                } else {
                    return elementMemento;
                }
            });
            try {
                final Writer charDataWriter = new FragmentWriter(executionContext, new NodeFragment(characterData));
                domSerializer.writeCharacterData(characterData, charDataWriter);
                charDataWriter.flush();
            } catch (IOException e) {
                throw new SmooksException(e.getMessage(), e);
            }
        }, executionContext, characterData.getParentNode());
    }

    @Override
    public void visitChildElement(final Element childElement, final ExecutionContext executionContext) throws SmooksException {
        final Element parentElement = (Element) childElement.getParentNode();
        onWrite(nodeWriter -> executionContext.getMementoCaretaker().stash(new ElementMemento(new NodeFragment(parentElement), this, false), elementMemento -> {
            if (!elementMemento.isOpen()) {
                try {
                    writeStartElement(parentElement, nodeWriter, executionContext);
                    nodeWriter.flush();
                } catch (IOException e) {
                    throw new SmooksException(e);
                }
                return new ElementMemento(new NodeFragment(parentElement), ConsumeSerializerVisitor.this, true);
            } else {
                return elementMemento;
            }
        }), executionContext, childElement.getParentNode());
    }

    @Override
    public void visitAfter(final Element element, final ExecutionContext executionContext) throws SmooksException {
        onWrite(nodeWriter -> closeElement(element, nodeWriter, executionContext), executionContext, element);
    }

    protected void onWrite(final Consumer<Writer> writerConsumer, final ExecutionContext executionContext, final Node node) {
        if (executionContext.getContentDeliveryRuntime().getContentDeliveryConfig() instanceof SaxNgContentDeliveryConfig) {
            final NodeFragment nodeFragment = new NodeFragment(node);
            final VisitorMemento<FragmentWriter> fragmentWriterMemento = executionContext.
                    getMementoCaretaker().
                    stash(new SimpleVisitorMemento<>(nodeFragment, this, new FragmentWriter(executionContext, nodeFragment)), restoredFragmentWriterMemento -> restoredFragmentWriterMemento);

            writerConsumer.accept(fragmentWriterMemento.getState());
        }
    }
}