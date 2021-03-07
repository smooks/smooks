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
package org.smooks.engine.delivery.sax;

import org.smooks.api.SmooksException;
import org.smooks.api.ExecutionContext;
import org.smooks.api.delivery.Filter;
import org.smooks.api.bean.context.BeanContext;
import org.smooks.api.delivery.sax.SAXElement;
import org.smooks.api.delivery.sax.SAXText;
import org.smooks.api.resource.visitor.sax.SAXElementVisitor;
import org.smooks.api.resource.visitor.sax.SAXVisitor;
import org.smooks.support.SAXElementWriterUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Optional;

/**
 * Default Serializer for SAX Filtering.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DefaultSAXElementSerializer implements SAXElementVisitor {

    private SAXVisitor writerOwner = this;
    private boolean rewriteEntities = true;

    public void setWriterOwner(SAXVisitor writerOwner) {
        this.writerOwner = writerOwner;
    }

    @Inject
    public void setRewriteEntities(@Named(Filter.ENTITIES_REWRITE) Optional<Boolean> rewriteEntities) {
        this.rewriteEntities = rewriteEntities.orElse(this.rewriteEntities);
    }

    @Override
    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        // Do nothing here apart from acquiring ownership of the element writer.
        // See is there any child text/elements first...
        element.getWriter(writerOwner);
    }

    @Override
    public void onChildText(SAXElement element, SAXText text, ExecutionContext executionContext) throws SmooksException, IOException {
        writeStartElement(element, executionContext.getBeanContext());
        if(element.isWriterOwner(writerOwner)) {
            text.toWriter(element.getWriter(writerOwner), rewriteEntities);
        }
    }

    @Override
    public void onChildElement(SAXElement element, SAXElement childElement, ExecutionContext executionContext) throws SmooksException, IOException {
        writeStartElement(element, executionContext.getBeanContext());
        // The child element is responsible for writing itself...
    }

    @Override
    public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        writeEndElement(element, executionContext.getBeanContext());
    }

    public void writeStartElement(SAXElement element, BeanContext beanContext) throws IOException {
        // We set a flag in the cache so as to mark the fact that the start element has been writen
        if(element.isWriterOwner(writerOwner)) {
            if(!isStartWritten(element)) {
                element.setCache(this, true);
                writeStart(element, beanContext);
            }
        }
    }

    public void writeEndElement(SAXElement element, BeanContext beanContext) throws IOException {
        if(element.isWriterOwner(writerOwner)) {
            writeEnd(element, beanContext);
        }
    }

    protected void writeStart(SAXElement element, BeanContext beanContext) throws IOException {
        SAXElementWriterUtil.writeStartElement(element, element.getWriter(writerOwner), rewriteEntities);
    }

    protected void writeEnd(SAXElement element, BeanContext beanContext) throws IOException {
        if(!isStartWritten(element)) {
            // It's an empty element...
            SAXElementWriterUtil.writeEmptyElement(element, element.getWriter(writerOwner), rewriteEntities);
        } else {
            SAXElementWriterUtil.writeEndElement(element, element.getWriter(writerOwner));
        }
    }

    public boolean isStartWritten(SAXElement element) {
        return element.getCache(this) != null;
    }
}
