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
package org.smooks.engine.delivery.dom.serialize;

import org.smooks.api.ExecutionContext;
import org.smooks.api.delivery.Filter;
import org.smooks.io.DomToXmlWriter;
import org.w3c.dom.*;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Writer;
import java.util.Optional;

public class DefaultDOMSerializerVisitor implements DOMSerializerVisitor {
    protected DomToXmlWriter domToXmlWriter;
    protected Boolean closeEmptyElements = false;
    protected Boolean rewriteEntities = true;

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
}
