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
package org.smooks.engine.delivery.sax.ng;

import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.api.delivery.Filter;
import org.smooks.api.resource.visitor.SerializerVisitor;
import org.smooks.api.resource.visitor.sax.ng.ElementVisitor;
import org.smooks.io.DomSerializer;
import org.smooks.io.Stream;
import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;

import jakarta.annotation.PostConstruct;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Writer;
import java.util.Optional;

/**
 * Serializes a node to the execution sink stream.
 */
public class SimpleSerializerVisitor implements SerializerVisitor, ElementVisitor {

    protected DomSerializer domSerializer;
    protected Boolean rewriteEntities = true;

    @PostConstruct
    public void postConstruct() {
        domSerializer = new DomSerializer(false, rewriteEntities);
    }

    @Inject
    public void setRewriteEntities(@Named(Filter.ENTITIES_REWRITE) Optional<Boolean> rewriteEntities) {
        this.rewriteEntities = rewriteEntities.orElse(this.rewriteEntities);
    }

    @Override
    public void writeStartElement(Element element, Writer writer, ExecutionContext executionContext) throws IOException {
        domSerializer.writeStartElement(element, writer);
    }

    @Override
    public void writeEndElement(Element element, Writer writer, ExecutionContext executionContext) throws IOException {
        domSerializer.writeEndElement(element, writer);
    }

    @Override
    public void writeCharacterData(Node node, Writer writer, ExecutionContext executionContext) throws IOException {
        domSerializer.writeCharacterData(node, writer);
    }

    @Override
    public void writeElementComment(Comment comment, Writer writer, ExecutionContext executionContext) throws IOException {
        domSerializer.writeElementComment(comment, writer);
    }

    @Override
    public void writeElementEntityRef(EntityReference entityRef, Writer writer, ExecutionContext executionContext) throws IOException {
        domSerializer.writeElementEntityRef(entityRef, writer);
    }

    @Override
    public void writeElementCDATA(CDATASection cdata, Writer writer, ExecutionContext executionContext) throws IOException {
        domSerializer.writeElementCDATA(cdata, writer);
    }

    @Override
    public void writeElementNode(Node node, Writer writer, ExecutionContext executionContext) throws IOException {
        throw new IOException("writeElementNode not implemented yet. Node: " + node.getNodeValue() + ", node: [" + node + "]");
    }

    @Override
    public boolean writeChildElements() {
        return true;
    }

    @Override
    public void visitAfter(Element element, ExecutionContext executionContext) {
        try {
            writeEndElement(element, Stream.out(executionContext), executionContext);
        } catch (IOException e) {
            throw new SmooksException(e);
        }
    }

    @Override
    public void visitChildText(CharacterData characterData, ExecutionContext executionContext) {
        try {
            writeCharacterData(characterData, Stream.out(executionContext), executionContext);
        } catch (IOException e) {
            throw new SmooksException(e);
        }
    }

    @Override
    public void visitChildElement(Element childElement, ExecutionContext executionContext) {

    }

    @Override
    public void visitBefore(Element element, ExecutionContext executionContext) {
        try {
            writeStartElement(element, Stream.out(executionContext), executionContext);
        } catch (IOException e) {
            throw new SmooksException(e);
        }
    }
}
