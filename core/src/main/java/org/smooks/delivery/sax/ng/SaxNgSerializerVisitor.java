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
package org.smooks.delivery.sax.ng;

import org.smooks.SmooksException;
import org.smooks.container.ExecutionContext;
import org.smooks.container.MementoCaretaker;
import org.smooks.delivery.DomToXmlWriter;
import org.smooks.delivery.Filter;
import org.smooks.delivery.SerializerVisitor;
import org.smooks.delivery.Visitor;
import org.smooks.delivery.dom.DOMElementVisitor;
import org.smooks.delivery.memento.AbstractVisitorMemento;
import org.smooks.delivery.memento.NodeVisitable;
import org.smooks.delivery.memento.Visitable;
import org.smooks.delivery.memento.VisitorMemento;
import org.smooks.delivery.sax.SAXElement;
import org.smooks.delivery.sax.SAXElementVisitor;
import org.smooks.delivery.sax.SAXText;
import org.smooks.io.DefaultFragmentWriter;
import org.smooks.io.NullWriter;
import org.w3c.dom.*;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Writer;
import java.util.Optional;
import java.util.function.Consumer;

public class SaxNgSerializerVisitor implements ElementVisitor, SAXElementVisitor, DOMElementVisitor, SerializerVisitor {
   
    protected DomToXmlWriter domToXmlWriter;
    private Boolean closeEmptyElements = true;
    private Boolean rewriteEntities = true;

    @Inject
    public void setCloseEmptyElements(@Named(Filter.CLOSE_EMPTY_ELEMENTS) Optional<Boolean> closeEmptyElements) {
        this.closeEmptyElements = closeEmptyElements.orElse(this.closeEmptyElements);
    }

    @Inject
    public void setRewriteEntities(@Named(Filter.ENTITIES_REWRITE) Optional<Boolean> rewriteEntities) {
        this.rewriteEntities = rewriteEntities.orElse(this.rewriteEntities);
    }

    @PostConstruct
    public void postConstruct() {
        domToXmlWriter = new DomToXmlWriter(closeEmptyElements, rewriteEntities);
    }

    @Override
    public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {

    }

    @Override
    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {

    }

    @Override
    public void onChildText(SAXElement element, SAXText childText, ExecutionContext executionContext) throws SmooksException, IOException {

    }

    @Override
    public void onChildElement(SAXElement element, SAXElement childElement, ExecutionContext executionContext) throws SmooksException, IOException {

    }

    @Override
    public void writeStartElement(Element element, Writer writer, ExecutionContext executionContext) throws IOException {
        domToXmlWriter.writeStartElement(element, writer);
    }

    @Override
    public void writeEndElement(Element element, Writer writer, ExecutionContext executionContext) throws IOException {
        domToXmlWriter.writeEndElement(element, writer);
    }

    @Override
    public void writeCharacterData(Node node, Writer writer, ExecutionContext executionContext) throws IOException {
        domToXmlWriter.writeCharacterData(node, writer);
    }

    @Override
    public void writeElementComment(Comment comment, Writer writer, ExecutionContext executionContext) throws IOException {
        domToXmlWriter.writeElementComment(comment, writer);
    }

    @Override
    public void writeElementEntityRef(EntityReference entityRef, Writer writer, ExecutionContext executionContext) throws IOException {
        domToXmlWriter.writeElementEntityRef(entityRef, writer);
    }

    @Override
    public void writeElementCDATA(CDATASection cdata, Writer writer, ExecutionContext executionContext) throws IOException {
        domToXmlWriter.writeElementCDATA(cdata, writer);
    }

    @Override
    public void writeElementNode(Node node, Writer writer, ExecutionContext executionContext) throws IOException {
        throw new IOException("writeElementNode not implemented yet. Node: " + node.getNodeValue() + ", node: [" + node + "]");
    }

    @Override
    public boolean writeChildElements() {
        return true;
    }

    protected static class StartElementMemento extends AbstractVisitorMemento {
        private Boolean isOpen;

        public StartElementMemento(Visitable visitable, Visitor visitor, Boolean isOpen) {
            super(visitable, visitor);
            this.isOpen = isOpen;
        }

        @Override
        public VisitorMemento copy() {
            return new StartElementMemento(visitable, visitor, isOpen);
        }

        @Override
        public void restore(VisitorMemento visitorMemento) {
            isOpen = ((StartElementMemento) visitorMemento).isOpen();
        }

        public Boolean isOpen() {
            return isOpen;
        }
    }

    public void writeStartElement(final Element element, final ExecutionContext executionContext) {
        if (!isStartWritten(element, executionContext.getMementoCaretaker())) {
            try {
                writeStartElement(element, new DefaultFragmentWriter(executionContext), executionContext);
            } catch (IOException e) {
                throw new SmooksException(e.getMessage(), e);
            }
            executionContext.getMementoCaretaker().save(new StartElementMemento(new NodeVisitable(element), this, true));
        }
    }

    public void writeEndElement(final Element element, final ExecutionContext executionContext) {
        final Writer writer = new DefaultFragmentWriter(executionContext);
        try {
            if (closeEmptyElements && !isStartWritten(element, executionContext.getMementoCaretaker())) {
                writer.write('<');
                writer.write(element.getTagName());
                domToXmlWriter.writeAttributes(element.getAttributes(), writer);
                writer.write(" />");
            } else {
                if (!isStartWritten(element, executionContext.getMementoCaretaker())) {
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
    public void visitChildText(final Element element, final ExecutionContext executionContext) throws SmooksException {
        onWrite(writer -> {
            try {
                if (!isStartWritten(element, executionContext.getMementoCaretaker())) {
                    writeStartElement(element, writer, executionContext);
                    executionContext.getMementoCaretaker().save(new StartElementMemento(new NodeVisitable(element), SaxNgSerializerVisitor.this, true));
                }
                domToXmlWriter.writeText(element, new DefaultFragmentWriter(executionContext));
                writer.flush();
            } catch (IOException e) {
                throw new SmooksException(e.getMessage(), e);
            }
        }, executionContext);
    }

    @Override
    public void visitChildElement(final Element childElement, final ExecutionContext executionContext) throws SmooksException {
        onWrite(writer -> {
            if (!isStartWritten((Element) childElement.getParentNode(), executionContext.getMementoCaretaker())) {
                try {
                    writeStartElement((Element) childElement.getParentNode(), writer, executionContext);
                    writer.flush();
                } catch (IOException e) {
                    throw new SmooksException(e.getMessage(), e);
                }
                executionContext.getMementoCaretaker().save(new StartElementMemento(new NodeVisitable(childElement.getParentNode()), SaxNgSerializerVisitor.this, true));
            }
        }, executionContext);
    }

    @Override
    public void visitAfter(final Element element, final ExecutionContext executionContext) throws SmooksException {
        onWrite(writer -> writeEndElement(element, executionContext), executionContext);
    }

    protected boolean isStartWritten(final Element element, final MementoCaretaker mementoCaretaker) {
        final StartElementMemento startElementMemento = new StartElementMemento(new NodeVisitable(element), this, false);
        mementoCaretaker.restore(startElementMemento);
        return startElementMemento.isOpen();
    }

    protected void onWrite(final Consumer<Writer> consumer, final ExecutionContext executionContext) {
        final DefaultFragmentWriter defaultFragmentWriter = new DefaultFragmentWriter(executionContext);
        if (executionContext.getDeliveryConfig() instanceof SaxNgContentDeliveryConfig && !(defaultFragmentWriter.getDelegateWriter() instanceof NullWriter)) {
            consumer.accept(defaultFragmentWriter);
        }
    }
}