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
import org.smooks.api.delivery.sax.StreamResultWriter;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.api.resource.visitor.dom.DOMElementVisitor;
import org.smooks.api.resource.visitor.interceptor.InterceptorVisitor;
import org.smooks.api.resource.visitor.sax.ng.ElementVisitor;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.smooks.engine.memento.SimpleVisitorMemento;
import org.smooks.io.FragmentWriter;
import org.smooks.io.Stream;
import org.w3c.dom.CharacterData;
import org.w3c.dom.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StreamResultWriterInterceptor extends AbstractInterceptorVisitor implements ElementVisitor, DOMElementVisitor, InterceptorVisitor {
    
    protected boolean isStreamResultWriter;

    @PostConstruct
    public void postConstruct() {
        isStreamResultWriter = getTarget().getContentHandler().getClass().isAnnotationPresent(StreamResultWriter.class);
    }
    
    @Override
    public void visitAfter(final Element element, final ExecutionContext executionContext) {
        if (isStreamResultWriter) {
            intercept(visitAfterInvocation, new StreamResultWriterDelegateElement(element), executionContext);
        } else {
            intercept(visitAfterInvocation, element, executionContext);
        }
    }

    @Override
    public void visitBefore(final Element element, final ExecutionContext executionContext) {
        if (isStreamResultWriter) {
            intercept(visitBeforeInvocation, new StreamResultWriterDelegateElement(element), executionContext);
        } else {
            intercept(visitBeforeInvocation, element, executionContext);
        }
    }

    @Override
    public void visitChildText(final CharacterData characterData, final ExecutionContext executionContext) {
        if (isStreamResultWriter) {
            intercept(visitChildTextInvocation, new StreamResultWriterDelegateCharacterData(characterData), executionContext);
        } else {
            intercept(visitChildTextInvocation, characterData, executionContext);
        }
    }

    @Override
    public void visitChildElement(final Element childElement, final ExecutionContext executionContext) {
        if (isStreamResultWriter) {
            intercept(visitChildElementInvocation, new StreamResultWriterDelegateElement(childElement), executionContext);
        } else {
            intercept(visitChildElementInvocation, childElement, executionContext);
        }
    }
    
    protected <T extends Visitor> void intercept(final Invocation<T> invocation, final StreamResultWriterDelegateNode streamResultWriterDelegateNode, final ExecutionContext executionContext) {
        final NodeFragment nodeFragment = new NodeFragment(streamResultWriterDelegateNode.getDelegateNode());
        executionContext.getMementoCaretaker().stash(new SimpleVisitorMemento<>(nodeFragment, this, new FragmentWriter(executionContext, nodeFragment, false)), writerMemento -> {
            try {
                writerMemento.getState().park();
            } catch (IOException e) {
                throw new SmooksException(e);
            }
            executionContext.put(Stream.STREAM_WRITER_TYPED_KEY, writerMemento.getState());
            super.intercept(invocation, streamResultWriterDelegateNode, executionContext);
            executionContext.put(Stream.STREAM_WRITER_TYPED_KEY, writerMemento.getState().getDelegateWriter());

            return writerMemento;
        });
    }

    abstract static class StreamResultWriterDelegateNode implements Node {
        private final Node node;

        StreamResultWriterDelegateNode(Node node) {
            this.node = node;
        }

        @Override
        public String getNodeName() {
            return node.getNodeName();
        }

        @Override
        public String getNodeValue() throws DOMException {
            return node.getNodeValue();
        }

        @Override
        public void setNodeValue(String nodeValue) throws DOMException {
            node.setNodeValue(nodeValue);
        }

        @Override
        public short getNodeType() {
            return node.getNodeType();
        }

        @Override
        public Node getParentNode() {
            return node.getParentNode();
        }

        @Override
        public NodeList getChildNodes() {
            return node.getChildNodes();
        }

        @Override
        public Node getFirstChild() {
            return node.getFirstChild();
        }

        @Override
        public Node getLastChild() {
            return node.getLastChild();
        }

        @Override
        public Node getPreviousSibling() {
            return node.getPreviousSibling();
        }

        @Override
        public Node getNextSibling() {
            return node.getNextSibling();
        }

        @Override
        public NamedNodeMap getAttributes() {
            return node.getAttributes();
        }

        @Override
        public Document getOwnerDocument() {
            return node.getOwnerDocument();
        }

        @Override
        public Node insertBefore(Node newChild, Node refChild) throws DOMException {
            return node.insertBefore(newChild, refChild);
        }

        @Override
        public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
            return node.replaceChild(newChild, oldChild);
        }

        @Override
        public Node removeChild(Node oldChild) throws DOMException {
            return node.removeChild(oldChild);
        }

        @Override
        public Node appendChild(Node newChild) throws DOMException {
            return node.appendChild(newChild);
        }

        @Override
        public boolean hasChildNodes() {
            return node.hasChildNodes();
        }

        @Override
        public Node cloneNode(boolean deep) {
            return node.cloneNode(deep);
        }

        @Override
        public void normalize() {
            node.normalize();
        }

        @Override
        public boolean isSupported(String feature, String version) {
            return node.isSupported(feature, version);
        }

        @Override
        public String getNamespaceURI() {
            return node.getNamespaceURI();
        }

        @Override
        public String getPrefix() {
            return node.getPrefix();
        }

        @Override
        public void setPrefix(String prefix) throws DOMException {
            node.setPrefix(prefix);
        }

        @Override
        public String getLocalName() {
            return node.getLocalName();
        }

        @Override
        public boolean hasAttributes() {
            return node.hasAttributes();
        }

        @Override
        public String getBaseURI() {
            return node.getBaseURI();
        }

        @Override
        public short compareDocumentPosition(Node other) throws DOMException {
            return node.compareDocumentPosition(other);
        }

        @Override
        public String getTextContent() throws DOMException {
            return node.getTextContent();
        }

        @Override
        public void setTextContent(String textContent) throws DOMException {
            node.setTextContent(textContent);
        }

        @Override
        public boolean isSameNode(Node other) {
            return node.isSameNode(other);
        }

        @Override
        public String lookupPrefix(String namespaceURI) {
            return node.lookupPrefix(namespaceURI);
        }

        @Override
        public boolean isDefaultNamespace(String namespaceURI) {
            return node.isDefaultNamespace(namespaceURI);
        }

        @Override
        public String lookupNamespaceURI(String prefix) {
            return node.lookupNamespaceURI(prefix);
        }

        @Override
        public boolean isEqualNode(Node arg) {
            return node.isEqualNode(arg);
        }

        @Override
        public Object getFeature(String feature, String version) {
            return node.getFeature(feature, version);
        }

        @Override
        public Object setUserData(String key, Object data, UserDataHandler handler) {
            return node.setUserData(key, data, handler);
        }

        @Override
        public Object getUserData(String key) {
            final Object userData = node.getUserData(key);
            if (NodeFragment.RESERVATIONS_USER_DATA_KEY.equals(key)) {
                final Map<Long, Object> reservedTokens = new HashMap<>((Map<Long, Object>) userData);
                reservedTokens.remove(FragmentWriter.RESERVED_WRITE_FRAGMENT_ID);
                return reservedTokens;
            } else {
                return userData;
            }
        }
        
        public Node getDelegateNode() {
            return node;
        }
    }

    static class StreamResultWriterDelegateCharacterData extends StreamResultWriterDelegateNode implements CharacterData {
        private final CharacterData characterData;

        StreamResultWriterDelegateCharacterData(CharacterData characterData) {
            super(characterData);
            this.characterData = characterData;
        }

        @Override
        public String getData() throws DOMException {
            return characterData.getData();
        }

        @Override
        public void setData(String data) throws DOMException {
            characterData.setData(data);
        }

        @Override
        public int getLength() {
            return characterData.getLength();
        }

        @Override
        public String substringData(int offset, int count) throws DOMException {
            return characterData.substringData(offset, count);
        }

        @Override
        public void appendData(String arg) throws DOMException {
            characterData.appendData(arg);
        }

        @Override
        public void insertData(int offset, String arg) throws DOMException {
            characterData.insertData(offset, arg);
        }

        @Override
        public void deleteData(int offset, int count) throws DOMException {
            characterData.deleteData(offset, count);
        }

        @Override
        public void replaceData(int offset, int count, String arg) throws DOMException {
            characterData.replaceData(offset, count, arg);
        }
    }

    static class StreamResultWriterDelegateElement extends StreamResultWriterDelegateNode implements Element {
        private final Element element;

        StreamResultWriterDelegateElement(Element element) {
            super(element);
            this.element = element;
        }

        @Override
        public String getTagName() {
            return element.getTagName();
        }

        @Override
        public String getAttribute(String name) {
            return element.getAttribute(name);
        }

        @Override
        public void setAttribute(String name, String value) throws DOMException {
            element.setAttribute(name, value);
        }

        @Override
        public void removeAttribute(String name) throws DOMException {
            element.removeAttribute(name);
        }

        @Override
        public Attr getAttributeNode(String name) {
            return element.getAttributeNode(name);
        }

        @Override
        public Attr setAttributeNode(Attr newAttr) throws DOMException {
            return element.setAttributeNode(newAttr);
        }

        @Override
        public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
            return element.removeAttributeNode(oldAttr);
        }

        @Override
        public NodeList getElementsByTagName(String name) {
            return element.getElementsByTagName(name);
        }

        @Override
        public String getAttributeNS(String namespaceURI, String localName) throws DOMException {
            return element.getAttributeNS(namespaceURI, localName);
        }

        @Override
        public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
            element.setAttributeNS(namespaceURI, qualifiedName, value);
        }

        @Override
        public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
            element.removeAttributeNS(namespaceURI, localName);
        }

        @Override
        public Attr getAttributeNodeNS(String namespaceURI, String localName) throws DOMException {
            return element.getAttributeNodeNS(namespaceURI, localName);
        }

        @Override
        public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
            return element.setAttributeNodeNS(newAttr);
        }

        @Override
        public NodeList getElementsByTagNameNS(String namespaceURI, String localName) throws DOMException {
            return element.getElementsByTagNameNS(namespaceURI, localName);
        }

        @Override
        public boolean hasAttribute(String name) {
            return element.hasAttribute(name);
        }

        @Override
        public boolean hasAttributeNS(String namespaceURI, String localName) throws DOMException {
            return element.hasAttributeNS(namespaceURI, localName);
        }

        @Override
        public TypeInfo getSchemaTypeInfo() {
            return element.getSchemaTypeInfo();
        }

        @Override
        public void setIdAttribute(String name, boolean isId) throws DOMException {
            element.setIdAttribute(name, isId);
        }

        @Override
        public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
            element.setIdAttributeNS(namespaceURI, localName, isId);
        }

        @Override
        public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
            element.setIdAttributeNode(idAttr, isId);
        }
    }
}