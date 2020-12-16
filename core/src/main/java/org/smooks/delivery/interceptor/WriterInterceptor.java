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

import org.smooks.container.ExecutionContext;
import org.smooks.delivery.SerializerVisitor;
import org.smooks.delivery.Visitor;
import org.smooks.delivery.dom.DOMElementVisitor;
import org.smooks.delivery.memento.AbstractVisitorMemento;
import org.smooks.delivery.memento.NodeVisitable;
import org.smooks.delivery.memento.Visitable;
import org.smooks.delivery.memento.VisitorMemento;
import org.smooks.delivery.sax.SAXWriterAccessException;
import org.smooks.delivery.sax.annotation.StreamResultWriter;
import org.smooks.delivery.sax.ng.AfterVisitor;
import org.smooks.delivery.sax.ng.BeforeVisitor;
import org.smooks.delivery.sax.ng.ChildrenVisitor;
import org.smooks.delivery.sax.ng.ElementVisitor;
import org.smooks.io.FragmentWriter;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.Writer;

public class WriterInterceptor extends AbstractInterceptorVisitor implements ElementVisitor, DOMElementVisitor, InterceptorVisitor {

    public static class ExclusiveWriter extends Writer {
        private Visitor currentVisitor;
        private Visitor ownerVisitor;

        private Writer writer;
        private final Element element;

        public ExclusiveWriter(Writer writer, Element element) {
            this.writer = writer;
            this.element = element;
        }
        
        public void setCurrentVisitor(Visitor currentVisitor) {
            this.currentVisitor = currentVisitor;
            if (ownerVisitor == null && currentVisitor.getClass().isAnnotationPresent(StreamResultWriter.class)) {
                ownerVisitor = currentVisitor;
            }
        }

        public void setWriter(Writer writer) {
            this.writer = writer;
        }

        public void acquire() {
            if (ownerVisitor == null) {
                ownerVisitor = currentVisitor;
            }   
        }
        
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            if (ownerVisitor == null) {
                acquire();
            }
            if (ownerVisitor == currentVisitor) {
                writer.write(cbuf, off, len);
            } else if (!(currentVisitor instanceof SerializerVisitor)) {
                throw new SAXWriterAccessException("Illegal access to the element writer for element '" + element.getNodeName() + "' by SAX visitor '" + currentVisitor.getClass().getName() + "'.  Writer already acquired by SAX visitor '" + ownerVisitor.getClass().getName() + "'.  See Element javadocs (https://www.smooks.org).  Change Smooks visitor resource configuration.");
            }
        }

        @Override
        public void flush() throws IOException {

        }

        @Override
        public void close() throws IOException {

        }

        public Visitor getOwnerVisitor() {
            return ownerVisitor;
        }

        public Writer getWriter() {
            return writer;
        }

        public Element getElement() {
            return element;
        }

        public Visitor getCurrentVisitor() {
            return currentVisitor;
        }

        @Override
        public String toString() {
            return writer.toString();
        }
    }

    public static class WriterMemento extends AbstractVisitorMemento {
        private Writer writer;

        public WriterMemento(final Writer writer, final Visitable visitable, final Visitor visitor) {
            super(visitable, visitor);
            this.writer = writer;
        }

        @Override
        public String getId() {
            if (id == null) {
                id = visitable.getId() + "@" + getClass().getName();
            }
            return id;
        }

        public Writer getWriter() {
            return writer;
        }

        @Override
        public VisitorMemento copy() {
            return new WriterMemento(writer, visitable, visitor);
        }

        @Override
        public void restore(final VisitorMemento visitorMemento) {
            final WriterMemento writerMemento = (WriterMemento) visitorMemento;
            writer = writerMemento.getWriter();
        }
    }

    @Override
    public void visitAfter(final Element element, final ExecutionContext executionContext) {
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
        }, executionContext, element);
    }

    @Override
    public void visitBefore(final Element element, final ExecutionContext executionContext) {
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
        }, executionContext, element);
    }

    @Override
    public void visitChildText(final Element element, final ExecutionContext executionContext) {
        intercept(new Invocation<ChildrenVisitor>() {
            @Override
            public Object invoke(ChildrenVisitor visitor) {
                visitor.visitChildText(element, executionContext);
                return null;
            }

            @Override
            public Class<ChildrenVisitor> getTarget() {
                return ChildrenVisitor.class;
            }
        }, executionContext, element);
    }

    @Override
    public void visitChildElement(final Element childElement, final ExecutionContext executionContext) {
        final WriterMemento parentElementWriterMemento = new WriterMemento(new ExclusiveWriter(executionContext.get(FragmentWriter.FRAGMENT_WRITER_TYPED_KEY), (Element) childElement.getParentNode()), new NodeVisitable(childElement.getParentNode()), this);
        executionContext.getMementoCaretaker().restore(parentElementWriterMemento);

        final WriterMemento childWriterMemento;
        if (parentElementWriterMemento.getWriter() instanceof ExclusiveWriter) {
            childWriterMemento = new WriterMemento(new ExclusiveWriter(((ExclusiveWriter) parentElementWriterMemento.getWriter()).getWriter(), childElement), new NodeVisitable(childElement), this);
        } else {
            childWriterMemento = new WriterMemento(parentElementWriterMemento.getWriter(), new NodeVisitable(childElement), this);
        }

        executionContext.getMementoCaretaker().save(childWriterMemento);

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
        }, executionContext, (Element) childElement.getParentNode());
    }
    
    protected <T extends Visitor> void intercept(final Invocation<T> invocation, final ExecutionContext executionContext, final Element element) {
        final WriterMemento writerMemento = new WriterMemento(new ExclusiveWriter(executionContext.get(FragmentWriter.FRAGMENT_WRITER_TYPED_KEY), element), new NodeVisitable(element), this);
        executionContext.getMementoCaretaker().restore(writerMemento);
        if (writerMemento.getWriter() instanceof ExclusiveWriter) {
            ((ExclusiveWriter) writerMemento.getWriter()).setCurrentVisitor(getTarget().getContentHandler());
        }
        
        final Writer originalWriter = executionContext.get(FragmentWriter.FRAGMENT_WRITER_TYPED_KEY);
        executionContext.put(FragmentWriter.FRAGMENT_WRITER_TYPED_KEY, writerMemento.getWriter());
        intercept(invocation);
        executionContext.getMementoCaretaker().save(new WriterMemento(executionContext.get(FragmentWriter.FRAGMENT_WRITER_TYPED_KEY), new NodeVisitable(element), this));
        executionContext.put(FragmentWriter.FRAGMENT_WRITER_TYPED_KEY, originalWriter);
    }
}